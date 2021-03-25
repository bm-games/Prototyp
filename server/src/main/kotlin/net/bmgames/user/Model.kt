package net.bmgames.user

import kotlinx.serialization.Serializable


/**
 * @param email Identifiziert User eindeutig
 * */
@Serializable
data class User(
    val email: String,
//    hier weitere Properties wie username o.ä. einfügen
)


