package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 流程变量值对象
 * 
 * 用于存储和传递流程执行过程中的变量数据
 * 
 * @author xtt
 */
public class ProcessVariables {
    
    private final Map<String, Object> variables;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public ProcessVariables() {
        this.variables = new HashMap<>();
    }
    
    public ProcessVariables(Map<String, Object> variables) {
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }
    
    /**
     * 从 JSON 字符串创建流程变量
     */
    public static ProcessVariables fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ProcessVariables();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, 
                new TypeReference<Map<String, Object>>() {});
            return new ProcessVariables(map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format for process variables", e);
        }
    }
    
    /**
     * 转换为 JSON 字符串
     */
    public String toJson() {
        if (variables.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert process variables to JSON", e);
        }
    }
    
    /**
     * 设置变量
     */
    public ProcessVariables setVariable(String key, Object value) {
        Map<String, Object> newVariables = new HashMap<>(this.variables);
        newVariables.put(key, value);
        return new ProcessVariables(newVariables);
    }
    
    /**
     * 获取变量
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * 获取变量（带默认值）
     */
    public Object getVariable(String key, Object defaultValue) {
        return variables.getOrDefault(key, defaultValue);
    }
    
    /**
     * 获取字符串变量
     */
    public String getString(String key) {
        Object value = getVariable(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取整数变量
     */
    public Integer getInteger(String key) {
        Object value = getVariable(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取布尔变量
     */
    public Boolean getBoolean(String key) {
        Object value = getVariable(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }
    
    /**
     * 判断变量是否存在
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
    
    /**
     * 移除变量
     */
    public ProcessVariables removeVariable(String key) {
        Map<String, Object> newVariables = new HashMap<>(this.variables);
        newVariables.remove(key);
        return new ProcessVariables(newVariables);
    }
    
    /**
     * 获取所有变量（只读视图）
     */
    public Map<String, Object> getAllVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return variables.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVariables that = (ProcessVariables) o;
        return Objects.equals(variables, that.variables);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(variables);
    }
    
    @Override
    public String toString() {
        return "ProcessVariables{" +
                "variables=" + variables +
                '}';
    }
}

