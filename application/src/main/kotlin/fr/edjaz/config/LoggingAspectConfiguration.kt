package fr.edjaz.config

import fr.edjaz.aop.logging.LoggingAspect

import io.github.jhipster.config.JHipsterConstants

import org.springframework.context.annotation.*
import org.springframework.core.env.Environment

@Configuration
@EnableAspectJAutoProxy
class LoggingAspectConfiguration {

    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    fun loggingAspect(env: Environment): LoggingAspect {
        return LoggingAspect(env)
    }
}
