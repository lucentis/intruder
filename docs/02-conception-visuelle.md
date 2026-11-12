# Intruder — Conception visuelle et design system

**Version :** 0.2  
**Statut :** direction artistique et fondations du design system définies — conception détaillée en cours.  
**Référence fonctionnelle :** `01-cahier-des-charges-fonctionnel.md`

> Ce document formalise les décisions visuelles prises pendant la conception des maquettes. Les éléments indiqués « À définir » ne constituent pas encore des décisions validées.

---

## 1. Objectif

Définir l'identité visuelle d'Intruder et spécifier chaque écran avant son implémentation en Jetpack Compose.

Le résultat attendu :

- une direction artistique cohérente ;
- un design system documenté ;
- une palette de couleurs et des tokens réutilisables ;
- une échelle typographique ;
- des règles d'espacement et de dimensions ;
- des composants réutilisables ;
- des maquettes de référence ;
- les états et interactions de chaque composant ;
- des règles d'accessibilité et d'adaptation aux tailles d'écran.

Les maquettes SVG servent de référence visuelle. Elles ne remplacent pas les spécifications de comportement et d'accessibilité qui seront définies pour l'implémentation Android.

---

## 2. Principes établis

- Application mobile-first.
- Interface claire, simple et lisible.
- Galerie comme écran principal.
- Navigation basse avec deux destinations : Galerie et Paramètres.
- Pas de dashboard, de statistiques ou de compteurs décoratifs.
- Informations de protection visibles, mais sans prendre trop de place.
- Les filtres sont directement visibles dans la galerie.
- Les badges de type et de protection restent discrets.
- Les erreurs et autorisations manquantes sont expliquées au plus près de la fonctionnalité.
- Pas de surcharge visuelle.
- Les réglages doivent être compréhensibles sans nécessiter de documentation.
- Les éléments interactifs doivent être immédiatement identifiables.
- Les états ne doivent jamais être communiqués uniquement par la couleur.

---

## 3. Direction artistique

<ins>Statut : validée.</ins>

### 3.1 Personnalité visuelle

La direction artistique retenue est :

**« Sécurité discrète »**

Intruder doit inspirer confiance, fiabilité et sérénité.

L'application doit évoquer un outil de protection sérieux, sans donner l'impression d'un logiciel de surveillance agressif ou anxiogène.

Les caractéristiques recherchées :

- sobriété ;
- discrétion ;
- clarté ;
- simplicité ;
- fiabilité ;
- cohérence ;
- élégance fonctionnelle.

### 3.2 Niveau de sobriété

L'interface privilégie :

- des surfaces claires ;
- des cartes blanches ;
- des fonds légèrement teintés ;
- des séparateurs fins ;
- des icônes simples ;
- des couleurs peu saturées ;
- des accents visuels limités.

Les ombres sont absentes ou très discrètes. Les cartes sont principalement différenciées par leur fond et leur rayon.

Les illustrations décoratives, dégradés, effets lumineux et animations visuellement imposantes sont à éviter.

### 3.3 Thèmes

- Thème clair : validé.
- Thème sombre : à définir.

Le thème clair constitue la référence de toutes les maquettes actuelles.

---

## 4. Identité de marque

<ins>Statut : partiellement défini.</ins>

### 4.1 Nom

Nom de l'application : **Intruder**.

### 4.2 Symbole visuel

Le bouclier est utilisé comme symbole de protection dans les maquettes actuelles.

Il représente la sécurité et la protection du téléphone.

Son utilisation actuelle ne constitue pas encore une validation d'un logo définitif.

### 4.3 Logo et icône d'application

À définir :

- logo définitif ;
- wordmark « Intruder » ;
- icône d'application ;
- variantes sur fond clair et sombre ;
- règles d'utilisation ;
- écran de lancement.

Ne pas créer de logo définitif avant validation de sa conception.

---

## 5. Design system

### 5.1 Palette de couleurs

<ins>Statut : palette claire validée.</ins>

La palette repose sur un bleu ardoise peu saturé. Il constitue la couleur principale de l'interface et remplace les accents verts initialement utilisés pour les éléments interactifs.

Le vert est réservé aux états positifs liés à la protection.

