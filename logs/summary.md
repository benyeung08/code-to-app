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

BUILD FAILED in 2m 18s
51 actionable tasks: 50 executed, 1 from cache
```
**编译错误 (19)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:3:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:4:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:5:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:6:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:7:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:8:12 Unresolved reference 'robolectric'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:10:2 Unresolved reference 'RunWith'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:10:10 Unresolved reference 'RobolectricTestRunner'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:13:6 Unresolved reference 'Test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:26:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:27:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:28:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:29:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:30:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:31:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:32:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:33:9 Unresolved reference 'assertNull'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:36:6 Unresolved reference 'Test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:38:9 Unresolved reference 'assertTrue'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 2m 18s
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

BUILD FAILED in 53s
61 actionable tasks: 10 executed, 1 from cache, 50 up-to-date
```
**编译错误 (19)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:3:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:4:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:5:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:6:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:7:12 Unresolved reference 'junit'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:8:12 Unresolved reference 'robolectric'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:10:2 Unresolved reference 'RunWith'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:10:10 Unresolved reference 'RobolectricTestRunner'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:13:6 Unresolved reference 'Test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:26:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:27:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:28:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:29:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:30:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:31:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:32:9 Unresolved reference 'assertEquals'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:33:9 Unresolved reference 'assertNull'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:36:6 Unresolved reference 'Test'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/update/UpdateCheckerAuthorReposTest.kt:38:9 Unresolved reference 'assertTrue'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 53s
```
