import json
import os

LANG_DIR = r"src/main/resources/assets/awakening_system/lang"

ES_KEYS = {
    "awakening_system.screen.guide.title": "§b§l✦ GUÍA DEL CAZADOR §7// §eMANUAL OFICIAL DEL SISTEMA ✦",
    "awakening_system.screen.guide.tab.system": "SISTEMA",
    "awakening_system.screen.guide.tab.ranks": "RANGOS",
    "awakening_system.screen.guide.tab.dungeons": "CALABOZOS",
    "awakening_system.screen.guide.tab.skills": "HABILIDADES",
    "awakening_system.screen.guide.tab.tower": "TORRE",
    "awakening_system.screen.guide.tab.cultivation": "CULTIVO",
    "awakening_system.screen.guide.tab.sponsors": "PATROCINIO",

    # SYSTEM TAB
    "awakening_system.screen.guide.system.header1": "§b§l[1. PROGRESIÓN Y EXPERIENCIA]",
    "awakening_system.screen.guide.system.text1_1": "• Ganas experiencia minando minerales, derrotando criaturas y cumpliendo misiones.",
    "awakening_system.screen.guide.system.text1_2": "• Cada nivel otorga Puntos de Atributo para forjar tu especialización única.",
    "awakening_system.screen.guide.system.header2": "§e§l[2. ATRIBUTOS DEL NÚCLEO]",
    "awakening_system.screen.guide.system.text2_1": "• Fuerza (Daño físico y minería) | Agilidad (Velocidad y evasión)",
    "awakening_system.screen.guide.system.text2_2": "• Vitalidad (Vida máxima y regeneración) | Percepción (Suerte y crítico)",
    "awakening_system.screen.guide.system.text2_3": "• Inteligencia (Energía y poder mágico) | Voluntad (Resistencia a efectos)",
    "awakening_system.screen.guide.system.header3": "§c§l[3. COMBO & MODO TRANCE (MANHWA ACCIÓN)]",
    "awakening_system.screen.guide.system.text3_1": "• Encadena 10 golpes seguidos sin recibir daño para entrar en §6Modo Trance§f.",
    "awakening_system.screen.guide.system.text3_2": "• En Trance ganas §a+35% Daño§f, velocidad sónica y partículas de sobrecarga.",
    "awakening_system.screen.guide.system.header4": "§6§l[4. DESPERTAR DE CLASES (NIVEL 30)]",
    "awakening_system.screen.guide.system.text4_1": "• Al alcanzar Nivel 30 desbloqueas tu especialización: Cazador, Asesino, Mago o Nigromante.",
    "awakening_system.screen.guide.system.header5": "§e§l[5. BANCO DEL SISTEMA Y MONEDAS FÍSICAS]",
    "awakening_system.screen.guide.system.text5_1": "• Clic derecho con Monedas del Despertar en mano para ingresarlas al saldo virtual al instante.",
    "awakening_system.screen.guide.system.text5_2": "• Usa la pestaña 'BANCO' en la Tienda para depositar o retirar lotes de monedas físicas.",

    # RANKS TAB
    "awakening_system.screen.guide.ranks.header1": "§6§l[ESCALA COMPLETA DE RANGOS DEL SISTEMA (41 RANGOS)]",
    "awakening_system.screen.guide.ranks.text1_1": "• Mortal: F (1) -> E (5) -> D (10) -> C (20) -> B (30) -> A (40) -> S (50)",
    "awakening_system.screen.guide.ranks.text1_2": "• Élite Humano: EX (70) -> SS (90) -> SSS (110) -> §6Nacional (130)§f [Cúspide Mortal]",
    "awakening_system.screen.guide.ranks.text1_3": "• Trascendencia: UR (150) -> UR+ (170) -> LR (190) -> SLR (210) -> MR (230) -> GR (250)",
    "awakening_system.screen.guide.ranks.text1_4": "• Soberanía: Trascendente (270) -> Trascendental (290) -> Monarca (310) -> Gobernante (330)",
    "awakening_system.screen.guide.ranks.text1_5": "• Deidades: Ser Supremo (350) -> Semidiós (370) -> Dios (400) -> Deidad (430) -> Ancestral (460)",
    "awakening_system.screen.guide.ranks.text1_6": "• Cósmico: Cósmico (490) -> Multiversal (520) -> Omniversal (550) -> Primordial (580)",
    "awakening_system.screen.guide.ranks.text1_7": "• Absoluto: Infinito (610) -> Omnipotente (640) -> Ser Absoluto (670) -> Absoluto (700)",
    "awakening_system.screen.guide.ranks.text1_8": "• Creación: Creador Supremo (730) -> Creador Absoluto (760) -> Soberano del Caos (800)",
    "awakening_system.screen.guide.ranks.text1_9": "• Deidades Finales: Monarca Atemporal (840) -> Deidad Astral (880) -> Singularidad (920)",
    "awakening_system.screen.guide.ranks.text1_10": "• §6Sin Límites (960) -> DIOS ABSOLUTO (1000)§f [Entidad Máxima del Multiverso]",
    "awakening_system.screen.guide.ranks.header2": "§b§l[CÓDIGO CROMÁTICO DE PORTALES]",
    "awakening_system.screen.guide.ranks.text2_1": "• §bCyan:§f Rangos E/D | §aVerde:§f C/B | §6Ámbar:§f A | §cCarmesí:§f S | §5Púrpura:§f SS a Nacional",
    "awakening_system.screen.guide.ranks.text2_2": "• §dOrquídea:§f Monarcas | §eDorado Celestial:§f Deidades (370+) | §4Red Gates:§f Mazmorras extremas",

    # DUNGEONS TAB
    "awakening_system.screen.guide.dungeons.header1": "§b§l[MAZMORRAS CONTINUAS DE 5 ETAPAS Y ARQUITECTURA GÓTICA]",
    "awakening_system.screen.guide.dungeons.text1_1": "• Las mazmorras son complejos continuos conectados a través de 4 Murallas Mágicas:",
    "awakening_system.screen.guide.dungeons.text1_2": "  1. §fAntecámara del Abismo:§7 Foso central hundido con vanguardia de enemigos.",
    "awakening_system.screen.guide.dungeons.text1_3": "  2. §fPasaje de Criptas:§7 Columnatas góticas, celdas con rejas de hierro y asesinos.",
    "awakening_system.screen.guide.dungeons.text1_4": "  3. §fFoso de Guerra y Magma:§7 Crisol circular hundido, pasarelas elevadas y Mini-Jefe.",
    "awakening_system.screen.guide.dungeons.text1_5": "  4. §fNave Catedralicia:§7 Techo abovedado de 12 bloques con contrafuertes y nigromantes.",
    "awakening_system.screen.guide.dungeons.text1_6": "  5. §6Salón del Trono Imperial:§e Estrado de 3 niveles con el Jefe Supremo (Igris / Baran).",
    "awakening_system.screen.guide.dungeons.header2": "§a§l[MURALLAS MÁGICAS Y TEMPORIZADOR]",
    "awakening_system.screen.guide.dungeons.text2_1": "• Para disolver una muralla debes eliminar a todos los enemigos de la fase actual.",
    "awakening_system.screen.guide.dungeons.text2_2": "• Cada muralla abierta otorga tiempo extra al temporizador para evitar que colapse.",
    "awakening_system.screen.guide.dungeons.header3": "§6§l[RECOMPENSAS Y ALTAR DE LA RULETA]",
    "awakening_system.screen.guide.dungeons.text3_1": "• Tras vencer al Monarca en la Fase 5, se manifestará el Altar de la Ruleta para reclamar tu botín. Regresa a la Sala 1 para cruzar el portal de salida.",

    # SKILLS TAB
    "awakening_system.screen.guide.skills.header1": "§d§l[TÍTULOS Y HABILIDADES DE RANGO ÚNICO (LÍMITE: 1 JUGADOR)]",
    "awakening_system.screen.guide.skills.text1_1": "• Solo un jugador por mundo/servidor puede ostentar estas distinciones:",
    "awakening_system.screen.guide.skills.text1_2": "  1. §5Monarca de las Sombras:§f Extracción Absoluta (Invoca sombras al instante).",
    "awakening_system.screen.guide.skills.text1_3": "  2. §bRey Demonio de la Salvación:§f La Cuarta Pared: Infinito (Cura masiva e inmunidad).",
    "awakening_system.screen.guide.skills.text1_4": "  3. §6El Más Fuerte:§f Autoridad del Gobernante (Telequinesis primordial).",
    "awakening_system.screen.guide.skills.text1_5": "  4. §cEmperador Dragón:§f Aliento Cósmico del Dragón (Rayo destructor total).",
    "awakening_system.screen.guide.skills.header2": "§9§l[MECÁNICA DE SOLDADOS DE SOMBRA]",
    "awakening_system.screen.guide.skills.text2_1": "• Derrota enemigos y extráelos para forjar tu ejército imperial de sombras.",
    "awakening_system.screen.guide.skills.text2_2": "• §bInmunidad Aliada:§f Tus soldados de sombra son completamente inmunes a tu daño.",
    "awakening_system.screen.guide.skills.text2_3": "• §bTeletransporte Automático:§f Si se alejan más de 16 bloques, regresan a tu espalda.",
    "awakening_system.screen.guide.skills.header3": "§e§l[EVOLUCIÓN DE HABILIDADES]",
    "awakening_system.screen.guide.skills.text3_1": "• Sube cualquier habilidad a nivel 10 para desbloquear su versión evolucionada.",

    # TOWER TAB
    "awakening_system.screen.guide.tower.header1": "§6§l[1. LA TORRE DE LOS RANKERS (100 PISOS)]",
    "awakening_system.screen.guide.tower.text1_1": "• Asciende por los 100 pisos de la Torre usando un Ticket de la Torre o /system tower.",
    "awakening_system.screen.guide.tower.text1_2": "• Comando de teletransporte para pruebas: /system tower floor <1-100>.",
    "awakening_system.screen.guide.tower.header2": "§e§l[2. CÁMARAS CERRADAS Y ESCALADO MONÓTONO]",
    "awakening_system.screen.guide.tower.text2_1": "• Cada piso es una sala cerrada con techo, 4 pilares estructurales y muros temáticos según el Tier.",
    "awakening_system.screen.guide.tower.text2_2": "• La salud (HP), daño y armadura de los enemigos escala estrictamente con el nivel del piso.",
    "awakening_system.screen.guide.tower.header3": "§b§l[3. PORTALES DE SHINSOO & CHECKPOINTS]",
    "awakening_system.screen.guide.tower.text3_1": "• Al eliminar a todos los monstruos se activa el Portal de Ascensión de Shinsoo para subir al siguiente piso.",
    "awakening_system.screen.guide.tower.text3_2": "• El portal sur y el Pergamino de Retorno guardan tu checkpoint de piso al volver al Overworld.",
    "awakening_system.screen.guide.tower.header4": "§d§l[4. CÚSPIDE (PISO 100) & PORTAL ISEKAI]",
    "awakening_system.screen.guide.tower.text4_1": "• En el piso 100 aguarda el Gran Administrador de la Torre (6,500 HP) y sus Guardianes Divinos.",
    "awakening_system.screen.guide.tower.text4_2": "• Al derrotarlo se abre el Gran Portal Isekai: Actualización 2.0 del Sistema y patrocinio masivo de las Constelaciones.",

    # CULTIVATION TAB
    "awakening_system.screen.guide.cultivation.header1": "§a§l[1. ENERGÍA DE QI Y MEDITACIÓN DE LOTO]",
    "awakening_system.screen.guide.cultivation.text1_1": "• El Qi es independiente del Maná. No se regenera solo ni con pociones: solo cultivando.",
    "awakening_system.screen.guide.cultivation.text1_2": "• Pulsa [C] o /system cultivate para meditar en posición de loto y acumular Qi celestial.",
    "awakening_system.screen.guide.cultivation.header2": "§6§l[2. REINOS DEL DANTIAN Y SUB-NIVELES (1-9)]",
    "awakening_system.screen.guide.cultivation.text2_1": "• 5 Reinos: Condensación de Qi -> Cimientos -> Núcleo Dorado -> Alma Naciente -> Ascensión Celestial.",
    "awakening_system.screen.guide.cultivation.text2_2": "• 9 Sub-niveles por reino. Al avanzar de etapa, el Qi se reinicia a 0 y la ganancia por segundo aumenta (+1 Qi/s por sub-nivel).",
    "awakening_system.screen.guide.cultivation.header3": "§b§l[3. LOS 12 MERIDIANOS MAYORES]",
    "awakening_system.screen.guide.cultivation.text3_1": "• Abre meridianos con Qi para potenciar tu velocidad, curación pasiva, daño y romper cuellos de botella.",
    "awakening_system.screen.guide.cultivation.header4": "§e§l[4. ESPADA VOLADORA DE CULTIVO 3D]",
    "awakening_system.screen.guide.cultivation.text4_1": "• Espada espiritual de combate letal (+12 Daño). Clic derecho para montar y volar en 3D.",
    "awakening_system.screen.guide.cultivation.text4_2": "• Control total con la mirada: pulsa [W] para avanzar hacia donde miras. [Shift] para desmontar y agacharse+clic derecho para recogerla.",

    # SPONSORS TAB
    "awakening_system.screen.guide.sponsors.header1": "§6§l[SISTEMA DE TRANSMISOR ESTELAR (ORV & MANHWAS)]",
    "awakening_system.screen.guide.sponsors.text1_1": "• Eres una Encarnación en el Transmisor Estelar. Las Constelaciones observan tus batallas.",
    "awakening_system.screen.guide.sponsors.text1_2": "• §bCualquier constelación§f puede donarte Monedas espontáneamente al lograr hazañas.",
    "awakening_system.screen.guide.sponsors.text1_3": "• Al subir de nivel o en calabozos, podrás acceder a la §eSelección de Patrocinador§f.",
    "awakening_system.screen.guide.sponsors.text1_4": "• Firmar con un Patrocinador Oficial te otorga su §aEstigma y Atributos exclusivos§f.",
    "awakening_system.screen.guide.sponsors.text1_5": "• §d¡Las demás constelaciones seguirán donándote y enviando mensajes como en el manhwa!§f",
    "awakening_system.screen.guide.sponsors.header2": "§e§l[PRINCIPALES CONSTELACIONES Y ESTIGMAS]",
    "awakening_system.screen.guide.sponsors.text2_1": "• §eEl Prisionero del Ceñidor Dorado (Sun Wukong):§f +Fuerza, Salto y Voluntad.",
    "awakening_system.screen.guide.sponsors.text2_2": "• §8Dragón Negro de la Llama Abisal:§f +Daño de Ataque y Resistencia al Empuje.",
    "awakening_system.screen.guide.sponsors.text2_3": "• §fJuez de Fuego Semejante al Demonio (Uriel):§f +Vida Máxima y Juicio Sagrado.",
    "awakening_system.screen.guide.sponsors.text2_4": "• §bRey Demonio de la Salvación (Kim Dokja):§f +Vida Máxima, Defensa e Inteligencia.",
    "awakening_system.screen.guide.sponsors.text2_5": "• §5Monarca de las Sombras (Ashborn):§f +Ataque, Dominio y Reducción de Coste Sombrío."
}

