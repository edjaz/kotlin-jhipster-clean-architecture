package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse

interface UserSearch {
    fun execute(query: String):List<UserModelResponse>
}
