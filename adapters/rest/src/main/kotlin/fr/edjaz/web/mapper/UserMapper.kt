package fr.edjaz.web.mapper

import org.springframework.stereotype.Service

/**
 * Mapper for the entity User and its DTO called UserDTO.
 *
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
class UserMapper {

/*    fun userToUserDTO(user: User): UserDTO {
        return UserDTO(user)
    }*/

/*    fun usersToUserDTOs(users: List<User>): List<UserDTO> {
        return users.stream()
            .filter({ Objects.nonNull(it) })
            .map<UserDTO>({ this.userToUserDTO(it) })
            .collect(Collectors.toList())
    }*/

/*    fun userDTOToUser(userDTO: UserDTO?): UserEntity? {
        if (userDTO == null) {
            return null
        } else {
            val user = UserEntity()
            user.id = userDTO.id
            user.login = userDTO.login
            user.firstName = userDTO.firstName
            user.lastName = userDTO.lastName
            user.email = userDTO.email
            user.imageUrl = userDTO.imageUrl
            user.activated = userDTO.isActivated
            user.langKey = userDTO.langKey
            val authorities = this.authoritiesFromStrings(userDTO.authorities)
            if (authorities != null) {
                user.authorities = authorities
            }
            return user
        }
    }*/

/*
    fun userDTOsToUsers(userDTOs: List<UserDTO>): List<UserEntity> {
        return userDTOs.stream()
            .filter({ Objects.nonNull(it) })
            .map<UserEntity>({ this.userDTOToUser(it) })
            .collect(Collectors.toList())
    }
*/

/*
    fun userFromId(id: Long?): UserEntity? {
        if (id == null) {
            return null
        }
        val user = UserEntity()
        user.id = id
        return user
    }
*/

/*    fun authoritiesFromStrings(strings: Set<String>?): Set<AuthorityEntity>? {
        return strings!!.stream().map {
            val auth = AuthorityEntity()
            auth.name = it
            auth
        }.collect(Collectors.toSet())
    }*/
}
