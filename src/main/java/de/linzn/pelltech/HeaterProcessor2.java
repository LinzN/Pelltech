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

import de.linzn.pelltech.objects.Inlet;
import de.linzn.pelltech.objects.Notify;
import de.linzn.pelltech.objects.Outlet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Applies canbus data to the configured inlets, outlets and notifies.
 */
public class HeaterProcessor2 {
    private final List<Inlet> inletsList;
    private final List<Outlet> outletsList;
    private final List<Notify> notifiesList;
    private volatile Date date;

    public HeaterProcessor2() {
        this.inletsList = List.copyOf(PelltechPlugin.pelltechPlugin.dbLogger.loadInlets());
        this.outletsList = List.copyOf(PelltechPlugin.pelltechPlugin.dbLogger.loadOutlets());
        this.notifiesList = List.copyOf(PelltechPlugin.pelltechPlugin.dbLogger.loadNotifies());
    }

    public synchronized void process(JSONObject rawJsonObject) {
        JSONArray inlets = rawJsonObject.optJSONArray("inlets");
        if (inlets != null) {
            this.updateInlets(inlets);
        }
        JSONArray outlets = rawJsonObject.optJSONArray("outlets");
        if (outlets != null) {
            this.updateOutlets(outlets);
        }
        JSONArray notifies = rawJsonObject.optJSONArray("notifies");
        if (notifies != null) {
            this.updateNotifies(notifies);
        }
        this.date = new Date();
    }

    private void updateInlets(JSONArray inlets) {
        for (int i = 0; i < inlets.length(); i++) {
            JSONObject jsonInlet = inlets.optJSONObject(i);
            if (jsonInlet == null || !jsonInlet.has("index")) {
                continue;
            }
            int index = jsonInlet.optInt("index");
            boolean health = jsonInlet.optString("state").equalsIgnoreCase("OK");
            double value = jsonInlet.optDouble("value", 0D);
            for (Inlet inlet : inletsList) {
                if (inlet.getIndex() == index) {
                    inlet.update(health, value);
                    break;
                }
            }
        }
    }

    private void updateOutlets(JSONArray outlets) {
        for (int i = 0; i < outlets.length(); i++) {
            JSONObject jsonOutlet = outlets.optJSONObject(i);
            if (jsonOutlet == null || !jsonOutlet.has("index")) {
                continue;
            }
            int index = jsonOutlet.optInt("index");
            boolean active = jsonOutlet.optString("value").equalsIgnoreCase("EIN");
            for (Outlet outlet : outletsList) {
                if (outlet.getIndex() == index) {
                    outlet.update(active);
                    break;
                }
            }
        }
    }

    private void updateNotifies(JSONArray notifies) {
        for (int i = 0; i < notifies.length(); i++) {
            JSONObject jsonNotifies = notifies.optJSONObject(i);
            if (jsonNotifies == null || !jsonNotifies.has("index")) {
                continue;
            }
            int index = jsonNotifies.optInt("index");
            boolean active = jsonNotifies.optString("state").equalsIgnoreCase("EIN");
            for (Notify notify : notifiesList) {
                if (notify.getIndex() == index) {
                    notify.update(active);
                    break;
                }
            }
        }
    }

    public List<Inlet> getInletsList() {
        return inletsList;
    }

    public List<Outlet> getOutletsList() {
        return outletsList;
    }

    public List<Notify> getNotifiesList() {
        return notifiesList;
    }

    public Date getDate() {
        return date;
    }
}
