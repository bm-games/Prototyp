@file:OptIn(KtorExperimentalLocationsAPI::class)

package net.bmgames.game.ui

import arrow.fx.coroutines.Atomic
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.html.*
import net.bmgames.configurator.Id
import net.bmgames.game.GameSession

@Location("/game/{game}")
class InGame(val game: Id)

fun Routing.ingamePage(gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {
    get<InGame> { params ->
        call.respondHtml {
            head {
                title { +"Game ${params.game}" }
                unsafe {
                    //language=HTML
                    +"""
                    <link rel="stylesheet" href="/static/xterm.css" />
                    <script src="/static/xterm.js"></script>
                    <script>
                        
                        let socket = new WebSocket("ws://localhost:8080/game/${params.game}");

                        socket.onopen = function(e) {
                          alert("[open] Connection established");
                          alert("Sending to server");
                          socket.send("My name is John");
                        };

                        socket.onmessage = function(event) {
                          alert(`[message] Data received from server: $\{event.data}`);
                        };

                        socket.onclose = function(event) {
                          if (event.wasClean) {
                            alert(`[close] Connection closed cleanly, code=$\{event.code} reason=$\{event.reason}`);
                          } else {
                            // e.g. server process killed or network down
                            // event.code is usually 1006 in this case
                            alert('[close] Connection died');
                          }
                        };

                        socket.onerror = function(error) {
                          alert(`[error] $\{error.message}`);
                        };
                    </script>
                    """.trimIndent()
                }
            }
            body {
                h1 {
                    +"Hi ${call.authentication.principal<OAuthAccessTokenResponse>()}"
                }
                p {
                    +"Welcome in Game ${params.game}"
                }

                textInput {
                    id = "name"
                }

                button {
                    onClick = "submit()"
                    +"Submit"
                }


            }
        }
    }
}
