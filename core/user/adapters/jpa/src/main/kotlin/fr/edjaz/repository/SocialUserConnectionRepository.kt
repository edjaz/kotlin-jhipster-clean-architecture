package fr.edjaz.repository

import fr.edjaz.domain.SocialUserConnectionEntity

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for the Social User Connection entity.
 */
interface SocialUserConnectionRepository : JpaRepository<SocialUserConnectionEntity, Long> {

    fun findAllByProviderIdAndProviderUserId(providerId: String, providerUserId: String): List<SocialUserConnectionEntity>

    fun findAllByProviderIdAndProviderUserIdIn(providerId: String, providerUserIds: Set<String>): List<SocialUserConnectionEntity>

    fun findAllByUserIdOrderByProviderIdAscRankAsc(userId: String): List<SocialUserConnectionEntity>

    fun findAllByUserIdAndProviderIdOrderByRankAsc(userId: String, providerId: String): List<SocialUserConnectionEntity>

    fun findAllByUserIdAndProviderIdAndProviderUserIdIn(userId: String, providerId: String, provideUserId: List<String>): List<SocialUserConnectionEntity>

    fun findOneByUserIdAndProviderIdAndProviderUserId(userId: String, providerId: String, providerUserId: String): SocialUserConnectionEntity

    fun deleteByUserIdAndProviderId(userId: String, providerId: String)

    fun deleteByUserIdAndProviderIdAndProviderUserId(userId: String, providerId: String, providerUserId: String)
}
