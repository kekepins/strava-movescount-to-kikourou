# Outil d'import des entrainements de Strava ou Movescount dans kikourou (http://www.kikourou.net)

Transfert des entrainements de Strava ou Movescount vers Kikourou.

Les données sont lues dans Strava grâce à l'api REST et envoyé dans kikourou comme le ferait un navigateur.
Pour strava on utilise des api pas documentées mais qui permettent aussi d'accéder aux entrainements, movescount renvoie aussi des données en REST.

# Comment avoir configurer strava ?

Pour faire marcher tout ça il faut créer une application strava pour avoir le droit d'accéder aux données.
Le but est de récupérer 2 informations le clientid et le cliensecret de l'application.

## Etape 1

Aller sur https://www.strava.com/settings/api

Remplir le formulaire pour créer une application :

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava21.png)

Attention à bien mettre localhost:5000 c'est important.

## Etape 2

Mettre une image bidon

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava3.png)

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava4.png)

## Etape 3

Et voilà on a fait le plus dur on peut récupérer les 2 précieuses informations

![alt tag](https://raw.githubusercontent.com/kekepins/strava/img/img/strava6.png)

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

## Version 1.4 (la dernière)

Cette version intégre le changement dans le mode d'authentification avec Strava (Oauth2).

Auparavant on disposait d'un token illimité poour accéder aux données, c'est maintenant fini la rigolade.

La première fois qu'on accède à Strava une page web est affichée pour authoriser l'accès, il faut bien sur répondre "oui c'est ok j'ai confiance tout va bien se passer"
NB : pour les utilisateur des versions précédentes il faut maintenant récupérer sur le site strava le clientid et le secret (vs le token) pour mettre dans le fichier de configuration.

## Version 1.3

* erreur dans l'import avec strava

## Version 1.2

* Des sports non gérés peuvent faire planter l'import (yoga, muscu ...)
* Le nombre d'entrainements trouvés dans movescount ne correspond pas à la limite fixé

## Version 1.1

Cette version rajoute :
* support de movescount
* ajout de l'url de l'entrainement source en commentaire privé de kikourou
* les entrainements ayant la même date dans kirourou ne sont plus coché 

## Version 1.0

Première version uniquement l'import depuis strava.

# Installation

## Dézipper dans un répertoire
On télécharge la dernière version de l'application ici : https://github.com/kekepins/strava/raw/master/dist/kikstrava_1.4.0.zip

On dézippe dans un répertoire.

## Démarrage double clic sur : kikstrava.bat

Attention avant de faire ça **il faut configurer le fichier stravakik.conf**

# Configuration

Il faut éditer le fichier 
**conf/stravakik.conf** et renseigner les informations :

* kik.user : user kikourou (celui du site)
* kik.password : psw kikourou (celui du site) 

* strava.clientid : le code client récupéré sur le site (voir le début)
* strava.secret : le secret récupéré sur le site (voir le début)

* movescount.email : email identifiant movescount 
* movescount.userkey : userkey movescount (voir plus haut pour le trouver)

 si besoin on peut configurer un proxy :
* proxy= true : le proxy est activé, si sait pas ce que c'est on met rien
* proxy.url : l'url du proxy
* proxy.port : le port du proxy

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
