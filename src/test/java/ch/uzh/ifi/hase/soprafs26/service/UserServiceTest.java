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

		User target = new User();
		target.setId(2L);
		target.setUsername("target");

		Mockito.when(userRepository.findById(1L))
				.thenReturn(java.util.Optional.of(follower));

		Mockito.when(userRepository.findById(2L))
				.thenReturn(java.util.Optional.of(target));

		Mockito.when(userRepository.save(Mockito.any(User.class)))
        		.thenReturn(follower);

		// when
		User result = userService.followUser(1L, 2L);

		// then
		assertNotNull(result);

		Mockito.verify(userRepository, Mockito.times(1))
				.save(Mockito.any(User.class));

		assertTrue(follower.getFollowing().contains(target));
	}

}
