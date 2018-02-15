package fr.edjaz.config

import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.liquibase.AsyncSpringLiquibase

import liquibase.integration.spring.SpringLiquibase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.core.task.TaskExecutor
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

import javax.sql.DataSource
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.sql.SQLException

@Configuration
@EnableJpaRepositories("fr.edjaz.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
@EnableElasticsearchRepositories("fr.edjaz.repository.search")
class DatabaseConfiguration(private val env: Environment) {

    private val log = LoggerFactory.getLogger(DatabaseConfiguration::class.java)

    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server
     * @throws SQLException if the server failed to start
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    @Throws(SQLException::class)
    fun h2TCPServer(): Any {
        try {
            // We don't want to include H2 when we are packaging for the "prod" profile and won't
            // actually need it, so we have to load / invoke things at runtime through reflection.
            val loader = Thread.currentThread().contextClassLoader
            val serverClass = Class.forName("org.h2.tools.Server", true, loader)
            val createServer = serverClass.getMethod("createTcpServer", Array<String>::class.java)
            return createServer.invoke(null, *arrayOf<Any>(arrayOf("-tcp", "-tcpAllowOthers")))

        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Failed to load and initialize org.h2.tools.Server", e)

        } catch (e: LinkageError) {
            throw RuntimeException("Failed to load and initialize org.h2.tools.Server", e)
        } catch (e: SecurityException) {
            throw RuntimeException("Failed to get method org.h2.tools.Server.createTcpServer()", e)

        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Failed to get method org.h2.tools.Server.createTcpServer()", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Failed to invoke org.h2.tools.Server.createTcpServer()", e)

        } catch (e: IllegalArgumentException) {
            throw RuntimeException("Failed to invoke org.h2.tools.Server.createTcpServer()", e)
        } catch (e: InvocationTargetException) {
            val t = e.targetException
            if (t is SQLException) {
                throw t
            }
            throw RuntimeException("Unchecked exception in org.h2.tools.Server.createTcpServer()", t)
        }

    }

    @Bean
    fun liquibase(@Qualifier("taskExecutor") taskExecutor: TaskExecutor,
                  dataSource: DataSource, liquibaseProperties: LiquibaseProperties): SpringLiquibase {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start asynchronously
        val liquibase = AsyncSpringLiquibase(taskExecutor, env)
        liquibase.dataSource = dataSource
        liquibase.changeLog = "classpath:config/liquibase/master.xml"
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
            liquibase.setShouldRun(false)
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled)
            log.debug("Configuring Liquibase")
        }
        return liquibase
    }
}
