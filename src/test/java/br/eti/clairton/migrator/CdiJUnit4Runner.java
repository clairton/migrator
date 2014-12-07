package br.eti.clairton.migrator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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
			try {
				Files.walkFileTree(file.toPath(),
						new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(Path file,
									BasicFileAttributes attrs)
									throws IOException {
								Files.delete(file);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult postVisitDirectory(Path dir,
									IOException exc) throws IOException {
								Files.delete(dir);
								return FileVisitResult.CONTINUE;
							}
						});
			} catch (final IOException e) {
				throw new InitializationError(e);
			}
		}
	}
}