package com.pass.prebug.hibernate.dao;

import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import com.pass.prebug.hibernate.database.Authors;

public class AuthorsDao extends GenericDaoJpa<Authors> {

	public Optional<Authors> findByAuthor(String author) {
		try {
			return Optional.of(findByProperty(Authors.class, "author", author));
		} catch (NoResultException e) {
			return Optional.empty();

		} catch (NonUniqueResultException nurex) {
			// should never happen since commitHash column is unique in the
			// database
			return Optional.empty();
		}
	}

	public Long CountAuthors(String author) {
		return CountEntry(Authors.class, author);
	}
}
