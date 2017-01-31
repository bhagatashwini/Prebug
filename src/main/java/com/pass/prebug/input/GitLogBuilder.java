package com.pass.prebug.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pass.prebug.hibernate.dao.GenericDaoJpa;
import com.pass.prebug.hibernate.database.BaseEntity;
import com.pass.prebug.hibernate.database.Commits;
import com.pass.prebug.hibernate.database.MetaInfos;
import com.pass.prebug.output.MetaInfosBuilder;
import com.pass.prebug.output.OutputBuilder;

import javassist.NotFoundException;

public class GitLogBuilder {
	private static final Logger log = LoggerFactory.getLogger(GitLogBuilder.class);
	private GenericDaoJpa<BaseEntity<?>> dao = new GenericDaoJpa<>();
	private static AppProperties pb;

	public void runGitLogCommand()
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, NotFoundException {
		dao.beginTransaction();
		pb = AppProperties.getInstance();
		List<String> gitcommand = buildGitCommand();
		createProcess(gitcommand);
		dao.endTransaction();
	}

	public void parseGitLog(File f) throws FileNotFoundException {

		GitStatistics stats = new GitStatistics();
		FileInputStream fis = new FileInputStream(f);
		Scanner sc = new Scanner(new InputStreamReader(fis));
		while ((sc.hasNextLine())) {
			String line = sc.nextLine();
			stats.addLine(line);
		}
		sc.close();
	}
	
	
	private Process createProcess(List<String> gitcommand) throws IOException {

		ProcessBuilder builder = new ProcessBuilder();
		builder.command(gitcommand);
		Process process = builder.start();
		log.info("Executing git Command");

		buildStatistics(process.getInputStream(), process.getErrorStream());
		int errCode = 0;
		try {
			errCode = process.waitFor();
			boolean errorCode;
			if (errCode == 0)
				errorCode = false;
			else
				errorCode = true;
			log.debug("Git command executed {} errors?", errorCode ? "with" : "without");
		} catch (InterruptedException e) {
			log.error("Error while executing Git command ");
		}
		return process;
	}

	private List<String> buildGitCommand() {

		String cmd    = pb.getProperty("git.command", "git");
		String repo   = pb.getProperty("git.repository");
		String before = getBeforeClause();
		String since  = getSinceClause();

		List<String> gitcommand = new ArrayList<>();
		gitcommand.add("\"" + cmd + "\"");
		if (cmd != null && !StringUtils.isEmpty(cmd)) {
			gitcommand.add("-C");
			gitcommand.add(repo);
		}
		gitcommand.add("log");
		gitcommand.add("--date=iso");
		gitcommand.add("--reverse");
		gitcommand.add("--raw");
		gitcommand.add("--numstat");

		if (since != null && !StringUtils.isEmpty(since)) {
			gitcommand.add("--since");
			gitcommand.add("\"" + since + "\"");
		}
		if (before != null && !StringUtils.isEmpty(before)) {
			gitcommand.add("--before");
			gitcommand.add("\"" + before + "\"");
		}
		gitcommand.add("--pretty=format:Commit:%H%nAuthor:%aN%nDate:%ci");
		return gitcommand;
	}

	private String getSinceClause() {

		Optional<String> dbSince = getSinceFromDatabase();
		String afterdate = dbSince.orElse(pb.getProperty("git.afterDate", ""));

		String sinceClause = "";
		if (afterdate != null && !StringUtils.isEmpty(afterdate)) {
			sinceClause = afterdate;
		}
		return sinceClause;
	}

	private Optional<String> getSinceFromDatabase() {
		MetaInfosBuilder mib = new MetaInfosBuilder();

		LocalDateTime since = null;
		try {
			MetaInfos minf = mib.getLastEntryBylastGeneratedDate();
			since = minf.getLastCommitDate();
		} catch (NoResultException nre) {
			// there is no meta data entry yet. We must be the first humans
			// here...
		}
		if (since == null)
			return Optional.empty();
		return Optional.of(since.toString());
	}

	private String getBeforeClause() {
		String beforedate = pb.getProperty("git.beforeDate", "");
		String beforeClause = "";
		if (beforedate != null && !StringUtils.isEmpty(beforedate)) {
			beforeClause = beforedate;
		}
		return beforeClause;
	}

	private void buildStatistics(InputStream in, InputStream err) throws IOException {
		GitStatistics stats = new GitStatistics();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		BufferedReader errreader = new BufferedReader(new InputStreamReader(err));
		String line = null;
		String errline = null;
		while ((line = reader.readLine()) != null) {
			if (errreader.ready() && (errline = errreader.readLine()) != null) {
				log.error( errline);
			}
			stats.addLine(line);
		}
		createMetaDatabase();

		OutputBuilder ob = new OutputBuilder();
		ob.getFileCommitInfo();
		log.info("Data Base Successfully enterned");
	}

	private void createMetaDatabase() {
		MetaInfosBuilder mib = new MetaInfosBuilder();
		
		MetaInfos mi = new MetaInfos();
		mi.setGeneratedDate(mib.getCurrentTimeStamp());
		mi.setAnalysisPeriod(mib.getReportStartPeriod() + " to " + mib.getReportEndPeriod());
		Commits commit = mib.getLastCommitInfo();
		mi.setLastCommitDate(commit.getcommitTimestamp());
		mi.setLastCommitHash(commit.getId());
		mi.setTotalFilesCount(mib.getCommitFileCount());
		mi.setTotalDevelopersCount(mib.getDeveloperCount());
		mi.setTotalCommitsCount(mib.getCommitCount());
		dao.save(mi);
	}
}
