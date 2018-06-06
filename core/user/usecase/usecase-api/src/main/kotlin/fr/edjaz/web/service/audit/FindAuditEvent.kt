package fr.edjaz.web.service.audit

import org.springframework.boot.actuate.audit.AuditEvent
import java.util.*

interface FindAuditEvent {
    fun execute(id: Long?): Optional<AuditEvent>
}
