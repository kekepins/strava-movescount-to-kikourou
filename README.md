# Outil d'import des entrainements de Strava ou Movescount dans kikourou (http://www.kikourou.net)

Transfert des entrainements de Strava ou Movescount vers Kikourou.

Les données sont lues dans Strava grâce à l'api REST et envoyé dans kikourou comme le ferait un navigateur.
Pour strava on utilise des api pas documentées mais qui permettent aussi d'accéder aux entrainements, movescount renvoie aussi des données en REST.

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

# Comment avoir son identifiant (userkey) movescount

Pour se connecter à movescount on a besoin de son mail qui a servi a ouvrir le compte movescount, ça c'est normalement facile.
Il faut arriver à trouver son userkey movescount, ça c'est un poil plus compliqué.
Il faut arriver à trouver le fichier de configuration de moveslink sur son PC : **Settings.xml**
Ce fichier est normalement ici :
C:\Users\XXXXXXXX\AppData\Roaming\Suunto\Moveslink2\

Ensuite on l'ouvre avec une éditeur de texte (notepad++ ...)

Et on cherche le texte :
```xml
<UserKey>*****-*****-****-****-*****</UserKey>
```

Bravo vous êtes très fort, on note précieusement ce numéro.

# Versions

## Version 1.1
https://github.com/kekepins/strava/raw/master/dist/kikstrava_1.1.0.zip

Cette version rajoute :
* support de movescount
* ajout de l'url de l'entrainement source en commentaire privé de kikourou
* les entrainements ayant la même date dans kirourou ne sont plus coché 

## Version 1.0
https://github.com/kekepins/strava/raw/master/dist/kikstrava_1.1.0.zip

Première version uniquement l'import depuis strava.

# Installation

## Dézipper dans un répertoire
On télécharge la dernière verison de l'application ici : https://github.com/kekepins/strava/raw/master/dist/kikstrava_1.1.0.zip

On dézippe dans un répertoire.

## Démarrage double clic sur : kikstrava.bat

Attention avant de faire ça **il faut configurer le fichier stravakik.conf**

# Configuration

Il faut éditer le fichier 
**conf/stravakik.conf** et renseigner les informations :

* kik.user : user kikourou (celui du site)
* kik.password : psw kikourou (celui du site) 
* strava.token : token strava (voir le début)
* movescount.email : email identifiant movescount 
* movescount.userkey : userkey movescount (voir plus haut pour le trouver)
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
