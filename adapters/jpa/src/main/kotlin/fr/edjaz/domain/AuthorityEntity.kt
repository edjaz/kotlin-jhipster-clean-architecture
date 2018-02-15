package fr.edjaz.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Column
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import java.io.Serializable

/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Table(name = "jhi_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class AuthorityEntity(
    @NotNull
    @Size(max = 50)
    @Id
    @Column(length = 50)
    var name: String? = null

) : Serializable {


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val authority = other as AuthorityEntity?

        return !if (name != null) name != authority!!.name else authority!!.name != null
    }

    override fun hashCode(): Int {
        return if (name != null) name!!.hashCode() else 0
    }

    override fun toString(): String {
        return "Authority{" +
                "name='" + name + '\''.toString() +
                "}"
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
