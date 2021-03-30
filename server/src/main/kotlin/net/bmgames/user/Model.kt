package net.bmgames.user

import kotlinx.serialization.Serializable

typealias UserId = String
typealias Username = String
/**
 * @param email Identifiziert User eindeutig
 * */
@Serializable
data class User(
    val user_id: UserId,
    val username: Username,
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


