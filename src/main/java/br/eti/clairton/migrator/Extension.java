package br.eti.clairton.migrator;

import static javax.enterprise.inject.spi.CDI.current;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extensão para migrar o banco de dados
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	private final Logger logger = LogManager.getLogger(getClass().getName());
	private final Annotation q = new AnnotationLiteral<Any>() {
		private static final long serialVersionUID = -8700665898396680284L;
	};

	public void init(final @Observes AfterDeploymentValidation adv)
			throws Exception {
		logger.info("Iniciando Migrator Extension");
		final Instance<Migrator> instance = current().select(Migrator.class, q);
		final Iterator<Migrator> iterator = instance.iterator();
		while (iterator.hasNext()) {
			final Migrator migrator = iterator.next();
			migrator.run();
		}
	}
}
