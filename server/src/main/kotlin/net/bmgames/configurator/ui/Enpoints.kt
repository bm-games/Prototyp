package net.bmgames.configurator.ui

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import net.bmgames.configurator.*

fun Route.configEndpoints() {

    post("/createMUD") {
        val config = call.receive<MUDConfig>()
        ConfiguratorMain.createGameConfig(config.id, config.startRoomId)
        println(config)
    }

    post("/createItem") {
        val config = call.receive<ItemConfig>()
        ItemConfigurator.createItem(config.id, config.name)
        println(config)
    }
    post("/createNPC") {
        val config = call.receive<NPCConfig>()
        NPCConfigurator.fullyCreateNPC(config.items, config.id, config.type, config.name, config.greeting)
        println(config)
    }
    post("/createRoom") {
        val config = call.receive<RoomConfig>()
        RoomConfigurator.createRoom(
            config.id,
            config.north,
            config.east,
            config.south,
            config.west,
            config.message
        )
        println(config)
    }
    post("/createClass") {
        val config = call.receive<ClassConfig>()
        ClassConfigurator.addClass(config.id)
        println(config)
    }
    post("/createRace") {
        val config = call.receive<RaceConfig>()
        RaceConfigurator.addRace(config.id)
        println(config)
    }
    post("/createStartequipment") {
        val config = call.receive<StartequipmentConfig>()
        StartingEquipmentConfigurator.defineStartingEquipment(config.id)
        println(config)
    }
}

@Serializable
data class MUDConfig(val id: String, val startRoomId: String)

@Serializable
data class ItemConfig(val id: String, val name: String)

@Serializable
data class NPCConfig(val id: String, val name: String, val type: String, val greeting: String, val items: String)

@Serializable
data class RoomConfig(
    val id: String,
    val north: String,
    val east: String,
    val south: String,
    val west: String,
    val message: String,
)

@Serializable
data class ClassConfig(val id: String)

@Serializable
data class RaceConfig(val id: String)

@Serializable
data class StartequipmentConfig(val id: String)
