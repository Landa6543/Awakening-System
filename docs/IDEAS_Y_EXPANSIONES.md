# 🌌 Awakening System: Hoja de Ruta y Expansiones Implementadas

Este documento recopila las expansiones diseñadas e implementadas para elevar el mod al nivel de las mejores experiencias RPG y Manhwa/Anime en Minecraft.

---

## 🏛️ 1. Estructuras con Lore Fragmentado: El Secreto en la Copa del Yggdrasil ✅ IMPLEMENTADO
- **Estructuras de Exploración Activas:**
  - **Campamentos de Expedición de Cazadores Abandonados (`hunter_expedition_camp`):** Tiendas desgastadas, hogueras y suministros de expedición.
  - **Puesto de Observación de Sabios (`ruined_stargazer_outpost`):** Bibliotecas astrales semiderruidas con telescopios celestiales y atriles.
  - **Tablillas de Piedra Ancestral (`ancient_lore_stele`):** Monumentos milenarios que traducen crónicas de los reinos y el Árbol del Mundo.
  - **Laberinto de Raíces del Yggdrasil (`yggdrasil_root_labyrinth`) y Valle Espiritual (`spirit_valley`):** Raíces titánicas y cañones de hierbas espirituales.
  - **Fortaleza del Culto de la Sangre (`blood_sect_fortress`) y Gruta Inmortal Ancestral (`ancestral_immortal_grotto`):** Santuarios secretos con reliquias de Murim.

---

## 🌀 2. Overhaul Visual y Cinemático de Habilidades (Estilo Anime/Manhwa) ✅ IMPLEMENTADO
- **"¡SURGE!" (Arise / Monarca de las Sombras — Solo Leveling):**
  - **Emergencia Sombreada:** Efecto cinemático con `ShadowEmergencePayload` que genera un charco de sombra líquida, partículas de abismo y elevación vertical.
  - **Ojos Neón Azul Eléctrico y Niebla:** Capa de brillo `ShadowInfantryRenderer` y ojos celestes penetrantes (`#00E5FF`).
- **Dagas de Sangre y Sombras (Sung Jin-Woo):**
  - Estelas de tajos en abanico azul neón y púrpura oscuro.
  - *Shadow Step* con teletransportación tras las sombras enemigas.
- **Espada de Cha Hae-In (Danza de la Luz Sagrada):**
  - Tajos dorados solares celestiales con estelas de velocidad y pétalos brillantes.
- **Espada del Monte Hua (Chung Myung):**
  - Cortes que acumulan pétalos de ciruelo y desatan el ¡Florecimiento Total de Qi floral!
- **Espada del Demonio Celestial (Nano Machine):**
  - Tajos de Qi de la Espada carmesí y negra (`DemonicSwordQiEntity`) que rasgan el espacio.

---

## ⚔️ 3. Bonificaciones de Set Completo y Habilidades Activas de Armaduras [Tecla Z] ✅ IMPLEMENTADO
- **Armadura de Mithril (Gracia Élfica):**
  - *Pasiva:* Caída lenta de pluma al agacharse en el aire, +25% de velocidad y regeneración acelerada de estamina.
  - *Activa [Tecla Z]:* *Parpadeo Dimensional:* Dash sónico instantáneo de 8 bloques dejando una silueta residual celeste.
- **Armadura de Adamantita (Furia Volcánica / Berserker):**
  - *Pasiva:* Inmunidad total al fuego y lava. Daño aumentado hasta +50% según la salud faltante.
  - *Activa [Tecla Z]:* *Onda de Magma:* Erupción sísmica ígnea que hace volar por los aires a los enemigos en 6 bloques.
- **Armadura de Jade Espiritual (Cuerpo Inmortal de Qi):**
  - *Pasiva:* 15% del daño recibido se almacena y regenera como Qi defensivo.
  - *Activa [Tecla Z]:* *Campana Dorada de Jade:* Invoca una campana espiritual impenetrable durante 4 segundos.
- **Armadura Astral (Soberanía del Dragón Celestial):**
  - *Pasiva:* Vuelo libre cósmico continuo con estela de dragón, inmunidad al vacío.
  - *Activa [Tecla Z]:* *Bramido del Dragón Celestial:* Onda de choque estelar y relámpago cósmico que aturde hordas.

