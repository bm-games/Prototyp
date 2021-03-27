package net.bmgames.configurator


/**
 * Unique Identifier
 * */
typealias Id = String

data class GameConfig(
    val name: Id,

    val rooms: Collection<RoomConfig>,
    val startingRoom: Id,

    val races: Collection<RaceConfig>,
    val classes: Collection<ClassConfig>,
    val startingEquipment: Collection<Item> //Vllt lieber Rassen/Klassen zuodrnen
)

/**
 * @param name ist gleichzeitig unique identifier
 * */
data class RaceConfig(
    val name: Id,
    // Fähigkeiten usw...
)

/**
 * @param name ist gleichzeitig unique identifier
 * */
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
data class RoomConfig(
    val id: Id,

    val north: Id?,
    val east: Id?,
    val south: Id?,
    val west: Id?,

    val message: String,
    val NPCs: Collection<NPCConfig>,
)

/**
 * Kann auch Objekt wie Kiste oder Schild sein
 * @property type Art des NPC
 * @property greeting Nachricht mit dem Spieler, die den Raum des NPCs betreten, begrüßt werden
 * @property items Items die gehalten werden
 * */
data class NPCConfig(
    val id: Id,

    val type: String,
    val name: String,
    val greeting: String?,
    val items: Collection<Item>?
)

data class Item(
    val id: Id,
    val name: String,
    // Fähigkeiten die der Spieler dadurch bekommt
)
