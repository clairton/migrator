package br.eti.clairton.migrator;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class Resource {
	private final Config config = new Config(true, true,
			"src/test/resources/datasets") {
		private int calls = 0;

		@Override
		public Boolean isDropAll() {
			return calls++ > 1;
		}
	};

	private final Connection connection;

	public Resource() throws Exception {
		final String url = "jdbc:hsqldb:file:target/migrator;hsqldb.lock_file=false;shutdown=true;create=true";
		connection = DriverManager.getConnection(url, "sa", "");
		connection.setAutoCommit(true);
	}

	@Produces
	public Config getConfig() {
		return config;
	}

	@Produces
	public Connection getConnection() {
		return connection;
	}

	public void closeConnection(@Disposes Connection connection)
			throws Exception {
		connection.close();
	}
}
