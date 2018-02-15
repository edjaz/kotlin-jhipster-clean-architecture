package fr.edjaz.web.service;

import fr.edjaz.AppApp;
import fr.edjaz.domain.AuthorityEntity;
import fr.edjaz.domain.UserEntity;
import fr.edjaz.domain.gateway.AuthorityGateway;
import fr.edjaz.domain.gateway.UserGateway;
import fr.edjaz.domain.gateway.UserSearchGateway;
import fr.edjaz.repository.AuthorityRepository;
import fr.edjaz.repository.UserRepository;
import fr.edjaz.repository.search.UserSearchRepository;
import fr.edjaz.web.service.mail.SendSocialRegistrationValidationEmail;
import fr.edjaz.web.service.security.AuthoritiesConstants;
import fr.edjaz.web.service.social.CreateSocialUser;
import fr.edjaz.web.service.social.CreateSocialUserImpl;
import fr.edjaz.web.service.social.DeleteUserSocialConnection;
import fr.edjaz.web.service.social.DeleteUserSocialConnectionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.connect.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppApp.class)
@Transactional
public class SocialServiceIntTest {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSearchGateway userSearchGateway;

    @Autowired
    private AuthorityGateway authorityGateway;

    @Autowired
    private UserGateway userGateway;

    @Mock
    private SendSocialRegistrationValidationEmail sendSocialRegistrationValidationEmail;

    @Mock
    private UsersConnectionRepository mockUsersConnectionRepository;

    @Mock
    private ConnectionRepository mockConnectionRepository;

