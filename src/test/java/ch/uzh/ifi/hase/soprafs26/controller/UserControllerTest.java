package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.RatingService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private EventService eventService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RatingService ratingService;

	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void givenUser_whenGetUser_thenReturnJson() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setStatus(UserStatus.ONLINE);

		given(userService.getUser(user.getId())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void loginUser_validInput_userLoggedIn() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("test token");

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");

		given(userService.loginUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.token", is(user.getToken())));
		
	}

	@Test
	public void updateUser_validInput_userUpdated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		User userUpdated = new User();
		userUpdated.setId(1L);
		userUpdated.setUsername("updatedUsername");
		userUpdated.setToken("1");
		userUpdated.setStatus(UserStatus.ONLINE);

		UserPutDTO userPutDTO = new UserPutDTO();
		// userPostDTO.setName("Test User");
		userPutDTO.setUsername("updatedUsername");

		given(userService.updateUser(Mockito.anyLong(), Mockito.any())).willReturn(userUpdated);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = put("/users/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPutDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(userUpdated.getId().intValue())))
				.andExpect(jsonPath("$.username", is(userUpdated.getUsername())))
				.andExpect(jsonPath("$.status", is(userUpdated.getStatus().toString())));
	
	}

	@Test
	public void followUser_validInput_userFollowed() throws Exception {
		// given
		User follower = new User();
		follower.setId(1L);
		follower.setUsername("followerUsername");
		follower.setToken("1");
		follower.setStatus(UserStatus.ONLINE);
		follower.setFollowing(new HashSet<>());

		User targetUser = new User();
		targetUser.setId(2L);
		targetUser.setUsername("targetUsername");
		targetUser.setToken("2");
		targetUser.setStatus(UserStatus.ONLINE);
		targetUser.setFollowing(new HashSet<>());

		follower.getFollowing().add(targetUser);

		
		given(userRepository.findByToken(anyString())).willReturn(follower);

		given(userService.followUser(follower.getId(), targetUser.getId()))
            .willReturn(follower);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users/{targetUserId}/follow", targetUser.getId())
				.header("Authorization", "Bearer 1")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(follower.getId().intValue())))
				.andExpect(jsonPath("$.username", is(follower.getUsername())))
				.andExpect(jsonPath("$.status", is(follower.getStatus().toString())))
				.andExpect(jsonPath("$.followingIds[0]", is(targetUser.getId().intValue())));
	
	}

	@Test
	public void followingUsers_getFollowingUsers_followingUsersReturned() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");

		User followedUser = new User();
		followedUser.setId(2L);
		followedUser.setUsername("followedUsername");


		user.setFollowing(new HashSet<>(Set.of(followedUser)));
		
		given(userRepository.findByToken(anyString())).willReturn(user);
		given(userService.getFollowingUsers(user.getId())).willReturn(user.getFollowing());

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder getRequest = get("/users/following")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON);
		
		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(followedUser.getId().intValue())))
				.andExpect(jsonPath("$[0].username", is(followedUser.getUsername())));
	}

	@Test
	public void validateToken_validToken_returnsOk() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");

		given(userRepository.findByToken(anyString())).willReturn(user);

		MockHttpServletRequestBuilder getRequest = get("/auth/validate")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest)
				.andExpect(status().isOk());
	}

	@Test
	public void getUserEvents_returnsEventList() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");

		ch.uzh.ifi.hase.soprafs26.entity.Event event = new ch.uzh.ifi.hase.soprafs26.entity.Event();
		event.setId(10L);
		event.setTitle("My Event");
		event.setIsPrivate(false);
		event.setCreator(user);
		event.setParticipants(List.of(user));

		given(eventService.getEventsByUserId(1L)).willReturn(List.of(event));

		MockHttpServletRequestBuilder getRequest = get("/users/1/events")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title", is(event.getTitle())));
	}

	@Test
	public void unfollowUser_validInput_returnsOk() throws Exception {
		User follower = new User();
		follower.setId(1L);
		follower.setUsername("follower");
		follower.setToken("token-1");
		follower.setStatus(UserStatus.ONLINE);
		follower.setFollowing(new HashSet<>());

		given(userRepository.findByToken(anyString())).willReturn(follower);
		given(userService.unfollowUser(follower.getId(), 2L)).willReturn(follower);

		MockHttpServletRequestBuilder deleteRequest = delete("/users/2/follow")
				.header("Authorization", "Bearer token-1")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(deleteRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(follower.getId().intValue())))
				.andExpect(jsonPath("$.username", is(follower.getUsername())));
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}