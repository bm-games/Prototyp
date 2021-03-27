package net.bmgames.configurator

class StartingEquipmentConfigurator{
    companion object{
        fun defineStartingEquipment(ids:String )
        {
            val _startingEquipment: MutableList<Item> = mutableListOf<Item>()
            val idArray = ids.split(", ", ",").toTypedArray()
            for(id in idArray)
            {
                for(item in ConfiguratorMain.allItems)
                {
                    if(id == item.id)
                    {
                        _startingEquipment.add(item)
                    }
                }
            }
            ConfiguratorMain.startingEquipment = _startingEquipment
        }
    }
}