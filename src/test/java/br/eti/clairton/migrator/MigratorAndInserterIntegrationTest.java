package br.eti.clairton.migrator;

import static org.jboss.weld.junit5.WeldInitiator.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@EnableWeld
@ExtendWith(WeldJunit5Extension.class)
public class MigratorAndInserterIntegrationTest {
	static {
		System.setProperty(Config.DROP, "true");
		System.setProperty(Config.POPULATE, "true");
		System.setProperty(Config.MIGRATE, "true");
	}

	private Class<?>[] classes = new Class<?>[] { Resource.class, Extension.class, MigratorDefault.class,
			Inserter.class };

	@WeldSetup
	public WeldInitiator weld = of(classes);

	@Inject
	private Connection connection;

	@Test
	public void testRun() throws SQLException {
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(5, resultSet.getInt(1));
	}
}
