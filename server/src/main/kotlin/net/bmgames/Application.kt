package net.bmgames

import com.typesafe.config.Config
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.forms.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*

import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.websocket.*

import kotlinx.html.*
import kotlinx.serialization.Serializable
import net.bmgames.configurator.*
import net.bmgames.configurator.ui.Configurator

import net.bmgames.game.gameServer

import net.bmgames.user.User
import net.bmgames.user.setupAuth
import java.io.File
import java.time.Duration


fun main(args: Array<String>) {
    EngineMain.main(args)
}


@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    // Ermöglicht Nutzung von Klassen für Endpoints
    install(Locations)
    install(Sessions) {
        cookie<User>("UserIdentifier", storage = SessionStorageMemory())
    }

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }


    val authConfig = setupAuth()

    routing {
        indexPage()


        routing {
            static("static") {
                // When running under IDEA make sure that working directory is set to this sample's project folder
                staticRootFolder = File("files")
                files("xterm")
            }
        }

        authenticate("auth0") {
            this@routing.gameServer()
            loginPage(authConfig)


            get("/config") {
                call.respondHtmlTemplate(Configurator()) { }
            }

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
                RoomConfigurator.createRoom(config.id, config.north, config.east, config.south, config.west, config.message)
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


