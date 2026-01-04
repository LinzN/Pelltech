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

package de.linzn.pelltech.webApi;

import com.sun.net.httpserver.HttpExchange;
import de.linzn.pelltech.PelltechPlugin;
import de.linzn.pelltech.objects.Inlet;
import de.linzn.pelltech.objects.Notify;
import de.linzn.pelltech.objects.Outlet;
import de.linzn.webapi.core.ApiResponse;
import de.linzn.webapi.core.HttpRequestClientPayload;
import de.linzn.webapi.modules.RequestInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PelltechWebApi extends RequestInterface {
    private PelltechPlugin pelltechPlugin;

    public PelltechWebApi(PelltechPlugin pelltechPlugin) {
        this.pelltechPlugin = pelltechPlugin;
    }

    @Override
    public Object callHttpEvent(HttpExchange httpExchange, HttpRequestClientPayload httpRequestClientPayload) throws IOException {
        ApiResponse apiResponse = new ApiResponse();

        double waterTemperature = -1D;
        int burnerStatus = 0;

        Date lastSync = this.pelltechPlugin.heaterProcessor.getDate();
        List<Inlet> inlets = this.pelltechPlugin.heaterProcessor.getInletsList();
        List<Outlet> outlets = this.pelltechPlugin.heaterProcessor.getOutletsList();
        List<Notify> notifies = this.pelltechPlugin.heaterProcessor.getNotifiesList();

        JSONArray inletsArray = new JSONArray();
        for (Inlet inlet : inlets) {
            JSONObject object = new JSONObject();
            object.put("name", inlet.getName());
            object.put("value", inlet.getValue());
            inletsArray.put(object);

            if (inlet.getIndex() == 3) {
                waterTemperature = inlet.getValue();
            }
        }

        apiResponse.getJSONObject().put("inlets", inletsArray);

        JSONArray outletsArray = new JSONArray();
        for (Outlet outlet : outlets) {
            JSONObject object = new JSONObject();
            object.put("name", outlet.getName());
            object.put("status", outlet.isActive());
            outletsArray.put(object);

            if (outlet.getIndex() == 1) {
                if (outlet.isActive()) {
                    burnerStatus = 1;
                }
            }
        }

        apiResponse.getJSONObject().put("outlets", outletsArray);

        JSONArray notifiesArray = new JSONArray();
        for (Notify notify : notifies) {
            JSONObject object = new JSONObject();
            object.put("name", notify.getName());
            object.put("status", notify.isActive());
            notifiesArray.put(object);

            if (notify.isActive()) {
                burnerStatus = 2;
            }
        }
        apiResponse.getJSONObject().put("notifies", notifiesArray);


        JSONObject burnerData = new JSONObject();
        burnerData.put("watertemperature", waterTemperature);
        burnerData.put("burnerstatus", burnerStatus);
        if (lastSync != null) {
            burnerData.put("datesimple", new SimpleDateFormat("HH:mm:ss").format(lastSync));
            burnerData.put("date", new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(lastSync));
        } else {
            burnerData.put("datesimple", "N.A");
            burnerData.put("date", "No Date yet");
        }

        apiResponse.getJSONObject().put("simple", burnerData);

        return apiResponse.buildResponse();
    }
}
