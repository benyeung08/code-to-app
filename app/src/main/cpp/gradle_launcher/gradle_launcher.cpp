
/**
 * gradle_launcher.cpp
 * 複用 web-to-app 現有的 node_launcher / go_exec_loader 架構
 * 作用：在 :native_build 獨立進程中 fork + exec gradlew
 * 環境隔離，避免 SELinux W^X 攔截主進程
 */

#include <jni.h>
#include <unistd.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "GradleLauncher"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

// 啟動 gradle 構建
// args: projectRoot, gradleTask, jdkDir, sdkDir, gradleHome
JNIEXPORT jint JNICALL
Java_com_webtoapp_core_runtime_NativeBuildRuntime_nativeLaunchGradle(
        JNIEnv *env,
        jobject /* this */,
        jstring jProjectRoot,
        jstring jTask,
        jstring jJdkDir,
        jstring jSdkDir,
        jstring jGradleHome
) {
    const char* projectRoot = env->GetStringUTFChars(jProjectRoot, nullptr);
    const char* task = env->GetStringUTFChars(jTask, nullptr);
    const char* jdkDir = env->GetStringUTFChars(jJdkDir, nullptr);
    const char* sdkDir = env->GetStringUTFChars(jSdkDir, nullptr);
    const char* gradleHome = env->GetStringUTFChars(jGradleHome, nullptr);

    LOGI("Project: %s Task: %s JDK: %s SDK: %s", projectRoot, task, jdkDir, sdkDir);

    pid_t pid = fork();
    if (pid < 0) {
        LOGE("fork failed");
        return -1;
    }

    if (pid == 0) {
        // Child process - 執行 gradle
        // 設置環境變數
        setenv("JAVA_HOME", jdkDir, 1);
        setenv("ANDROID_HOME", sdkDir, 1);
        setenv("ANDROID_SDK_ROOT", sdkDir, 1);
        setenv("GRADLE_HOME", gradleHome, 1);

        // 構造 PATH: jdk/bin:gradle/bin:原PATH
        std::string newPath = std::string(jdkDir) + "/bin:" + gradleHome + "/bin:" + getenv("PATH");
        setenv("PATH", newPath.c_str(), 1);

        // 跳轉到專案目錄
        if (chdir(projectRoot) != 0) {
            LOGE("chdir to %s failed", projectRoot);
            _exit(127);
        }

        // 構造 gradlew 命令
        // 優先用 ./gradlew，如果沒有則用 gradle
        std::string gradlew = std::string(projectRoot) + "/gradlew";
        const char* execPath = access(gradlew.c_str(), X_OK) == 0 ? gradlew.c_str() : "gradle";

        // 參數: gradlew assembleDebug --stacktrace --no-daemon
        const char* argv[] = {
            execPath,
            task,
            "--stacktrace",
            "--no-daemon",
            "-Dorg.gradle.jvmargs=-Xmx1024m",
            nullptr
        };

        LOGI("Executing: %s %s", execPath, task);
        execv(execPath, (char* const*)argv);

        // 如果 execv 失敗
        LOGE("execv failed for %s", execPath);
        _exit(127);
    } else {
        // Parent process - 等待子進程
        int status = 0;
        waitpid(pid, &status, 0);
        LOGI("Gradle process exited with status %d", WEXITSTATUS(status));

        env->ReleaseStringUTFChars(jProjectRoot, projectRoot);
        env->ReleaseStringUTFChars(jTask, task);
        env->ReleaseStringUTFChars(jJdkDir, jdkDir);
        env->ReleaseStringUTFChars(jSdkDir, sdkDir);
        env->ReleaseStringUTFChars(jGradleHome, gradleHome);

        return WEXITSTATUS(status);
    }
}

// 獲取構建日誌的 pipe (用於實時日誌回傳)
JNIEXPORT jstring JNICALL
Java_com_webtoapp_core_runtime_NativeBuildRuntime_getBuildLogPath(
        JNIEnv *env,
        jobject /* this */,
        jstring jProjectRoot
) {
    const char* root = env->GetStringUTFChars(jProjectRoot, nullptr);
    std::string logPath = std::string(root) + "/app/build/outputs/logs/build.log";
    env->ReleaseStringUTFChars(jProjectRoot, root);
    return env->NewStringUTF(logPath.c_str());
}

}
