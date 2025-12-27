/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
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
import de.linzn.stem.STEMApp;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class MqttCanbusListener implements IMqttMessageListener {
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        JSONObject jsonPayload = new JSONObject(new String(mqttMessage.getPayload()));
        MQTTCanbusReceiveEvent mqttCanbusReceiveEvent = new MQTTCanbusReceiveEvent(jsonPayload);
        STEMApp.getInstance().getEventModule().getStemEventBus().fireEvent(mqttCanbusReceiveEvent);

        if (!mqttCanbusReceiveEvent.isCanceled()) {
            PelltechPlugin.pelltechPlugin.heaterProcessor.process(jsonPayload);
        }
    }
}
