package fr.edjaz.web.service.audit

import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindAllAuditEvent {
    fun execute(pageable: Pageable): Page<AuditEvent>
}
