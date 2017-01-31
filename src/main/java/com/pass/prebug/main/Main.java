package com.pass.prebug.main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pass.prebug.input.AppProperties;
import com.pass.prebug.input.GitLogBuilder;
import com.pass.prebug.input.LocalProperties;

import javassist.NotFoundException;

public class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, NotFoundException, SQLException {

		log.info("Starting the analysis....");

		AppProperties.setInstance(new LocalProperties());
		GitLogBuilder glb = new GitLogBuilder();
		glb.runGitLogCommand();
	

	}

}
