/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.pelltech;

import de.linzn.pelltech.events.MQTTCanbusReceiveEvent;
import de.linzn.stem.api.mqtt.MqttHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MqttCanbusListener implements MqttHandler {

    @Override
    public void onMessage(String topic, byte[] payload) {
        JSONObject jsonPayload;
        try {
            jsonPayload = new JSONObject(new String(payload, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().warning("Invalid canbus payload on " + topic);
            return;
        }
        MQTTCanbusReceiveEvent mqttCanbusReceiveEvent = new MQTTCanbusReceiveEvent(jsonPayload);
        PelltechPlugin.pelltechPlugin.getStemApi().events().fire(mqttCanbusReceiveEvent);
        if (!mqttCanbusReceiveEvent.isCanceled()) {
            PelltechPlugin.pelltechPlugin.heaterProcessor.process(jsonPayload);
        }
    }
}
