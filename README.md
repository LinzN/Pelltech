![MirraNET Logo](https://raw.githubusercontent.com/LinzN/Stem/master/stem-core/src/main/resources/header-logo.png)

# pelltech

> Pellet heating data from the CAN bus in [STEM](https://github.com/LinzN/Stem).

pelltech receives the values of a pellet heating (temperatures, outputs and error messages) from a CAN bus
bridge via MQTT, shows them on the console and through [web-api](https://github.com/LinzN/web-api) and
raises a high priority notification as soon as the heating reports a new error.

---

## Features

- Receives CAN bus data on the MQTT topic `uvr/canbus/data`
- Asks connected StemLink clients for new data every 3 minutes (channel `heater_data`)
- Inlets (sensor values), outlets (on/off) and notifies (error messages) are defined in the database;
  indexes the CAN bus sends that are not defined yet are added automatically
- New active notify: notification with priority `HIGH` and an information block on the display
- `MQTTCanbusReceiveEvent` is fired for every message and can be canceled by other plugins

---

## Requirements

| Requirement | Version                                                        |
|-------------|----------------------------------------------------------------|
| STEM        | 1.2.0 or newer (full `stem` dependency, uses StemLink), database module configured |
| Plugins     | [web-api](https://github.com/LinzN/web-api)                     |
| Hardware    | CAN bus bridge that publishes JSON to `uvr/canbus/data`         |

---

## Installation

1. Build the plugin with `mvn clean package` (or take the build from Jenkins).
2. Copy `target/pelltech.jar` into the `plugins` folder of STEM.
3. Start STEM. Missing database tables are created automatically.
4. Optional: give the entries meaningful names in the database (see below) and restart STEM.

There is no `config.yml`.

---

## Database

All tables are created automatically if they do not exist (MySQL/MariaDB). The inlets, outlets and notifies
are loaded at startup from these tables:

| Table                   | Columns                      |
|-------------------------|------------------------------|
| `addon_heater_inlets`   | `inlet_id`, `index`, `name`  |
| `addon_heater_outlets`  | `outlet_id`, `index`, `name` |
| `addon_heater_notifies` | `notify_id`, `index`, `name` |

When the CAN bus sends an index that is not in the table yet, it is added with a placeholder name such as
`Inlet 7`. Rename it in the database and restart STEM to show a meaningful name:

```sql
UPDATE addon_heater_inlets SET name = 'Kesseltemperatur' WHERE `index` = 7;
```

The tables `addon_heater_inlets_data`, `addon_heater_outlets_data` and `addon_heater_notifies_data` are
created for history data.

`index` is the index used by the CAN bus bridge. Two indexes have a fixed meaning for the web API:
inlet `3` is the water temperature, outlet `1` is the burner.

---

## MQTT payload

```json
{
  "inlets":   [ { "index": 3, "state": "OK",  "value": 64.5 } ],
  "outlets":  [ { "index": 1, "value": "EIN" } ],
  "notifies": [ { "index": 2, "state": "AUS" } ]
}
```

An inlet is healthy when `state` is `OK`. Outlets and notifies are active when the value is `EIN`.

---

## Commands

| Command          | Description                                                  |
|------------------|--------------------------------------------------------------|
| `heating status` | Lists all inlets, outlets, notifies and the time of the last data |

---

## Web API

`/pelltech/data` returns:

| Field      | Description                                                                        |
|------------|------------------------------------------------------------------------------------|
| `inlets`   | `name` and `value` of every inlet                                                  |
| `outlets`  | `name` and `status` of every outlet                                                |
| `notifies` | `name` and `status` of every notify                                                |
| `simple`   | `watertemperature`, `burnerstatus` (0 = off, 1 = burning, 2 = error), `date`, `datesimple` |

---

## License

Copyright (c) MirraNET, Niklas Linz. Licensed under the
[GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.html).
