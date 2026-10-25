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

package de.linzn.pelltech.command;


import de.linzn.pelltech.PelltechPlugin;
import de.linzn.pelltech.objects.Inlet;
import de.linzn.pelltech.objects.Notify;
import de.linzn.pelltech.objects.Outlet;
import de.linzn.stem.modules.commandModule.ICommand;

import java.util.Date;
import java.util.List;

public class HeatingCommand implements ICommand {
    @Override
    public boolean executeTerminal(String[] strings) {
        if (strings.length > 0) {
            String command = strings[0];

            if (command.equalsIgnoreCase("status")) {
                status();
            }
        }
        return true;
    }

    private void status() {
        List<Inlet> inlets = PelltechPlugin.pelltechPlugin.heaterProcessor.getInletsList();
        List<Outlet> outlets = PelltechPlugin.pelltechPlugin.heaterProcessor.getOutletsList();
        List<Notify> notifies = PelltechPlugin.pelltechPlugin.heaterProcessor.getNotifiesList();

        Date lastSyncDate = PelltechPlugin.pelltechPlugin.heaterProcessor.getDate();
        String lastSync = lastSyncDate == null ? "never" : ((System.currentTimeMillis() - lastSyncDate.getTime()) / 1000) + " seconds ago!";

        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("## Inlets:");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        for (Inlet inlet : inlets) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().info(inlet.getName() + " status -> " + inlet.isHealth() + " value -> " + inlet.getValue());
        }

        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("## Outlets:");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        for (Outlet outlet : outlets) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().info(outlet.getName() + " active -> " + outlet.isActive());
        }
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("## Notifies:");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        for (Notify notify : notifies) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().info(notify.getName() + " status -> " + notify.isActive());
        }

        PelltechPlugin.pelltechPlugin.getPluginLogger().info("############################################");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info("## Last sync:");
        PelltechPlugin.pelltechPlugin.getPluginLogger().info(lastSync);
    }
}
