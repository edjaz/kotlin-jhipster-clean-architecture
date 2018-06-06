package fr.edjaz.repository

import org.springframework.social.connect.*
import java.util.stream.Collectors

class CustomSocialUsersConnectionRepository(private val socialUserConnectionRepository: SocialUserConnectionRepository, private val connectionFactoryLocator: ConnectionFactoryLocator) : UsersConnectionRepository {

    override fun findUserIdsWithConnection(connection: Connection<*>): List<String> {
        val key = connection.key
        val socialUserConnections = socialUserConnectionRepository.findAllByProviderIdAndProviderUserId(key.providerId, key.providerUserId)
        return socialUserConnections.stream()
                .map<String>( { it.userId })
                .collect(Collectors.toList())
    }

    override fun findUserIdsConnectedTo(providerId: String, providerUserIds: Set<String>): Set<String> {
        val socialUserConnections = socialUserConnectionRepository.findAllByProviderIdAndProviderUserIdIn(providerId, providerUserIds)
        return socialUserConnections.stream()
                .map<String>({ it.userId })
                .collect(Collectors.toSet())
    }

    override fun createConnectionRepository(userId: String?): ConnectionRepository {
        if (userId == null) {
            throw IllegalArgumentException("userId cannot be null")
        }
        return CustomSocialConnectionRepository(userId, socialUserConnectionRepository, connectionFactoryLocator)
    }
}
