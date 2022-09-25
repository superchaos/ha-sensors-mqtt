package com.supersoft.mqtt.cb;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

@Slf4j
public class SupersoftMqttCallback implements MqttCallback {
    private MqttClient client;
    private MqttConnectOptions options;

    public SupersoftMqttCallback(MqttClient client, MqttConnectOptions options) {
        this.client = client;
        this.options = options;
    }

    public void connectionLost(Throwable throwable) {
        //连接断掉会执行到这里
        log.info("mqtt connection lost", throwable);
        try {
            client.connect(options);
            log.info("mqtt reconnect success");
        } catch (Exception e) {
            log.error("mqtt reconnect error", e);
        }
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        //subscribe后会执行到这里
        log.info("消息的主题: {}", s);
        log.info("消息的Qos: {}", mqttMessage.getQos());
        log.info("消息的ID: {}", mqttMessage.getId());
        log.info("消息的内容: {}", new String(mqttMessage.getPayload()));
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