#### Couleurs fondamentales

| Token | Valeur | Utilisation |
|---|---|---|
| `color.background` | `#F8FAFC` | Fond général des écrans |
| `color.surface` | `#FFFFFF` | Cartes et surfaces principales |
| `color.surfaceVariant` | `#F1F5F9` | Fonds secondaires, icônes et filtres inactifs |
| `color.text.primary` | `#27272A` | Titres et textes principaux |
| `color.text.secondary` | `#71717A` | Descriptions et métadonnées |
| `color.text.tertiary` | `#A1A1AA` | Informations très secondaires |
| `color.border` | `#E2E8F0` | Bordures et séparateurs |

#### Couleurs principales

| Token | Valeur | Utilisation |
|---|---|---|
| `color.primary` | `#64748B` | Accent principal |
| `color.primaryVariant` | `#475569` | Icônes secondaires et éléments interactifs discrets |
| `color.primaryContainer` | `#F1F5F9` | Fond des éléments associés à l'accent |
| `color.onPrimary` | `#FFFFFF` | Texte et icônes sur accent principal |

Le bleu ardoise est utilisé pour :

- les interrupteurs activés ;
- les filtres sélectionnés ;
- la navigation active ;
- les icônes interactives ;
- les flèches de navigation ;
- les indicateurs de sélection ;
- les éléments de contrôle.

#### Couleurs sémantiques

| Token | Valeur | Utilisation |
|---|---|---|
| `color.success` | `#34764F` | État positif de protection |
| `color.successContainer` | `#E0EEE4` | Fond des indicateurs positifs |
| `color.error` | `#C95757` | Échec et erreur |
| `color.warning` | À définir | Attention et avertissement |
| `color.info` | `#64748B` | Information générale |

Règles d'utilisation :

- Le vert n'est pas la couleur principale des interactions.
- Un interrupteur activé utilise le bleu ardoise.
- Le vert indique un état positif lié à la protection ou au fonctionnement.
- Le rouge est réservé aux erreurs et échecs.
- Les couleurs sémantiques doivent rester peu saturées.
- Les états doivent être accompagnés d'un libellé ou d'une icône pertinente.

#### Palette de référence

| Couleur | Aperçu | Valeur |
|---|---|---|
| Fond | `#F8FAFC` | `#F8FAFC` |
| Surface | `#FFFFFF` | `#FFFFFF` |
| Surface secondaire | `#F1F5F9` | `#F1F5F9` |
| Accent | `#64748B` | `#64748B` |
| Texte principal | `#27272A` | `#27272A` |
| Texte secondaire | `#71717A` | `#71717A` |
| Bordure | `#E2E8F0` | `#E2E8F0` |
| Succès | `#34764F` | `#34764F` |
| Erreur | `#C95757` | `#C95757` |

Les contrastes devront être vérifiés lors de l'implémentation, notamment pour les petits textes, les badges et les éléments désactivés.

---

### 5.2 Typographie

<ins>Statut : famille typographique validée.</ins>

#### Famille

**Roboto**, police système Android.

Aucune police externe n'est prévue.

L'utilisation de la police système permet de conserver une apparence native, de limiter les dépendances et de bénéficier du comportement de mise à l'échelle Android.

Les SVG de conception utilisent Arial comme approximation visuelle. La police réelle de l'application sera Roboto.

#### Échelle typographique

| Style | Taille | Graisse | Utilisation |
|---|---:|---|---|
| `display` | 28sp | 600 | Titres exceptionnels |
| `headline` | 24sp | 600 | Titres principaux |
| `title` | 18sp | 600 | Titres de sections |
| `titleSmall` | 16sp | 500 | Titres d'éléments |
| `body` | 14sp | 400 | Texte courant |
| `bodyMedium` | 14sp | 500 | Texte courant important |
| `label` | 12sp | 500 | Labels et métadonnées |
| `caption` | 11sp | 400 | Informations secondaires |

Les graisses disponibles sont principalement :

- Regular (400) ;
- Medium (500) ;
- SemiBold (600).

Le Bold (700) est réservé à des cas ponctuels, lorsqu'une hiérarchie visuelle le justifie.

#### Règles typographiques

