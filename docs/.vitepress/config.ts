import { defineConfig } from 'vitepress'

// GitHub Pages 会把站点挂在 /code-to-app/ 下。
// 若绑定了自定义域名，用 DOCS_BASE=/ 覆盖：DOCS_BASE=/ npm run build
const base = process.env.DOCS_BASE || '/code-to-app/'

export default defineConfig({
  base,
  lang: 'zh-Hans',
  title: 'CodeToApp',
  description:
    '设备端 APK 工坊：fork+exec 真实服务器运行时、加固的网络栈、可发布的加固包，全程不需要电脑。',

  head: [
    ['meta', { name: 'theme-color', content: '#0b0d12' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'CodeToApp — 设备端 APK 工坊' }],
    ['meta', { property: 'og:description', content: '在手机上把源码、站点、运行时直接打包成 APK。' }]
  ],

  themeConfig: {
    logo: '/logo.svg',

    nav: [
      { text: '指南', link: '/guide/', activeMatch: '/guide/' },
      { text: '开发者', link: '/developer/', activeMatch: '/developer/' },
      {
        text: 'v1.0.0-beta2',
        items: [
          {
            text: '更新日志',
            link: 'https://github.com/benyeung08/code-to-app/releases'
          },
          {
            text: '下载 APK',
            link: 'https://github.com/benyeung08/code-to-app/raw/apk/builds/latest.apk'
          }
        ]
      }
    ],

    sidebar: {
      '/guide/': [
        {
          text: '开始',
          items: [
            { text: '简介', link: '/guide/' },
            { text: '快速上手', link: '/guide/getting-started' },
            { text: '应用类型', link: '/guide/app-types' },
            { text: '构建与导出', link: '/guide/build-apk' }
          ]
        }
      ],
      '/developer/': [
        {
          text: '开发者',
          items: [
            { text: '概览', link: '/developer/' },
            { text: '导出流水线', link: '/developer/export-pipeline' },
            { text: '架构', link: '/developer/architecture' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/benyeung08/code-to-app' }
    ],

    footer: {
      message: '基于 WebToApp 改造 · 以 MIT 授权发布',
      copyright: 'Copyright © 2026 benyeung08'
    },

    search: {
      provider: 'local'
    },

    outline: {
      level: [2, 3]
    },

    editLink: {
      pattern: 'https://github.com/benyeung08/code-to-app/edit/main/docs/:path',
      text: '在 GitHub 上编辑此页'
    },

    lastUpdated: {
      text: '最后更新于'
    }
  },

  markdown: {
    lineNumbers: false
  }
})
