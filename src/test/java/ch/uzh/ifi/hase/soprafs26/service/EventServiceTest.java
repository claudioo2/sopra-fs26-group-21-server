package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.User;   
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;



import java.time.LocalDateTime;


public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private EventService eventService;

    private User creator;
    private Event newEvent;
    private Event event;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        creator = new User();
        creator.setId(1L);
        creator.setUsername("creator");

        newEvent = new Event();
        newEvent.setTitle("Test Event");
        newEvent.setStartTime(LocalDateTime.of(2026, 4, 13, 10, 0));
        newEvent.setEndTime(LocalDateTime.of(2026, 4, 25, 10, 0));

        event = new Event();
        event.setId(10L);
        event.setTitle("Test Event");
        event.setCreator(creator);

        

        when(eventRepository.findByInviteCode(any())).thenReturn(null);
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createEvent_validTimesTest() {
        Event result = eventService.createEvent(newEvent, creator);

        assertEquals(creator, result.getCreator());

        assertNotNull(result.getInviteCode());

        verify(eventRepository).save(newEvent);
    }

    @Test
    void createEvent_invalidTimeTest() {
        newEvent.setStartTime(LocalDateTime.of(2026, 4, 25, 10, 0));
        newEvent.setEndTime(LocalDateTime.of(2026, 4, 13, 10, 0));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.createEvent(newEvent, creator)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("End time must be after start time", exception.getReason());

        // ensure nothing is saved
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteEventTest() {
        Long eventId = 10L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        eventService.deleteEvent(eventId, creator);

        verify(eventRepository, never()).delete(any());
        verify(eventRepository).save(event);
        assertNotNull(event.getCancelledAt());
        verify(messagingTemplate).convertAndSend(eq("/topic/events/10/cancelled"), any(Object.class));
    }

}
