package br.eti.clairton.migrator;


import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;


/**
 * Extensão para migrar o banco de dados
 * 
 * @author Clairton Rodrigo Heinzen clairton.rodrigo@gmail.com
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	private static final Logger logger = getLogger(Extension.class.getSimpleName());
	private final Annotation any = new AnnotationLiteral<Any>() {
		private static final long serialVersionUID = -8700665898396680284L;
	};

	public void init(final @Observes AfterDeploymentValidation adv, final BeanManager manager) throws Exception {
		logger.log(INFO, "Iniciando Migrator Extension");
		final Set<Bean<?>> beans = manager.getBeans(Migrator.class, any);
		logger.log(INFO, "{} Migrator(s) encontrados", beans.size());
		for (final Bean<?> bean : beans) {
			final CreationalContext<?> context = manager.createCreationalContext(bean);
			final Class<?> type = bean.getBeanClass();
			final Object object = manager.getReference(bean, type, context);
			final Migrator instance = (Migrator) object;
			instance.run();
		}
	}
}
