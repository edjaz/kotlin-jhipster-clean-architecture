package fr.edjaz.web.service.audit

import fr.edjaz.domain.gateway.PersistenceAuditEventGateway
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class FindAuditEventByDatesImpl(
    private val persistenceAuditEventGateway: PersistenceAuditEventGateway
    , private val auditEventConverter: AuditEventConverter
) :FindAuditEventByDates {
    override fun execute(fromDate: Instant, toDate: Instant, pageable: Pageable): Page<AuditEvent> {
        return persistenceAuditEventGateway.findAllByAuditEventDateBetween(fromDate, toDate, pageable)
            .map( { auditEventConverter.convertToAuditEvent(it) })
    }
}
