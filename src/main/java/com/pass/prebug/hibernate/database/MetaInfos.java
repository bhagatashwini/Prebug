package com.pass.prebug.hibernate.database;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class MetaInfos extends BaseEntity<Long> {
	@Id
	@GeneratedValue
	private Long id;
	private LocalDateTime generatedDate;
	private LocalDateTime lastCommitDate;
	private String analysisPeriod;
	private String lastCommitHash;
	private Long totalFilesCount;
	private Long totalCommitsCount;
	private Long totalDevelopersCount;

	@Override
	public Long getId() {
		return id;
	}

	public Long setId(Long id) {
		return this.id = id;
	}

	public LocalDateTime getGeneratedDate() {
		return generatedDate;
	}

	public void setGeneratedDate(LocalDateTime generatedDate) {
		this.generatedDate = generatedDate;
	}

	public String getAnalysisPeriod() {
		return analysisPeriod;
	}

	public void setAnalysisPeriod(String analysisPeriod) {
		this.analysisPeriod = analysisPeriod;
	}

	public String getLastCommitHash() {
		return lastCommitHash;
	}

	public void setLastCommitHash(String lastCommitHash) {
		this.lastCommitHash = lastCommitHash;
	}

	public LocalDateTime getLastCommitDate() {
		return lastCommitDate;
	}

	public void setLastCommitDate(LocalDateTime lastCommitDate) {
		this.lastCommitDate = lastCommitDate;
	}

	public Long getTotalFilesCount() {
		return totalFilesCount;
	}

	public void setTotalFilesCount(Long totalFilesCount) {
		this.totalFilesCount = totalFilesCount;
	}

	public Long getTotalCommitsCount() {
		return totalCommitsCount;
	}

	public void setTotalCommitsCount(Long totalCommitsCount) {
		this.totalCommitsCount = totalCommitsCount;
	}

	public Long getTotalDevelopersCount() {
		return totalDevelopersCount;
	}

	public void setTotalDevelopersCount(Long totalDevelopersCount) {
		this.totalDevelopersCount = totalDevelopersCount;
	}

}