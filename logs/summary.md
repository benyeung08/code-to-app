## Gradle 报错摘要

### compile-app.log
**What went wrong**
```
```
**编译错误 (0)**
```
```
**失败任务 (0)**
```
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
Execution failed for task ':app:compileStandardDebugUnitTestKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

BUILD FAILED in 57s
68 actionable tasks: 16 executed, 1 from cache, 51 up-to-date
```
**编译错误 (1)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/test/java/com/webtoapp/core/apkbuilder/AppTypePreflightSmokeTest.kt:67:16 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugUnitTestKotlin FAILED
Execution failed for task ':app:compileStandardDebugUnitTestKotlin'.
BUILD FAILED in 57s
```
