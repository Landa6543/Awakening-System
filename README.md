# Awakening System

<p align="center">
  <img src="purple_slime_icon.png" alt="Awakening System Icon - Purple Slime" width="220" height="220" />
</p>

<p align="center">
  <strong>An immersive RPG progression, dungeon, and cultivation mod for Minecraft 1.21.1 (NeoForge).</strong><br>
  <em>Inspired by legendary manhwa and fantasy masterpieces: Solo Leveling, Omniscient Reader's Viewpoint, and Murim Martial Arts.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg" alt="Minecraft 1.21.1" />
  <img src="https://img.shields.io/badge/NeoForge-21.1.249+-orange.svg" alt="NeoForge" />
  <img src="https://img.shields.io/badge/Java-21-blue.svg" alt="Java 21" />
  <img src="https://img.shields.io/badge/License-MIT-purple.svg" alt="MIT License" />
  <img src="https://img.shields.io/badge/Author-LanderAlo-success.svg" alt="Author LanderAlo" />
</p>

---

## 🌟 Overview

**Awakening System** transforms Minecraft into a living, narrative-driven action RPG. An omniscient entity known as «The System» observes your actions in real-time, judging your playstyle to bestow a true Hunter Awakening. Uncover hidden affinities, cultivate ancient internal Qi, climb hunter ranks from Rank E all the way to Rank S and Monarch/EX, and challenge dimensional gate incursions.

---

## ⚔️ Key Features

### 1. Hunter Awakening & Free Stat Growth
- **Awakening Assessment**: Awaken from an ordinary survivor into an official Hunter, acquiring dynamic titles and emergent combat classes.
- **Uncapped Stat Allocation**: Earn stat points on each level-up to freely allocate into **Strength, Vitality, Agility, Intelligence, Perception, and Dexterity** with no artificial level caps.
- **Emergent Class Trees**: Advance your specialization across multiple paths: Warrior, Assassin, Mage, Necromancer, Martial Monk, Ranger, and more.

### 2. Dynamic, Fully Responsive RPG HUD
- **Hunter Status Card**: Real-time Health (HP), Mana (MP), and Martial Qi bars featuring smooth damage interpolation and *Damage Lag* visual trails.
- **Active Combat Hotbar**: Direct active skill triggers for slots `[V] [R] [G] [B] [X]`, plus Constellation Stigma `[Y]`.
- **Display Scaling & Adaptation**: Built-in dynamic GUI scale compensation (`/hud scale auto`) preventing any overlap with Minecraft's hotbar across all screen resolutions.
- **HUD Customization**: Switch combat skills between bottom-left corner and centered above hotbar (`/hud skills`).

### 3. Custom Realms & Dimensions

#### 🌳 Isekai World (`awakening_system:isekai_world`)
- A lush, sacred fantasy realm resting beneath the canopy of the great **Yggdrasil World Tree**.
- **Exclusive Mana Fauna**: Mana-infused Cows, Sheep, Pigs, and Chickens with animated 3D ears and tails.
- **Elemental Fantasy Slimes (10 Color Variants)**: Charming teardrop-shaped cubic slimes with expressive anime faces (Purple, Azure, Gold, Sakura, Emerald, Ruby, Amber, Mint, Frost, and Obsidian). Slimes can be tamed, bred, and ridden!

#### 🏔️ Murim Realm (`awakening_system:murim_world`)
- Celestial floating martial sect island soaring above eternal sea of clouds.
- **Qi Cultivation**: Practice meditative internal breathing, conquer cultivation bottlenecks, and breakthrough mortal realms.
- **Tantric Alchemy**: Refine spiritual pills inside the Alchemy Cauldron by combining medicinal herbs, beast cores, and catalyst stones.
- **Monumental Torii Gate**: Cinematic unified vortex portal with dual counter-rotating layers of Spiritual Jade, Crimson Plum, and Dragon Gold.

### 4. Dimensional Gates & Boss Incursions
- **Ranked Gates (Rank E to Monarch)**: Dynamic 100-frame fluid vortex portals with dimensional particles and real-time color resonance.
- **Wave Incursion Arenas**: Survive swarms of dungeon beasts, elite guards, and floor guardians.
- **Colossal Boss Encounters**:
  - **Elemental Demon Kings**: Fully articulated 3D winged demon lords with phase-based attacks.
  - **Ancient Celestial Dragon**: A 12-segment articulated oriental dragon boss and flying mount with dragon breath attacks.

### 5. Constellation Streaming & Companions
- **Constellation Channel (Omniscient Reader)**: Receive cosmic gaze notifications, message donations, coin sponsorships, and powerful Stigmas in an interactive live feed.
- **Dokkaebi Familiar**: A loyal, levitating mystical companion who opens the Dokkaebi System Shop.
- **Hunter Guild & Bounty Board**: Accept regional monster contracts, climb guild reputation, and claim exclusive rewards.
- **Hunter Guidebook**: Comprehensive in-game manual detailing combos, cultivation breakthroughs, and game mechanics.

### 6. Full Multilingual Localization
- 100% synchronized across **11 languages** with over 4,700 localized keys:
  - English (`en_us`), Spanish (`es_es`), Russian (`ru_ru`), Korean (`ko_kr`), Japanese (`ja_jp`), Simplified Chinese (`zh_cn`), German (`de_de`), French (`fr_fr`), Brazilian Portuguese (`pt_br`), Italian (`it_it`), and Ukrainian (`uk_ua`).

---

## 🎮 Keybindings & Controls

| Key | Default Action |
|:---:|---|
| **`K`** | Open / Close System Main Menu (Status, Skills, Quests, Classes, Constellations, Shop) |
| **`V`** | Active Skill Slot 1 |
| **`R`** | Active Skill Slot 2 |
| **`G`** | Active Skill Slot 3 |
| **`B`** | Active Skill Slot 4 |
| **`X`** | Active Skill Slot 5 (Ultimate Technique) |
| **`Y`** | Constellation Stigma / Divine Skill |

*(All keybindings can be customized in the Minecraft Controls menu)*

---

## 🛠️ Essential Commands

- `/system` — View hunter profile, active stats, and current rank.
- `/system attribute <stat>` — Allocate free attribute points quickly.
- `/hud scale <auto|small|medium|large|value>` — Adjust RPG HUD sizing and scaling.
- `/hud skills <bottom_left|above_hotbar>` — Switch active skill bar position.
- `/dungeon enter <rank>` — Enter a dimensional rift dungeon incursion.

---

## 📦 Requirements & Building

- **Minecraft**: 1.21.1
- **Mod Loader**: NeoForge `21.1.249` or higher
- **Java**: Java 21 LTS

### Build from source:
```powershell
.\gradlew.bat compileJava
.\gradlew.bat jar
```

The compiled mod JAR will be output to:
`build/libs/awakening_system-0.1.0.jar`

---

## 📄 License & Credits

- **Author**: LanderAlo
- **License**: [MIT License](LICENSE)
- **Credits**: Inspired by *Solo Leveling* by Chugong, *Omniscient Reader's Viewpoint* by sing N song, and classic Murim manhwa fantasy.
