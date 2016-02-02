package br.eti.clairton.migrator;

import static liquibase.database.DatabaseFactory.getInstance;

import java.sql.Connection;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

@Dependent
public class MigratorDefault implements Migrator {
	private static final Logger logger = LogManager.getLogger(MigratorDefault.class);

	private final Connection connection;
	private final Config config;
	private final ClassLoader classLoader;

	@Deprecated
	protected MigratorDefault() {
		this(null, null);
	}

	@Inject
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config) {
		this(connection, config, MigratorDefault.class.getClassLoader());
	}

	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config, final @NotNull ClassLoader classLoader) {
		this.connection = connection;
		this.config = config;
		this.classLoader = classLoader;
	}

	@Override
	public void run() {
		try {
			final DatabaseConnection jdbcConnection = new JdbcConnection(connection);
			final Database database = getInstance().findCorrectDatabaseImplementation(jdbcConnection);
			final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classLoader);
			final Liquibase liquibase = new Liquibase(config.getChangelogPath(), resourceAccessor, database);
			final boolean autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			if (config.isDrop()) {
				try {
					logger.info("Desligando dataBase change lock");
					/*
					 * desliga o lock ao subir em ambiente de teste ou desenvolvimento
					 */
					final String command = "UPDATE databasechangeloglock SET locked=false";
					connection.createStatement().executeUpdate(command);
					connection.commit();
					logger.info("DataBase change lock desligado");
				} catch (final Exception e) {
					connection.rollback();
				} finally {
					connection.setAutoCommit(false);
				}
				logger.info("Deletando objetos");
				liquibase.dropAll();
			}
			final String context = "";
			logger.info("Rodando changesets {}", config.getChangelogPath());
			liquibase.update(context);
			logger.info("Changesets {} aplicados com sucesso", config.getChangelogPath());
			new Inserter().run(connection, config, classLoader);
			connection.commit();
	        connection.setAutoCommit(autoCommit);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
