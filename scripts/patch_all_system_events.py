import sys
import os

sys.stdout.reconfigure(encoding='utf-8')

def replace_in_file(fpath, old_text, new_text):
    if not os.path.exists(fpath):
        print(f"Error: {fpath} does not exist!")
        return False
    with open(fpath, "r", encoding="utf-8") as f:
        content = f.read()
    if old_text not in content:
        print(f"Warning: could not find target in {fpath}: {old_text[:60]}")
        return False
    content = content.replace(old_text, new_text, 1)
    with open(fpath, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Successfully patched {fpath}")
    return True

# 1. ElementalAltarBlock.java
p = "src/main/java/com/soybe/awakeningsystem/block/ElementalAltarBlock.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§5§l[CASTILLO CENTRAL] §f¡Los 4 Sellos Elementales han caído! La Gran Barrera de Éter se ha disipado y el Salón del Trono del Rey Demonio está abierto."), false);',
    'player.displayClientMessage(Component.translatable("awakening_system.block.elemental_altar.all_seals_broken"), false);'
)

# 2. AwakeningCoinEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/AwakeningCoinEntity.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l[CARA - SOL DORADO] §e¡La fortuna te favorece! (+Suerte II y Rapidez)"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.coin_entity.heads"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§b§l[CRUZ - LUNA SOMBRÍA] §f¡El destino te protege! (+Escudo de Absorción)"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.coin_entity.tails"), true);'
)

# 3. CelestialDragonEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/CelestialDragonEntity.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ ¡EL DRAGÓN CELESTIAL RECONOCE TU ALMA SUPREMA! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.recognized"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ ¡LA PERLA DEL DRAGÓN CELESTIAL HA RESTAURADO TOTALMENTE AL DRAGÓN! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.pearl_restored"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ EL DRAGÓN CELESTIAL ENTRA EN REPOSO SERPENTINO ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.resting"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ EL DRAGÓN CELESTIAL SE PREPARA PARA ELEVARSE ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.taking_flight"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ ¡EL DRAGÓN CELESTIAL FUE DOMESTICADO (MODO CREATIVO)! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.creative_tamed"), true);'
)

# 4. SystemEvents.java
p = "src/main/java/com/soybe/awakeningsystem/SystemEvents.java"

# Fables
old_f1 = """                if (fable.isWorldFirst()) {
                    AwakeningSystemMod.notify(p,
                            "§e✦ ¡PRIMACÍA MUNDIAL DEL STAR STREAM! ✦",
                            "§f" + pName + " §eha forjado la fábula única: " + fable.grade().colorCode() + "[" + fable.title() + "]",
                            fable.grade().hexColor());
                } else if (p.getUUID().equals(player.getUUID())) {
                    AwakeningSystemMod.notify(p,
                            "§b✦ ¡NUEVA FÁBULA ADQUIRIDA! ✦",
                            "§fHas adquirido la fábula " + fable.grade().colorCode() + "[" + fable.title() + "] §7(+" + fable.weight() + " Peso)",
                            fable.grade().hexColor());
                }"""
new_f1 = """                if (fable.isWorldFirst()) {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.fable_world_first.title",
                            "awakening_system.notification.fable_world_first.desc",
                            fable.grade().hexColor(),
                            pName, fable.grade().colorCode() + "[" + fable.title() + "]");
                } else if (p.getUUID().equals(player.getUUID())) {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.fable_acquired.title",
                            "awakening_system.notification.fable_acquired.desc",
                            fable.grade().hexColor(),
                            fable.grade().colorCode() + "[" + fable.title(), fable.weight());
                }"""
replace_in_file(p, old_f1, new_f1)

# Dungeon selection
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§cYa te encuentras dentro de un calabozo activo."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.already_in_dungeon"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§cNecesitas una Llave Dimensional en tu inventario."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.need_key"), true);'
)

