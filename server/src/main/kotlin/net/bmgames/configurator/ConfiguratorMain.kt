package net.bmgames.configurator

import com.google.gson.*

class ConfiguratorMain
{
    companion object{
        var allItems: MutableList<Item> = mutableListOf()
        var roomList: MutableList<RoomConfig> = mutableListOf()
        var raceList: MutableList<RaceConfig> = mutableListOf()
        var classList: MutableList<ClassConfig> = mutableListOf()
        var startingEquipment: MutableList<Item> = mutableListOf()

        fun createGameConfig(id:String, startingRoom:String):GameConfig
        {
            return GameConfig(id, roomList, startingRoom, raceList, classList, startingEquipment)
        }

        fun generateGameJson(game: GameConfig) :String
        {
            return Gson().toJson(game)
        }

    }
}