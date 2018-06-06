package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.AuthorityGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GetAuthoritiesImpl(private val authorityRepository: AuthorityGateway) : GetAuthorities {
    private val log = LoggerFactory.getLogger(this::class.java)
    override fun execute(): List<String> = authorityRepository.findAll().map { it.name!! }
}
