package com.pass.prebug.hibernate.database;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pass.prebug.input.AppProperties;;

@Entity
public class CommitFiles extends BaseEntity<Long> {

	private static final Logger log = LoggerFactory.getLogger(CommitFiles.class);

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String file = null;

	private int totalLines;

	@OneToMany(mappedBy = "commitFiles")
	private Set<FileCommitInfos> fileCommitInfos = new HashSet<FileCommitInfos>();

	private static AppProperties pb;// = new LocalProperties();

	// Constructor
	public CommitFiles() {
	}

	public CommitFiles(String file) {
		this.file = file;
	}

	// Getter and Setter Method
	@Override
	public Long getId() {
		return id;
	}

	public Long setId(Long id) {
		return this.id = id;
	}

	public String getFile() {
		return file;
	}

	public String setFile(String file) {
		return this.file = file;
	}

	public Set<FileCommitInfos> getFileCommitInfo() {
		return fileCommitInfos;
	}

	public void setFileCommitInfo(Set<FileCommitInfos> FileCommitInfos) {
		this.fileCommitInfos = FileCommitInfos;
	}

	public String toString() {
		return "CommitFiles: " + this.id + ", " + this.file;

	}

	public long getAddedLineCount() {
		// Set<FileCommitInfos> commitsFileInfo = getFileCommitInfo();
		// long total = 0;
		// for (FileCommitInfos commitFile : commitsFileInfo) {
		// if (commitFile.getFileMode() == "M") {
		// total =+ commitFile.getAddedLines();
		// } else {
		// total =+ 0;
		// }
		// }

		// return total;
		// Fix for "M" file mode
		return getFileCommitInfo().stream().filter(f -> "M".equals(f.getFileMode())).mapToLong(f -> f.getAddedLines())
				.sum();

	}

	public long getModifiedLineCount() {

		return getFileCommitInfo().stream().mapToLong(f -> f.getModifiedLines()).sum();

	}

	public long getDeletedLineCount() {

		return getFileCommitInfo().stream().mapToLong(f -> f.getDeletedLines()).sum();

	}

	public long getCommitCount() {

		return getFileCommitInfo().stream().map(f -> f.getCommits()).count();

	}

	public long getAge() {
		ArrayList<LocalDateTime> tmp = new ArrayList<LocalDateTime>();
		Set<FileCommitInfos> commitsFile = getFileCommitInfo();
		for (FileCommitInfos commitFile : commitsFile) {
			tmp.add(commitFile.getCommits().getcommitTimestamp());
		}

		LocalDateTime prevDate, resultDate, nextDate = null;
		long days = 0;
		Collections.sort(tmp, new Comparator<LocalDateTime>() {
			@Override
			public int compare(LocalDateTime o1, LocalDateTime o2) {
				// TODO Auto-generated method stub
				return o1.toString().compareTo(o2.toString());
			}
		});

		if (tmp.isEmpty()) {
			days = 0;

		} else {
			for (int i = 0; i < tmp.size() - 1; ++i) {

				if (i == tmp.size() - 1) {
					prevDate = tmp.get(0);
					nextDate = tmp.get(i);
					resultDate = LocalDateTime.from(prevDate);
					days += resultDate.until(nextDate, ChronoUnit.DAYS);
				} else {
					prevDate = tmp.get(i);
					nextDate = tmp.get(i + 1);
					resultDate = LocalDateTime.from(prevDate);
					days += resultDate.until(nextDate, ChronoUnit.DAYS);
				}

			}

		}

		return days;
	}

	public long getNumberOfDistinctAuthors() {
		return getFileCommitInfo().stream().map(cf -> cf.getAuthors()).distinct().count();
	}

	public long getChurnedLinesCount() {
		return getAddedLineCount() + getModifiedLineCount();
	}

