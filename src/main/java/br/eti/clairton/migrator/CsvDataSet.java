package br.eti.clairton.migrator;

import static org.dbunit.dataset.csv.CsvDataSetWriter.NULL;
import static org.dbunit.dataset.datatype.DataType.UNKNOWN;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.csv.CsvParser;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.csv.CsvParserImpl;

/**
 * Single {@link IDataSet} csv.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class CsvDataSet extends CachedDataSet implements IDataSet {
	/**
	 * Construtor padrão.
	 * 
	 * @param file
	 *            arquivo a ser carregado
	 * @throws DataSetException
	 *             caso ocorra algum problema
	 */
	public CsvDataSet(final URL file) throws DataSetException {
		super();
		produceFromFile(file);
	}

	private void produceFromFile(final URL file) throws DataSetException, CsvParserException {
		try {
			final CsvParser parser = new CsvParserImpl();
			final List<?> readData = parser.parse(file);
			final List<?> readColumns = ((List<?>) readData.get(0));
			final Column[] columns = new Column[readColumns.size()];
			for (int i = 0; i < readColumns.size(); i++) {
				final String columnName = ((String) readColumns.get(i)).trim();
				columns[i] = new Column(columnName, UNKNOWN);
			}
			final String fileName = new File(file.getFile()).getName();
			final Integer endIndex = fileName.indexOf(".csv");
			final String tableName = fileName.substring(0, endIndex);
			final ITableMetaData metaData = new DefaultTableMetaData(tableName,columns);
			startTable(metaData);
			for (int i = 1; i < readData.size(); i++) {
				final List<?> rowList = (List<?>) readData.get(i);
				final Object[] row = rowList.toArray();
				for (int col = 0; col < row.length; col++) {
					if (row[col].equals(NULL)) {
						row[col] = null;
					} else {
						row[col] = row[col];
					}
				}
				row(row);
			}
			endTable();
		} catch (final Exception e) {
			throw new DataSetException(e);
		}
	}
}
