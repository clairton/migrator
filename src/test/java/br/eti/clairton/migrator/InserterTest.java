package br.eti.clairton.migrator;

import static java.lang.System.setProperty;
import static java.lang.Thread.currentThread;
import static java.sql.DriverManager.getConnection;
import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static org.jboss.vfs.VFS.getChild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarInputStream;

import org.jboss.vfs.VirtualFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InserterTest {
	static {
		setProperty(Config.DROP, "true");
		setProperty(Config.POPULATE, "true");
		setProperty(Config.MIGRATE, "true");
	}

	private Connection connection;

	@BeforeEach
	public void setUp() throws Exception {
		final String url = "jdbc:hsqldb:mem:migrator2";
		connection = getConnection(url, "sa", "");
		connection.setAutoCommit(true);
		try {
			connection.createStatement().execute("DELETE FROM aplicacoes");
		} catch (Exception e) {}
	}

	@Test
	public void testInsideJar() throws Exception {
		final ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
			@Override
			public Enumeration<URL> getResources(String name) throws IOException {
				// jar:file:/home/maxicreditosc/.m2/repository/br/com/maxicredito/auth-api/1.0.0-SNAPSHOT/auth-api-1.0.0-SNAPSHOT.jar!/datasets/
				final File jar = new File("src/test/resources/test.jar");
				final Collection<URL> c;
				if(name.endsWith("xml")){
					final String file = "jar:file:" + jar.getAbsolutePath() + "!/db/changelogs/changelog-main.xml";
					final URL url = new URL(file);
					c = asList(url);						
				}else if(name.endsWith("xsd")){
					return super.getResources(name);					
				}else{
					final String file = "jar:file:" + jar.getAbsolutePath() + "!/datasets/";
					final URL url = new URL(file);
					c = asList(url);
				}
				return enumeration(c);
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
					final Collection<URL> c;
					if(name.endsWith("xml")){
						final String file = "jar:file:" + jar.getAbsolutePath() + "!/db/changelogs/changelog-main.xml";
						final URL url = new URL(file);
						c = asList(url);						
					}else{
						final String file = "jar:file:" + jar.getAbsolutePath() + "!/datasets/";
						final URL url = new URL(file);
						final VirtualFile vf = getChild(url.toURI());
						c = asList(vf.toURL());
					}
					return enumeration(c);
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
		final List<URL> list = inserter.loadJar(jarFile, currentThread().getContextClassLoader(), "datasets");
		assertEquals(1, list.size());
	}

	private void run(final ClassLoader cl) throws Exception {
		final Config config = new Config("datasets");
		final Inserter inserter = new Inserter();
		final Migrator migrator = new MigratorDefault(connection, config, inserter, cl);
		migrator.run();
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes Where nome='Jar'";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(1, resultSet.getInt(1));
	}
}
