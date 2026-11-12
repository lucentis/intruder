# Intruder — Documentation du projet

**Version documentaire :** 0.1  
**Périmètre :** V1 Android  
**Statut :** cahier des charges fonctionnel consolidé ; conception visuelle à réaliser.

## Objectif

Ce dossier centralise les décisions prises pour concevoir Intruder. Il sert de référence pendant la conception et l'implémentation afin d'éviter les décisions contradictoires et la perte de contexte.

## Documents

| Fichier | Contenu | Statut |
|---|---|---|
| `01-cahier-des-charges-fonctionnel.md` | Objectifs, périmètre, parcours, écrans, règles fonctionnelles, confidentialité et limites V1 | Consolidé à partir des décisions validées |
| `02-conception-visuelle.md` | Direction artistique, design system, maquettes et spécifications UI | À compléter ensemble |

## Contexte technique connu

- Application Android native en Kotlin.
- Interface prévue avec Jetpack Compose.
- Dépôt de production : `intruder`.
- `intruder-proto` est un prototype de faisabilité distinct ; il ne doit pas être modifié dans le cadre du projet final.
- Appareil de validation actuel : Samsung Galaxy A26 5G, Android 16 / One UI 8.5.
- Niveau minimal envisagé : Android 12 (API 31).
- Les versions exactes de Kotlin, Gradle, Android Gradle Plugin, `compileSdk` et `targetSdk` seront fixées au démarrage du dépôt final avec des versions stables compatibles.

## Convention de mise à jour

- Une décision validée est reportée dans le document concerné.
- Une idée non validée est marquée **À décider** ou **Proposition** ; elle ne doit pas être présentée comme une exigence.
- Une décision remplacée est mise à jour dans le document de référence afin d'éviter les contradictions.
- Les évolutions hors V1 sont consignées séparément sans élargir silencieusement le périmètre.

## État d'avancement

- [x] Objectif et périmètre fonctionnel V1.
- [x] Galerie et paramètres.
- [x] Parcours de première ouverture.
- [ ] Direction artistique et design system.
- [ ] Maquettes détaillées de tous les écrans.
- [ ] Architecture technique détaillée.
- [ ] Plan de réalisation et stratégie de tests.
