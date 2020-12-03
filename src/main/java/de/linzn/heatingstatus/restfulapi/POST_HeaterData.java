/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.heatingstatus.restfulapi;

import de.linzn.heatingstatus.HeatingStatusPlugin;
import de.linzn.restfulapi.api.jsonapi.IRequest;
import de.linzn.restfulapi.api.jsonapi.RequestData;
import org.json.JSONObject;

public class POST_HeaterData implements IRequest {
    @Override
    public Object proceedRequestData(RequestData requestData) {
        JSONObject jsonObject = new JSONObject();
        JSONObject requestBody = new JSONObject(requestData.getPostQueryData().get("requestBody"));

        jsonObject.put("data", requestBody);
        jsonObject.put("valid", requestBody.length() > 0);

        HeatingStatusPlugin.heatingStatusPlugin.heaterProcessor.process(jsonObject);
        return jsonObject;
    }

    @Override
    public String name() {
        return "heater-canbus-data";
    }

}