---

## 💊 4. Alquimia de Murim: Caldero y Píldoras Espirituales de Cultivo (Dan / 丹) ✅ IMPLEMENTADO
- **Caldero de Alquimia de Murim (`murim_alchemy_cauldron`):**
  - Bloque interactivo que, colocado sobre fuego espiritual (fuego, alma, fogata, magma o lava), se enciende (`LIT = true`) y permite refinar píldoras con un clic derecho consumiendo hierbas del inventario.
  - Guía de Dao en pantalla con clic derecho sin ingredientes.
- **Píldoras Divinas:**
  - **Píldora de Limpieza de Médula:** Purifica impurezas y abre permanentemente 1 meridiano bloqueado sin gastar Qi.
  - **Píldora del Dragón Naciente:** Otorga +1,500 Qi de cultivo, Regeneración II y Fuerza III por 2 minutos.
  - **Píldora de Ruptura de Reino:** Garantiza el éxito directo en la Ruptura de Reino del Dantian sin fallos ni desvíos de Qi.
  - **Elixir de la Tortuga Negra:** Resistencia II (10 min), Absorción IV (10 min) y curación instantánea total de salud.

---

## 🗡️ 5. Armas Exóticas Místicas ✅ IMPLEMENTADO
- **Espada Voladora de Jade (*Feijian* - 飞剑 - `jade_feijian`):**
  - Espada rápida de Qi (8.5 daño, 1.8 velocidad).
  - *Clic Derecho:* Lanza la espada en vuelo teledirigido persiguiendo enemigos (`JadeFeijianEntity`). Inflige 16 de daño de Qi perforante y **regresa automáticamente volando a la mano del cultivador**.
- **Guandao Astral del Dragón (`astral_guandao`):**
  - Alabarda celestial de gran alcance (13.0 daño, 1.0 velocidad).
  - *Clic Derecho:* Desata un **Tajo Creciente de Dragón Celestial** (`AstralDragonCrescentEntity`) de 24 bloques que atraviesa enemigos con 18 de daño cósmico, derribo y fuego estelar. (4s CD)
- **Gran Martillo de Guerra de Adamantita (`adamantite_war_hammer`):**
  - Martillo demoledor colosal (15.0 daño, 0.75 velocidad).
  - *Pasiva:* Los golpes críticos aturden al enemigo (Lentitud IV y Debilidad II).
  - *Clic Derecho:* **Fisura Sísmica y Caída en Picado**. Si estás en el aire, caes en picado acelerado al suelo. Al impactar, desata un terremoto sísmico de 7 a 10 bloques que lanza enemigos por los aires e inflige 20-30 de daño de aplastamiento. (6s CD)

---

## 🎰 6. Expansión de Estadísticas Gacha: Contador de Deseos Realizados ✅ IMPLEMENTADO
- **Área Objetivo:** Panel superior de estadísticas en el modal de Historial de Deseos (`GachaWishScreen` -> `renderHistoryModal`).
- **Descripción:**
  - Contador visible y destacado que muestra los **Deseos Totales Lanzados/Realizados** a lo largo de la partida por el jugador.
  - Sincronizado en red mediante `OpenGachaScreenPayload` y `GachaWishResultPayload` desde `data.totalGachaPulls()`.
  - Caja de resumen de Pity ampliada a 50px de altura con 3 filas limpiamente distribuidas:
    - *Fila 1:* `Pity 5★ Acumulado: X / 80` | `Pity 4★ Acumulado: Y / 10`
    - *Fila 2:* `Garantía Promocional: ¡100% GARANTIZADO! / 50/50` | `Tiradas Principiante: Z / 20`
    - *Fila 3:* `§fDeseos Totales Realizados: §6{totalWishes} §e✦` | `§fProtogemas Invertidas: §b{totalWishes * 160} §7💎`

---

