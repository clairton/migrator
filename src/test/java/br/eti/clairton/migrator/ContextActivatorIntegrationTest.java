package br.eti.clairton.migrator;

import static org.jboss.weld.junit5.WeldInitiator.of;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@EnableWeld
@ExtendWith(WeldJunit5Extension.class)
class ContextActivatorIntegrationTest {

	@WeldSetup
	public WeldInitiator weld = of(Calculator.class);

	@Test
	public void contextNotActive() {
		assertThrows(ContextNotActiveException.class, () -> {
			sum(1, 2);
		});
	}

	@Test
	public void contextActive() throws Exception {
		final ContextActivator activator = new ContextActivator();
		activator.start(CDI.current().getBeanManager());
		sum(1, 2);
		activator.stop();

	}

	private Integer sum(final Integer number1, final Integer number2) {
		final Calculator calculator = weld.select(Calculator.class).get();
		return calculator.sum(1, 2);
	}
}
