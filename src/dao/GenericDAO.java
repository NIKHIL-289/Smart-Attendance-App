package com.attendance.dao;

import com.attendance.exception.AttendanceException;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface demonstrating Java Generics.
 * Provides a type-safe contract for all database access objects.
 *
 * Advanced Java Concept: Generics – <T> type parameter, Optional return types.
 *
 * @param <T>  the entity type
 * @param <ID> the primary-key type
 */
public interface GenericDAO<T, ID> {

    /**
     * Persist a new entity to the database.
     * @return the saved entity with its generated ID
     */
    T save(T entity) throws AttendanceException;

    /**
     * Retrieve an entity by its primary key, wrapped in an Optional.
     */
    Optional<T> findById(ID id) throws AttendanceException;

    /**
     * Retrieve all entities of this type.
     */
    List<T> findAll() throws AttendanceException;

    /**
     * Update an existing entity.
     */
    boolean update(T entity) throws AttendanceException;

    /**
     * Delete an entity by its primary key.
     */
    boolean deleteById(ID id) throws AttendanceException;
}
