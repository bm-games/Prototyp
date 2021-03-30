package net.bmgames.game

import arrow.core.extensions.list.foldable.find
import net.bmgames.game.Move.Companion.Direction

sealed class GameAction
data class Message(val reciepient: IngamePlayer, val text: String) : GameAction()

fun processLogin(avatar: Avatar, game: Game): Pair<List<GameAction>, Game> {
    val (actions, player) =
        with(game.getOfflinePlayer(avatar.name) ?: game.createInitialPlayer(avatar)) {
            moveToRoom(game.getRoom(game.startRoom)!!)
        }

    return Pair(
        listOf(Message(player, "Willkommen ${avatar.name}")).plus(actions),
        game.copy(
            offlinePlayers = game.offlinePlayers - player,
            onlinePlayers = game.onlinePlayers + player
        )
    )
}

fun processLogout(player: IngamePlayer, game: Game): Pair<List<GameAction>, Game> =
    Pair(
        emptyList(),
        game.copy(
            offlinePlayers = game.offlinePlayers + player,
            onlinePlayers = game.onlinePlayers - player
        )
    )


fun processCommand(player: IngamePlayer, command: Command, game: Game): Pair<List<GameAction>, Game> =
    when (command) {
        is Chat -> processChatCommand(player, command, game) to game
        is Look -> processLookCommand(player, command, game) to game
        is Move -> processMoveCommand(player, command, game)
        is Pickup -> processPickupCommand(player, command, game)
    }

fun processChatCommand(sender: IngamePlayer, command: Chat, game: Game): List<GameAction> =
    when (command) {
        is Chat.Say ->
            game.onlinePlayers
                .filter { other ->
                    sender !is IngamePlayer.Normal
                            || (other is IngamePlayer.Normal && sender.room == other.room)
                            || other is IngamePlayer.Master
                }
                .map {
                    Message(it, "${sender.name} sagt: ${command.message}")
                }
        is Chat.Whisper -> {
            val reciepient =
                if (command.reciepient == "master")
                    game.onlinePlayers.find { it is IngamePlayer.Master }
                else
                    game.onlinePlayers.find { it.name == command.reciepient }
            if (reciepient == null) listOf(Message(sender, "${command.reciepient} is nicht da."))
            else listOf(
                Message(sender, "Du hast ${command.reciepient} geflüstert: ${command.message}"),
                Message(reciepient, "${sender.name} flüstert dir: ${command.message}"),
            )
        }
    }

fun processMoveCommand(player: IngamePlayer, command: Move, game: Game): Pair<List<GameAction>, Game> {
    if (player !is IngamePlayer.Normal) {
        return listOf(Message(player, "No movement for you.")) to game
    }
    val nextRoom = game.getRoom(player.room)
        ?.config
        ?.let(command.direction.nextRoom)
        ?.let(game::getRoom)

    return if (nextRoom == null) {
        listOf(Message(player, "Hier ist nur eine Wand. Probier mal \"look\"")) to game
    } else {
        player
            .moveToRoom(nextRoom)
            .map2 { newPlayer ->
                Game.onlinePlayers.modify(game) { it.minus(player).plus(newPlayer) }
            }
    }
}

fun IngamePlayer.moveToRoom(room: Room): Pair<List<GameAction>, IngamePlayer> =
    if (this is IngamePlayer.Normal)
        Pair(
            listOf(
                Message(
                    this,
                    if (room.config.message.startsWith("Welcome in")
                        || room.config.message.startsWith("Willkommen in")
                    ) room.config.message
                    else "Willkommen in Raum " + room.config.message
                )
            ),
            IngamePlayer.Normal.room.set(this, room.config.id)
        )
    else Pair(emptyList(), this)


fun processLookCommand(player: IngamePlayer, command: Look, game: Game): List<GameAction> {
    if (player !is IngamePlayer.Normal) {
        return listOf(Message(player, "No looking for you."))
    }
    val room = game.getRoom(player.room)
    return if (room == null) {
        listOf(Message(player, "Hier ist nichts."))
    } else {
        val playersInRoom =
            game.onlinePlayers.filter {
                it.name != player.name
                        && it is IngamePlayer.Normal
                        && it.room == room.config.id
            }.map { it as IngamePlayer.Normal }

        if (command.target != "room") {
            val targetPlayer = playersInRoom.find { it.name == command.target }.orNull()
            if (targetPlayer != null) {
                return listOf(
                    Message(
                        player,
                        "${targetPlayer.name} ist ${targetPlayer.avatar.race.name} und ${targetPlayer.avatar.klasse.name}"
                    )
                )
            }
            val targetNPCs = room.config.NPCs.find { it.name == command.target }
            if (targetNPCs != null) {
                return listOf(
                    Message(
                        player,
                        "${targetNPCs.name}, ein ${targetNPCs.type} sagt: ${targetNPCs.greeting ?: "Hi"}"
                    )
                )
            }
            return listOf(Message(player, "${command.target} wurde nicht gefunden."))
        } else {
            return Direction.values()
                .mapNotNull { direction ->
                    direction.nextRoom(room.config)
                        ?.let(game::getRoom)
                        ?.let { Message(player, "In Richtung ${direction.name.toLowerCase()} ist eine Tür.") }
                }.plus(
                    if (room.items.isNotEmpty()) listOf(
                        Message(
                            player,
                            "Items in diesem Raum: ${room.items.joinToString(", ") { it.name }}"
                        )
                    )
                    else emptyList()
                ).plus(
                    if (player.inventory.isNotEmpty())
                        listOf(Message(player, "Inventar: ${player.inventory.joinToString(", ") { it.name }}"))
                    else emptyList()
                ).plus(
                    if (playersInRoom.isNotEmpty())
                        listOf(Message(player, "Spieler in diesem Raum: ${playersInRoom.joinToString(", ") { it.name }}"))
                    else emptyList()
                ).plus(
                    if (room.config.NPCs.isNotEmpty())
                        listOf(
                            Message(
                                player,
                                "Du siehst einige NPCs: ${room.config.NPCs.joinToString(" and ") { it.name + " (" + it.type + ")" }}"
                            )
                        )
                    else emptyList()
                )
        }
    }
}


fun processPickupCommand(player: IngamePlayer, command: Pickup, game: Game): Pair<List<GameAction>, Game> {
    if (player !is IngamePlayer.Normal) {
        return listOf(Message(player, "No items for you.")) to game
    }
    val room = game.getRoom(player.room)
    val item = room?.items?.find { it.name == command.obj }

    return if (item == null) {
        listOf(Message(player, "Konnte ${command.obj} nicht finden.")) to game
    } else {
        val newRoom = Room.items.modify(room) { it - item }
        val newPlayer = IngamePlayer.Normal.inventory.modify(player) { it + item }
        emptyList<GameAction>() to game.copy(
            rooms = game.rooms.minus(room).plus(newRoom),
            onlinePlayers = game.onlinePlayers.minus(player).plus(newPlayer)
        )
    }
}
