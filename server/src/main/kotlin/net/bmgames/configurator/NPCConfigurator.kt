package net.bmgames.configurator

import com.google.gson.Gson

class NPCConfigurator{
    companion object {
        fun createNPC(id:String,type:String, name:String, greeting:String, items:Collection<Item>):NPCConfig
        {
            return NPCConfig(id, type, name, greeting, items)
        }

        fun createNPCInventory(ids:String ):Collection<Item>
        {
            val npcInventory: MutableList<Item> = mutableListOf<Item>()
            val idArray = ids.split(", ", ",").toTypedArray()
            for(id in idArray)
            {
                for(item in ConfiguratorMain.allItems)
                {
                    if(id == item.id)
                    {
                        npcInventory.add(item)
                    }
                }
            }
            return npcInventory
        }


        fun fullyCreateNPC(itemIDs: String,id: String, type:String, name:String, greeting:String)
        {
            val inventory = createNPCInventory(itemIDs)
            val NPC = createNPC(id, type, name, greeting, inventory)
            RoomConfigurator.npcList.add(NPC)
        }
    }
}