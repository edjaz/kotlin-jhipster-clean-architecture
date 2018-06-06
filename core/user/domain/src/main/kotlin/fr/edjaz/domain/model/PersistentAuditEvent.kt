package fr.edjaz.domain.model

import java.io.Serializable
import java.time.Instant
import java.util.HashMap

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
class PersistentAuditEvent() : Serializable {
    var id: Long? = null
    var principal: String? = null
    var auditEventDate: Instant? = null
    var auditEventType: String? = null
    var data: Map<String, String> = HashMap()
}
