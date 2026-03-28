package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Event;

@Repository("eventRepository")
public interface EventRepository extends JpaRepository<Event, Long> {
	// Event findByName(String name);
	Event findById(long id);
	
}
