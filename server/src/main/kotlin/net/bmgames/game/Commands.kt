package net.bmgames.game


import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.Left
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import net.bmgames.configurator.Id
import net.bmgames.configurator.RoomConfig
import java.io.StringWriter


sealed class Command

sealed class Chat(parser: ArgParser) : Command() {
    class Say(parser: ArgParser) : Chat(parser) {
        private val msg by parser.positionalList("Nachricht")
        val message by lazy { msg.joinToString(" ") }
    }

    class Whisper(parser: ArgParser) : Chat(parser) {
        val reciepient by parser.positional("Empfänger. \"master\" or \"m\" für den Dungeon Master")
        private val msg by parser.positionalList("Nachricht")
        val message by lazy { msg.joinToString(" ") }
    }
}

class Move(parser: ArgParser) : Command() {
    val direction by parser.mapping(
        map = Direction.values().flatMap {
            listOf(
                "--${it.name.toLowerCase()}" to it,
                "-${it.name.toLowerCase().subSequence(0,1)}" to it
            )
        }.toMap(),
        help = "die gewünschte Richtung"
    )

    companion object {
        enum class Direction(val nextRoom: (RoomConfig) -> Id?) {
            NORTH(nextRoom = RoomConfig::north),
            EAST(nextRoom = RoomConfig::east),
            SOUTH(nextRoom = RoomConfig::south),
            WEST(nextRoom = RoomConfig::west)
        }
    }
}

class Look(parser: ArgParser) : Command() {
    val target by parser.positional("dein Ziel. Standard ist \"room\" für den aktuellen Raum").default("room")
}

class Pickup(parser: ArgParser) : Command() {
    val obj by parser.positional(help = "das Objekt das du aufheben willst")
}


val commands: Map<String, (ArgParser) -> (Command)> = mapOf(
    "say" to Chat::Say,
    "whisper" to Chat::Whisper,
    "go" to ::Move,
    "look" to ::Look,
    "pickup" to ::Pickup,
)

suspend fun parseCommand(input: String): Either<String, Command> {
    val args = input
        .trim()
        .split(Regex("[ \t]+"))

    val commandConstructor = args.getOrNull(0)?.let { name -> commands[name] }

    return if (commandConstructor == null)
        Left("Verfügbare Befehle: ${commands.keys.joinToString(", ")}")
    else
        catch {
            ArgParser(args.subList(1, args.size).toTypedArray()).parseInto(commandConstructor)
        }.mapLeft { error ->
            when (error) {
                is SystemExitException ->
                    with(StringWriter()) {
                        error.printUserMessage(this, args[0], 0)
                        toString()
                    }
                else -> error.localizedMessage
            }
        }
}


suspend fun main(args: Array<String>) {
    while (true) {
        val arg = readLine() ?: continue;
        with(parseCommand(arg)) {
            when (this) {
                is Either.Left -> println(a)
                is Either.Right -> println(b)
            }
        }
    }
}