EN_KEYS = {
    "awakening_system.screen.guide.title": "§b§l✦ HUNTER GUIDE §7// §eOFFICIAL SYSTEM MANUAL ✦",
    "awakening_system.screen.guide.tab.system": "SYSTEM",
    "awakening_system.screen.guide.tab.ranks": "RANKS",
    "awakening_system.screen.guide.tab.dungeons": "DUNGEONS",
    "awakening_system.screen.guide.tab.skills": "SKILLS",
    "awakening_system.screen.guide.tab.tower": "TOWER",
    "awakening_system.screen.guide.tab.cultivation": "CULTIVATION",
    "awakening_system.screen.guide.tab.sponsors": "SPONSORS",

    # SYSTEM TAB
    "awakening_system.screen.guide.system.header1": "§b§l[1. PROGRESSION & EXPERIENCE]",
    "awakening_system.screen.guide.system.text1_1": "• Earn experience by mining ores, defeating creatures, and completing quests.",
    "awakening_system.screen.guide.system.text1_2": "• Each level grants Attribute Points to forge your unique specialization.",
    "awakening_system.screen.guide.system.header2": "§e§l[2. CORE ATTRIBUTES]",
    "awakening_system.screen.guide.system.text2_1": "• Strength (Physical damage & mining) | Agility (Speed & evasion)",
    "awakening_system.screen.guide.system.text2_2": "• Vitality (Max health & regeneration) | Perception (Luck & critical hit)",
    "awakening_system.screen.guide.system.text2_3": "• Intelligence (Energy & magic power) | Willpower (Effect resistance)",
    "awakening_system.screen.guide.system.header3": "§c§l[3. COMBO & TRANCE MODE (ACTION MANHWA)]",
    "awakening_system.screen.guide.system.text3_1": "• Chain 10 consecutive strikes without taking damage to enter §6Trance Mode§f.",
    "awakening_system.screen.guide.system.text3_2": "• In Trance you gain §a+35% Damage§f, sonic movement speed, and overload particles.",
    "awakening_system.screen.guide.system.header4": "§6§l[4. CLASS AWAKENING (LEVEL 30)]",
    "awakening_system.screen.guide.system.text4_1": "• Upon reaching Level 30 you unlock your specialization: Hunter, Assassin, Mage, or Necromancer.",
    "awakening_system.screen.guide.system.header5": "§e§l[5. SYSTEM BANK & PHYSICAL COINS]",
    "awakening_system.screen.guide.system.text5_1": "• Right-click while holding Awakening Coins to deposit them into your virtual balance instantly.",
    "awakening_system.screen.guide.system.text5_2": "• Use the 'BANK' tab in the Shop to deposit or withdraw physical coin bundles.",

    # RANKS TAB
    "awakening_system.screen.guide.ranks.header1": "§6§l[FULL SYSTEM RANK SCALE (41 RANKS)]",
    "awakening_system.screen.guide.ranks.text1_1": "• Mortal: F (1) -> E (5) -> D (10) -> C (20) -> B (30) -> A (40) -> S (50)",
    "awakening_system.screen.guide.ranks.text1_2": "• Human Elite: EX (70) -> SS (90) -> SSS (110) -> §6National (130)§f [Mortal Peak]",
    "awakening_system.screen.guide.ranks.text1_3": "• Transcendence: UR (150) -> UR+ (170) -> LR (190) -> SLR (210) -> MR (230) -> GR (250)",
    "awakening_system.screen.guide.ranks.text1_4": "• Sovereignty: Transcendent (270) -> Transcendental (290) -> Monarch (310) -> Ruler (330)",
    "awakening_system.screen.guide.ranks.text1_5": "• Deities: Supreme Being (350) -> Demigod (370) -> God (400) -> Deity (430) -> Ancestral (460)",
    "awakening_system.screen.guide.ranks.text1_6": "• Cosmic: Cosmic (490) -> Multiversal (520) -> Omniversal (550) -> Primordial (580)",
    "awakening_system.screen.guide.ranks.text1_7": "• Absolute: Infinite (610) -> Omnipotent (640) -> Absolute Being (670) -> Absolute (700)",
    "awakening_system.screen.guide.ranks.text1_8": "• Creation: Supreme Creator (730) -> Absolute Creator (760) -> Chaos Sovereign (800)",
    "awakening_system.screen.guide.ranks.text1_9": "• Final Deities: Timeless Monarch (840) -> Astral Deity (880) -> Singularity (920)",
    "awakening_system.screen.guide.ranks.text1_10": "• §6Limitless (960) -> ABSOLUTE GOD (1000)§f [Supreme Entity of the Multiverse]",
    "awakening_system.screen.guide.ranks.header2": "§b§l[PORTAL COLOR CODING]",
    "awakening_system.screen.guide.ranks.text2_1": "• §bCyan:§f Ranks E/D | §aGreen:§f C/B | §6Amber:§f A | §cCrimson:§f S | §5Purple:§f SS to National",
    "awakening_system.screen.guide.ranks.text2_2": "• §dOrchid:§f Monarchs | §eCelestial Gold:§f Deities (370+) | §4Red Gates:§f Extreme dungeons",

    # DUNGEONS TAB
    "awakening_system.screen.guide.dungeons.header1": "§b§l[CONTINUOUS 5-STAGE DUNGEONS & GOTHIC ARCHITECTURE]",
    "awakening_system.screen.guide.dungeons.text1_1": "• Dungeons are seamless complexes connected through 4 Magical Barrier Walls:",
    "awakening_system.screen.guide.dungeons.text1_2": "  1. §fAbyssal Antechamber:§7 Sunken central pit with frontline vanguard enemies.",
    "awakening_system.screen.guide.dungeons.text1_3": "  2. §fCrypt Passageway:§7 Gothic colonnades, iron barred cells, and lurking assassins.",
    "awakening_system.screen.guide.dungeons.text1_4": "  3. §fWar & Magma Pit:§7 Sunken circular crucible, elevated catwalks, and Mini-Boss.",
    "awakening_system.screen.guide.dungeons.text1_5": "  4. §fCathedral Nave:§7 12-block high vaulted ceiling with buttresses and necromancers.",
    "awakening_system.screen.guide.dungeons.text1_6": "  5. §6Imperial Throne Hall:§e 3-tiered dais hosting the Supreme Boss (Igris / Baran).",
    "awakening_system.screen.guide.dungeons.header2": "§a§l[MAGIC WALLS & DUNGEON TIMER]",
    "awakening_system.screen.guide.dungeons.text2_1": "• To dissolve a wall you must eliminate all enemies in the current stage.",
    "awakening_system.screen.guide.dungeons.text2_2": "• Each cleared wall grants extra time to the collapse timer.",
    "awakening_system.screen.guide.dungeons.header3": "§6§l[REWARDS & ROULETTE ALTAR]",
    "awakening_system.screen.guide.dungeons.text3_1": "• Defeating the Monarch in Stage 5 manifests the Roulette Altar for your loot. Return to Room 1 to cross the exit gate.",

    # SKILLS TAB
    "awakening_system.screen.guide.skills.header1": "§d§l[UNIQUE RANK TITLES & SKILLS (LIMIT: 1 PLAYER)]",
    "awakening_system.screen.guide.skills.text1_1": "• Only one player per world/server can hold each unique distinction:",
    "awakening_system.screen.guide.skills.text1_2": "  1. §5Shadow Monarch:§f Absolute Extraction (Summons shadows instantly).",
    "awakening_system.screen.guide.skills.text1_3": "  2. §bDemon King of Salvation:§f Fourth Wall: Infinite (Massive heal & immunity).",
    "awakening_system.screen.guide.skills.text1_4": "  3. §6The Strongest:§f Ruler's Authority (Primordial psychokinesis).",
    "awakening_system.screen.guide.skills.text1_5": "  4. §cDragon Emperor:§f Cosmic Dragon Breath (Total destructive beam).",
    "awakening_system.screen.guide.skills.header2": "§9§l[SHADOW SOLDIER MECHANICS]",
    "awakening_system.screen.guide.skills.text2_1": "• Slay enemies and extract their souls to build your imperial shadow army.",
    "awakening_system.screen.guide.skills.text2_2": "• §bAllied Immunity:§f Your shadow soldiers are completely immune to your own attacks.",
    "awakening_system.screen.guide.skills.text2_3": "• §bAuto-Teleport:§f If they stray farther than 16 blocks, they blink right behind you.",
    "awakening_system.screen.guide.skills.header3": "§e§l[SKILL EVOLUTION]",
    "awakening_system.screen.guide.skills.text3_1": "• Raise any skill to Level 10 to unlock its evolved form.",

    # TOWER TAB
    "awakening_system.screen.guide.tower.header1": "§6§l[1. TOWER OF RANKERS (100 FLOORS)]",
    "awakening_system.screen.guide.tower.text1_1": "• Ascend through all 100 floors using a Tower Ticket or /system tower.",
    "awakening_system.screen.guide.tower.text1_2": "• Testing teleport command: /system tower floor <1-100>.",
    "awakening_system.screen.guide.tower.header2": "§e§l[2. SEALED CHAMBERS & SCALED THREATS]",
    "awakening_system.screen.guide.tower.text2_1": "• Each floor is an enclosed chamber with ceiling, 4 structural pillars, and tier-themed walls.",
    "awakening_system.screen.guide.tower.text2_2": "• Enemy HP, attack damage, and armor scale strictly with floor level.",
    "awakening_system.screen.guide.tower.header3": "§b§l[3. SHINSOO PORTALS & CHECKPOINTS]",
    "awakening_system.screen.guide.tower.text3_1": "• Defeating all monsters unlocks the Shinsoo Ascension Portal to climb higher.",
    "awakening_system.screen.guide.tower.text3_2": "• The south exit portal and Return Scrolls preserve your highest checkpoint upon returning home.",
    "awakening_system.screen.guide.tower.header4": "§d§l[4. PINNACLE (FLOOR 100) & ISEKAI PORTAL]",
    "awakening_system.screen.guide.tower.text4_1": "• Awaiting on Floor 100 is the Grand Tower Administrator (6,500 HP) and Divine Guardians.",
    "awakening_system.screen.guide.tower.text4_2": "• Defeating him opens the Great Isekai Portal: System 2.0 Update and massive Constellation sponsorship.",

    # CULTIVATION TAB
    "awakening_system.screen.guide.cultivation.header1": "§a§l[1. QI ENERGY & LOTUS MEDITATION]",
    "awakening_system.screen.guide.cultivation.text1_1": "• Qi is separate from Mana. It does not regenerate passively or with potions: only by cultivating.",
    "awakening_system.screen.guide.cultivation.text1_2": "• Press [C] or /system cultivate to assume the lotus position and gather celestial Qi.",
    "awakening_system.screen.guide.cultivation.header2": "§6§l[2. DANTIAN REALMS & SUB-LEVELS (1-9)]",
    "awakening_system.screen.guide.cultivation.text2_1": "• 5 Realms: Qi Condensation -> Foundation Establishment -> Golden Core -> Nascent Soul -> Celestial Ascension.",
    "awakening_system.screen.guide.cultivation.text2_2": "• 9 Sub-levels per realm. Advancing resets Qi to 0 and increases passive gathering rate (+1 Qi/s per sub-level).",
    "awakening_system.screen.guide.cultivation.header3": "§b§l[3. THE 12 MAJOR MERIDIANS]",
    "awakening_system.screen.guide.cultivation.text3_1": "• Unlock meridians using Qi to boost speed, passive regeneration, damage, and break through bottlenecks.",
    "awakening_system.screen.guide.cultivation.header4": "§e§l[4. 3D FLYING CULTIVATION SWORD]",
    "awakening_system.screen.guide.cultivation.text4_1": "• Lethal spiritual combat blade (+12 Damage). Right-click to mount and fly in 3D space.",
    "awakening_system.screen.guide.cultivation.text4_2": "• Look-directed control: press [W] to fly where you look. [Shift] to dismount, Sneak+Right-Click to retrieve it.",

    # SPONSORS TAB
    "awakening_system.screen.guide.sponsors.header1": "§6§l[STAR STREAM SYSTEM (ORV & MANHWAS)]",
    "awakening_system.screen.guide.sponsors.text1_1": "• You are an Incarnation in the Star Stream. Constellations closely watch your trials.",
    "awakening_system.screen.guide.sponsors.text1_2": "• §bAny constellation§f can spontaneously donate Coins when witnessing legendary feats.",
    "awakening_system.screen.guide.sponsors.text1_3": "• When leveling up or clearing dungeons, you can open §eSponsor Selection§f.",
    "awakening_system.screen.guide.sponsors.text1_4": "• Signing with an Official Sponsor grants their exclusive §aStigma and Attributes§f.",
    "awakening_system.screen.guide.sponsors.text1_5": "• §dOther constellations will still donate and send messages like in the webtoon!§f",
    "awakening_system.screen.guide.sponsors.header2": "§e§l[KEY CONSTELLATIONS & STIGMAS]",
    "awakening_system.screen.guide.sponsors.text2_1": "• §ePrisoner of the Golden Headband (Sun Wukong):§f +Strength, Jump Boost & Willpower.",
    "awakening_system.screen.guide.sponsors.text2_2": "• §8Abyssal Black Flame Dragon:§f +Attack Damage & Knockback Resistance.",
    "awakening_system.screen.guide.sponsors.text2_3": "• §fDemon-like Judge of Fire (Uriel):§f +Max Health & Holy Judgment.",
    "awakening_system.screen.guide.sponsors.text2_4": "• §bDemon King of Salvation (Kim Dokja):§f +Max Health, Defense & Intelligence.",
    "awakening_system.screen.guide.sponsors.text2_5": "• §5Shadow Monarch (Ashborn):§f +Attack, Domain & Shadow Cost Reduction."
}

