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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Applies canbus data to the configured inlets, outlets and notifies.
 * Indexes that are not in the database yet are added with a placeholder name that can be changed there.
 */
public class HeaterProcessor2 {
    private final CopyOnWriteArrayList<Inlet> inletsList;
    private final CopyOnWriteArrayList<Outlet> outletsList;
    private final CopyOnWriteArrayList<Notify> notifiesList;
    private volatile Date date;

    public HeaterProcessor2() {
        this.inletsList = new CopyOnWriteArrayList<>(PelltechPlugin.pelltechPlugin.dbLogger.loadInlets());
        this.outletsList = new CopyOnWriteArrayList<>(PelltechPlugin.pelltechPlugin.dbLogger.loadOutlets());
        this.notifiesList = new CopyOnWriteArrayList<>(PelltechPlugin.pelltechPlugin.dbLogger.loadNotifies());
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
            Inlet inlet = inletsList.stream().filter(entry -> entry.getIndex() == index).findFirst().orElse(null);
            if (inlet == null) {
                inlet = new Inlet(index, "Inlet " + index);
                PelltechPlugin.pelltechPlugin.dbLogger.registerInlet(inlet);
                inletsList.add(inlet);
            }
            inlet.update(health, value);
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
            Outlet outlet = outletsList.stream().filter(entry -> entry.getIndex() == index).findFirst().orElse(null);
            if (outlet == null) {
                outlet = new Outlet(index, "Outlet " + index);
                PelltechPlugin.pelltechPlugin.dbLogger.registerOutlet(outlet);
                outletsList.add(outlet);
            }
            outlet.update(active);
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
            Notify notify = notifiesList.stream().filter(entry -> entry.getIndex() == index).findFirst().orElse(null);
            if (notify == null) {
                notify = new Notify(index, "Notify " + index);
                PelltechPlugin.pelltechPlugin.dbLogger.registerNotify(notify);
                notifiesList.add(notify);
            }
            notify.update(active);
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
