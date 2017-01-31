package com.pass.prebug.hibernate.database;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.NaturalId;

@Entity
public class Commits extends BaseEntity<String> {

	@Id
	@NaturalId
	private String commitHash = null;

	private LocalDateTime commitTimestamp =null;
	
	@OneToMany(mappedBy = "commits", cascade = { CascadeType.ALL })
	private Set<FileCommitInfos> fileCommitInfos = new HashSet<FileCommitInfos>();

	// Constructor
	public Commits() {
	}

	public Commits(String commithash) {

		this.commitHash = commithash;
	}

	// Getter and Setter Methods

	@Override
	public String getId() {
		return commitHash;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public String setCommitHash(String commitHash) {
		return this.commitHash = commitHash;
	}
	
	public LocalDateTime getcommitTimestamp() {
		return commitTimestamp;
	}

	public void setcommitTimestamp(LocalDateTime commitTimestamp) {
		this.commitTimestamp = commitTimestamp;
	}

	public Set<FileCommitInfos> getFileCommitInfo() {
		return fileCommitInfos;
	}

	public void setFileCommitInfo(Set<FileCommitInfos> FileCommitInfos) {
		this.fileCommitInfos = FileCommitInfos;
	}

	public void addFileCommitInfo(FileCommitInfos info) {
		info.setCommits(this);
		fileCommitInfos.add(info);
		
	}


}
