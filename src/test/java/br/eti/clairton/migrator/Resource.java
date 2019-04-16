package br.eti.clairton.migrator;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class Resource {
	private final Config config = new Config("datasets") {
		private int calls = 0;

		@Override
		public Boolean isDrop() {
			return calls++ > 1;
		}
	};

	private final Connection connection;

	public Resource() throws Exception {
		final String url = "jdbc:hsqldb:mem:migrator";
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

	public void closeConnection(@Disposes Connection connection) throws Exception {
		connection.close();
	}
}
