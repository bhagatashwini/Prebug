package com.pass.prebug.main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.pass.prebug.input.AppProperties;
import com.pass.prebug.input.GitLogBuilder;

import javassist.NotFoundException;

@Mojo(name="Prebugs",aggregator=true)
public class PrebugsMojo extends AbstractMojo {

	@Parameter(name="paramMap")
	Map<String, String> paramMap;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		AppProperties.setInstance(new MavenProperties(paramMap));
		GitLogBuilder glb = new GitLogBuilder();
		try {
			glb.runGitLogCommand();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | IOException | NotFoundException ex) {
			
			
			throw new MojoExecutionException("Exception executing the prebugs plugin.", ex);
		}		
	}

}
