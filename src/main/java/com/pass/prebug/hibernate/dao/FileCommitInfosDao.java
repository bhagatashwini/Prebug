package com.pass.prebug.hibernate.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import com.pass.prebug.hibernate.database.FileCommitInfos;

public class FileCommitInfosDao extends GenericDaoJpa<FileCommitInfos>{

	public Optional<FileCommitInfos> findInfoByFile(String file) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try {
			return Optional.of(findByProperty(FileCommitInfos.class, "commitfile", file));
		} catch (NoResultException  e) {
			return Optional.empty();
		} catch (NonUniqueResultException nurex){
			// should never happen since commitHash column is unique in the database
			return Optional.empty();
		}
	}
}