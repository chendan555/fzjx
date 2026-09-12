package com.casic.assess.dc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API：连接/断开、表列表、主题、字段结构、解析
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final DataCollectService service;

    public ApiController(DataCollectService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("connected", service.isConnected());
        return m;
    }

    @PostMapping("/connect")
    public Map<String, Object> connect(@RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            service.connect(str(body, "host"), str(body, "port"), str(body, "user"),
                    str(body, "pass"), str(body, "db"), str(body, "modelDb"));
            m.put("ok", true);
            m.put("message", "连接成功");
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", "连接失败: " + e.getMessage());
        }
        return m;
    }

    @PostMapping("/disconnect")
    public Map<String, Object> disconnect() {
        service.disconnect();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    @GetMapping("/tables")
    public Map<String, Object> tables() {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            m.put("ok", true);
            m.put("tables", service.tables());
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", e.getMessage());
        }
        return m;
    }

    @GetMapping("/topics")
    public Map<String, Object> topics() {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            m.put("ok", true);
            m.put("topics", service.topics());
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", e.getMessage());
        }
        return m;
    }

    @GetMapping("/structure")
    public Map<String, Object> structure(@RequestParam(value = "type", required = false) String type) {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            m.put("ok", true);
            m.put("fields", service.structure(type));
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", e.getMessage());
        }
        return m;
    }

    @GetMapping("/feature")
    public Map<String, Object> feature(@RequestParam("table") String table) {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            m.put("ok", true);
            m.put("result", service.featurePoints(table));
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", e.getMessage());
        }
        return m;
    }

    @PostMapping("/seed")
    public Map<String, Object> seed(@RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            m.putAll(service.seed(str(body, "table")));
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", "造数据失败: " + e.getMessage());
        }
        return m;
    }

    @PostMapping("/parse")
    public Map<String, Object> parse(@RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            String table = str(body, "table");
            List<String> topics = (List<String>) body.get("topics");
            int limit = body.get("limit") == null ? 1000 : Integer.parseInt(String.valueOf(body.get("limit")));
            m.put("ok", true);
            m.put("result", service.parse(table, topics, limit));
        } catch (Exception e) {
            m.put("ok", false);
            m.put("message", e.getMessage());
        }
        return m;
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }
}
