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

## 关联数据库

本项目使用 **4 个库**（本机开发实例：MySQL 5.7.40 · `127.0.0.1:3306` · 用户 `root`，默认密码见 `data-collect-ui/src/store.js` 连接表单；后端连接由页面「连接」表单传入，可指向其他 host/port）。

| 库 | 角色 | 表 | 说明 |
|------|------|------|------|
| `data_collect` | 采集库（业务数据） | `t123`、`t456`、`t789`、`topic_config` | 任务采集数据，一行一个主题报文（MessagePack BLOB） |
| `modelmanager` | 模型库（主题与字段定义） | `topic`、`simulatmutual`、`dataitem`、`compdataitem` | 主题注册 + 结构字段，解析时按这里的 `meaning` 映射成中文键 |
| `sim_control_db_copy` | 仿真控制库（样本/任务） | `task_design_example`、`task_example_correlation` | 任务设计样本与「推演任务 ↔ 样本」关联 |
| `big_sample_db` | 大样本库（仿真方案） | `experiment_design`、`big_sample` | 仿真方案与大样本 |

### 表数据要点

- `data_collect.t123 / t456 / t789`：`id + topic + msg_info(BLOB)`，结构相同、各约 255~258 行，每表 10 个主题。
  弹道相关主题：`TOPIC_QY_YJ18BTrajectoryData` 125 行（5 枚弹 × 25 轨迹点）、`TOPIC_MSG_HIT` 5 行、`TOPIC_QY_MISSILELAUNCHTIME` 5 行；
  其余主题（`TOPIC_MDL_CREATE` / `TOPIC_SIM_CONTROL` / `TOPIC_MSG_TOPIC_AirForce_TakeOffReport` / `..._DetectReport` / `..._DataFuse_MSG` / `..._LAUNCH_MISSILE_FAILD`）各 20 行。
- `data_collect.topic_config`：可勾选主题白名单（9 条）。
- `modelmanager.topic`：主题注册（`HIT` / `ATTACK` / `YJ18BTrajectoryData` / `MISSILELAUNCHTIME` / `TEST`）。
- `modelmanager.simulatmutual` + `dataitem`：主题结构主表与属性字段。`simulatmutual`(5 行)= 主题类型名（HIT / ATTACK / YJ18BTrajectoryData / MISSILELAUNCHTIME / TEST）；`dataitem`(28 行) 的列为 `simulatmutual_id` / `meaning`（中文含义，解析时作为键）/ `name`（字段名）/ `type` / `instruction_type` / `compdata_id`。
  各主题字段数：HIT(1)=3、**ATTACK(2)=5**（发射平台UID / 发射平台类型 / 导弹UID / **导弹类型 missileType** / 发射时间）、YJ18BTrajectoryData(9)=13、**MISSILELAUNCHTIME(10)=3**、TEST(11)=2，另有 2 行 `simulatmutual_id IS NULL`（平台类型 ptType / 平台id ptId）。
  **发射主题（id=10）字段为 `entityUID(弹id)` / `platformType(平台类型)` / `entityType(导弹类型)`** —— 页面「导弹类型」列即由此字段映射而来。
- `modelmanager.compdataitem`：1 行（`messageData`）。
- `sim_control_db_copy.task_design_example`：样本1、样本2；`task_example_correlation`：`deduce_task_id` 123→样本1、456→样本1、789→样本2。
- `big_sample_db.experiment_design`：仿真方案1(2026-09-06)、仿真方案2(2026-09-07)；`big_sample`：样本推演 → 方案1。

### 关联关系

```
big_sample_db.experiment_design (仿真方案)
        └── big_sample (大样本)

sim_control_db_copy.task_design_example (样本1/2)
        └── task_example_correlation (deduce_task_id = 123 / 456 / 789)
                    └──> data_collect.t123 / t456 / t789   ← 任务ID 即表名后缀（t+任务ID）
                                └── 按 modelmanager.topic + simulatmutual/dataitem 的字段定义解析
                                            └──> 页面：弹道特征点提取 / 导弹剖面解析
```

### 数据库导出（换机部署）

`db-export/` 目录是 4 个库的 `mysqldump` 导出（**含 `CREATE DATABASE`**，UTF-8，带 `DROP TABLE IF EXISTS`，可直接在另一台机器导入）：

| 文件 | 内容 |
|------|------|
| `all-databases.sql` | 4 个库合并，**推荐用这个**（约 166 KB） |
| `big_sample_db.sql` / `data_collect.sql` / `sim_control_db_copy.sql` / `modelmanager.sql` | 单库导出 |

导入命令与校验步骤见 `db-export/README.md`。导出带 `--hex-blob`（`msg_info` 是 MessagePack BLOB，缺这个参数导入会损坏二进制），并已做回灌验证（源库与导入库逐行 MD5 一致）。注意：导出是**快照**，导入后新机器上的数据即为此版本，之后本机再改动需重新导出。

### 代码引用位置

- 后端 `data-collect-web/src/main/java/com/casic/assess/dc/DataCollectService.java`：`dataDb = "data_collect"`、`modelDb = "modelmanager"`，JDBC URL 由 `/api/connect` 传入。
- 前端 `data-collect-ui/src/store.js`：默认 `127.0.0.1:3306 / root / root / data_collect / modelmanager`。
- `/api/tables` 只返回表名匹配 `^t[0-9]+$` 的**前 2 张**（`LIMIT 2`）→ 页面左侧目前只显示 `t123`、`t456`。

### 业务说明（弹道特征点提取）

- 读三主题并按弹id 关联：`TOPIC_MSG_HIT` / `TOPIC_QY_MISSILELAUNCHTIME` / `TOPIC_QY_YJ18BTrajectoryData`；`isHit=false` 的弹不返回。
- 「导弹类型」取自发射主题的 `entityType(导弹类型)` 字段；三张任务表的发射行当前均已带类型值（中程弹道导弹 / 近程弹道导弹 / 高超音速导弹）。
- 若模型库结构缺该字段，后端 `structureOrFallback()` 会兜底补上；造数 `rep.missileType` 已赋值，重新造数不会再丢类型。
- 造数（会清空该表三主题后重建 5 枚弹）：`POST /api/seed`，JSON body `{"table":"t123"}`。
