package br.eti.clairton.migrator;

import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static javax.enterprise.inject.spi.CDI.current;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.control.RequestContextController;
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
	private Integer count = 0;

	public void run(final @Observes AfterDeploymentValidation adv, final BeanManager manager) throws Exception {
		try {
			logger.log(INFO, "Iniciando Migrator Extension");
			final Set<Bean<?>> beans = manager.getBeans(Migrator.class, any);
			logger.log(INFO, "{0} Migrator(s) encontrados", beans.size());
			for (final Bean<?> bean : beans) {
				final CreationalContext<?> context = manager.createCreationalContext(bean);
				final Class<?> type = bean.getBeanClass();
				final Object object = manager.getReference(bean, type, context);
				final Migrator instance = (Migrator) object;
				logger.log(INFO, "Rodando {0}", instance.getClass());
				instance.run();
			}
		} catch (final ContextNotActiveException e) {
			// activate request escope
			count++;
			final RequestContextController controller = current().select(RequestContextController.class).get();
			controller.activate();
			if (count <= 1) {
				try {
					run(adv, manager);
				} catch (final Throwable t) {
					throw t;
				} finally {
					controller.deactivate();
				}
			}
		}
	}
}
