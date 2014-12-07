package br.eti.clairton.migrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InserterTest {
	private Connection connection;
	private Inserter inserter;

	// @Before
	public void init() throws Exception {
		final Resource resource = new Resource();
		connection = resource.getConnection();
		inserter = new Inserter(connection, resource.getConfig());
		inserter.init();
	}

	// @Test
	public void testRun() throws SQLException {
		assertNotNull(connection);
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(4, resultSet.getInt(1));
	}
}
