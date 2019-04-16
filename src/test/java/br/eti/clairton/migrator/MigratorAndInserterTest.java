package br.eti.clairton.migrator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class MigratorAndInserterTest {

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
