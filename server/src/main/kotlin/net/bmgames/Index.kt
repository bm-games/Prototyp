@file:OptIn(KtorExperimentalLocationsAPI::class)
package net.bmgames

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.html.*

@Location("/")
class Index

fun Routing.indexPage() {
    get<Index> {
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
}
