package fr.edjaz.repository

import fr.edjaz.AppApp
import fr.edjaz.domain.SocialUserConnectionEntity

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.social.connect.*
import org.springframework.social.connect.support.ConnectionFactoryRegistry
import org.springframework.social.connect.support.OAuth1ConnectionFactory
import org.springframework.social.connect.support.OAuth2ConnectionFactory
import org.springframework.social.oauth1.OAuth1Operations
import org.springframework.social.oauth1.OAuth1ServiceProvider
import org.springframework.social.oauth2.*
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

import java.util.Arrays
import java.util.HashSet

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.*
import org.elasticsearch.index.mapper.MapperBuilders.`object`
import org.elasticsearch.index.mapper.MapperBuilders.`object`
import org.junit.Ignore


@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
@Transactional
class CustomSocialUsersConnectionRepositoryIntTest {

    private var connectionFactoryRegistry: ConnectionFactoryRegistry? = null

    private var connectionFactory: TestFacebookConnectionFactory? = null

    private var usersConnectionRepository: CustomSocialUsersConnectionRepository? = null

    private var connectionRepository: ConnectionRepository? = null

    @Autowired
    private val socialUserConnectionRepository: SocialUserConnectionRepository? = null

    @Before
    fun setUp() {
        socialUserConnectionRepository!!.deleteAll()

        connectionFactoryRegistry = ConnectionFactoryRegistry()
        connectionFactory = TestFacebookConnectionFactory()
        connectionFactoryRegistry!!.addConnectionFactory(connectionFactory!!)
        usersConnectionRepository = CustomSocialUsersConnectionRepository(socialUserConnectionRepository, connectionFactoryRegistry!!)
        connectionRepository = usersConnectionRepository!!.createConnectionRepository("1")
    }

