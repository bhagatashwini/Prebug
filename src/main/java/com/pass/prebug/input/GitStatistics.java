package com.pass.prebug.input;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pass.prebug.hibernate.dao.AuthorsDao;
import com.pass.prebug.hibernate.dao.CommitFilesDao;
import com.pass.prebug.hibernate.dao.CommitsDao;
import com.pass.prebug.hibernate.dao.GenericDaoJpa;
import com.pass.prebug.hibernate.database.Authors;
import com.pass.prebug.hibernate.database.BaseEntity;
import com.pass.prebug.hibernate.database.CommitFiles;
import com.pass.prebug.hibernate.database.Commits;
import com.pass.prebug.hibernate.database.FileCommitInfos;

public class GitStatistics {
	private static final Logger log = LoggerFactory.getLogger(GitStatistics.class);
	private static GenericDaoJpa<BaseEntity<?>> dao = new GenericDaoJpa<>();
	private static CommitsDao commitdao = new CommitsDao();
	private static CommitFilesDao commitfiledao = new CommitFilesDao();
	private static AuthorsDao authordao = new AuthorsDao();
	private HashMap<String, String> fileMode;

	private CommitFiles commitFiles;
	private Authors author;
	private Commits commit;
	private FileCommitInfos info;

	public void addLine(String line) {
		log.trace("Read line from log: {}", line);
		
		AppProperties pb = AppProperties.getInstance();
		String filetype = pb.getProperty("file.type");

		if (!line.isEmpty()) {
			if (line.startsWith("Commit:")) {
				commit = readCommit(line);
				fileMode = new HashMap<String, String>();
			} else if (line.startsWith("Author:")) {
				author = readAuthor(line);
			} else if (line.startsWith("Date:")) {
				commit.setcommitTimestamp(readDate(line));
			} else if (line.startsWith(":")) {
				readFileMode(line, filetype);
			} else if (containsFileType(line, filetype)) {
				String[] splits = line.toString().split("\t");
				commitFiles = readFile(splits[2]);
				int plusline = getPlusLinesCount(splits);
				int negativeline = getNegativeLinesCount(splits);
				commitFiles.addTotalLines(countTotalLines(plusline, negativeline));

				info = createInfo(plusline, negativeline);
				info.setAuthors(author);
				if (fileMode.containsKey(commitFiles.getFile())) {
					info.setFileMode(fileMode.get(commitFiles.getFile()));
				}
				commit.addFileCommitInfo(info);
				commitFiles.addCommitFileInfo(info);

				// Check for deleted file from repository
				// if(info.getDeletedLines() >= commitFiles.getTotalLines()){

				// if(commitFiles.getTotalLines() <= 0){
				// commitfiledao.deleteFile(commitFiles);
				// } else {

				saveIfNew(author, commit, commitFiles);
				dao.save(info);

			}
		}

	}

	private void readFileMode(String line, String filetype) {

		if (containsFileType(line, filetype)) {
			String[] spiltted = line.split(" ");
			String line2 = spiltted[(spiltted.length) - 1];
			String[] spiltted2 = line2.split("\t");
			fileMode.put(spiltted2[1], spiltted2[0]);
		}

	}

	private int getNegativeLinesCount(String[] splits) {
		int negativeLine = 0;
		String negativeLineStr = null;
		negativeLineStr = splits[1];
		if (negativeLineStr.equals("-")) {
			negativeLine = 0;
		} else {
			negativeLine = Integer.parseInt(negativeLineStr);
		}
		return negativeLine;
	}

	private int getPlusLinesCount(String[] splits) {
		int plusLine = 0;
		String plusLineStr = splits[0];
		if (plusLineStr.equals("-")) {
			plusLine = 0;
		} else {
			plusLine = Integer.parseInt(plusLineStr);
		}
		return plusLine;
	}

	private Commits readCommit(String line) {
		int pos = line.indexOf(":");
		int len = line.length();
		String commitHash = line.substring(pos + 1, len);
		log.debug("CommitID :{}",commitHash);
		return commitdao.findByHash(commitHash).orElse(new Commits(commitHash));
	}

	private Authors readAuthor(String line) {
		int pos = line.indexOf(":");
		int len = line.length();
		String author = line.substring(pos + 1, len);
		log.debug("AuthorID: {}" , author);
		return authordao.findByAuthor(author).orElse(new Authors(author));
	}

	private LocalDateTime readDate(String line) {
		int pos = 4;
		int len = line.length();
		String date = line.substring(pos + 1, len);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldt = LocalDateTime.parse(date.substring(0, 20).trim(), formatter);
		log.debug("Date: {}" , ldt);
		return ldt;
	}

	private CommitFiles readFile(String fileName) {
		String oldPath = fileName;
		String newPath = fileName;
		// Check whether file got Renamed
		if (fileName.contains("=>")) {
			String[] paths = updateFilePath(fileName);
			oldPath = paths[0];
			newPath = paths[1];
		}
		CommitFiles file = commitfiledao.findByFile(oldPath).orElse(new CommitFiles(newPath));

		if (!oldPath.equals(newPath)) {
			if (!commitfiledao.findByFile(newPath).isPresent()) {
				file.setFile(newPath);
			} else {
				// TODO: do we have to merge these two files and sum up the
				// churn?
				file = commitfiledao.findByFile(newPath).orElse(new CommitFiles(newPath));

			}
		}
		return file;

	}
	
