package br.eti.clairton.migrator;

import java.io.File;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

/**
 * Carrega os dados no banco de dados.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Dependent
public class Inserter {
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final Connection connection;
	private final Config config;

	@Deprecated
	protected Inserter() {
		this(null, null);
	}

	@Inject
	public Inserter(final Connection connection, final Config config) {
		super();
		this.connection = connection;
		this.config = config;
	}

	@PostConstruct
	public void init() throws Exception {
		if (config.isInsert()) {
			logger.info("Carregando dataSets");
			load(config.getDataSetPath(), connection);
		}
	}

	/**
	 * Carrega os arquivos passados como parametro. Utiliza o
	 * {@link EntityManager} com qualificador {@link Default}.
	 * 
	 * @param annotation
	 *            marcacao de dataSet
	 * @throws Exception
	 *             caso ocorra um erro ao popular a dataBase
	 */
	@Transactional
	public void load(final DataSet annotation) throws Exception {
		final Collection<String> files = Arrays.asList(annotation.value());
		logger.info("Datasets a inserir " + files);
		final Annotation qualifier = getQualifier(annotation.qualifier());
		logger.info("Recuperando conexão com qualifier " + qualifier);
		final Connection connnection = CDI.current()
				.select(Connection.class, qualifier).get();
		logger.info("Conexão recuperada " + connnection);
		load(files, connnection);
	}

	/**
	 * Carrega os arquivos passados como parametro. Utiliza o
	 * {@link EntityManager} com qualificador {@link Default}.
	 * 
	 * @param files
	 *            arquivos a serem carregados
	 * @param connection
	 *            conexão
	 * @throws Exception
	 *             caso ocorra um erro ao popular a dataBase
	 */
	@Transactional
	public void load(final Collection<String> files, final Connection connection)
			throws Exception {
		final Collection<IDataSet> dataSets = new ArrayList<>(files.size());
		for (final String file : files) {
			final IDataSet dataSet;
			if (file.endsWith(".csv")) {
				dataSet = new CsvDataSet(new File(file));
			} else {
				throw new IllegalStateException(
						"Only supports CSV data sets for the moment");
			}
			dataSets.add(dataSet);
		}
		logger.info("Arquivos a serem carregados " + dataSets);
		final IDataSet dataSet = new CompositeDataSet(
				dataSets.toArray(new IDataSet[dataSets.size()]));
		final IDatabaseConnection ddsc = new DatabaseConnection(connection);
		final ITableFilter filter = new DatabaseSequenceFilter(ddsc);
		final IDataSet fDataSet = new FilteredDataSet(filter, dataSet);
		load(fDataSet, connection);
	}

	/**
	 * Carrega os arquivos passados como parametro.
	 * 
	 * @param path
	 *            onde estão os arquivos
	 * @param connection
	 *            conexão com banco de dados
	 * @throws Exception
	 *             caso ocorra algun problema
	 */
	@Transactional
	public void load(final String path, final Connection connection)
			throws Exception {
		final IDataSet dataSet = new org.dbunit.dataset.csv.CsvDataSet(
				new File(path));
		logger.info("Inserindo datasets: ");
		for (final String table : dataSet.getTableNames()) {
			logger.info("     " + table);
		}
		load(dataSet, connection);
	}

	/**
	 * Carrega o dataset passados como parametro.
	 * 
	 * @param dataSet
	 *            com os dados
	 * @param connection
	 *            conexão com banco de dados
	 * @throws Exception
	 *             caso ocorra algun problema
	 */
	@Transactional
	public void load(final IDataSet dataSet, final Connection connection)
			throws Exception {
		final IDatabaseConnection ddsc = new DatabaseConnection(connection);
		ddsc.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
				new HsqldbDataTypeFactory());
		DatabaseOperation.INSERT.execute(ddsc, dataSet);
	}

	private <T extends Annotation> Annotation getQualifier(final Class<T> type) {
		return new Inject() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return type;
			}
		};
	}
}
