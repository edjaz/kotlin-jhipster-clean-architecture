package fr.edjaz.repository

import fr.edjaz.domain.AuthorityEntity
import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.model.Authority

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Repository
interface AuthorityRepository : JpaRepository<AuthorityEntity, String>

@Component
class AuthorityGatewayImpl(private val authorityRepository: AuthorityRepository) : AuthorityGateway {
    override fun findOne(name: String): Authority = authorityRepository.findOne(name).toDomain()
    override fun findAll(): List<Authority> = authorityRepository.findAll().toDomainList()
}

fun AuthorityEntity.toDomain(): Authority =  Authority(name = name)
fun List<AuthorityEntity>.toDomainList() : List<Authority> = map { it.toDomain() }

