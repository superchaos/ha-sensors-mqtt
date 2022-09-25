package com.supersoft.mqtt.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supersoft.mqtt.data.MqttSensor;
import com.supersoft.mqtt.cb.SupersoftMqttCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class MqttController {
    private static final String CLIENT_ID = "HA";
    private static final String TOPIC = "homeassistant/sensor/%s/%s/%s";
    private static final String CONFIG = "config";
    private static final String STATE = "state";

    private MqttClient client;
    private MqttConnectOptions options;

    @Value("${mqtt.host}")
    public String HOST = null;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(HOST, CLIENT_ID,new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setUserName("ha");
            options.setCleanSession(false);
            options.setKeepAliveInterval(10);
            options.setConnectionTimeout(50);
            client.setCallback(new SupersoftMqttCallback(client, options));
            client.connect(options);
        } catch (MqttException e) {
            log.error("启动mqtt失败", e);
        }
    }

    @GetMapping("/mqtt/config/{device}/{name}/{type}/{unit}")
    public void configMqtt(@PathVariable String device, @PathVariable String name, @PathVariable String type,
                         @PathVariable String unit) {
        String topic = String.format(TOPIC, device, name, CONFIG);
        String stateTopic = String.format(TOPIC, device, name, STATE);
        MqttSensor sensor = new MqttSensor();
        sensor.setName(name);
        sensor.setUnique_id(name);
        sensor.setDevice_class(type);
        sensor.setUnit_of_measurement(unit);
        sensor.setState_topic(stateTopic);
        MqttMessage configMessage = new MqttMessage();
        configMessage.setPayload(JSON.toJSONString(sensor).getBytes());
        try {
            client.publish(topic, configMessage);
            log.info("send config success, topic={}, message={}", topic, configMessage);
        } catch (Exception e) {
            log.error("send config error", e);
        }
    }

    @GetMapping("/mqtt/state/{device}/{name}/{value}")
    public void stateMqtt(@PathVariable String device, @PathVariable String name, @PathVariable String value) {
        Map map = new HashMap();
        map.put("value", value);
        String stateTopic = String.format(TOPIC, device, name, STATE);
        MqttMessage stateMessage = new MqttMessage();
        stateMessage.setPayload(JSON.toJSONString(map).getBytes());
        try {
            client.publish(stateTopic, stateMessage);
            log.info("send state success, topic={}, message={}", stateTopic, stateMessage);
        } catch (Exception e) {
            log.error("send state error", e);
        }
    }

    @PostMapping("/mqtt/sensors/{device}")
    public String sensors(@PathVariable String device, @RequestBody String data) {
        if (StringUtils.isBlank(device)) {
            return "device can not be null\n";
        }
        if (StringUtils.isBlank(data)) {
            return "sensors can not be null\n";
        }
        Map<String, Object> map = parseObject(data);
        if (MapUtils.isEmpty(map)) {
            return "sensors data parse error\n";
        }
        map.entrySet().stream().forEach(item -> {
            String key = item.getKey();
            if (key.endsWith("_input")) {
                configMqtt(device, key, "temperature", "°C");
                String value = item.getValue().toString();
                if (validateNumber(value)) {
                    BigDecimal decimal = new BigDecimal(value);
                    decimal = decimal.setScale(1, BigDecimal.ROUND_HALF_UP);
                    stateMqtt(device, key, decimal.toString());
                }
            }
        });
        return "send sensor data success\n";
    }

    private Map<String, Object> parseObject(String data) {
        Map<String, Object> map = new HashMap<>();
        JSONObject jsonObject = JSON.parseObject(data);
        jsonObject.entrySet().stream().forEach(item -> walk(null, item.getKey(), item.getValue(), map));
        return map;
    }

    private void walk(String parent, String key, Object object, Map<String, Object> map) {
        if (object == null) {
            return;
        }
        String path = StringUtils.isNotBlank(parent) ? parent + "_" + key : key;
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.entrySet().stream().forEach(item -> walk(noBlank(path), item.getKey(), item.getValue(), map));
        } else {
            map.put(path, object);
        }
    }

    private String noBlank(String str) {
        return str.replaceAll("\\s+", "").toLowerCase();
    }

    private boolean validateNumber(String str) {
        if(StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("[+-]?[0-9]+(\\.[0-9]+)?");
    }
}
