## Gradle 报错摘要

### compile-app.log
**What went wrong**
```
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileStandardDebugKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

BUILD FAILED in 2m 32s
51 actionable tasks: 50 executed, 1 from cache
```
**编译错误 (28)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:4:17 Unresolved reference 'test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:5:26 Unresolved reference 'truth'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:23:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:24:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:25:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:26:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:27:12 Unresolved reference 'robolectric'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:28:12 Unresolved reference 'robolectric'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:30:2 Unresolved reference 'RunWith'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:30:10 Unresolved reference 'RobolectricTestRunner'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:31:2 Unresolved reference 'Config'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:34:6 Unresolved reference 'Rule'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:35:38 Unresolved reference 'KoinCleanupRule'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:37:10 Unresolved reference 'Rule'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:38:16 Unresolved reference 'TemporaryFolder'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:41:17 Unresolved reference 'ApplicationProvider'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:43:6 Unresolved reference 'Test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:49:13 Unresolved reference 'assertThat'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:63:9 Unresolved reference 'assertThat'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:72:74 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:72:80 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:77:65 Unresolved reference 'absolutePath'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:89:49 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:89:55 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:97:44 Unresolved reference 'absolutePath'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:106:71 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:106:77 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:113:69 Unresolved reference 'absolutePath'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 2m 32s
```

### compile-shell.log
**What went wrong**
```
```
**编译错误 (0)**
```
```
**失败任务 (0)**
```
```

### unit-test.log
**What went wrong**
```
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileStandardDebugKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

BUILD FAILED in 52s
61 actionable tasks: 10 executed, 1 from cache, 50 up-to-date
```
**编译错误 (28)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:4:17 Unresolved reference 'test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:5:26 Unresolved reference 'truth'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:23:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:24:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:25:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:26:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:27:12 Unresolved reference 'robolectric'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:28:12 Unresolved reference 'robolectric'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:30:2 Unresolved reference 'RunWith'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:30:10 Unresolved reference 'RobolectricTestRunner'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:31:2 Unresolved reference 'Config'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:34:6 Unresolved reference 'Rule'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:35:38 Unresolved reference 'KoinCleanupRule'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:37:10 Unresolved reference 'Rule'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:38:16 Unresolved reference 'TemporaryFolder'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:41:17 Unresolved reference 'ApplicationProvider'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:43:6 Unresolved reference 'Test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:49:13 Unresolved reference 'assertThat'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:63:9 Unresolved reference 'assertThat'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:72:74 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:72:80 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:77:65 Unresolved reference 'absolutePath'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:89:49 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:89:55 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:97:44 Unresolved reference 'absolutePath'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:106:71 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:106:77 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:113:69 Unresolved reference 'absolutePath'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 52s
```
