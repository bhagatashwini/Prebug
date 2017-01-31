package com.pass.prebug.hibernate.dao;

import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import com.pass.prebug.hibernate.database.CommitFiles;

public class CommitFilesDao extends GenericDaoJpa<CommitFiles> {

	public Optional<CommitFiles> findByFile(String file) {
		try {
			return Optional.of(findByProperty(CommitFiles.class, "file", file));
		} catch (NoResultException e) {
			return Optional.empty();
		} catch (NonUniqueResultException nurex) {
			// should never happen since commitHash column is unique in the
			// database
			return Optional.empty();
		}
	}

	public Long CountFiles(String file) {
		return CountEntry(CommitFiles.class, file);
	}
}