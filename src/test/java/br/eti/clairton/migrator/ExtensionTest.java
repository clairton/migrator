package br.eti.clairton.migrator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

class ExtensionTest {
	@Test
	void testRunCollectionOfMigratorCallMigratorRunOneTime() throws Exception {
		final Extension extension = new Extension();
		final Migrator migrator = mock(Migrator.class);
		final Collection<Migrator> migrators = Arrays.asList(migrator);
		extension.run(migrators);
		verify(migrator).run();
	}
}
