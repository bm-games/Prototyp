package net.bmgames

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.css.CSSBuilder
import kotlinx.html.*
import net.bmgames.game.installGameServer
import net.bmgames.user.User
import net.bmgames.user.setupAuth
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

    // Ermöglicht Nutzung von Klassen für Endpoints
    install(Locations)

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

    installGameServer()

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
