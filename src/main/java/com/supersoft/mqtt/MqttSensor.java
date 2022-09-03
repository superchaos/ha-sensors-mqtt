package com.supersoft.mqtt;

import lombok.Data;

@Data
public class MqttSensor {
    /**
     * 设备类型
     * humidity
     */
    String device_class = "temperature";

    /**
     * 设备名称
     */
    String name;

    /**
     * 定义唯一编码
     */
    String unique_id;

    /**
     * 上报topic
     */
    String state_topic;

    /**
     * 单位
     */
    String unit_of_measurement;

    /**
     * 取值方式模板
     */
    String value_template = "{{value_json.value}}";
}
