# Outil d'import des entrainements de Strava ou Movescount dans kikourou (http://www.kikourou.net)

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
On télécharge l'application ici : https://github.com/kekepins/strava/raw/master/dist/kikstrava_1.0.0.zip

On dézippe dans un répertoire.

## Démarrage double clic sur : kikstrava.bat

Attention avant de faire ça **il faut configurer le fichier stravakik.conf**

# Configuration

Il faut éditer le fichier 
**conf/stravakik.conf** et renseigner les informations :

* kik.user : user kikourou (celui du site)
* kik.password : psw kikourou (celui du site) 
* strava.token : token strava (voir le début)
* si besoin on peut configurer un proxy

# Fonctionnement

Si vous êtes fort vous avez normalement réussi à lancer l'application :

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/appli1.png)

Reste plus qu'à cliquer sur le bouton et les entraiments de strava doivent apparaitre :

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/appli2.png)

On peut modifier certaines infos (validation par **Entrée**)
Et on clique sur l'autre bouton et ça envoie dans kikourou :

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/appli3.png)

# Pour les développeurs

## build

```
$ mvn clean package
```

## création d'une release

On s'amuse et on utilise les nouveautés de java 9 qui permet de construire des jre moins gros afin de créer un package complet d'exécution.
Pour celà on on utilise **jlink**

Exemple dans notre cas : 
```
jlink --module-path C:/devtools/jkdk19_64/jmods;mods --add-modules kikstrava --launcher start-app=kikstrava/kikstrava.KikStravaGui --output release --strip-debug --compress 2 --no-header-files --no-man-pages
```

NB : au préalable j'ai rajouté, compilé, et incorporé les fichiers **module-info.java**  pour toutes les librairies utilisées.
Ces librairies sont stockées accessible dans le répertoire mods/
