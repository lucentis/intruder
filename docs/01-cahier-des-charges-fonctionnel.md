# Intruder — Cahier des charges fonctionnel

**Version :** 0.1  
**Périmètre :** Version 1 (V1)  
**Statut :** consolidation des décisions fonctionnelles et UX validées.

---

## 1. Présentation du projet

### 1.1 Objectif

Intruder est une application Android de protection locale qui enregistre certains événements liés à l'accès au téléphone :

- les déverrouillages réussis ;
- les tentatives échouées de saisie du code PIN Android.

Selon les réglages de l'utilisateur et les capacités disponibles, l'application tente de prendre une photo avec la caméra avant lors de chaque événement détecté. Les événements sont conservés dans une galerie privée à l'intérieur de l'application.

### 1.2 Principes du produit

- Fonctionnement local, sans compte utilisateur.
- Aucune synchronisation ni infrastructure serveur.
- Pas de publicité, d'analytics ou de télémétrie.
- Les événements sont enregistrés même lorsqu'aucune photo n'est disponible.
- La surveillance et la capture photo sont deux réglages indépendants.
- Les limites imposées par Android doivent être expliquées clairement.
- Une autorisation refusée ne doit pas empêcher l'utilisation des autres fonctionnalités disponibles.

### 1.3 Plateforme et contexte

- Application Android native en Kotlin.
- Interface prévue avec Jetpack Compose.
- Appareil de validation actuel : Samsung Galaxy A26 5G, Android 16 / One UI 8.5.
- Niveau minimal envisagé : Android 12 (API 31).
- Le dépôt final `intruder` est distinct du prototype `intruder-proto`, qui reste un laboratoire de faisabilité.
- Les versions exactes des outils Android seront choisies au démarrage du dépôt final.

---

## 2. Périmètre fonctionnel V1

### 2.1 Inclus

- Détection des déverrouillages réussis.
- Détection des tentatives échouées de code PIN Android.
- Tentative de capture avec la caméra avant pour chaque événement si la capture est activée et autorisée.
- Enregistrement local de chaque événement, même si la photo est ignorée ou échoue.
- Galerie en grille et en liste.
- Filtres par type et période.
- Consultation du détail d'un événement.
- Suppression individuelle et multiple.
- Paramètres de protection, notifications, stockage et informations sur l'application.
- Notifications limitées aux erreurs techniques.
- Assistant de première ouverture et d'activation.

### 2.2 Hors périmètre V1

- Verrouillage de l'application Intruder par PIN.
- Authentification biométrique.
- Export ou partage des événements et photos.
- Synchronisation, compte utilisateur ou sauvegarde cloud.
- Statistiques et tableaux de bord.
- Historique technique séparé.
- Génération et stockage de fichiers miniatures distincts.
- Choix de caméra ou de qualité photo par l'utilisateur.
- Détection des échecs biométriques ou des schémas de déverrouillage.
- Identification de la méthode utilisée pour un déverrouillage réussi.

---

## 3. Événements détectés

### 3.1 Déverrouillage réussi

- Détecté via l'événement Android `ACTION_USER_PRESENT`.
- Tous les moyens de déverrouillage sont traités de la même manière.
- Intruder ne cherche pas à déterminer si le téléphone a été déverrouillé par PIN, biométrie, schéma ou autre méthode.
- Type : `UNLOCK_SUCCESS`.

### 3.2 Tentative échouée

- Détection des échecs de saisie du code PIN via le mécanisme Device Admin et `DeviceAdminReceiver.onPasswordFailed()`.
- La V1 ne détecte pas les échecs biométriques ni les échecs de schéma.
- Type : `UNLOCK_FAILED`.

### 3.3 Un événement par callback

Chaque callback détecté produit son propre événement. Il n'y a pas d'agrégation ni de déduplication fonctionnelle.

---

## 4. Règles de capture photo

### 4.1 Comportement général

