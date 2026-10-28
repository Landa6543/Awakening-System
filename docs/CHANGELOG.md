# 📜 Changelog — Awakening System v0.3.1 (Tactical Map & Polish)

## 🗺️ v0.3.1 Highlights
- **Standalone Tactical Minimap & Fullscreen World Map (`[M]`):**
  - Completely custom, in-house map engine operating natively without JourneyMap or Xaero's Minimap.
  - Dynamic HUD minimap with real-time coordinates, biome name, player heading, and HUD editor customization.
  - Fullscreen World Map (`[M]`) with mouse drag-panning, wheel zoom, time/coordinate HUD, and dimension switching.
  - Multi-threaded binary chunk caching (`WorldMapChunkCache`) with instant loading and zero FPS lag.
- **Natural Terrain Shading & JourneyMap Visual Parity:**
  - Eradicated harsh neon/fluorescent biomes using JourneyMap's exact grass luminance calibration ($0.74\times$) and foliage formula ($0.78\times$).
  - Northwest hill-shading relief simulating sunlight on NW slopes and topographic shadow on SE slopes for true 3D contouring.
  - Water bathymetry with alpha blending (shallow turquoise waters vs deep navy ocean trenches).
- **Radar Dynamic Vertical Slicing & Mob Crowding Control:**
  - Surface vs Cave modes: subterranean cave mobs deep below ground (`ly < surfaceY - 5.0`) are culled when on the surface. In caves, mobs are restricted to $\pm 8.5$ blocks vertical slice.
  - Herd animal proximity culling: passive farm animals (cows, sheep, pigs, chickens) capped at 2 per 7 blocks, with strict limits of 10 on minimap and 24 on fullscreen map.
- **Per-World Waypoint Storage & GUI Brightness Fix:**
  - Waypoints stored per world in `config/awakening_waypoints/<world_id>.json`. New worlds now start with 0 waypoints (no bleed from previous worlds).
  - Waypoint edit screen brightness fix: removed duplicate background gradient call, restoring 100% vibrant RGB color wheel and preview swatches.
  - In-world 3D waypoint beacon beams and distance tags.
- **Custom Realm Woods, Logs & Worldgen Polish:**
  - Registered all custom logs (Cuihua, Adhigama, Sandbearer, Spirit Plum, Celestial Pine, Mana Wood, Eldoria, Aralia, Yumemiru, Otogi, Maple) to `#minecraft:mineable/axe`, `#minecraft:logs`, `#minecraft:planks`, etc.
  - Fixed axe mining speed, recipes for crafting 4 planks per log, campfire log seat blocks, stripping interactions, and drop loot tables.

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
