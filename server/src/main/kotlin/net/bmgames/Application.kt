package net.bmgames

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.websocket.*
import net.bmgames.configurator.ui.configEndpoints
import net.bmgames.configurator.ui.configPage
import net.bmgames.game.gameServer
import net.bmgames.user.User
import net.bmgames.user.setupAuth
import java.time.Duration
import kotlin.collections.set

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    // Ermöglicht Nutzung von Klassen für Endpoints
    install(Locations)
    install(Sessions) {
        cookie<User>("UserIdentifier") {
            val secretEncryptKey = hex("00112233445566778899aabbccddeeff")
            val secretAuthKey = hex("02030405060708090a0b0c")
            cookie.extensions["SameSite"] = "lax"
            cookie.httpOnly = true
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
        }
    }

    install(CallLogging) {
        filter { call -> call.request.path().startsWith("/") }
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

        gameServer()
        configPage()
        configEndpoints()

        authenticate("auth0") {
            loginPage(authConfig)
        }
        get("/logout") {
            call.sessions.clear<User>();
            call.respondRedirect("/")
        }
/*
        routing {
            static("static") {
                // When running under IDEA make sure that working directory is set to this sample's project folder
                staticRootFolder = File("files")
                files("xterm")
            }
        }*/

    }
}