- Caméra avant uniquement.
- Une seule tentative de capture au maximum par événement.
- Capture déclenchée uniquement lorsqu'un événement est traité et que la capture automatique est activée.
- La caméra n'est pas utilisée en continu.
- Aucune option utilisateur de sélection de caméra ou de qualité en V1.

### 4.2 États de capture

| État | Signification |
|---|---|
| `SUCCESS` | Une photo a été prise et enregistrée. |
| `FAILED` | Une tentative a échoué pour une raison technique. |
| `SKIPPED` | La capture était désactivée ; aucune tentative n'a été effectuée. |

Un événement doit toujours être enregistré, quel que soit le résultat de la capture.

### 4.3 Traitement

- Capture activée : tenter la capture, puis enregistrer l'événement avec la photo si elle existe.
- Capture désactivée : enregistrer l'événement avec l'état `SKIPPED`.
- Capture en échec : enregistrer l'événement sans photo, avec l'état `FAILED` et un motif si celui-ci est connu.
- Une erreur de capture ne doit pas empêcher l'enregistrement de l'événement.
- En cas d'échec d'enregistrement en base, signaler l'erreur technique ; ne pas prétendre que l'événement a été sauvegardé.
- Pas de nouvelle tentative en V1.
- Pas de vérification préalable arbitraire de l'espace disque : la capture est tentée et l'erreur est traitée si le système la renvoie.

### 4.4 Concurrence et indisponibilité caméra

Intruder tente chaque capture sans file d'attente, délai, attente ou nouvelle tentative applicative. Si Android ou la caméra refuse une capture (caméra occupée, ressource indisponible, etc.), le résultat est `FAILED` et l'événement est conservé.

### 4.5 Stockage des photos

- Stockage privé interne à l'application (`filesDir`).
- Pas d'enregistrement dans la galerie média Android.
- Pas d'accès depuis les applications de fichiers ordinaires.
- Une seule photo originale par événement ; aucun fichier miniature séparé en V1.
- Pas de métadonnées GPS.
- Le timestamp de l'événement stocké en base est la référence temporelle.

### 4.6 Photo de test

Pendant l'installation, si l'autorisation caméra est accordée, l'utilisateur peut tester la caméra avant. La photo de test est temporaire, affichée pour vérification puis supprimée. Elle ne crée aucun événement.

---

## 5. Enregistrement local et suppression

### 5.1 Données d'un événement

Le modèle fonctionnel d'un événement comprend :

- identifiant ;
- type (`UNLOCK_SUCCESS` ou `UNLOCK_FAILED`) ;
- date et heure ;
- chemin de photo nullable ;
- état de capture (`SUCCESS`, `FAILED`, `SKIPPED`) ;
- motif d'erreur nullable.

La base locale prévue est Room, avec index sur le timestamp pour le tri chronologique.

### 5.2 Suppression

- Suppression individuelle depuis le détail.
- Suppression multiple depuis le mode sélection de la galerie.
- Confirmation obligatoire avant suppression définitive.
- Pour une suppression multiple, afficher le nombre d'événements concernés.
- La suppression de l'événement en base reste prioritaire : si le fichier photo est absent ou si sa suppression échoue, l'événement doit tout de même être supprimé de la base.
- Signaler une erreur technique si le fichier n'a pas pu être supprimé.
- Pas de corbeille ni de récupération en V1.

---

## 6. Navigation et structure générale

Deux destinations principales, accessibles depuis une barre de navigation inférieure :

1. **Galerie** — destination affichée après l'activation et écran principal de l'application.
2. **Paramètres** — accès aux quatre sections de configuration.

La barre de navigation inférieure reste disponible dans les destinations principales. Les sous-écrans disposent d'une navigation de retour adaptée.

### 6.1 En-tête de la galerie

- Nom « Intruder » aligné à gauche.
- Badge d'état de protection aligné à droite, sur la même ligne.
- Pas de raccourci Paramètres dans cet en-tête, car Paramètres possède son propre onglet.

