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

### microauth

* Service gérant l'authentification et l'authorisation.
* Il permet de gérer les utilisateurs et de générer un token JWT lors de leur connexion. Il interagit avec la base de données MySQL.
* _Stack technique spécifique : Spring Security, Spring Boot Web, Spring Boot Data JPA, JsonWebToken_

### microfront

* Service faisant office d'interface utilisateur.
* _Stack technique spécifique : Thymeleaf, BootStrap, Spring Boot Web, Reactor 3_

### microlabo

* Service en charge des requêtes CRUD pour les entités _Patients_
* Il interagit avec la base de données MySQL
* _Stack technique spécifique : Spring Boot Web, Spring Boot Data JPA_

### micronotes

* Service en charge des requêtes CRUD pour les documents _Notes_
* Il interagit avec la base de données MongoDB
* _Stack technique spécifique : Spring Boot Web, Spring Boot Data MongoDB_

### microrisk

* Service en charge de la logique d'assertion des risques pour les patients
* * Il interagit avec la base de données MongoDB
* _Stack technique spécifique : Spring Boot Web, Spring Boot Data MongoDB_