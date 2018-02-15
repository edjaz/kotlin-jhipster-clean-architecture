package fr.edjaz.web.service.user.request

data class UpdateUserRequest(
        val firstName: String,
        val lastName: String,
        val email: String,
        val langKey: String,
        val imageUrl: String
)
