# ⚡ Awakening System — Version 0.3.3
### *The Monarchs, Limbo & Economy Overhaul*

* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge 21.1.249+
* **Java:** Java 21 LTS

---

## 👑 Boss Redesigns & The Ant King: Baran, Igris & Beru
* **Demon King Baran Redesign:** Complete visual and combat overhaul for Baran featuring brand-new 3D GeckoLib models, glowing demonic horns, magma-infused lightning attacks, and dynamic battle animations.
* **Blood-Red Commander Igris Redesign:** Reworked from the ground up with an imposing 3D armor silhouette, glowing visor effects, fluid greatsword slash combos, and his iconic crimson plume cape.
* **New Boss Integration — The Ant King, Beru:** Introduced Beru as a catastrophic apex predator boss! Features lightning-fast dash strikes, aerial swoops, paralyzing screech sound effects, and multi-phase combat.
* **Shadow Extraction Support:** Defeating these legendary monarchs allows Shadow Monarchs to extract them as elite Shadow Soldiers, preserving their custom models, animations, and battlefield abilities.

---

## 🏰 Dungeons & Red Gates Overhaul
* **Themed Multi-Chamber Dungeons:** Dungeons are no longer single static rooms. They now generate as continuous procedural gauntlets featuring progressive enemy waves, branching corridors, and grand boss chambers:
  * **Baran's Magma Domain:** Volcanic nether-brick bastions guarded by flame spirits and demon legionnaires.
  * **Igris's Throne Citadel:** Majestic chivalric halls and royal throne rooms surrounded by blood-red knights.
  * **Beru's Insect Colony:** Subterranean infested ant hives swarming with deadly Ant Soldiers.
* **Red Gate Peril:** High-rank dimensional rifts and Red Gates seal behind entering hunters, locking all exit portals until the dungeon boss is conquered.

---

## 🌌 New Dimension: The Limbo
Death inside a dungeon is no longer a simple respawn.
* **The Realm of Lost Souls:** Dying within any Dungeon Rift banishes your soul to **The Limbo**, a desolate, pitch-black wasteland shrouded in cosmic silence and dense fog.
* **Find the Exit Gates:** To return to the living realm with your equipment, you must navigate obsidian ruins, evade hunting horrors, and locate one of the cardinal Exit Portals.
* **New Horrors — Stalkers & Ophanim:** Beware of the entities dwelling in the mist. Featuring extended 64-block acoustic detection, you will hear their eerie calls and hovering wings long before they strike.

---

## 💰 Complete Hunter Shop & Economy Rebalance
The System Shop has been thoroughly overhauled to provide a balanced, challenging, and rewarding progression curve:
* **Strict Level-Gating on Mystery Boxes:** Mystery loot boxes are no longer available for early-game gambling. Players must earn their ranks to unlock them:
  * **Common Box:** Level 10 (350 Coins)
  * **Lucky Box:** Level 20 (850 Coins)
  * **Rare Box:** Level 35 (2,200 Coins)
  * **Epic Box:** Level 55 (6,500 Coins)
  * **Legendary Box:** Level 75 (20,000 Coins)
  * **Roulette Tokens & Altar:** Level 20 (3,500 Coins) & Level 25 (30,000 Coins)
* **Tiered Equipment & Weapon Level Caps:** All weapons, tools, and armors now have minimum level requirements corresponding to their power tier (Iron -> Diamond -> Netherite -> Soul Alloy -> Astralite -> Mithril -> Adamantite -> Spirit Jade -> Astral -> Celestial -> Primordial), eliminating overpowered skips.
* **Dimensional Progression Locks:** Advanced upgrade smithing templates, dimensional furnaces, and key relics are strictly locked behind dimension visits and System tiers (System 1.0 through System 4.0), preventing out-of-sequence gear acquisition.
* **Adjusted Coin Economy:** Item costs, pouch upgrades, and furnace investments have been scaled realistically to match high-level dungeon hunting yields.

---

## 🛡️ New 3D Armors & Legendary Equipment
* **Custom 3D Animated Armor Sets (GeckoLib):**
  * **Soulforge Plate:** Imbued with pulsating blue spectral fire and spiritual warding.
  * **Astralite Regalia:** Sleek starlight mail that amplifies mana capacity and critical strikes.
  * **Abyssal Vanguard Armor:** Heavy shadowy plate forged from dark rift metal for frontline survivability.
* **Matching Weapons & Tools:** Soul-imbued swords, heavy waraxes, katanas, pickaxes, and daggers with tailored attack animations and impact sound effects.

---

## 💎 New Minerals & Metallurgy: Soul Alloy & Astralite
* **Soul Alloy Progression:** Discover dormant Soul Alloy ingots and smelt them inside high-temperature Blast Furnaces to forge radiant **Ignited Soul Alloy Ingots**.
* **Astralite:** Rare celestial ore harvested across dimensional rifts, used for crafting high-tier magical catalysts and astralite gear.

---

## 🏛️ World Structure Redesigns
* **Nine Tribulations Pagoda Redesign:** Complete architectural overhaul of the sacred Overworld Pagoda with deepslate-crimson temple aesthetics, seamless entrance gate detection, and stability improvements.
* **Roulette Pavilion Redesign:** The Overworld casino pavilion has been visually redesigned with a grander oriental pavilion roof, decorative pillars, and improved roulette table layout.
* **Creative Pagoda Sandbox:** Build and customize your own trial arenas outside the dimension using `/pagoda arena [stage]` or `/structure pagoda_arena [stage]`, complete with an auto-placed Structure Block configured to save your custom arena NBTs.

