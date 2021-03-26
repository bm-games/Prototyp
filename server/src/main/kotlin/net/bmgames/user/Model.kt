package net.bmgames.user

import kotlinx.serialization.Serializable


/**
 * @param user_id, username Identifiziert User eindeutig
 * */
@Serializable
data class User(
    val user_id: String,
    val username: String,
    val accessToken: String,
//    hier weitere Properties wie username o.ä. einfügen
)

// Public Api Response
@Serializable
data class Userinfo(
    val sub: String,
)

// Managment Api Response
data class FullUserInfo(
    val username: String
)


