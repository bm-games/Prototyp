package net.bmgames

import com.typesafe.config.Config
import io.ktor.application.*
import io.ktor.auth.*
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
import kotlinx.css.CSSBuilder
import kotlinx.html.*
import net.bmgames.user.User
import java.time.Duration

import io.ktor.sessions.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import net.bmgames.user.FullUserInfo
import net.bmgames.user.Userinfo
import org.apache.http.auth.AUTH


val client = HttpClient(CIO){
    install(JsonFeature) {
        serializer = GsonSerializer{}
    }
}
var apikey: String? = null;

fun main(args: Array<String>){
    EngineMain.main(args)
}


/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
//@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

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

    install(Sessions) {
        cookie<User>("UserIdentifier", storage = SessionStorageMemory())
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

        /*
        * JSON
        */
        get("/json") {
            val user = call.sessions.get<User>()
            if (user != null){
                call.respond(user)
            }else{
                call.respond("Doener")
            }



        }
        post("/json") {
            val customer = call.receive<User>()
            call.respond(customer)
        }

        apikey = environment.config.propertyOrNull("auth0.apikey")?.getString()

        install(ContentNegotiation) {
            json()
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
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
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

fun ApplicationCall.getUsername(): String? {
    return sessions.get<User>()?.username
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

private suspend fun ApplicationCall.loggedInSuccessResponse(callback: OAuthAccessTokenResponse.OAuth2) {
    val accessToken: String = callback.accessToken

    val userInfoWithOutUsername: Userinfo = client.get<Userinfo>("https://bm-games.eu.auth0.com/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    val fullUserInfo: FullUserInfo = client.get<FullUserInfo>("https://bm-games.eu.auth0.com/api/v2/users/${userInfoWithOutUsername.sub}") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $apikey")
        }
    }
    sessions.set(User(user_id = userInfoWithOutUsername.sub, username = fullUserInfo.username, accessToken = callback.accessToken))

    respondHtml {
        head {
            title { +"Logged in" }
        }
        body {
            h1 {
                +"You are logged in"
            }
            p {
               +"Your Username is ${fullUserInfo.username}, your User_ID is ${userInfoWithOutUsername.sub} and your AccessToken is ${callback.accessToken}"
            }
        }
    }
}

