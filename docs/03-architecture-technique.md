# 03 — Architecture technique

> **Projet :** Intruder  
> **Version :** 0.2  
> **Statut :** architecture cible de la V1, à valider par les essais sur appareil réel

## 1. Objectif du document

Ce document décrit la structure interne de l'application et les responsabilités techniques de ses composants.

Le cahier des charges fonctionnel reste la référence pour les fonctionnalités et les comportements attendus. Pour éviter les doublons, ce document ne les redétaille pas : il explique comment organiser le code pour les implémenter.

## 2. Choix structurants

- Application Android native en Kotlin.
- Interface avec Jetpack Compose.
- Un seul module Gradle `app` pour la V1.
- Architecture organisée en couches `ui`, `domain`, `data` et `platform`.
- Room pour la persistance structurée.
- DataStore pour les préférences.
- Camera2 pour la capture ponctuelle.
- Pas de backend ni de dépendance réseau.

L'objectif est de garder une architecture suffisamment séparée pour être testable, sans introduire prématurément une architecture multi-modules ou une abstraction pour chaque classe.

## 3. Architecture générale

```text
┌─────────────────────────────────────────────┐
│                    UI                       │
│          Compose screens / components       │
└──────────────────────┬──────────────────────┘
                       │
┌──────────────────────▼──────────────────────┐
│                 ViewModels                  │
│       état d'écran et actions utilisateur   │
└──────────────────────┬──────────────────────┘
                       │
┌──────────────────────▼──────────────────────┐
│                  Domain                     │
│   ProtectionController / EventProcessor    │
│           modèles et cas d'usage            │
└──────────────┬────────────────┬─────────────┘
               │                │
┌──────────────▼───────┐  ┌─────▼─────────────┐
│        Data          │  │     Platform      │
│ Room / DataStore     │  │ Android services │
│ repositories         │  │ receivers/camera │
└──────────────────────┘  └───────────────────┘
```

Règles de dépendance :

- `ui` dépend de `domain`, jamais directement des API Camera2 ou des composants de surveillance.
- `domain` contient les règles d'orchestration et dépend d'interfaces ou de contrats, pas des écrans.
- `data` implémente l'accès aux données.
- `platform` encapsule les API Android spécifiques.
- Les composants Android sont des adaptateurs d'entrée : ils transmettent les événements au domaine et ne contiennent pas la logique métier.

Pour une V1, ces couches peuvent rester dans un seul module et un seul package racine.

## 4. Organisation des packages

Arborescence proposée :

```text
app/src/main/java/fr/lucentis/intruder/
├── IntruderApplication.kt
├── MainActivity.kt
├── di/
│   └── AppContainer.kt
├── domain/
│   ├── model/
│   ├── event/
│   │   ├── EventProcessor.kt
│   │   └── EventRepository.kt
│   └── protection/
│       └── ProtectionController.kt
├── data/
│   ├── database/
│   │   ├── IntruderDatabase.kt
│   │   ├── EventDao.kt
│   │   └── EventEntity.kt
│   ├── preferences/
│   │   └── ProtectionPreferences.kt
│   └── repository/
│       └── RoomEventRepository.kt
├── platform/
│   ├── monitoring/
│   │   └── UnlockMonitorService.kt
│   ├── admin/
│   │   └── IntruderAdminReceiver.kt
│   ├── boot/
│   │   └── BootReceiver.kt
│   └── camera/
│       ├── CameraCapture.kt
│       └── CameraCaptureFactory.kt
└── ui/
    ├── navigation/
    ├── gallery/
    ├── settings/
    ├── components/
    └── theme/
```

Cette arborescence est indicative. Il faut éviter de créer des sous-packages vides ou des interfaces qui n'apportent aucune souplesse réelle.

## 5. Composants Android : adaptateurs d'entrée

### 5.1 `UnlockMonitorService`

Service Android responsable de l'enregistrement temporaire du receiver dynamique qui écoute `ACTION_USER_PRESENT`.

Responsabilités techniques :
- enregistrer le receiver pendant la durée de vie du service ;
- transmettre le callback au composant d'orchestration des événements ;
- désenregistrer le receiver à l'arrêt ;
- ne pas gérer la caméra ni la base de données directement.

Le receiver peut être une instance interne créée par le service. Une classe dédiée n'est pas nécessaire pour ce seul callback.

### 5.2 `IntruderAdminReceiver`

Sous-classe de `DeviceAdminReceiver`.