---

## 📦 Visual Improvements & Polish
* **Dungeon Chest Visual Upgrade:** Enhanced textures across all 7 tiers of locked dungeon treasure chests, offering crisp high-definition visuals in hand, inventories, and placed worlds.
* **New Interactive Blocks:** Functional Roulette tables, Soulforge Altars, and ornate decorative masonry.

---

## ⚔️ Combat Stability, Shadow Scaling & Anti-Crash Guard
* **Server Anti-Crash Protection:** Added re-entrant combat cascade protection preventing `StackOverflowError` crashes during chain cleave attacks or damage reflection loops.
* **True Shadow Stat Retention:** Extracted Shadow Soldiers now preserve 100% of their fallen target's scaled maximum health, attack damage, and high-tier RPG levels.
* **Weapon Progression Filter:** Stackable utility items (bowls, food, ingots, blocks) are now strictly excluded from weapon leveling and attribute progression.

---

## 🎨 Open-Source & Asset Acknowledgments
* **Diorama Mod Integration:** Real-time ray-traced voxel minimap and tactical map engine created by **Dara Adedeji** (`SunkenInTime`) under the MIT License ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/diorama) | [GitHub](https://github.com/SunkenInTime/minecraft-orthogonal-map)).
* **Limbo Dimension Aberrations:** 3D entity models for the *Spider Head Monster*, *Tall Three Eyed Stalker*, and *One Eyed Stilt Walker* created by **`@ziiwplay`** under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
* **The Entity of Limbo:** 3D entity model for *THE_ENTITY* created by **`@nori`** under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
* **Skulk Golem:** 3D guardian model created by **`@elma_taburlas`** under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
* *(Complete open-source licensing documentation: [CREDITS.md on GitHub](https://github.com/Landa6543/Awakening-System/blob/main/CREDITS.md)).*

---


# 📜 Awakening System — Changelog v0.3.2

> **Version Transition:** `v0.3.1` ➔ `v0.3.2`  
> **Minecraft Version:** `1.21.1`  
> **Mod Loader:** NeoForge (`21.1.249+`)  
> **Java Version:** Java 21 LTS  
> **Release Target:** `awakening_system-0.3.2.jar`  

---

## 🛡️ 1. Critical Fix: Dungeon Dimension Isolation & External Mod Mob Infiltration Fix

A critical bug was resolved where players in Rank D dungeons encountered external mod mobs (e.g., wolves and ants from *Mo' Creatures*, Alex's Mobs, or creature mods) scaled up to Level 70–80, while native dungeon mobs properly stayed around Level 20:

* **Preservation of Overworld Distance Scaling (Core Feature):**
  * The core RPG system of **distance-based mob level scaling** (*"the further away from spawn, the higher the mob level"*) remains **100% active and preserved** throughout the Overworld and standard dimensions (Lv. 1–20 near spawn up to Lv. 80–100+ in distant zones).
* **Root Cause: Foreign Mod Mob Infiltration in Dungeon Realm:**
  * Dungeon instances generate inside their own dimension (`DUNGEON_REALM`) at distant coordinates (X >= 10,000, Z >= 10,000).
  * External creature mods were naturally spawning in the dark chambers of the dungeon dimension. Because those external mobs did not belong to the dungeon system, they were erroneously subjected to coordinate-based distance scaling, artificially inflating their level to 75–80 with extreme damage and health.
* **Triple-Layer Strict Dungeon Entity Isolation Filter:**
  * **Layer 1 (Natural Spawn Suppression):** `FinalizeSpawnEvent` cancels any natural or chunk-based mob spawn inside `DUNGEON_REALM`.
  * **Layer 2 (Highest-Priority Join Interception):** In `EntityJoinLevelEvent` (`HIGHEST` priority), any living entity entering `DUNGEON_REALM` that is NOT a player, NOT a player shadow soldier (`shadow_soldier`), NOT a tamed pet, and NOT a registered dungeon minion/boss (`dungeon_mob`) is immediately cancelled and discarded.
  * **Layer 3 (Active Session Tick Purge):** Active dungeon sessions continuously sweep a 150-block radius around the chamber every second (20 ticks), instantly purging and discarding any non-whitelisted foreign entities or leaked ambient creatures.
  * Dungeon instances are now strictly isolated: **ONLY** Awakening System dungeon minions, bosses, shadow soldiers, players, and pets can ever exist inside dungeons.
* **Baran & Igris Theme Minion and Boss Stat Scaling:**
  * Minions and mini-bosses in Baran and Igris themed chambers previously had hardcoded Rank S baseline stats (180–450 HP and 12–20 base DMG) across all ranks.
  * Health, attack damage, and armor now scale proportionally with dungeon rank tier (`tierRatio = Math.max(0.30D, (double) tier / 7.0D)`). In a Rank D dungeon, minions now appropriately possess 50–75 HP and 4–5 base attack damage.
  * Baran and Igris boss armor and knockback resistance now scale smoothly according to rank tier instead of having max armor in low-tier portals.

---

## 🌐 2. System Versioning & Dimension Side Tabs Progression Fix

* **Dimension-Based System Progression (Not Level-Based):**
  * System updates are tied directly to dimensional travel rather than player level. Passing into each realm unlocks its corresponding System tier:
    * **Default / Overworld:** Base System 1.0 (standard system screen without external dimension tabs).
    * **System 2.0 (Isekai Arrival):** Unlocked upon entering the Isekai Realm (`isekai_world`), enabling Overworld, Isekai, and Subspace navigation tabs.
    * **System 3.0 (Murim Arrival):** Unlocked upon entering the Murim Realm (`murim_world`), enabling the Murim navigation tab.
    * **System 4.0 (Teyvat Arrival):** Unlocked upon entering Teyvat (`teyvat_world`), enabling the Teyvat navigation tab.
    * When players are in any other realm, return navigation tabs remain accessible to allow easy transit back.
* **Fix for LAN / OP Privilege Bypass:**
  * Removed `isDev` check (`hasPermissions(2)`) from `getActiveSideTabs()`. Previously, any LAN host or player with operator status was flagged as a developer, bypassing dimensional progression checks and rendering all 5 dimension tabs at Level 1.

---

## 📦 3. Shop Mystery Boxes Level Lock Requirements

Minimum level requirements were introduced for purchasing loot boxes in the Hunter Point Shop:

* **Common Mystery Box (`box_common`):** Requires **Level 10** (Cost: 350 Coins).
* **Lucky Mystery Box (`lucky_box`):** Requires **Level 20** (Cost: 850 Coins).
* **Rare Mystery Box (`box_rare`):** Requires **Level 35** (Cost: 2,200 Coins).
* **Epic Mystery Box (`box_epic`):** Requires **Level 55** (Cost: 6,500 Coins).
* **Legendary Mystery Box (`box_legendary`):** Requires **Level 75** (Cost: 20,000 Coins).
* Purchase attempts below the required level display a clear notification stating the minimum level needed.

---

## ⚡ 4. Dungeon Break Event Fixes & Hardening

* **Chunk Pre-loading:** The event now pre-loads a 3x3 chunk area surrounding the rift prior to collapse, guaranteeing that terrain is fully generated and ready before monster emergence.
* **Persistence & Daylight Protection:** Monsters breaking through gain persistence (preventing despawn when players distance themselves) and sunlight immunity so the horde does not burn during the day.
* **Break Leader:** Every dungeon break spawns a designated Break Leader with buffed combat statistics and special naming.
* **Full Localization:** Translatable broadcast announcements and alerts configured for all languages (`es_es`, `es_cl`, `en_us`, `ru_ru`).
* **Cooldown Protection:** Added a 10-minute cooldown and 180-block radius restriction between consecutive breaks to prevent overlapping crises.

---

## 🗺️ 5. Biome & Dimension Titles Integration (Traveler's Titles)

* Implemented full compatibility with the **Traveler's Titles** mod format across all 40 mod biomes and 28 dimensions:
  * Dimension titles: `"travelerstitles.awakening_system.<dim>"` with matching hex color overrides `"travelerstitles.awakening_system.<dim>.color"`.
  * Biome titles: `"biome.awakening_system.<biome>"` and `"travelerstitles.biome.awakening_system.<biome>"` with custom hex colors `"travelerstitles.biome.awakening_system.<biome>.color"`.
  * Vanilla biome title overrides for Spanish locales (`travelerstitles.biome.minecraft.<id>`) ensuring that players in Spanish games see translated names rather than raw English terms.

---

## ⚔️ 6. Dokkaebi Server-Wide Timed Sub-Scenarios & Progressive Overkill Overhaul

* **Synchronized Global Server Event:**
  * Replaced individual, out-of-sync player tick triggers with a centralized server event (`onServerTick` every ~20 minutes / 24,000 ticks).
  * Dokkaebi Bihyung initiates a Star Stream emergency broadcast to all active Overworld hunters simultaneously with celestial chime acoustics.
  * Dokkaebi spawns near active players, and each hunter receives their own individual HUD tracking card and percentage progress bar.
* **Progressive Difficulty Tiers & Generous Timers:**
  * Scenario targets dynamically adapt according to highest hunter level on the server, with significantly extended hunt timers:
    * **Tier 1 (Basic Hunt):** 8–10 kills across **6:00 minutes** (increased to eliminate scarcity stress in quiet biomes). Base reward: **250 Coins, 400 EXP**.
    * **Tier 2 (Advanced Hunt):** 15–18 kills across **8:00 minutes**. Base reward: **400 Coins, 700 EXP**.
    * **Tier 3 (Elite Hunt):** 20–24 kills across **10:00 minutes**. Base reward: **600 Coins, 1,100 EXP**.
    * **Tier 4 (Star Stream Calamity):** 25–32 kills across **12:00 minutes**. Base reward: **850 Coins, 1,600 EXP**.
* **Universal Kill Detection:**
  * Fixed an issue where indirect kills failed to register. The engine now recognizes melee attacks, bow/trident projectiles, lingering Fire Aspect/poison damage, and kills performed by summoned Shadow Soldiers or tamed allies.
  * Properly tracks kills across all hostile entities, creature mods, and custom RPG mobs.
* **Escalating Overkill Rates & Constellation Gift Milestones:**
  * Exceeding the quota transforms the HUD card into a radiant gold frame and grants scaling rewards per extra kill:
    * **1–5 Extra Kills:** +50 Coins & +80 EXP each (Rank: *Combat Overload*).
    * **6–10 Extra Kills:** +75 Coins & +120 EXP each (Rank: *Destructive Frenzy*).
    * **11–20 Extra Kills:** +100 Coins & +160 EXP each (Rank: *Stellar Massacre*).
    * **21+ Extra Kills:** +150 Coins & +250 EXP each (Rank: *Absolute Calamity*).
  * Achieving Overkill milestones triggers real-time fanfares and awards physical Constellation Gifts deposited directly into player inventories upon completion:
    * **+5 Extra Kills:** +150 Coins donation bonus + **1x Mana Crystal Shard (Rank E)**.
    * **+10 Extra Kills:** +350 Coins donation bonus + **1x Mana Crystal Shard (Rank D)**.
    * **+15 Extra Kills:** +700 Coins donation bonus + **1x Mana Crystal Shard (Rank C)**.
    * **+20 Extra Kills:** +1,200 Coins donation bonus + **1x Mana Crystal Shard (Rank B)**.
    * **+30 Extra Kills:** +2,500 Coins donation bonus + **1x Mana Crystal Shard (Rank A)**.
* **Safe NPC Interaction & Automatic Resolution:**
  * Talking to Dokkaebi after reaching the quota will no longer forfeit remaining hunt time; Bihyung displays remaining time and encourages continuous Overkill hunting.
  * Hunters may Sneak + Right-Click to cash out early if desired.
  * Timer expiration automatically resolves all base rewards, Overkill coin/EXP bonuses, level-up calculations (`awardExperience`), and fireworks.
* **Full Trilingual Localization:**
  * All announcements, actionbar overlays, dialog prompts, rank tags, and completion breakdowns are fully localized with dedicated translatable keys in **Spanish (`es_es`, `es_cl`)**, **English (`en_us`)**, and **Russian (`ru_ru`)**.

---


# 📜 Awakening System — Changelog v0.3.1

> **Version Transition:** `v0.3.0` ➔ `v0.3.1`  
> **Minecraft Version:** `1.21.1`  
> **Mod Loader:** NeoForge (`21.1.249+`)  
> **Java Version:** Java 21 LTS  
> **Release Target:** `awakening_system-0.3.1.jar`  

---

## 🌲 1. Custom Woods, Logs & Worldgen Crafting Polish

A comprehensive registration and drop audit was conducted across all custom realm woods (**Cuihua, Adhigama, Sandbearer, Spirit Plum, Celestial Pine, Mana Wood, Eldoria, Aralia, Yumemiru, Otogi, and Maple**):

* **Axe Mining Speed Parity (`#minecraft:mineable/axe`)**:
  * Corrected tool tag assignments. All axe tiers (wooden, stone, iron, golden, diamond, and netherite) now harvest all custom logs, wood blocks, and planks at normal mining speeds with full efficiency.
* **Minecraft & NeoForge Tag Integration**:
  * Integrated all custom woods into standard tags: `#minecraft:logs`, `#minecraft:logs_that_burn`, `#minecraft:planks`, `#minecraft:fences`, `#minecraft:fence_gates`, and `#minecraft:wooden_fences`.
  * Guarantees full out-of-the-box compatibility with furnace fuels, recipe viewers (JEI/REI/EMI), and external storage mod filters.
* **Plank Crafting Recipes**:
  * Added recipes converting **1 Log ➔ 4 Planks** across all custom wood families, craftable in both crafting tables and the 2×2 player inventory grid.
* **Drop Tables & Harvesting**:
  * Fixed loot tables so that logs, stripped variants, leaves, stairs, slabs, fences, gates, doors, and trapdoors drop their intended item forms when mined.
* **Axe Stripping & Campfire Log Seats**:
  * Implemented right-click axe stripping behavior on custom logs.
  * Added the functional campfire log seat block (`CampfireLogSeatBlock`).

---

## 🗺️ 2. Standalone Tactical Minimap & Fullscreen World Map

A complete, built-in in-house map engine operating natively without requiring JourneyMap, Xaero's, or any other external map mod:

* **In-Game HUD Minimap (`HunterMinimapRenderer`)**:
  * Clean, border-accented tactical HUD minimap rendered in the corner of the screen.
  * Real-time coordinate HUD ($X, Y, Z$), active biome indicator, and smooth player heading pointer.
  * Position, scale, opacity, and visibility can be freely adjusted through the System HUD Editor (`GuiConfigScreen`).
* **Fullscreen Tactical World Map (`[M]`, `HunterWorldMapScreen`)**:
  * Accessible at any moment via the default **`[M]`** keybinding.
  * Fluid click-and-drag mouse panning and mouse-wheel zoom scaling.
  * Dimension switcher supporting the Overworld, Nether, End, Subspace, Abyss, and custom realms.
  * In-game time display, real-time cursor coordinates, and Day/Night surface lighting toggle.
* **High-Performance Binary Chunk Cache (`WorldMapChunkCache`)**:
  * Multi-threaded asynchronous chunk rasterization with zero tick lag or FPS drops during high-speed exploration.
  * Per-world binary cache files (`.bin`) stored in `run/awakening_map/` for instantaneous map reload.

---

## 🎨 3. JourneyMap / Xaero's Visual Parity & Shading

* **Natural Earthy Terrain Tints**:
  * Eradicated oversaturated, garish, and fluorescent coloring across biomes.
  * Applied JourneyMap's exact grass calibration formula: a $0.74\times$ luminance multiplier applied to vanilla biome grass tints to produce natural, soothing pasture greens.
  * Foliage and leaves calibrated with a $0.78\times$ factor for authentic forest canopies.
* **Northwest Hill-Shading Relief (3D Topography)**:
  * Dynamic hill-shading relief where northwest-facing slopes receive sunlight illumination and southeast-facing slopes receive topographic shadowing.
  * Accurately reveals cliffs, ravines, rolling hills, and mountain ridges with authentic depth.
* **Realistic Water Bathymetry**:
  * Depth-based alpha blending calculates water depth relative to the seabed or riverbed.
  * Renders shallow beaches and coastlines in translucent turquoise/aquamarine, transitioning into deep navy blue for ocean trenches.
* **Comprehensive Block Palette Mapping**:
  * Accurate pixel color rendering for stone, deepslate, gravel, sand, dirt, clay, snow, ice, terracotta, and realm ores.

---

## 📡 4. Mob Radar Dynamic Vertical Slicing & Density Control

* **Surface vs. Cave Vertical Culling**:
  * **Surface Exploration**: Subterranean entities dwelling in deep caverns below ground (`ly < surfaceY - 5.0`) are automatically culled using `Heightmap.Types.MOTION_BLOCKING`, keeping the map clear of cave clutter while on the surface.
  * **Cave / Underground Exploration**: When below ground, the radar automatically restricts mob display to a vertical slice of $\pm 8.5$ blocks relative to the player's current $Y$ level, showing only entities on the active floor or gallery.
* **Passive Herd Animal Proximity Limiter**:
  * Added density culling for clustered farm animals (cows, sheep, pigs, chickens, rabbits): clusters are capped at a maximum of 2 icons per 7-block radius.
  * Hard limits on rendered passive icons: maximum 10 on the minimap HUD, maximum 24 on the fullscreen world map.
* **Tactical Entity Radar Indicators (`EntityIconRenderer`)**:
  * Color-coded radar markers: Bosses (crimson skulls), Hostile Monsters (red markers), Passive/Neutral Creatures (green/yellow markers), Friendly NPCs & Players (distinct cyan/white pointers).

---

## 📍 5. Per-World Waypoint Isolation & GUI Brightness Fix

* **Per-World Waypoint Storage**:
  * Waypoints are now stored independently in `config/awakening_waypoints/<world_id>.json`, keyed by the world folder name (`sp_<folder>`) for singleplayer or server address (`mp_<ip>`) for multiplayer.
  * **Multi-World Bleed Bug Resolved**: Newly generated worlds start completely clean with **0 waypoints**, preventing waypoints from leaking across different saves.
  * Cache is automatically purged upon leaving a world (`ClientPlayerNetworkEvent.LoggingOut`).
* **Waypoint Editor GUI Brightness Fix (`WaypointEditScreen`, `WaypointListScreen`)**:
  * Fixed an issue where the RGB color picker wheel, sliders, and preview swatches appeared dark and washed out. The duplicate background gradient call (`super.render`) was removed, restoring 100% RGB brightness and color vibrancy.
  * In-game management: create, rename, coordinate edit, toggle visibility, and assign custom RGB colors.
* **In-World 3D Beacons (`WaypointBeaconRenderer`)**:
  * Renders customizable vertical light beams and floating distance tags in the 3D game world pointing toward marked waypoints.

---

## 🧘 6. Meridian System & Dantian Cultivation Engine

* **Interactive Acupuncture & Acupoint Interface (`MeridianScreen`)**:
  * Dedicated interactive body cultivation screen accessible from the hunter menu.
  * Renders human meridian channels and major extraordinary vessels (*Ren Mai* - Conception, *Du Mai* - Governing).
  * Major acupoints (*Baihui*, *Yintang*, *Tanzhong*, *Qihai*, *Mingmen*, *Yongquan*, Lower Dantian) featuring distinct visual states: Locked, Breakthrough Ready (pulsing glow), and Opened (radiant golden/crimson energy).
* **Alchemical Breakthroughs & Stat Progression**:
  * Opening acupoints consumes alchemical breakthrough pills (*Marrow Cleansing Pill*, *Realm Breaking Pill*) and cultivation Qi.
  * Unlocking complete meridian circuits grants permanent passive boons: amplified Qi regeneration, reduced skill cooldowns, increased physical damage, and natural regeneration.

---

## ⚔️ 7. Epic Fight Official Integration & Native ACE Engine Retirement

Based on player testing and community preference, the native ACE prototype has been cleanly retired in favor of **official out-of-the-box integration with Epic Fight (1.21.1)**:

* **Clean Decoupling & Keybinding Liberation**:
  * Deactivated client attack interception mixins (`AceMinecraftClientMixin`), restoring standard attack event flow so Epic Fight controls combat inputs.
  * Completely freed up the **`[R]`** keybinding (previously reserved for ACE stance toggles), allowing Epic Fight to use its default key for toggling Mining/Combat mode without key collisions.
  * Retired the custom ACE HUD overlay to eliminate visual clutter and avoid overlaps with Epic Fight's UI.
* **72 Custom Weapon Capabilities Configured for Epic Fight**:
  * Audited and optimized weapon capability definitions across all custom weapons in `src/main/resources/data/awakening_system/capabilities/weapons/`:
    * **Swords & Rapiers** (e.g. *Absolution, Soul Solid, Primordial Sword, Celestial Sword, Astral Sword*): Configured to `epicfight:sword` (fluid one-handed and dual-wield combos).
    * **Greatswords & Scythes** (e.g. *Black Dragon Scythe, Crimson Moon's Semblance, Creator Claymore*): Configured to `epicfight:greatsword` (heavy two-handed cleaves, momentum swings, and sweeps).
    * **Katanas & Feijian / Murim Blades** (e.g. *Plum Blossom Sword, Sky Demon Sword, Heaven Shaking Sword, Tailless, Shunguang Sword*): Updated to `epicfight:tachi` (elegant two-handed slashing moveset with authentic posture).
    * **Spears & Polearms** (e.g. *Astral Guandao, Black Tassel*): Configured to `epicfight:spear` (long-reach linear thrusts and guard stances).
    * **Daggers** (e.g. *Hunter Dagger*): Configured to `epicfight:dagger` (rapid alternating slashes and dual-dagger combinations).
    * **Axes & Warhammers**: Configured to `epicfight:axe`.
    * **Bows** (e.g. *Amos Bow, Aqua Simulacra, Thundering Pulse, Skyward Harp, Polar Star*): Configured to `epicfight:bow`.
* **Dual Combat Mod Ecosystem Support (Better Combat & Epic Fight)**:
  * **Better Combat Integration**: All 63 melee weapons have custom attributes configured in `data/awakening_system/weapon_attributes/` (katanas with `bettercombat:katana`, scythes with `bettercombat:scythe`, claymores with `bettercombat:claymore`, spears with `bettercombat:spear`, daggers with `bettercombat:dagger`, swords, and axes).
  * **Epic Fight Integration**: All 72 weapons and bows have capabilities configured in `data/awakening_system/capabilities/weapons/` (`epicfight:sword`, `epicfight:tachi`, `epicfight:greatsword`, `epicfight:spear`, `epicfight:dagger`, `epicfight:axe`, `epicfight:bow`).
* **System Active Skills & Spells Preservation**:
  * All right-click weapon abilities and custom hotkey System abilities (Cultivation Qi `[C]`, Stigmas `[Y]`, Spell Slots `[V]`, `[G]`, `[B]`, `[X]`, Martial Arts, Armor Skills `[Z]`, and Hunter World Map `[M]`) remain fully accessible and responsive alongside both Better Combat and Epic Fight.

---

## 🐉 8. Celestial Dragon Serpentine Animation & Wyvern Kinematics

* **Oriental Serpentine Wave Kinematics (`CelestialDragonModel.java`)**:
  * Eliminated rigid linear poses during flight and hovering.
  * Implemented an authentic sinusoidal wave propagation function across all 31 articulated vertebrae:
    $$\theta_{\text{yaw}}(i) = A_{\text{yaw}} \times \sin(\omega t - i \cdot k) + \Delta \text{yaw}_{\text{turn}}$$
    $$\theta_{\text{pitch}}(i) = A_{\text{pitch}} \times \cos(\omega t - i \cdot k) + \Delta \text{pitch}_{\text{turn}}$$
  * Produces continuous, majestic 3D helical swimming curves through clouds and sky.
  * **Jitter & Phase Shearing Elimination**: Resolved the issue where the dragon shuddered violently upon spawning:
    * **Constant-Frequency Monotonic Time**: Fixed phase shears caused by multiplying dynamic speed by elapsed ticks ($t \cdot \Delta \omega$). The wave now uses a strictly continuous, monotonic frequency ($\omega = 0.085\,\text{rad/tick}$), smoothly scaling only the amplitude with movement speed.
    * **Stable Kinematic Root**: Kept `neck1.yRot = 0.0F` so downstream body joints do not whip the world coordinate anchor of the 31-joint chain.
    * **Vertebral Differential Clamping**: Clamped inter-segment rotational differentials to $\pm 14^\circ$ yaw and $\pm 10^\circ$ pitch with cervical ease-in dampening, eliminating snapping during sharp turns.
    * **Smooth Sitting Damping**: Replaced boolean pose popping with continuous amplitude damping ($0.35\times$) during sitting.
  * **Fluid Whiskers, Tail Tuft & Claws**: Silk-like trailing inertia on dragon whiskers, whip-like tail tuft swaying, and coordinated ethereal paddling gestures on divine claws.
* **Ancient Celestial Dragon Boss Colossal Coiling (`AncientCelestialDragonBossModel.java`)**:
  * Calibrated monotonic wave propagation across all 63 chain vertebrae (spanning $+100$ blocks in the world), rendering up to 1.8 full serpentine coils sweeping across the Murim sky at $\omega = 0.055\,\text{rad/tick}$.
  * Completely stabilized against jitter with clamped vertebral angles and root yaw locking.
  * Dynamic body tension and whip lash during roar and Qi breath attacks.
* **Wyvern 3D Flight & Banking Roll (`WyvernModel.java`)**:
  * Upgraded flat wing flapping to true 3D wing kinematics combining vertical flap, aerodynamic sweep, and feathering angle of attack.
  * Decoupled flight and ground frequencies to prevent phase jumping during takeoff and landing transitions.
  * Added realistic body banking roll when turning, tucked flight legs, and sinusoidal tail stinger whip.

---


# 📜 Changelog — Awakening System v0.2.1 (Hotfix)

## 🛠️ v0.2.1 Hotfix Highlights
- **Responsive Top-Up & Gift Card Store UI:**
  - Resolved window boundary overflow and layout clipping where modal borders, right columns, and titles were pushed off-screen on smaller resolutions or higher GUI scales (GUI Scale 3/4).
  - Implemented dynamic auto-fitting and scaling for all headers, balances, gift cards, and crystal packages.
  - Buttons and mouse click detection perfectly synchronized with dynamic layout.
- **100% Multilingual Localization Parity (11 Supported Languages):**
  - Completed exact 5,093 translation keys across English (`en_us`), Russian (`ru_ru`), Spanish (`es_es`), and all Latin American Spanish regional dialects (`es_mx`, `es_ar`, `es_cl`, `es_co`, `es_ec`, `es_pe`, `es_uy`, `es_ve`).
  - Added missing Russian keys for Teyvat dimensions, Pagoda trials, ores, and sound subtitles.
  - Added missing English keys for Wyvern entities, items, and Paimon sound subtitles.
  - Fully localized Blessing of the Welkin Moon screen, claim notices, and Gift Card tooltips.
- **Modal Persistence on Purchases & Conversions:**
  - Players are no longer abruptly kicked back to the banner screen when buying Gift Cards or converting Genesis Crystals / Stellar Jade; balances now update live in place.
- **Ranker Tower Ticket Progression Softlock Fixed:**
  - Resolved a progression gating error in the System Shop where the **Ranker Tower Ticket** was mistakenly locked behind *System 2.0 (Isekai)*. Since conquering the Ranker Tower in System 1.0 is the canonical prerequisite to activate the portal and ascend to the Isekai world, the ticket is now properly available for purchase in **System 1.0**.
- **Gacha Wish Soundscape Polish:**
  - Replaced an unimmersive wooden chest opening sound (`CHEST_OPEN`) when wishing with a full inventory with an authentic celestial magical chime (`AMETHYST_BLOCK_CHIME`) alongside instant tactile button click audio feedback.
- **Genshin-Style 2X Double First Top-Up Bonus:**
  - The first purchase of any Genesis Crystal package (60, 330, 1090, 2240, 3880, or 8080) now awards **double crystals (2X)**, mirroring Genshin Impact!
  - Unclaimed bonuses are marked in the store UI with an authentic gold `[2X]` tag and detailed preview tooltips.
- **Creative Mode Inventory Safety:**
  - Gift Cards are no longer consumed from the player's hand when right-clicked in Creative mode (`instabuild`).

---

# 📜 Changelog — Awakening System v0.2.0 (Beta)

> [!NOTE]
> ### 📢 Why is v0.2.0 Releasing Early as a Public Beta?
> Version **0.2.0** is being published ahead of schedule as a **Public Beta** build to prioritize immediate gameplay stability, solve critical modpack conflicts, and gather player feedback while remaining late-game content continues active development:
> 
> 1. **Immediate Modpack Anti-Stuck & Boundary Failsafes:**
>    - Many players experienced game-breaking boundary entrapment or void drops when using random teleportation items and spells from popular mods (such as the *Chorus Totem* from the *Artifacts* mod, *Iron's Spells 'n Spellbooks*, *Wizards*, vanilla Chorus Fruits, and Ender Pearls).
>    - Rather than withholding these crucial bugfixes until the entirety of v0.2.0 is finalized, this Beta delivers instant spatial containment and auto-rescue protection for servers and singleplayer worlds.
> 2. **Early Community Testing & Balancing:**
>    - Players can immediately experience and test the new **Spiral Abyss (Espiral del Abismo)** dimension, complete with authentic **Genshin Impact monsters (Slimes, Hilichurls, Mitachurls)** featuring 100% original studio soundscapes and 3D models.
> 3. **Active Iterative Development:**
>    - Advanced late-game features (higher Spiral Abyss trial chambers, expanded Murim Dan alchemy recipes, and additional cultivator weapons) are actively being developed and will roll out smoothly in upcoming beta patches leading to the final stable release.

---

## 🛡️ Modpack Compatibility & Failsafe System (Anti-Stuck Protection)
- **Dungeon Boundary Teleport Containment:**
  - Added spatial boundary event interception (`EntityTeleportEvent`) to fully support accessory, magic, and combat mods (such as the **Chorus Totem** from the *Artifacts* mod, *Iron's Spells 'n Spellbooks*, *Wizards*, vanilla Chorus Fruits, and Ender Pearls).
  - Any random or spell-based teleportation attempt that would land outside the bedrock bunker or inside solid walls is automatically **contained and redirected to safe ground in the active chamber center**.
- **Bedrock & Block Breaking in Creative Mode:**
  - Players in **Creative Mode** (`isCreative()` / `instabuild`) can now break Bedrock, barriers, and any blocks inside Dungeons, Ranker Towers, and Pagoda Trials for inspection, construction, and rescue purposes.
- **Continuous Out-of-Bounds Watchdog:**
  - Continuous real-time positioning watchdog running every tick. If a player glitches through geometry due to lag, knockback, or high-speed dashes, they are instantly relocated back onto the active chamber floor with zero velocity.
- **Stranded Player Auto-Rescue:**
  - Players who disconnect or have corrupted saves stranded outside active dungeon instances (e.g. from previous teleport bugs) are automatically detected upon login and **safely teleported back to their Overworld spawn point or bed**.
- **Universal Escape Commands (No OP Required):**
  - Emergency escape commands available to all players without cheats or operator permissions: `/escape`, `/dungeon exit`, `/dungeon escape`, `/tower exit`, and `/pagoda exit`.

## 🎵 Authentic Genshin Impact Monster Soundscapes (100% Original Audio)
- **Elemental Slimes:**
  - Replaced vanilla sounds with official, original studio sound effects from Genshin Impact (48,000 Hz Vorbis).
  - Authentic cute vocalizations, bloops, squishes, water chirps, springy jumps, and death pops.
- **Hilichurls:**
  - Iconic native Hilichurlian battle chants and vocalizations ("*Ya!*", "*Olah!*", "*Yaika!*").
  - Authentic hurt vocal grunts, combat shouts, and distinct death cries.
- **Mitachurls:**
  - Deep, resonant guttural roars and menacing battle growls.
  - Custom sound events for **Blazing Axe Earth Slam** and **Tower Shield Charge Dash**.
  - Authentic impact grunts and thunderous defeat roars.

---

## 🍮 Volumetric Squash & Stretch 2.0 & Ultra-Round 3D Mesh
- **Canonical Non-Overlapping Faceted Mesh (Zero Z-Fighting / Anti-Flicker):**
  - Completely eradicated polygon flickering and texture jittering ("tiritan") by restoring the canonical disjoint 26-box Blockbench structure where no quads overlap or share identical spatial planes.
  - Symmetrical mirrored UV mapping for Anemo gliding wings ensuring crisp matching white-feathered textures on both sides.
- **Elemental Texture Palette Purification (100% Cyan Artifact Removal):**
  - Thoroughly cleaned all non-Hydro elemental texture atlases (*Pyro, Electro, Dendro, Cryo*), replacing over 13,000 unpainted leftover cyan template pixels with authentic warm flame gradients, deep lightning purples, lush forest greens, and frost crystal whites.
  - Pyro, Electro, and Dendro slimes will never display miscolored cyan/blue domes or flanks.
- **Mathematical Volume Preservation:**
  - Slimes now deform with accurate volumetric elasticity ($\text{width} = \sqrt{1 / \text{height}}$).
  - Launching into the air causes snappy vertical elongation (stretch).
  - Hitting the ground triggers deep horizontal pancake expansion (squash).
  - Added organic dynamic tilt angles based on velocity when airborne.
  - Fully synchronized on both client and server ticks.

---

## 🌀 Spiral Abyss: 3D Ley Line Monolith, Red Challenge Key & Arena Boundaries
- **3D Ley Line Monolith (Monolito de las Líneas Ley):**
  - Replaced temporary vanilla blocks and invisible Iron Golems with the authentic floating 3D Monolith from Genshin Impact for defense chambers (Floor X - Chamber 1).
  - Features an ancient stone domain pedestal, a levitating central obelisk with glowing Ley Line conduits, an inner glowing cyan core, two counter-rotating orbital runic rings, and apex crystal shards.
  - Emits periodic taunt/aggro pulses that draw all Abyss monsters (slimes, hilichurls, mitachurls) to prioritize attacking it.
  - Slashes/impacts trigger crystal resonance sound effects and cyan sparks. Real-time health percentage displayed on both its holographic name tag and the chamber BossBar.
- **3D Red Challenge Key (Llave Roja de Desafío):**
  - Replaced terracotta and armorstand placeholders with the iconic Genshin Impact floating crimson challenge sword/key and orbital dial.
  - Hovers in the center of combat chambers during preparation (`WAITING_FOR_TRIGGER`).
  - Right-clicking or hitting the mechanism triggers authentic Genshin challenge fanfare (`SoundEvents.UI_TOAST_CHALLENGE_COMPLETE` + beacon bell), bursts into crimson flame shockwaves, and initiates the chamber battle.
- **High-Altitude Barrier Wall:**
  - Added an impenetrable 16-block-high wall of invisible barrier blocks (`Blocks.BARRIER`) surrounding the entire circular arena perimeter ($r = 18$).
  - Prevents players, mobs, and projectiles from falling into the deep dark chasm or leaping out of bounds.
- **Active Containment Watchdog:**
  - Real-time tick monitor catches any entity or player forced outside the platform by extreme knockback, spells, or clipping bugs, immediately teleporting them safely back onto the arena floor.

---

## 👾 Authentic Genshin Impact Monster Lineup
- **Elemental Slimes (Teyvat):**
  - Completely overhauled with ultra-round 3D dumpling geometry and exact UV mapping.
  - Authentic high-resolution textures for all 7 elements: *Pyro, Hydro, Anemo, Electro, Cryo, Geo, and Dendro*.
  - Full 3D canonical accessories: flapping glide wings (Anemo), frost spikes (Cryo), lightning rod horn (Electro), crystal horns (Geo), and blooming flower foliage (Dendro).
- **Hilichurls:**
  - Canonical Blockbench humanoid model with 3D sculpted bone mask relief and angled ear tufts.
  - 4 specialized variants with dedicated weapons and equipment:
    - **Fighter:** Wooden tribal club.
    - **Berserker:** Flaming torch club.
    - **Guard:** Reinforced wooden tribal shield.
    - **Shooter:** Functional wooden crossbow.
- **Mitachurls:**
  - High-definition (256x256) brute model featuring layered horns, thick neck mane, shoulder guards, and battle armor.
  - Dynamic equipment sets: **Blazing Magma Battleaxe** and **Massive Wooden Tower Shield**.

---

## 🌌 New Dimensions, Cultivation & Combat Progression
- **Spiral Abyss Dimension (Espiral del Abismo):**
  - 12 tiered trial floors with timed combat encounters, Abyssal Moon blessings, star progression, and reward vaults.
- **Murim Qi Cultivation & Cauldron Alchemy:**
  - Interactive **Murim Alchemy Cauldron** block that ignites over spiritual fires (fire, soul fire, campfire, magma, or lava) to refine immortal Dao pills (*Dan*).
  - Marrow Cleansing Pills (permanently unlock blocked meridians), Rising Dragon Pills, and Realm Breakthrough Elixirs.
- **Exotic Cultivator Weaponry:**
  - **Jade Flying Sword (*Feijian*):** Homing telekinetic projectile attack that automatically returns to the cultivator's hand.
  - **Astral Dragon Guandao:** Piercing celestial crescent blade wave.
  - **Adamantite War Hammer:** Ground slam and seismic shockwave fissure.
- **Sacred Yggdrasil World Tree Protection:**
  - Complete immunity against fire, lava griefing, explosions, and destructive lightning bolts.
  - Native Mana Pixie entities and spiritual herbs.
- **Physical 3D Roulette Crate:**
  - Boss altar now spins loot physically in mid-air before awarding the conqueror's prize.

---

## 📦 File Information
- **Target File:** `awakening_system-0.2.0.jar`
- **Mod Loader:** NeoForge 1.21.1
- **Release Channel:** Beta