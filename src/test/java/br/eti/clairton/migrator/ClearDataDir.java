package br.eti.clairton.migrator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ClearDataDir implements TestRule {

	@Override
	public Statement apply(Statement base, Description description) {
		final File file = new File("target/migrator");
		System.err.println(new File(".").getAbsolutePath());
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
				throw new RuntimeException(e);
			}
		}
		return base;
	}

}
