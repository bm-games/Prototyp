package net.bmgames.user

import kotlinx.serialization.Serializable

typealias Email = String

/**
 * @param email Identifiziert User eindeutig
 * */
@Serializable
data class User(
    val email: Email,
//    hier weitere Properties wie username o.ä. einfügen
)


