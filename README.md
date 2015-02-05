# Migrator
Migração de dados e população de dados iniciais, usando respectivamente liquibase e dbunit. 
O processo é executado através de uma extensão do CDI. É necessário fornecer uma instancia @Defaul
de br.eti.clairton.migrator.Config, como por exemplo:
		
```java
@Produces
public Config getConfig(){
  final Boolean droparBancoPrimeiro = Boolean.TRUE;
  final Boolean inserirFixtures = Boolean.TRUE;
  final String diretorioDataSets = "resources/datasets";
  return new Config(droparBancoPrimeiro, inserirFixtures, diretorioDataSets);
}	
```
Os dados podem ser inseridos dinamicamente no CSV usado pelo DBUnit, utilizando a marcação ${sql(string sql)} exemplo:
```csv
id,nome
1001,Corretora
1002,Controladoria
1003,Financeiro
1004,${sql(select 'Valor por Sql' from aplicacoes)}
```