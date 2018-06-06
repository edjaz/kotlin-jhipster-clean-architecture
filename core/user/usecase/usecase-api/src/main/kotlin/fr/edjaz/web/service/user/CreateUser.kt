package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.request.CreateUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse

interface CreateUser {
    fun execute(request: CreateUserRequest): UserModelResponse
}
