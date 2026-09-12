package com.casic.assess.dc;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ExtensionValue;
import org.msgpack.value.IntegerValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 业务服务：连接两个库（采集库 data_collect + 模型库 modelmanager）、
 * 扫描 t 表、读取主题/字段结构、MessagePack 通用解包并映射 key-value。
 */
@Service
public class DataCollectService {

    private volatile Connection conn;
    private volatile String dataDb = "data_collect";
    private volatile String modelDb = "modelmanager";

    /** 连接测试 + 建立连接（失败抛出异常由调用方处理） */
    public synchronized void connect(String host, String port, String user, String pass, String db, String model) throws SQLException {
        if (conn != null) {
            try { conn.close(); } catch (Exception ignore) { }
            conn = null;
        }
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        Connection c = DriverManager.getConnection(url, user, pass);
        // 连接验证
        try (Statement st = c.createStatement()) {
            st.executeQuery("SELECT 1");
        }
        this.conn = c;
        this.dataDb = db;
        this.modelDb = model;
    }

    public synchronized void disconnect() {
        if (conn != null) {
            try { conn.close(); } catch (Exception ignore) { }
            conn = null;
        }
    }

    public boolean isConnected() {
        return conn != null;
    }

    private void requireConn() {
        if (conn == null) throw new IllegalStateException("尚未连接数据库，请先点【连接】");
    }

