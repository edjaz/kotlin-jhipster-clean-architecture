package fr.edjaz.repository

import fr.edjaz.domain.PersistentAuditEventEntity
import fr.edjaz.domain.gateway.PersistenceAuditEventGateway
import fr.edjaz.domain.model.PersistentAuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

import java.time.Instant

/**
 * Spring Data JPA repository for the PersistentAuditEvent entity.
 */
@Repository
interface PersistenceAuditEventRepository : JpaRepository<PersistentAuditEventEntity, Long> {

    fun findByPrincipal(principal: String): List<PersistentAuditEventEntity>

    fun findByAuditEventDateAfter(after: Instant): List<PersistentAuditEventEntity>

    fun findByPrincipalAndAuditEventDateAfter(principal: String, after: Instant): List<PersistentAuditEventEntity>

    fun findByPrincipalAndAuditEventDateAfterAndAuditEventType(principle: String, after: Instant, type: String): List<PersistentAuditEventEntity>

    fun findAllByAuditEventDateBetween(fromDate: Instant, toDate: Instant, pageable: Pageable): Page<PersistentAuditEventEntity>
}

@Component
class PersistenceAuditEventGatewayImpl(private val repo: PersistenceAuditEventRepository) : PersistenceAuditEventGateway {
    override fun deleteAll() =  repo.deleteAll()

    override fun findAll(): List<PersistentAuditEvent> = repo.findAll().toDomainList()

    override fun save(persistentAuditEvent: PersistentAuditEvent) {
        repo.save(persistentAuditEvent.toEntity())
    }

    override fun findByPrincipal(principal: String): List<PersistentAuditEvent> = repo.findByPrincipal(principal).toDomainList()

    override fun findByAuditEventDateAfter(after: Instant): List<PersistentAuditEvent> = repo.findByAuditEventDateAfter(after).toDomainList()

    override fun findByPrincipalAndAuditEventDateAfter(principal: String, after: Instant): List<PersistentAuditEvent>
        = repo.findByPrincipalAndAuditEventDateAfter(principal, after).toDomainList()

    override fun findByPrincipalAndAuditEventDateAfterAndAuditEventType(principle: String, after: Instant, type: String): List<PersistentAuditEvent>
        = repo.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principle, after, type).toDomainList()

    override fun findAllByAuditEventDateBetween(fromDate: Instant, toDate: Instant, pageable: Pageable): Page<PersistentAuditEvent>
        = repo.findAllByAuditEventDateBetween(fromDate, toDate, pageable).map { it.toDomain() }

    override fun findAll(pageable: Pageable): Page<PersistentAuditEvent> = repo.findAll(pageable).map { it.toDomain() }

    override fun findOne(id: Long?): PersistentAuditEvent? {
        val audit = repo.findOne(id)
        return if(audit == null) null
        else audit.toDomain()
    }

}

fun List<PersistentAuditEventEntity>.toDomainList(): List<PersistentAuditEvent> = this.map { it.toDomain() }
fun PersistentAuditEventEntity.toDomain(): PersistentAuditEvent {
    val persistentAuditEvent = PersistentAuditEvent()
    persistentAuditEvent.id = id
    persistentAuditEvent.principal = principal
    persistentAuditEvent.auditEventDate = auditEventDate
    persistentAuditEvent.auditEventType = auditEventType
    persistentAuditEvent.data = data
    return persistentAuditEvent
}
fun PersistentAuditEvent.toEntity(): PersistentAuditEventEntity{
    val entity = PersistentAuditEventEntity()
    entity.id = id
    entity.principal = principal
    entity.auditEventDate = auditEventDate
    entity.auditEventType = auditEventType
    entity.data = data
    return entity
}
