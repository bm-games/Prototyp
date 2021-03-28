package net.bmgames.configurator

class RaceConfigurator {
    companion object{
        fun addRace(id: String)
        {
            ConfiguratorMain.raceList.add(RaceConfig(id))
        }
    }
}