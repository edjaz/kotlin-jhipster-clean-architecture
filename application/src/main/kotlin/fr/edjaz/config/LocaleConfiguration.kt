package fr.edjaz.config

import io.github.jhipster.config.locale.AngularCookieLocaleResolver

import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor

@Configuration
class LocaleConfiguration : WebMvcConfigurerAdapter(), EnvironmentAware {

    override fun setEnvironment(environment: Environment) {
        // unused
    }

    @Bean(name = arrayOf("localeResolver"))
    fun localeResolver(): LocaleResolver {
        val cookieLocaleResolver = AngularCookieLocaleResolver()
        cookieLocaleResolver.cookieName = "NG_TRANSLATE_LANG_KEY"
        return cookieLocaleResolver
    }

    override fun addInterceptors(registry: InterceptorRegistry?) {
        val localeChangeInterceptor = LocaleChangeInterceptor()
        localeChangeInterceptor.paramName = "language"
        registry!!.addInterceptor(localeChangeInterceptor)
    }
}
