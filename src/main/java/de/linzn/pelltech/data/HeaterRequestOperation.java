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

package de.linzn.pelltech.data;

import de.linzn.pelltech.PelltechPlugin;
import de.linzn.stem.STEMApp;
import de.linzn.stem.taskManagment.operations.AbstractOperation;
import de.linzn.stem.taskManagment.operations.OperationOutput;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Asks the connected StemLink clients for new heater data.
 */
public class HeaterRequestOperation extends AbstractOperation {

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.writeUTF("request");
        } catch (IOException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error(e);
            operationOutput.setExit(-1);
            return operationOutput;
        }
        /* StemLink is not part of the plugin API, so the core is used directly */
        byte[] data = byteArrayOutputStream.toByteArray();
        STEMApp.getInstance().getStemLinkModule().getStemLinkServer().getClients().values()
                .forEach(serverConnection -> serverConnection.writeOutput("heater_data", data));
        operationOutput.setExit(0);
        return operationOutput;
    }
}
