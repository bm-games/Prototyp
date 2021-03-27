package net.bmgames.configurator.ui

import io.ktor.html.*
import kotlinx.html.*
import io.ktor.application.*

class Configurator: Template<HTML> {
    override fun HTML.apply() {
        head{
            title { +"Konfigurator" }
        }
        body{
            h1{
                +"Wilkommen beim MUD-Konfigurator"
            }
            div {
                h2 { +"MUD-ID eingeben" }
                textInput { id = "input1" }
            }
            div {
                div{
                    h2 { +"Item - Konfigurator" }
                    p { +"ID:" }
                    textInput {  }
                    p { +"Name:"}
                    textInput {  }
                }
                div{
                    h2 { +"NPC - Konfigurator" }
                    p { +"ID:" }
                    textInput {  }
                    p { +"Typ:" }
                    textInput {  }
                    p { +"Name:" }
                    textInput {  }
                    p { +"Greeting:" }
                    textInput {  }
                    p { +"Items:" }
                    textInput {  }
                }
                div{
                    h2 { +"Raum - Konfigurator" }
                    p { +"ID:" }
                    textInput {  }
                    div{
                        h3 { +"Verbindungen zu anderen Räumen (ID's angeben):" }
                        p { +"Norden:" }
                        textInput {  }
                        p { +"Osten:" }
                        textInput {  }
                        p { +"Süden:" }
                        textInput {  }
                        p { +"Westen:" }
                        textInput {  }
                    }
                    p { +"Nachricht:" }
                    textInput {  }
                    p { +"NPC's:" }
                    textInput {  }
                }
                div{
                    h2 { +"Charakterklassen - Konfigurator" }
                    p { +"ID." }
                    textInput {  }
                }
                div{
                    h2 { +"Charakterrassen - Konfigurator" }
                    p { +"ID." }
                    textInput { id = "input" }
                }
            }
            button {
                +"Konfiguration speichern"
                onClick = "test()"
                script{
                    
                }
            }
        }
    }
}