---

## 7. Galerie

### 7.1 Vue par défaut

- Grille de trois colonnes.
- Vignettes carrées.
- Événements du plus récent au plus ancien.
- Groupement par date.
- En-têtes de groupes sans compteur : « Aujourd'hui », « Hier », puis dates.
- Aucun compteur d'événements affiché à côté des dates.

### 7.2 Badges de type

Chaque vignette comporte un petit badge indiquant le type :

- Succès : style neutre et discret.
- Échec : couleur d'alerte pour faciliter le repérage.

Le badge ne doit pas masquer excessivement la photo.

### 7.3 Vignettes sans photo

- `SKIPPED` : icône indiquant que la capture a été ignorée/désactivée.
- `FAILED` : icône indiquant un échec de capture.
- Le badge du type d'événement reste visible.
- Utiliser une icône, pas une grande illustration.

### 7.4 Filtres

Les filtres restent visibles au-dessus de la galerie :

- Type : Tous / Succès / Échecs.
- Période : Aujourd'hui / 7 derniers jours / 30 derniers jours / Personnalisée.
- Les filtres sont cumulables.
- Les filtres actifs ont un fond légèrement coloré ; les filtres inactifs restent neutres.
- Un contrôle permet de réinitialiser les filtres et un indicateur discret signale qu'un filtrage est actif.
- Le contrôle de changement grille/liste se trouve sur la même ligne que les filtres, à droite.

### 7.5 Mode liste

Une ligne par événement, avec :

- miniature de la photo ou icône adaptée ;
- type d'événement ;
- date et heure ;
- état de capture, de manière discrète.

Le mode liste conserve les filtres et le tri chronologique.

### 7.6 Préférence d'affichage

- Grille par défaut lors de la première utilisation.
- Le dernier mode choisi (grille ou liste) est mémorisé dans les préférences locales.
- Cette préférence n'est pas exposée comme un réglage visible dans la page Paramètres.

### 7.7 Détail d'un événement

Le détail affiche :

1. Photo en grand si disponible.
2. Badge Succès / Échec.
3. Date et heure exactes.
4. État de capture : réussie, ignorée ou échouée.
5. Motif précis si une erreur est connue.
6. Action Supprimer.

Pas de navigation précédent/suivant en V1. Le retour ramène à la galerie.

### 7.8 Sélection multiple

- Appui long sur une vignette pour entrer en mode sélection.
- Le titre devient « X sélectionnés ».
- Action « Tout sélectionner » limitée aux résultats correspondant aux filtres courants.
- Action « Supprimer ».
- Action « Annuler » pour quitter le mode sélection.
- Confirmation avant suppression définitive.

### 7.9 Galerie vide

État vide simple comprenant :

- une icône ;
- le titre « Aucun événement pour le moment » ;
- le texte « Les événements détectés apparaîtront ici. » ;
- un accès direct aux paramètres de protection.

---

## 8. Badge d'état de protection

Le badge est affiché dans l'en-tête de la galerie, à droite du nom Intruder.

| État | Condition |
|---|---|
| `Désactivé` | Protection globale désactivée. |
| `Aucun` | Protection globale active, mais aucune surveillance individuelle activée. |
| `Succès` | Surveillance des déverrouillages réussis seule, opérationnelle. |
| `Échecs` | Surveillance des PIN échoués seule, opérationnelle. |
| `Actif` | Toutes les surveillances demandées sont opérationnelles. |
| `Attention` | Au moins une fonctionnalité demandée ne fonctionne pas à cause d'un problème technique ou d'une autorisation manquante. |

Une fonctionnalité volontairement désactivée par l'utilisateur n'est pas considérée comme une panne.

Si le badge indique « Attention », l'utilisateur peut le toucher pour voir les fonctionnalités concernées et les actions possibles.

