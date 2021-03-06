/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package enmasse.mqtt.examples;

import enmasse.mqtt.MqttServer;
import enmasse.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example of using the MQTT server
 */
public class MqttApp {

    private static final Logger log = LoggerFactory.getLogger(MqttApp.class);

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        MqttServer mqttServer = MqttServer.create(vertx);
        mqttServer
                .endpointHandler(endpoint -> {

                    log.info("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

                    if (endpoint.auth() != null) {
                        log.info("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password() + "]");
                    }
                    if (endpoint.will() != null) {
                        log.info("[will topic = " + endpoint.will().willTopic() + " msg = " + endpoint.will().willMessage() +
                                " QoS = " + endpoint.will().willQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
                    }
                    endpoint.writeConnack(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);

                    endpoint.subscribeHandler(subscribe -> {

                        List<Integer> grantedQosLevels = new ArrayList<>();
                        for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
                            log.info("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                            grantedQosLevels.add(s.qualityOfService().value());
                        }
                        endpoint.writeSuback(subscribe.messageId(), grantedQosLevels);
                    });

        })
                .listen(ar -> {

                    if (ar.succeeded()) {

                        log.info("MQTT server is listening on port " + ar.result().actualPort());
                    }
        });

        try {
            System.in.read();
            mqttServer.close();
            vertx.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
