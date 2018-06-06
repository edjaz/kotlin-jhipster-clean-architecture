package fr.edjaz.repository.search

import fr.edjaz.domain.UserEntity
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.domain.model.User
import fr.edjaz.repository.UserRepository
import fr.edjaz.repository.toDomain
import fr.edjaz.repository.toEntity
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

/**
 * Spring Data Elasticsearch repository for the User entity.
 */
@Repository
interface UserSearchRepository : ElasticsearchRepository<UserEntity, Long>


@Component
class UserSearchGatewayImpl(private val userRepository: UserSearchRepository) : UserSearchGateway {
    override fun search(query: String): MutableIterable<User>? {
        return userRepository.search(QueryBuilders.queryStringQuery(query)).map { it.toDomain() }.toMutableList()
    }

    override fun delete(user: User) = userRepository.delete(user.toEntity())

    override fun save(user: User): User = userRepository.save(user.toEntity()).toDomain()
}
