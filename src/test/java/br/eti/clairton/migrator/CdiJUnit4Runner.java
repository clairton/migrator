package br.eti.clairton.migrator;

import java.io.File;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class CdiJUnit4Runner extends BlockJUnit4ClassRunner {

	private final Class<?> klass;
	private final Weld weld;
	private final WeldContainer container;

	public CdiJUnit4Runner(final Class<?> klass)
			throws org.junit.runners.model.InitializationError {
		super(klass);
		this.klass = klass;
		this.weld = new Weld();
		this.container = weld.initialize();
		clearDatabasePath();
	}

	@Override
	protected Object createTest() throws Exception {
		final Object test = container.instance().select(klass).get();
		return test;
	}

	private void clearDatabasePath() throws InitializationError {
		final File file = new File("target/migrator");
		if (file.exists()) {
			clearDatabasePath(file);
		}
	}

	private void clearDatabasePath(final File file) {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				clearDatabasePath(f);
			}
		} else {
			file.delete();
		}
	}
}