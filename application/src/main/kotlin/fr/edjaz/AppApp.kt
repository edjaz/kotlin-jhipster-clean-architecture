package fr.edjaz

import fr.edjaz.config.ApplicationProperties
import fr.edjaz.web.config.DefaultProfileUtil
import io.github.jhipster.config.JHipsterConstants
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.annotation.PostConstruct

@ComponentScan
@EnableAutoConfiguration(exclude = arrayOf(MetricFilterAutoConfiguration::class, MetricRepositoryAutoConfiguration::class))
@EnableConfigurationProperties(LiquibaseProperties::class, ApplicationProperties::class)
class AppApp(private val env: Environment) {

    /**
     * Initializes app.
     *
     *
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     *
     *
     * You can find more information on how profiles work with JHipster on [http://www.jhipster.tech/profiles/](http://www.jhipster.tech/profiles/).
     */
    @PostConstruct
    fun initApplication() {
        val activeProfiles = Arrays.asList(*env.activeProfiles)
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time.")
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time.")
        }
    }





}

private val log = LoggerFactory.getLogger(AppApp::class.java)
/**
 * Main method, used to run the application.
 *
 * @param args the command line arguments
 * @throws UnknownHostException if the local host name could not be resolved into an address
 */
//@Throws(UnknownHostException::class)
//@JvmStatic
fun main(args: Array<String>) {
    val app = SpringApplication(AppApp::class.java)
    DefaultProfileUtil.addDefaultProfile(app)
    val env = app.run(*args).environment
    var protocol = "http"
    if (env.getProperty("server.ssl.key-store") != null) {
        protocol = "https"
    }
    log.info("\n----------------------------------------------------------\n\t" +
        "Application '{}' is running! Access URLs:\n\t" +
        "Local: \t\t{}://localhost:{}\n\t" +
        "External: \t{}://{}:{}\n\t" +
        "Profile(s): \t{}\n----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        protocol,
        env.getProperty("server.port"),
        protocol,
        InetAddress.getLocalHost().hostAddress,
        env.getProperty("server.port"),
        env.activeProfiles)
}
