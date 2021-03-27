package net.bmgames.game

import net.bmgames.configurator.*

private val dummyConfig = GameConfig(
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


/**
 * Hier würde der Gamestate aus einer Datei oder DB geladen werden
 * */
fun loadGameState(id: Id): Game? =
    Game(
        id = id,
        config = dummyConfig,
        startRoom = dummyConfig.startRoom,
        rooms = dummyConfig.rooms.map { Room(it, it.items) },
        offlinePlayers = emptyList(),
        onlinePlayers = emptyList()
    )

fun Game.createInitialPlayer(avatar: Avatar): IngamePlayer =
    if (avatar.name == "player2")
        IngamePlayer.Master(avatar.name, avatar.name)
    else
        IngamePlayer.Normal(
            user = avatar.name,
            avatar = avatar,
            room = startRoom,
            inventory = config.startingEquipment
        )