La capture photo est distincte des surveillances : son activation/désactivation ne change pas le type de surveillance sélectionné, mais un problème affectant une fonctionnalité demandée doit être signalé.

---

## 9. Paramètres

La page principale Paramètres présente quatre sections sous forme de cartes :

1. Protection.
2. Notifications.
3. Stockage.
4. À propos.

### 9.1 Protection

Trois blocs :

**Protection globale**
- Interrupteur principal pour mettre en pause ou reprendre la surveillance.
- Désactiver la protection globale arrête les surveillances et aucun événement n'est enregistré.
- Les préférences individuelles sont conservées.

**Surveillance**
- Interrupteur « Déverrouillages réussis ».
- Interrupteur « Codes PIN échoués ».

**Capture photo**
- Interrupteur « Photos automatiques ».

Les réglages de surveillance et de capture sont indépendants. La capture peut rester configurée même si les surveillances sont désactivées.

Les autorisations sont affichées près des fonctionnalités concernées :
- état Administrateur de l'appareil près des tentatives PIN échouées ;
- état de l'autorisation caméra près des photos automatiques.

Si une autorisation est retirée dans Android :
- afficher un avertissement sous la fonctionnalité concernée ;
- proposer « Ouvrir les paramètres Android » ;
- désactiver uniquement la fonctionnalité concernée ;
- conserver les autres fonctionnalités opérationnelles ;
- ne pas relancer automatiquement et de manière insistante une demande d'autorisation.

### 9.2 Notifications

- Un interrupteur « Notifications techniques ».
- Affichage de l'état de l'autorisation Android.
- Un seul canal Android : « Alertes techniques ».
- Les notifications concernent uniquement les problèmes techniques, jamais les événements normaux.
- Notifications silencieuses par défaut : pas de son ni de vibration.
- Aucun résumé quotidien.

### 9.3 Stockage

Afficher :
- espace utilisé par les photos et les données de l'application ;
- nombre total d'événements ;
- mention indiquant que les données restent sur le téléphone ;
- accès à la galerie pour supprimer les événements et leurs photos.

Pas d'export, de partage ou de sauvegarde cloud en V1.

### 9.4 À propos

Afficher :
- logo et nom Intruder ;
- numéro de version ;
- description courte ;
- mentions légales ;
- politique de confidentialité ;
- licences des bibliothèques utilisées.

---

## 10. Notifications techniques

### 10.1 Principe

Une notification est émise pour chaque occurrence d'une erreur technique. Pas de regroupement, de limitation de fréquence ou de résumé en V1.

### 10.2 Catégories

- Échec de capture.
- Permission révoquée ou manquante.
- Surveillance interrompue.
- Espace de stockage insuffisant.
- Erreur technique interne.

### 10.3 Confidentialité

Le contenu visible sur l'écran verrouillé reste générique et ne révèle pas de détails sensibles.

Exemple :
- Titre : « Problème technique détecté ».
- Message : « Une fonctionnalité de protection a rencontré une erreur. Ouvrez Intruder pour en savoir plus. »

Les détails sont consultables dans l'application :
- erreurs liées à un événement : dans le détail de l'événement ;
- erreurs générales : avertissement dans les paramètres tant que le problème persiste.

Pas d'historique technique séparé en V1.

### 10.4 Permission notifications

Sur Android 13 et versions ultérieures, l'autorisation `POST_NOTIFICATIONS` est demandée pendant la première ouverture. En cas de refus, la protection continue ; les erreurs sont traitées et enregistrées, mais les notifications peuvent ne pas être visibles.

---

## 11. Première ouverture et activation

L'assistant est affiché lors de la première utilisation. Il n'est pas rejoué à chaque lancement.

### Étape 1 — Bienvenue

Présenter :
- les fonctions de surveillance ;
- les captures photo automatiques ;
- le stockage local et l'absence de compte/serveur ;
- les limites et autorisations Android.

