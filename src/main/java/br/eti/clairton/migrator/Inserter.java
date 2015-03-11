package br.eti.clairton.migrator;

import static org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

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
@ApplicationScoped
public class Inserter {
	private final Logger logger = LogManager.getLogger(getClass().getName());

	public void run(final Connection connection, final Config config, final ClassLoader classLoader) throws Exception {
		if (config.isPopulate()) {
			logger.info("Carregando dataSets");
			final Collection<URL> files = new ArrayList<URL>();
			final String path = config.getDataSetPath();
			final Enumeration<URL> resources = classLoader.getResources(path);
			while (resources != null && resources.hasMoreElements()) {
				final URL url = resources.nextElement();
				final String scheme = url.toURI().getScheme();
				if("vfs".equals(scheme)){
					final InputStream inputStream = url.openStream();
					if(inputStream instanceof JarInputStream){						
						final JarInputStream jarStream = (JarInputStream) inputStream;
						files.addAll(loadJar(jarStream, classLoader, path));
					}
				}				
				if ("jar".equals(scheme)) {
					logger.info("Jar " + url.getPath());
					final JarURLConnection conn = (JarURLConnection) url.openConnection();
					final Enumeration<JarEntry> en = conn.getJarFile().entries();
					final String mainEntryName = conn.getEntryName();
					while (en.hasMoreElements()) {
						final JarEntry entry = en.nextElement();
						final String entryName = entry.getName();
						if (entryName.startsWith(mainEntryName) && entryName.endsWith(".csv")) {
							final String name = url.toURI() + entryName.replace(mainEntryName, "");
							logger.info("Adicionando arquivo csv {}", name);
							URL u = new URL(name);
							files.add(u);
						}
					}
				} else {
					final File file = new File(url.getPath());
					logger.info("Diretório " + file);
					listFilesForFolder(file, files);
				}
			}
			load(files.toArray(new URL[files.size()]), connection);
		}
	}
	
	public List<URL> loadJar(final JarInputStream jarStream,final ClassLoader classLoader, final String path) throws IOException{
		final List<URL> files = new ArrayList<URL>();
		while (true) {
			final JarEntry entry = jarStream.getNextJarEntry();
			if(entry == null){
				break;
			}else{
				if(entry.toString().endsWith(".csv")){									
					final URL file = classLoader.getResource(path+"/"+entry);
					logger.info("Adicionando arquivo csv {}", file);
					files.add(file);
				}
			}
		}
		return files;
	}

	private void listFilesForFolder(final File file, Collection<URL> files) {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				listFilesForFolder(f, files);
			}
		} else {
			if (file.toString().endsWith(".csv")) {
				logger.info("Adicionando arquivo csv " + file);
				try {
					files.add(new File(file.toString()).toURI().toURL());
				} catch (final MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
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
	public void load(final DataSet annotation, final Connection connection)
			throws Exception {
		final Collection<String> files = Arrays.asList(annotation.value());
		logger.info("Datasets a inserir " + files);
		final Annotation qualifier = getQualifier(annotation.qualifier());
		logger.info("Recuperando conexão com qualifier {}", qualifier.annotationType().getSimpleName());
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
	public void load(final Collection<String> files, final Connection connection)
			throws Exception {
		final Collection<URL> csvs = new ArrayList<URL>(files.size());
		for (final String path : files) {
			final ClassLoader cl = getClass().getClassLoader();
			final Enumeration<URL> resources = cl.getResources(path);
			while (resources.hasMoreElements()) {
				final URL url = (URL) resources.nextElement();
				csvs.add(url);

			}
		}
		load(csvs.toArray(new URL[csvs.size()]), connection);
	}

	public void load(final URL[] files, final Connection connection)
			throws Exception {
		final Collection<IDataSet> dataSets = new ArrayList<IDataSet>(
				files.length);
		for (final URL file : files) {
			if (!file.toString().endsWith(".csv")) {
				throw new IllegalStateException(
						"Only supports CSV and SQL data sets for the moment");
			}
			// Decorate the class and call addReplacementObject method
			final ReplacementDataSet rDataSet = new ReplacementDataSet(new CsvDataSet(file));
			final String content = getString(file.openStream());
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
		connection.setAutoCommit(false);
		load(fDataSet, connection);
		connection.commit();
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

	private static String getString(final InputStream is) {
		BufferedReader br = null;
		final StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();

	}
}
