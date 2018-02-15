package fr.edjaz.web.service.user

import fr.edjaz.web.service.user.request.CompletePasswordResetRequest
import fr.edjaz.web.service.user.response.UserModelResponse
import java.util.*

interface CompletePasswordReset {
    fun execute(request: CompletePasswordResetRequest): Optional<UserModelResponse>

}
