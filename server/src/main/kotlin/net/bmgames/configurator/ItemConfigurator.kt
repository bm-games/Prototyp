package net.bmgames.configurator

class ItemConfigurator {
    companion object{
        fun createItem(id: String, name: String)
        {
            ConfiguratorMain.allItems.add(Item(id, name))
        }
    }
}