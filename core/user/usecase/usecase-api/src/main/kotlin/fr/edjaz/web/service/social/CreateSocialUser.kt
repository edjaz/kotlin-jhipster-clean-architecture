package fr.edjaz.web.service.social

import org.springframework.social.connect.Connection

interface CreateSocialUser {
    fun execute(connection: Connection<*>?, langKey: String)
}
