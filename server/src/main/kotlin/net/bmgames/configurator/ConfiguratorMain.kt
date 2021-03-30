package net.bmgames.configurator

import com.google.gson.*

class ConfiguratorMain {
    companion object {
        var allMUDs: MutableMap<String, String> = mutableMapOf()
        var allItems: MutableList<Item> = mutableListOf()
        var roomList: MutableList<RoomConfig> = mutableListOf()
        var raceList: MutableList<RaceConfig> = mutableListOf()
        var classList: MutableList<ClassConfig> = mutableListOf()
        var startingEquipment: MutableList<Item> = mutableListOf()

        fun createGameConfig(id: String, startingRoom: String) {
            val gameConfig = GameConfig(id, startingRoom, roomList, raceList, classList, startingEquipment)
            val gameConfigJson = Gson().toJson(gameConfig)
            allMUDs.put(gameConfig.name, gameConfigJson)
        }


    }
}
