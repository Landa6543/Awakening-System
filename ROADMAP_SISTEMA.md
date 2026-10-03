# 🗺️ ROADMAP MAESTRO - AWAKENING SYSTEM (MOD EXPANSION)

Este documento contiene la lista detallada y centralizada de todas las características, mejoras visuales, correcciones de errores y sistemas pendientes para el mod **Awakening System**.

---

## 📌 ÍNDICE DE MÓDULOS

1. [Módulo A: Interfaz y HUD (HUD de Vida & Gremio)](#-módulo-a-interfaz-y-hud)
2. [Módulo B: Cofres con Candado y Loot Instanciado por Jugador (Tipo Lootr)](#-módulo-b-cofres-con-candado-y-loot-instanciado)
3. [Módulo C: Pulido Visual, Texturas HD, Puertas y Bloques](#-módulo-c-pulido-visual-texturas-hd-puertas-y-bloques)
4. [Módulo D: Madera, Vegetación y Bloques Faltantes](#-módulo-d-madera-vegetación-y-bloques-faltantes)
5. [Módulo E: Minería en Teyvat y Rebalanceo de Tienda](#-módulo-e-minería-en-teyvat-y-rebalanceo-de-tienda)
6. [Módulo F: Traducciones y Textos de Sets](#-módulo-f-traducciones-y-textos-de-sets)
7. [Módulo G: Sistema de Gremios V2 (Economía Dual, Seguridad y Territorio)](#-módulo-g-sistema-de-gremios-v2)
8. [Módulo H: Documentación y Mecánicas de Teyvat](#-módulo-h-documentación-y-mecánicas-de-teyvat)

---

## 🖥️ MÓDULO A: INTERFAZ Y HUD

- [x] **A.1. HUD de Vida y Nombre de Enemigos (`SystemHudOverlay.java`)**:
  - Separar el título/nombre de la criatura o jefe del valor numérico y porcentaje de vida.
  - El nombre se posiciona en la parte superior y la barra de vida centrada abajo para que nombres largos nunca se solapen con la cifra de HP.
- [x] **A.2. Solapamiento de Columnas en Miembros (`GuildScreen.java`)**:
  - Corregir coordenadas X de las cabeceras `ESTADO` y `RANGO` para eliminar el texto fusionado `ESTADRANGO`.
- [x] **A.3. Bug de Reseteo de Pestaña en Gremio**:
  - Guardar el estado de pestaña activa (`lastTab`). Al realizar donaciones monetarias o comprar bendiciones, la pantalla mantendrá la pestaña en la que estaba el usuario en lugar de regresarlo a "Miembros".
- [x] **A.4. Ocultamiento de la Caja de Invitación (`inviteBox`)**:
  - Asegurar que el cuadro de texto para invitar jugadores solo sea visible y activo en la pestaña de Miembros (`currentTab == 0`), ocultándose en Tesorería y Bendiciones.
- [x] **A.5. Ajuste de Desborde de Texto en Tesorería**:
  - Limitar el ancho y habilitar salto de línea para el mensaje explicativo *"Dona Moras para subir el nivel del gremio y desbloquear bendiciones (+EXP)."* para evitar que se desborde del marco.

---

## 🗝️ MÓDULO B: COFRES CON CANDADO Y LOOT INSTANCIADO (TIPO LOOTR)

- [x] **B.1. Sistema de Loot Individual por Jugador (`LockedTreasureChestBlockEntity.java`)**:
  - Modificar la entidad del cofre para almacenar un mapeo de inventarios personales vinculados al `UUID` de cada jugador.
  - Al interactuar con la llave requerida (Overworld, Nether, End, Teyvat, Isekai, Murim o Llaves de Santuario), el cofre se desbloquea únicamente para ese jugador y genera su botín independiente.
  - Si otro jugador llega a la mazmorra, el cofre permanece con candado para él; deberá usar su propia llave y recibirá su propio botín completo, evitando que otros jugadores saqueen las recompensas antes de tiempo.
- [x] **B.2. Registro de Bloques Físicos e Ítems de Cofres en Creativo**:
  - Registrar formalmente los 6 bloques físicos y objetos de cofres dimensionales en [`SystemBlocks.java`], [`SystemItems.java`] y [`ModCreativeTabs.java`].
  - Permitir colocar, inspeccionar y abrir cada uno de los 6 tipos en modo creativo o supervivencia.

---

## 🎨 MÓDULO C: PULIDO VISUAL, TEXTURAS HD, PUERTAS Y BLOQUES

- [x] **C.1. Rediseño HD (32x32) de las 6 Llaves**:
  - Rediseñar texturas con volumen, relieves metálicos, brillo rúnico y gemas incrustadas para:
    1. Llave de Mazmorra Ancestral (*Overworld*)
    2. Llave Ígnea del Inframundo (*Nether*)
    3. Llave del Vacío y las Sombras (*End*)
    4. Llave del Santuario Arcano (*Teyvat*)
    5. Llave Rúnica de Maná (*Isekai*)
    6. Llave del Dado Ancestral (*Murim*)
- [x] **C.2. Rediseño Completo de la Armadura Abisal**:
  - Sustituir la textura monocromática y azulada plana de las capas de armadura (`abyssal_layer_1.png` y `abyssal_layer_2.png`) por una armadura forjada en obsidiana abisal y cobalto profundo con relieves dorados/arcanos y runas luminosas.
  - Rediseñar los 4 iconos de inventario (Casco, Pechera, Grebas, Botas).
- [x] **C.3. Texturas de Bloques Abisales**:
  - Mejorar Mineral Abisal, Bloque Abisal y Horno Abisal con mayor tridimensionalidad y texturas detalladas.
- [x] **C.4. Corrección de Puertas con Agujeros Transparentes**:
  - Arreglar las texturas de puertas con recortes vacíos (Puerta de bambú, Adhigama, Sandbearer) cubriendo los espacios vacíos con entramados de madera o paneles de papel shoji tradicional.
- [x] **C.5. Texturas de Troncos Estilo Vainilla**:
  - Rediseñar `spirit_plum_log` y `celestial_pine_log` con corteza y anillos concéntricos idénticos al estilo estético del roble/pino vainilla de Minecraft.

---

## 🌲 MÓDULO D: MADERA, VEGETACIÓN Y BLOQUES FALTANTES

- [x] **D.1. Bloques Derivados de Madera para Ciruelo y Pino**:
  - Registrar escaleras, losas, vallas, puertas de valla, puertas y trampillas para las maderas de Ciruelo Espiritual y Pino Milenario.
- [x] **D.2. Brotes (Saplings) y Crecimiento**:
  - Asegurar la presencia de los brotes en la pestaña creativa y verificar que crezcan correctamente al aplicarles polvo de hueso.
- [x] **D.3. Generación Natural de Árboles**:
  - Incorporar la generación de árboles en los biomas correspondientes de Teyvat e Isekai.

---

## 💎 MÓDULO E: MINERÍA EN TEYVAT Y REBALANCEO DE TIENDA

- [x] **E.1. Minerales Elementales y Cristales en Teyvat**:
  - Generación en el mundo de vetas de gemas elementales (Ágata Agnidus, Lazurita Varunada, Amatista Vajrada, Turquesa Vayuda, Jade Shivada, Topacio Prithiva, Esmeralda Nagadus).
  - Generación de yacimientos de Cristal Mágico y Hierro Blanco con drops de trozos y pedazos para forja.
- [x] **E.2. Rebalanceo de Tienda del Sistema**:
  - Rebalancear precios de compra y venta en `ShopDefinitions` y `SellDefinitions`.
  - Añadir nuevos artículos para vender a cambio de monedas del sistema (minerales de Teyvat, gemas elementales en 4 tiers, drops de hilichurls/slimes, frutas y maderas).
  - ⚠️ **REGLA ESTRICTA**: NO tocar los precios ni atributos de las bolsas de viaje (`wanderer_pouch`, `adventurer_pouch`, `outlander_pouch`, `emergency_pouch`, `omniscience_pouch`) ni de las tarjetas de regalo (`gift_card_5`, `15`, `30`, `50`, `100`).

---

## 📜 MÓDULO F: TRADUCCIONES Y TEXTOS DE SETS

- [x] **F.1. Textos de Armadura Abisal**:
  - Añadir nombres, descripciones de piezas y bonificación de conjunto en los archivos de idioma (`en_us.json`, `es_es.json`, etc.).
- [x] **F.2. Textos de Armadura de Mora**:
  - Añadir descripción, lore y efectos del set de armadura de mora en los archivos de idioma.

---

## 🏛️ MÓDULO G: SISTEMA DE GREMIOS V2

- [x] **G.1. Tesorería Dual (Moras + Monedas del Sistema)**:
  - Almacenar fondos separados para Moras y Monedas de Despertar.
  - Mostrar ambos saldos en la GUI con opciones para depositar ambas monedas.
- [x] **G.2. Permisos y Seguridad en Retiros**:
  - Sistema de retiros donde los miembros pueden consultar o solicitar fondos, pero los retiros deben ser autorizados o ejecutados exclusivamente por Altos Mandos (Líder o Sublíder) para proteger las arcas del gremio contra robos.
- [x] **G.3. Establecer Sede (HQ) sin Restricción del Faro**:
  - Permitir a los líderes marcar la sede de su gremio (`/guild sethq`) libremente.
  - La bendición "Faro de Teletransporte" funcionará como una ventaja de viaje rápido para teletransportarse instantáneamente a la sede.
- [x] **G.4. Reclamo de Territorio (Chunks del Gremio)**:
  - Sistema de protección de chunks alrededor de la sede para proteger bloques, cofres y mecanismos contra intrusos o vandalismo.
  - Límite máximo de chunks reclamables que escala según el **Nivel del Gremio** (`(level + 1)^2`) evitando monopolizaciones de terreno.
- [x] **G.5. Progresión por Misiones Cooperativas**:
  - Subida de nivel mediante misiones en conjunto y recompensas cooperativas al derrotar jefes mundiales (+300 EXP de gremio por jefe).

---

## 📖 MÓDULO H: DOCUMENTACIÓN Y MECÁNICAS DE TEYVAT

- **Estación del Gremio de Pescadores (`fishing_block`)**:
  - Bloque arquitectónico canónico para el canje de peces raros por equipamiento y estabilizadores.
- **Prototipos de Forja (`ForgeBilletItem`)**:
  - Moldes base de armas 4★ (Tierras del Norte y Tierras Centrales) para forjar armamento regional con cristales mágicos y hierro blanco.
- **Llaves y Santuarios de las Profundidades (`TeyvatShrineKeyItem` & `TeyvatShrineOfDepthsFeature`)**:
  - Estructuras antiguas repartidas por Teyvat selladas que resguardan Cofres Lujosos con 40 Protogemas, Destinos y abundante Mora.

---

## 🛑 MÓDULO I: OCULTAMIENTO DEL WITHER EN MODO CREATIVO

- [x] **I.1. Filtro en Pestañas Creativas (`BuildCreativeModeTabContentsEvent`)**:
  - Filtrado y eliminación automática de cualquier objeto relacionado al Wither (`wither_spawn_egg`, etc.) en todas las pestañas creativas y pestaña de búsqueda.
  - Ocultamiento de ítems incompletos en pestañas del mod para evitar que jugadores los vean o generen hasta rehacer el modelo completamente en una versión posterior.
