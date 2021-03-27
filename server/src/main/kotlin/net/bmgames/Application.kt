package net.bmgames

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.forms.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.css.CSSBuilder
import kotlinx.html.*
import kotlinx.html.dom.document
import kotlinx.serialization.Serializable
import net.bmgames.configurator.*
import net.bmgames.configurator.ui.Configurator
import net.bmgames.user.User
import java.time.Duration

fun main(args: Array<String>): Unit =
    EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
//@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        json()
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

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }


    /*
    * Websockets
    */
    routing {
        installChatServer()
    }

    setupAuth()

    /*
    * HTML
    */
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"index page" }
                }
                body {
                    h1 {
                        +"Try to login"
                    }
                    p {
                        a(href = "login") {
                            +"Login"
                        }
                    }
                }
            }
        }

        get("/config"){
            call.respondHtmlTemplate(Configurator()){ }
        }

        post("/createMUD"){
            val config = call.receive<Config>()
            ConfiguratorMain.createGameConfig(config.id, config.startRoomId)
            println(config)
        }

        post("/createItem"){
            val config = call.receive<ItemConfig>()
            ItemConfigurator.createItem(config.id, config.name)
            println(config)
        }
        post("/createNPC"){
            val config = call.receive<NPCConfig>()
            NPCConfigurator.fullyCreateNPC(config.items, config.id, config.type, config.name, config.greeting)
            println(config)
        }
        post("/createRoom"){
            val config = call.receive<RoomConfig>()
            RoomConfigurator.createRoom(config.id, config.north, config.east, config.south, config.west, config.message)
            println(config)
        }
        post("/createClass"){
            val config = call.receive<ClassConfig>()
            ClassConfigurator.addClass(config.id)
            println(config)
        }
        post("/createRace"){
            val config = call.receive<RaceConfig>()
            RaceConfigurator.addRace(config.id)
            println(config)
        }
        post("/createStartequipment"){
            val config = call.receive<StartequipmentConfig>()
            StartingEquipmentConfigurator.defineStartingEquipment(config.id)
            println(config)
        }
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

        /*
        * Auth
        */
        authenticate("auth0") {
            route("login") {
                param("error") {
                    handle {
                        call.loginFailedPage(call.parameters.getAll("error").orEmpty())
                    }
                }

                handle {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse>()
                    if (principal != null) {
                        call.loggedInSuccessResponse(principal)
                    } else {
                        call.respondHtml {
                            head {
                                title { +"index page" }
                            }
                            body {
                                h1 {
                                    +"Try again"
                                }
                                p {
                                    a(href = "login") {
                                        +"Login"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class Config(val id: String, val startRoomId: String)
@Serializable
data class ItemConfig(val id: String, val name: String)
@Serializable
data class NPCConfig(val id: String, val name: String, val type: String, val greeting: String, val items: String)
@Serializable
data class RoomConfig(val id: String, val north: String, val east: String, val south: String, val west: String, val message: String)
@Serializable
data class ClassConfig(val id: String)
@Serializable
data class RaceConfig(val id: String)
@Serializable
data class StartequipmentConfig(val id: String)



suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}

private suspend fun ApplicationCall.loginFailedPage(errors: List<String>) {
    respondHtml {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login error"
            }

            for (e in errors) {
                p {
                    +e
                }
            }
        }
    }
}

private suspend fun ApplicationCall.loggedInSuccessResponse(callback: OAuthAccessTokenResponse) {
    respondHtml {
        head {
            title { +"Logged in" }
        }
        body {
            h1 {
                +"You are logged in"
            }
            p {
                +"Your token is $callback"
            }
        }
    }
}
