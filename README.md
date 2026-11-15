# Nouvelle version : import depuis Garmin Connect !!!!

**[Télécharger KikStrava 1.9.0 pour Windows](https://github.com/kekepins/strava-movescount-to-kikourou/raw/master/dist/kikstrava-1.9.0.zip)**

Dézippe l'archive, renseigne `conf/stravakik.conf`, puis lance `KikStrava.exe`.
Aucun Java n'est requis.

# Outil d'import des entrainements de Strava ou Garmin vers kikourou (https://www.kikourou.net)

Transfert des entrainements de **Strava** ou **Garmin Connect** vers Kikourou.

Les données Strava sont lues grâce à l'api REST, et envoyées dans kikourou comme le ferait un navigateur.
Pour Strava on utilise des api pas documentées mais qui permettent aussi d'accéder aux entrainements.
Pour Garmin, on utilise l'**API personnelle Garmin Connect** (clé API personnelle) : la clé est échangée
contre un jeton d'accès, puis on récupère la liste des activités en REST (JSON).

# Comment configurer Strava ?

Pour autoriser KikStrava à accéder à tes activités Strava, crée une application Strava afin
de récupérer son identifiant client (`client ID`) et son secret (`client secret`).

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


# Comment configurer Garmin ?

L'import depuis Garmin utilise une **clé API personnelle** Garmin Connect. Une seule information
est nécessaire : cette clé, à renseigner dans `garmin.apikey` du fichier de configuration.

## Étape 1 — Générer la clé API personnelle

Connecte-toi à ton compte Garmin, puis va dans la section **« Clé API personnelle »**
(« Personal API Key ») de ton compte développeur Garmin Connect.

Documentation Garmin : https://developer.garmin.com/gc-developer-program/personal/

Génère une clé : elle ressemble à `GCDC_xxxxxxxx-....` (une longue chaîne).

## Étape 2 — Renseigner la clé dans la configuration

Dans `conf/stravakik.conf`, renseigner :

```
garmin.apikey=GCDC_xxxxxxxx-....
```

C'est tout. Au lancement, sélectionne **Garmin** dans l'application, choisis la période,
clique sur « Voir les entrainements » puis « Importer dans Kikourou ».

> Remarque sur le fonctionnement : l'application échange automatiquement la clé API contre un
> jeton d'accès OAuth2 (endpoint `personal-api-key/token`), puis interroge l'API
> `activitylist-service` pour lister tes activités. Les types de sport Garmin (course, trail,
> vélo, VTT, natation…) sont convertis vers les sports correspondants de kikourou.

> ⚠️ La clé API personnelle est un **secret** : ne la partage pas et ne la commite pas dans un
> dépôt public. En cas de fuite, régénère-la depuis ton compte Garmin.


# Versions

## Version 1.9 (la dernière)

Nouveautés :
- **Ajout de l'import depuis Garmin Connect** via la clé API personnelle
- Sélection de la source (Strava ou Garmin) dans l'interface
- Scripts de lancement et de construction du livrable pour macOS (`run.sh`, `build-app.sh`)
  et Windows (`run.bat`, `build-app.bat`)

## Version 1.8 (15 avril 2024)

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

# Synchronisation quotidienne (Windows)

Le livrable Windows autonome inclut `sync-daily.bat` et `install-daily-sync.ps1`.
Configure d'abord `conf/stravakik.conf` avec Kikourou et au moins une source. Pour Strava, ouvre une première fois
l'application graphique et autorise l'accès au compte.

Depuis le dossier contenant `KikStrava.exe`, enregistre la tâche quotidienne à 06:00 :

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\install-daily-sync.ps1 -Source Garmin
```

La source par défaut est Garmin. Pour utiliser Strava ou les deux sources, choisis
`-Source Strava` ou `-Source Both`. Pour choisir une autre heure, ajoute par exemple
`-At "20:00"`.
Le lancement manuel accepte aussi `KikStrava.exe --sync --source=garmin`,
`--source=strava` ou `--source=both`.
La tâche s'exécute sous le compte Windows courant quand celui-ci est connecté; si l'ordinateur
était indisponible à l'heure prévue, l'exécution est reportée à son prochain démarrage.
Les synchronisations sont journalisées dans `%LOCALAPPDATA%\KikStrava\sync.log`.

Pour tester manuellement l'import Garmin sur les trois activités les plus récentes,
exécute `KikStrava.exe --test-import=3`. Les activités déjà présentes sont ignorées;
les nouvelles sont vérifiées dans le calendrier Kikourou. La commande en lecture seule
`KikStrava.exe --verify-import=3` permet de refaire uniquement la vérification.
Pour vérifier en lecture seule les dernières activités Strava sans les importer,
exécute `KikStrava.exe --verify-strava=3` (ou remplace `3` par le nombre à vérifier).

La synchronisation examine les 30 derniers jours et autorise plusieurs activités le même jour.
Elle évite les réimportations en comparant la date, la durée et la distance (arrondie à 100 m),
y compris entre Strava et Garmin.
Pour supprimer la tâche, exécute :

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\install-daily-sync.ps1 -Uninstall
```

# Configuration

Il faut éditer le fichier 
**conf/stravakik.conf** et renseigner les informations :

* kik.user : user kikourou (celui du site)
* kik.password : psw kikourou (celui du site) 

* strava.clientid : le code client récupéré sur le site (voir le début)
* strava.secret : le secret récupéré sur le site (voir le début)

* garmin.apikey : la clé API personnelle Garmin (voir « Comment configurer Garmin ? »)

Il faut configurer **au moins une source** : Strava (clientid + secret) **ou** Garmin (apikey).

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

## Compilation

```
mvn clean package
```

Cela produit `target/kikstrava-<version>-jar-with-dependencies.jar`.

Le classifier natif JavaFX se choisit avec la propriété `platform` (par défaut `mac` dans le pom) :

```
mvn -Dplatform=win clean package      # Windows
mvn -Dplatform=mac clean package      # macOS Intel / JVM x86_64
mvn -Dplatform=mac-aarch64 ...        # macOS Apple Silicon (JVM arm64)
mvn -Dplatform=linux ...              # Linux
```

## Lancer l'application (avec la config locale)

- macOS / Linux : `./run.sh`
- Windows : `run.bat`

Ces scripts compilent, puis lancent l'application en chargeant `conf_local/stravakik.conf`
et en fournissant JavaFX via `--module-path` (classifier natif détecté automatiquement).

## Construire un livrable autonome (runtime embarqué, sans Java installé)

- macOS : `./build-app.sh` → `target/dist/KikStrava.app` et `KikStrava-*.dmg`
- Windows : `build-app.bat` → `target/dist/KikStrava\KikStrava.exe` (+ `.msi` si WiX Toolset installé)

Ces scripts utilisent `jpackage` (fourni par le JDK ≥ 17). La configuration à remplir par
l'utilisateur se trouve à la racine du livrable sous `conf/stravakik.conf`.

> Astuce repo d'entreprise : si Maven est configuré avec un `settings.xml` spécifique, renseigne-le
> via la variable `MVN_SETTINGS` en haut des scripts `.bat`, ou l'option `-s` pour les scripts `.sh`.
