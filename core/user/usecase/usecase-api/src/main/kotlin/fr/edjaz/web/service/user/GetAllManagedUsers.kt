package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.response.UserModelResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetAllManagedUsers {
    fun execute(pageable: Pageable): Page<UserModelResponse>
}
