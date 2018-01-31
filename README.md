# kik-strava

Transfert des entrainements de Strava vers Kikourou.

Les données sont lues dans Strava grâce à l'api REST et envoyé dans kikourou comme le ferait un navigateur.



# configuration

Il faut rajouter dans le classpath un fichier de configuration 

stravakik.conf

Avec les informations suivante :

* user kikourou (celui du site)
* psw kikourou (celui du site) 
* token strava (https://www.strava.com/settings/api)

# Comment avoir un token strava ?

Pour faire marcher tout ça il faut obtenir un token Strava, ce token permet à l'application d'accéder à mes entrainements pour faire le transfert vers kikourou.

## Etape 1

Aller sur https://www.strava.com/settings/api

Remplir le formulaire :

## Etape 2

Mettre une image bidon

## Etape 3

Et voilà on a fait le plus dur on peut récupérer le précieux


# Pour les développeurs

## build

```
$ mvn clean package
```
