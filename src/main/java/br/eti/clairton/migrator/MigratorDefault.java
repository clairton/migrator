package br.eti.clairton.migrator;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static liquibase.database.DatabaseFactory.getInstance;

import java.sql.Connection;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

@Dependent
public class MigratorDefault implements Migrator {
	private static final Logger logger = getLogger(MigratorDefault.class.getSimpleName());

	private final Config config;
	private final Inserter inserter;
	private final Liquibase liquibase;

	@Deprecated
	protected MigratorDefault() {
		this((Liquibase) null, null, null);
	}

	@Deprecated
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config) {
		this(connection, config, new Inserter(), MigratorDefault.class.getClassLoader());
	}

	@Inject
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config,
			final @NotNull Inserter inserter) {
		this(connection, config, inserter, MigratorDefault.class.getClassLoader());
	}

	@Deprecated
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config,
			final @NotNull ClassLoader classLoader) {
		this(connection, config, new Inserter(), classLoader);
	}

	public MigratorDefault(final Connection connection, final Config config, final Inserter inserter,
			final ClassLoader classLoader) {
		this(getLiquibase(classLoader, connection, config.getChangelogPath()), config, inserter);
	}

	public MigratorDefault(final Liquibase liquibase, final Config config, final Inserter inserter) {
		this.liquibase = liquibase;
		this.config = config;
		this.inserter = inserter;
	}

	@Override
	public void run() {
		final DatabaseConnection connection = liquibase.getDatabase().getConnection();
		try {
			if (!config.isMigrate()) {
				logger.log(INFO, "Não irá rodar as migrações");
				return;
			}
			if (config.getSchema() != null && !config.getSchema().isEmpty()) {
				logger.log(INFO, "Setando o esquema padrão para {0}", config.getSchema());
				final Database database = liquibase.getDatabase();
				database.setDefaultSchemaName(config.getSchema());
			} else {
				logger.log(INFO, "Não foi setado o esquema padrão");
			}
			final boolean autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(FALSE);
			try {
				if (config.getSchema() != null && !config.getSchema().isEmpty()) {
					// create schema, if already exist does not a problem
					connection.nativeSQL(format("CREATE SCHEMA %s;", config.getSchema()));
				}
			} catch (final Exception e) {
				try {
					connection.rollback();
				} catch (final DatabaseException e1) {
				}
			}
			if (config.isDrop()) {
				turnoff(connection);
				logger.log(INFO, "Deletando objetos");
				if (config.getSchema() != null && !config.getSchema().isEmpty()) {
					final CatalogAndSchema schemas = new CatalogAndSchema(null, config.getSchema());
					liquibase.dropAll(schemas);
				} else {
					liquibase.dropAll();
				}
			}
			final String context = "";
			logger.log(INFO, "Rodando changesets {0}", config.getChangelogPath());
			liquibase.update(context);
			logger.log(INFO, "Changesets {0} aplicados com sucesso", config.getChangelogPath());
			final ClassLoader classLoader = liquibase.getResourceAccessor().toClassLoader();
			inserter.run(((JdbcConnection) connection).getWrappedConnection(), config, classLoader);
			connection.commit();
			connection.setAutoCommit(autoCommit);
		} catch (final Exception e) {
			try {
				connection.rollback();
			} catch (final DatabaseException e1) {
			}
			turnoff(connection);
			throw new IllegalStateException(e);
		}
	}

	protected void turnoff(final DatabaseConnection connection) {
		try {
			connection.rollback();
		} catch (final DatabaseException e1) {
		}
		try {
			logger.log(INFO, "Desligando dataBase changelock");
			/*
			 * desliga o lock ao subir em ambiente de teste ou desenvolvimento
			 */
			final String command = "UPDATE databasechangeloglock SET locked=false";
			connection.nativeSQL(command);
			connection.commit();
			logger.log(INFO, "DataBase change lock desligado");
		} catch (final Exception e) {
			try {
				connection.rollback();
			} catch (final Exception e2) {
			}
		} finally {
			try {
				connection.setAutoCommit(FALSE);
			} catch (final Exception e) {
			}
		}
	}

	public Config getConfig() {
		return config;
	}

	public Inserter getInserter() {
		return inserter;
	}

	public Liquibase getLiquibase() {
		return liquibase;
	}

	private static Liquibase getLiquibase(final ClassLoader classLoader, final Connection connection,
			final String changelogPath) {
		try {
			DatabaseConnection jdbcConnection = new JdbcConnection(connection);
			final Database database = getInstance().findCorrectDatabaseImplementation(jdbcConnection);
			final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classLoader);
			final Liquibase liquibase = new Liquibase(changelogPath, resourceAccessor, database);
			return liquibase;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
