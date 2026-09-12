# fzjx

DataCollect 数据解析平台（弹道特征点提取 / 仿真数据解析）

## 目录

| 目录 | 说明 |
|------|------|
| `data-collect-ui` | 前端 Vue3 + Vite（含 `public/feature-extract` 自包含工具页） |
| `data-collect-web` | 后端 Spring Boot（REST + MessagePack 解析 + 静态资源） |

## 前端

```bash
cd data-collect-ui
npm install
npm run dev      # 开发，/api 代理到 127.0.0.1:8080
npm run build    # 构建到 dist/
```

构建后需将 `dist/*` 覆盖到 `data-collect-web/src/main/resources/static/`，再由后端统一托管。

## 后端

```bash
cd data-collect-web
mvn -s settings.xml spring-boot:run
```

默认端口 `8080`。连接数据库后可在「弹道特征点提取」页解析三主题关联数据，并使用导弹剖面解析（距离坐标 / Excel 导出）。