- Éviter les textes trop petits.
- Utiliser une hiérarchie claire entre titre, description et métadonnée.
- Ne pas multiplier les graisses dans une même section.
- Ne pas utiliser systématiquement le gras pour les valeurs de paramètres.
- Prévoir la mise à l'échelle avec les paramètres de taille de police Android.

Les tailles des SVG sont des références de composition et non des valeurs définitives de `sp`.

---

### 5.3 Espacements et dimensions

<ins>Statut : principes validés.</ins>

#### Échelle d'espacement

Base : 4dp.

| Token | Valeur |
|---|---:|
| `spacing.xs` | 4dp |
| `spacing.sm` | 8dp |
| `spacing.md` | 12dp |
| `spacing.lg` | 16dp |
| `spacing.xl` | 20dp |
| `spacing.2xl` | 24dp |
| `spacing.3xl` | 32dp |
| `spacing.4xl` | 40dp |

Les espacements doivent privilégier les multiples de 4dp, avec une préférence pour les multiples de 8dp.

#### Marges

- Marge horizontale de référence : 16 à 20dp.
- Espacement entre sections : 16 à 24dp.
- Espacement entre éléments d'une même section : 8 à 16dp.
- Padding interne des cartes : généralement 16dp.

Les valeurs peuvent être adaptées selon le contenu et la densité de l'écran.

#### Rayons

<ins>Statut : échelle validée.</ins>

| Token | Valeur | Utilisation |
|---|---:|---|
| `radius.sm` | 8dp | Badges et petits éléments |
| `radius.md` | 12dp | Boutons, champs et contrôles |
| `radius.lg` | 16dp | Cartes et sections principales |
| `radius.full` | 999dp | Pilules et interrupteurs |

Les cartes principales utilisent un rayon de 16dp.

Les fonds d'icônes utilisent généralement un rayon de 10 à 12dp.

#### Tailles tactiles

- Cible tactile minimale : 48 × 48dp.
- Les icônes peuvent être visuellement plus petites que leur zone tactile.
- Les éléments interactifs proches doivent conserver un espacement suffisant.

#### Icônes

- Style : linéaire, simple et cohérent.
- Épaisseur de trait généralement comprise entre 1.5 et 2dp.
- Couleur principale des icônes secondaires : `#475569`.
- Couleur des icônes actives : `#64748B`.
- Fond des icônes dans les paramètres : `#F1F5F9`.

Éviter de mélanger des icônes pleines et linéaires sans raison fonctionnelle.

#### Élévation et bordures

- Pas d'ombres prononcées.
- Cartes différenciées principalement par leur surface blanche.
- Séparateurs : `#E2E8F0`.
- Élévation faible ou nulle par défaut.
- Les bordures ne doivent pas alourdir l'interface.

---

### 5.4 Composants réutilisables

<ins>Statut : direction visuelle définie, spécifications détaillées à réaliser.</ins>

#### Cartes

- Fond : `#FFFFFF`.
- Rayon : 16dp.
- Padding généralement de 16dp.
- Pas d'ombre marquée.
- Séparateurs internes : `#E2E8F0`.

Éviter les cartes imbriquées.

#### Interrupteur

- État activé : fond `#64748B`.
- État désactivé : à définir lors de la spécification du composant.
- Curseur : blanc.
- Forme : pilule.
- Taille tactile minimale : 48 × 48dp.
- L'état doit être compréhensible avec TalkBack.

#### Ligne de paramètre

Structure habituelle :

- icône sur fond `#F1F5F9` ;
- titre principal ;
- description facultative ;
- valeur ou contrôle à droite ;
- séparateur lorsque plusieurs lignes sont regroupées.

#### Boutons

À spécifier :

- principal ;
- secondaire ;
- texte ;
- destructif.

Les boutons doivent respecter les couleurs, rayons et tailles tactiles du système.

#### Filtres

- Filtre sélectionné : fond `#64748B`, texte blanc.
- Filtre non sélectionné : fond `#F1F5F9`, texte `#475569`.
- Forme pilule.
- Les filtres cumulés doivent rester visuellement identifiables.

#### Badges

