package fr.edjaz.config

import io.github.jhipster.async.ExceptionHandlingAsyncTaskExecutor
import io.github.jhipster.config.JHipsterProperties

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.*
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import java.util.concurrent.Executor

@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfiguration(private val jHipsterProperties: JHipsterProperties) : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(AsyncConfiguration::class.java)

    @Bean(name = arrayOf("taskExecutor"))
    override fun getAsyncExecutor(): Executor {
        log.debug("Creating Async Task Executor")
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = jHipsterProperties.async.corePoolSize
        executor.maxPoolSize = jHipsterProperties.async.maxPoolSize
        executor.setQueueCapacity(jHipsterProperties.async.queueCapacity)
        executor.threadNamePrefix = "app-Executor-"
        return ExceptionHandlingAsyncTaskExecutor(executor)
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return SimpleAsyncUncaughtExceptionHandler()
    }
}
