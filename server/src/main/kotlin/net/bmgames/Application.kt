package net.bmgames

import com.typesafe.config.Config
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.features.*
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
import net.bmgames.game.gameServer
import net.bmgames.user.User
import net.bmgames.user.setupAuth
import java.io.File
import java.time.Duration


fun main(args: Array<String>){
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


    setupAuth()

    routing {
        indexPage()

        routing {
            static("static") {
                // When running under IDEA make sure that working directory is set to this sample's project folder
                staticRootFolder = File("files")
                files("xterm")
            }
        }
        post("/createMUD") {
            val config = call.receive<Config>()
            println(config)
            call.respond(config)
        }
        authenticate("auth0") {
            this@routing.gameServer()
            loginPage(environment)
        }
    }
}
