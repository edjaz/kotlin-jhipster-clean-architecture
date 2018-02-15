package fr.edjaz.web.rest

import fr.edjaz.AppApp
import fr.edjaz.domain.PersistentAuditEventEntity
import fr.edjaz.domain.gateway.PersistenceAuditEventGateway
import fr.edjaz.repository.PersistenceAuditEventRepository
import fr.edjaz.web.service.audit.*
import org.hamcrest.Matchers.hasItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Test class for the AuditResource REST controller.
 *
 * @see AuditResource
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
@Transactional
class AuditResourceIntTest {

    @Autowired
    private val auditEventRepository: PersistenceAuditEventRepository? = null

    @Autowired
    private val auditEventGateway: PersistenceAuditEventGateway? = null

    @Autowired
    private val auditEventConverter: AuditEventConverter? = null

    @Autowired
    private val jacksonMessageConverter: MappingJackson2HttpMessageConverter? = null

    @Autowired
    private val formattingConversionService: FormattingConversionService? = null

    @Autowired
    private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver? = null

    private var auditEvent: PersistentAuditEventEntity? = null

    private var restAuditMockMvc: MockMvc? = null

    private var findAllAuditEvent: FindAllAuditEvent? = null
    private var findAuditEventByDates: FindAuditEventByDates? = null
    private var findAuditEvent: FindAuditEvent? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        findAllAuditEvent = FindAllAuditEventImpl(auditEventGateway!!, auditEventConverter!!)
        findAuditEventByDates = FindAuditEventByDatesImpl(auditEventGateway!!, auditEventConverter!!)
        findAuditEvent = FindAuditEventImpl(auditEventGateway!!, auditEventConverter!!)
        val auditResource = AuditResource(findAllAuditEvent!!, findAuditEventByDates!!, findAuditEvent!!)
        this.restAuditMockMvc = MockMvcBuilders.standaloneSetup(auditResource)
            .setCustomArgumentResolvers(pageableArgumentResolver!!)
            .setConversionService(formattingConversionService)
            .setMessageConverters(jacksonMessageConverter!!).build()
    }

    @Before
    fun initTest() {
        auditEventRepository!!.deleteAll()
        auditEvent = PersistentAuditEventEntity()
        auditEvent!!.auditEventType = SAMPLE_TYPE
        auditEvent!!.principal = SAMPLE_PRINCIPAL
        auditEvent!!.auditEventDate = SAMPLE_TIMESTAMP
    }

    @Test
    @Throws(Exception::class)
    fun getAllAudits() {
        // Initialize the database
        auditEventRepository!!.save<PersistentAuditEventEntity>(auditEvent)

        // Get all the audits
        restAuditMockMvc!!.perform(get("/management/audits"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].principal").value(hasItem(SAMPLE_PRINCIPAL)))
    }

    @Test
    @Throws(Exception::class)
    fun getAudit() {
        // Initialize the database
        auditEventRepository!!.save<PersistentAuditEventEntity>(auditEvent)

        // Get the audit
        restAuditMockMvc!!.perform(get("/management/audits/{id}", auditEvent!!.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.principal").value(SAMPLE_PRINCIPAL))
    }

    @Test
    @Throws(Exception::class)
    fun getAuditsByDate() {
        // Initialize the database
        auditEventRepository!!.save<PersistentAuditEventEntity>(auditEvent)

        // Generate dates for selecting audits by date, making sure the period will contain the audit
        val fromDate = SAMPLE_TIMESTAMP.minusSeconds(SECONDS_PER_DAY).toString().substring(0, 10)
        val toDate = SAMPLE_TIMESTAMP.plusSeconds(SECONDS_PER_DAY).toString().substring(0, 10)

        // Get the audit
        restAuditMockMvc!!.perform(get("/management/audits?fromDate=$fromDate&toDate=$toDate"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].principal").value(hasItem(SAMPLE_PRINCIPAL)))
    }

    @Test
    @Throws(Exception::class)
    fun getNonExistingAuditsByDate() {
        // Initialize the database
        auditEventRepository!!.save<PersistentAuditEventEntity>(auditEvent)

        // Generate dates for selecting audits by date, making sure the period will not contain the sample audit
        val fromDate = SAMPLE_TIMESTAMP.minusSeconds(2 * SECONDS_PER_DAY).toString().substring(0, 10)
        val toDate = SAMPLE_TIMESTAMP.minusSeconds(SECONDS_PER_DAY).toString().substring(0, 10)

        // Query audits but expect no results
        restAuditMockMvc!!.perform(get("/management/audits?fromDate=$fromDate&toDate=$toDate"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header().string("X-Total-Count", "0"))
    }

    @Test
    @Throws(Exception::class)
    fun getNonExistingAudit() {
        // Get the audit
        restAuditMockMvc!!.perform(get("/management/audits/{id}", java.lang.Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    companion object {

        private val SAMPLE_PRINCIPAL = "SAMPLE_PRINCIPAL"
        private val SAMPLE_TYPE = "SAMPLE_TYPE"
        private val SAMPLE_TIMESTAMP = Instant.parse("2015-08-04T10:11:30Z")
        private val SECONDS_PER_DAY = (60 * 60 * 24).toLong()
    }
}
