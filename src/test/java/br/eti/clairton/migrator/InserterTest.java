package br.eti.clairton.migrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarInputStream;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;

public class InserterTest {
	static {
		System.setProperty(Config.DROP, "true");
		System.setProperty(Config.POPULATE, "true");
	}

	private Connection connection;

	@Before
	public void setUp() throws Exception {
		final String url = "jdbc:hsqldb:file:target/database/migrator2;hsqldb.lock_file=false;shutdown=true;create=true";
		connection = DriverManager.getConnection(url, "sa", "");
		connection.setAutoCommit(true);
		try {
			connection.createStatement().execute("DELETE FROM aplicacoes");
		} catch (Exception e) {

		}
	}

	@Test
	public void testInsideJar() throws Exception {
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
		run(classLoader);
	}

	@Test
	public void testVfs() throws Exception {
		final ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
			@Override
			public Enumeration<URL> getResources(String name) throws IOException {
				try {
					// file:/home/maxicreditosc/.m2/repository/br/com/maxicredito/auth-api/1.0.0-SNAPSHOT/auth-api-1.0.0-SNAPSHOT.jar/datasets/
					final File jar = new File("src/test/resources/test.jar");
					final String file = "jar:file:" + jar.getAbsolutePath() + "!/datasets/";
					final URL url = new URL(file);
					final VirtualFile vf = VFS.getChild(url.toURI());
					final Collection<URL> c = Arrays.asList(vf.toURL());
					return Collections.enumeration(c);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		};
		try {
			run(classLoader);
		} catch (IllegalStateException e) {
			/*
			 * não consegui simular uma URL que devolva um JarInputStream, então
			 * so testo se é um vfs scheme
			 */
			assertTrue(e.getCause() instanceof FileNotFoundException);
		}
	}

	@Test
	public void testJarInputStream() throws IOException {
		final File jar = new File("src/test/resources/test.jar");
		final FileInputStream fis = new FileInputStream(jar);
		final JarInputStream jarFile = new JarInputStream(fis);
		final Inserter inserter = new Inserter();
		final List<URL> list = inserter.loadJar(jarFile, Thread.currentThread().getContextClassLoader(), "datasets");
		assertEquals(1, list.size());
	}

	private void run(final ClassLoader cl) throws Exception {
		final Config config = new Config("datasets");
		final Migrator migrator = new MigratorDefault(connection, config, cl);
		migrator.run();
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes Where nome='Jar'";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(1, resultSet.getInt(1));
	}
}
