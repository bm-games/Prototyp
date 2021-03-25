package net.bmgames.game

import net.bmgames.configurator.ClassConfig
import net.bmgames.configurator.GameConfig
import net.bmgames.configurator.RaceConfig


/**
 * Wird beim ersten Betreten eines MUDs erstellt
 * */
data class Avatar(
    val name: String,
    val race: RaceConfig,
    val klasse: ClassConfig
)
