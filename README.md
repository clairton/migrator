# Migrator
		Migração de dados e população de dados iniciais, usando respectivamente liquibase
	e dbunit. O processo é executado através de uma extensão do CDI.
		Necessário fornecer uma instancia @Defaul de br.eti.clairton.migrator.Config, como por exemplo
		
```
	@Produces
	public Config getConfig(){
		final Boolean droparBancoPrimeiro = Boolean.TRUE;
		final Boolean inserirFixtures = Boolean.TRUE;
		final String diretorioDataSets = "src/test/resources/datasets";
		return new Config(droparBancoPrimeiro, inserirFixtures, diretorioDataSets);
	}	
```

	Segue algumas outras informações interessantes sobre o projeto.	

# Versionamento
		As versões seguem o padrão especificado http://semver.org/lang/pt-BR/, 
		portanto tire um tempo para ler e entender.
		Utiliza o GIT(http://git-scm.com/) como sistema de versionamento para o fontes.

# Convenção de nomes de classes, variavéis e pacotes	e banco de dados
	Como padrão de codificação foi estabelecido usar o https://google-styleguide.googlecode.com/svn/trunk/javaguide.html.
	Porém destaca-ser alguns pontos:
		*Nome da classe como Substantivo
		*Nome de método como Verbo
		*Notação polonesa, hungara, alemã ou de qualquer nacionalidade estão extremamente proibidas
		*Use nomes expressivos e simples, mas não exóticos