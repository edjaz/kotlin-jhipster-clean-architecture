package fr.edjaz.domain.gateway

import fr.edjaz.domain.model.Authority

/**
 * Spring Data JPA repository for the Authority entity.
 */
interface AuthorityGateway {
    fun findOne(name:String): Authority
    fun findAll():List<Authority>
}