Bouton : « Commencer ». Aucune autorisation n'est demandée sur cet écran.

### Étape 2 — Administrateur de l'appareil

Expliquer que cette autorisation permet de détecter les tentatives de code PIN échouées. Elle ne permet pas à Intruder de lire le code PIN ni de déverrouiller le téléphone.

Actions : « Autoriser » et « Plus tard ».

Si refusée, l'installation continue et la surveillance des PIN échoués reste désactivée.

### Étape 3 — Caméra

Expliquer que la caméra avant est utilisée uniquement lors d'un événement détecté, si la capture automatique est activée ; elle n'est pas utilisée en continu.

Actions : « Autoriser » et « Plus tard ».

Si refusée, la surveillance reste possible ; les événements sont enregistrés avec `SKIPPED` lorsque la capture est désactivée.

### Étape 4 — Test caméra

Si l'autorisation est accordée :
- proposer « Tester la caméra » ;
- afficher la photo capturée ;
- permettre de continuer même si le test échoue ;
- supprimer la photo de test après vérification.

Si l'autorisation est refusée, cette étape est sautée.

### Étape 5 — Notifications et vérification

Sur Android 13+ :
- expliquer l'usage des notifications techniques ;
- proposer « Autoriser les notifications » et « Plus tard ».

Puis vérifier l'état réel des fonctionnalités demandées. Présenter les résultats sous forme de liste avec état et explication.

### Étape 6 — Récapitulatif et activation

Afficher les réglages et l'état réel :
- Déverrouillages réussis : activés par défaut.
- Tentatives de PIN échouées : activées si l'autorisation Administrateur est accordée.
- Photos automatiques : activées si la caméra est autorisée.
- Notifications techniques : activées si l'autorisation est accordée.

Les fonctionnalités dont l'autorisation a été refusée restent désactivées. Le refus d'une autorisation ne bloque pas l'activation des autres fonctions.

Bouton principal : « Activer la protection ».

Au toucher :
- enregistrer les préférences ;
- démarrer les surveillances réellement disponibles ;
- ouvrir directement la galerie.

### Préférences initiales

- Protection globale : OFF jusqu'à la confirmation finale.
- Surveillance des déverrouillages réussis : ON par défaut.
- Surveillance des PIN échoués : ON par défaut, sous réserve de l'autorisation Administrateur.
- Photos automatiques : ON par défaut, sous réserve de l'autorisation caméra.
- Notifications techniques : ON si l'autorisation est accordée.

---

## 12. Cycle de vie Android et limites connues

Intruder doit respecter les restrictions du système Android et ne pas tenter de les contourner.

- Le service de surveillance des déverrouillages utilise un service Android avec un receiver dynamique pour `ACTION_USER_PRESENT`.
- Les échecs de PIN sont reçus par le composant Device Admin.
- Le traitement des événements et la capture sont partagés autant que possible entre les deux chemins.
- Un composant de démarrage peut tenter de restaurer la surveillance après un redémarrage, dans la limite des restrictions Android.
- Après un redémarrage, l'application peut demander à l'utilisateur d'ouvrir Intruder pour vérifier l'état réel de la protection.
- Si le processus est tué, le receiver dynamique disparaît et la surveillance concernée peut s'interrompre jusqu'à la prochaine réconciliation.
- Après un arrêt forcé par l'utilisateur ou Android, Intruder ne cherche pas à contourner le blocage du système ; la vérification est faite au prochain lancement manuel.
- Pas de watchdog, boucle de redémarrage, polling ou surveillance périodique artificielle.
- Le fonctionnement en arrière-plan et les restrictions de caméra/service doivent être vérifiés sur l'appareil cible.

Message d'information envisagé :

> Après le redémarrage de votre téléphone, ouvrez Intruder pour vérifier que toutes les fonctionnalités de protection sont bien actives.

Ces contraintes sont des limites techniques connues, pas des promesses de disponibilité absolue.

