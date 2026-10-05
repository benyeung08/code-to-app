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
Execution failed for task ':app:testStandardDebugUnitTest'.
> There were failing tests. See the report at: file:///home/runner/work/code-to-app/code-to-app/app/build/reports/tests/testStandardDebugUnitTest/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 2m 7s
70 actionable tasks: 18 executed, 1 from cache, 51 up-to-date
```
**编译错误 (0)**
```
```
**失败任务 (9)**
```
UpdateCheckerAuthorReposTest > parseAuthorRepos filters forks and the app repo itself FAILED
DownloadBridgeCustomLocationTest > custom mode routes media blob into the configured directory FAILED
DownloadBridgeCustomLocationTest > app private mode keeps media blob inside the app directory FAILED
DownloadBridgeCustomLocationTest > custom mode routes chunked media download into the configured directory FAILED
WebAppModelExtendedTest > AppType has all expected values FAILED
> Task :app:testStandardDebugUnitTest FAILED
Execution failed for task ':app:testStandardDebugUnitTest'.
> There were failing tests. See the report at: file:///home/runner/work/code-to-app/code-to-app/app/build/reports/tests/testStandardDebugUnitTest/index.html
BUILD FAILED in 2m 7s
```