## 🌑 7. Dimensión del Limbo y Reforma del Subespacio (En Planificación) ⏳
- **Muerte en Mazmorras / Portales:** En lugar del respawn común, el cazador es transportado a la dimensión del **Limbo**.
- **Cinemática de Despertar:** Pantalla en negro con parpadeo progresivo de párpados, cámara inclinada que se levanta suavemente del suelo e inmersión sonora (exhalación pesada, fatiga y latidos).
- **Bloqueo del Sistema (Candados 🔒):** Todas las transferencias de portales dimensionales quedan bloqueadas con candado mientras se permanezca en el Limbo.
- **Acceso Restringido al Subespacio:** Se permite abrir el inventario remoto del subespacio para sacar suministros de emergencia, pero se prohíbe el viaje físico hacia el subespacio para evitar la evasión.
- **Subespacio de Vacío Puro:** Eliminación total de la capa de Bedrock en la dimensión del Subespacio; plataformas flotantes en el vacío donde caer implica la muerte.
- *Documento detallado de especificación:* Ver [docs/DISENO_DIMENSION_LIMBO_Y_SUBESPACIO.md](file:///c:/Users/soybe/Desktop/Mods%20NeoForge/Sistema/docs/DISENO_DIMENSION_LIMBO_Y_SUBESPACIO.md) y [ROADMAP_PROXIMA_VERSION.md#19](file:///c:/Users/soybe/Desktop/Mods%20NeoForge/Sistema/ROADMAP_PROXIMA_VERSION.md).

---

## 🪨 8. Rediseño Orgánico de Cuevas de Mazmorra ("Cave Dungeons / Grutas Abisales") ⏳
- **Superación de la Mazmorra Cuadrada:** En lugar de presets de salas cerradas de bloques de piedra, se implementan mega-cavernas naturales tridimensionales de 40 a 80 bloques de altura vertical.
- **Geología Monumental:** Estalactitas gigantes colgadas del techo que pueden desprenderse como trampas, puentes naturales de roca erosionada sobre abismos, lagos subterráneos de lava/agua y geodas colosales de maná.
- **Nidos Orgánicos y Emboscadas Biológicas:** Spawners camuflados en hendiduras rocosas, colmenas arácnidas en techos y monstruos adaptados a la oscuridad que atacan verticalmente.
- **Cámara del Jefe Cavernosa:** Domo natural con estalagmitas para cobertura táctica y jefes que aprovechan el entorno vertical.
- **Movilidad Tridimensional:** Diseñadas para poner a prueba el doble salto, dashes aéreos, teletransporte y escalada del mod.
- *Detalle completo en:* [ROADMAP_PROXIMA_VERSION.md#20](file:///c:/Users/soybe/Desktop/Mods%20NeoForge/Sistema/ROADMAP_PROXIMA_VERSION.md).

---

## 📋 9. Sistema de Eventos con Tabla Adaptable (Event Board / Leaderboard) ⏳
- **Inspiración y Dinámica:** Inspirado en eventos masivos de creadores/servidores (estilo Dedsafío / manhwas de supervivencia), ofreciendo una tabla visual interactiva de progresión y competencia.
- **Tabla Adaptable (Event Board GUI):** Cuadrícula de casillas de misiones con estados visuales (Bloqueada, En Progreso, Completada, Corona/Estrella), con barras de progreso en tiempo real y reclamo directo de premios.
- **Categorías de Desafíos:** Caza y exterminio de jefes, speedruns de portales, recolección y alquimia de píldoras, resistencia en oleadas y metas cooperativas de gremio.
- **Leaderboard Dinámico en Vivo:** Clasificación en tiempo real para servidores y LAN con puntuación acumulada, avatares y tiempos de resolución.
- **100% Configurable vía Datapacks (JSON):** Configuración data-driven (`data/awakeningsystem/events/...`) que permite a administradores y creadores diseñar temporadas y eventos sin tocar una sola línea de código Java.
- **Tiers de Recompensas y Trofeos:** Bronce, Plata, Oro y Rango Nacional con artefactos exclusivos, auras cosméticas de partículas y títulos sobre el cazador.
- *Detalle completo en:* [ROADMAP_PROXIMA_VERSION.md#21](file:///c:/Users/soybe/Desktop/Mods%20NeoForge/Sistema/ROADMAP_PROXIMA_VERSION.md).


