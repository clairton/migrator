package br.eti.clairton.migrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiJUnit4Runner.class)
public class MigratorAndInserterTest {
	static {
		System.setProperty(Config.DROP, "true");
		System.setProperty(Config.POPULATE, "true");
	}

	private @Inject Connection connection;

	@Test
	public void testRun() throws SQLException {
		assertNotNull(connection);
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(5, resultSet.getInt(1));
	}

	@Test
	public void testInsertExpression() throws SQLException {
		assertNotNull(connection);
		final Statement statement = connection.createStatement();
		final String sql = "SELECT COUNT(*) FROM aplicacoes WHERE aplicacoes.nome='Valor por Sql'";
		final ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		assertEquals(1, resultSet.getInt(1));
	}

	@Test
	public void testSql() throws Exception {
		final String sql = "select 'Valor por Sql' from aplicacoes";
		final String expression = "1004,${sql(" + sql + ")}";
		final String s = "\\$\\{sql\\(";
		final String e = "\\)\\}";
		// check de format ${sql()}
		final Pattern pattern = Pattern.compile(s + ".*" + e);
		final Matcher matcher = pattern.matcher(expression);
		// check all occurance
		assertTrue(matcher.find());
	}
}
