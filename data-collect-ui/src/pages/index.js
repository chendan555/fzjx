/**
 * 页面注册表 —— 以后加页面只需两步：
 *   1) 在 src/pages/ 下新建一个 .vue 页面组件
 *   2) 在这里 import 并往 PAGES 数组加一条菜单配置
 * 图标是内联 SVG 字符串（stroke 用 currentColor 跟随文字颜色）。
 */
import SimParsePage from './SimParsePage.vue'
import FeatureExtractPage from './FeatureExtractPage.vue'

export const PAGES = [
    {
        key: 'sim-parse',
        label: '仿真数据解析',
        component: SimParsePage,
        icon: '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M1 8h3l1.5-4 3 8 2-6 1 2h3.5"/></svg>'
    },
    {
        key: 'feature-extract',
        label: '弹道特征点提取',
        component: FeatureExtractPage,
        icon: '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 13.5 5.5 3l3.5 8 2.5-5L14 10.5"/><circle cx="8" cy="8" r="6.2"/></svg>'
    }
    // ← 以后加页面在这加一行，例如：
    // { key: 'xxx', label: 'XXX', component: XxxPage, icon: '<svg .../>' }
]

export function getPage(key) {
    return PAGES.find(p => p.key === key) || PAGES[0]
}
