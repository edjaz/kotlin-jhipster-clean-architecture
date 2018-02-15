package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface GetUserWithAuthoritiesByLogin {
    fun execute(login: String): Optional<UserModelResponse>
}
