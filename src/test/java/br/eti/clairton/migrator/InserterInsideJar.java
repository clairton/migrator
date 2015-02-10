package br.eti.clairton.migrator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.Test;

public class InserterInsideJar {
	static {
		System.setProperty(Config.DROP, "true");
		System.setProperty(Config.POPULATE, "true");
	}

	@Test
	public void testInsideJar() throws SQLException {
		final String url = "jdbc:hsqldb:file:target/database/migrator2;hsqldb.lock_file=false;shutdown=true;create=true";
		final Connection connection = DriverManager
				.getConnection(url, "sa", "");
		connection.setAutoCommit(true);
		final Config config = new Config("datasets");
		final ClassLoader classLoader = new ClassLoader(getClass()
				.getClassLoader()) {
			@Override
			public Enumeration<URL> getResources(String name)
					throws IOException {
				// jar:file:/home/maxicreditosc/.m2/repository/br/com/maxicredito/auth-api/1.0.0-SNAPSHOT/auth-api-1.0.0-SNAPSHOT.jar!/datasets/
				final File jar = new File("src/test/resources/test.jar");
				final String file = "jar:file:" + jar.getAbsolutePath()
						+ "!/datasets/";
				final URL url = new URL(file);
				final Collection<URL> c = Arrays.asList(url);
				return Collections.enumeration(c);
			}
		};
		final Migrator migrator = new MigratorDefault(connection, config,
				classLoader);
		migrator.run();
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes Where nome='Jar'";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(1, resultSet.getInt(1));
	}
}
