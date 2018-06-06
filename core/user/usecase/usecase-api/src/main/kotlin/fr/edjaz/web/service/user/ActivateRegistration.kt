package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface ActivateRegistration {
    fun execute(key: String): Optional<UserModelResponse>
}
