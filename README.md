# Exemple Canvas Hibernate tipus JPA One To Many

En aquest projecte hi ha un exemple de Hibernate tipus JPA One To Many

## Compilació i exeució

Cal el 'Maven' per compilar el projecte
```bash
mvn clean
mvn compile
mvn test
```

### Execució amb script
Per executar el projecte a Windows cal
```bash
.\run.ps1 com.project.Main
```

Per executar el projecte a Linux/macOS cal
```bash
./run.sh com.project.Main
```

### Execució amb mvn
Si prefereixes executar el projecte pas a pas, pots seguir les següents instruccions:

Neteja el projecte per eliminar fitxers anteriors:
```bash
mvn clean
```

Compila el projecte:
```bash
mvn compile test package
```

Executa la classe principal:
```bash
mvn exec:java -q -Dexec.mainClass="<com.project.Main>" <param1> <param2> <param3>
```

On:
* <com.project.Main>: és la classe principal que vols executar.
* \<param1>, \<param2>, \<param3>: són els paràmetres que necessites passar a la teva aplicació.


### Execució directa amb java
Preparar les dependències en una carpeta (Recomanada per a proves)
```bash
mvn clean package dependency:copy-dependencies
```

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -cp "target/classes;target/dependency/*" com.project.Main
```

### Execució directa amb java (jar package)
```bash
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar target/hibernate-jpa-one-to-many-1.0.0.jar
```

### Execució de tests
```bash
# Executar TOTS els tests
mvn test
# Executar un test individual especificant package o només nom del test
mvn test "-Dtest=com.project.CartItemTest"
mvn test -Dtest=CartItemTest
# Executar múltiples tests específics (separats per comes)
mvn test -Dtest=""
# Tots els tests que comencin amb "Lectura"
mvn test -Dtest="Cart*"
# Tots els tests que continguin "Arxiu"
mvn test -Dtest="*Item*"
# Tests específics de List i Scanner
mvn test -Dtest="*Cart*,*Item*"
```

## Docker per treballar amb mysql

### Iniciar el contenedor
docker-compose up -d

### Verificar que está funcionando
docker-compose ps

### Ver los logs si hay algún problema
docker-compose logs

### Para detener el contenedor
docker-compose down