	/**
	 * Calculate the line counts
	 * l_+ : added lines +
	 * l_- : deleted lines -
	 * l_+ - l_- = n
	 * if n >= 0 --> n: added lines
	 * --> l_-: modified lines
	 * if n < 0 |n|: deleted lines
	 * l_+: modified lines
	 */
	private class Lines {
		int addedLines = 0, deletedLines = 0, modifiedLines = 0;
		
		public Lines(int pluslines, int negativelines){
			int n = pluslines - negativelines;

			if (n >= 0) {
				addedLines = n;
				deletedLines = 0;
				modifiedLines = negativelines;
			} else {
				addedLines = 0;
				deletedLines = n * (-1);
				modifiedLines = pluslines;
			}
		}
		
		public int getAddedLines(){
			return addedLines;
		}
		public int getDeletedLines(){
			return deletedLines;
		}
		public int getModifiedLines(){
			return modifiedLines;
		}
		public int getTotalLines(){
			return addedLines - deletedLines;
		}
	}

	private int countTotalLines(int pluslines, int negativelines) {
		
		Lines lines = new Lines(pluslines, negativelines);
		return lines.getTotalLines();
	}

	private FileCommitInfos createInfo(int plusLines, int negativeLines) {

		Lines lines = new Lines(plusLines, negativeLines);

		FileCommitInfos info = new FileCommitInfos();

		info.setAddedLines(lines.getAddedLines());
		info.setModifiedLines(lines.getModifiedLines());
		info.setDeletedLines(lines.getDeletedLines());
		return info;
	}

	private boolean containsFileType(String line, String fileType) {

		String[] fileTypes = fileType.split(",");
		boolean containsFileType = false;
		for (String ft : fileTypes) {

			if (line.endsWith(ft) || line.endsWith(ft + "}")) {
				log.debug("FileType: {}" , ft);
				containsFileType = true;
			}
		}

		return containsFileType;
	}

	public String[] updateFilePath(String fileName) {

		// 0 0 src/{ => main/java}/junit/extensions/ActiveTestSuite.java
		// old 0 0 src/junit/extensions/ActiveTestSuite.java
		// new 0 0 src/main/java/junit/extensions/ActiveTestSuite.java

		// 0 0 {org => src/org}/junit/tests/package-info.java
		// old 0 0 org/junit/tests/package-info.java
		// new 0 0 src/org/junit/tests/package-info.java

		// 9 4 src/main/java/org/junit/{internal => }/runners/ParentRunner.java
		// old 9 4 src/main/java/org/junit/internal/runners/ParentRunner.java
		// new 9 4 src/main/java/org/junit/runners/ParentRunner.java

		// : 2 1 org/junit/{runners => tests}/Enclosed.java
		// old: 2 1 org/junit/{runners => tests}/Enclosed.java
		// Line 3866: 2 1 org/junit/{runners => tests}/Enclosed.java

		String oldPath, newPath, searchPath = null, replacePath = null, firstPart, lastPart = null;

		int firstBracePos = fileName.indexOf("{");
		int lastBracePos = fileName.indexOf("}");
		int equalPos = fileName.indexOf("=");
		int greaterPos = fileName.indexOf(">");
		int len = fileName.length();

		if (firstBracePos == -1) {
			oldPath = fileName.substring(0, equalPos - 1);	
			newPath = fileName.substring(greaterPos + 1, len);
			searchPath = oldPath.trim();
			replacePath = newPath.trim();
		} else {

			oldPath = fileName.substring(firstBracePos + 1, equalPos - 1);
			newPath = fileName.substring(greaterPos + 1, lastBracePos);
			firstPart = fileName.substring(0, firstBracePos);
			// fix when lastpart is empty
			if (!((lastBracePos + 1) == len)) {
				lastPart = fileName.substring(lastBracePos + 2, len);
			} else {
				lastPart = fileName.substring(lastBracePos + 1, len);
			}

			if (!firstPart.trim().isEmpty()) {
				if (!oldPath.trim().isEmpty()) {
					if (!newPath.trim().isEmpty()) {
						// All present
						searchPath = firstPart + oldPath + prepareLastPart(lastPart);
						replacePath = firstPart + newPath.trim() + prepareLastPart(lastPart);
					} else {
						// newpath missing
						searchPath = firstPart + oldPath + prepareLastPart(lastPart);
						replacePath = firstPart + lastPart;
					}
				} else {
					// oldpath missing
					searchPath = firstPart + lastPart;
					replacePath = firstPart + newPath.trim() + prepareLastPart(lastPart);
				}

			} else {
				// first part missing
				searchPath = oldPath + prepareLastPart(lastPart);
				replacePath = newPath.trim() + prepareLastPart(lastPart);
			}
		}
		
		log.debug("Seachpath: {},replacePath:{}", searchPath,replacePath);
		String[] paths = { searchPath.trim(), replacePath.trim() };
		return paths;

	}

	private String prepareLastPart(String lastPart) {
		return lastPart.length() > 0 ? "/" + lastPart : "";
	}

	private void saveIfNew(BaseEntity<?>... entities) {
		for (BaseEntity<?> entity : entities) {
			if (dao.isNew(entity)) {
				dao.save(entity);
			}
		}
	}
}
