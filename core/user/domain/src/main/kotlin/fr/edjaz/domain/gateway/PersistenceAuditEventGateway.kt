package fr.edjaz.domain.gateway

import fr.edjaz.domain.model.PersistentAuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import java.time.Instant

/**
 * Spring Data JPA repository for the PersistentAuditEvent entity.
 */
interface PersistenceAuditEventGateway {

    fun findByPrincipal(principal: String): List<PersistentAuditEvent>

    fun findByAuditEventDateAfter(after: Instant): List<PersistentAuditEvent>

    fun findByPrincipalAndAuditEventDateAfter(principal: String, after: Instant): List<PersistentAuditEvent>

    fun findByPrincipalAndAuditEventDateAfterAndAuditEventType(principle: String, after: Instant, type: String): List<PersistentAuditEvent>

    fun findAllByAuditEventDateBetween(fromDate: Instant, toDate: Instant, pageable: Pageable): Page<PersistentAuditEvent>

    fun findAll(pageable: Pageable): Page<PersistentAuditEvent>

    fun findOne(id: Long?): PersistentAuditEvent?
    fun save(persistentAuditEvent: PersistentAuditEvent)
    fun findAll(): List<PersistentAuditEvent>
    fun deleteAll()
}
