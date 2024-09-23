# OpenClassrooms - Parcours de développeur d'applications Java - Projet 9

### Langages de programmation

* Backend: Java 17
* Frontend: HTML/CSS

### Stack technique commun

|          Fonction           |               Outil               |
|:---------------------------:|:---------------------------------:|
|          Framework          |   Java Spring with Spring Boot    |
|      Build & Packaging      |               Maven               |
| Boilerplate Code Reduction  |              Lombok               |
|           Testing           | JUnit 5, MockMvc, Mockito, okHttp |
|      Testing coverage       |              JaCoCo               |
|       Testing report        |             Surefire              |


## Architecture Microservices

### Diagramme

![Diagramme](https://i.imgur.com/XWkGunQ.png)

### gateway

* Passerelle permettant de relier les différents microservices en routant les requêtes http vers le service sollicité.
* C'est le point d'entrée unique de notre application.
* _Stack technique spécifique : Spring Cloud Gateway, JsonWebToken_

[Lien vers le module] (https://github.com/Scud-P/MicroLabo/tree/exploration/gateway)

### microauth

* Service gérant l'authentification et l'authorisation.
* Il permet de gérer les utilisateurs et de générer un token JWT lors de leur connexion. Il interagit avec la base de données MySQL.
* _Stack technique spécifique : Spring Security, Spring Boot Web, Spring Boot Data JPA, JsonWebToken_

[Lien vers le module] (https://github.com/Scud-P/MicroLabo/tree/exploration/gateway)

### microfront

* Service faisant office d'interface utilisateur.
* _Stack technique spécifique : Thymeleaf, BootStrap, Spring Boot Web, Reactor 3_

[Lien vers le module] (https://github.com/Scud-P/MicroLabo/tree/exploration/microfront)

### microlabo

* Service en charge des requêtes CRUD pour les entités _Patients_
* Il interagit avec la base de données MySQL
* _Stack technique spécifique : Spring Boot Web, Spring Boot Data JPA_

  [Lien vers le module] (https://github.com/Scud-P/MicroLabo/tree/exploration/microlabo)

### micronotes

* Service en charge des requêtes CRUD pour les documents _Notes_
* Il interagit avec la base de données MongoDB
* _Stack technique spécifique : Spring Boot Web, Spring Boot Data MongoDB_

[Lien vers le module] (https://github.com/Scud-P/MicroLabo/tree/exploration/micronotes)

### microrisk

* Service en charge de la logique d'assertion des risques pour les patients
* Il interagit avec la base de données MongoDB
* _Stack technique spécifique : Spring Boot Web, Spring Boot Data MongoDB_

[Lien vers le module] (https://github.com/Scud-P/MicroLabo/tree/exploration/microrisk)

### Containerisation

Afin de faciliter le déploiement de l'application, chaque microservice possède son Dockerfile qui spécifie :

* ``` FROM openjdk:17-jdk-alpine ``` : l'image à partir de laquelle docker se base pour construire notre image. Ici, nous utilisons une image légère contenant une installation minimale de Java 17 basée sur une distribution Linux.
* ``` WORKDIR /app ``` : on définit le répertoire à partir duquel toutes les commandes successives vont être exécutées.
* ``` COPY target/nom_du_service-version-SNAPSHOT.jar app.jar ``` : on copie le fichier .jar généré lors de l'installation du service
* ``` ENV VARIABLE_ENVIRONNEMENT=variable ``` : optionnel, on déclare les variables d'environnement
* ``` EXPOSE PORT ``` : on déclare le port utilisé à l'intérieur du conteneur
* ``` ENTRYPOINT ["java", "-jar", "app.jar"] ``` : on définit la commande exécutée au démarrage du conteneur

De plus, nous avons un docker-compose.yml à la racine du projet qui permet de démarrer tous les conteneurs dans le bon ordre, ce qui est important lorsque certains composants dépendent des autres pour fonctionner. Comme l'authentification qui dépend des utilisateurs dans la base de données MySQL.

On commence par le mot-clé suivant.

``` services: ``` :

Puis pour chaque service, les informations suivantes sont renseignées.

### Configuration des services

Chaque service est configuré avec les informations suivantes :

```yaml
nom_du_service:  # Nom du service dans Docker Compose
  image: nom_image  # Image Docker utilisée pour ce service
  build:  # Instruction pour construire l'image Docker
    context: ./nom_du_context  # Chemin vers le répertoire contenant le Dockerfile
  ports:
    - port_exposé:port_écouté  # Port sur la machine hôte (port_exposé) mappé au port dans le conteneur (port_écouté)
  depends_on:
    - nom_du_composant  # Ce service dépend de ce composant, il attendra que ce dernier soit démarré pour démarrer lui-même
  environment:
    - nom_de_la_variable  # Variables d'environnement passées au conteneur
  volumes:
    - nom_du_dossier  # Mappe un volume au conteneur
  network:
    - nom_du_réseau  # Le réseau Docker sur lequel ce service communique avec d'autres services

# On termine par des informations valables pour tous les services.

volumes:
  nom_du_volume_1:  # Définition d'un volume persistant que plusieurs services peuvent partager
  nom_du_volume_2:  # Un autre volume persistant

networks:
  nom_du_réseau:  # Définition d'un réseau Docker que les services peuvent utiliser pour communiquer
    driver: type_de_réseau
```