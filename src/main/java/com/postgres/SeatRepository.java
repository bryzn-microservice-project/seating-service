package com.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import com.postgres.models.Movies;
import java.util.List;

/*
 * NOTES: all included methods inside of JPA repository
        save(S entity)	                            Save or update an entity
        saveAll(Iterable<S> entities)	            Save multiple entities
        findById(ID id)	                            Find by primary key
        existsById(ID id)	                        Check existence by ID
        findAll()	                                Get all records
        findAllById(Iterable<ID> ids)	            Get records by list of IDs
        count()	                                    Count total records
        deleteById(ID id)	                        Delete by ID
        delete(T entity)	                        Delete a specific entity
        deleteAllById(Iterable<? extends ID> ids)	Delete multiple by ID
        deleteAll(Iterable<? extends T> entities)	Delete multiple entities
        deleteAll()	                                Delete all records
 */


// Spring Data JPA creates CRUD implementation at runtime automatically.
public interface SeatRepository extends JpaRepository<Movies, Long> {
	List<Movies> findByMovieName(String seatNumber);
}