    /** t 开头（t+数字任务ID）的数据表，只返回表名；需求：列表只留 1-2 张表 */
    public synchronized List<Map<String, Object>> tables() throws SQLException {
        requireConn();
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = ? AND table_name REGEXP '^t[0-9]+$' ORDER BY table_name LIMIT 2")) {
            ps.setString(1, dataDb);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString(1));
                    list.add(m);
                }
            }
        }
        return list;
    }

    /** 主题清单：modelmanager.topic，name/type/remark */
    public synchronized List<Map<String, Object>> topics() throws SQLException {
        requireConn();
        List<Map<String, Object>> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT name, type, remark FROM `" + modelDb + "`.topic ORDER BY name")) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", rs.getString(1));
                m.put("type", rs.getString(2));
                m.put("remark", rs.getString(3));
                list.add(m);
            }
        }
        return list;
    }

    /** 某主题（type）的字段结构：simulatmutual a LEFT JOIN dataitem b */
    public synchronized List<Map<String, Object>> structure(String type) throws SQLException {
        requireConn();
        List<Map<String, Object>> list = new ArrayList<>();
        if (type == null || type.isEmpty()) return list;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT b.name, b.meaning, b.type FROM `" + modelDb + "`.simulatmutual a "
                        + "LEFT JOIN `" + modelDb + "`.dataitem b ON a.id = b.simulatmutual_id "
                        + "WHERE a.name = ?")) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString(1));
                    m.put("meaning", rs.getString(2));
                    m.put("type", rs.getString(3));
                    list.add(m);
                }
            }
        }
        return list;
    }

    /** 解析：按表+主题查询并解包 msg_info，有字段结构则按位映射 key-value */
    public synchronized Map<String, Object> parse(String table, List<String> topics, int limit) throws SQLException {
        requireConn();
        Map<String, Object> result = new LinkedHashMap<>();
        if (table == null || table.isEmpty()) throw new IllegalArgumentException("未选择数据表");
        if (topics == null || topics.isEmpty()) throw new IllegalArgumentException("未选择主题");

        // 预加载各主题结构：主题名 -> 字段列表
        Map<String, String> typeByName = new LinkedHashMap<>();
        for (Map<String, Object> t : topics()) typeByName.put(String.valueOf(t.get("name")), t.get("type") == null ? "" : String.valueOf(t.get("type")));
        Map<String, List<Map<String, Object>>> structByName = new LinkedHashMap<>();
        for (String tn : topics) {
            String type = typeByName.getOrDefault(tn, "");
            structByName.put(tn, structure(type));
        }

        StringBuilder ph = new StringBuilder();
        for (int i = 0; i < topics.size(); i++) {
            if (i > 0) ph.append(',');
            ph.append('?');
        }
        String sql = "SELECT id, topic, msg_info FROM `" + table + "` WHERE topic IN (" + ph + ") ORDER BY id DESC LIMIT " + Math.max(1, limit);
        List<Map<String, Object>> rows = new ArrayList<>();
        int ok = 0, fail = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < topics.size(); i++) ps.setString(i + 1, topics.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("id"));
                    String topic = rs.getString("topic");
                    row.put("topic", topic);
                    byte[] raw = rs.getBytes("msg_info");
                    row.put("len", raw == null ? 0 : raw.length);
                    Object obj = tryUnpack(rs);
                    if (obj == null) {
                        fail++;
                        row.put("data", "[解析失败]");
                    } else {
                        ok++;
                        List<Map<String, Object>> fields = structByName.get(topic);
                        if (fields != null && !fields.isEmpty() && obj instanceof List) {
                            List<?> arr = (List<?>) obj;
                            Map<String, Object> kv = new LinkedHashMap<>();
                            for (int i = 0; i < fields.size(); i++) {
                                Map<String, Object> f = fields.get(i);
                                String key = String.valueOf(f.get("meaning"));
                                if (key == null || key.isEmpty() || "null".equals(key)) key = String.valueOf(f.get("name"));
                                if (key == null || "null".equals(key)) key = "field_" + i;
                                if (kv.containsKey(key)) {
                                    int k = 1;
                                    while (kv.containsKey(key + "_" + k)) k++;
                                    key = key + "_" + k;
                                }
                                kv.put(key, i < arr.size() ? arr.get(i) : null);
                            }
                            for (int i = fields.size(); i < arr.size(); i++) {
                                kv.put("col_" + (i - fields.size()), arr.get(i));
                            }
                            row.put("data", kv);
                        } else {
                            row.put("data", obj);
                        }
                    }
                    rows.add(row);
                }
            }
        }
        result.put("rows", rows);
        result.put("ok", ok);
        result.put("fail", fail);
        return result;
    }

    /** 弹道特征点提取：固定读三个主题，按弹id 关联，返回逐弹合成行 + 轨迹序列
     *  发射主题 = TOPIC_QY_MISSILELAUNCHTIME（entityUID=弹id · platformType=平台类型 · entityType=导弹类型），
     *  轨迹只关联该发射主题，不再关联旧 AttackBatReport。 */
    private static final String[] FEATURE_TOPICS = {
            "TOPIC_MSG_HIT",
            "TOPIC_QY_MISSILELAUNCHTIME",
            "TOPIC_QY_YJ18BTrajectoryData"
    };
    private static final String[] MID_KEYS = {
            "弹id", "弹ID", "弹Id", "弹号", "导弹ID", "导弹编号",
            "导弹UID", "导弹uid", "导弹id", "导弹Id",
            "mid", "missileId", "MissileId", "missile_id",
            "missileUID", "missileUid", "MissileUID",
            "weaponUID", "weaponUid", "WeaponUID",
            "entityUID", "EntityUID", "entityUid"
    };

    public synchronized Map<String, Object> featurePoints(String table) throws SQLException {
        requireConn();
        if (table == null || table.isEmpty()) throw new IllegalArgumentException("未选择数据表");

        // 1. 读取三个固定主题（id 升序，轨迹时间序列自然有序）
        Map<String, List<Map<String, Object>>> byTopic = new LinkedHashMap<>();
        for (String t : FEATURE_TOPICS) byTopic.put(t, new ArrayList<>());
        Map<String, String> typeByName = new LinkedHashMap<>();
        for (Map<String, Object> t : topics()) typeByName.put(String.valueOf(t.get("name")), t.get("type") == null ? "" : String.valueOf(t.get("type")));
        Map<String, List<Map<String, Object>>> structByName = new LinkedHashMap<>();
        for (String tn : FEATURE_TOPICS) structByName.put(tn, structureOrFallback(tn, typeByName.getOrDefault(tn, "")));
        String sql = "SELECT id, topic, msg_info FROM `" + table + "` WHERE topic IN (?,?,?) ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < FEATURE_TOPICS.length; i++) ps.setString(i + 1, FEATURE_TOPICS[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("id"));
                    String topic = rs.getString("topic");
                    row.put("topic", topic);
                    Object obj = tryUnpack(rs);
                    if (obj == null) {
                        row.put("data", "[解析失败]");
                    } else {
                        Map<String, Object> mapped = mapByStructure(obj, structByName.get(topic));
                        row.put("data", mapped != null ? mapped : obj);
                    }
                    List<Map<String, Object>> list = byTopic.get(topic);
                    if (list == null) {
                        list = new ArrayList<>();
                        byTopic.put(topic, list);
                    }
                    list.add(row);
                }
            }
        }

        // 2. 按弹id 分组：HIT / AttackBatReport 每弹保留末行，轨迹保留全序列
        Map<String, Map<?, ?>> hitByMid = new LinkedHashMap<>();
        Map<String, Map<?, ?>> reportByMid = new LinkedHashMap<>();
        Map<String, List<Map<?, ?>>> trajByMid = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : byTopic.entrySet()) {
            for (Map<String, Object> row : e.getValue()) {
                Object data = row.get("data");
                if (!(data instanceof Map)) continue;
                Map<?, ?> dm = (Map<?, ?>) data;
                String mid = firstMid(dm);
                if (mid == null) continue;
                if ("TOPIC_MSG_HIT".equals(e.getKey())) {
                    hitByMid.put(mid, dm);
                } else if ("TOPIC_QY_MISSILELAUNCHTIME".equals(e.getKey())) {
                    reportByMid.put(mid, dm);
                } else {
                    trajByMid.computeIfAbsent(mid, k -> new ArrayList<>()).add(dm);
                }
            }
        }

        // 3. 命中过滤：轨迹为主数据（全量），仅保留 HIT.isHit=true 的弹，false 不返回前端
        Set<String> mids = new LinkedHashSet<>(trajByMid.keySet());
        Iterator<String> it = mids.iterator();
        while (it.hasNext()) {
            String mid = it.next();
            if (!isHitTrue(hitByMid.get(mid))) it.remove();
        }

        // 4. 主数据=轨迹全量：逐轨迹点输出一行（并入该弹批报/命中信息），弹id 为主键
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> series = new LinkedHashMap<>();
        for (String mid : mids) {
            List<Map<?, ?>> traj = trajByMid.get(mid);
            if (traj == null || traj.isEmpty()) continue;
            series.put(mid, traj);
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("弹id", mid);
            mergeInto(base, reportByMid.get(mid));
            mergeInto(base, hitByMid.get(mid));
            for (Map<?, ?> pt : traj) {
                Map<String, Object> row = new LinkedHashMap<>(base);
                mergeInto(row, pt);
                rows.add(row);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("series", series);
        return result;
    }

    /** 在数据对象里找第一个存在的弹id 键 */
    private static String firstMid(Map<?, ?> data) {
        for (String k : MID_KEYS) {
            Object v = data.get(k);
            if (v != null && !String.valueOf(v).trim().isEmpty()) return String.valueOf(v);
        }
        return null;
    }

    /** HIT 命中判定：是否命中 / 命中状态 / isHit 任一为真（兼容 1/true/是/命中 等写法） */
    private static boolean isHitTrue(Map<?, ?> hit) {
        if (hit == null) return false;
        Object v = hit.get("是否命中");
        if (v == null) v = hit.get("命中状态");
        if (v == null) v = hit.get("isHit");
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).doubleValue() != 0;
        String s = String.valueOf(v).trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "是".equals(s) || "yes".equals(s) || "命中".equals(s);
    }

    /** 数组 → key-value 结构映射（无结构时返回 null，调用方保留原对象） */
    private static Map<String, Object> mapByStructure(Object obj, List<Map<String, Object>> fields) {
        if (!(obj instanceof List)) return null;
        List<?> arr = (List<?>) obj;
        Map<String, Object> kv = new LinkedHashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            Map<String, Object> f = fields.get(i);
            String key = String.valueOf(f.get("meaning"));
            if (key == null || key.isEmpty() || "null".equals(key)) key = String.valueOf(f.get("name"));
            if (key == null || "null".equals(key)) key = "field_" + i;
            if (kv.containsKey(key)) {
                int k = 1;
                while (kv.containsKey(key + "_" + k)) k++;
                key = key + "_" + k;
            }
            kv.put(key, i < arr.size() ? arr.get(i) : null);
        }
        for (int i = fields.size(); i < arr.size(); i++) kv.put("col_" + (i - fields.size()), arr.get(i));
        return kv;
    }

    /* 三个主题的内置默认结构（模型库未定义时兜底，保证演示数据可用） */
    private static final Map<String, List<Map<String, Object>>> DEFAULT_STRUCTURES = new LinkedHashMap<>();
    static {
        addStruct("TOPIC_QY_YJ18BTrajectoryData", "弹id,当前时间,经度,纬度,高度,北向速度,天向速度,东向速度,滚转角,俯仰角,偏航角,当前阶段,合速度");
        addStruct("TOPIC_MSG_HIT", "弹id,当前时间,经度,纬度,高度,命中状态,脱靶量");
        addStruct("TOPIC_QY_MISSILELAUNCHTIME", "弹id,平台类型,导弹类型");
    }
    private static void addStruct(String topic, String csv) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String n : csv.split(",")) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", n);
            m.put("meaning", n);
            m.put("type", "STRING");
            list.add(m);
        }
        DEFAULT_STRUCTURES.put(topic, list);
    }

    /** 主题结构：模型库有定义则用之，否则用内置默认结构 */
    private List<Map<String, Object>> structureOrFallback(String topic, String type) throws SQLException {
        List<Map<String, Object>> f = structure(type);
        if (f == null || f.isEmpty()) {
            f = DEFAULT_STRUCTURES.get(topic);
            if (f == null) f = new ArrayList<>();
        }
        /* 发射主题兜底补 导弹类型(entityType)：模型库未定义该字段时由代码补齐，
         * 保证「导弹类型」列有值（造数 genFieldValue 按「类型」键写入 missileType） */
        if ("TOPIC_QY_MISSILELAUNCHTIME".equals(topic)) {
            boolean hasType = false;
            for (Map<String, Object> ff : f) {
                Object meaning = ff.get("meaning");
                Object name = ff.get("name");
                if ("导弹类型".equals(meaning) || "entityType".equalsIgnoreCase(String.valueOf(name))) {
                    hasType = true;
                    break;
                }
            }
            if (!hasType) {
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("name", "entityType");
                t.put("meaning", "导弹类型");
                t.put("type", "STRING");
                f.add(t);
            }
        }
        return f;
    }

    /** 造测试数据：清空三个固定主题后，按结构生成 5 枚弹（轨迹 25 点 + 命中 + 发射 MISSILELAUNCHTIME） */
    public synchronized Map<String, Object> seed(String table) throws SQLException, IOException {
        requireConn();
        if (table == null || table.isEmpty()) throw new IllegalArgumentException("未选择数据表");
        Map<String, String> typeByName = new LinkedHashMap<>();
        for (Map<String, Object> t : topics()) typeByName.put(String.valueOf(t.get("name")), t.get("type") == null ? "" : String.valueOf(t.get("type")));
        Map<String, List<Map<String, Object>>> structByName = new LinkedHashMap<>();
        for (String tn : FEATURE_TOPICS) structByName.put(tn, structureOrFallback(tn, typeByName.getOrDefault(tn, "")));

        /* 清旧：新三主题 + 旧发射主题 AttackBatReport 残留 */
        String[] cleanup = { FEATURE_TOPICS[0], FEATURE_TOPICS[1], FEATURE_TOPICS[2],
                "TOPIC_MSG_TOPIC_INTEL_AttackBatReport" };
        String delSql = "DELETE FROM `" + table + "` WHERE topic IN (?,?,?,?)";
        try (PreparedStatement del = conn.prepareStatement(delSql)) {
            for (int i = 0; i < cleanup.length; i++) del.setString(i + 1, cleanup[i]);
            del.executeUpdate();
        }

        /* 表 id 非自增：显式取 MAX(id)+1，下限 9000000（与测试数据约定一致） */
        long nextId = 9000000L;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0) FROM `" + table + "`")) {
            if (rs.next()) nextId = Math.max(9000000L, rs.getLong(1) + 1);
        }

        Random rnd = new Random(20260829L);
        String[] mids = { "导弹1", "导弹2", "导弹3", "导弹4", "导弹5" };
        String[] mTypes = { "中程弹道导弹", "近程弹道导弹", "高超音速导弹", "中程弹道导弹" };
        String[] platforms = { "陆基机动发射车", "舰载垂直发射", "空射平台", "固定发射井" };
        int[] counts = new int[FEATURE_TOPICS.length];
        long baseMs = 1724900000000L;
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO `" + table + "` (id, topic, msg_info) VALUES (?, ?, ?)")) {
            for (int mi = 0; mi < mids.length; mi++) {
                String mid = mids[mi];
                double lon0 = 116.4 + rnd.nextDouble() * 0.3;
                double lat0 = 39.5 + rnd.nextDouble() * 0.3;
                double lonT = lon0 + 0.6 + rnd.nextDouble() * 0.3;
                double latT = lat0 + 0.4 + rnd.nextDouble() * 0.3;
                String mType = mTypes[mi % mTypes.length];
                String platform = platforms[mi % platforms.length];

                /* 轨迹 25 点（带弹道阶段演进） */
                int trajN = 25;
                for (int i = 0; i < trajN; i++) {
                    double p = (i + 0.5) / trajN;
                    GenCtx c = trajCtx(mid, baseMs + mi * 3600000L + i * 100L, p, lon0, lat0, lonT, latT);
                    insertSeed(ins, structByName.get("TOPIC_QY_YJ18BTrajectoryData"), "TOPIC_QY_YJ18BTrajectoryData", c, nextId++);
                    counts[2]++;
                }
                /* 命中 1 行：首弹命中，次弹未命中（用于演示命中过滤） */
                GenCtx hit = new GenCtx();
                hit.mid = mid;
                hit.timeMs = baseMs + mi * 3600000L + trajN * 100L;
                hit.lon = lonT; hit.lat = latT; hit.alt = 0;
                hit.pitch = -20; hit.phase = "命中";
                hit.missileType = mType; hit.platform = platform;
                if (mi < 4) {
                    hit.status = "已命中";
                    hit.hit = 1;
                    hit.offset = Math.round((12.5 + rnd.nextDouble() * 40) * 10) / 10.0;
                } else {
                    hit.status = "未命中";
                    hit.hit = 0;
                    hit.offset = 0;
                }
                hit.target = "靶标T-" + (mi + 1); hit.batch = "B-20260829-" + (mi + 1);
                insertSeed(ins, structByName.get("TOPIC_MSG_HIT"), "TOPIC_MSG_HIT", hit, nextId++);
                counts[0]++;
                /* 发射 1 行（TOPIC_QY_MISSILELAUNCHTIME：弹id + 平台类型 + 导弹类型） */
                GenCtx rep = new GenCtx();
                rep.mid = mid; rep.timeMs = hit.timeMs;
                rep.platform = platform; rep.status = "已发射";
                rep.missileType = mType;
                insertSeed(ins, structByName.get("TOPIC_QY_MISSILELAUNCHTIME"), "TOPIC_QY_MISSILELAUNCHTIME", rep, nextId++);
                counts[1]++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("inserted", counts[0] + counts[1] + counts[2]);
        Map<String, Object> byTopic = new LinkedHashMap<>();
        for (int i = 0; i < FEATURE_TOPICS.length; i++) byTopic.put(FEATURE_TOPICS[i], counts[i]);
        result.put("topics", byTopic);
        return result;
    }

    /** 单行生成上下文 */
    private static class GenCtx {
        String mid = "";
        long timeMs = 0;
        double lon = 0, lat = 0, alt = 0;
        double vn = 0, vu = 0, ve = 0, roll = 0, pitch = 0, yaw = 0, speed = 0;
        String phase = "", missileType = "", platform = "", status = "", target = "", batch = "";
        int hit = 0;
        double offset = 0;
    }

    /** 轨迹点上下文：按弹道进程推进阶段/高度/速度/姿态 */
    private static GenCtx trajCtx(String mid, long t, double p, double lon0, double lat0, double lonT, double latT) {
        GenCtx c = new GenCtx();
        c.mid = mid;
        c.timeMs = t;
        double e = p * p * (3 - 2 * p);
        c.lon = lon0 + (lonT - lon0) * e;
        c.lat = lat0 + (latT - lat0) * e;
        if (p < 0.35) {
            c.phase = "助推段";
            c.vn = 500 + 900 * p; c.vu = 150 + 550 * p; c.ve = 40;
            c.alt = 30000 * Math.pow(p / 0.35, 1.25);
            c.pitch = 50 - 20 * p;
        } else if (p < 0.65) {
            c.phase = "惯性飞行段";
            c.vn = 1250; c.vu = 400 - (p - 0.35) * 2500; c.ve = 90;
            c.alt = 80000 * Math.sin(Math.PI * (p - 0.1) / 1.5) - 20000;
            c.pitch = 20 * (1 - (p - 0.35) / 0.3);
        } else if (p < 0.85) {
            c.phase = "再入段";
            c.vn = 1650; c.vu = -300 - 400 * (p - 0.65) / 0.2; c.ve = 110;
            c.alt = 70000 * Math.pow((1 - p) / 0.35, 1.1) + 5000;
            c.pitch = -20 - 10 * (p - 0.65) / 0.2;
        } else {
            c.phase = "末段机动";
            c.vn = 1500; c.vu = -80 - 140 * (p - 0.85) / 0.15; c.ve = -120;
            c.alt = 4500 * Math.pow(1 - (p - 0.85) / 0.15, 1.4);
            c.pitch = -30;
        }
        c.roll = Math.sin(p * 12) * 5;
        c.yaw = 15 * Math.sin(p * 6);
        c.speed = Math.round(Math.sqrt(c.vn * c.vn + c.vu * c.vu + c.ve * c.ve) * 10) / 10.0;
        c.alt = Math.round(c.alt);
        c.vn = Math.round(c.vn * 10) / 10.0;
        c.vu = Math.round(c.vu * 10) / 10.0;
        c.ve = Math.round(c.ve * 10) / 10.0;
        c.roll = Math.round(c.roll * 10) / 10.0;
        c.pitch = Math.round(c.pitch * 10) / 10.0;
        c.yaw = Math.round(c.yaw * 10) / 10.0;
        return c;
    }

    /** 按字段结构生成一行值并插入（MessagePack 数组，BLOB 列按原始字节写入） */
    private static void insertSeed(PreparedStatement ins, List<Map<String, Object>> fields, String topic, GenCtx c, long id) throws SQLException, IOException {
        List<Object> values = new ArrayList<>();
        for (Map<String, Object> f : fields) values.add(genFieldValue(f, c));
        ins.setLong(1, id);
        ins.setString(2, topic);
        ins.setBytes(3, packArray(values));
        ins.executeUpdate();
    }

    /** 字段值生成：按字段含义/名称匹配上下文 */
    private static Object genFieldValue(Map<String, Object> f, GenCtx c) {
        String name = String.valueOf(f.get("name"));
        String meaning = String.valueOf(f.get("meaning"));
        String key = (meaning != null && !"null".equals(meaning) && !meaning.isEmpty()) ? meaning : name;
        if (key == null || "null".equals(key)) key = name;
        for (String mk : MID_KEYS) if (key.equals(mk)) return c.mid;
        if (key.contains("时间")) return c.timeMs;
        if (key.contains("经度")) return c.lon;
        if (key.contains("纬度") || key.contains("维度") || key.equalsIgnoreCase("lat")) return c.lat;
        if (key.contains("高度") || key.equalsIgnoreCase("alt") || key.equals("H")) return c.alt;
        if (key.contains("北向") || key.equalsIgnoreCase("vn")) return c.vn;
        if (key.contains("天向") || key.contains("垂向") || key.equalsIgnoreCase("vu")) return c.vu;
        if (key.contains("东向") || key.equalsIgnoreCase("ve")) return c.ve;
        if (key.contains("滚转")) return c.roll;
        if (key.contains("俯仰")) return c.pitch;
        if (key.contains("偏航")) return c.yaw;
        if (key.contains("阶段")) return c.phase;
        if (key.contains("合速度") || key.contains("速度") || key.equalsIgnoreCase("speed")) return c.speed;
        if (key.contains("平台")) return c.platform;   /* 发射平台类型 先于 类型 匹配 */
        if (key.contains("类型")) return c.missileType;
        if (key.contains("状态")) return c.status;
        if (key.contains("目标")) return c.target;
        if (key.contains("批次")) return c.batch;
        if (key.contains("命中")) return c.hit;
        if (key.contains("脱靶")) return c.offset;
        String type = String.valueOf(f.get("type"));
        if (type != null) {
            String t = type.toUpperCase();
            if (t.contains("INT")) return 0;
            if (t.contains("DOUBLE") || t.contains("FLOAT") || t.contains("DECIMAL") || t.contains("NUMERIC")) return 0d;
        }
        return "";
    }

    /** 值列表 → MessagePack 数组字节 */
    private static byte[] packArray(List<Object> values) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(values.size());
        for (Object v : values) {
            if (v == null) packer.packNil();
            else if (v instanceof Integer) packer.packInt((Integer) v);
            else if (v instanceof Long) packer.packLong((Long) v);
            else if (v instanceof Double) packer.packDouble((Double) v);
            else if (v instanceof Float) packer.packFloat((Float) v);
            else if (v instanceof Boolean) packer.packBoolean((Boolean) v);
            else packer.packString(String.valueOf(v));
        }
        packer.close();
        return packer.toByteArray();
    }

    /** 合并：源字段写入目标（后写覆盖），null 值跳过 */
    private static void mergeInto(Map<String, Object> target, Map<?, ?> src) {
        if (src == null) return;
        for (Map.Entry<?, ?> e : src.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            target.put(String.valueOf(e.getKey()), e.getValue());
        }
    }

    /** 通用解包：先取原始字节（BLOB / latin1 文本均正确），再试 ISO-8859-1 文本还原，返回可 JSON 化的对象 */
    private Object tryUnpack(ResultSet rs) throws SQLException {
        byte[] raw = rs.getBytes("msg_info");
        if (raw != null && raw.length > 0) {
            Value v = unpackValue(raw);
            if (v != null) return toObject(v);
        }
        String s = rs.getString("msg_info");
        if (s != null && !s.isEmpty()) {
            Value v = unpackValue(s.getBytes(StandardCharsets.ISO_8859_1));
            if (v != null) return toObject(v);
        }
        return null;
    }

    /** 解包一个完整的 msgpack 根对象；只有完整消费全部字节才算合法 */
    private static Value unpackValue(byte[] msgInfo) {
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(msgInfo);
            if (!unpacker.hasNext()) return null;
            Value v = unpacker.unpackValue();
            return unpacker.hasNext() ? null : v;
        } catch (Exception ignore) {
            return null;
        }
    }

    /** Value -> 普通 Java 对象（可 JSON 化） */
    private static Object toObject(Value v) {
        if (v == null) return null;
        switch (v.getValueType()) {
            case NIL: return null;
            case BOOLEAN: return v.asBooleanValue().getBoolean();
            case INTEGER: {
                IntegerValue iv = v.asIntegerValue();
                if (iv.isInIntRange()) return iv.toInt();
                if (iv.isInLongRange()) return iv.toLong();
                return iv.toBigInteger();
            }
            case FLOAT: return v.asFloatValue().toDouble();
            case STRING: return v.asStringValue().asString();
            case BINARY: return "0x" + hex(v.asBinaryValue().asByteArray());
            case EXTENSION: {
                ExtensionValue e = v.asExtensionValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("__ext_type", e.getType());
                m.put("__ext_data", "0x" + hex(e.getData()));
                return m;
            }
            case ARRAY: {
                List<Object> list = new ArrayList<>();
                for (Value e : v.asArrayValue()) list.add(toObject(e));
                return list;
            }
            case MAP: {
                Map<String, Object> m = new LinkedHashMap<>();
                MapValue mv = v.asMapValue();
                for (Map.Entry<Value, Value> e : mv.map().entrySet()) {
                    m.put(String.valueOf(toObject(e.getKey())), toObject(e.getValue()));
                }
                return m;
            }
            default: return v.toString();
        }
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x & 0xff));
        return sb.toString();
    }
}