Responsabilité :
- recevoir `onPasswordFailed()` ;
- déléguer rapidement le traitement à un composant adapté ;
- ne pas effectuer de capture ni de travail asynchrone long dans le callback lui-même.

### 5.3 `FailedUnlockService`

Service court servant de relais entre le callback Device Admin et le traitement applicatif.

Il déclenche le traitement puis s'arrête. Le choix d'un service ordinaire est une hypothèse d'implémentation, pas une garantie Android : son démarrage et l'accès caméra depuis ce contexte devront être testés sur les versions ciblées.

### 5.4 `BootReceiver`

Adaptateur du broadcast de démarrage.

Il demande au contrôleur de protection de réconcilier l'état souhaité avec ce qu'Android autorise à démarrer. Il ne contient pas de logique de décision du statut et ne doit pas lancer de boucle de redémarrage.

## 6. Services du domaine

### 6.1 `EventProcessor`

Orchestrateur commun aux différents points d'entrée d'événements.

Il reçoit une commande contenant au minimum :
- le type d'événement ;
- l'horodatage de détection.

Il coordonne ensuite les dépendances nécessaires : lecture des préférences applicables, demande de capture si nécessaire, puis persistance de l'événement.

Il ne doit pas connaître les écrans Compose ni être appelé directement depuis l'interface pour simuler un événement système.

Pour le rendre testable, ses dépendances doivent pouvoir être remplacées par des implémentations de test, notamment le service de capture et le repository.

### 6.2 `ProtectionController`

Orchestrateur de l'état de protection.

Il coordonne les préférences, les capacités Android et les composants de surveillance. Il expose un état observable que l'interface peut présenter.

Il est le point central pour les opérations de réconciliation :
- au lancement de l'application ;
- au retour de l'application au premier plan ;
- après une modification des préférences ;
- après un retour depuis les paramètres Android ;
- lors d'une tentative de restauration au démarrage de l'appareil.

Il ne faut pas persister un simple booléen `isRunning` comme vérité durable. L'état opérationnel doit être vérifié à partir des composants et des capacités réellement disponibles.

### 6.3 ViewModels

Chaque écran possède un ViewModel chargé de :
- exposer les données nécessaires à l'écran ;
- transformer les actions utilisateur en appels au domaine ;
- gérer les états de chargement et les erreurs présentables ;
- conserver l'état d'interface qui doit survivre aux recompositions.

Les ViewModels ne gèrent ni les callbacks Android, ni Camera2, ni les requêtes SQL.

## 7. Capture : frontière technique

`CameraCaptureFactory` construit une nouvelle instance de `CameraCapture` pour chaque opération.

`CameraCapture` encapsule le cycle de vie Camera2 et retourne un résultat explicite, par exemple :

```kotlin
sealed interface CaptureResult {
    data class Success(val filePath: String) : CaptureResult
    data class Failure(val reason: CaptureFailureReason?) : CaptureResult
}
```

Le résultat ne doit pas dépendre d'une exception non typée utilisée comme flux normal. Les erreurs système connues sont converties en catégories internes ; les détails techniques restent disponibles pour le diagnostic sans exposer de chemins privés dans les logs.

Le traitement est ponctuel : pas de caméra maintenue ouverte entre deux événements. La stratégie de concurrence reste volontairement simple en V1 : aucune file d'attente ni politique de retry applicative. Les collisions ou refus du système doivent produire un résultat d'échec exploitable par `EventProcessor`.

## 8. Persistance et repositories

### 8.1 Room

Room contient les événements persistés. Les entités Room restent dans la couche `data` et ne sont pas utilisées directement dans les ViewModels ou les écrans.

`EventDao` porte les requêtes SQL. `RoomEventRepository` traduit les entités en modèles utilisés par le domaine.

Les requêtes de galerie doivent être paginées et triées chronologiquement en base, plutôt que de charger l'ensemble des événements en mémoire.

### 8.2 DataStore

Un repository de préférences encapsule DataStore. Il expose un flux de préférences et des opérations de mise à jour explicites.

Les préférences représentent les choix de l'utilisateur. Elles ne constituent pas une preuve que les composants Android correspondants sont effectivement actifs.

### 8.3 Fichiers

Le repository d'événements conserve les références vers les fichiers photo privés. La suppression d'un événement coordonne la suppression du fichier et de la ligne Room.

