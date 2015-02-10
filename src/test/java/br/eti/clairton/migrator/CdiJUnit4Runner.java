package br.eti.clairton.migrator;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;

public class CdiJUnit4Runner extends CdiTestRunner {
	static {
		System.setProperty(Config.DROP, "true");
		System.setProperty(Config.POPULATE, "true");
	}

	public CdiJUnit4Runner(final Class<?> klass)
			throws org.junit.runners.model.InitializationError {
		super(klass);
	}
}