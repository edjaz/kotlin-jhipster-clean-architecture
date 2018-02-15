package fr.edjaz.web.service.user.request

import java.time.Instant

data class RegisterUserRequest(
        var id: Long? = null,
        var login: String? = null,
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var imageUrl: String? = null,
        var isActivated: Boolean = false,
        var langKey: String? = null,
        var createdBy: String? = null,
        var createdDate: Instant? = null,
        var lastModifiedBy: String? = null,
        var lastModifiedDate: Instant? = null,
        var authorities: Set<String>? = null,
        var password:String
)
