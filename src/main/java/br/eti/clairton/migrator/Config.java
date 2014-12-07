package br.eti.clairton.migrator;

import javax.enterprise.inject.Vetoed;

@Vetoed
public class Config {
	private final Boolean dropAll;
	private final Boolean insert;
	private final String dataSetPath;

	public Config(final Boolean dropAll, final Boolean insert,
			final String dataSetPath) {
		super();
		this.dropAll = dropAll;
		this.insert = insert;
		this.dataSetPath = dataSetPath;
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
}
