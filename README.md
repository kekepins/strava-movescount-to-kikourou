# kik-strava

Transfert des entrainements de Strava vers Kikourou.

Les données sont lus dans Strava grâce à l'api REST et envoyé dans kikourou comme le ferait un navigateur.

# build

```
$ mvn clean package
```

# configuration

Il faut rajouter dans le classpath un fichier de configuration 

stravakik.conf

Avec les information suivante :

user kikourou
psw kikourou 
token strava

