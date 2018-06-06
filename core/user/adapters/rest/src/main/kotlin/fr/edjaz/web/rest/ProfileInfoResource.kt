package fr.edjaz.web.rest

import fr.edjaz.web.config.DefaultProfileUtil
import io.github.jhipster.config.JHipsterProperties
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Resource to return information about the currently running Spring profiles.
 */
@RestController
@RequestMapping("/api")
class ProfileInfoResource(private val env: Environment, private val jHipsterProperties: JHipsterProperties) {

    @GetMapping("/profile-info")
    fun getActiveProfiles(): ProfileInfoVM {
            val activeProfiles = DefaultProfileUtil.getActiveProfiles(env)
            return ProfileInfoVM(activeProfiles, getRibbonEnv(activeProfiles))
        }

    private fun getRibbonEnv(activeProfiles: Array<String>): String? {
        val displayOnActiveProfiles = jHipsterProperties.ribbon.displayOnActiveProfiles ?: return null
        val ribbonProfiles = ArrayList(Arrays.asList(*displayOnActiveProfiles))
        val springBootProfiles = Arrays.asList(*activeProfiles)
        ribbonProfiles.retainAll(springBootProfiles)
        return if (!ribbonProfiles.isEmpty()) {
            ribbonProfiles[0]
        } else null
    }

    inner class ProfileInfoVM(val activeProfiles: Array<String>, val ribbonEnv: String?)
}
