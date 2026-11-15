# 📜 Credits & Third-Party Assets

This project incorporates third-party 3D models, creative assets, and open-source software libraries in accordance with their respective open-source licenses and community permissions. We express our deepest gratitude to all original creators for their outstanding contributions to the Minecraft modding, Blockbench, and 3D art communities.

---

## 🗺️ Open-Source Software & Rendering Integrations

### 1. Diorama (Ray-Traced Voxel Minimap & World Map Engine)
* **Author / Creator:** **Dara Adedeji** (`SunkenInTime`)
* **CurseForge Page:** [Diorama on CurseForge](https://www.curseforge.com/minecraft/mc-mods/diorama)
* **Modrinth Page:** [Diorama on Modrinth](https://modrinth.com/mod/diorama-map)
* **Source Code:** [GitHub - SunkenInTime/minecraft-orthogonal-map](https://github.com/SunkenInTime/minecraft-orthogonal-map)
* **License:** [MIT License](https://opensource.org/licenses/MIT) *(Full legal notice bundled in [LICENSE_DIORAMA.txt](https://github.com/Landa6543/Awakening-System/blob/main/LICENSE_DIORAMA.txt))*
* **Usage in Awakening System:**
  * Serves as the high-performance ray-traced voxel mapping engine powering the mod's HUD Minimap, underground cave slice navigation, fullscreen tactical map (`M`), fluid waypoint markers, and entity radar system.

### 2. DungeonZ (Dungeon Structure Pieces)
* **Author / Creator:** **Globox_Z** and Zura (structures built by `ftxeven`)
* **CurseForge Page:** [DungeonZ on CurseForge](https://www.curseforge.com/minecraft/mc-mods/dungeonz)
* **Source Code:** [GitHub - Globox1997/Dungeonz](https://github.com/Globox1997/Dungeonz)
* **License:** [MIT License](https://opensource.org/licenses/MIT) *(Full legal notice bundled in [LICENSE_DUNGEONZ.txt](https://github.com/Landa6543/Awakening-System/blob/main/LICENSE_DUNGEONZ.txt))*
* **Usage in Awakening System:**
  * The structure pieces (`.nbt`) and jigsaw template pools of the Dark Dungeon and the Jungle Temple, adapted under `data/awakening_system/structure/ruin_gate/` and `worldgen/template_pool/ruin_gate/` for the Ruin Gates. Their own blocks were swapped for vanilla ones; the placement code is the mod's own.

---

## 🗿 3D Entity Models & Animations (Blockbench Community)

### 1. Skulk Golem (`skulk_golem` / `mutant_warden`)
* **Creator / Author:** `@elma_taburlas`
* **Original Asset Title:** *Skulk golem* (*"Very consumed by the darkness indeed"*)
* **Source:** Blockbench Workshop Community
* **License:** [Creative Commons Attribution 4.0 International (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)
* **Format:** Blockbench Modded Entity (`.bbmodel` / `.geo.json`)
* **Usage in Awakening System:**
  * Serves as the ancient dormant statue and awakened guardian / mini-boss in the Deep Dark Caverns of Dimension 10 (`tower_floor_10`).
  * Features dual states: a dormant petrified statue resting pose (`estatue`), motion/vibration-activated awakening, and combat states (`walk`, `attack`).

---

### 2. Limbo Shadow Stalkers & Void Creatures (`@ziiwplay`)
* **Creator / Author:** `@ziiwplay`
* **Source:** Blockbench Community Showcase
* **License:** [Creative Commons Attribution 4.0 International (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)
* **Format:** Blockbench Modded Entity / Animated Models (`.bbmodel` / `.geo.json`)
* **Creatures & Models:**
  1. **Spider Head Monster** (`spider-head-monster.bbmodel` / `spider_head.geo.json`)
     * Multi-jointed arachnid nightmare with an elongated monolithic head bearing three vertical luminescent eyes.
  2. **Tall Three Eyed Stalker** (`tall-three-eyed-stalker.bbmodel` / `tall_stalker.geo.json`)
     * Towering, slender bipedal shadow stalker with elongated arms, clawed hands, and triple vertical gaze.
  3. **One Eyed Stilt Walker** (`one-eyed-stilt-walker.bbmodel` / `stilt_walker.geo.json`)
     * High-altitude abyssal walker elevated on massive two-segmented stilt legs, centered around an ominous cyclopean eye.
* **Usage in Awakening System:**
  * Atmospheric wandering predators and psychological hazards in the **Limbo Dimension** (`awakening_system:limbo`), stalking fallen hunters as they navigate the fog in search of escape doors.

---

### 3. The Entity of Limbo (`@nori`)
* **Creator / Author:** `@nori`
* **Model Title:** **THE_ENTITY** (`the-entity.bbmodel` / `limbo_entity.geo.json`)
* **Source:** Blockbench Community Showcase
* **License:** [Creative Commons Attribution 4.0 International (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)
* **Format:** Blockbench Modded Entity / Animated Model (`.bbmodel` / `.geo.json`)
* **Usage in Awakening System:**
  * Mysterious, ethereal shadow presence lingering in the deep mist of Limbo. Emits dark soul-fog particles and reacts to nearby players with uncanny movements.

---

## ⚔️ 3D Weapons, Equipment & Item Models (Blockbench Workshop)

Various custom 3D weapons, greatswords, scythes, and equipment models in **Awakening System** are adapted from talented artists in the **Blockbench Workshop** community, 3D modeling repositories, and open community showcases under **Creative Commons Attribution (CC BY 4.0)** and permissive open-source licenses:

### 1. Greatswords & Claymores
* **Assets:** `creator_claymore.json`, `void_claymore.json`, `wolfs_gravestone.json`, `redhorn_stonethresher.json`
* **Format:** Blockbench Java Block/Item Model (`.json`)
* **License:** [Creative Commons Attribution 4.0 (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/) / Open Community Resource
* **Attribution:** Adapted from community weapon submissions and Blockbench creators. Features full hand/GUI/ground display transforms and custom layered textures.

### 2. Scythes, Polearms & Hammers
* **Assets:** `dread_scythe.json`, `adamantite_war_hammer.json`, `herta_hammer.json`, `crimson_moons_semblance.json`, `staff_of_homa.json`
* **Format:** Blockbench Custom 3D Model (`.json`)
* **License:** [Creative Commons Attribution 4.0 (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)
* **Usage:** High-rank monarch weapons, dimensional trial drops, and boss combat gear.

### 3. Precision Bows & Ranged Weapons
* **Assets:** `amos_bow.json`, `aqua_simulacra.json`, `astral_archers_bow.json`, `hunter_path.json`, `polar_star.json`, `skyward_harp.json`, `thundering_pulse.json`, `end_of_the_line.json`, `hamayumi.json`
* **Format:** Blockbench Custom 3D Models with dynamic draw cycle variants (`_pulling_0`, `_pulling_1`, `_pulling_2`, `_standby`)
* **License:** [Creative Commons Attribution 4.0 (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)

---

## 🧑‍🌾 Townsfolk Skins (Floor 11 Town NPCs)

The people of the medieval town on floor 11 wear player skins shared on **[MinecraftSkins.com](https://www.minecraftskins.com/)** by their creators. The blonde innkeeper had her eyes painted in; the rest are used as uploaded. Files live in `textures/entity/town_npc/`.

| In the mod | Skin | Creator |
|---|---|---|
| `innkeeper_blonde.png` (Greta) | [new skin](https://www.minecraftskins.com/skin/23915839/new-skin/) | **powergirl25** |
| `innkeeper_fae.png` (Lyria) | [Fae Tavern Keep](https://www.minecraftskins.com/skin/23509082/fae-tavern-keep/) | **BlueBunniez26** |
| `innkeeper_alice.png` (Maren) | [Alice the Tavern Keeper](https://www.minecraftskins.com/skin/23135420/alice-the-tavern-keeper/) | **LostIsMissing** |
| `librarian.png` (Elandor) | [Elven Scholar](https://www.minecraftskins.com/skin/16338840/elven-scholar/) | **JomoArigoto** |
| `baker.png` (Bram) | [Baker](https://www.minecraftskins.com/skin/22582542/baker/) | **Wawelski** |
| `baker_woman.png` (Hilda) | [Baker](https://www.minecraftskins.com/skin/24371604/baker/) | **Matchalatte9** |
| `merchant.png` (Dorian) | [Medieval Merchant](https://www.minecraftskins.com/skin/23528847/medieval-merchant/) | **Gsantos7** |
| `townsman_green.png` (Cedric) | [Medieval Merchant](https://www.minecraftskins.com/skin/24161488/medieval-merchant/) | **vabigou** |
| `townswoman_emily.png` (Elise) | [Emily Holt v2](https://www.minecraftskins.com/skin/24366215/emily-holt-v2/) | **CT9712Phase** |
| `townswoman_paula.png` (Rosalind) | [Paula Boris](https://www.minecraftskins.com/skin/24070862/paula-boris/) | **Minhyukah** |
| `farmer.png` (Godwin) | [Peasant fat ALT](https://www.minecraftskins.com/skin/24108478/peasant-fat-alt/) | **Wawelski** |
| `farmer2.png` (Rowan) | [Ginger peasant](https://www.minecraftskins.com/skin/24102059/ginger-peasant/) | **jcolesandwich** |
| `farmer_woman.png` (Brenna) | [Alex peasant maiden](https://www.minecraftskins.com/skin/24241287/alex-peasant-maiden/) | **AdrianDrago** |
| `priest.png` (Anselm) | [Friar in Brown Carmelite Habit](https://www.minecraftskins.com/skin/24020041/friar-in-brown-carmelite-habit/) | **Mrcouchman** |

`smith.png` and `bearded.png` are the mod author's own skins.

---

## 🌌 Celestial Armory Sprites

* **Assets:** the 28 celestial weapons in `textures/item/celestial/` (Exoblade, Galaxia, Ark of the Cosmos, Omniblade, Death's Ascension, …).
* **Origin:** original 2D sprites recreated from scratch for Awakening System, **inspired by** weapons of Terraria and its Calamity Mod. They are not copies of those games' files.

---

## 🎨 Creative Partnerships & Content Permissions

### 1. Genshin Mod Asset Integration (`fapdos`)
* **Creator:** **[fapdos](https://www.curseforge.com/members/fapdos/projects)**
* **Terms:** Official creator permission granted to use, adapt, and integrate signature weapon designs, textures, and assets for Awakening System.

---

## 🚪 Reserved / Inactive Assets (Future Versions)

* **RPG Portal Structure / Model:** Held in reserve for dimensional transitions in upcoming version milestones.
* **Grave / Tombstone Model (`tumba`):** Reserved for the upcoming post-0.3.4 ghost death recovery system.
* **Dragon 3D Model & Boss Rigs:** Reserved for future world boss encounters and seasonal events.

---

## 📩 Notice to Community Artists & Creators

We strive to maintain 100% rigorous, transparent attribution for all community-created artwork and models. If you are an artist or author of any asset featured in the mod and would like to:
1. Update your handle, name, or alias,
2. Add or modify direct links to your portfolio (ArtStation, Sketchfab, Twitter/X, Discord),
3. Request specific license wording or notes,

Please feel free to open an issue or contact the mod author (**LanderAlo**) directly. We are always eager to support and properly celebrate the creators who enrich the Minecraft community!
