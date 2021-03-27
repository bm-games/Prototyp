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
                p { +"MUD-ID eingeben" }
                textInput { id = "inputMUDId" }
            }
            div {
                div{
                    h2 { +"Item - Konfigurator" }
                    p { +"ID:" }
                    textInput { id = "inputItemId" }
                    p { +"Name:"}
                    textInput { id = "inputItemName" }
                    button {
                        onClick = "submitItem()"
                        +"Submit"
                    }
                }
                div{
                    h2 { +"NPC - Konfigurator" }
                    p { +"ID:" }
                    textInput { id = "inputNPCId" }
                    p { +"Typ:" }
                    textInput { id = "inputNPCType" }
                    p { +"Name:" }
                    textInput { id = "inputNPCName" }
                    p { +"Greeting:" }
                    textInput { id = "inputNPCGreeting" }
                    p { +"Items:" }
                    textInput { id = "inputNPCItems" }
                    button {
                        onClick = "submitNPC()"
                        +"Submit"
                    }
                }
                div{
                    h2 { +"Raum - Konfigurator" }
                    p { +"ID:" }
                    textInput { id = "inputRoomId" }
                    div{
                        h3 { +"Verbindungen zu anderen Räumen (ID's angeben):" }
                        p { +"Norden:" }
                        textInput { id = "inputRoomNorthId" }
                        p { +"Osten:" }
                        textInput { id = "inputRoomEastId" }
                        p { +"Süden:" }
                        textInput { id = "inputRoomSouthId" }
                        p { +"Westen:" }
                        textInput { id = "inputRoomWestId" }
                    }
                    p { +"Nachricht:" }
                    textInput { id = "inputRoomMessage" }
                    button {
                        onClick = "submitRoom()"
                        +"Submit"
                    }
                }
                div{
                    h2 { +"Charakterklassen - Konfigurator" }
                    p { +"ID:" }
                    textInput { id = "inputClassId" }
                    button {
                        onClick = "submitClass()"
                        +"Submit"
                    }
                }
                div{
                    h2 { +"Charakterrassen - Konfigurator" }
                    p { +"ID:" }
                    textInput { id = "inputRaceId" }
                    button {
                        onClick = "submitRace()"
                        +"Submit"
                    }
                }
                div{
                    h2 { +"Startequipment auswählen" }
                    textInput { id = "inputStartequipment" }
                    button {
                        onClick = "submitStartequipment()"
                        +"Submit"
                    }
                }
                div{
                    h2 { +"Startraum auswählen" }
                    textInput { id = "inputStartRoomId" }
                }
            }
            button{
                onClick = "backToDashboard()"
                +"Speichern und zurück zur Startseite"
            }
            unsafe{
                //language=HTML
                +"""
                        <script>
                            function submitItem() {
                                let config = {
                                    id: document.getElementById("inputItemId").value,
                                    name: document.getElementById("inputItemName").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createItem", options)
                                    .then(console.log)
                                
                            }
                            function submitNPC() {
                                let config = {
                                    id: document.getElementById("inputNPCId").value,
                                    name: document.getElementById("inputNPCName").value,
                                    type: document.getElementById("inputNPCType").value,
                                    greeting: document.getElementById("inputNPCGreeting").value,
                                    items: document.getElementById("inputNPCItems").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createNPC", options)
                                    .then(console.log)
                                
                            }
                            function submitRoom() {
                                let config = {
                                    id: document.getElementById("inputRoomId").value,
                                    north: document.getElementById("inputRoomNorthId").value,
                                    east: document.getElementById("inputRoomEastId").value,
                                    south: document.getElementById("inputRoomSouthId").value,
                                    west: document.getElementById("inputRoomWestId").value,
                                    message: document.getElementById("inputRoomMessage").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createRoom", options)
                                    .then(console.log)
                                
                            }
                            function submitClass() {
                                let config = {
                                    id: document.getElementById("inputClassId").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createClass", options)
                                    .then(console.log)
                                
                            }
                            function submitRace() {
                                let config = {
                                    id: document.getElementById("inputRaceId").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createRace", options)
                                    .then(console.log)
                                
                            }
                            function submitStartequipment() {
                                let config = {
                                    id: document.getElementById("inputStartequipment").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createStartequipment", options)
                                    .then(console.log)
                                
                            }
                            function backToDashboard() {
                                let config = {
                                    id: document.getElementById("inputMUDId").value,
                                    startRoomId: document.getElementById("inputStartRoomId").value
                                }
                                
                                // request options
                                const options = {
                                    method: 'POST',
                                    body: JSON.stringify(config),
                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                }
                                
                                // send POST request
                                fetch("/createMUD", options)
                                    .then(console.log)
                                    .then(res => window.location.assign("/dashboard"));
                                
                            }
                        </script>
                    """.trimIndent()
            }
        }
    }
}