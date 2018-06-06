package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface GetUserWithAuthorities {
    fun execute(): Optional<UserModelResponse>
    fun execute(id: Long?): Optional<UserModelResponse>
}
