package com.pass.prebug.hibernate.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Authors extends BaseEntity<Long> {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String author = null;

	// Constructor
	public Authors() {
	}

	public Authors(String author) {
		this.author = author;
	}

	// Getter and Setter Method
	@Override
	public Long getId() {
		return id;
	}

	public Long setId(Long id) {
		return this.id = id;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	


}