package com.pass.prebug.hibernate.dao;

import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import com.pass.prebug.hibernate.database.Commits;

public class CommitsDao extends GenericDaoJpa<Commits> {

	public Optional<Commits> findByHash(String hash) {

		try {
			return Optional.of(findByProperty(Commits.class, "commitHash", hash));
		} catch (NoResultException e) {
			return Optional.empty();
		} catch (NonUniqueResultException nurex) {
			// should never happen since commitHash column is unique in the
			// database
			return Optional.empty();
		}
	}

	public Commits findFirstCommitEntry(String propertyName) {

		return findFirstEntry(Commits.class, propertyName);
	}

	public Commits findLastCommitEntry(String propertyName) {

		return findLastEntry(Commits.class, propertyName);
	}

	public Long CountCommits(String Commit) {
		return CountEntry(Commits.class, Commit);
	}

}
