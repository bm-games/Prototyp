package net.bmgames.user

import kotlinx.serialization.Serializable

typealias UserId = String


/**
 * @param user_id, username Identifiziert User eindeutig
 * */
@Serializable
data class User(
    val user_id: UserId,
    val username: String,
    val accessToken: String,
//    hier weitere Properties wie username o.ä. einfügen
)

