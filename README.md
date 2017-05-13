# migrator[![Build Status](https://drone.io/github.com/clairton/migrator/status.png)](https://drone.io/github.com/clairton/migrator/latest)
Migração de dados e população de dados iniciais, usando respectivamente liquibase e dbunit. 
O processo é executado através de uma extensão do CDI. É necessário fornecer uma instancia @Defaul
de br.eti.clairton.migrator.Config, como por exemplo:
```java
@Produces
public Config getConfig(){
  System.setProperty("br.eti.clairton.migrator.populate", "true");
  System.setProperty("br.eti.clairton.migrator.drop", "true");
  final String diretorioDataSets = "resources/datasets";
  return new Config(diretorioDataSets);
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
Na intancia de config pode-se configurar se o liquibase ira deletar o banco antes de aplicar as alterações, e
se será populado o banco com os changesets. O comportamente pode ser alterado setando as propriedades
"br.eti.clairton.migrator.populate" e "br.eti.clairton.migrator.drop", 
ou especializando o Config e personalizando os métodos "isDrop" e "isPopulate".

Necessitará fornecer uma instancia de Connection com qualifier @Default.

Download através do maven, dependência:
```xml
<dependency>
	<groupId>br.eti.clairton</groupId>
    <artifactId>migrator</artifactId>
	<version>1.4.0</version>
</dependency>
```