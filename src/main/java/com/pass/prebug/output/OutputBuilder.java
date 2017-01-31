package com.pass.prebug.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pass.prebug.hibernate.dao.GenericDaoJpa;
import com.pass.prebug.hibernate.database.CommitFiles;
import com.pass.prebug.hibernate.database.MetaInfos;
import com.pass.prebug.input.AppProperties;

public class OutputBuilder {
	private static final Logger log = LoggerFactory.getLogger(OutputBuilder.class);
	@SuppressWarnings("rawtypes")
	private static GenericDaoJpa dao = new GenericDaoJpa();;
	private static AppProperties pb ;
	private static MetaInfosBuilder mib = new MetaInfosBuilder();

	public void getFileCommitInfo() throws IOException  {
        pb = AppProperties.getInstance();
		ArrayList<Map<String, Object>> statlist = getStatList();
		List<HashMap<String, Object>> list = getFileList();
		generateHtmlOutput(statlist, list);

	}

	private ArrayList<Map<String, Object>> getStatList() {
		ArrayList<Map<String, Object>> statlist = new ArrayList<>();
		Map<String, Object> statmap = new HashMap<String, Object>();
		MetaInfos minf = mib.getLastEntryBylastGeneratedDate();
		statmap.put("Generated", minf.getGeneratedDate());
		statmap.put("ReportPeriod", minf.getAnalysisPeriod());
		statmap.put("TotalFiles", minf.getTotalFilesCount());
		statmap.put("TotalAuthors", minf.getTotalDevelopersCount());
		statmap.put("TotalCommits", minf.getTotalCommitsCount());
		statlist.add(statmap);
		return statlist;
	}

	@SuppressWarnings("unchecked")
	private List<HashMap<String, Object>> getFileList() throws IOException {
		List<CommitFiles> filelist = null;
		filelist = dao.findByWhereClause(CommitFiles.class, "totalLines");

		List<HashMap<String, Object>> list = new ArrayList<>();
		File f = new File("D:\\PrebugOutput\\EsfbundNewOutputResult.txt"); //TODO remove this block
		if (f.exists()) {
			f.delete();
		}
		FileWriter fw = new FileWriter(f);
		for (CommitFiles file : filelist) {
          
			/* organize our data */
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("FileName", file.getFile());
			map.put("FuturRevisionCount", file.getFutureRevisionCount());
			map.put("RevisionCount", file.getCommitCount());
			map.put("DeveloperCount", file.getNumberOfDistinctAuthors());
			map.put("ChurnedLineCount", (file.getChurnedLinesCount()));
			map.put("TotalLines", file.getTotalLines());
			list.add((HashMap<String, Object>) map);

			fw.append(file.getFile() + "\t" + file.getCommitCount() + "\t" + file.getNumberOfDistinctAuthors() + "\t"
					+ file.getAddedLineCount() + "\t" + file.getDeletedLineCount() + "\t" + file.getModifiedLineCount()
					+ "\t" + file.getTotalLines() + "\t" + file.getAge() + "\t" + file.getFutureRevisionCount() + "\n");
		}
		fw.close();
		/* Sorting in descending order */
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> map1, Map<String, Object> map2) {
				double val1 = 0 ;
				double val2=0;
				for (Map.Entry<String, Object> entry : map1.entrySet()) {
					if (entry.getKey() == "FuturRevisionCount")
						val1 = (double) entry.getValue();
				}
				for (Map.Entry<String, Object> entry : map2.entrySet()) {
					if (entry.getKey() == "FuturRevisionCount")
						val2 = (double) entry.getValue();
				}

				//return Integer.valueOf(val2.toString()).compareTo(Integer.valueOf(val1.toString()));
				
			return Double.compare(val2, val1);
			
			}
		});
		
		
//		Collections.sort(list, new Comparator<Map<String, Object>>() {
//			@Override
//			public int compare(Map<String, Object> map1, Map<String, Object> map2) {
//				Object val1 = 0 ;
//				Object val2=0;
//				for (Map.Entry<String, Object> entry : map1.entrySet()) {
//					if (entry.getKey() == "FuturRevisionCount")
//						val1 =  entry.getValue();
//				}
//				for (Map.Entry<String, Object> entry : map2.entrySet()) {
//					if (entry.getKey() == "FuturRevisionCount")
//						val2 =  entry.getValue();
//				}
//
//				return Integer.valueOf(val2.toString()).compareTo(Integer.valueOf(val1.toString()));
//				
//			
//			
//			}
//		});
	return list;
	}

	private void generateHtmlOutput(ArrayList<Map<String, Object>> statlist, List<HashMap<String, Object>> list) throws IOException {

		Properties velocityProperties = new Properties();
		velocityProperties.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		velocityProperties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		/* first, get and initialize an engine */
		VelocityEngine ve = new VelocityEngine();
		ve.init(velocityProperties);

		/* add that list to a VelocityContext */
		VelocityContext context = new VelocityContext();
		context.put("statlist", statlist);
		context.put("list", list);

		copyStaticFiles();
		File resultFile = createOutputFile();

		try {
			Template template = ve.getTemplate("html-style/Test.html");
			FileWriter fileWriter = new FileWriter(resultFile);
			template.merge(context, fileWriter);
			fileWriter.close();
		} catch (Exception e) {
		    log.error("Problem with Velocity Template" , e);
		}
	}

	/**
	 * Create the Outputfile and delete an existing first.
	 */
	private File createOutputFile() {
		String targetDir = pb.getProperty("output.directory");
		// Create Index.html at output Directory
		File resultFile = new File(targetDir + "/Prebug.html");
		if (resultFile.exists()) {
			resultFile.delete();
		}
		return resultFile;
	}

	private void copyStaticFiles() throws IOException {
		String targetDir = pb.getProperty("output.directory");
		File targetStyleFile = new File(targetDir + "/styles.css");
		InputStream is = OutputBuilder.class.getResourceAsStream("/html-style/styles.css");
		
		if (!targetStyleFile.exists()) {
			Files.copy(is, targetStyleFile.toPath());
		}
	}

}