La cohérence entre le système de fichiers et Room n'étant pas transactionnelle, le code doit traiter séparément les deux opérations et tolérer un fichier déjà absent. Il ne faut pas prétendre qu'une transaction Room couvre les opérations sur les fichiers.

## 9. Injection et cycle de vie des dépendances

`IntruderApplication` initialise un `AppContainer` qui construit les dépendances partagées :

- base Room et DAO ;
- repositories ;
- accès DataStore ;
- `EventProcessor` ;
- `ProtectionController` ;
- `CameraCaptureFactory`.

Les composants Android qui doivent accéder à ces dépendances récupèrent le conteneur via l'instance `Application`.

Pour la V1, une injection manuelle explicite est suffisante. Ajouter un framework de dependency injection reste possible plus tard, mais n'est pas nécessaire pour obtenir une séparation testable.

Attention à ne pas conserver dans le conteneur une instance de caméra liée à un `Context` d'écran ou à une Activity. Les dépendances partagées doivent utiliser un contexte applicatif ; les ressources caméra doivent avoir une durée de vie courte et propre à chaque capture.

## 10. Gestion des erreurs

Les erreurs doivent être traitées à la frontière où elles peuvent être comprises :

- `platform` convertit les erreurs Android en résultats ou catégories internes ;
- `data` remonte les erreurs de persistance ;
- `domain` décide de la conséquence sur le traitement et l'état de protection ;
- `ui` transforme les états destinés à l'utilisateur en messages et actions.

Éviter :
- les `catch` génériques qui masquent une erreur ;
- les erreurs silencieusement ignorées ;
- les exceptions utilisées pour représenter des résultats attendus ;
- les messages d'interface construits dans les classes Android ;
- les logs contenant des données personnelles ou des chemins de fichiers privés.

Les erreurs doivent conserver assez d'information pour le diagnostic, sans coupler le domaine aux classes d'exception propres à Android.

## 11. Cycle de vie et points de fragilité

Les receivers dynamiques sont liés à la durée de vie de leur enregistrement. Si le processus est tué, l'enregistrement réalisé par le service disparaît également.

L'architecture ne doit donc pas considérer l'état sauvegardé dans DataStore comme une preuve de fonctionnement. `ProtectionController` doit pouvoir réconcilier l'état au prochain point d'entrée fiable.

Les points suivants sont des risques techniques à vérifier sur appareil :
- restrictions de démarrage de services depuis les callbacks système ;
- accès à Camera2 depuis le contexte réel de traitement ;
- comportement après destruction du processus ;
- comportement après redémarrage ;
- restrictions propres aux versions Android et aux couches constructeur.

Aucun mécanisme de contournement des restrictions système, watchdog ou redémarrage permanent n'est prévu.

## 12. Stratégie de tests

### Tests unitaires
- `EventProcessor` avec repository et capture simulés ;
- `ProtectionController` avec états de permissions et services simulés ;
- conversion des résultats Camera2 en résultats métier ;
- règles de réconciliation et gestion des erreurs.

### Tests d'intégration
- repository Room et requêtes paginées ;
- lecture et mise à jour DataStore ;
- coordination de la persistance avec la suppression des fichiers ;
- traitement complet d'un événement avec capture simulée.

### Tests instrumentés sur appareil réel
- callbacks système réellement reçus ;
- démarrage et arrêt des services ;
- comportement Camera2 dans le contexte de traitement ;
- reprise après destruction du processus et redémarrage ;
- révocation et restauration des permissions.

Les tests instrumentés doivent être consignés avec le modèle d'appareil, la version Android et la version de l'application. Un succès sur un appareil ne démontre pas une compatibilité universelle.

## 13. Ordre d'implémentation technique

1. Créer le projet Android et valider la chaîne Kotlin/Gradle sur l'appareil cible.
2. Mettre en place `AppContainer`, Room et DataStore.
3. Implémenter les contrats de repository et tester `EventProcessor` avec une capture simulée.
4. Valider séparément le callback `ACTION_USER_PRESENT`.
5. Valider séparément le callback `onPasswordFailed()` et son relais de traitement.
6. Intégrer Camera2 et vérifier les contraintes d'accès réelles.
7. Implémenter `ProtectionController` et ses réconciliations.
8. Ajouter la restauration au démarrage et les vérifications de cycle de vie.
9. Construire les écrans Compose à partir du cahier des charges et du document de conception visuelle.

L'ordre met les inconnues Android les plus risquées au début, avant d'investir dans l'interface complète.