- Dimensions compactes.
- Rayon arrondi.
- Texte court et lisible.
- Pas de couleurs trop saturées.

Les badges Succès et Échec doivent rester discrets.

#### Navigation basse

- Deux destinations : Galerie et Paramètres.
- Fond : `#FFFFFF`.
- Séparateur supérieur : `#E2E8F0`.
- Destination inactive : gris `#77777A`.
- Destination active : bleu ardoise `#64748B`.
- Icônes linéaires.
- Libellés visibles sous les icônes.

La navigation doit conserver les mêmes dimensions et positions sur les écrans principaux.

#### Autres composants à spécifier

- vignette de galerie ;
- ligne de liste ;
- sélecteur de période ;
- bouton grille/liste ;
- état vide ;
- bannière d'avertissement ;
- dialogue de confirmation ;
- barre supérieure ;
- indicateur de chargement ;
- message d'erreur.

Pour chaque composant : variantes, dimensions, états, interactions et accessibilité.

---

## 6. Écrans à maquettter

### 6.1 Navigation principale

- Galerie.
- Paramètres.
- Barre de navigation inférieure.
- En-tête avec « Intruder » à gauche et badge d'état à droite.

### 6.2 Galerie

Prévoir les maquettes :

- galerie en grille avec événements ;
- galerie en liste ;
- filtres Type et Période ;
- filtres actifs ;
- filtres cumulés ;
- période personnalisée ;
- aucun événement ;
- aucun résultat correspondant aux filtres ;
- vignettes avec photo ;
- vignettes `SKIPPED` ;
- vignettes `FAILED` ;
- mode sélection ;
- sélection multiple ;
- confirmation de suppression.

### 6.3 Détail d'un événement

Prévoir :

- événement avec photo ;
- événement sans photo (`SKIPPED`) ;
- événement sans photo (`FAILED`) ;
- motif d'erreur disponible ;
- confirmation de suppression.

### 6.4 Paramètres

Prévoir :

- page principale avec les quatre sections ;
- Protection ;
- Surveillance ;
- Capture photo ;
- Notifications ;
- Stockage ;
- À propos ;
- états d'autorisation accordée, refusée ou révoquée ;
- avertissement « Attention » et accès aux paramètres Android.

### 6.5 Première ouverture

Prévoir :

- bienvenue ;
- explication Administrateur de l'appareil ;
- explication caméra ;
- test caméra ;
- explication notifications ;
- vérification des fonctionnalités ;
- récapitulatif et activation ;
- variantes lorsque les autorisations sont refusées ou le test caméra échoue.

---

## 7. Interactions et transitions

À spécifier :

- navigation entre onglets et sous-écrans ;
- ouverture du détail depuis une vignette ;
- appui long et entrée en mode sélection ;
- sélection/désélection ;
- sélection de tous les résultats filtrés ;
- confirmation et résultat de suppression ;
- ouverture et application des filtres ;
- réinitialisation des filtres ;
- bascule grille/liste ;
- ouverture des paramètres Android ;
- comportement du badge « Attention » ;
- retour arrière Android.

Les animations doivent rester sobres et ne pas ralentir l'accès à la galerie.

---

## 8. États système et cas particuliers

Pour chaque écran, prévoir :

- état normal ;
- chargement ;
- contenu vide ;
- erreur ;
- permission manquante ;
- fonctionnalité indisponible ;
- état désactivé ;
- résultat d'action (suppression, changement de réglage, etc.).

Ne jamais utiliser uniquement la couleur pour communiquer un état.

Les messages d'erreur doivent expliquer clairement le problème et, lorsque c'est possible, indiquer comment le résoudre.

---

## 9. Accessibilité

À documenter et vérifier :

- contrastes suffisants ;
- tailles de zones tactiles adaptées ;
- descriptions accessibles des icônes ;
- ordre de lecture cohérent ;
- prise en charge de la taille de police système ;
- distinction des états sans dépendre uniquement de la couleur ;
- libellés explicites pour les interrupteurs ;
- compatibilité TalkBack.

---

## 10. Adaptation aux écrans

La référence est un téléphone Android.

Définir :

