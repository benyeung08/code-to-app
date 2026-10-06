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

BUILD FAILED in 3m 20s
51 actionable tasks: 50 executed, 1 from cache
```
**编译错误 (6)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppBuildScreen.kt:113:22 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppBuildScreen.kt:114:39 Unresolved reference 'kotlinx'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppBuildScreen.kt:118:42 Argument type mismatch: actual type is 'Any', but 'WebApp' was expected.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppRunScreen.kt:21:43 Cannot access 'val RowColumnParentData?.weight: Float': it is internal in file.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppWorkspaceScreen.kt:215:67 Unresolved reference 'sp'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppWorkspaceScreen.kt:220:63 Unresolved reference 'sp'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 3m 20s
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

BUILD FAILED in 1m 29s
61 actionable tasks: 10 executed, 1 from cache, 50 up-to-date
```
**编译错误 (6)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppBuildScreen.kt:113:22 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppBuildScreen.kt:114:39 Unresolved reference 'kotlinx'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppBuildScreen.kt:118:42 Argument type mismatch: actual type is 'Any', but 'WebApp' was expected.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppRunScreen.kt:21:43 Cannot access 'val RowColumnParentData?.weight: Float': it is internal in file.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppWorkspaceScreen.kt:215:67 Unresolved reference 'sp'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/CodeToAppWorkspaceScreen.kt:220:63 Unresolved reference 'sp'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 1m 29s
```