# Passives
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§f[LA CUARTA PARED]", "§b«La Cuarta Pared absorbe el impacto letal por completo».", 0xFF70E8FF);',
    'AwakeningSystemMod.notify(sp, "awakening_system.notification.fourth_wall.title", "awakening_system.notification.fourth_wall.desc", 0xFF70E8FF);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§e✦ BARRERA DE ÉTER ✦", "§6[Las Constelaciones despliegan un escudo a tu favor]", 0xFFFFD700);',
    'AwakeningSystemMod.notify(sp, "awakening_system.notification.ether_barrier.title", "awakening_system.notification.ether_barrier.desc", 0xFFFFD700);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§8✦ CORAZÓN NEGRO ✦", "§7«El Corazón Negro del Monarca late con furia. La muerte ha sido rechazada».", 0xFF9966FF);',
    'AwakeningSystemMod.notify(sp, "awakening_system.notification.black_heart.title", "awakening_system.notification.black_heart.desc", 0xFF9966FF);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§c✦ FURIA INMORTAL ✦", "§c«¡Tu furia se niega a morir! Daño masivo concedido».", 0xFFFF3333);',
    'AwakeningSystemMod.notify(sp, "awakening_system.notification.immortal_fury.title", "awakening_system.notification.immortal_fury.desc", 0xFFFF3333);'
)

# Yggdrasil Summit
replace_in_file(p,
    'p.displayClientMessage(Component.literal("§d§l[Cúspide de Yggdrasil] §fHas ascendido sobre la copa de hojas del Gran Árbol. Explora el dosel para hallar la entrada oculta."), true);',
    'p.displayClientMessage(Component.translatable("awakening_system.action.yggdrasil_summit"), true);'
)

# Astral Rescue & Dokkaebi
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§5✦ ¡RESCATE ASTRAL! ✦", "§dLas constelaciones celestiales te han arrancado del abismo del vacío.", 0xFFFF55FF);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.astral_rescue.title", "awakening_system.notification.astral_rescue.desc", 0xFFFF55FF);'
)

old_dok_ok = """                    if (extraKills > 0) {
                        AwakeningSystemMod.notify(player, "§6✦ ¡OVERKILL EXTRAORDINARIO! ✦",
                                "§e¡+" + extraKills + " bajas extra! Total: +" + totalCoins + " Monedas y +" + totalExp + " EXP.", 0xFFFFD700);
                        player.displayClientMessage(Component.literal("§e[¡El Dokkaebi y las constelaciones enloquecen con tu masacre! +" + bonusCoins + " monedas extra por overkill]"), true);
                    } else {
                        AwakeningSystemMod.notify(player, "§6✦ ¡ESCENARIO SUPERADO CON ÉXITO! ✦",
                                "§f¡Has cumplido la cuota del Dokkaebi! +150 Monedas y +500 EXP.", 0xFFFFD700);
                        player.displayClientMessage(Component.literal("§6[Las constelaciones rugen de emoción por tu supervivencia y envían una lluvia de monedas]"), true);
                    }"""
new_dok_ok = """                    if (extraKills > 0) {
                        AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_overkill.title",
                                "awakening_system.notification.dokkaebi_overkill.desc", 0xFFFFD700, extraKills, totalCoins, totalExp);
                        player.displayClientMessage(Component.translatable("awakening_system.action.constellations_cheer_coins"), true);
                    } else {
                        AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_success.title",
                                "awakening_system.notification.dokkaebi_success.desc", 0xFFFFD700);
                        player.displayClientMessage(Component.translatable("awakening_system.action.constellations_cheer_coins"), true);
                    }"""
replace_in_file(p, old_dok_ok, new_dok_ok)

old_dok_fail = """                    AwakeningSystemMod.notify(player, "§c✦ ¡ESCENARIO FALLIDO! ✦",
                            "§c¡Tormenta de Probabilidad desatada! No cumpliste la cuota a tiempo.", 0xFFFF5555);"""
new_dok_fail = """                    AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_failed.title",
                            "awakening_system.notification.dokkaebi_failed.desc", 0xFFFF5555);"""
replace_in_file(p, old_dok_fail, new_dok_fail)

old_dok_start = """                AwakeningSystemMod.notify(player, "§c⚡ ESCENARIO PRINCIPAL FORZADO ⚡",
                        "§eEl Dokkaebi ha abierto una transmisión: ¡Elimina " + target + " enemigos en 3 minutos!", 0xFFFF4444);"""
new_dok_start = """                AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_scenario_start.title",
                        "awakening_system.notification.dokkaebi_scenario_start.desc", 0xFFFF4444, target);"""
replace_in_file(p, old_dok_start, new_dok_start)

