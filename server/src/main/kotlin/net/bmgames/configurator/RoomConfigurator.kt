package net.bmgames.configurator

class RoomConfigurator {
    companion object{
        var npcList: MutableList<NPCConfig> = mutableListOf<NPCConfig>()

        fun createRoom(id: String,north: String?, east: String?, south: String?, west: String?, message: String)
        {
            val room = RoomConfig(id, north, east, south, west, message, npcList)
            npcList.clear()
            ConfiguratorMain.roomList.add(room)
        }
    }
}