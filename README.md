# kik-strava

Transfert des entrainements de Strava vers Kikourou.

Les données sont lues dans Strava grâce à l'api REST et envoyé dans kikourou comme le ferait un navigateur.

# Comment avoir un token strava ?

Pour faire marcher tout ça il faut obtenir un token Strava, ce token permet à l'application d'accéder à mes entrainements pour faire le transfert vers kikourou.

## Etape 1

Aller sur https://www.strava.com/settings/api

Remplir le formulaire :

![alt tag](https://raw.githubusercontent.com/kekepins/kekepins/strava/blob/img/img/strava21.png)

## Etape 2

Mettre une image bidon

![alt tag](https://raw.githubusercontent.com/kekepins/kekepins/strava/blob/img/img/strava3.png)

![alt tag](https://raw.githubusercontent.com/kekepins/kekepins/strava/blob/img/img/strava4.png)

## Etape 3

Et voilà on a fait le plus dur on peut récupérer le précieux

![alt tag](https://raw.githubusercontent.com/kekepins/kekepins/strava/blob/img/img/strava5.png)

# Installation

# Configuration

Il faut rajouter dans le classpath un fichier de configuration 

stravakik.conf

Avec les informations suivante :

* user kikourou (celui du site)
* psw kikourou (celui du site) 
* token strava (https://www.strava.com/settings/api)



# Pour les développeurs

## build

```
$ mvn clean package
```
