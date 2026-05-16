package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		// testUser.setName("testName");
		testUser.setUsername("testUsername");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		// assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	// @Test
	// public void createUser_duplicateName_throwsException() {
	// 	// given -> a first user has already been created
	// 	userService.createUser(testUser);

	// 	// when -> setup additional mocks for UserRepository
	// 	// Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
	// 	Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

	// 	// then -> attempt to create second user with same user -> check that an error
	// 	// is thrown
	// 	assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	// }

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		// Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}



    @Test
    public void updateUser_setsAllowPrivateMessages_success() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("alice");
        existingUser.setAllowPrivateMessages(true);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(existingUser));

        User updates = new User();
        updates.setAllowPrivateMessages(false);

        User result = userService.updateUser(1L, updates);

        assertEquals(false, result.getAllowPrivateMessages());
        Mockito.verify(userRepository, Mockito.times(1)).save(existingUser);
    }

	@Test
	public void followUser_validInput_success() {
		// given
		User follower = new User();
		follower.setId(1L);
		follower.setUsername("follower");
		follower.setFollowing(new HashSet<>());

		User target = new User();
		target.setId(2L);
		target.setUsername("target");

		Mockito.when(userRepository.findById(1L))
				.thenReturn(Optional.of(follower));

		Mockito.when(userRepository.findById(2L))
				.thenReturn(Optional.of(target));

		Mockito.when(userRepository.save(Mockito.any(User.class)))
				.thenReturn(follower);

		// when
		User result = userService.followUser(1L, 2L);

		// then
		assertNotNull(result);

		Mockito.verify(userRepository, Mockito.times(1))
				.save(Mockito.any(User.class));

		assertTrue(
            follower.getFollowing().stream()
                    .anyMatch(user -> user.getId().equals(2L))
    	);
	}

	@Test
	public void getUsers_returnsAllUsers() {
		User user2 = new User();
		user2.setId(2L);
		user2.setUsername("other");

		Mockito.when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

		List<User> result = userService.getUsers();

		assertEquals(2, result.size());
	}

	@Test
	public void getUser_existingId_returnsUser() {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		User result = userService.getUser(1L);

		assertNotNull(result);
		assertEquals(testUser.getUsername(), result.getUsername());
	}

	@Test
	public void getUser_notFound_throwsException() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.getUser(99L));
	}

	@Test
	public void loginUser_validCredentials_success() {
		User stored = new User();
		stored.setId(1L);
		stored.setUsername("testUsername");
		stored.setPassword("secret");
		stored.setStatus(UserStatus.OFFLINE);

		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(stored);
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(stored);

		User loginAttempt = new User();
		loginAttempt.setUsername("testUsername");
		loginAttempt.setPassword("secret");

		User result = userService.loginUser(loginAttempt);

		assertEquals(UserStatus.ONLINE, result.getStatus());
	}

	@Test
	public void loginUser_wrongPassword_throwsException() {
		User stored = new User();
		stored.setUsername("testUsername");
		stored.setPassword("correct");

		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(stored);

		User loginAttempt = new User();
		loginAttempt.setUsername("testUsername");
		loginAttempt.setPassword("wrong");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginAttempt));
	}

	@Test
	public void unfollowUser_validInput_success() {
		User follower = new User();
		follower.setId(1L);
		follower.setUsername("follower");

		User followee = new User();
		followee.setId(2L);
		followee.setUsername("followee");

		follower.setFollowing(new HashSet<>(Set.of(followee)));

		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
		Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(followee));
		Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(follower);

		User result = userService.unfollowUser(1L, 2L);

		assertNotNull(result);
		assertTrue(follower.getFollowing().stream().noneMatch(u -> u.getId().equals(2L)));
	}

	@Test
	public void unfollowUser_notFollowing_throwsConflict() {
		User follower = new User();
		follower.setId(1L);
		follower.setFollowing(new HashSet<>());

		User followee = new User();
		followee.setId(2L);

		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
		Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(followee));

		assertThrows(ResponseStatusException.class, () -> userService.unfollowUser(1L, 2L));
	}

	@Test
	public void followedUsers_getFollowingUsers_FollowingUsersReturned() {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");

		User followedUser = new User();
		followedUser.setId(2L);
		followedUser.setUsername("followedUsername");

		user.setFollowing(new HashSet<>(Set.of(followedUser)));

		Mockito.when(userRepository.findById(1L))
				.thenReturn(Optional.of(user));

		// when
		Set<User> result = userService.getFollowingUsers(1L);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.equals(Set.of(followedUser)));
	}

}
