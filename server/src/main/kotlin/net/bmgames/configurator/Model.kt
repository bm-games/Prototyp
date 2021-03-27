package net.bmgames.configurator

import arrow.core.NonEmptyList
import arrow.core.identity
import kotlinx.serialization.Serializable

/**
 * Unique Identifier
 * */
typealias Id = String

@Serializable
data class GameConfig(
    val name: Id,

    val startRoom: Id,
    val rooms: Collection<RoomConfig>,

    val races: Collection<RaceConfig>,
    val classes: Collection<ClassConfig>,
    val startingEquipment: List<Item>, //Vllt lieber Rassen/Klassen zuodrnen
) {
    fun getRace(name: String): RaceConfig? {
        return races.find { race -> name == race.name }
    }

    fun getClass(name: String): ClassConfig? {
        return classes.find { classe -> name == classe.name }
    }
}

/**
 * @param name ist gleichzeitig unique identifier
 * */
@Serializable
data class RaceConfig(
    val name: Id,
    // Fähigkeiten usw...
)

/**
 * @param name ist gleichzeitig unique identifier
 * */
@Serializable
data class ClassConfig(
    val name: Id,
    // Fähigkeiten usw...
)

/**
 * @param north verbundener Raum
 * @param east verbundener Raum
 * @param south verbundener Raum
 * @param west verbundener Raum
 * @param message
 * */
@Serializable
data class RoomConfig(
    val id: Id,

    val north: Id? = null,
    val east: Id? = null,
    val south: Id? = null,
    val west: Id? = null,

    val message: String,
    val NPCs: Collection<NPCConfig> = emptyList(),
    val items: Collection<Item> = emptyList(),
)

/**
 * Kann auch Objekt wie Kiste oder Schild sein
 * @property type Art des NPC
 * @property greeting Nachricht mit dem Spieler, die den Raum des NPCs betreten, begrüßt werden
 * @property items Items die gehalten werden
 * */
@Serializable
data class NPCConfig(
    val id: Id,

    val type: String,
    val name: String,
    val greeting: String?,
    val items: Collection<Item>?,
)

@Serializable
data class Item(
    val id: Id,
    val name: String,
    // Fähigkeiten die der Spieler dadurch bekommt
)
