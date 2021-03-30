@file:OptIn(KtorExperimentalLocationsAPI::class)

package net.bmgames.game.ui

import arrow.fx.coroutines.Atomic
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.html.*
import net.bmgames.authenticated
import net.bmgames.configurator.Id
import net.bmgames.game.GameSession

@Location("/game/{game}")
class InGame(val game: Id)

fun Routing.ingamePage(gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {
    get<InGame> { params ->
        authenticated {
            call.respondHtml {
                head {
                    title { +"Game ${params.game}" }
                    unsafe {
                        //language=HTML
                        +"""
                    <script>
                        
                        let socket = new WebSocket(window.location.href.replace("http", "ws"));
                        socket.onopen = function(e) {
                            document.getElementById("status").innerText = "Verbunden."
                        };
                            
                        socket.onmessage = function(event) {
                            const out = document.getElementsByClassName('output')[0];
                            out.innerHTML += '<span class="string">' + event.data.replace('/n', '<br/>') + '\n</span>';
                            out.scrollTop = out.scrollHeight;                      
                        };

                        socket.onclose = function(event) {
                            document.getElementById("status").innerText = "Verbindung getrennt."
                        };

                        socket.onerror = function(error) {
                            document.getElementById("status").innerText = "Verbindung abgebrochen."
                        };
                        
                        function input(e) {
                            if(e.keyCode == 13) {
                                const value = e.target.value.replaceAll('\n', '');
                                if(value == "") return;
                                const out = document.getElementsByClassName('output')[0];
                                document.getElementById('inputLine').value = "";
                                if(value == "clear") {
                                    out.innerHTML = "";
                                    return;
                                }
                                out.innerHTML += '<span class="command">' + value + '\n</span>';
                                socket.send(value);
                                out.scrollTop = out.scrollHeight;
                            }
                      }
                    </script>
                    <style>
                        #terminal,
                        #terminal pre.output,
                        #terminal pre.output span,
                        #terminal textarea,
                        #terminal textarea:focus {
                            font-size:14px;
                            line-height:1.3;
                            font-weight: normal;
                            font-family:"Consolas", "Andale Mono", "Courier New", "Courier", monospace;
                            border:0 none;
                            outline:0 none;
                            -webkit-box-shadow:none;
                               -moz-box-shadow:none;
                                    box-shadow:none;
                        }
                        #terminal {
                            background:#333;
                            color: #ccc;
                            padding:20px 20px 15px;
                            -webkit-border-radius: 10px;
                               -moz-border-radius: 10px;
                                    border-radius: 10px;
                            max-width:640px;
                            margin:30px auto;
                        }
                        #terminal pre.output {
                            display:block;
                            white-space:pre-wrap;
                            width:100%;
                            height:400px;
                            overflow-y:auto;
                            position:relative;
                            padding:0;
                            margin:0 0 10px;
                            border:0 none;
                        }
                        #terminal pre.output span           { color:#f7f7f7; }
                        #terminal pre.output span.command   { color:#ccc; }
                        #terminal pre.output span.prefix    { color:#777; }
                        #terminal pre.output span.undefined { color:#777; }
                        #terminal pre.output span.string    { color:#99f; }
                        #terminal pre.output span.number    { color:#7f7; }
                        #terminal pre.output span.error     { color:#f77; }
                        
                        #terminal .input {
                            padding:0 0 0 15px;
                            position:relative;
                        }
                        #terminal .input:before {
                            content:">";
                            position:absolute;
                            top: 1px;
                            left: 0;
                            color:#ddd
                        }
                        #terminal textarea {
                            color:#f7f7f7;
                            background:#333;
                            border:0 none;
                            outline:0 none;
                            padding:0;
                            margin:0;
                            resize: none;
                            width:100%;
                            overflow:hidden;
                        }
                        #terminal textarea:focus {
                            outline:0 none;
                        }                        
                        #terminal pre.output::-webkit-scrollbar,
                        #terminal pre.output::-webkit-scrollbar-button,
                        #terminal pre.output::-webkit-scrollbar-track,
                        #terminal pre.output::-webkit-scrollbar-track-piece,
                        #terminal pre.output::-webkit-scrollbar-thumb,
                        #terminal pre.output::-webkit-scrollbar-corner,
                        #terminal pre.output::-webkit-resizer {
                            background: transparent;
                        }
                        #terminal pre.output::-webkit-scrollbar {
                            width:  7px;
                            height: 7px;
                            -webkit-border-radius: 4px;
                                    border-radius: 4px;
                        }
                        #terminal pre.output::-webkit-scrollbar-track-piece {
                            -webkit-border-radius: 5px;
                                    border-radius: 5px;
                        }
                        #terminal pre.output::-webkit-scrollbar-thumb {
                            background: #4f4f4f;
                                    border-radius: 5px;
                        }
                        #terminal pre.output::-webkit-scrollbar-button {
                            width:0;
                            height:0;
                        }
                    </style>
                    """.trimIndent()
                    }
                }
                body {
                    h1 {
                        style = "text-align: center; font-family: cursive"
                        +"Hi ${username}, willkommen in \"${params.game}\""
                    }
                    p{
                        id = "status"
                        +"Verbindung wird hergestellt..."
                    }
                    div {
                        id = "terminal"
                        onClick = "document.getElementById('inputLine').focus()"

                        pre {
                            classes = setOf("output")
                        }
                        div {
                            classes = setOf("input")

                            textArea {
                                rows = "1"
                                id = "inputLine"
                                onKeyDown = "input(event)"
                            }
                        }
                    }
                }
            }
        }
    }
}