    private CreateSocialUser createSocialUser;
    private DeleteUserSocialConnection deleteUserSocialConnection;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(sendSocialRegistrationValidationEmail).execute(anyObject());
        doNothing().when(mockConnectionRepository).addConnection(anyObject());
        when(mockUsersConnectionRepository.createConnectionRepository(anyString())).thenReturn(mockConnectionRepository);
        createSocialUser= new CreateSocialUserImpl(mockUsersConnectionRepository,authorityGateway,passwordEncoder,userGateway,sendSocialRegistrationValidationEmail,userSearchGateway);
        deleteUserSocialConnection= new DeleteUserSocialConnectionImpl(mockUsersConnectionRepository);
        //socialService = new SocialService(mockUsersConnectionRepository, authorityGateway, passwordEncoder, userGateway, sendSocialRegistrationValidationEmail, userSearchRepository);
    }

    @Test
    public void testDeleteUserSocialConnection() throws Exception {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");
        createSocialUser.execute(connection, "fr");
        MultiValueMap<String, Connection<?>> connectionsByProviderId = new LinkedMultiValueMap<>();
        connectionsByProviderId.put("PROVIDER", null);
        when(mockConnectionRepository.findAllConnections()).thenReturn(connectionsByProviderId);

        // Exercise
        deleteUserSocialConnection.execute("LOGIN");

        // Verify
        verify(mockConnectionRepository, times(1)).removeConnections("PROVIDER");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSocialUserShouldThrowExceptionIfConnectionIsNull() {
        // Exercise
        createSocialUser.execute(null, "fr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSocialUserShouldThrowExceptionIfConnectionHasNoEmailAndNoLogin() {
        // Setup
        Connection<?> connection = createConnection("",
            "",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSocialUserShouldThrowExceptionIfConnectionHasNoEmailAndLoginAlreadyExist() {
        // Setup
        UserEntity user = createExistingUser("login",
            "mail@mail.com",
            "OTHER_FIRST_NAME",
            "OTHER_LAST_NAME",
            "OTHER_IMAGE_URL");
        Connection<?> connection = createConnection("LOGIN",
            "",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        try {
            // Exercise
            createSocialUser.execute(connection, "fr");
        } finally {
            // Teardown
            userRepository.delete(user);
        }
    }

    @Test
    public void testCreateSocialUserShouldCreateUserIfNotExist() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        // Verify
        final Optional<UserEntity> user = userRepository.findOneByEmailIgnoreCase("mail@mail.com");
        assertThat(user).isPresent();

        // Teardown
        userRepository.delete(user.get());
    }

    @Test
    public void testCreateSocialUserShouldCreateUserWithSocialInformation() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        UserEntity user = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        assertThat(user.getFirstName()).isEqualTo("FIRST_NAME");
        assertThat(user.getLastName()).isEqualTo("LAST_NAME");
        assertThat(user.getImageUrl()).isEqualTo("IMAGE_URL");

        // Teardown
        userRepository.delete(user);
    }

    @Test
    public void testCreateSocialUserShouldCreateActivatedUserWithRoleUserAndPassword() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        UserEntity user = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        assertThat(user.getActivated()).isEqualTo(true);
        assertThat(user.getPassword()).isNotEmpty();
        AuthorityEntity userAuthority = authorityRepository.findOne(AuthoritiesConstants.INSTANCE.USER);
        assertThat(user.getAuthorities().toArray()).containsExactly(userAuthority);

        // Teardown
        userRepository.delete(user);
    }

    @Test
    public void testCreateSocialUserShouldCreateUserWithExactLangKey() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        final UserEntity user = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        assertThat(user.getLangKey()).isEqualTo("fr");

        // Teardown
        userRepository.delete(user);
    }

    @Test
    public void testCreateSocialUserShouldCreateUserWithLoginSameAsEmailIfNotTwitter() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER_OTHER_THAN_TWITTER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        UserEntity user = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        assertThat(user.getLogin()).isEqualTo("first_name_last_name");

        // Teardown
        userRepository.delete(user);
    }

    @Test
    public void testCreateSocialUserShouldCreateUserWithSocialLoginWhenIsTwitter() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "twitter");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        UserEntity user = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        assertThat(user.getLogin()).isEqualToIgnoringCase("login");

        // Teardown
        userRepository.delete(user);
    }

    @Test
    public void testCreateSocialUserShouldCreateSocialConnection() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        verify(mockConnectionRepository, times(1)).addConnection(connection);

        // Teardown
        UserEntity userToDelete = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        userRepository.delete(userToDelete);
    }

    @Test
    public void testCreateSocialUserShouldNotCreateUserIfEmailAlreadyExist() {
        // Setup
        createExistingUser("other_login",
            "mail@mail.com",
            "OTHER_FIRST_NAME",
            "OTHER_LAST_NAME",
            "OTHER_IMAGE_URL");
        long initialUserCount = userRepository.count();
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        assertThat(userRepository.count()).isEqualTo(initialUserCount);

        // Teardown
        UserEntity userToDelete = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        userRepository.delete(userToDelete);
    }

    @Test
    public void testCreateSocialUserShouldNotChangeUserIfEmailAlreadyExist() {
        // Setup
        createExistingUser("other_login",
            "mail@mail.com",
            "OTHER_FIRST_NAME",
            "OTHER_LAST_NAME",
            "OTHER_IMAGE_URL");
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        UserEntity userToVerify = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        assertThat(userToVerify.getLogin()).isEqualTo("other_login");
        assertThat(userToVerify.getFirstName()).isEqualTo("OTHER_FIRST_NAME");
        assertThat(userToVerify.getLastName()).isEqualTo("OTHER_LAST_NAME");
        assertThat(userToVerify.getImageUrl()).isEqualTo("OTHER_IMAGE_URL");
        // Teardown
        userRepository.delete(userToVerify);
    }

    @Test
    public void testCreateSocialUserShouldSendRegistrationValidationEmail() {
        // Setup
        Connection<?> connection = createConnection("LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER");

        // Exercise
        createSocialUser.execute(connection, "fr");

        //Verify
        verify(sendSocialRegistrationValidationEmail, times(1)).execute(anyObject());

        // Teardown
        UserEntity userToDelete = userRepository.findOneByEmailIgnoreCase("mail@mail.com").get();
        userRepository.delete(userToDelete);
    }

    private Connection<?> createConnection(String login,
                                           String email,
                                           String firstName,
                                           String lastName,
                                           String imageUrl,
                                           String providerId) {
        UserProfile userProfile = mock(UserProfile.class);
        when(userProfile.getEmail()).thenReturn(email);
        when(userProfile.getUsername()).thenReturn(login);
        when(userProfile.getFirstName()).thenReturn(firstName);
        when(userProfile.getLastName()).thenReturn(lastName);

        Connection<?> connection = mock(Connection.class);
        ConnectionKey key = new ConnectionKey(providerId, "PROVIDER_USER_ID");
        when(connection.fetchUserProfile()).thenReturn(userProfile);
        when(connection.getKey()).thenReturn(key);
        when(connection.getImageUrl()).thenReturn(imageUrl);

        return connection;
    }

    private UserEntity createExistingUser(String login,
                                          String email,
                                          String firstName,
                                          String lastName,
                                          String imageUrl) {
        UserEntity user = new UserEntity();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setImageUrl(imageUrl);
        return userRepository.saveAndFlush(user);
    }
}
