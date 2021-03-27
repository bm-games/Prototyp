package net.bmgames.game

import arrow.core.*
import arrow.fx.coroutines.Atomic
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import net.bmgames.configurator.Id
import net.bmgames.user.User
import net.bmgames.user.UserId
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.plus


suspend fun Routing.startSocket(path: String, gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {
    val counter = AtomicInteger(0)

    webSocket("$path/{game}") { //WebSocketSession
        val socket = this
        val gameId = call.parameters["game"]

        val user = /*call.sessions.get<User>() ?:*/ User("player${counter.getAndIncrement()}", "player${counter.get()}", "")

        if (user == null) {
            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Nicht angemeldet."))
            return@webSocket
        }

        if (gameId == null) {
            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Game-Id wurde nicht angegeben."))
            return@webSocket
        }

        //Create game if it doesn't exist
        val gameRef = gamesRef.modify { games ->
            val game = games[gameId] ?: loadGameState(gameId)?.let { state -> Atomic.unsafe(GameSession(state)) }
            if (game == null)
                Pair(games, null)
            else
                Pair(games.plus(gameId to game), game)
        }

        if (gameRef == null) {
            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Game mit der Id $gameId wurde nicht gefunden."))
            return@webSocket
        }

        val userId = user.user_id
        var playerState: Either<JoiningInteraction, String> =
            Left(
                JoiningInteraction(
                    questions = askPlayer(NewPlayer(userId), gameRef.get().game.config),
                    currentQuestion = 0,
                    answers = emptyList()
                )
            )

        if (playerState is Either.Left) {
            socket.send(playerState.a.getQuestion())
        }

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    when (playerState) {
                        is Either.Left -> {
                            val interaction = playerState.a
                            val answer = interaction.parseAnswer(frame.readText(), gameRef.get().game)
                            when (answer) {
                                is Either.Left -> socket.send(answer.a)
                                is Either.Right -> {
                                    val nextState = answer.b
                                    when (nextState) {
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
                                            socket.send("Willkommen, ${avatar.name}")
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
                { processLogout(game.getOnlinePlayer(userId)!!, game) },
                GameSession.connections.modify { it - userId }
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
