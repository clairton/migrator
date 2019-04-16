package br.eti.clairton.migrator;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Calculator {

	public Integer sum(final Integer number1, final Integer number2) {
		return number1 + number2;
	}
}
