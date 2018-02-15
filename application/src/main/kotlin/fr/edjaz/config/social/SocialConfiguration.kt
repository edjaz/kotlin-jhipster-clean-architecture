package fr.edjaz.config.social

import fr.edjaz.repository.SocialUserConnectionRepository
import fr.edjaz.repository.CustomSocialUsersConnectionRepository
import fr.edjaz.web.security.jwt.TokenProvider
import fr.edjaz.web.security.social.CustomSignInAdapter

import io.github.jhipster.config.JHipsterProperties

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.social.UserIdSource
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer
import org.springframework.social.config.annotation.EnableSocial
import org.springframework.social.config.annotation.SocialConfigurer
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.web.ConnectController
import org.springframework.social.connect.web.ProviderSignInController
import org.springframework.social.connect.web.ProviderSignInUtils
import org.springframework.social.connect.web.SignInAdapter
import org.springframework.social.facebook.connect.FacebookConnectionFactory
import org.springframework.social.google.connect.GoogleConnectionFactory
import org.springframework.social.security.AuthenticationNameUserIdSource
import org.springframework.social.twitter.connect.TwitterConnectionFactory

// jhipster-needle-add-social-connection-factory-import-package

/**
 * Basic Spring Social configuration.
 *
 *
 *
 * Creates the beans necessary to manage Connections to social services and
 * link accounts from those services to internal Users.
 */
@Configuration
@EnableSocial
class SocialConfiguration(private val socialUserConnectionRepository: SocialUserConnectionRepository,
                          private val environment: Environment) : SocialConfigurer {

    private val log = LoggerFactory.getLogger(SocialConfiguration::class.java)

    @Bean
    fun connectController(connectionFactoryLocator: ConnectionFactoryLocator,
                          connectionRepository: ConnectionRepository): ConnectController {

        val controller = ConnectController(connectionFactoryLocator, connectionRepository)
        controller.setApplicationUrl(environment.getProperty("spring.application.url"))
        return controller
    }

    override fun addConnectionFactories(connectionFactoryConfigurer: ConnectionFactoryConfigurer, environment: Environment) {
        // Google configuration
        val googleClientId = environment.getProperty("spring.social.google.client-id")
        val googleClientSecret = environment.getProperty("spring.social.google.client-secret")
        if (googleClientId != null && googleClientSecret != null) {
            log.debug("Configuring GoogleConnectionFactory")
            connectionFactoryConfigurer.addConnectionFactory(
                    GoogleConnectionFactory(
                            googleClientId,
                            googleClientSecret
                    )
            )
        } else {
            log.error("Cannot configure GoogleConnectionFactory id or secret null")
        }

        // Facebook configuration
        val facebookClientId = environment.getProperty("spring.social.facebook.client-id")
        val facebookClientSecret = environment.getProperty("spring.social.facebook.client-secret")
        if (facebookClientId != null && facebookClientSecret != null) {
            log.debug("Configuring FacebookConnectionFactory")
            connectionFactoryConfigurer.addConnectionFactory(
                    FacebookConnectionFactory(
                            facebookClientId,
                            facebookClientSecret
                    )
            )
        } else {
            log.error("Cannot configure FacebookConnectionFactory id or secret null")
        }

        // Twitter configuration
        val twitterClientId = environment.getProperty("spring.social.twitter.client-id")
        val twitterClientSecret = environment.getProperty("spring.social.twitter.client-secret")
        if (twitterClientId != null && twitterClientSecret != null) {
            log.debug("Configuring TwitterConnectionFactory")
            connectionFactoryConfigurer.addConnectionFactory(
                    TwitterConnectionFactory(
                            twitterClientId,
                            twitterClientSecret
                    )
            )
        } else {
            log.error("Cannot configure TwitterConnectionFactory id or secret null")
        }

        // jhipster-needle-add-social-connection-factory
    }

    override fun getUserIdSource(): UserIdSource {
        return AuthenticationNameUserIdSource()
    }

    override fun getUsersConnectionRepository(connectionFactoryLocator: ConnectionFactoryLocator): UsersConnectionRepository {
        return CustomSocialUsersConnectionRepository(socialUserConnectionRepository, connectionFactoryLocator)
    }

    @Bean
    fun signInAdapter(userDetailsService: UserDetailsService, jHipsterProperties: JHipsterProperties,
                      tokenProvider: TokenProvider): SignInAdapter {
        return CustomSignInAdapter(userDetailsService, jHipsterProperties,
            tokenProvider)
    }

    @Bean
    fun providerSignInController(connectionFactoryLocator: ConnectionFactoryLocator, usersConnectionRepository: UsersConnectionRepository, signInAdapter: SignInAdapter): ProviderSignInController {
        val providerSignInController = ProviderSignInController(connectionFactoryLocator, usersConnectionRepository, signInAdapter)
        providerSignInController.setSignUpUrl("/social/signup")
        providerSignInController.setApplicationUrl(environment.getProperty("spring.application.url"))
        return providerSignInController
    }

    @Bean
    fun getProviderSignInUtils(connectionFactoryLocator: ConnectionFactoryLocator, usersConnectionRepository: UsersConnectionRepository): ProviderSignInUtils {
        return ProviderSignInUtils(connectionFactoryLocator, usersConnectionRepository)
    }
}
