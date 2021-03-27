package net.bmgames.game

import arrow.core.andThen
import arrow.core.identity
import arrow.fx.coroutines.Atomic
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import net.bmgames.configurator.Id
import java.util.concurrent.atomic.AtomicInteger


suspend fun Routing.startSocket(path: String, gamesRef: Atomic<Map<Id, Atomic<GameSession>>>) {
    val counter = AtomicInteger(0)

    webSocket("$path/{game}") { //WebSocketSession
        val socket = this
        val gameId = call.parameters["game"]

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

        val playerName = "player${counter.getAndIncrement()}"
        gameRef.updateSession(
            { processLogin(playerName, game) },
            GameSession.connections.modify { conns -> conns.plus(playerName to socket) }
        )

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val cmd = parseCommand(frame.readText())
                    gameRef.updateSession({
                        val player = game.getOnlinePlayer(playerName)!!
                        cmd
                            .mapLeft { Pair(listOf(Message(player, it)), game) }
                            .fold(::identity) { command -> processCommand(player, command, game) }
                    })
                } else {
                    continue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            gameRef.updateSession(
                { processLogout(game.getOnlinePlayer(playerName)!!, game) },
                GameSession.connections.modify { it - playerName }
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
