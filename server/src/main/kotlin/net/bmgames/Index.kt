@file:OptIn(KtorExperimentalLocationsAPI::class)

package net.bmgames

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import io.ktor.locations.*
import net.bmgames.game.ui.Dashboard

@Location("/")
class Index

val loginPage: HTML.() -> Unit = {
    head {
        title { +"BM-Games Prototyp" }
    }
    body {
        h1 {
            +"Willkommen bei BlackMamba Games"
        }
        p {
            a(href = "login") {
                +"Login"
            }
        }
    }
}

fun Routing.indexPage() {
    get<Index> {
        if (user == null) {
            call.respondHtml(block = loginPage)
        } else {
            call.respondRedirect(call.url(Dashboard()))
        }
    }
}
