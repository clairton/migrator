# Migrator
Migração de dados e população de dados iniciais, usando respectivamente liquibase e dbunit. O processo é executado através de uma extensão do CDI. É
necessário fornecer uma instancia @Defaul de br.eti.clairton.migrator.Config, como por exemplo:
		
```java
@Produces
public Config getConfig(){
  final Boolean droparBancoPrimeiro = Boolean.TRUE;
  final Boolean inserirFixtures = Boolean.TRUE;
  final String diretorioDataSets = "src/test/resources/datasets";
  return new Config(droparBancoPrimeiro, inserirFixtures, diretorioDataSets);
}	
```