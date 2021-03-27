package net.bmgames.configurator

class ClassConfigurator
{
    companion object
    {
        fun addClass(id: String)
        {
            ConfiguratorMain.classList.add(ClassConfig(id))
        }
    }

}