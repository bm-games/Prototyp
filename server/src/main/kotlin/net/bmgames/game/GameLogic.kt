package net.bmgames.game

import arrow.core.NonEmptyListOf
import net.bmgames.configurator.GameConfig
import net.bmgames.game.Move.Companion.Direction

sealed class GameAction

data class Message(val reciepient: Player, val text: String) : GameAction()
data class MakeAvatar(val reciepient: Player, val text: List<String>) : GameAction()

fun askPlayer(player: Player.NewPlayer, config: GameConfig): MakeAvatar {
    return MakeAvatar(player, listOf(
        "Gib deinen Namen ein: ",
        "Wähle deine Rasse: " + config.races.joinToString (", "){ race -> race.name},
        "Wähle deine Klasse: "+ config.classes.joinToString (", "){ klasse -> klasse.name}
    ))
}

fun createAvatar(player: Player,text: List<String>, config: GameConfig): Avatar {
    return Avatar(text.get(0),
        config.races.find { race -> text.get(1)==race.name }!!,
        config.classes.find { klassen -> text.get(2)== klassen.name }!!)
}

fun processLogin(playerName: String, game: Game): Pair<List<GameAction>, Game> {
    val (actions, player) =
        with(game.getOfflinePlayer(playerName) ?: game.createInitialPlayer(playerName)) {
            moveToRoom(game.getRoom(game.startRoom)!!)
        }

    return Pair(
        actions.plus(Message(player, "Welcome $playerName")),
        game.copy(
            offlinePlayers = game.offlinePlayers - player,
            onlinePlayers = game.onlinePlayers + player
        )
    )
}

fun processLogout(player: Player, game: Game): Pair<List<GameAction>, Game> =
    Pair(
        emptyList(),
        game.copy(
            offlinePlayers = game.offlinePlayers + player,
            onlinePlayers = game.onlinePlayers - player
        )
    )


fun processCommand(player: Player, command: Command, game: Game): Pair<List<GameAction>, Game> =
    when (command) {
        is Chat -> processChatCommand(player, command, game) to game
        is Look -> processLookCommand(player, command, game) to game
        is Move -> processMoveCommand(player, command, game)
        is Pickup -> processPickupCommand(player, command, game)
    }

fun processChatCommand(sender: Player, command: Chat, game: Game): List<GameAction> =
    when (command) {
        is Chat.Say ->
            game.onlinePlayers
                .filter { other ->
                    sender !is Player.Normal
                            || (other is Player.Normal && sender.room == other.room)
                            || other is Player.Master
                }
                .map {
                    Message(it, "${sender.name} says: ${command.message}")
                }
        is Chat.Whisper -> {
            val reciepient =
                if (listOf("master", "m").contains(command.reciepient))
                    game.onlinePlayers.find { it is Player.Master }
                else
                    game.onlinePlayers.find { it.name == command.reciepient }
            if (reciepient == null) listOf(Message(sender, "Could not find player ${command.reciepient}"))
            else listOf(
                Message(sender, "You whispered to ${command.reciepient}: ${command.message}"),
                Message(reciepient, "${sender.name} whispers to you: ${command.message}"),
            )
        }
    }

fun processMoveCommand(player: Player, command: Move, game: Game): Pair<List<GameAction>, Game> {
    if (player !is Player.Normal) {
        return listOf(Message(player, "No movement for you.")) to game
    }
    val nextRoom = game.getRoom(player.room)
        ?.config
        ?.let(command.direction.nextRoom)
        ?.let(game::getRoom)

    return if (nextRoom == null) {
        listOf(Message(player, "There is no room in this direction. Try \"look\"")) to game
    } else {
        player
            .moveToRoom(nextRoom)
            .map2 { newPlayer ->
                Game.onlinePlayers.modify(game) { it.minus(player).plus(newPlayer) }
            }
    }
}

fun Player.moveToRoom(room: Room): Pair<List<GameAction>, Player> =
    if (this is Player.Normal)
        Pair(
            listOf(Message(this, room.config.message)),
            Player.Normal.room.set(this, room.config.id)
        )
    else Pair(emptyList(), this)


fun processLookCommand(player: Player, command: Look, game: Game): List<GameAction> {
    if (player !is Player.Normal) {
        return listOf(Message(player, "No looking for you."))
    }
    val room = game.getRoom(player.room)
    return if (room == null)
        listOf(Message(player, "Empty room"))
    else Direction.values()
        .mapNotNull { direction ->
            direction.nextRoom(room.config)
                ?.let(game::getRoom)
                ?.let { Message(player, "There's a room ${direction.name.toLowerCase()} of you.") }
        }.plus(
            if (room.items.isNotEmpty()) listOf(Message(player,
                "Items in this room: ${room.items.joinToString(", ") { it.name }}"))
            else emptyList()
        ).plus(
            Message(player, "Inventory: ${player.inventory.joinToString(", ") { it.name }}")
        )
}


fun processPickupCommand(player: Player, command: Pickup, game: Game): Pair<List<GameAction>, Game> {
    if (player !is Player.Normal) {
        return listOf(Message(player, "No items for you.")) to game
    }
    val room = game.getRoom(player.room)
    val item = room?.items?.find { it.name == command.obj }

    return if (item == null) {
        listOf(Message(player, "Could not find a ${command.obj}")) to game
    } else {
        val newRoom = Room.items.modify(room) { it - item }
        val newPlayer = Player.Normal.inventory.modify(player) { it + item }
        emptyList<GameAction>() to game.copy(
            rooms = game.rooms.minus(room).plus(newRoom),
            onlinePlayers = game.onlinePlayers.minus(player).plus(newPlayer)
        )
    }
}
