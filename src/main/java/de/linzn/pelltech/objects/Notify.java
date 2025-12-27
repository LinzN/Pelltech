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

package de.linzn.pelltech.objects;


import de.linzn.pelltech.PelltechPlugin;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationIntent;
import de.linzn.stem.modules.notificationModule.NotificationPriority;

public class Notify {
    private int index;
    private String name;
    private boolean active;
    private long date;

    private InformationBlock informationBlock = null;

    public Notify(int index, String name) {
        this.index = index;
        this.name = name;
        this.active = false;
        this.date = System.currentTimeMillis();
    }

    public void update(boolean active) {
        boolean oldValue = this.active;
        this.active = active;
        this.date = System.currentTimeMillis();
        this.checkStatus(oldValue);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    private void checkStatus(boolean oldValue) {
        if (!oldValue) {
            if (this.active) {
                String message = "New notify " + this.name.toUpperCase() + " with state ACTIVE is called!";
                STEMApp.getInstance().getNotificationModule().pushNotification(message, NotificationPriority.HIGH, PelltechPlugin.pelltechPlugin);

                if (this.informationBlock != null && this.informationBlock.isActive()) {
                    this.informationBlock.expire();
                    this.informationBlock = null;
                }
                this.informationBlock = new InformationBlock("Pelltech Heizung", "Neue Meldung: " + this.name.toUpperCase(), PelltechPlugin.pelltechPlugin);
                this.informationBlock.setIcon("FIRE");
                this.informationBlock.setExpireTime(-1L);
                this.informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
                STEMApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);

            } else {
                if (this.informationBlock != null && this.informationBlock.isActive()) {
                    this.informationBlock.expire();
                    this.informationBlock = null;
                }
            }
        }
    }

    public long getDate() {
        return date;
    }
}
