package br.eti.clairton.migrator;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.runners.model.InitializationError;

public class CdiJUnit4Runner extends CdiTestRunner {
	static {
		System.setProperty(Config.DROP, "true");
		System.setProperty(Config.POPULATE, "true");
        System.setProperty(Config.MIGRATE, "true");
	}

	public CdiJUnit4Runner(final Class<?> klass) throws InitializationError {
		super(klass);
	}
}