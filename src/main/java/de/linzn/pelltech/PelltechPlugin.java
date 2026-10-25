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

import de.linzn.pelltech.command.HeatingCommand;
import de.linzn.pelltech.data.HeaterCallback;
import de.linzn.pelltech.dblogger.DBLogger;
import de.linzn.pelltech.webApi.WebApiHandler;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

public class PelltechPlugin extends STEMPlugin {

    public static PelltechPlugin pelltechPlugin;
    public DBLogger dbLogger;
    public HeaterProcessor2 heaterProcessor;
    private WebApiHandler webApiHandler;

    public PelltechPlugin() {
        pelltechPlugin = this;
    }

    @Override
    public void onEnable() {
        this.dbLogger = new DBLogger();
        this.dbLogger.createTables();
        this.heaterProcessor = new HeaterProcessor2();
        this.getStemApi().scheduler().registerCallback(new HeaterCallback());
        this.getStemApi().commands().register("heating", new HeatingCommand());
        this.getStemApi().mqtt().subscribe("uvr/canbus/data", new MqttCanbusListener());
        this.webApiHandler = new WebApiHandler(this);
    }

    @Override
    public void onDisable() {
        if (this.webApiHandler != null) {
            this.webApiHandler.disable();
        }
    }
}
