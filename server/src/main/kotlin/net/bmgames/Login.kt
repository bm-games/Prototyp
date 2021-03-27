package net.bmgames


import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*


fun Route.loginPage() {
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
    }
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
            a("/dashboard") {
                +"See available games"
            }

        }
    }
}
