# 数据库导出（DataCollect 平台 4 个库）

本目录是项目所用 **4 个库** 的 `mysqldump` 导出，用于在另一台电脑上还原环境。

导出源：MySQL **5.7.40** · `127.0.0.1:3306` · 用户 `root` · 字符集 `utf8mb4`

## 文件清单

| 文件 | 大小 | 内容 |
|------|------|------|
| `all-databases.sql` | ~166 KB | **4 个库合并，推荐用这个** |
| `big_sample_db.sql` | ~3 KB | 大样本库（仿真方案） |
| `data_collect.sql` | ~157 KB | 采集库（任务数据） |
| `modelmanager.sql` | ~6 KB | 模型库（主题与字段定义） |
| `sim_control_db_copy.sql` | ~3 KB | 仿真控制库（样本 / 任务关联） |

导出内容：**建库 + 建表 + 数据**（含 `CREATE DATABASE`、`DROP TABLE IF EXISTS`、`LOCK TABLES`），单库文件与合并文件任选其一导入即可。

导出参数：`--single-transaction --quick --hex-blob --routines --events --triggers --default-character-set=utf8mb4`。
其中 **`--hex-blob` 必须保留**：`data_collect.t*` 的 `msg_info` 是 MessagePack 二进制（BLOB），若按普通字符串导出，导入时会因字符集转换把二进制字节替换成 `EFBFBD`（U+FFFD）而损坏。导出文件里 BLOB 形如 `0x93A7E5AF...`，与字符集无关、可原样还原。

## 数据快照（导出时）

| 库.表 | 行数 |
|------|------|
| `data_collect.t123` / `t456` / `t789` | 258 / 255 / 255 |
| `data_collect.topic_config` | 9 |
| `modelmanager.topic` / `simulatmutual` / `dataitem` / `compdataitem` | 5 / 5 / 28 / 1 |
| `sim_control_db_copy.task_design_example` / `task_example_correlation` | 2 / 3 |
| `big_sample_db.experiment_design` / `big_sample` | 2 / 1 |

其中 `modelmanager.dataitem` 已包含发射主题的 **`entityType(导弹类型)`** 字段（`simulatmutual_id = 10`），这是页面「导弹类型」列的数据来源。

## 导入步骤（新电脑）

前置：已安装 MySQL（5.7 或 8.0 均可），字符集 `utf8mb4`。

```bash
# 1) 合并导入（推荐）
mysql -h127.0.0.1 -P3306 -uroot -p < all-databases.sql

# 或 2) 分库导入
mysql -h127.0.0.1 -uroot -p < data_collect.sql
mysql -h127.0.0.1 -uroot -p < modelmanager.sql
mysql -h127.0.0.1 -uroot -p < sim_control_db_copy.sql
mysql -h127.0.0.1 -uroot -p < big_sample_db.sql
```

Windows PowerShell 下如遇 `<` 重定向不支持，可改用：

```powershell
Get-Content .\all-databases.sql -Raw -Encoding UTF8 | mysql -h127.0.0.1 -uroot -p
# 或
cmd /c "mysql -h127.0.0.1 -uroot -p < all-databases.sql"
```

## 导入后校验

```sql
SHOW DATABASES LIKE '%sample%';
SELECT COUNT(*) FROM data_collect.t123;                    -- 258
SELECT COUNT(*) FROM modelmanager.dataitem;                -- 28
SELECT b.meaning, b.name FROM modelmanager.simulatmutual a
  JOIN modelmanager.dataitem b ON a.id = b.simulatmutual_id
 WHERE a.name = 'MISSILELAUNCHTIME';                       -- 应含 导弹类型/entityType
```

## 导入后在项目里使用

1. 启动后端：`cd data-collect-web && mvn -s settings.xml spring-boot:run`（默认 8080）
2. 页面顶部「连接」表单填：主机 / 端口 / 用户 / 密码 / 采集库 `data_collect` / 模型库 `modelmanager`
3. 打开「弹道特征点提取」→ 选表（`/api/tables` 只返回 `^t[0-9]+$` 的前 2 张，即 `t123`、`t456`）→ 点「解析数据」

## 注意

- **已验证**：用本目录的 `all-databases.sql` 做过一次完整回灌（导入到临时库名），与源库逐行比对：`t123`(258 行) / `t456`(255) / `t789`(255) 的 `msg_info` **BLOB 逐行 MD5 完全一致**，`modelmanager.dataitem` 字段零差异，随后临时库已删除。
- 导出为**快照**：本机之后新增的字段/数据不会自动同步，需要重新执行导出。
- 重新导出时**务必带上 `--hex-blob`**，否则二进制会被字符集转换破坏（见上文）。
- 如目标机器用 MySQL 8.0：本导出基于 5.7（无 `utf8mb4_0900_ai_ci` 等 8.0 专有语法），一般可直接导入；若报默认字符集相关错误，在导入前给目标实例设置 `character_set_server=utf8mb4`。
- 这几个库里没有视图/存储过程/事件，导出已带 `--routines --events --triggers` 参数，导入时无额外依赖。
