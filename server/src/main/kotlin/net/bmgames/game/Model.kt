package net.bmgames.game

import arrow.optics.optics
import kotlinx.serialization.Serializable
import net.bmgames.configurator.*
import net.bmgames.user.Username


/**
 * Wird beim ersten Betreten eines MUDs erstellt
 * */
@Serializable
data class Avatar(
    val name: String,
    val race: RaceConfig,
    val klasse: ClassConfig,
)

sealed class Player {
    abstract val user: Username
}

@Serializable
@optics
data class NewPlayer(override val user: Username) : Player()

@Serializable
@optics
sealed class IngamePlayer : Player() {
    abstract val name: String

    @Serializable
    @optics
    data class Master(override val user: Username, override val name: String) : IngamePlayer()

    @Serializable
    @optics
    data class Normal(
        override val user: Username,
        val avatar: Avatar,

        val room: Id,
        val inventory: List<Item>,
        //Weitere stats wie HP, Buffs, Fähigkeiten, usw...
    ) : IngamePlayer() {
        override val name: String
            get() = avatar.name
    }

    override fun equals(other: Any?) =
        when (other) {
            is IngamePlayer -> name == other.name
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
    val items: Collection<Item>,
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
    val offlinePlayers: Collection<IngamePlayer>,
    val onlinePlayers: Collection<IngamePlayer>,
) {
    fun getOfflinePlayer(name: String): IngamePlayer? =
        offlinePlayers.find { it.name == name }

    fun getOnlinePlayer(name: String): IngamePlayer? =
        onlinePlayers.find { it.name == name }

    fun getRoom(id: Id): Room? =
        rooms.find { it.config.id == id }

    fun getPlayer(name: String): IngamePlayer? {
        return getOfflinePlayer(name) ?: getOnlinePlayer(name)
    }
}
