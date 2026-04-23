package ch.uzh.ifi.hase.soprafs26.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.hase.soprafs26.entity.Post;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PostPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.PostService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PostController.class)
public class PostControllerTest {
    @Autowired
	private MockMvc mockMvc;

    @MockitoBean
	private PostService postService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EventRepository eventRepository;

    @BeforeEach
    public void setupAuthentication() {
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        given(userRepository.findByToken(anyString())).willReturn(authenticatedUser);
    }

    @Test
    void createPost_validInput_returnsCreatedPost() throws Exception {
        // given
        Post post = new Post();
        post.setId(1L);
        post.setContent("Test post");

        given(postService.createPost(any(PostPostDTO.class)))
                .willReturn(post);


        PostPostDTO postPostDTO = new PostPostDTO();
        postPostDTO.setContent("Test post");

        MockHttpServletRequestBuilder request = post("/events/1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postPostDTO))
                .header("Authorization", "Bearer valid-token");

        // when + then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("Test post")));
    }

    @Test
    void getPosts_validEvent_returnsPostList() throws Exception {
        // given
        Post post = new Post();
        post.setId(1L);
        post.setContent("Hello World");

        given(postService.getPostsByEvent(eq(1L), eq("valid-token")))
                .willReturn(List.of(post));

        MockHttpServletRequestBuilder request = get("/events/1/posts")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // when + then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].content", is("Hello World")));
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
