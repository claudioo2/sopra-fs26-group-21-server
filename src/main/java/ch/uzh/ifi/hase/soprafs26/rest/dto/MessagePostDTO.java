package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class MessagePostDTO {
    private String content;
    private Long eventId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
