package fr.edjaz.web.rest

import io.github.jhipster.config.JHipsterProperties
import fr.edjaz.AppApp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Test class for the ProfileInfoResource REST controller.
 *
 * @see ProfileInfoResource
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
class ProfileInfoResourceIntTest {

    @Mock
    private val environment: Environment? = null

    @Mock
    private val jHipsterProperties: JHipsterProperties? = null

    private var restProfileMockMvc: MockMvc? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val mockProfile = arrayOf("test")
        val ribbon = JHipsterProperties.Ribbon()
        ribbon.displayOnActiveProfiles = mockProfile
        `when`(jHipsterProperties!!.ribbon).thenReturn(ribbon)

        val activeProfiles = arrayOf("test")
        `when`(environment!!.defaultProfiles).thenReturn(activeProfiles)
        `when`(environment.activeProfiles).thenReturn(activeProfiles)

        val profileInfoResource = ProfileInfoResource(environment, jHipsterProperties)
        this.restProfileMockMvc = MockMvcBuilders
                .standaloneSetup(profileInfoResource)
                .build()
    }

    @Test
    @Throws(Exception::class)
    fun getProfileInfoWithRibbon() {
        restProfileMockMvc!!.perform(get("/api/profile-info"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    @Throws(Exception::class)
    fun getProfileInfoWithoutRibbon() {
        val ribbon = JHipsterProperties.Ribbon()
        ribbon.displayOnActiveProfiles = null
        `when`(jHipsterProperties!!.ribbon).thenReturn(ribbon)

        restProfileMockMvc!!.perform(get("/api/profile-info"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    @Throws(Exception::class)
    fun getProfileInfoWithoutActiveProfiles() {
        val emptyProfile = arrayOf<String>()
        `when`(environment!!.defaultProfiles).thenReturn(emptyProfile)
        `when`(environment.activeProfiles).thenReturn(emptyProfile)

        restProfileMockMvc!!.perform(get("/api/profile-info"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }
}
