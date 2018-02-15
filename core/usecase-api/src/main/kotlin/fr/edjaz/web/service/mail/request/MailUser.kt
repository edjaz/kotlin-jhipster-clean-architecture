package fr.edjaz.web.service.mail.request

import java.time.Instant

data class MailUser(
        var id: Long? = null,
        var login: String? = null,
        var password: String? = null,
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var activated: Boolean = false,
        var langKey: String? = null,
        var imageUrl: String? = null,
        var activationKey: String? = null,
        var resetKey: String? = null,
        var resetDate: Instant? = null
)
