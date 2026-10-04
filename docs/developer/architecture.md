---
title: 架构
---

# 架构

## 分层

```text
ui/             Jetpack Compose 界面（screens / components / theme）
  ├── screens/  各个页面
  ├── components/ 复用组件
  └── viewmodel/ 状态容器

core/           业务逻辑（与 UI 无关）
  ├── apkbuilder/   导出流水线
  ├── appmodifier/  克隆与改名
  ├── update/       检查更新与版本历史
  ├── i18n/         十种语言字符串
  ├── market/       模块市场
  └── stats/        运行时统计

data/           数据层
  ├── model/    Room 实体与配置
  ├── dao/      数据访问
  └── repository/ 仓库
```

## 状态管理

Compose + ViewModel + StateFlow。UI 层不直接碰数据库，一律经过 `MainViewModel`。

## 多语言

字符串集中在 `core/i18n/`，按 `Strings.lang` 分发：

```kotlin
val appName: String get() = when (Strings.lang) {
    AppLanguage.CHINESE -> "CodeToApp"
    AppLanguage.ENGLISH -> "CodeToApp"
    // ...
}
```

::: warning 一致性测试
`AppStringsResourceConsistencyTest` 会拒绝**任何本地化 key** 进入 `values/strings.xml`。
那个文件只允许放 `translatable="false"` 的资源，也不允许建 `values-*` 目录。
:::

## 应用类型

`AppType` 是一个枚举，在 `data/model/WebApp.kt`。

::: tip 加新类型要注意
Kotlin 对枚举的 `when` 做**穷尽检查**。新增一个枚举值后，
所有没有 `else` 分支的 `when` 都会编译失败。

加 `CODETOAPP` 时踩过这个坑：项目里 15 个文件共 175 处分支需要补，
漏掉一个就红。建议一次改完再提交。
:::

## 版本解析

`UpdateChecker.Version` 支持 semver 与预发布后缀：

```kotlin
data class Version(
    val major: Int, val minor: Int, val patch: Int,
    val pre: String = ""
)
```

排序规则：正式版 > 预发布版，`beta1 < beta2`。

::: danger 别用空格写版本号
`versionName = "1.0.0 beta2"`（空格）会被 `Version.parse()` 解析成
patch 段 `"0 beta2"` → `toIntOrNull()` 返回 null → 归零 → 变成**正式版 1.0.0**。

正式版比 `1.0.0-beta2` 大，于是更新提示永远不触发。必须用连字符：`"1.0.0-beta2"`。
:::
