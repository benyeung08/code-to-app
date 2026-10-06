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

BUILD FAILED in 3m 31s
51 actionable tasks: 50 executed, 1 from cache
```
**编译错误 (1)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CreateCodeToAppScreen.kt:278:43 Unresolved reference 'Edit'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 3m 31s
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

BUILD FAILED in 1m 34s
61 actionable tasks: 10 executed, 1 from cache, 50 up-to-date
```
**编译错误 (1)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CreateCodeToAppScreen.kt:278:43 Unresolved reference 'Edit'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 1m 34s
```
