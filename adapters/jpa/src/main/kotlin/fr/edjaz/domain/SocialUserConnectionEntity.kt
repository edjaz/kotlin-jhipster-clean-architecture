package fr.edjaz.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

import javax.persistence.*
import javax.validation.constraints.NotNull
import java.io.Serializable
import java.util.Objects

/**
 * A Social user.
 */
@Entity
@Table(name = "jhi_social_user_connection")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class SocialUserConnectionEntity : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null

    @NotNull
    @Column(name = "user_id", length = 255, nullable = false)
    var userId: String? = null

    @NotNull
    @Column(name = "provider_id", length = 255, nullable = false)
    var providerId: String? = null

    @NotNull
    @Column(name = "provider_user_id", length = 255, nullable = false)
    var providerUserId: String? = null

    @NotNull
    @Column(nullable = false)
    var rank: Long? = null

    @Column(name = "display_name", length = 255)
    var displayName: String? = null

    @Column(name = "profile_url", length = 255)
    var profileURL: String? = null

    @Column(name = "image_url", length = 255)
    var imageURL: String? = null

    @NotNull
    @Column(name = "access_token", length = 255, nullable = false)
    var accessToken: String? = null

    @Column(length = 255)
    var secret: String? = null

    @Column(name = "refresh_token", length = 255)
    var refreshToken: String? = null

    @Column(name = "expire_time")
    var expireTime: Long? = null

    constructor() {}
    constructor(userId: String?,
                providerId: String?,
                providerUserId: String?,
                rank: Long?,
                displayName: String?,
                profileURL: String?,
                imageURL: String?,
                accessToken: String?,
                secret: String?,
                refreshToken: String?,
                expireTime: Long?) {
        this.userId = userId
        this.providerId = providerId
        this.providerUserId = providerUserId
        this.rank = rank
        this.displayName = displayName
        this.profileURL = profileURL
        this.imageURL = imageURL
        this.accessToken = accessToken
        this.secret = secret
        this.refreshToken = refreshToken
        this.expireTime = expireTime
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val user = other as SocialUserConnectionEntity?

        return if (id != user!!.id) {
            false
        } else true

    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return "SocialUserConnection{" +
                "id=" + id +
                ", userId=" + userId +
                ", providerId='" + providerId + '\''.toString() +
                ", providerUserId='" + providerUserId + '\''.toString() +
                ", rank=" + rank +
                ", displayName='" + displayName + '\''.toString() +
                ", profileURL='" + profileURL + '\''.toString() +
                ", imageURL='" + imageURL + '\''.toString() +
                ", accessToken='" + accessToken + '\''.toString() +
                ", secret='" + secret + '\''.toString() +
                ", refreshToken='" + refreshToken + '\''.toString() +
                ", expireTime=" + expireTime +
                '}'.toString()
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
