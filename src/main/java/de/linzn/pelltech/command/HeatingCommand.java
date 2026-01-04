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
import de.linzn.stem.STEMApp;
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
        return false;
    }

    private void status() {
        List<Inlet> inlets = PelltechPlugin.pelltechPlugin.heaterProcessor.getInletsList();
        List<Outlet> outlets = PelltechPlugin.pelltechPlugin.heaterProcessor.getOutletsList();
        List<Notify> notifies = PelltechPlugin.pelltechPlugin.heaterProcessor.getNotifiesList();

        long lastSync = (new Date().getTime() - PelltechPlugin.pelltechPlugin.heaterProcessor.getDate().getTime()) / 1000;

        STEMApp.LOGGER.LIVE("############################################");
        STEMApp.LOGGER.LIVE("## Inlets:");
        STEMApp.LOGGER.LIVE("############################################");
        for (Inlet inlet : inlets) {
            STEMApp.LOGGER.LIVE(inlet.getName() + " status -> " + inlet.isHealth() + " value -> " + inlet.getValue());
        }

        STEMApp.LOGGER.LIVE("############################################");
        STEMApp.LOGGER.LIVE("## Outlets:");
        STEMApp.LOGGER.LIVE("############################################");
        for (Outlet outlet : outlets) {
            STEMApp.LOGGER.LIVE(outlet.getName() + " active -> " + outlet.isActive());
        }
        STEMApp.LOGGER.LIVE("############################################");
        STEMApp.LOGGER.LIVE("## Notifies:");
        STEMApp.LOGGER.LIVE("############################################");
        for (Notify notify : notifies) {
            STEMApp.LOGGER.LIVE(notify.getName() + " status -> " + notify.isActive());
        }

        STEMApp.LOGGER.LIVE("############################################");
        STEMApp.LOGGER.LIVE("## Last sync:");
        STEMApp.LOGGER.LIVE(lastSync + " seconds ago!");
    }
}