    @Test
    fun findUserIdWithConnection() {
        insertFacebookConnection()
        val userIds = usersConnectionRepository!!.findUserIdsWithConnection(connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java))
        assertEquals("1", userIds[0])
    }

    @Test
    fun findUserIdWithConnectionNoSuchConnection() {
        val connection = connectionFactory!!.createConnection(AccessGrant("12345"))
        assertEquals(0, usersConnectionRepository!!.findUserIdsWithConnection(connection).size.toLong())
    }

    @Test
    fun findUserIdWithConnectionMultipleConnectionsToSameProviderUser() {
        insertFacebookConnection()
        insertFacebookConnectionSameFacebookUser()
        val localUserIds = usersConnectionRepository!!.findUserIdsWithConnection(connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java))
        assertThat(localUserIds).containsExactly("1", "2")
    }

    @Test
    fun findUserIdsConnectedTo() {
        insertFacebookConnection()
        insertFacebookConnection3()
        val localUserIds = usersConnectionRepository!!.findUserIdsConnectedTo("facebook", HashSet(Arrays.asList("9", "11")))
        assertEquals(2, localUserIds.size.toLong())
        assertTrue(localUserIds.contains("1"))
        assertTrue(localUserIds.contains("2"))
    }

    @Test
    fun findAllConnections() {
        connectionFactoryRegistry!!.addConnectionFactory(TestTwitterConnectionFactory())
        insertTwitterConnection()
        insertFacebookConnection()
        val connections = connectionRepository!!.findAllConnections()
        assertEquals(2, connections.size.toLong())
        val facebook = connections.getFirst("facebook") as Connection<TestFacebookApi>
        assertFacebookConnection(facebook)
        val twitter = connections.getFirst("twitter") as Connection<TestTwitterApi>
        assertTwitterConnection(twitter)
    }

    @Test
    fun findAllConnectionsMultipleConnectionResults() {
        connectionFactoryRegistry!!.addConnectionFactory(TestTwitterConnectionFactory())
        insertTwitterConnection()
        insertFacebookConnection()
        insertFacebookConnection2()
        val connections = connectionRepository!!.findAllConnections()
        assertEquals(2, connections.size.toLong())
        assertEquals(2, connections["facebook"]!!.size.toLong())
        assertEquals(1, connections["twitter"]!!.size.toLong())
    }

    @Test
    fun findAllConnectionsEmptyResult() {
        connectionFactoryRegistry!!.addConnectionFactory(TestTwitterConnectionFactory())
        val connections = connectionRepository!!.findAllConnections()
        assertEquals(2, connections.size.toLong())
        assertEquals(0, connections["facebook"]!!.size.toLong())
        assertEquals(0, connections["twitter"]!!.size.toLong())
    }

    @Test(expected = IllegalArgumentException::class)
    fun noSuchConnectionFactory() {
        insertTwitterConnection()
        connectionRepository!!.findAllConnections()
    }

    @Test
    fun findConnectionsByProviderId() {
        connectionFactoryRegistry!!.addConnectionFactory(TestTwitterConnectionFactory())
        insertTwitterConnection()
        val connections = connectionRepository!!.findConnections("twitter")
        assertEquals(1, connections.size.toLong())
        assertTwitterConnection(connections[0] as Connection<TestTwitterApi>)
    }

    @Test
    fun findConnectionsByProviderIdEmptyResult() {
        assertTrue(connectionRepository!!.findConnections("facebook").isEmpty())
    }

    @Test
    fun findConnectionsByApi() {
        insertFacebookConnection()
        insertFacebookConnection2()
        val connections = connectionRepository!!.findConnections(TestFacebookApi::class.java)
        assertEquals(2, connections.size.toLong())
        assertFacebookConnection(connections[0])
    }

    @Test
    fun findConnectionsByApiEmptyResult() {
        assertTrue(connectionRepository!!.findConnections(TestFacebookApi::class.java).isEmpty())
    }

    @Test
    fun findConnectionsToUsers() {
        connectionFactoryRegistry!!.addConnectionFactory(TestTwitterConnectionFactory())
        insertTwitterConnection()
        insertFacebookConnection()
        insertFacebookConnection2()
        val providerUsers = LinkedMultiValueMap<String, String>()
        providerUsers.add("facebook", "10")
        providerUsers.add("facebook", "9")
        providerUsers.add("twitter", "1")
        val connectionsForUsers = connectionRepository!!.findConnectionsToUsers(providerUsers)
        assertEquals(2, connectionsForUsers.size.toLong())
        val providerId = connectionsForUsers.getFirst("facebook").key.providerUserId
        assertTrue("10" == providerId || "9" == providerId)
        assertFacebookConnection(connectionRepository!!.getConnection(ConnectionKey("facebook", "9")) as Connection<TestFacebookApi>)
        assertTwitterConnection(connectionsForUsers.getFirst("twitter") as Connection<TestTwitterApi>)
    }

    @Test
    fun findConnectionsToUsersEmptyResult() {
        val providerUsers = LinkedMultiValueMap<String, String>()
        providerUsers.add("facebook", "1")
        assertTrue(connectionRepository!!.findConnectionsToUsers(providerUsers).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun findConnectionsToUsersEmptyInput() {
        val providerUsers = LinkedMultiValueMap<String, String>()
        connectionRepository!!.findConnectionsToUsers(providerUsers)
    }

    @Test
    fun findConnectionByKey() {
        insertFacebookConnection()
        assertFacebookConnection(connectionRepository!!.getConnection(ConnectionKey("facebook", "9")) as Connection<TestFacebookApi>)
    }

    @Test(expected = NoSuchConnectionException::class)
    fun findConnectionByKeyNoSuchConnection() {
        connectionRepository!!.getConnection(ConnectionKey("facebook", "bogus"))
    }

    @Test
    fun findConnectionByApiToUser() {
        insertFacebookConnection()
        insertFacebookConnection2()
        assertFacebookConnection(connectionRepository!!.getConnection(TestFacebookApi::class.java, "9"))
        assertEquals("10", connectionRepository!!.getConnection(TestFacebookApi::class.java, "10").key.providerUserId)
    }

    @Test(expected = NoSuchConnectionException::class)
    fun findConnectionByApiToUserNoSuchConnection() {
        assertFacebookConnection(connectionRepository!!.getConnection(TestFacebookApi::class.java, "9"))
    }

    @Test
    fun findPrimaryConnection() {
        insertFacebookConnection()
        assertFacebookConnection(connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java))
    }

    @Test
    fun findPrimaryConnectionSelectFromMultipleByRank() {
        insertFacebookConnection2()
        insertFacebookConnection()
        assertFacebookConnection(connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java))
    }

    @Test(expected = NotConnectedException::class)
    fun findPrimaryConnectionNotConnected() {
        connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java)
    }

    @Test
    fun removeConnections() {
        val facebookConnection = insertFacebookConnection()
        insertFacebookConnection2()
        assertThat(socialUserConnectionRepository!!.findOne(facebookConnection.id)).isNotNull()
        connectionRepository!!.removeConnections("facebook")
        assertThat(socialUserConnectionRepository!!.findOne(facebookConnection.id)).isNull()
    }

    @Test
    fun removeConnectionsToProviderNoOp() {
        connectionRepository!!.removeConnections("twitter")
    }

    @Test
    fun removeConnection() {
        val facebookConnection = insertFacebookConnection()
        assertThat(socialUserConnectionRepository!!.findOne(facebookConnection.id)).isNotNull()
        connectionRepository!!.removeConnection(ConnectionKey("facebook", "9"))
        assertThat(socialUserConnectionRepository!!.findOne(facebookConnection.id)).isNull()
    }

    @Test
    fun removeConnectionNoOp() {
        connectionRepository!!.removeConnection(ConnectionKey("facebook", "1"))
    }

    @Test
    fun addConnection() {
        val connection = connectionFactory!!.createConnection(AccessGrant("123456789", null, "987654321", 3600L))
        connectionRepository!!.addConnection(connection)
        val restoredConnection = connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java)
        assertEquals(connection, restoredConnection)
        assertNewConnection(restoredConnection)
    }

    @Test(expected = DataIntegrityViolationException::class)
    fun addConnectionDuplicate() {
        val connection = connectionFactory!!.createConnection(AccessGrant("123456789", null, "987654321", 3600L))
        connectionRepository!!.addConnection(connection)
        connectionRepository!!.addConnection(connection)
        socialUserConnectionRepository!!.flush()
    }

    @Test
    fun updateConnectionProfileFields() {
        connectionFactoryRegistry!!.addConnectionFactory(TestTwitterConnectionFactory())
        insertTwitterConnection()
        val twitter = connectionRepository!!.getPrimaryConnection(TestTwitterApi::class.java)
        assertEquals("http://twitter.com/kdonald/picture", twitter.imageUrl)
        twitter.sync()
        assertEquals("http://twitter.com/kdonald/a_new_picture", twitter.imageUrl)
        connectionRepository!!.updateConnection(twitter)
        val twitter2 = connectionRepository!!.getPrimaryConnection(TestTwitterApi::class.java)
        assertEquals("http://twitter.com/kdonald/a_new_picture", twitter2.imageUrl)
    }

    // Todo:
    @Ignore
    @Test
    fun updateConnectionAccessFields() {
        insertFacebookConnection()
        val facebook = connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java)
        assertEquals("234567890", facebook.api.accessToken)
        facebook.refresh()
        connectionRepository!!.updateConnection(facebook)
        val facebook2 = connectionRepository!!.getPrimaryConnection(TestFacebookApi::class.java)
        assertEquals("765432109", facebook2.api.accessToken)
        val data = facebook.createData()
        assertEquals("654321098", data.refreshToken)
    }

    @Test
    fun findPrimaryConnection_afterRemove() {
        insertFacebookConnection()
        insertFacebookConnection2()
        // 9 is the providerUserId of the first Facebook connection
        connectionRepository!!.removeConnection(ConnectionKey("facebook", "9"))
        assertEquals(1, connectionRepository!!.findConnections(TestFacebookApi::class.java).size.toLong())
        assertNotNull(connectionRepository!!.findPrimaryConnection(TestFacebookApi::class.java))
    }

    private fun insertTwitterConnection(): SocialUserConnectionEntity {
        return createExistingSocialUserConnection(
                "1",
                "twitter",
                "1",
                1L,
                "@kdonald",
                "http://twitter.com/kdonald",
                "http://twitter.com/kdonald/picture",
                "123456789",
                "987654321", null, null
        )
    }

    private fun insertFacebookConnection(): SocialUserConnectionEntity {
        return createExistingSocialUserConnection(
                "1",
                "facebook",
                "9",
                1L, null, null, null,
                "234567890", null,
                "345678901",
                System.currentTimeMillis() + 3600000)
    }

    private fun insertFacebookConnection2(): SocialUserConnectionEntity {
        return createExistingSocialUserConnection(
                "1",
                "facebook",
                "10",
                2L, null, null, null,
                "456789012", null,
                "56789012",
                System.currentTimeMillis() + 3600000)
    }

    private fun insertFacebookConnection3(): SocialUserConnectionEntity {
        return createExistingSocialUserConnection(
                "2",
                "facebook",
                "11", 2L, null, null, null,
                "456789012", null,
                "56789012",
                System.currentTimeMillis() + 3600000)
    }

    private fun insertFacebookConnectionSameFacebookUser(): SocialUserConnectionEntity {
        return createExistingSocialUserConnection(
                "2",
                "facebook",
                "9",
                1L, null, null, null,
                "234567890", null,
                "345678901",
                System.currentTimeMillis() + 3600000)
    }

    private fun createExistingSocialUserConnection(userId: String,
                                                   providerId: String,
                                                   providerUserId: String,
                                                   rank: Long?,
                                                   displayName: String?,
                                                   profileURL: String?,
                                                   imageURL: String?,
                                                   accessToken: String,
                                                   secret: String?,
                                                   refreshToken: String?,
                                                   expireTime: Long?): SocialUserConnectionEntity {
        val socialUserConnectionEntityToSabe = SocialUserConnectionEntity(
                userId,
                providerId,
                providerUserId,
                rank,
                displayName,
                profileURL,
                imageURL,
                accessToken,
                secret,
                refreshToken,
                expireTime)
        return socialUserConnectionRepository!!.save(socialUserConnectionEntityToSabe)
    }

    private fun assertNewConnection(connection: Connection<TestFacebookApi>) {
        assertEquals("facebook", connection.key.providerId)
        assertEquals("9", connection.key.providerUserId)
        assertEquals("Keith Donald", connection.displayName)
        assertEquals("http://facebook.com/keith.donald", connection.profileUrl)
        assertEquals("http://facebook.com/keith.donald/picture", connection.imageUrl)
        assertTrue(connection.test())
        val api = connection.api
        assertNotNull(api)
        assertEquals("123456789", api.accessToken)
        assertEquals("123456789", connection.createData().accessToken)
        assertEquals("987654321", connection.createData().refreshToken)
    }

    private fun assertTwitterConnection(twitter: Connection<TestTwitterApi>) {
        assertEquals(ConnectionKey("twitter", "1"), twitter.key)
        assertEquals("@kdonald", twitter.displayName)
        assertEquals("http://twitter.com/kdonald", twitter.profileUrl)
        assertEquals("http://twitter.com/kdonald/picture", twitter.imageUrl)
        val twitterApi = twitter.api
        assertEquals("123456789", twitterApi.accessToken)
        assertEquals("987654321", twitterApi.secret)
        twitter.sync()
        assertEquals("http://twitter.com/kdonald/a_new_picture", twitter.imageUrl)
    }

    private fun assertFacebookConnection(facebook: Connection<TestFacebookApi>) {
        assertEquals(ConnectionKey("facebook", "9"), facebook.key)
        assertEquals(null, facebook.displayName)
        assertEquals(null, facebook.profileUrl)
        assertEquals(null, facebook.imageUrl)
        val facebookApi = facebook.api
        assertEquals("234567890", facebookApi.accessToken)
        facebook.sync()
        assertEquals("Keith Donald", facebook.displayName)
        assertEquals("http://facebook.com/keith.donald", facebook.profileUrl)
        assertEquals("http://facebook.com/keith.donald/picture", facebook.imageUrl)
    }

    // test facebook provider
    private class TestFacebookConnectionFactory : OAuth2ConnectionFactory<TestFacebookApi>("facebook", TestFacebookServiceProvider(), TestFacebookApiAdapter())

    private class TestFacebookServiceProvider : OAuth2ServiceProvider<TestFacebookApi> {

        override fun getOAuthOperations(): OAuth2Operations {
            return object : OAuth2Operations {
                override fun buildAuthorizeUrl(grantType: GrantType, params: OAuth2Parameters): String? {
                    return null
                }

                override fun buildAuthenticateUrl(grantType: GrantType, params: OAuth2Parameters): String? {
                    return null
                }

                override fun buildAuthorizeUrl(params: OAuth2Parameters): String? {
                    return null
                }

                override fun buildAuthenticateUrl(params: OAuth2Parameters): String? {
                    return null
                }

                override fun exchangeForAccess(authorizationGrant: String, redirectUri: String, additionalParameters: MultiValueMap<String, String>): AccessGrant? {
                    return null
                }

                override fun exchangeCredentialsForAccess(username: String, password: String, additionalParameters: MultiValueMap<String, String>): AccessGrant? {
                    return null
                }

                override fun refreshAccess(refreshToken: String, additionalParameters: MultiValueMap<String, String>): AccessGrant {
                    return AccessGrant("765432109", "read", "654321098", 3600L)
                }

                @Deprecated("")
                override fun refreshAccess(refreshToken: String, scope: String, additionalParameters: MultiValueMap<String, String>): AccessGrant {
                    return AccessGrant("765432109", "read", "654321098", 3600L)
                }

                override fun authenticateClient(): AccessGrant? {
                    return null
                }

                override fun authenticateClient(scope: String): AccessGrant? {
                    return null
                }
            }
        }

        override fun getApi(accessToken: String): TestFacebookApi {
            return  object:TestFacebookApi {
                override var accessToken: String = accessToken
            }
        }

    }

    interface TestFacebookApi {

        val accessToken: String

    }

    private class TestFacebookApiAdapter : ApiAdapter<TestFacebookApi> {

        private val accountId = "9"

        private val name = "Keith Donald"

        private val profileUrl = "http://facebook.com/keith.donald"

        private val profilePictureUrl = "http://facebook.com/keith.donald/picture"

        override fun test(api: TestFacebookApi): Boolean {
            return true
        }

        override fun setConnectionValues(api: TestFacebookApi, values: ConnectionValues) {
            values.setProviderUserId(accountId)
            values.setDisplayName(name)
            values.setProfileUrl(profileUrl)
            values.setImageUrl(profilePictureUrl)
        }

        override fun fetchUserProfile(api: TestFacebookApi): UserProfile {
            return UserProfileBuilder().setName(name).setEmail("keith@interface21.com").setUsername("Keith.Donald").build()
        }

        override fun updateStatus(api: TestFacebookApi, message: String) {

        }
    }

    // test twitter provider
    private class TestTwitterConnectionFactory : OAuth1ConnectionFactory<TestTwitterApi>("twitter", TestTwitterServiceProvider(), TestTwitterApiAdapter())

    private class TestTwitterServiceProvider : OAuth1ServiceProvider<TestTwitterApi> {

        override fun getOAuthOperations(): OAuth1Operations? {
            return null
        }

        override fun getApi(accessToken: String, secret: String): TestTwitterApi {
            return object : TestTwitterApi {
                override val accessToken: String
                    get() = accessToken

                override val secret: String
                    get() = secret
            }
        }

    }

    interface TestTwitterApi {

        val accessToken: String

        val secret: String

    }

    private class TestTwitterApiAdapter : ApiAdapter<TestTwitterApi> {

        private val accountId = "1"

        private val name = "@kdonald"

        private val profileUrl = "http://twitter.com/kdonald"

        private val profilePictureUrl = "http://twitter.com/kdonald/a_new_picture"

        override fun test(api: TestTwitterApi): Boolean {
            return true
        }

        override fun setConnectionValues(api: TestTwitterApi, values: ConnectionValues) {
            values.setProviderUserId(accountId)
            values.setDisplayName(name)
            values.setProfileUrl(profileUrl)
            values.setImageUrl(profilePictureUrl)
        }

        override fun fetchUserProfile(api: TestTwitterApi): UserProfile {
            return UserProfileBuilder().setName(name).setUsername("kdonald").build()
        }

        override fun updateStatus(api: TestTwitterApi, message: String) {}

    }
}
