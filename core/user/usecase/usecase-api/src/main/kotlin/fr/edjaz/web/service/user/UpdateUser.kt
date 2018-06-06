package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.request.FullUpdateUserRequest
import fr.edjaz.web.service.user.request.UpdateUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface UpdateUser {
    fun execute(request: UpdateUserRequest)

    fun execute(request: FullUpdateUserRequest): Optional<UserModelResponse>
}
