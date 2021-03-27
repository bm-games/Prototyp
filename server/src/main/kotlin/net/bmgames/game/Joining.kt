package net.bmgames.game

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import net.bmgames.configurator.ClassConfig
import net.bmgames.configurator.GameConfig
import net.bmgames.configurator.RaceConfig

data class MakeAvatar(
    val reciepient: NewPlayer,
    val questions: List<Pair<String, (String, Game) -> Either<String, Any>>>,
)


data class JoiningInteraction(
    val questions: MakeAvatar,
    private val currentQuestion: Int,
    val answers: List<Any>,
) {
    fun parseAnswer(answer: String, game: Game): Either<String, Either<JoiningInteraction, List<Any>>> {
        val (_, parse) = questions.questions[currentQuestion]
        return parse(answer, game)
            .map { parsedAnswer ->
                if (currentQuestion + 1 == questions.questions.size)
                    Right(answers + parsedAnswer)
                else
                    Left(copy(
                        currentQuestion = currentQuestion + 1,
                        answers = answers + parsedAnswer
                    ))
            }
    }

    fun getQuestion(): String {
        return questions.questions[currentQuestion].first
    }
}


fun askPlayer(player: NewPlayer, config: GameConfig): MakeAvatar {
    return MakeAvatar(
        player,
        listOf(
            "Gib deinen Namen ein: " to { response, game ->
                if (response == "master" || game.getPlayer(response) != null)
                    Left("Dieser Name existiert bereits.")
                else
                    Right(response)
            },
            "Wähle deine Rasse: " + config.races.joinToString(", ") { race -> race.name } to { response, game ->
                val race = game.config.getRace(response)
                if (race == null)
                    Left("Dieser Rasse existiert nicht.")
                else
                    Right(race)
            },

            "Wähle deine Klasse: " + config.classes.joinToString(", ") { klasse -> klasse.name } to { response, game ->
                val clazz = game.config.getClass(response)
                if (clazz == null)
                    Left("Dieser Klasse existiert nicht.")
                else
                    Right(clazz)
            },
        )
    )
}

fun createAvatar(answers: List<Any>): Avatar {
    return Avatar(
        answers[0] as String,
        answers[1] as RaceConfig,
        answers[2] as ClassConfig,
    )
}
