package fr.edjaz.domain

import javax.persistence.*
import javax.validation.constraints.NotNull
import java.io.Serializable
import java.time.Instant
import java.util.HashMap

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Entity
@Table(name = "jhi_persistent_audit_event")
class PersistentAuditEventEntity : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "event_id")
    var id: Long? = null

    @NotNull
    @Column(nullable = false)
    var principal: String? = null

    @Column(name = "event_date")
    var auditEventDate: Instant? = null

    @Column(name = "event_type")
    var auditEventType: String? = null

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "jhi_persistent_audit_evt_data", joinColumns = arrayOf(JoinColumn(name = "event_id")))
    var data: Map<String, String> = HashMap()
}
