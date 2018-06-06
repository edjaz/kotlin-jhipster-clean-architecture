package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.request.RegisterUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse

interface RegisterUser {
    fun execute(request: RegisterUserRequest): UserModelResponse
}
