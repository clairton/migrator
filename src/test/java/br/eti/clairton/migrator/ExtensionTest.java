package br.eti.clairton.migrator;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.jupiter.api.Test;

class ExtensionTest {

	@Test
	void testRunBeanManagerCollectionOfMigratorStartScope() throws Exception {
		final Extension extension = new Extension();
		final Migrator migrator = mock(Migrator.class);
		final ContextActivator activator = mock(ContextActivator.class);
		doThrow(new ContextNotActiveException()).when(migrator).run();
		final Collection<Migrator> migrators = Arrays.asList(migrator);
		final BeanManager manager = mock(BeanManager.class);
		try {
			extension.run(activator, manager, migrators);
		} catch (ContextNotActiveException e) {
		}
		verify(activator).start(manager);
		verify(activator).stop();
	}

	@Test
	void testRunCollectionOfMigratorCallMigratorRunOneTime() throws Exception {
		final Extension extension = new Extension();
		final Migrator migrator = mock(Migrator.class);
		final Collection<Migrator> migrators = Arrays.asList(migrator);
		extension.run(migrators);
		verify(migrator).run();
	}

}
