package com.pass.prebug.hibernate.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class FileCommitInfos extends BaseEntity<Long> {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private CommitFiles commitFiles = new CommitFiles();

	@ManyToOne
	private Commits commits = new Commits();

	@ManyToOne
	private Authors authors = new Authors();
		
	private int addedLines;
	private int deletedLines;
	private int modifiedLines;
	private String fileMode;

	// Getter and Setter Method

	public Long getId() {
		return id;
	}

	public Long setId(Long id) {
		return this.id = id;
	}

	public CommitFiles getCommitFile() {
		return commitFiles;
	}

	public void setCommitFile(CommitFiles commitFiles) {
		this.commitFiles = commitFiles;
	}
	
	public Commits getCommits() {
		return commits;
	}

	public void setCommits(Commits commits) {
		this.commits = commits;
	}

	public Authors getAuthors() {
		return authors;
	}

	public void setAuthors(Authors authors) {
		this.authors = authors;
	}

	public long getAddedLines() {
		return addedLines;
	}

	public void setAddedLines(int addedLines) {
		this.addedLines = addedLines;
	}

	public long getDeletedLines() {
		return deletedLines;
	}

	public void setDeletedLines(int deletedLines) {
		this.deletedLines = deletedLines;
	}

	public long getModifiedLines() {
		return modifiedLines;
	}

	public void setModifiedLines(int modifiedLines) {
		this.modifiedLines = modifiedLines;
	}

	public String getFileMode() {
		return fileMode;
	}

	public void setFileMode(String fileMode) {
		this.fileMode = fileMode;
	}
	

}
