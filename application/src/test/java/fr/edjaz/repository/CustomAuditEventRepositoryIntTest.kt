package fr.edjaz.repository

import fr.edjaz.AppApp
import fr.edjaz.domain.config.Constants
import fr.edjaz.domain.gateway.PersistenceAuditEventGateway
import fr.edjaz.domain.model.PersistentAuditEvent
import fr.edjaz.web.service.audit.AuditEventConverter
import fr.edjaz.web.service.audit.CustomAuditEventRepository
import fr.edjaz.web.service.audit.CustomAuditEventRepository.Companion.EVENT_DATA_COLUMN_MAX_LENGTH
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Test class for the CustomAuditEventRepository class.
 *
 * @see CustomAuditEventRepository
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
@Transactional
class CustomAuditEventRepositoryIntTest {

    @Autowired
    private val persistenceAuditEventGateway: PersistenceAuditEventGateway? = null

    @Autowired
    private val auditEventConverter: AuditEventConverter? = null

    private var customAuditEventRepository: CustomAuditEventRepository? = null

    private var testUserEvent: PersistentAuditEvent? = null

    private var testOtherUserEvent: PersistentAuditEvent? = null

    private var testOldUserEvent: PersistentAuditEvent? = null

    @Before
    fun setup() {
        customAuditEventRepository = CustomAuditEventRepository(persistenceAuditEventGateway!!, auditEventConverter!!)
        persistenceAuditEventGateway.deleteAll()
        val oneHourAgo = Instant.now().minusSeconds(3600)

        testUserEvent = PersistentAuditEvent()
        testUserEvent!!.principal = "test-user"
        testUserEvent!!.auditEventType = "test-type"
        testUserEvent!!.auditEventDate = oneHourAgo
        val data = HashMap<String, String>()
        data["test-key"] = "test-value"
        testUserEvent!!.data = data

        testOldUserEvent = PersistentAuditEvent()
        testOldUserEvent!!.principal = "test-user"
        testOldUserEvent!!.auditEventType = "test-type"
        testOldUserEvent!!.auditEventDate = oneHourAgo.minusSeconds(10000)

        testOtherUserEvent = PersistentAuditEvent()
        testOtherUserEvent!!.principal = "other-test-user"
        testOtherUserEvent!!.auditEventType = "test-type"
        testOtherUserEvent!!.auditEventDate = oneHourAgo
    }

    @Test
    fun testFindAfter() {
        persistenceAuditEventGateway!!.save(testUserEvent!!)
        persistenceAuditEventGateway.save(testOldUserEvent!!)

        val events = customAuditEventRepository!!.find(Date.from(testUserEvent!!.auditEventDate!!.minusSeconds(3600)))
        assertThat(events).hasSize(1)
        val event = events[0]
        assertThat(event.principal).isEqualTo(testUserEvent!!.principal)
        assertThat(event.type).isEqualTo(testUserEvent!!.auditEventType)
        assertThat(event.data).containsKey("test-key")
        assertThat(event.data["test-key"].toString()).isEqualTo("test-value")
        assertThat(event.timestamp).isEqualTo(Date.from(testUserEvent!!.auditEventDate!!))
    }

    @Test
    fun testFindByPrincipal() {
        persistenceAuditEventGateway!!.save(testUserEvent!!)
        persistenceAuditEventGateway.save(testOldUserEvent!!)
        persistenceAuditEventGateway.save(testOtherUserEvent!!)

        val events = customAuditEventRepository!!
                .find("test-user", Date.from(testUserEvent!!.auditEventDate!!.minusSeconds(3600)))
        assertThat(events).hasSize(1)
        val event = events[0]
        assertThat(event.principal).isEqualTo(testUserEvent!!.principal)
        assertThat(event.type).isEqualTo(testUserEvent!!.auditEventType)
        assertThat(event.data).containsKey("test-key")
        assertThat(event.data["test-key"].toString()).isEqualTo("test-value")
        assertThat(event.timestamp).isEqualTo(Date.from(testUserEvent!!.auditEventDate!!))
    }

    @Test
    fun testFindByPrincipalNotNullAndAfterIsNull() {
        persistenceAuditEventGateway!!.save(testUserEvent!!)
        persistenceAuditEventGateway.save(testOtherUserEvent!!)

        val events = customAuditEventRepository!!.find("test-user", null)
        assertThat(events).hasSize(1)
        assertThat(events[0].principal).isEqualTo("test-user")
    }

    @Test
    fun testFindByPrincipalIsNullAndAfterIsNull() {
        persistenceAuditEventGateway!!.save(testUserEvent!!)
        persistenceAuditEventGateway.save(testOtherUserEvent!!)

        val events = customAuditEventRepository!!.find(null, null)
        assertThat(events).hasSize(2)
        assertThat(events).extracting("principal")
                .containsExactlyInAnyOrder("test-user", "other-test-user")
    }

