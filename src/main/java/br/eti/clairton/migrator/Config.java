package br.eti.clairton.migrator;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.valueOf;
import static java.lang.System.getProperty;

import javax.enterprise.inject.Vetoed;

@Vetoed
public class Config {
	public static final String POPULATE = "br.eti.clairton.migrator.populate";
	public static final String DROP = "br.eti.clairton.migrator.drop";
	private final String dataSetPath;
	private final String schema;
	private final String changelogPath;

	public Config(final String dataSetPath) {
		this(dataSetPath, "db/changelogs/changelog-main.xml");
	}

	public Config(final String dataSetPath, final String changelogPath) {
		this(dataSetPath, changelogPath, "public");
	}
	
	public Config(final String dataSetPath, final String changelogPath, final String schema) {
		super();
		this.dataSetPath = dataSetPath;
		this.changelogPath = changelogPath;
		this.schema = schema;
	}

	public Boolean isDrop() {
		final String property = getProperty(DROP);
		if (property == null) {
			return FALSE;
		} else {
			return valueOf(property);
		}
	}

	public Boolean isPopulate() {
		final String property = getProperty(POPULATE);
		if (property == null) {
			return FALSE;
		} else {
			return valueOf(property);
		}
	}

	public String getDataSetPath() {
		return dataSetPath;
	}

	public String getChangelogPath() {
		return changelogPath;
	}
	
	public String getSchema() {
		return schema;
	}
}