	public double getFutureRevisionCountLogistic() {

		pb = AppProperties.getInstance();
		String beta0 = pb.getProperty("beta.Constant", "-0.894407253648894");
		String beta1 = pb.getProperty("beta.REVC", "-0.00294593430141643");
		String beta2 = pb.getProperty("beta.CLOC/TL", "-0.117149143806947");
		String beta3 = pb.getProperty("beta.DEVC", "-0.000746264833793162");
		String beta4 = pb.getProperty("beta.WORK/AgeWeek", "-0.000746264833793162");
		String beta5 = pb.getProperty("beta.NEW", "-0.000746264833793162");
		String beta6 = pb.getProperty("beta.TL", "-0.000746264833793162");

		double ß0 = Double.parseDouble(beta0);
		double ß1 = Double.parseDouble(beta1);
		double ß2 = Double.parseDouble(beta2);
		double ß3 = Double.parseDouble(beta3);
		double ß4 = Double.parseDouble(beta4);
		double ß5 = Double.parseDouble(beta5);
		double ß6 = Double.parseDouble(beta6);
		long totalLines = getTotalLines();
		long churnedLines = getChurnedLinesCount();
		long ageWeek = getAge() / 7;
		long work = getAddedLineCount() + getDeletedLineCount() + getModifiedLineCount();
		long newChange = 0;
		long workAge = 0;
		long churnedTolalLines = 0;
		// -0.582016275529234 * :Name( "REVC/TL" ) + -1.33370846747174 * :Name(
		// "CLOC/TL" )
		// + -0.0965241531397999 * :DEVC + -0.0449926811172696 * :Name(
		// "Work/Ageweek" ) +
		// -0.00270746405216662 * :NEW + -0.00199715459736915 * :TL
		// double linear=(ß1*getCommitCount())+(ß2 *
		// getNumberOfDistinctAuthors())+(ß3*getChurnedLinesCount());
		if (getDeletedLineCount() == 0) {
			newChange = 0;
		} else {
			newChange = (churnedLines / getDeletedLineCount());
		}

		if (ageWeek == 0) {
			workAge = 0;
		} else {
			workAge = work / ageWeek;
		}

		if (totalLines == 0) {

			churnedTolalLines = 0;
		} else {

			churnedTolalLines = churnedLines / totalLines;
		}
		double linear = (ß1 * (getCommitCount())) + (ß2 * (churnedTolalLines)) + (ß3 * getNumberOfDistinctAuthors())
				+ (ß4 * workAge) + (ß5 * newChange) + (ß6 * totalLines);
		double cum0 = 1 / (1 + Math.exp((ß0 - linear)));
		double prob1 = (1 - cum0);
		double roundOff = Math.round(prob1 * 100.0) / 100.0;
		Object[] params = { linear, cum0, prob1, roundOff };
		log.debug("Linear: {}, cum0: {}, Prob1: {}, roundOff: {}", params);

		return roundOff;

	}

	public double getFutureRevisionCount() {
		pb = AppProperties.getInstance();
		String beta0 = pb.getProperty("beta.Constant", "-0.894407253648894");
		String beta1 = pb.getProperty("beta.REVC", "-0.00294593430141643");
		String beta2 = pb.getProperty("beta.DEVC", "-0.117149143806947");
		String beta3 = pb.getProperty("beta.CLOC", "-0.000746264833793162");
		double ß0 = Double.parseDouble(beta0);
		double ß1 = Double.parseDouble(beta1);
		double ß2 = Double.parseDouble(beta2);
		double ß3 = Double.parseDouble(beta3);
		double linear = (ß1 * getCommitCount()) + (ß2 * getNumberOfDistinctAuthors()) + (ß3 * getChurnedLinesCount());
		double cum0 = 1 / (1 + Math.exp((ß0 - linear)));
		double prob1 = (1 - cum0);
		double roundOff = Math.round(prob1 * 100.0) / 100.0;
		Object[] params = { linear, cum0, prob1, roundOff };
		log.debug("Linear: {}, cum0: {}, Prob1: {}, roundOff: {}", params);
		return roundOff;

	}

	public long getFutureRevisionCountLinear() {

		pb = AppProperties.getInstance();
		String beta0 = pb.getProperty("beta.Constant", "-0.894407253648894");
		String beta1 = pb.getProperty("beta.REVC", "-0.00294593430141643");
		String beta2 = pb.getProperty("beta.DEVC", "-0.117149143806947");
		String beta3 = pb.getProperty("beta.CLOC", "-0.000746264833793162");
		double ß0 = Double.parseDouble(beta0);
		double ß1 = Double.parseDouble(beta1);
		double ß2 = Double.parseDouble(beta2);
		double ß3 = Double.parseDouble(beta3);
		long linear = (long) (ß0 + (ß1 * getCommitCount()) + (ß2 * getNumberOfDistinctAuthors())
				+ (ß3 * getChurnedLinesCount()));
		return linear;

	}

	public long getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}

	public void addCommitFileInfo(FileCommitInfos info) {
		info.setCommitFile(this);
		fileCommitInfos.add(info);
	}

	public void addTotalLines(int addedLines) {
		totalLines += addedLines;

	}
}
