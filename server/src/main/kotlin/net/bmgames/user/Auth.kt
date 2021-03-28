package net.bmgames.user

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.locations.*


data class Auth0Config(
    val url: String,
    val clientId: String,
    val clientSecret: String,
    val apikey: String,
//    val audience: String
) {
    val accessTokenUrl = "$url/oauth/token"
    val authorizeUrl = "$url/authorize"
//    val logoutUrl = "$url/v1/logout"
}

fun auth0ConfigReader(config: Config): Auth0Config =
    Auth0Config(
        url = config.getString("auth0.url"),
        clientId = config.getString("auth0.clientId"),
        clientSecret = config.getString("auth0.clientSecret"),
        apikey = config.getString("auth0.apikey")
//        audience = config.tryGetString("auth0.audience") ?: "api://default"
    )

fun Auth0Config.asOAuth2Config(): OAuthServerSettings.OAuth2ServerSettings =
    OAuthServerSettings.OAuth2ServerSettings(
        name = "auth0",
        authorizeUrl = authorizeUrl,
        accessTokenUrl = accessTokenUrl,
        clientId = clientId,
        clientSecret = clientSecret,
        requestMethod = Post,
        defaultScopes = listOf("openid","profile","email","nickname","sub","name","preferred_username","username")
    )

@KtorExperimentalLocationsAPI
fun Application.setupAuth(): Auth0Config {
    val config = auth0ConfigReader(ConfigFactory.load() ?: throw Exception("Could not load config"))
    install(Authentication) {
        oauth("auth0") {
            urlProvider = { url("/login") }
            providerLookup = { config.asOAuth2Config() }
            client = HttpClient(Apache)
        }
    }
    return config
}
