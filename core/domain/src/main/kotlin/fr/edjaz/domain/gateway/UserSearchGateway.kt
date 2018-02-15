package fr.edjaz.domain.gateway

import fr.edjaz.domain.model.User

interface UserSearchGateway {
    fun save(user: User): User
    fun delete(user: User)
    fun search(query:String): MutableIterable<User>?
}
