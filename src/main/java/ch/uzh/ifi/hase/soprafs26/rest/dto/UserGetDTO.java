package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

public class UserGetDTO {

	private Long id;
	// private String name;
	private String username;
	private UserStatus status;
	private String bio;
    private Boolean allowPrivateMessages;
	private List<User> following;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	// public String getName() {
	// 	return name;
	// }

	// public void setName(String name) {
	// 	this.name = name;
	// }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}
    public Boolean getAllowPrivateMessages() {
        return allowPrivateMessages;
    }

    public void setAllowPrivateMessages(Boolean allowPrivateMessages) {
        this.allowPrivateMessages = allowPrivateMessages;
    }

	public List<User> getFollowing() {
		return following;
	}

	public void setFollowing(List<User> following) {
		this.following = following;
	}
}
