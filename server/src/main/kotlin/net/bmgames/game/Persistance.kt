package net.bmgames.game

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.bmgames.configurator.*

val dummyConfig = GameConfig(
    name = "Demo",
    startRoom = "start",
    rooms = listOf(
        RoomConfig(
            id = "start", message = "Hi",
            south = "a", east = "c"
        ),
        RoomConfig(
            id = "a", message = "Raum A",
            east = "b", north = "start",
            items = listOf(
                Item("260", "Apfel"),
                Item("268", "Holzschwert")
            )
        ),
        RoomConfig(
            id = "b", message = "Raum B",
            north = "c", west = "b",
        ),
        RoomConfig(
            id = "c", message = "Raum C",
            west = "start", south = "b",
            items = listOf(
                Item("298", "Helm")
            )
        )
    ),
    races = listOf(
        RaceConfig("Mensch")
    ),
    classes = listOf(
        ClassConfig("Student")
    ),
    startingEquipment = emptyList()
)

val coolMUD =
    Json.decodeFromString<GameConfig>("""{ "name": "Cooler MUD", "rooms": [ { "id": "5", "north": "6", "east": "", "south": "", "west": "", "message": "Willkommen in St. GeORKen", "NPCs": [ { "id": "4", "type": "Ork", "name": "Geork", "greeting": "Guten mORKen", "items": [ { "id": "3", "name": "Bohne" } ] } ] }, { "id": "6", "north": "", "east": "", "south": "5", "west": "", "message": "SouthsideCity", "NPCs": [] } ], "startRoom": "5", "races": [ { "name": "Gurke" }, { "name": "Paprika" } ], "classes": [ { "name": "Magier" }, { "name": "Kampfgurke" } ], "startingEquipment": [ { "id": "1", "name": "Schwert" }, { "id": "2", "name": "Schild" } ] }""")

/**
 * Hier würde der Gamestate aus einer Datei oder DB geladen werden
 * */
fun loadGameState(id: Id): Game? =
    getConfig(id)?.let {
        Game(
            id = it.name,
            config = it,
            startRoom = it.startRoom,
            rooms = it.rooms.map { room -> Room(room, room.items) },
            offlinePlayers = emptyList(),
            onlinePlayers = emptyList()
        )
    }

private fun getConfig(id: Id): GameConfig? = ConfiguratorMain.allMUDs[id]?.let { Json.decodeFromString<GameConfig>(it) }

fun Game.createInitialPlayer(avatar: Avatar): IngamePlayer =
        IngamePlayer.Normal(
            user = avatar.name,
            avatar = avatar,
            room = startRoom,
            inventory = config.startingEquipment
        )
