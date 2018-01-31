# kik-strava

Transfert des entrainements de Strava vers Kikourou.

Les données sont lues dans Strava grâce à l'api REST et envoyé dans kikourou comme le ferait un navigateur.

# Comment avoir un token strava ?

Pour faire marcher tout ça il faut obtenir un token Strava, ce token permet à l'application d'accéder à mes entrainements pour faire le transfert vers kikourou.

## Etape 1

Aller sur https://www.strava.com/settings/api

Remplir le formulaire :

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava21.png)

## Etape 2

Mettre une image bidon

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava3.png)

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava4.png)

## Etape 3

Et voilà on a fait le plus dur on peut récupérer le précieux

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/srtava5.png)

# Installation

## Dézipper dans un répertoire
url 

## Double clic sur : kikstrava.bat

Attention avant de faire ça il faut configurer le fichier stravakik.conf

# Configuration

I aut éditer le dichier 
stravakik.conf et renseigner les informations 

Avec les informations suivante :

* user kikourou (celui du site)
* psw kikourou (celui du site) 
* token strava (https://www.strava.com/settings/api)
* si besoin on peut configurer un proxy

# Pour les développeurs

## build

```
$ mvn clean package
```
