@file:OptIn(KtorExperimentalLocationsAPI::class)

package net.bmgames


import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import net.bmgames.game.ui.Dashboard
import net.bmgames.user.Auth0Config
import net.bmgames.user.FullUserInfo
import net.bmgames.user.User
import net.bmgames.user.Userinfo

@Location("/login")
class Login

fun Route.loginPage(config: Auth0Config) {
    location<Login> {
        param("error") {
            handle {
                call.loginFailedPage(call.parameters.getAll("error").orEmpty())
            }
        }

        handle {
            val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
            if (principal != null) {
                call.loggedInSuccessResponse(principal, config)
            } else {
                call.respondHtml(block = loginPage)
            }
        }
    }
}


private suspend fun ApplicationCall.loginFailedPage(errors: List<String>) {
    respondHtml {
        head {
            title { +"Login error" }
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

val client = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = GsonSerializer {}
    }
}

private suspend fun ApplicationCall.loggedInSuccessResponse(
    callback: OAuthAccessTokenResponse.OAuth2,
    config: Auth0Config,
) {
    val apikey = config.apikey
    val accessToken: String = callback.accessToken

    val userInfoWithOutUsername: Userinfo = client.get<Userinfo>("https://bm-games.eu.auth0.com/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    val fullUserInfo: FullUserInfo =
        client.get<FullUserInfo>("https://bm-games.eu.auth0.com/api/v2/users/${userInfoWithOutUsername.sub}") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $apikey")
            }
        }
    sessions.set(
        User(
            user_id = userInfoWithOutUsername.sub,
            username = fullUserInfo.username,
            accessToken = callback.accessToken
        )
    )
    respondRedirect(url(Dashboard()))
/*
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
            a("/dashboard") {
                +"See available games"
            }

        }
    }*/
}


val PipelineContext<Unit, ApplicationCall>.user: User?
    get() = call.sessions.get<User>()


suspend fun PipelineContext<Unit, ApplicationCall>.authenticated(f: suspend User.() -> Unit) {
    val user = call.sessions.get<User>()
    if (user == null) {
        call.respond(HttpStatusCode.Unauthorized)
    } else {
        f(user)
    }
}
