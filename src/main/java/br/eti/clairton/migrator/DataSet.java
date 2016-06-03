package br.eti.clairton.migrator;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;

import javax.enterprise.inject.Default;
import javax.inject.Qualifier;
import javax.sql.DataSource;

/**
 * Annotação para configurar as fixtures que seram utilizadas no test.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Inherited
@Documented
public @interface DataSet {
	/**
	 * Arrays de string com o nome dos arquivos. O nome deve ser com o diretorio
	 * completo(exemplo: src/test/resources/datasets/aplicacoes.csv).
	 * 
	 * @return {@link Array}
	 */
	String[] value() default {};

	/**
	 * Qualifier do {@link DataSource}.
	 * 
	 * @return {@link Qualifier}
	 */
	Class<? extends Annotation> qualifier() default Default.class;
}
