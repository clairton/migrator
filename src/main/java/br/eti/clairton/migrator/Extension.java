package br.eti.clairton.migrator;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extensão para migrar o banco de dados
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	private static final Logger logger = LogManager.getLogger(Extension.class);
	private final Annotation any = new AnnotationLiteral<Any>() {
		private static final long serialVersionUID = -8700665898396680284L;
	};

	public void init(final @Observes AfterDeploymentValidation adv, final BeanManager manager) throws Exception {
		logger.info("Iniciando Migrator Extension");
		final Set<Bean<?>> beans = manager.getBeans(Migrator.class, any);
		logger.info("{} Migrator(s) encontrados", beans.size());
		for (final Bean<?> bean : beans) {
			final CreationalContext<?> context = manager.createCreationalContext(bean);
			final Class<?> type = bean.getBeanClass();
			final Object object = manager.getReference(bean, type, context);
			final Migrator instance = (Migrator) object;
			instance.run();
		}
	}
}
