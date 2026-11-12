# Intruder — Conception visuelle et design system

**Version :** 0.1  
**Statut :** document de travail — conception à réaliser.  
**Référence fonctionnelle :** `01-cahier-des-charges-fonctionnel.md`.

> Ce document est volontairement un plan de conception. Les éléments marqués « À définir » ne sont pas encore des décisions validées.

---

## 1. Objectif

Définir l'identité visuelle d'Intruder et spécifier chaque écran avant l'implémentation en Jetpack Compose.

Le résultat attendu :
- une direction artistique cohérente ;
- un design system documenté ;
- des maquettes de référence ;
- les états et interactions de chaque composant ;
- des règles d'accessibilité et d'adaptation aux tailles d'écran.

## 2. Principes déjà établis

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

## 3. Direction artistique

**Statut : À définir ensemble.**

À décider :
- personnalité visuelle de la marque ;
- niveau de sobriété ;
- couleurs principales et secondaires ;
- couleur d'accent ;
- couleurs sémantiques (succès, attention, erreur, information) ;
- thème clair et éventuel thème sombre ;
- traitement des surfaces et arrière-plans ;
- style des illustrations éventuelles.

La direction doit inspirer la confiance et la sécurité sans rendre l'application anxiogène.

## 4. Identité de marque

**Statut : À définir.**

Éléments à concevoir :
- logo et icône d'application ;
- wordmark « Intruder » ;
- éventuel symbole associé ;
- règles d'utilisation du logo ;
- variantes pour fond clair/sombre ;
- écran de lancement.

Ne pas créer de logo définitif avant validation de la direction artistique.

## 5. Design system

### 5.1 Couleurs

À définir et documenter sous forme de tokens :

- `color.background`
- `color.surface`
- `color.surfaceVariant`
- `color.text.primary`
- `color.text.secondary`
- `color.border`
- `color.primary`
- `color.accent`
- `color.success`
- `color.warning`
- `color.error`
- `color.info`

Préciser les valeurs pour les thèmes retenus et vérifier les contrastes.

### 5.2 Typographie

À définir :
- famille typographique ;
- tailles et graisses ;
- styles de titres ;
- corps de texte ;
- légendes et métadonnées ;
- interlignage ;
- règles de mise à l'échelle Android.

### 5.3 Espacements et dimensions

À définir :
- échelle d'espacement ;
- marges de page ;
- espacements entre sections ;
- tailles tactiles minimales ;
- dimensions des icônes ;
- hauteur des champs et boutons ;
- rayons des cartes et contrôles ;
- élévations et bordures.

### 5.4 Composants réutilisables

À spécifier :
- bouton principal, secondaire et texte ;
- interrupteur ;
- ligne de paramètre ;
- carte de section ;
- badge de type d'événement ;
- badge d'état de protection ;
- bouton/contrôle de filtre ;
- sélecteur de période ;
- bouton grille/liste ;
- vignette de galerie ;
- ligne de liste ;
- état vide ;
- bannière d'avertissement ;
- dialogue de confirmation ;
- barre de navigation inférieure ;
- barre supérieure ;
- indicateur de chargement ;
- message d'erreur.

Pour chaque composant : variantes, dimensions, états, interactions, accessibilité et comportement en thème clair/sombre.

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

Ne jamais utiliser uniquement la couleur pour communiquer un état : ajouter un libellé ou une icône pertinente.

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

## 10. Adaptation aux écrans

La référence est un téléphone Android. Définir :
- marges et largeur de contenu ;
- comportement sur petits écrans ;
- gestion des grandes tailles de texte ;
- gestion des zones système et des découpes ;
- orientation prise en charge ;
- comportement sur écrans plus larges si cela est retenu.

## 11. Livrables attendus

- Direction artistique validée.
- Palette et tokens documentés.
- Échelle typographique.
- Bibliothèque de composants UI.
- Maquettes de tous les écrans V1.
- États alternatifs et erreurs.
- Parcours interactif ou prototype de navigation, si utile.
- Référence suffisamment précise pour développer les écrans en Jetpack Compose sans interprétation majeure.

## 12. Ordre de travail proposé

1. Définir la direction artistique.
2. Choisir palette, typographie et style des icônes.
3. Définir les tokens et composants de base.
4. Maquetter la navigation principale.
5. Maquetter la galerie et ses états.
6. Maquetter le détail d'événement.
7. Maquetter les paramètres.
8. Maquetter l'assistant de première ouverture.
9. Vérifier cohérence, accessibilité et petits écrans.
10. Valider les maquettes avant implémentation.

---

## Journal des décisions visuelles

| Sujet | Statut | Décision |
|---|---|---|
| Structure de navigation | Validé | Barre basse : Galerie et Paramètres |
| En-tête galerie | Validé | « Intruder » à gauche, badge d'état à droite |
| Galerie par défaut | Validé | Grille de 3 colonnes, vignettes carrées |
| Filtres | Validé | Type et Période visibles en permanence |
| Filtres actifs | Validé | Fond légèrement coloré |
| Groupes de dates | Validé | Titres seuls, sans compteur |
| Badge de type | Validé | Petit badge Succès / Échec sur la vignette |
| Mode liste | Validé | Miniature/icône, type, date/heure, état de capture |
| Détail événement | Validé | Photo, type, date/heure, capture, erreur connue, suppression |
| Mode sélection | Validé | Titre avec nombre sélectionné, tout sélectionner, supprimer, annuler |
| Écran vide | Validé | Icône, message simple, accès aux paramètres de protection |
| Direction artistique | À définir | — |
| Palette | À définir | — |
| Typographie | À définir | — |
| Logo | À définir | — |
| Design system | À définir | — |
| Maquettes détaillées | À réaliser | — |
