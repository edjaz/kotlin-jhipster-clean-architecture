package fr.edjaz.web.service.audit

import fr.edjaz.domain.gateway.PersistenceAuditEventGateway
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FindAllAuditEventImpl(
    private val persistenceAuditEventGateway: PersistenceAuditEventGateway
    , private val auditEventConverter: AuditEventConverter
) : FindAllAuditEvent {
    override fun execute(pageable: Pageable): Page<AuditEvent> {
        return persistenceAuditEventGateway.findAll(pageable).map({ auditEventConverter.convertToAuditEvent(it) })
    }
}
