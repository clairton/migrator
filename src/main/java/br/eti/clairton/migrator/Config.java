package br.eti.clairton.migrator;

import javax.enterprise.inject.Vetoed;

@Vetoed
public class Config {
	private final Boolean dropAll;
	private final Boolean insert;
	private final String dataSetPath;
	private final String changelogPath;

	public Config(final Boolean dropAll, final Boolean insert,
			final String dataSetPath) {
		this(dropAll, insert, dataSetPath, "db/changelogs/changelog-main.xml");
	}

	public Config(Boolean dropAll, Boolean insert, String dataSetPath,
			String changelogPath) {
		super();
		this.dropAll = dropAll;
		this.insert = insert;
		this.dataSetPath = dataSetPath;
		this.changelogPath = changelogPath;
	}

	public Boolean isDropAll() {
		return dropAll;
	}

	public Boolean isInsert() {
		return insert;
	}

	public String getDataSetPath() {
		return dataSetPath;
	}

	public String getChangelogPath() {
		return changelogPath;
	}
}
