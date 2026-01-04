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

package de.linzn.pelltech.objects;

public class Outlet {
    private int index;
    private String name;
    private boolean active;
    private long date;

    public Outlet(int index, String name) {
        this.index = index;
        this.name = name;
        this.active = false;
        this.date = System.currentTimeMillis();
    }

    public void update(boolean active) {
        this.active = active;
        this.date = System.currentTimeMillis();
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

    public long getDate() {
        return date;
    }
}
