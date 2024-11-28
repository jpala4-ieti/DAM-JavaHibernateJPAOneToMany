# Exemple Canvas Hibernate tipus JPA One To Many #

En aquest projecte hi ha un exemple de Hibernate tipus XML One To Many

## Compilació i funcionament ###

Cal el 'Maven' per compilar el projecte
```bash
mvn clean
mvn compile
mvn test
```

Per executar el projecte a Windows cal
```bash
.\run.ps1 com.project.Main
```

Per executar el projecte a Linux/macOS cal
```bash
./run.sh com.project.Main
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
