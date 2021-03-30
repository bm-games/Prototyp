package net.bmgames.configurator

class RoomConfigurator {
    companion object{
        var npcList: MutableList<NPCConfig> = mutableListOf<NPCConfig>()

        fun createRoom(id: String,north: String?, east: String?, south: String?, west: String?, message: String)
        {
            val list: MutableList<NPCConfig> = mutableListOf<NPCConfig>()
            for(item in npcList){
                list.add(item)
            }
            val room = RoomConfig(id, north, east, south, west, message, list)
            ConfiguratorMain.roomList.add(room)
            npcList.clear()
        }
    }
}
