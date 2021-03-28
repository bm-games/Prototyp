package net.bmgames.game

import arrow.fx.coroutines.Atomic
import arrow.optics.optics
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import net.bmgames.configurator.Id
import net.bmgames.game.ui.dashboard
import net.bmgames.game.ui.ingamePage
import net.bmgames.user.UserId


fun Routing.gameServer() = runBlocking {

    val gamesRef = Atomic(emptyMap<Id, Atomic<GameSession>>())

    startSocket("/game", gamesRef)

    dashboard(gamesRef)
    ingamePage(gamesRef)

}


@optics
data class GameSession(
    val game: Game,
    val connections: Map<UserId, WebSocketSession> = emptyMap(),
) {
    companion object
}

