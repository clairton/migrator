package br.eti.clairton.migrator;

import static java.nio.file.Files.walkFileTree;
import static org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
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
	private final Logger logger = LogManager.getLogger(getClass().getName());
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

	public void init() throws Exception {
		if (config.isInsert()) {
			logger.info("Carregando dataSets");
			final Collection<String> files = new ArrayList<>();
			walkFileTree(new File(config.getDataSetPath()).toPath(),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(final Path file,
								final BasicFileAttributes attrs)
								throws IOException {
							if (file.toString().endsWith(".csv")) {
								files.add(file.toAbsolutePath().toString());
							}
							return FileVisitResult.CONTINUE;
						}
					});
			load(files, connection);
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
	public void load(final DataSet annotation, final Connection connection)
			throws Exception {
		final Collection<String> files = Arrays.asList(annotation.value());
		logger.info("Datasets a inserir " + files);
		final Annotation qualifier = getQualifier(annotation.qualifier());
		logger.info("Recuperando conexão com qualifier "
				+ qualifier.annotationType().getSimpleName());
		logger.info("Conexão recuperada " + connection);
		load(files, connection);
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
		for (final String path : files) {
			final File file = new File(path);
			if (!path.endsWith(".csv")) {
				throw new IllegalStateException(
						"Only supports CSV and SQL data sets for the moment");
			}
			// Decorate the class and call addReplacementObject method
			final ReplacementDataSet rDataSet = new ReplacementDataSet(
					new CsvDataSet(file));
			final String content = new String(Files.readAllBytes(file.toPath()));
			final String s = "\\$\\{sql\\(";
			final String e = "\\)\\}";
			// check de format ${sql()}
			final Pattern pattern = Pattern.compile(s + ".*" + e);
			final Matcher matcher = pattern.matcher(content);
			// check all occurance
			while (matcher.find()) {
				int begin = matcher.start();
				int end = matcher.end();
				final String key = content.substring(begin, end);
				final String sql = key.replaceAll(s, "").replaceAll(e, "");
				final Statement statement = connection.createStatement();
				final ResultSet resultSet = statement.executeQuery(sql);
				resultSet.next();
				final String value = resultSet.getString(1);
				rDataSet.addReplacementObject(key, value);
			}
			dataSets.add(rDataSet);
		}
		final IDataSet[] a = new IDataSet[dataSets.size()];
		final IDataSet dataSet = new CompositeDataSet(dataSets.toArray(a));
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
		final DefaultDataTypeFactory factory = new HsqldbDataTypeFactory();
		ddsc.getConfig().setProperty(PROPERTY_DATATYPE_FACTORY, factory);
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
