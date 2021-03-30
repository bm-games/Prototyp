@file:OptIn(KtorExperimentalLocationsAPI::class)

package net.bmgames.game.ui


import arrow.fx.coroutines.Atomic
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.html.*
import net.bmgames.authenticated
import net.bmgames.configurator.ConfiguratorMain
import net.bmgames.configurator.Id
import net.bmgames.game.GameSession

@Location("/dashboard")
class Dashboard

fun Routing.dashboard(gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {
    get<Dashboard> {
        authenticated {
            val games = gamesRef.get()
            call.respondHtml {
                head {
                    title { +"Dashboard" }

                }
                body {
                    h1 {
                        +"Dashboard"
                    }

                    p {
                        +"Verfügbare Spiele: "
                    }
                    games.map { (id, _) ->
                        p {
                            style = "margin-left: 1rem"
                            a("/game/$id") {
                                +id
                            }
                        }
                    }
                    p {
                        +"Verfügbare Konfigurationen: "
                    }
                    ConfiguratorMain.allMUDs.filterKeys { !games.containsKey(it) }
                        .forEach { (name, _) ->
                            p {
                                style = "margin-left: 1rem"
                                a("/game/$name") {
                                    +"Starte Spiel \"$name\""
                                }
                            }
                        }
                    p {
                        a("/config") {
                            +"Konfiguriere ein eigenes Spiel"
                        }
                    }
                    p {
                        a("/logout") {
                            +"Logout"
                        }
                    }

                }
            }
        }
    }
}