- marges et largeur de contenu ;
- comportement sur petits écrans ;
- gestion des grandes tailles de texte ;
- gestion des zones système et des découpes ;
- orientation prise en charge ;
- comportement sur écrans plus larges si cela est retenu.

Les maquettes actuelles utilisent une base de 390 × 844 pixels. Cette dimension constitue une référence de composition et non une taille fixe à reproduire dans l'application.

L'implémentation devra utiliser les unités et mécanismes d'adaptation Android appropriés (`dp`, `sp`, insets système, contraintes de mise en page).

---

## 11. Livrables attendus

- Direction artistique validée.
- Palette et tokens documentés.
- Échelle typographique.
- Échelle d'espacement et de rayons.
- Bibliothèque de composants UI.
- Maquettes de tous les écrans V1.
- États alternatifs et erreurs.
- Parcours interactif ou prototype de navigation, si utile.
- Référence suffisamment précise pour développer les écrans en Jetpack Compose sans interprétation majeure.

---

## 12. Ordre de travail

1. Direction artistique et identité visuelle.
2. Palette, typographie et style des icônes.
3. Tokens et composants de base.
4. Navigation principale.
5. Galerie et ses états.
6. Détail d'événement.
7. Paramètres et sous-écrans.
8. Assistant de première ouverture.
9. Vérification de cohérence, accessibilité et petits écrans.
10. Validation des maquettes avant implémentation.

---

## 13. Journal des décisions visuelles

| Sujet | Statut | Décision |
|---|---|---|
| Direction artistique | Validé | « Sécurité discrète » |
| Niveau de sobriété | Validé | Interface claire, sobre et peu chargée |
| Thème clair | Validé | Référence des maquettes |
| Thème sombre | À définir | — |
| Palette principale | Validé | Bleu ardoise `#64748B` |
| Fond général | Validé | `#F8FAFC` |
| Surface | Validé | `#FFFFFF` |
| Surface secondaire | Validé | `#F1F5F9` |
| Texte principal | Validé | `#27272A` |
| Texte secondaire | Validé | `#71717A` |
| Bordures | Validé | `#E2E8F0` |
| Couleur de succès | Validé | Vert `#34764F` |
| Couleur d'erreur | Validé | Rouge `#C95757` |
| Typographie | Validé | Roboto, police système Android |
| Échelle typographique | Validé | 11 à 28sp selon la hiérarchie |
| Échelle d'espacement | Validé | Base 4dp |
| Rayons | Validé | 8, 12, 16 et 999dp |
| Élévation | Validé | Absente ou très discrète |
| Style des icônes | Validé | Linéaire, sobre |
| Structure de navigation | Validé | Barre basse : Galerie et Paramètres |
| En-tête galerie | Validé | « Intruder » à gauche, badge d'état à droite |
| Galerie par défaut | Validé | Grille de 3 colonnes, vignettes carrées |
| Filtres | Validé | Type et Période visibles en permanence |
| Filtres actifs | Validé | Fond bleu ardoise, texte blanc |
| Groupes de dates | Validé | Titres seuls, sans compteur |
| Badge de type | Validé | Petit badge Succès / Échec sur la vignette |
| Mode liste | Validé | Miniature/icône, type, date/heure, état de capture |
| Détail événement | Validé | Photo, type, date/heure, capture, erreur connue, suppression |
| Mode sélection | Validé | Titre avec nombre sélectionné, tout sélectionner, supprimer, annuler |
| Écran vide | Validé | Icône, message simple, accès aux paramètres de protection |
| Navigation basse | Validé | Fond blanc, séparateur fin, accent bleu ardoise actif |
| Logo définitif | À définir | — |
| Icône d'application | À définir | — |
| Thème sombre | À définir | — |
| Composants détaillés | À réaliser | Variantes, états et interactions |
| Maquettes détaillées | En cours | Harmonisation écran par écran |

---

## 14. Écrans déjà harmonisés

Les maquettes suivantes ont été alignées sur la direction artistique et la palette retenues :

- Galerie.
- Paramètres.
- Protection globale.
- Stockage local.
- À propos.
- Surveillance.
- Capture photo.
- Notifications techniques.

Ces écrans constituent les premières références visuelles du design system. Ils devront être revérifiés ensemble lors de la phase de validation globale.