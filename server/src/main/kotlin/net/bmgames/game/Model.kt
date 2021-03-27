package net.bmgames.game

import arrow.optics.optics
import kotlinx.serialization.Serializable
import net.bmgames.configurator.*
import net.bmgames.user.Email


/**
 * Wird beim ersten Betreten eines MUDs erstellt
 * */
@Serializable
data class Avatar(
    val name: String,
    val race: RaceConfig,
    val classes: ClassConfig,
)


@Serializable
@optics
sealed class Player {
    abstract val user: Email
    abstract val name: String

    @Serializable
    @optics
    data class Master(override val user: Email, override val name: String) : Player()

    @Serializable
    @optics
    data class NewPlayer(override val user: Email, override val name: String) : Player()

    @Serializable
    @optics
    data class Normal(
        override val user: Email,
        val avatar: Avatar,

        val room: Id,
        val inventory: List<Item>,
        //Weitere stats wie HP, Buffs, Fähigkeiten, usw...
    ) : Player() {
        override val name: String
            get() = avatar.name
    }

    override fun equals(other: Any?) =
        when (other) {
            is Player -> name == other.name
            else -> false
        }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

}


@Serializable
@optics
data class Room(
    val config: RoomConfig,
    val items: Collection<Item>
)

/**
 * Der eigentliche Ingame State
 * */
@Serializable
@optics
data class Game(
    val id: Id,
    val config: GameConfig,

    val startRoom: Id,
    val rooms: Collection<Room>,
    val offlinePlayers: Collection<Player>,
    val onlinePlayers: Collection<Player>,
) {
    fun getOfflinePlayer(name: String): Player? =
        offlinePlayers.find { it.name == name }

    fun getOnlinePlayer(name: String): Player? =
        onlinePlayers.find { it.name == name }

    fun getRoom(id: Id): Room? =
        rooms.find { it.config.id == id }
}
