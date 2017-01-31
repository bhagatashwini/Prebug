package com.pass.prebug.output;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.pass.prebug.hibernate.dao.AuthorsDao;
import com.pass.prebug.hibernate.dao.CommitsDao;
import com.pass.prebug.hibernate.dao.CommitFilesDao;
import com.pass.prebug.hibernate.dao.MetaInfoDao;
import com.pass.prebug.hibernate.database.Commits;
import com.pass.prebug.hibernate.database.MetaInfos;

public class MetaInfosBuilder {
	private static CommitsDao cdao = new CommitsDao();
	private static CommitFilesDao fdao = new CommitFilesDao();
	private static AuthorsDao adao = new AuthorsDao();
	private static MetaInfoDao mdao = new MetaInfoDao();

	public LocalDateTime getCurrentTimeStamp() {
		java.util.Date date = new java.util.Date();
		return new Timestamp(date.getTime()).toLocalDateTime();
	}

	public LocalDateTime getReportStartPeriod() {
		Commits startcommit = null;
		LocalDateTime startCommitTimestamp = null;
		startcommit = cdao.findFirstCommitEntry("commitTimestamp");
		startCommitTimestamp = startcommit.getcommitTimestamp();
		return startCommitTimestamp;
	}

	public LocalDateTime getReportEndPeriod() {
		Commits endCommit = null;
		LocalDateTime endCommitTimestamp = null;
		endCommit = cdao.findLastCommitEntry("commitTimestamp");
		endCommitTimestamp = endCommit.getcommitTimestamp();
		return endCommitTimestamp;
	}

	public Long getCommitCount() {

		return cdao.CountCommits("commitHash");
	}

	public Long getCommitFileCount() {

		return fdao.CountFiles("file");
	}

	public Long getDeveloperCount() {

		return adao.CountAuthors("author");
	}

	public MetaInfos getLastEntryBylastGeneratedDate() {
		return mdao.findLastMetaInfoEntry("lastCommitDate");
	}

	public Commits getLastCommitInfo() {
		return cdao.findLastCommitEntry("commitTimestamp");
	}

}
