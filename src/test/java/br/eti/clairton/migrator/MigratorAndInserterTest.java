package br.eti.clairton.migrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiJUnit4Runner.class)
public class MigratorAndInserterTest {
	private @Inject Connection connection;

	@Test
	public void testRun() throws SQLException {
		assertNotNull(connection);
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(4, resultSet.getInt(1));
	}
}
