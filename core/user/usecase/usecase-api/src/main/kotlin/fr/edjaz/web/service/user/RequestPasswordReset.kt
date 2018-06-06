package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface RequestPasswordReset {
    fun execute(mail: String): Optional<UserModelResponse>
}
