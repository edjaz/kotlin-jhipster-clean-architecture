package fr.edjaz.web.service.audit

import fr.edjaz.domain.gateway.PersistenceAuditEventGateway
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class FindAuditEventImpl(
    private val persistenceAuditEventGateway: PersistenceAuditEventGateway
    , private val auditEventConverter: AuditEventConverter
) : FindAuditEvent {
    override fun execute(id: Long?): Optional<AuditEvent> {
        return Optional.ofNullable(persistenceAuditEventGateway.findOne(id!!)).map({ auditEventConverter.convertToAuditEvent(it) })
    }
}
