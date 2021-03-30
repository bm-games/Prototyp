package net.bmgames.game

import arrow.core.*
import arrow.fx.coroutines.Atomic
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import net.bmgames.configurator.Id
import net.bmgames.user.User
import kotlin.collections.plus


suspend fun Routing.startSocket(path: String, gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {

    webSocket("$path/{game}") {
        val socket = this
        val gameId = call.parameters["game"]

        val user = call.sessions.get<User>()

        if (user == null) {
            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Nicht angemeldet."))
            return@webSocket
        }

        if (gameId == null) {
            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Game-Id wurde nicht angegeben."))
            return@webSocket
        }

        var username = user.username
        var isMaster = false
        //Create game if it doesn't exist
        val gameRef = gamesRef.modify { games ->
            val game = games[gameId]
                ?: loadGameState(gameId)
                    ?.let { state ->
                        isMaster = true
                        username = "master"
                        Atomic.unsafe(
                            GameSession(
                                state.copy(onlinePlayers = listOf(IngamePlayer.Master(username, username))),
                                connections = mapOf(username to socket)
                            )
                        )
                    }
            if (game == null)
                Pair(games, null)
            else
                Pair(games.plus(gameId to game), game)
        }

        if (gameRef == null) {
            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Game mit der Id $gameId wurde nicht gefunden."))
            return@webSocket
        }

        var playerState: Either<JoiningInteraction, String> =
            with(gameRef.get().game) {
                if (getOnlinePlayer(username) != null && isMaster) Right(username)
                else
                    Left(
                        JoiningInteraction(
                            questions = askPlayer(NewPlayer(username), gameRef.get().game.config),
                            currentQuestion = 0,
                            answers = emptyList()
                        )
                    )
            }

        if (playerState is Either.Left) {
            socket.send(playerState.a.getQuestion())
        } else if (playerState is Either.Right) {
           socket.send("Willkommen " + playerState.b)
        }

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    when (playerState) {
                        is Either.Left -> {
                            val interaction = playerState.a
                            when (val answer = interaction.parseAnswer(frame.readText(), gameRef.get().game)) {
                                is Either.Left -> socket.send(answer.a)
                                is Either.Right -> {
                                    when (val nextState = answer.b) {
                                        is Either.Left -> { //Nächste Frage
                                            val nextInteraction = nextState.a
                                            socket.send(nextInteraction.getQuestion())
                                            playerState = Left(nextInteraction)
                                        }
                                        is Either.Right -> { //Fragen abgeschlossen
                                            val avatar = createAvatar(nextState.b)
                                            playerState = Right(avatar.name)
                                            gameRef.updateSession(
                                                { processLogin(avatar, game) },
                                                GameSession.connections.modify { conns -> conns.plus(avatar.name to socket) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is Either.Right -> {
                            val playerName = playerState.b
                            val cmd = parseCommand(frame.readText())
                            gameRef.updateSession({
                                val player = game.getOnlinePlayer(playerName)!!
                                cmd
                                    .mapLeft { Pair(listOf(Message(player, it)), game) }
                                    .fold(::identity) { command -> processCommand(player, command, game) }
                            })
                        }
                    }
                } else {
                    continue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            gameRef.updateSession(
                {
                    game.getOnlinePlayer(username)?.let { processLogout(it, game) }
                        ?: emptyList<GameAction>() to game
                },
                GameSession.connections.modify { it - username }
            )
        }
    }
}

suspend fun Atomic<GameSession>.updateSession(
    tick: GameSession.() -> Pair<List<GameAction>, Game>,
    fSession: (GameSession) -> GameSession = ::identity,
) = modify {
    val (actions, newState) = tick(it)
    val updatedSession = (GameSession.game.set(newState) andThen fSession).invoke(it)
    Pair(
        updatedSession,
        updatedSession.connections.handleActions(actions)
    )
}()


fun Map<String, WebSocketSession>.handleActions(actions: List<GameAction>): suspend () -> Unit = {
    actions.forEach { action ->
        when (action) {
            is Message -> get(action.reciepient.name)?.send(action.text)
        }
    }
}