# Bank
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§c§l[BANCO DEL SISTEMA]", "§cNo tienes Monedas del Despertar físicas en tu inventario.", 0xFFFF7666);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title_error", "awakening_system.notification.bank.no_coins", 0xFFFF7666);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§6§l[BANCO DEL SISTEMA]", "§aHas depositado " + toDeposit + " monedas. §7Saldo: §e" + data.coins() + " Monedas", 0xFF50FA7B);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title", "awakening_system.notification.bank.deposited", 0xFF50FA7B, toDeposit, data.coins());'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§c§l[BANCO DEL SISTEMA]", "§cSaldo virtual insuficiente para retirar " + amount + " monedas.", 0xFFFF7666);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title_error", "awakening_system.notification.bank.insufficient", 0xFFFF7666, amount);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§6§l[BANCO DEL SISTEMA]", "§eHas retirado " + amount + " Monedas del Despertar físicas.", 0xFFFFD866);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title", "awakening_system.notification.bank.withdrawn", 0xFFFFD866, amount);'
)

# Stigmas
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ ESTIGMA ACTIVADO: §e¡Llamas del Juicio del Juez del Fuego! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.judge_flames"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ ESTIGMA ACTIVADO: §e¡Impacto Sísmico del Báculo Ruyi! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.ruyi_staff"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§8§l✦ ESTIGMA ACTIVADO: §9¡Dominio Absoluto del Monarca de las Sombras! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.shadow_monarch"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§4§l✦ ESTIGMA ACTIVADO: §c¡Aliento de Aniquilación del Dragón del Caos! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.chaos_dragon"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§b§l✦ ESTIGMA ACTIVADO: §3¡Ojo Trascendente del Regresor Supremo! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.regressor_eye"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§d§l✦ ESTIGMA ACTIVADO: §f¡Veinticuatro Movimientos del Ciruelo en Flor! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.plum_blossom"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§e§l✦ ESTIGMA ACTIVADO: §6¡Bendición Sagrada de la Constelación Patrocinadora! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.sponsor_blessing"), true);'
)

# Tribulation / Physique
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[Tribulación Celestial] §fYa te encuentras bajo el juicio del Rayo Divino. ¡Sobrevive primero!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.tribulation_active"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c[CULTIVO] Tu físico marcial ya ha sido fijado por el destino y no puede ser alterado."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.physique_already_set"), true);'
)

# Star stream donation
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§6✦ PATROCINIO DEL STAR STREAM ✦", "§f¡Una Constelación te ha transferido §e" + coinGift + " Monedas§f!", 0xFFFFD700);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.star_stream_sponsor.title", "awakening_system.notification.star_stream_sponsor.desc", 0xFFFFD700, coinGift);'
)

# Job & Skills
old_job1 = """                AwakeningSystemMod.notify(player, "§6✦ ¡DESPERTAR DE OCUPACIÓN DISPONIBLE! ✦",
                        "§fHas alcanzado el Nivel 30. Abre tu Sistema [K] para elegir tu Clase Definitiva.", 0xFFFFD700);"""
new_job1 = """                AwakeningSystemMod.notify(player, "awakening_system.notification.job_available.title",
                        "awakening_system.notification.job_available.desc", 0xFFFFD700);"""
replace_in_file(p, old_job1, new_job1)

old_sp1 = """                AwakeningSystemMod.notify(player, "§6✦ PUNTO DE HABILIDAD (SP) ✦",
                        "§f¡Has ganado §e1 SP §fpara tu Árbol de Habilidades de Clase!", 0xFFFFD700);"""
new_sp1 = """                AwakeningSystemMod.notify(player, "awakening_system.notification.sp_gained.title",
                        "awakening_system.notification.sp_gained.desc", 0xFFFFD700);"""
replace_in_file(p, old_sp1, new_sp1)

replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§cNivel Insuficiente", "§fNecesitas alcanzar el §eNivel 30 §fpara despertar una Ocupación.", 0xFFFF5555);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.job_low_level.title", "awakening_system.notification.job_low_level.desc", 0xFFFF5555);'
)

old_job_done = """        AwakeningSystemMod.notify(player, "§6¡DESPERTAR DE OCUPACIÓN COMPLETADO!",
                "§fTe has convertido en un " + className + "§f.", 0xFFFFD700);"""
new_job_done = """        AwakeningSystemMod.notify(player, "awakening_system.notification.job_awakened.title",
                "awakening_system.notification.job_awakened.desc", 0xFFFFD700, className);"""
replace_in_file(p, old_job_done, new_job_done)

replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§cRequisitos No Cumplidos", "§fNo cumples los requisitos de nivel, nodo previo o puntos de habilidad (SP).", 0xFFFF5555);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.skill_req_not_met.title", "awakening_system.notification.skill_req_not_met.desc", 0xFFFF5555);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§cPuntos Insuficientes", "§fNecesitas al menos " + node.spCost() + " Punto(s) de Habilidad (SP).", 0xFFFF5555);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.skill_sp_needed.title", "awakening_system.notification.skill_sp_needed.desc", 0xFFFF5555, node.spCost());'
)
old_learn = """        AwakeningSystemMod.notify(player, "§6¡HABILIDAD DESBLOQUEADA!",
                "§fHas aprendido §b" + sName + "§f en tu Árbol de Clase.", 0xFFFFD700);"""
new_learn = """        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_learned.title",
                "awakening_system.notification.skill_learned.desc", 0xFFFFD700, sName);"""
replace_in_file(p, old_learn, new_learn)

replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§cMejora No Disponible", "§fNo cumples los requisitos de nivel o puntos de habilidad (SP) para mejorar esta técnica.", 0xFFFF5555);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.skill_upgrade_req.title", "awakening_system.notification.skill_upgrade_req.desc", 0xFFFF5555);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§cPuntos Insuficientes", "§fNecesitas " + spCost + " SP para mejorar esta habilidad.", 0xFFFF5555);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.skill_upgrade_sp.title", "awakening_system.notification.skill_upgrade_sp.desc", 0xFFFF5555, spCost);'
)

old_upgrade = """        AwakeningSystemMod.notify(player, "§6¡HABILIDAD MEJORADA!",
                "§b" + sName + " §fha ascendido a §eRango " + roman + "§f.", 0xFFFFD700);"""
new_upgrade = """        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_upgraded.title",
                "awakening_system.notification.skill_upgraded.desc", 0xFFFFD700, sName, roman);"""
replace_in_file(p, old_upgrade, new_upgrade)

# Dimension locks & armor skills
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§cAcceso al Mundo Isekai bloqueado. Requiere Sistema 2.0."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.isekai_requires_sys20"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§cAcceso al Reino Murim bloqueado. Requiere ascensión al Sistema 3.0."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.murim_requires_sys30"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c[No tienes equipado un conjunto completo de armadura con habilidad activa]"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.no_armor_skill"), true);'
)

# Armor ultimate skills
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§b✦ PARPADEO DIMENSIONAL ✦", "§f¡Has rasgado el espacio a velocidad supersónica!", 0xFF55FFFF);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_blink.title", "awakening_system.notification.armor_skill_blink.desc", 0xFF55FFFF);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§c✦ FISURA DE MAGMA ✦", "§6¡Columnas de magma y roca fundida expulsan a tus enemigos!", 0xFFFF4500);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_magma.title", "awakening_system.notification.armor_skill_magma.desc", 0xFFFF4500);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§a✦ CAMPANA DORADA DE JADE ✦", "§2¡Invulnerabilidad y reflejo absoluto de daño activados por 5s!", 0xFF55FF55);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_jade_bell.title", "awakening_system.notification.armor_skill_jade_bell.desc", 0xFF55FF55);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§5✦ ESTALLIDO ASTRAL DEL DRAGÓN ✦", "§d¡El rugido del Dragón Celestial y una supernova estelar devastan el campo!", 0xFFFF55FF);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_astral_burst.title", "awakening_system.notification.armor_skill_astral_burst.desc", 0xFFFF55FF);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§8✦ MANTO DEL TIRANO ✦", "§7Las sombras aterrorizan e inmovilizan a todos los enemigos.", 0xFF8800FF);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_tyrant_shroud.title", "awakening_system.notification.armor_skill_tyrant_shroud.desc", 0xFF8800FF);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§c✦ FURIA DE VALTER ✦", "§4¡Fuerza berserker desatada! +Velocidad, +Fuerza y +Absorción.", 0xFFFF0000);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_valter_rage.title", "awakening_system.notification.armor_skill_valter_rage.desc", 0xFFFF0000);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§d✦ FLORACIÓN DEL CIRUELO ✦", "§f¡Danza de cien espadas con pétalos de ciruelo!", 0xFFFF77AA);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_plum_bloom.title", "awakening_system.notification.armor_skill_plum_bloom.desc", 0xFFFF77AA);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§f✦ OJO DEL LECTOR ✦", "§b«El protagonista observa el escenario completo con visión omnisciente».", 0xFF70E8FF);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_reader_eye.title", "awakening_system.notification.armor_skill_reader_eye.desc", 0xFF70E8FF);'
)

print("All SystemEvents patches applied successfully!")
