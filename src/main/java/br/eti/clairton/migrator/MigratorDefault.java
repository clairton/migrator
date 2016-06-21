package br.eti.clairton.migrator;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static liquibase.database.DatabaseFactory.getInstance;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.sql.Connection;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

@Dependent
public class MigratorDefault implements Migrator {
	private static final Logger logger = getLogger(MigratorDefault.class);

	private final Connection connection;
	private final Config config;
	private final ClassLoader classLoader;
	private final Inserter inserter;

	@Deprecated
	protected MigratorDefault() {
		this(null, null, null, null);
	}

	@Deprecated
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config) {
		this(connection, config, new Inserter(), MigratorDefault.class.getClassLoader());
	}

	@Inject
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config, final Inserter inserter) {
		this(connection, config, inserter, MigratorDefault.class.getClassLoader());
	}
	
	@Deprecated
	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config, final @NotNull ClassLoader classLoader) {
		this(connection, config, new Inserter(), classLoader);
	}

	public MigratorDefault(final @NotNull Connection connection, final @NotNull Config config, final Inserter inserter, final @NotNull ClassLoader classLoader) {
		this.connection = connection;
		this.config = config;
		this.classLoader = classLoader;
		this.inserter = inserter;
	}

	@Override
	public void run() {
		try {
			final DatabaseConnection jdbcConnection = new JdbcConnection(connection);
			final Database database = getInstance().findCorrectDatabaseImplementation(jdbcConnection);
			if(config.getSchema() != null && !config.getSchema().isEmpty()){
				database.setDefaultSchemaName(config.getSchema());
			}
			final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classLoader);
			final Liquibase liquibase = new Liquibase(config.getChangelogPath(), resourceAccessor, database);
			final boolean autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(FALSE);
			try{
				//create schema, if already exist does not a problem
				connection.createStatement().executeQuery(format("CREATE SCHEMA %s;", config.getSchema()));
			}catch(final Exception e){}
			if (config.isDrop()) {
				try {
					logger.info("Desligando dataBase changelock");
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
					connection.setAutoCommit(FALSE);
				}
				logger.info("Deletando objetos");
				if(config.getSchema() != null && !config.getSchema().isEmpty()){
					final CatalogAndSchema schemas = new CatalogAndSchema(null, config.getSchema());					
					liquibase.dropAll(schemas);
				} else {					
					liquibase.dropAll();
				}
			}
			final String context = "";
			logger.info("Rodando changesets {}", config.getChangelogPath());
			liquibase.update(context);
			logger.info("Changesets {} aplicados com sucesso", config.getChangelogPath());
			inserter.run(connection, config, classLoader);
			connection.commit();
	        connection.setAutoCommit(autoCommit);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
