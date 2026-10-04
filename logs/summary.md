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

BUILD FAILED in 2m 57s
51 actionable tasks: 50 executed, 1 from cache
```
**编译错误 (35)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt:4211:47 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt:5108:89 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/Strings.kt:166:51 Unresolved reference 'appTypeCodeToApp'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1432:35 Return type mismatch: expected 'String', actual 'Any'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:5 Syntax error: Expecting an expression.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:5 Syntax error: Expecting '->'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:9 Unresolved reference 'appTypeCodeToApp'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:25 Syntax error: Expecting an expression.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:25 Syntax error: Expecting '->'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:27 Variable expected.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:34 Unresolved reference 'get'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:38 Syntax error: Expecting an expression.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/navigation/AppNavigation.kt:189:29 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/navigation/AppNavigation.kt:198:75 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1132:51 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1166:46 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1172:71 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1177:69 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1295:29 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1301:75 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1306:73 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1437:25 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1458:51 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1478:49 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3078:69 Use '||' instead of commas in conditions of 'when' without a subject.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3078:71 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3080:36 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3081:36 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3089:66 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3090:72 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3108:55 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3111:54 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3121:59 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3123:52 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3128:36 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 2m 57s
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

BUILD FAILED in 1m 17s
61 actionable tasks: 10 executed, 1 from cache, 50 up-to-date
```
**编译错误 (35)**
```
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt:4211:47 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt:5108:89 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/Strings.kt:166:51 Unresolved reference 'appTypeCodeToApp'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1432:35 Return type mismatch: expected 'String', actual 'Any'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:5 Syntax error: Expecting an expression.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:5 Syntax error: Expecting '->'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:9 Unresolved reference 'appTypeCodeToApp'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:25 Syntax error: Expecting an expression.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:25 Syntax error: Expecting '->'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:27 Variable expected.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:34 Unresolved reference 'get'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/core/i18n/StringsA.kt:1433:38 Syntax error: Expecting an expression.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/navigation/AppNavigation.kt:189:29 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/navigation/AppNavigation.kt:198:75 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1132:51 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1166:46 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1172:71 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1177:69 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1295:29 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1301:75 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1306:73 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1437:25 'when' expression must be exhaustive. Add the 'CODETOAPP' branch or an 'else' branch.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1458:51 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/screens/HomeScreen.kt:1478:49 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3078:69 Use '||' instead of commas in conditions of 'when' without a subject.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3078:71 Unresolved reference 'AppType'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3080:36 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3081:36 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3089:66 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3090:72 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3108:55 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3111:54 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3121:59 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3123:52 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
e: file:///home/runner/work/code-to-app/code-to-app/app/src/main/java/com/webtoapp/ui/webview/WebViewActivity.kt:3128:36 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'WebApp?'.
```
**失败任务 (3)**
```
> Task :app:compileStandardDebugKotlin FAILED
Execution failed for task ':app:compileStandardDebugKotlin'.
BUILD FAILED in 1m 17s
```