RU_KEYS = {
    "awakening_system.screen.guide.title": "§b§l✦ РУКОВОДСТВО ОХОТНИКА §7// §eОФИЦИАЛЬНЫЙ СПРАВОЧНИК СИСТЕМЫ ✦",
    "awakening_system.screen.guide.tab.system": "СИСТЕМА",
    "awakening_system.screen.guide.tab.ranks": "РАНГИ",
    "awakening_system.screen.guide.tab.dungeons": "ПОДЗЕМЕЛЬЯ",
    "awakening_system.screen.guide.tab.skills": "НАВЫКИ",
    "awakening_system.screen.guide.tab.tower": "БАШНЯ",
    "awakening_system.screen.guide.tab.cultivation": "КУЛЬТИВАЦИЯ",
    "awakening_system.screen.guide.tab.sponsors": "СПОНСОРЫ",

    # SYSTEM TAB
    "awakening_system.screen.guide.system.header1": "§b§l[1. ПРОГРЕСС И ОПЫТ]",
    "awakening_system.screen.guide.system.text1_1": "• Получайте опыт при добыче руды, победе над существами и выполнении заданий.",
    "awakening_system.screen.guide.system.text1_2": "• Каждый уровень даёт Очки Характеристик для создания уникальной специализации.",
    "awakening_system.screen.guide.system.header2": "§e§l[2. БАЗОВЫЕ ХАРАКТЕРИСТИКИ]",
    "awakening_system.screen.guide.system.text2_1": "• Сила (Физ. урон и добыча) | Ловкость (Скорость и уклонение)",
    "awakening_system.screen.guide.system.text2_2": "• Живучесть (Макс. здоровье и регенерация) | Восприятие (Удача и крит)",
    "awakening_system.screen.guide.system.text2_3": "• Интеллект (Энергия и магия) | Сила воли (Стойкость к эффектам)",
    "awakening_system.screen.guide.system.header3": "§c§l[3. КОМБО И РЕЖИМ ТРАНСА (МАНХВА ЭКШН)]",
    "awakening_system.screen.guide.system.text3_1": "• Нанесите 10 ударов подряд без получения урона, чтобы войти в §6Режим Транса§f.",
    "awakening_system.screen.guide.system.text3_2": "• В Трансе вы получаете §a+35% к урону§f, сверхзвуковую скорость и частицы перегрузки.",
    "awakening_system.screen.guide.system.header4": "§6§l[4. ПРОБУЖДЕНИЕ КЛАССА (30 УРОВЕНЬ)]",
    "awakening_system.screen.guide.system.text4_1": "• При достижении 30 уровня открывается специализация: Охотник, Ассасин, Маг или Некромант.",
    "awakening_system.screen.guide.system.header5": "§e§l[5. БАНК СИСТЕМЫ И ФИЗИЧЕСКИЕ МОНЕТЫ]",
    "awakening_system.screen.guide.system.text5_1": "• ПКМ с Монетами Пробуждения в руке мгновенно зачисляет их на виртуальный счёт.",
    "awakening_system.screen.guide.system.text5_2": "• Используйте вкладку 'БАНК' в Магазине для внесения или снятия физических монет.",

    # RANKS TAB
    "awakening_system.screen.guide.ranks.header1": "§6§l[ПОЛНАЯ ШКАЛА РАНГОВ СИСТЕМЫ (41 РАНГ)]",
    "awakening_system.screen.guide.ranks.text1_1": "• Смертные: F (1) -> E (5) -> D (10) -> C (20) -> B (30) -> A (40) -> S (50)",
    "awakening_system.screen.guide.ranks.text1_2": "• Человеческая элита: EX (70) -> SS (90) -> SSS (110) -> §6Национальный (130)§f [Пик смертных]",
    "awakening_system.screen.guide.ranks.text1_3": "• Трансцендентность: UR (150) -> UR+ (170) -> LR (190) -> SLR (210) -> MR (230) -> GR (250)",
    "awakening_system.screen.guide.ranks.text1_4": "• Суверенитет: Трансцендент (270) -> Трансцендентальный (290) -> Монарх (310) -> Правитель (330)",
    "awakening_system.screen.guide.ranks.text1_5": "• Божества: Высшее Существо (350) -> Полубог (370) -> Бог (400) -> Божество (430) -> Древний (460)",
    "awakening_system.screen.guide.ranks.text1_6": "• Космические: Космический (490) -> Мультивселенский (520) -> Омнивселенский (550) -> Изначальный (580)",
    "awakening_system.screen.guide.ranks.text1_7": "• Абсолютные: Бесконечный (610) -> Всемогущий (640) -> Абсолютное Существо (670) -> Абсолютный (700)",
    "awakening_system.screen.guide.ranks.text1_8": "• Творение: Верховный Творец (730) -> Абсолютный Творец (760) -> Владыка Хаоса (800)",
    "awakening_system.screen.guide.ranks.text1_9": "• Финальные: Вневременной Монарх (840) -> Астральное Божество (880) -> Сингулярность (920)",
    "awakening_system.screen.guide.ranks.text1_10": "• §6Безграничный (960) -> АБСОЛЮТНЫЙ БОГ (1000)§f [Высшая сущность Мультивселенной]",
    "awakening_system.screen.guide.ranks.header2": "§b§l[ЦВЕТОВАЯ КОДИРОВКА ПОРТАЛОВ]",
    "awakening_system.screen.guide.ranks.text2_1": "• §bГолубой:§f Ранги E/D | §aЗелёный:§f C/B | §6Янтарный:§f A | §cБагровый:§f S | §5Пурпурный:§f SS до Национального",
    "awakening_system.screen.guide.ranks.text2_2": "• §dОрхидея:§f Монархи | §eНебесное Золото:§f Божества (370+) | §4Красные Врата:§f Экстремальные подземелья",

    # DUNGEONS TAB
    "awakening_system.screen.guide.dungeons.header1": "§b§l[НЕПРЕРЫВНЫЕ 5-ЭТАПНЫЕ ПОДЗЕМЕЛЬЯ В ГОТИЧЕСКОМ СТИЛЕ]",
    "awakening_system.screen.guide.dungeons.text1_1": "• Подземелья представляют собой единые комплексы, разделенные 4 магическими барьерами:",
    "awakening_system.screen.guide.dungeons.text1_2": "  1. §fАванзал Бездны:§7 Углубленный центральный ров с авангардом врагов.",
    "awakening_system.screen.guide.dungeons.text1_3": "  2. §fПроход Крипты:§7 Готические колоннады, железные решетки и скрытные убийцы.",
    "awakening_system.screen.guide.dungeons.text1_4": "  3. §fРов Войны и Магмы:§7 Круглый тигель, возвышенные мостки и Мини-Босс.",
    "awakening_system.screen.guide.dungeons.text1_5": "  4. §fСоборный Неф:§7 Сводчатый потолок в 12 блоков, контрфорсы и некроманты.",
    "awakening_system.screen.guide.dungeons.text1_6": "  5. §6Императорский Тронный Зал:§e Трёхуровневое возвышение с Верховным Боссом (Игрис / Баран).",
    "awakening_system.screen.guide.dungeons.header2": "§a§l[МАГИЧЕСКИЕ БАРЬЕРЫ И ТАЙМЕР]",
    "awakening_system.screen.guide.dungeons.text2_1": "• Для снятия барьера необходимо уничтожить всех врагов текущей фазы.",
    "awakening_system.screen.guide.dungeons.text2_2": "• Каждый открытый барьер даёт дополнительное время, чтобы предотвратить обрушение.",
    "awakening_system.screen.guide.dungeons.header3": "§6§l[НАГРАДЫ И АЛТАРЬ РУЛЕТКИ]",
    "awakening_system.screen.guide.dungeons.text3_1": "• После победы над Монархом на 5 этапе появится Алтарь Рулетки с трофеями. Вернитесь в Зал 1 для выхода.",

    # SKILLS TAB
    "awakening_system.screen.guide.skills.header1": "§d§l[УНИКАЛЬНЫЕ ТИТУЛЫ И НАВЫКИ (ЛИМИТ: 1 ИГРОК)]",
    "awakening_system.screen.guide.skills.text1_1": "• Только один игрок в мире/сервере может обладать этими отличиями:",
    "awakening_system.screen.guide.skills.text1_2": "  1. §5Теневой Монарх:§f Абсолютное Извлечение (Мгновенный призыв теней).",
    "awakening_system.screen.guide.skills.text1_3": "  2. §bКороль Демонов Спасения:§f Четвертая Стена: Бесконечность (Мощное исцеление и иммунитет).",
    "awakening_system.screen.guide.skills.text1_4": "  3. §6Сильнейший:§f Власть Правителя (Изначальный телекинез).",
    "awakening_system.screen.guide.skills.text1_5": "  4. §cИмператор Драконов:§f Космическое Дыхание Дракона (Разрушительный луч).",
    "awakening_system.screen.guide.skills.header2": "§9§l[МЕХАНИКА ТЕНЕВЫХ СОЛДАТ]",
    "awakening_system.screen.guide.skills.text2_1": "• Побеждайте врагов и извлекайте их души для создания имперской армии теней.",
    "awakening_system.screen.guide.skills.text2_2": "• §bИммунитет союзников:§f Ваши теневые солдаты полностью неуязвимы к вашему урону.",
    "awakening_system.screen.guide.skills.text2_3": "• §bАвтотелепортация:§f При отдалении более чем на 16 блоков они возвращаются за спину.",
    "awakening_system.screen.guide.skills.header3": "§e§l[ЭВОЛЮЦИЯ НАВЫКОВ]",
    "awakening_system.screen.guide.skills.text3_1": "• Повысьте любой навык до 10 уровня, чтобы открыть его улучшенную версию.",

    # TOWER TAB
    "awakening_system.screen.guide.tower.header1": "§6§l[1. БАШНЯ РАНКЕРОВ (100 ЭТАЖЕЙ)]",
    "awakening_system.screen.guide.tower.text1_1": "• Поднимайтесь по 100 этажам Башни с Билетом в Башню или командой /system tower.",
    "awakening_system.screen.guide.tower.text1_2": "• Команда телепортации для тестирования: /system tower floor <1-100>.",
    "awakening_system.screen.guide.tower.header2": "§e§l[2. ЗАКРЫТЫЕ ЗАЛЫ И МАСШТАБИРОВАНИЕ]",
    "awakening_system.screen.guide.tower.text2_1": "• Каждый этаж — закрытый зал с потолком, 4 колоннами и стенами в стиле уровня.",
    "awakening_system.screen.guide.tower.text2_2": "• Здоровье, урон и броня врагов строго масштабируются с номером этажа.",
    "awakening_system.screen.guide.tower.header3": "§b§l[3. ПОРТАЛЫ ШИНСУ И ТОЧКИ СОХРАНЕНИЯ]",
    "awakening_system.screen.guide.tower.text3_1": "• Победа над всеми монстрами открывает Портал Восхождения Шинсу на следующий этаж.",
    "awakening_system.screen.guide.tower.text3_2": "• Южный портал и Свитки Возврата сохраняют достигнутый этаж при возвращении домой.",
    "awakening_system.screen.guide.tower.header4": "§d§l[4. ВЕРШИНА (100 ЭТАЖ) И ПОРТАЛ ИСЕКАЙ]",
    "awakening_system.screen.guide.tower.text4_1": "• На 100 этаже ожидает Великий Администратор Башни (6,500 HP) и его Божественные Стражи.",
    "awakening_system.screen.guide.tower.text4_2": "• Победа над ним открывает Великий Портал Исекай: Обновление Системы 2.0 и спонсорство Созвездий.",

    # CULTIVATION TAB
    "awakening_system.screen.guide.cultivation.header1": "§a§l[1. ЭНЕРГИЯ ЦИ И МЕДИТАЦИЯ ЛОТОСА]",
    "awakening_system.screen.guide.cultivation.text1_1": "• Ци отделена от Маны. Не восстанавливается сама и зельями: только культивацией.",
    "awakening_system.screen.guide.cultivation.text1_2": "• Нажмите [C] или /system cultivate, чтобы сесть в позу лотоса и накапливать Ци.",
    "awakening_system.screen.guide.cultivation.header2": "§6§l[2. СФЕРЫ ДАНЬТЯНЯ И ПОДУРОВНИ (1-9)]",
    "awakening_system.screen.guide.cultivation.text2_1": "• 5 Сфер: Конденсация Ци -> Заложение Основ -> Золотое Ядро -> Зарождающаяся Душа -> Небесное Вознесение.",
    "awakening_system.screen.guide.cultivation.text2_2": "• 9 Подуровней в каждой сфере. Переход сбрасывает Ци до 0 и увеличивает приток (+1 Ци/с за подуровень).",
    "awakening_system.screen.guide.cultivation.header3": "§b§l[3. 12 ОСНОВНЫХ МЕРИДИАНОВ]",
    "awakening_system.screen.guide.cultivation.text3_1": "• Открывайте меридианы с помощью Ци для ускорения, регенерации, урона и прорыва узких мест.",
    "awakening_system.screen.guide.cultivation.header4": "§e§l[4. 3D ЛЕТАЮЩИЙ МЕЧ КУЛЬТИВАЦИИ]",
    "awakening_system.screen.guide.cultivation.text4_1": "• Смертоносный духовный меч (+12 к урону). ПКМ для полёта в трёхмерном пространстве.",
    "awakening_system.screen.guide.cultivation.text4_2": "• Управление взглядом: нажмите [W] для полёта по направлению взгляда. [Shift] — спешиться, Sneak+ПКМ — забрать.",

    # SPONSORS TAB
    "awakening_system.screen.guide.sponsors.header1": "§6§l[СИСТЕМА ЗВЁЗДНОГО ПОТОКА (ORV И МАНХВЫ)]",
    "awakening_system.screen.guide.sponsors.text1_1": "• Вы Воплощение в Звёздном Потоке. Созвездия наблюдают за вашими битвами.",
    "awakening_system.screen.guide.sponsors.text1_2": "• §bЛюбое созвездие§f может спонтанно пожертвовать Монеты за великие подвиги.",
    "awakening_system.screen.guide.sponsors.text1_3": "• При повышении уровня или в подземельях можно открыть §eВыбор Спонсора§f.",
    "awakening_system.screen.guide.sponsors.text1_4": "• Контракт с Официальным Спонсором дарует его эксклюзивную §aСтигму и Характеристики§f.",
    "awakening_system.screen.guide.sponsors.text1_5": "• §dОстальные созвездия продолжат донатить и присылать сообщения, как в вебтуне!§f",
    "awakening_system.screen.guide.sponsors.header2": "§e§l[КЛЮЧЕВЫЕ СОЗВЕЗДИЯ И СТИГМЫ]",
    "awakening_system.screen.guide.sponsors.text2_1": "• §eУзник Золотого Обруча (Сунь Укун):§f +Сила, Высота прыжка и Сила воли.",
    "awakening_system.screen.guide.sponsors.text2_2": "• §8Чёрный Пламенный Дракон Бездны:§f +Урон от атак и сопротивление отбрасыванию.",
    "awakening_system.screen.guide.sponsors.text2_3": "• §fДемоноподобный Огненный Судья (Уриил):§f +Макс. здоровье и Священный Суд.",
    "awakening_system.screen.guide.sponsors.text2_4": "• §bКороль Демонов Спасения (Ким Докча):§f +Макс. здоровье, Защита и Интеллект.",
    "awakening_system.screen.guide.sponsors.text2_5": "• §5Теневой Монарх (Эшборн):§f +Атака, Владычество и Снижение затрат теней."
}

def update_json(filepath, new_keys):
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)
    for k, v in new_keys.items():
        data[k] = v
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Updated {filepath} with {len(new_keys)} keys")

# 1. Update en_us.json
update_json(os.path.join(LANG_DIR, "en_us.json"), EN_KEYS)

# 2. Update ru_ru.json
update_json(os.path.join(LANG_DIR, "ru_ru.json"), RU_KEYS)

# 3. Update all Spanish files
for fname in os.listdir(LANG_DIR):
    if fname.startswith("es_") and fname.endswith(".json"):
        update_json(os.path.join(LANG_DIR, fname), ES_KEYS)

print("Done updating all language files!")
