package br.eti.clairton.migrator;

import static javax.enterprise.inject.spi.CDI.current;

import java.lang.reflect.Method;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class ContextActivator {
	private final String name = "javax.enterprise.context.control.RequestContextController";
	private final Class<?> klazz;
	private final Object controller;

	public ContextActivator() throws ClassNotFoundException {
		this(current().getBeanManager());
	}

	public ContextActivator(final BeanManager manager) throws ClassNotFoundException {
		klazz = Class.forName(name);
		final Set<Bean<?>> beans = manager.getBeans(klazz);
		final Bean<?> bean = manager.resolve(beans);
		final CreationalContext<?> ctx = manager.createCreationalContext(bean);
		controller = manager.getReference(bean, klazz, ctx);
	}

	public void start() throws Exception {
		final Method activate = klazz.getMethod("activate");
		activate.invoke(controller);
	}

	public void stop() throws Exception {
		final Method deactivate = klazz.getMethod("deactivate");
		deactivate.invoke(controller);
	}
}
