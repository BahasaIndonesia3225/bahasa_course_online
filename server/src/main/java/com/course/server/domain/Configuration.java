package com.course.server.domain;

/**
 * 配置表
 */
public class Configuration {

    /**
     * 主键
     */
    private String id;
    /*
    * 配置key
     */
    private String configurationKey;
    /**
     * 配置value
     */
    private String configurationValue;
    /**
     * 状态（0-开启，1-关闭）
     */
    private Integer state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    public void setConfigurationKey(String configurationKey) {
        this.configurationKey = configurationKey;
    }

    public String getConfigurationValue() {
        return configurationValue;
    }

    public void setConfigurationValue(String configurationValue) {
        this.configurationValue = configurationValue;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "id='" + id + '\'' +
                ", configurationKey='" + configurationKey + '\'' +
                ", configurationValue='" + configurationValue + '\'' +
                ", state=" + state +
                '}';
    }
}
