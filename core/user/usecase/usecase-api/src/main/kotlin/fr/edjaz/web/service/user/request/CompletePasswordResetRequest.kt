package fr.edjaz.web.service.user.request

data class CompletePasswordResetRequest(
        val newPassword: String,
        val key: String
)
