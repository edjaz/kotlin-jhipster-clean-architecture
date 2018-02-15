package fr.edjaz.web.mapper

import fr.edjaz.web.dto.UserDTO
import fr.edjaz.web.rest.vm.ManagedUserVM
import fr.edjaz.web.service.mail.request.MailUser
import fr.edjaz.web.service.user.request.CreateUserRequest
import fr.edjaz.web.service.user.request.FullUpdateUserRequest
import fr.edjaz.web.service.user.request.RegisterUserRequest
import fr.edjaz.web.service.user.request.UpdateUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse

fun ManagedUserVM.toRequest(): RegisterUserRequest = RegisterUserRequest(
    id = id,
    login = login,
    firstName = firstName,
    lastName = lastName,
    email = email,
    imageUrl = imageUrl,
    isActivated = isActivated,
    langKey = langKey,
    createdBy = createdBy,
    createdDate = createdDate,
    lastModifiedBy = lastModifiedBy,
    lastModifiedDate = lastModifiedDate,
    authorities = authorities,
    password = password!!
)


fun UserDTO.toUpdateRequest(): UpdateUserRequest = UpdateUserRequest(
    firstName = firstName!!,
    lastName = lastName!!,
    email = email!!,
    langKey = langKey!!,
    imageUrl = imageUrl!!
)



fun UserModelResponse.toMailUser(): MailUser = MailUser(
    id = id,
    login = login,
    password = password,
    firstName = firstName,
    lastName = lastName,
    email = email,
    activated = activated!!,
    langKey = langKey,
    imageUrl = imageUrl,
    activationKey = activationKey,
    resetKey = resetKey,
    resetDate = resetDate
)

fun UserDTO.toRequest(): CreateUserRequest = CreateUserRequest(
    id = id,
    login = login,
    firstName = firstName,
    lastName = lastName,
    email = email,
    imageUrl = imageUrl,
    isActivated = isActivated,
    langKey = langKey,
    createdBy = createdBy,
    createdDate = createdDate,
    lastModifiedBy = lastModifiedBy,
    lastModifiedDate = lastModifiedDate,
    authorities = authorities
)


fun UserDTO.toFullUpdateRequest(): FullUpdateUserRequest = FullUpdateUserRequest(
    id = id,
    login = login,
    firstName = firstName,
    lastName = lastName,
    email = email,
    imageUrl = imageUrl,
    isActivated = isActivated,
    langKey = langKey,
    createdBy = createdBy,
    createdDate = createdDate,
    lastModifiedBy = lastModifiedBy,
    lastModifiedDate = lastModifiedDate,
    authorities = authorities
)
