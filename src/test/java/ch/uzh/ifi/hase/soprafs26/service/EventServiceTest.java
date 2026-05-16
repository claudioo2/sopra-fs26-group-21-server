package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.EventCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.time.LocalDateTime;


public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

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

        eventService.deleteEvent(eventId, creator);

        verify(eventRepository).delete(event);
    }

    @Test
    void getEventById_existingEvent_returned() {
        when(eventRepository.findById(10L)).thenReturn(event);

        Event result = eventService.getEventById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getEventById_notFound_throwsNotFound() {
        when(eventRepository.findById(99L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> eventService.getEventById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getEventsInRadius_returnsEventsInRange() {
        Event nearby = new Event();
        nearby.setLatitude(47.3769);
        nearby.setLongitude(8.5417);
        nearby.setEndTime(LocalDateTime.now().plusDays(1));
        nearby.setIsPrivate(false);
        nearby.setCategory(EventCategory.SPORTS);

        when(eventRepository.findAll()).thenReturn(List.of(nearby));

        List<Event> result = eventService.getEventsInRadius(47.3769, 8.5417, 10.0, creator, null);

        assertEquals(1, result.size());
    }

    @Test
    void getEventsInRadius_excludesExpiredEvents() {
        Event expired = new Event();
        expired.setLatitude(47.3769);
        expired.setLongitude(8.5417);
        expired.setEndTime(LocalDateTime.now().minusDays(1));
        expired.setIsPrivate(false);
        expired.setCategory(EventCategory.SPORTS);

        when(eventRepository.findAll()).thenReturn(List.of(expired));

        List<Event> result = eventService.getEventsInRadius(47.3769, 8.5417, 10.0, creator, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsInRadius_excludesPrivateEventForNonParticipant() {
        User outsider = new User();
        outsider.setId(99L);

        Event privateEvent = new Event();
        privateEvent.setLatitude(47.3769);
        privateEvent.setLongitude(8.5417);
        privateEvent.setEndTime(LocalDateTime.now().plusDays(1));
        privateEvent.setIsPrivate(true);
        privateEvent.setParticipants(new ArrayList<>());
        privateEvent.setCategory(EventCategory.SPORTS);

        when(eventRepository.findAll()).thenReturn(List.of(privateEvent));

        List<Event> result = eventService.getEventsInRadius(47.3769, 8.5417, 10.0, outsider, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsInRadius_includesPrivateEventForParticipant() {
        Event privateEvent = new Event();
        privateEvent.setLatitude(47.3769);
        privateEvent.setLongitude(8.5417);
        privateEvent.setEndTime(LocalDateTime.now().plusDays(1));
        privateEvent.setIsPrivate(true);
        privateEvent.setParticipants(new ArrayList<>(List.of(creator)));
        privateEvent.setCategory(EventCategory.SPORTS);

        when(eventRepository.findAll()).thenReturn(List.of(privateEvent));

        List<Event> result = eventService.getEventsInRadius(47.3769, 8.5417, 10.0, creator, null);

        assertEquals(1, result.size());
    }

    @Test
    void getEventsInRadius_filtersByCategory() {
        Event sportsEvent = new Event();
        sportsEvent.setLatitude(47.3769);
        sportsEvent.setLongitude(8.5417);
        sportsEvent.setEndTime(LocalDateTime.now().plusDays(1));
        sportsEvent.setIsPrivate(false);
        sportsEvent.setCategory(EventCategory.SPORTS);

        Event musicEvent = new Event();
        musicEvent.setLatitude(47.3769);
        musicEvent.setLongitude(8.5417);
        musicEvent.setEndTime(LocalDateTime.now().plusDays(1));
        musicEvent.setIsPrivate(false);
        musicEvent.setCategory(EventCategory.MUSIC);

        when(eventRepository.findAll()).thenReturn(List.of(sportsEvent, musicEvent));

        List<Event> result = eventService.getEventsInRadius(47.3769, 8.5417, 10.0, creator, Set.of(EventCategory.SPORTS));

        assertEquals(1, result.size());
        assertEquals(EventCategory.SPORTS, result.get(0).getCategory());
    }

    @Test
    void updateEvent_success() {
        Long eventId = 10L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Event updates = new Event();
        updates.setTitle("New Title");

        Event result = eventService.updateEvent(eventId, updates, creator);

        assertEquals("New Title", result.getTitle());
        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_notFound_throwsNotFound() {
        Long eventId = 99L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> eventService.updateEvent(eventId, new Event(), creator));
    }

    @Test
    void updateEvent_notCreator_throwsForbidden() {
        Long eventId = 10L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        User other = new User();
        other.setId(99L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> eventService.updateEvent(eventId, new Event(), other));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateEvent_invalidTimes_throwsBadRequest() {
        Long eventId = 10L;
        event.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        event.setEndTime(LocalDateTime.of(2026, 5, 2, 10, 0));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        Event updates = new Event();
        updates.setEndTime(LocalDateTime.of(2026, 4, 1, 10, 0));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> eventService.updateEvent(eventId, updates, creator));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getEventsByUserId_returnsList() {
        when(eventRepository.findByParticipantId(1L)).thenReturn(List.of(event));

        List<Event> result = eventService.getEventsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(event.getId(), result.get(0).getId());
    }

    @Test
    void getEventParticipants_returnsParticipants() {
        User participant = new User();
        participant.setId(2L);
        event.setParticipants(List.of(creator, participant));
        when(eventRepository.findById(10L)).thenReturn(event);

        List<User> result = eventService.getEventParticipants(10L);

        assertEquals(2, result.size());
    }

    @Test
    void getEventParticipants_nullParticipants_returnsEmpty() {
        event.setParticipants(null);
        when(eventRepository.findById(10L)).thenReturn(event);

        List<User> result = eventService.getEventParticipants(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