---

## 13. Confidentialité et sécurité

### 13.1 Données

- Base Room et photos dans le stockage privé de l'application.
- Pas de réseau ni de permission Internet dans le manifeste.
- Pas de GPS ni de métadonnées de localisation.
- Pas de synchronisation, compte, cloud, analytics ou publicité.
- Sauvegarde Android désactivée afin d'éviter la restauration de données après désinstallation/réinitialisation.
- Pas de chiffrement applicatif supplémentaire prévu en V1 ; la protection standard du stockage Android est utilisée.

### 13.2 Permissions prévues

- `CAMERA`.
- Composant Device Admin pour la détection des échecs PIN.
- `POST_NOTIFICATIONS` sur Android 13+.

Aucune permission de localisation, contacts, fichiers partagés, microphone ou Internet n'est prévue.

### 13.3 Journaux techniques

Ne jamais inscrire dans les logs : photos ou contenu des photos, chemins de fichiers personnels, données personnelles ou informations révélant le contenu d'un événement.

Les logs peuvent contenir uniquement les éléments techniques nécessaires : type d'événement, date/heure, état de capture, catégorie/code d'erreur et informations strictement nécessaires au diagnostic.

---

## 14. Principes d'interface et d'expérience utilisateur

Les choix visuels détaillés restent à définir dans `02-conception-visuelle.md`.

Principes fonctionnels déjà validés :
- interface simple, lisible et mobile-first ;
- navigation limitée à Galerie et Paramètres ;
- galerie comme écran d'accueil ;
- informations importantes visibles sans surcharge ;
- badges d'état discrets ;
- filtres accessibles directement ;
- erreurs expliquées près de la fonctionnalité concernée ;
- aucune permission demandée sans explication ;
- refus d'une permission non bloquant ;
- pas de statistiques ni d'éléments décoratifs inutiles.

---

## 15. Points restant à concevoir ou préciser

Les éléments suivants ne sont pas encore des décisions visuelles finales :

- identité graphique et direction artistique ;
- palette de couleurs et thèmes ;
- typographie et échelle de texte ;
- formes, rayons, élévations et espacements ;
- style des icônes ;
- composants Compose réutilisables ;
- maquettes détaillées de chaque écran ;
- états de chargement, erreurs, confirmations et interactions ;
- règles d'accessibilité ;
- comportement précis des filtres personnalisés et du sélecteur de dates ;
- détails des sous-écrans et transitions.

Les détails d'architecture, de schéma Room final, de gestion des erreurs et de stratégie de tests doivent également être consolidés dans une documentation technique dédiée avant l'implémentation.

---

## 16. Critères de validation fonctionnelle V1

La V1 pourra être considérée comme fonctionnellement conforme lorsque :

- les événements pris en charge sont détectés dans les conditions Android autorisées ;
- chaque événement détecté est enregistré, même sans photo ;
- une capture désactivée produit `SKIPPED` ;
- une capture en erreur produit `FAILED` et n'empêche pas l'enregistrement de l'événement ;
- la galerie trie les événements du plus récent au plus ancien ;
- les filtres Type et Période sont cumulables ;
- grille/liste conserve le mode choisi ;
- le détail affiche les informations disponibles ;
- les suppressions sont confirmées et cohérentes entre base et fichiers ;
- les préférences individuelles sont conservées lorsque la protection globale est mise en pause ;
- le badge reflète l'état réel par rapport aux préférences ;
- les autorisations refusées ne bloquent pas les fonctionnalités indépendantes ;
- les notifications ne concernent que les erreurs techniques ;
- aucune donnée n'est envoyée à un serveur ;
- les limites Android sont présentées sans promesse irréaliste.

---

## 17. Historique documentaire

| Version | Évolution |
|---|---|
| 0.1 | Première consolidation des décisions fonctionnelles et UX V1 validées pendant la phase de conception. |
