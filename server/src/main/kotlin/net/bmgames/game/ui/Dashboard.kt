@file:OptIn(KtorExperimentalLocationsAPI::class)

package net.bmgames.game.ui


import arrow.fx.coroutines.Atomic
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.html.*
import net.bmgames.configurator.Id
import net.bmgames.game.GameSession

@Location("/dashboard")
class Dashboard

fun Routing.dashboard(gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {
    get<Dashboard> {
        val games = gamesRef.get()
        call.respondHtml {
            head {
                title { +"Dashboard" }

            }
            body {
                h1 {
                    +"Dashboard"
                }
                games.map { (id, _) ->
                    p {
                        a("/game/$id") {
                            +id
                        }
                    }
                }
                p {
                    a("/game/demo") {
                        +"Create demo game"
                    }
                }

            }
        }
    }
}
