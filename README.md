# 15 avril 2024 une nouvelle version est disponible !!!!

https://github.com/kekepins/strava/raw/master/dist/kikstrava-1.8.0.zip

# Outil d'import des entrainements de Strava vers kikourou (https://www.kikourou.net)

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


# Versions

## Version 1.8 (la dernière, 15 avril 2024)

Problèmes corrigés :
- Nouveau serveur kikourou, nouveaux problèmes
- Ajout de certains sports qui fonctionnaientt pas
- Suppression du support movescount

## Version 1.7 (15 janvier 2020)
Problèmes corrigés :
- Expiration des licences strava
- Sports movescount mal gérés (vtt, course)
- Lecture de la configuration caractêre espace ignoré

## Version 1.6 (25 octobre 2019)

Strava demande d'être plus fin dans les droits d'accés aux données sinon on a une erreur.
Nb, si on a des problèmes on peut avant de lancer supprimer le fichier .stravacode celà déclenchera à nouveau une demande d'autorisation à Strava.
Le plus simple est de configurer complétement l'application.

## Version 1.5

Encore des problèmes de format de données qui changent dans Strava.

## Version 1.4 

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
On télécharge la dernière version de l'application ici : https://github.com/kekepins/strava/raw/master/dist/kikstrava-1.8.0.zip

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

 si besoin on peut configurer un proxy :
* proxy= true/false : le proxy est activé, si sait pas ce que c'est on met rien (valeur false)
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
$  mvn javafx:jlink package
```

Si certains veulent compiler sur autre chose que windows, il doit falloir changer dans le fichier pom.xml

```
		<platform>win</platform>
```	

On peut tenter de mettre mac ou linux, aucune idée si ça marche.
