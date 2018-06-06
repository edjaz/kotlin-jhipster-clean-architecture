package fr.edjaz.web.service.audit

import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant

interface FindAuditEventByDates {
    fun execute(fromDate: Instant, toDate: Instant, pageable: Pageable): Page<AuditEvent>
}
