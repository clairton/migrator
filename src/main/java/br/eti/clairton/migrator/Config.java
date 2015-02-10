package br.eti.clairton.migrator;

import static java.lang.System.getProperty;

import javax.enterprise.inject.Vetoed;

@Vetoed
public class Config {
	public static final String POPULATE = "br.eti.clairton.migrator.populate";
	public static final String DROP = "br.eti.clairton.migrator.drop";
	private final String dataSetPath;
	private final String changelogPath;

	public Config(final String dataSetPath) {
		this(dataSetPath, "db/changelogs/changelog-main.xml");
	}

	public Config(String dataSetPath, String changelogPath) {
		super();
		this.dataSetPath = dataSetPath;
		this.changelogPath = changelogPath;
	}

	public Boolean isDrop() {
		final String property = getProperty(DROP);
		if (property == null) {
			return Boolean.FALSE;
		} else {
			return Boolean.valueOf(property);
		}
	}

	public Boolean isPopulate() {
		final String property = getProperty(POPULATE);
		if (property == null) {
			return Boolean.FALSE;
		} else {
			return Boolean.valueOf(property);
		}
	}

	public String getDataSetPath() {
		return dataSetPath;
	}

	public String getChangelogPath() {
		return changelogPath;
	}
}