    @Test
    fun findByPrincipalAndType() {
        persistenceAuditEventGateway!!.save(testUserEvent!!)
        persistenceAuditEventGateway.save(testOldUserEvent!!)

        testOtherUserEvent!!.auditEventType = testUserEvent!!.auditEventType
        persistenceAuditEventGateway.save(testOtherUserEvent!!)

        val testUserOtherTypeEvent = PersistentAuditEvent()
        testUserOtherTypeEvent.principal = testUserEvent!!.principal
        testUserOtherTypeEvent.auditEventType = "test-other-type"
        testUserOtherTypeEvent.auditEventDate = testUserEvent!!.auditEventDate
        persistenceAuditEventGateway.save(testUserOtherTypeEvent)

        val events = customAuditEventRepository!!.find("test-user",
                Date.from(testUserEvent!!.auditEventDate!!.minusSeconds(3600)), "test-type")
        assertThat(events).hasSize(1)
        val event = events[0]
        assertThat(event.principal).isEqualTo(testUserEvent!!.principal)
        assertThat(event.type).isEqualTo(testUserEvent!!.auditEventType)
        assertThat(event.data).containsKey("test-key")
        assertThat(event.data["test-key"].toString()).isEqualTo("test-value")
        assertThat(event.timestamp).isEqualTo(Date.from(testUserEvent!!.auditEventDate!!))
    }

    @Test
    fun addAuditEvent() {
        val data = HashMap<String, Any>()
        data["test-key"] = "test-value"
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository!!.add(event)
        val persistentAuditEvents = persistenceAuditEventGateway!!.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.principal).isEqualTo(event.principal)
        assertThat(persistentAuditEvent.auditEventType).isEqualTo(event.type)
        assertThat(persistentAuditEvent.data).containsKey("test-key")
        assertThat(persistentAuditEvent.data["test-key"]).isEqualTo("test-value")
        assertThat(persistentAuditEvent.auditEventDate).isEqualTo(event.timestamp.toInstant())
    }

    @Test
    fun addAuditEventTruncateLargeData() {
        val data = HashMap<String, Any>()
        val largeData = StringBuilder()
        for (i in 0 until EVENT_DATA_COLUMN_MAX_LENGTH + 10) {
            largeData.append("a")
        }
        data["test-key"] = largeData
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository!!.add(event)
        val persistentAuditEvents = persistenceAuditEventGateway!!.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.principal).isEqualTo(event.principal)
        assertThat(persistentAuditEvent.auditEventType).isEqualTo(event.type)
        assertThat(persistentAuditEvent.data).containsKey("test-key")
        val actualData = persistentAuditEvent.data["test-key"]
        assertThat(actualData!!.length).isEqualTo(EVENT_DATA_COLUMN_MAX_LENGTH)
        assertThat(actualData).isSubstringOf(largeData)
        assertThat(persistentAuditEvent.auditEventDate).isEqualTo(event.timestamp.toInstant())
    }

    @Test
    fun testAddEventWithWebAuthenticationDetails() {
        val session = MockHttpSession(null, "test-session-id")
        val request = MockHttpServletRequest()
        request.session = session
        request.remoteAddr = "1.2.3.4"
        val details = WebAuthenticationDetails(request)
        val data = HashMap<String, Any>()
        data["test-key"] = details
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository!!.add(event)
        val persistentAuditEvents = persistenceAuditEventGateway!!.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.data["remoteAddress"]).isEqualTo("1.2.3.4")
        assertThat(persistentAuditEvent.data["sessionId"]).isEqualTo("test-session-id")
    }

    @Test
    fun testAddEventWithNullData() {
        val data = HashMap<String, Any?>()
        data["test-key"] = null
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository!!.add(event)
        val persistentAuditEvents = persistenceAuditEventGateway!!.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.data["test-key"]).isEqualTo("null")
    }

    @Test
    fun addAuditEventWithAnonymousUser() {
        val data = HashMap<String, Any>()
        data["test-key"] = "test-value"
        val event = AuditEvent(Constants.ANONYMOUS_USER, "test-type", data)
        customAuditEventRepository!!.add(event)
        val persistentAuditEvents = persistenceAuditEventGateway!!.findAll()
        assertThat(persistentAuditEvents).hasSize(0)
    }

    @Test
    fun addAuditEventWithAuthorizationFailureType() {
        val data = HashMap<String, Any>()
        data["test-key"] = "test-value"
        val event = AuditEvent("test-user", "AUTHORIZATION_FAILURE", data)
        customAuditEventRepository!!.add(event)
        val persistentAuditEvents = persistenceAuditEventGateway!!.findAll()
        assertThat(persistentAuditEvents).hasSize(0)
    }

}
