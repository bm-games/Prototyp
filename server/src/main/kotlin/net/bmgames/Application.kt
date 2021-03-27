package net.bmgames

import io.ktor.application.*
import io.ktor.auth.*
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
import kotlinx.serialization.Serializable
import net.bmgames.game.gameServer
import net.bmgames.user.setupAuth
import java.io.File
import java.time.Duration

fun main(args: Array<String>): Unit =
    EngineMain.main(args)


/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    // Ermöglicht Nutzung von Klassen für Endpoints
    install(Locations)
    install(Sessions)

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
            loginPage()
        }
    }
}

@Serializable
data class Config (val name: String)
/*

        /*
        * JSON
        */
        get("/json") {
            call.respond(User("test@test.de"))
        }
        post("/json") {
            val customer = call.receive<User>()
            call.respond(customer)
        }
    //Wird für Browserclient benötigt, wenn dieser auf anderer URL/Port läuft als der Webserver
    /*install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }*/

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}

 */

