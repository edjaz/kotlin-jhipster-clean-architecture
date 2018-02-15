package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface UserWithAuthorities {
    fun execute(): Optional<UserModelResponse>
}
