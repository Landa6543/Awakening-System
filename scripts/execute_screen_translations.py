import os
import re
import json

print("Starting screen translation implementation...")

# 1. Load dictionary of all new keys for ES, EN, and RU
TRANSLATIONS = {
    # Phase completions and actions
    "awakening_system.notification.phase_1_completed.title": {
        "es": "§a§l¡FASE 1 COMPLETADA!",
        "en": "§a§lPHASE 1 COMPLETED!",
        "ru": "§a§lЭТАП 1 ЗАВЕРШЁН!"
    },
    "awakening_system.notification.phase_1_completed.message": {
        "es": "§eLa Muralla Mágica se ha disuelto. ¡Avanza a la Fase 2!",
        "en": "§eThe Magic Barrier has dissolved. Advance to Phase 2!",
        "ru": "§eМагический барьер рассеян. Переходите к этапу 2!"
    },
    "awakening_system.action.phase_completed_title": {
        "es": "§a§l¡FASE %s COMPLETADA!",
        "en": "§a§lPHASE %s COMPLETED!",
        "ru": "§a§lЭТАП %s ЗАВЕРШЁН!"
    },
    "awakening_system.action.phase_1_unlocked": {
        "es": "§eLa Muralla Mágica se ha disuelto. ¡Avanza a la Fase 2!",
        "en": "§eThe Magic Barrier has dissolved. Advance to Phase 2!",
        "ru": "§eМагический барьер рассеян. Переходите к этапу 2!"
    },
    "awakening_system.action.phase_2_unlocked": {
        "es": "§e¡Puerta del Foso de Guerra abierta! Avanza a la Fase %s.",
        "en": "§eWar Pit Gate unlocked! Advance to Phase %s.",
        "ru": "§eВорота военной ямы открыты! Переходите к этапу %s."
    },
    "awakening_system.action.phase_3_unlocked": {
        "es": "§e¡Puertas de la Catedral abiertas! Avanza a la Fase %s.",
        "en": "§eCathedral Gates unlocked! Advance to Phase %s.",
        "ru": "§eВрата Собора открыты! Переходите к этапу %s."
    },
    "awakening_system.action.phase_4_unlocked": {
        "es": "§c¡El Salón del Trono se ha abierto! ¡El Rey Demonio te espera!",
        "en": "§cThe Throne Room has unlocked! The Demon King awaits you!",
        "ru": "§cТронный зал открыт! Король демонов ждёт вас!"
    },
    "awakening_system.dungeon.barrier_warning_1": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los enemigos de la Fase 1 (§c%s restantes§e) para disolver la muralla.",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eDefeat Phase 1 enemies (§c%s remaining§e) to dissolve the wall.",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eУничтожьте врагов этапа 1 (осталось: §c%s§e), чтобы снять стену."
    },
    "awakening_system.dungeon.barrier_blocked_1": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar hasta eliminar a los enemigos de la Fase 1!",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eYou cannot advance until all Phase 1 enemies are defeated!",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eВы не можете пройти, пока не уничтожите врагов этапа 1!"
    },
    "awakening_system.dungeon.barrier_warning_2": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los guardias de la Fase 2 (§c%s restantes§e) para abrir el Foso de Guerra.",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eDefeat Phase 2 guards (§c%s remaining§e) to unlock the War Pit.",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eУничтожьте стражей этапа 2 (осталось: §c%s§e), чтобы открыть Военную яму."
    },
    "awakening_system.dungeon.barrier_blocked_2": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar hasta eliminar a los guardias de la Fase 2!",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eYou cannot advance until Phase 2 guards are defeated!",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eВы не можете пройти, пока стражи этапа 2 не уничтожены!"
    },
    "awakening_system.dungeon.barrier_warning_3": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los guerreros de la Fase 3 (§c%s restantes§e) para abrir la Catedral.",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eDefeat Phase 3 warriors (§c%s remaining§e) to open the Cathedral.",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eУничтожьте воинов этапа 3 (осталось: §c%s§e), чтобы открыть Собор."
    },
    "awakening_system.dungeon.barrier_blocked_3": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar hasta eliminar a los guerreros de la Fase 3!",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eYou cannot advance until Phase 3 warriors are defeated!",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eВы не можете пройти, пока воины этапа 3 не уничтожены!"
    },
    "awakening_system.dungeon.barrier_warning_4": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los guardias de la Fase 4 (§c%s restantes§e) para abrir el Salón del Trono.",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eDefeat Phase 4 guards (§c%s remaining§e) to unlock the Throne Room.",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eУничтожьте стражей этапа 4 (осталось: §c%s§e), чтобы открыть Тронный зал."
    },
    "awakening_system.dungeon.barrier_blocked_4": {
        "es": "§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar al Trono hasta eliminar a los guardias de la Fase 4!",
        "en": "§c§l[MAGIC BARRIER ACTIVE] §eYou cannot enter the Throne Room until Phase 4 guards are defeated!",
        "ru": "§c§l[МАГИЧЕСКИЙ БАРЬЕР] §eВы не можете войти в Тронный зал, пока стражи этапа 4 не уничтожены!"
    },
    "awakening_system.dungeon.entering_rift": {
        "es": "¡Cruzando el vórtice dimensional! %s",
        "en": "Crossing the dimensional vortex! %s",
        "ru": "Переход через пространственный вихрь! %s"
    },
    "awakening_system.dungeon.rift_nearby_prompt": {
        "es": "%s §f- §eCruza el portal dimensional para entrar",
        "en": "%s §f- §eStep through the dimensional portal to enter",
        "ru": "%s §f- §eВойдите в пространственный портал, чтобы войти"
    },
    "awakening_system.dungeon.left_unconquered": {
        "es": "§e⚠ Has salido del calabozo sin conquistarlo. El portal permanece activo en el exterior hasta que expire.",
        "en": "§e⚠ You exited the dungeon unconquered. The portal remains active outside until it expires.",
        "ru": "§e⚠ Вы вышли из подземелья, не покорив его. Портал снаружи остаётся активным до истечения времени."
    },
    "awakening_system.dungeon.dungeon_break_imminent": {
        "es": "§4§l⚠ ¡DESBORDE DE MAZMORRA INMINENTE! §cEl portal ha colapsado y los monstruos están invadiendo el exterior.",
        "en": "§4§l⚠ IMMINENT DUNGEON BREAK! §cThe portal has collapsed and monsters are invading the outside.",
        "ru": "§4§l⚠ НЕИЗБЕЖНЫЙ ПРОРЫВ ПОДЗЕМЕЛЬЯ! §cПортал разрушен, монстры вторгаются наружу."
    },
    "awakening_system.dungeon.already_in_dungeon": {
        "es": "§cYa estás dentro de un calabozo activo.",
        "en": "§cYou are already inside an active dungeon.",
        "ru": "§cВы уже находитесь в активном подземелье."
    },
    "awakening_system.dungeon.roulette_prize": {
        "es": "§6§l✦ ¡PREMIO: §e%s x%s §6(+§e%s⛃§6, +§b%sXP§6)! ✦",
        "en": "§6§l✦ PRIZE: §e%s x%s §6(+§e%s⛃§6, +§b%sXP§6)! ✦",
        "ru": "§6§l✦ ПРИЗ: §e%s x%s §6(+§e%s⛃§6, +§b%sXP§6)! ✦"
    },
    # Dokkaebi & Scenario
    "awakening_system.notification.dokkaebi_scenario_start.title": {
        "es": "§c⚡ ESCENARIO PRINCIPAL FORZADO ⚡",
        "en": "§c⚡ FORCED MAIN SCENARIO ⚡",
        "ru": "§c⚡ ПРИНУДИТЕЛЬНЫЙ ОСНОВНОЙ СЦЕНАРИЙ ⚡"
    },
    "awakening_system.notification.dokkaebi_scenario_start.desc": {
        "es": "§eEl Dokkaebi ha abierto una transmisión: ¡Elimina %s enemigos en 3 minutos!",
        "en": "§eThe Dokkaebi opened a stream: Eliminate %s enemies within 3 minutes!",
        "ru": "§eТоккэби запустил трансляцию: Уничтожьте %s врагов за 3 минуты!"
    },
    "awakening_system.notification.dokkaebi_overkill.title": {
        "es": "§6✦ ¡OVERKILL EXTRAORDINARIO! ✦",
        "en": "§6✦ EXTRAORDINARY OVERKILL! ✦",
        "ru": "§6✦ НЕВЕРОЯТНЫЙ OVERKILL! ✦"
    },
    "awakening_system.notification.dokkaebi_overkill.desc": {
        "es": "§e¡+%s bajas extra! Total: +%s Monedas y +%s EXP.",
        "en": "§e+%s extra kills! Total: +%s Coins and +%s EXP.",
        "ru": "§e+%s доп. убийств! Итого: +%s Монет и +%s Опыта."
    },
    "awakening_system.notification.dokkaebi_success.title": {
        "es": "§6✦ ¡ESCENARIO SUPERADO CON ÉXITO! ✦",
        "en": "§6✦ SCENARIO CLEARED SUCCESSFULLY! ✦",
        "ru": "§6✦ СЦЕНАРИЙ УСПЕШНО ПРОЙДЕН! ✦"
    },
    "awakening_system.notification.dokkaebi_success.desc": {
        "es": "§f¡Has cumplido la cuota del Dokkaebi! +150 Monedas y +500 EXP.",
        "en": "§fYou met the Dokkaebi's quota! +150 Coins and +500 EXP.",
        "ru": "§fВы выполнили норму Токкэби! +150 Монет и +500 Опыта."
    },
    "awakening_system.notification.dokkaebi_failed.title": {
        "es": "§c✦ ¡ESCENARIO FALLIDO! ✦",
        "en": "§c✦ SCENARIO FAILED! ✦",
        "ru": "§c✦ СЦЕНАРИЙ ПРОВАЛЕН! ✦"
    },
    "awakening_system.notification.dokkaebi_failed.desc": {
        "es": "§c¡Tormenta de Probabilidad desatada! No cumpliste la cuota a tiempo.",
        "en": "§cProbability Storm unleashed! You failed to meet the quota in time.",
        "ru": "§cШторм Вероятности начался! Вы не выполнили норму вовремя."
    },
    "awakening_system.action.constellations_cheer_coins": {
        "es": "§6[Las constelaciones rugen de emoción por tu supervivencia y envían una lluvia de monedas]",
        "en": "§6[The constellations roar with excitement for your survival and shower you with coins]",
        "ru": "§6[Созвездия ревут от восторга за ваше выживание и осыпают монетами]"
    },
    "awakening_system.hud.dokkaebi_extra_kills": {
        "es": "§e%s §8| §fBajas: §a%s§7/§e%s §6§l[+%s EXTRA!]",
        "en": "§e%s §8| §fKills: §a%s§7/§e%s §6§l[+%s EXTRA!]",
        "ru": "§e%s §8| §fУбийств: §a%s§7/§e%s §6§l[+%s ДОП.!]"
    },
    "awakening_system.dokkaebi.quest_started_title": {
        "es": "§e§l✦ [SUB-ESCENARIO DEL STAR STREAM] ✦",
        "en": "§e§l✦ [STAR STREAM SUB-SCENARIO] ✦",
        "ru": "§e§l✦ [ПОД-СЦЕНАРИЙ STAR STREAM] ✦"
    },
    "awakening_system.dokkaebi.quest_started_desc": {
        "es": "§b%s\n§f%s\n§6Recompensa: §e%s Monedas",
        "en": "§b%s\n§f%s\n§6Reward: §e%s Coins",
        "ru": "§b%s\n§f%s\n§6Награда: §e%s Монет"
    },
    "awakening_system.dokkaebi.quest_objective_done_title": {
        "es": "§a§l✦ ¡OBJETIVO COMPLETADO! ✦",
        "en": "§a§l✦ OBJECTIVE COMPLETED! ✦",
        "ru": "§a§l✦ ЦЕЛЬ ДОСТИГНУТА! ✦"
    },
    "awakening_system.dokkaebi.quest_objective_done_desc": {
        "es": "§fHabla con Bihyung para reclamar tus monedas y recompensas.",
        "en": "§fTalk to Bihyung to claim your coins and rewards.",
        "ru": "§fПоговорите с Бихёном, чтобы забрать свои монеты и награды."
    },
    "awakening_system.dokkaebi.quest_progress": {
        "es": "§8[§eStar Stream§8] §fProgreso de Sub-Escenario: §e%s/%s",
        "en": "§8[§eStar Stream§8] §fSub-Scenario Progress: §e%s/%s",
        "ru": "§8[§eStar Stream§8] §fПрогресс под-сценария: §e%s/%s"
    },
    "awakening_system.dokkaebi.quest_completed_title": {
        "es": "§e§l✦ ¡SUB-ESCENARIO COMPLETADO! ✦",
        "en": "§e§l✦ SUB-SCENARIO COMPLETED! ✦",
        "ru": "§e§l✦ ПОД-СЦЕНАРИЙ ЗАВЕРШЕН! ✦"
    },
    "awakening_system.dokkaebi.quest_completed_desc": {
        "es": "§fBihyung: '¡Brillante transmisión, encarnación!\nLas Constelaciones han enviado sus monedas.'\n§e+%s Monedas acreditadas.",
        "en": "§fBihyung: 'Brilliant stream, incarnation!\nThe Constellations have sent their coins.'\n§e+%s Coins credited.",
        "ru": "§fБихён: «Блестящая трансляция, воплощение!\nСозвездия прислали свои монеты».\n§e+%s Монет зачислено."
    },
    # Bank
    "awakening_system.notification.bank.title": {
        "es": "§6§l[BANCO DEL SISTEMA]",
        "en": "§6§l[SYSTEM BANK]",
        "ru": "§6§l[БАНК СИСТЕМЫ]"
    },
    "awakening_system.notification.bank.title_error": {
        "es": "§c§l[BANCO DEL SISTEMA]",
        "en": "§c§l[SYSTEM BANK]",
        "ru": "§c§l[БАНК СИСТЕМЫ]"
    },
    "awakening_system.notification.bank.no_coins": {
        "es": "§cNo tienes Monedas del Despertar físicas en tu inventario.",
        "en": "§cYou do not have physical Awakening Coins in your inventory.",
        "ru": "§cВ вашем инвентаре нет физических Монет Пробуждения."
    },
    "awakening_system.notification.bank.deposited": {
        "es": "§aHas depositado %s monedas. §7Saldo: §e%s Monedas",
        "en": "§aYou deposited %s coins. §7Balance: §e%s Coins",
        "ru": "§aВы внесли %s монет. §7Баланс: §e%s Монет"
    },
    "awakening_system.notification.bank.insufficient": {
        "es": "§cSaldo virtual insuficiente para retirar %s monedas.",
        "en": "§cInsufficient virtual balance to withdraw %s coins.",
        "ru": "§cНедостаточно средств для снятия %s монет."
    },
    "awakening_system.notification.bank.withdrawn": {
        "es": "§eHas retirado %s Monedas del Despertar físicas.",
        "en": "§eYou withdrew %s physical Awakening Coins.",
        "ru": "§eВы сняли %s физических Монет Пробуждения."
    },
    # Fables & Passives
    "awakening_system.notification.fable_world_first.title": {
        "es": "§e✦ ¡PRIMACÍA MUNDIAL DEL STAR STREAM! ✦",
        "en": "§e✦ STAR STREAM WORLD FIRST! ✦",
        "ru": "§e✦ МИРОВОЙ ПЕРВЫЙ УСПЕХ STAR STREAM! ✦"
    },
    "awakening_system.notification.fable_world_first.desc": {
        "es": "§f%s §eha forjado la fábula única: %s",
        "en": "§f%s §ehas forged the unique fable: %s",
        "ru": "§f%s §eсоздал уникальную басню: %s"
    },
    "awakening_system.notification.fable_acquired.title": {
        "es": "§b✦ ¡NUEVA FÁBULA ADQUIRIDA! ✦",
        "en": "§b✦ NEW FABLE ACQUIRED! ✦",
        "ru": "§b✦ ПОЛУЧЕНА НОВАЯ БАСНЯ! ✦"
    },
    "awakening_system.notification.fable_acquired.desc": {
        "es": "§fHas adquirido la fábula [%s§f] §7(+%s Peso)",
        "en": "§fYou acquired fable [%s§f] §7(+%s Weight)",
        "ru": "§fВы получили басню [%s§f] §7(+%s Вес)"
    },
    "awakening_system.notification.fourth_wall.title": {
        "es": "§f[LA CUARTA PARED]",
        "en": "§f[THE FOURTH WALL]",
        "ru": "§f[ЧЕТВЁРТАЯ СТЕНА]"
    },
    "awakening_system.notification.fourth_wall.desc": {
        "es": "§b«La Cuarta Pared absorbe el impacto letal por completo».",
        "en": "§b«The Fourth Wall completely absorbs the lethal impact».",
        "ru": "§b«Четвёртая Стена полностью поглощает смертельный удар»."
    },
    "awakening_system.notification.ether_barrier.title": {
        "es": "§e✦ BARRERA DE ÉTER ✦",
        "en": "§e✦ AETHER BARRIER ✦",
        "ru": "§e✦ ЭФИРНЫЙ БАРЬЕР ✦"
    },
    "awakening_system.notification.ether_barrier.desc": {
        "es": "§6[Las Constelaciones despliegan un escudo a tu favor]",
        "en": "§6[The Constellations deploy a shield in your favor]",
        "ru": "§6[Созвездия разворачивают щит в вашу пользу]"
    },
    "awakening_system.notification.black_heart.title": {
        "es": "§8✦ CORAZÓN NEGRO ✦",
        "en": "§8✦ BLACK HEART ✦",
        "ru": "§8✦ ЧЁРНОЕ СЕРДЦЕ ✦"
    },
    "awakening_system.notification.black_heart.desc": {
        "es": "§7«El Corazón Negro del Monarca late con furia. La muerte ha sido rechazada».",
        "en": "§7«The Monarch's Black Heart beats with fury. Death has been rejected».",
        "ru": "§7«Чёрное Сердце Монарха бьётся с яростью. Смерть отвергнута»."
    },
    "awakening_system.notification.immortal_fury.title": {
        "es": "§c✦ FURIA INMORTAL ✦",
        "en": "§c✦ IMMORTAL FURY ✦",
        "ru": "§c✦ БЕССМЕРТНАЯ ЯРОСТЬ ✦"
    },
    "awakening_system.notification.immortal_fury.desc": {
        "es": "§c«¡Tu furia se niega a morir! Daño masivo concedido».",
        "en": "§c«Your fury refuses to die! Massive damage granted».",
        "ru": "§c«Ваша ярость отказывается умирать! Нанесён колоссальный урон»."
    },
    "awakening_system.notification.astral_rescue.title": {
        "es": "§5✦ ¡RESCATE ASTRAL! ✦",
        "en": "§5✦ ASTRAL RESCUE! ✦",
        "ru": "§5✦ АСТРАЛЬНОЕ СПАСЕНИЕ! ✦"
    },
    "awakening_system.notification.astral_rescue.desc": {
        "es": "§dLas constelaciones celestiales te han arrancado del abismo del vacío.",
        "en": "§dThe celestial constellations pulled you from the void abyss.",
        "ru": "§dНебесные созвездия вырвали вас из бездны пустоты."
    },
    "awakening_system.notification.star_stream_sponsor.title": {
        "es": "§6✦ PATROCINIO DEL STAR STREAM ✦",
        "en": "§6✦ STAR STREAM SPONSORSHIP ✦",
        "ru": "§6✦ СПОНСОРСТВО STAR STREAM ✦"
    },
    "awakening_system.notification.star_stream_sponsor.desc": {
        "es": "§f¡Una Constelación te ha transferido §e%s Monedas§f!",
        "en": "§fA Constellation transferred §e%s Coins§f to you!",
        "ru": "§fСозвездие передало вам §e%s Монет§f!"
    },
    # Jobs and skills
    "awakening_system.notification.job_available.title": {
        "es": "§6✦ ¡DESPERTAR DE OCUPACIÓN DISPONIBLE! ✦",
        "en": "§6✦ OCCUPATION AWAKENING AVAILABLE! ✦",
        "ru": "§6✦ ДОСТУПНО ПРОБУЖДЕНИЕ ПРОФЕССИИ! ✦"
    },
    "awakening_system.notification.job_available.desc": {
        "es": "§fHas alcanzado el Nivel 30. Abre tu Sistema [K] para elegir tu Clase Definitiva.",
        "en": "§fYou reached Level 30. Open your System [K] to choose your Ultimate Class.",
        "ru": "§fВы достигли уровня 30. Откройте Систему [K], чтобы выбрать свой высший класс."
    },
    "awakening_system.notification.sp_gained.title": {
        "es": "§6✦ PUNTO DE HABILIDAD (SP) ✦",
        "en": "§6✦ SKILL POINT (SP) ✦",
        "ru": "§6✦ ОЧКО НАВЫКА (SP) ✦"
    },
    "awakening_system.notification.sp_gained.desc": {
        "es": "§f¡Has ganado §e1 SP §fpara tu Árbol de Habilidades de Clase!",
        "en": "§fYou earned §e1 SP §ffor your Class Skill Tree!",
        "ru": "§fВы получили §e1 SP §fдля дерева классовых навыков!"
    },
    "awakening_system.notification.job_low_level.title": {
        "es": "§cNivel Insuficiente",
        "en": "§cInsufficient Level",
        "ru": "§cНедостаточный уровень"
    },
    "awakening_system.notification.job_low_level.desc": {
        "es": "§fNecesitas alcanzar el §eNivel 30 §fpara despertar una Ocupación.",
        "en": "§fYou need to reach §eLevel 30 §fto awaken an Occupation.",
        "ru": "§fВам необходимо достичь §eУровня 30§f, чтобы пробудить Профессию."
    },
    "awakening_system.notification.job_awakened.title": {
        "es": "§6¡DESPERTAR DE OCUPACIÓN COMPLETADO!",
        "en": "§6OCCUPATION AWAKENING COMPLETED!",
        "ru": "§6ПРОБУЖДЕНИЕ ПРОФЕССИИ ЗАВЕРШЕНО!"
    },
    "awakening_system.notification.job_awakened.desc": {
        "es": "§fTe has convertido en un %s§f.",
        "en": "§fYou have become a %s§f.",
        "ru": "§fВы стали %s§f."
    },
    "awakening_system.notification.skill_req_not_met.title": {
        "es": "§cRequisitos No Cumplidos",
        "en": "§cRequirements Not Met",
        "ru": "§cТребования не выполнены"
    },
    "awakening_system.notification.skill_req_not_met.desc": {
        "es": "§fNo cumples los requisitos de nivel, nodo previo o puntos de habilidad (SP).",
        "en": "§fYou do not meet the level, previous node, or skill point (SP) requirements.",
        "ru": "§fВы не соответствуете требованиям уровня, предыдущего узла или очков навыков (SP)."
    },
    "awakening_system.notification.skill_sp_needed.title": {
        "es": "§cPuntos Insuficientes",
        "en": "§cInsufficient Points",
        "ru": "§cНедостаточно очков"
    },
    "awakening_system.notification.skill_sp_needed.desc": {
        "es": "§fNecesitas al menos %s Punto(s) de Habilidad (SP).",
        "en": "§fYou need at least %s Skill Point(s) (SP).",
        "ru": "§fВам требуется как минимум %s очко(в) навыка (SP)."
    },
    "awakening_system.notification.skill_learned.title": {
        "es": "§6¡HABILIDAD DESBLOQUEADA!",
        "en": "§6SKILL UNLOCKED!",
        "ru": "§6НАВЫК РАЗБЛОКИРОВАН!"
    },
    "awakening_system.notification.skill_learned.desc": {
        "es": "§fHas aprendido §b%s§f en tu Árbol de Clase.",
        "en": "§fYou learned §b%s§f in your Class Tree.",
        "ru": "§fВы изучили §b%s§f в дереве своего класса."
    },
    "awakening_system.notification.skill_upgrade_req.title": {
        "es": "§cMejora No Disponible",
        "en": "§cUpgrade Unavailable",
        "ru": "§cУлучшение недоступно"
    },
    "awakening_system.notification.skill_upgrade_req.desc": {
        "es": "§fNo cumples los requisitos de nivel o puntos de habilidad (SP) para mejorar esta técnica.",
        "en": "§fYou do not meet the level or SP requirements to upgrade this technique.",
        "ru": "§fВы не соответствуете требованиям уровня или SP для улучшения этой техники."
    },
    "awakening_system.notification.skill_upgrade_sp.title": {
        "es": "§cPuntos Insuficientes",
        "en": "§cInsufficient Points",
        "ru": "§cНедостаточно очков"
    },
    "awakening_system.notification.skill_upgrade_sp.desc": {
        "es": "§fNecesitas %s SP para mejorar esta habilidad.",
        "en": "§fYou need %s SP to upgrade this skill.",
        "ru": "§fВам требуется %s SP для улучшения этого навыка."
    },
    "awakening_system.notification.skill_upgraded.title": {
        "es": "§6¡HABILIDAD MEJORADA!",
        "en": "§6SKILL UPGRADED!",
        "ru": "§6НАВЫК УЛУЧШЕН!"
    },
    "awakening_system.notification.skill_upgraded.desc": {
        "es": "§b%s §fha ascendido a §eRango %s§f.",
        "en": "§b%s §fhas ascended to §eRank %s§f.",
        "ru": "§b%s §fвозвысился до §eРанга %s§f."
    },
    # Armor active skills
    "awakening_system.notification.armor_skill_blink.title": {
        "es": "§b✦ PARPADEO DIMENSIONAL ✦",
        "en": "§b✦ DIMENSIONAL BLINK ✦",
        "ru": "§b✦ ПРОСТРАНСТВЕННЫЙ СКАЧОК ✦"
    },
    "awakening_system.notification.armor_skill_blink.desc": {
        "es": "§f¡Has rasgado el espacio a velocidad supersónica!",
        "en": "§fYou ripped through space at supersonic speed!",
        "ru": "§fВы рассекли пространство на сверхзвуковой скорости!"
    },
    "awakening_system.notification.armor_skill_magma.title": {
        "es": "§c✦ FISURA DE MAGMA ✦",
        "en": "§c✦ MAGMA FISSURE ✦",
        "ru": "§c✦ МАГМАТИЧЕСКИЙ РАЗЛОМ ✦"
    },
    "awakening_system.notification.armor_skill_magma.desc": {
        "es": "§6¡Columnas de magma y roca fundida expulsan a tus enemigos!",
        "en": "§6Pillars of magma and molten rock blast enemies away!",
        "ru": "§6Столбы магмы и расплавленной породы отбрасывают врагов!"
    },
    "awakening_system.notification.armor_skill_jade_bell.title": {
        "es": "§a✦ CAMPANA DORADA DE JADE ✦",
        "en": "§a✦ JADE GOLDEN BELL ✦",
        "ru": "§a✦ НЕФРИТОВЫЙ ЗОЛОТОЙ КОЛОКОЛ ✦"
    },
    "awakening_system.notification.armor_skill_jade_bell.desc": {
        "es": "§2¡Invulnerabilidad y reflejo absoluto de daño activados por 5s!",
        "en": "§2Invulnerability and absolute damage reflection active for 5s!",
        "ru": "§2Неуязвимость и абсолютное отражение урона активны 5с!"
    },
    "awakening_system.notification.armor_skill_astral_burst.title": {
        "es": "§5✦ ESTALLIDO ASTRAL DEL DRAGÓN ✦",
        "en": "§5✦ ASTRAL DRAGON BURST ✦",
        "ru": "§5✦ АСТРАЛЬНЫЙ ВЗРЫВ ДРАКОНА ✦"
    },
    "awakening_system.notification.armor_skill_astral_burst.desc": {
        "es": "§d¡El rugido del Dragón Celestial y una supernova estelar devastan el campo!",
        "en": "§dThe Celestial Dragon's roar and a stellar supernova devastate the field!",
        "ru": "§dРёв Небесного Дракона и звёздная сверхновая опустошают поле боя!"
    },
    "awakening_system.notification.armor_skill_tyrant_shroud.title": {
        "es": "§8✦ MANTO DEL TIRANO ✦",
        "en": "§8✦ TYRANT'S SHROUD ✦",
        "ru": "§8✦ САВАН ТИРАНА ✦"
    },
    "awakening_system.notification.armor_skill_tyrant_shroud.desc": {
        "es": "§7Las sombras aterrorizan e inmovilizan a todos los enemigos.",
        "en": "§7Shadows terrify and immobilize all enemies.",
        "ru": "§7Тени вселяют ужас и сковывают всех врагов."
    },
    "awakening_system.notification.armor_skill_valter_rage.title": {
        "es": "§c✦ FURIA DE VALTER ✦",
        "en": "§c✦ VALTER'S RAGE ✦",
        "ru": "§c✦ ЯРОСТЬ ВАЛЬТЕРА ✦"
    },
    "awakening_system.notification.armor_skill_valter_rage.desc": {
        "es": "§4¡Fuerza berserker desatada! +Velocidad, +Fuerza y +Absorción.",
        "en": "§4Berserker might unleashed! +Speed, +Strength, and +Absorption.",
        "ru": "§4Сила берсерка пробуждена! +Скорость, +Сила и +Поглощение."
    },
    "awakening_system.notification.armor_skill_plum_bloom.title": {
        "es": "§d✦ FLORACIÓN DEL CIRUELO ✦",
        "en": "§d✦ PLUM BLOSSOM BLOOM ✦",
        "ru": "§d✦ ЦВЕТЕНИЕ СЛИВЫ ✦"
    },
    "awakening_system.notification.armor_skill_plum_bloom.desc": {
        "es": "§f¡Danza de cien espadas con pétalos de ciruelo!",
        "en": "§fDance of a hundred swords with plum blossom petals!",
        "ru": "§fТанец сотни клинков с лепестками сливы!"
    },
    "awakening_system.notification.armor_skill_reader_eye.title": {
        "es": "§f✦ OJO DEL LECTOR ✦",
        "en": "§f✦ READER'S EYE ✦",
        "ru": "§f✦ ОКО ЧИТАТЕЛЯ ✦"
    },
    "awakening_system.notification.armor_skill_reader_eye.desc": {
        "es": "§b«El protagonista observa el escenario completo con visión omnisciente».",
        "en": "§b«The protagonist observes the full scenario with omniscient vision».",
        "ru": "§b«Главный герой обозревает весь сценарий всевидящим взором»."
    },
    # Pills
    "awakening_system.item.black_tortoise_pill.notify_title": {
        "es": "§3✦ ELIXIR DE LA TORTUGA NEGRA ✦",
        "en": "§3✦ BLACK TORTOISE ELIXIR ✦",
        "ru": "§3✦ ЭЛИКСИР ЧЁРНОЙ ЧЕРЕПАХИ ✦"
    },
    "awakening_system.item.black_tortoise_pill.notify_desc": {
        "es": "§bVitalidad inmutable y Resistencia Marcial activadas.",
        "en": "§bImmutable Vitality and Martial Resistance activated.",
        "ru": "§bНепоколебимая живучесть и воинское сопротивление активированы."
    },
    "awakening_system.item.marrow_cleansing_pill.opened_title": {
        "es": "§a✦ LIMPIEZA DE MÉDULA ✦",
        "en": "§a✦ MARROW CLEANSING ✦",
        "ru": "§a✦ ОЧИЩЕНИЕ КОСТНОГО МОЗГА ✦"
    },
    "awakening_system.item.marrow_cleansing_pill.opened_desc": {
        "es": "§f¡Impurezas purificadas! Has desbloqueado el Meridiano %s.",
        "en": "§fImpurities cleansed! You unlocked Meridian %s.",
        "ru": "§fПримеси очищены! Вы открыли Меридиан %s."
    },
    "awakening_system.item.marrow_cleansing_pill.max_title": {
        "es": "§a✦ MÉDULA PERFECTA ✦",
        "en": "§a✦ PERFECT MARROW ✦",
        "ru": "§a✦ СОВЕРШЕННЫЙ МОЗГ ✦"
    },
    "awakening_system.item.marrow_cleansing_pill.max_desc": {
        "es": "§fTus 12 meridianos ya están abiertos: +1,200 Qi de Cultivo.",
        "en": "§fAll 12 meridians already opened: +1,200 Cultivation Qi.",
        "ru": "§fВсе 12 меридианов уже открыты: +1,200 Ци культивации."
    },
    "awakening_system.item.realm_breakthrough_pill.pinnacle_title": {
        "es": "§d✦ CÚSPIDE DE REINO ALCANZADA ✦",
        "en": "§d✦ REALM PINNACLE REACHED ✦",
        "ru": "§d✦ ВЕРШИНА ЦАРСТВА ДОСТИГНУТА ✦"
    },
    "awakening_system.item.realm_breakthrough_pill.pinnacle_desc": {
        "es": "§fHas alcanzado el Sub-nivel 9 de %s.",
        "en": "§fYou reached Sub-level 9 of %s.",
        "ru": "§fВы достигли 9-го подуровня царства %s."
    },
    "awakening_system.item.realm_breakthrough_pill.breakthrough_title": {
        "es": "§6✦ ¡RUPTURA DE REINO EXITOSA! ✦",
        "en": "§6✦ REALM BREAKTHROUGH SUCCESSFUL! ✦",
        "ru": "§6✦ ПРОРЫВ ЦАРСТВА УСПЕШЕН! ✦"
    },
    "awakening_system.item.realm_breakthrough_pill.breakthrough_desc": {
        "es": "§f¡Has avanzado al %s!",
        "en": "§fYou advanced to %s!",
        "ru": "§fВы перешли в %s!"
    },
    "awakening_system.item.rising_dragon_pill.notify_title": {
        "es": "§6✦ PÍLDORA DEL DRAGÓN NACIENTE ✦",
        "en": "§6✦ RISING DRAGON PILL ✦",
        "ru": "§6✦ ПИЛЮЛЯ ВОСХОДЯЩЕГО ДРАКОНА ✦"
    },
    "awakening_system.item.rising_dragon_pill.notify_desc": {
        "es": "§e¡Torrente espiritual de dragón! +1,500 Qi de Cultivo.",
        "en": "§eDragon spiritual torrent! +1,500 Cultivation Qi.",
        "ru": "§eДуховный поток дракона! +1,500 Ци культивации."
    },
    # Stigmas
    "awakening_system.stigma.activated.judge_flames": {
        "es": "§6§l✦ ESTIGMA ACTIVADO: §e¡Llamas del Juicio del Juez del Fuego! ✦",
        "en": "§6§l✦ STIGMA ACTIVATED: §eFlames of Judgment of the Fire Judge! ✦",
        "ru": "§6§l✦ СТИГМА АКТИВИРОВАНА: §eПламя Суда Огненного Судьи! ✦"
    },
    "awakening_system.stigma.activated.ruyi_staff": {
        "es": "§6§l✦ ESTIGMA ACTIVADO: §e¡Impacto Sísmico del Báculo Ruyi! ✦",
        "en": "§6§l✦ STIGMA ACTIVATED: §eSeismic Impact of the Ruyi Staff! ✦",
        "ru": "§6§l✦ СТИГМА АКТИВИРОВАНА: §eСейсмический удар посоха Руйи! ✦"
    },
    "awakening_system.stigma.activated.shadow_monarch": {
        "es": "§8§l✦ ESTIGMA ACTIVADO: §9¡Dominio Absoluto del Monarca de las Sombras! ✦",
        "en": "§8§l✦ STIGMA ACTIVATED: §9Absolute Domain of the Shadow Monarch! ✦",
        "ru": "§8§l✦ СТИГМА АКТИВИРОВАНА: §9Абсолютное владение Монарха Теней! ✦"
    },
    "awakening_system.stigma.activated.chaos_dragon": {
        "es": "§4§l✦ ESTIGMA ACTIVADO: §c¡Aliento de Aniquilación del Dragón del Caos! ✦",
        "en": "§4§l✦ STIGMA ACTIVATED: §cAnnihilation Breath of the Chaos Dragon! ✦",
        "ru": "§4§l✦ СТИГМА АКТИВИРОВАНА: §cДыхание аннигиляции Дракона Хаоса! ✦"
    },
    "awakening_system.stigma.activated.regressor_eye": {
        "es": "§b§l✦ ESTIGMA ACTIVADO: §3¡Ojo Trascendente del Regresor Supremo! ✦",
        "en": "§b§l✦ STIGMA ACTIVATED: §3Transcendent Eye of the Supreme Regressor! ✦",
        "ru": "§b§l✦ СТИГМА АКТИВИРОВАНА: §3Трансцендентное Око Высшего Регрессора! ✦"
    },
    "awakening_system.stigma.activated.plum_blossom": {
        "es": "§d§l✦ ESTIGMA ACTIVADO: §f¡Veinticuatro Movimientos del Ciruelo en Flor! ✦",
        "en": "§d§l✦ STIGMA ACTIVATED: §fTwenty-Four Movements of Plum Blossoms! ✦",
        "ru": "§d§l✦ СТИГМА АКТИВИРОВАНА: §fДвадцать четыре движения цветущей сливы! ✦"
    },
    "awakening_system.stigma.activated.sponsor_blessing": {
        "es": "§e§l✦ ESTIGMA ACTIVADO: §6¡Bendición Sagrada de la Constelación Patrocinadora! ✦",
        "en": "§e§l✦ STIGMA ACTIVATED: §6Holy Blessing of Sponsor Constellation! ✦",
        "ru": "§e§l✦ СТИГМА АКТИВИРОВАНА: §6Священное благословение Созвездия-Спонсора! ✦"
    },
    # General Actions
    "awakening_system.action.already_in_dungeon": {
        "es": "§cYa te encuentras dentro de un calabozo activo.",
        "en": "§cYou are already inside an active dungeon.",
        "ru": "§cВы уже находитесь в активном подземелье."
    },
    "awakening_system.action.need_key": {
        "es": "§cNecesitas una Llave Dimensional en tu inventario.",
        "en": "§cYou need a Dimensional Key in your inventory.",
        "ru": "§cВам нужен Пространственный Ключ в инвентаре."
    },
    "awakening_system.action.yggdrasil_summit": {
        "es": "§d§l[Cúspide de Yggdrasil] §fHas ascendido sobre la copa de hojas del Gran Árbol. Explora el dosel para hallar la entrada oculta.",
        "en": "§d§l[Yggdrasil Summit] §fYou ascended above the Great Tree's leaves. Explore the canopy to find the hidden entrance.",
        "ru": "§d§l[Вершина Иггдрасиля] §fВы поднялись на крону Великого Древа. Исследуйте полог, чтобы найти скрытый вход."
    },
    "awakening_system.action.tribulation_active": {
        "es": "§c§l[Tribulación Celestial] §fYa te encuentras bajo el juicio del Rayo Divino. ¡Sobrevive primero!",
        "en": "§c§l[Heavenly Tribulation] §fYou are already under Divine Lightning judgment. Survive first!",
        "ru": "§c§l[Небесная кара] §fВы уже под судом Божественной молнии. Сначала выживите!"
    },
    "awakening_system.action.physique_already_set": {
        "es": "§c[CULTIVO] Tu físico marcial ya ha sido fijado por el destino y no puede ser alterado.",
        "en": "§c[CULTIVATION] Your martial physique has already been set by destiny and cannot be altered.",
        "ru": "§c[КУЛЬТИВАЦИЯ] Ваше телосложение уже определено судьбой и не может быть изменено."
    },
    "awakening_system.action.isekai_requires_sys20": {
        "es": "§cAcceso al Mundo Isekai bloqueado. Requiere Sistema 2.0.",
        "en": "§cIsekai World access locked. Requires System 2.0.",
        "ru": "§cДоступ в мир Исекай заблокирован. Требуется Система 2.0."
    },
    "awakening_system.action.murim_requires_sys30": {
        "es": "§cAcceso al Reino Murim bloqueado. Requiere ascensión al Sistema 3.0.",
        "en": "§cMurim Realm access locked. Requires System 3.0 ascension.",
        "ru": "§cДоступ в мир Мурим заблокирован. Требуется вознесение до Системы 3.0."
    },
    "awakening_system.action.no_armor_skill": {
        "es": "§c[No tienes equipado un conjunto completo de armadura con habilidad activa]",
        "en": "§c[You do not have a full armor set with an active skill equipped]",
        "ru": "§c[На вас не экипирован полный комплект брони с активным навыком]"
    },
    "awakening_system.roulette.insufficient_coins": {
        "es": "§c§l[RULETA] §7Saldo insuficiente (1.000 Monedas requeridas).",
        "en": "§c§l[ROULETTE] §7Insufficient balance (1,000 Coins required).",
        "ru": "§c§l[РУЛЕТКА] §7Недостаточно средств (требуется 1 000 Монет)."
    },
    # Weapons
    "awakening_system.weapon.adamantite_warhammer.smash": {
        "es": "§c§l✦ ¡GOLPE DEMOLEDOR! §fEnemigo aturdido (Lentitud IV, Debilidad II)",
        "en": "§c§l✦ CRUSHING BLOW! §fEnemy stunned (Slowness IV, Weakness II)",
        "ru": "§c§l✦ СОКРУШИТЕЛЬНЫЙ УДАР! §fВраг оглушён (Замедление IV, Слабость II)"
    },
    "awakening_system.weapon.adamantite_warhammer.plunge": {
        "es": "§6§l✦ ¡CAÍDA EN PICADO SÍSMICA!",
        "en": "§6§l✦ SEISMIC NOSEDIVE!",
        "ru": "§6§l✦ СЕЙСМИЧЕСКОЕ ПИКИРОВАНИЕ!"
    },
    "awakening_system.weapon.astral_guandao.slash": {
        "es": "§b§l✦ ¡TAJO DE DRAGÓN ASTRAL! §f(Creciente Celestial desatado)",
        "en": "§b§l✦ ASTRAL DRAGON SLASH! §f(Celestial Crescent unleashed)",
        "ru": "§b§l✦ РАЗРЕЗ АСТРАЛЬНОГО ДРАКОНА! §f(Небесный полумесяц пробуждён)"
    },
    "awakening_system.item.awakening_coin.toss": {
        "es": "§6§l[LANZAMIENTO] §e¡Lanzando ficha al aire (Cara o Cruz)...!",
        "en": "§6§l[TOSS] §eTossing coin into the air (Heads or Tails)...!",
        "ru": "§6§l[БРОСОК] §eПодбрасывание монеты в воздух (Орёл или Решка)...!"
    },
    "awakening_system.item.celestial_dragon_pearl.assimilated": {
        "es": "§6§l✦ [PERLA CELESTIAL] §fHas asimilado el núcleo dracónico (+1,000 Qi / Salud y Energía al 100%).",
        "en": "§6§l✦ [CELESTIAL PEARL] §fYou assimilated the draconic core (+1,000 Qi / 100% Health and Energy).",
        "ru": "§6§l✦ [НЕБЕСНАЯ ЖЕМЧУЖИНА] §fВы усвоили ядро дракона (+1,000 Ци / 100% Здоровья и Энергии)."
    },
    "awakening_system.item.chaos_mana_core.assimilated": {
        "es": "§5§l[NÚCLEO DEL CAOS] §fHas asimilado la energía de los Cuatro Reyes Elementales (+3500 Qi / +2000 EXP).",
        "en": "§5§l[CHAOS CORE] §fYou assimilated the Four Elemental Kings' energy (+3,500 Qi / +2,000 EXP).",
        "ru": "§5§l[ЯДРО ХАОСА] §fВы усвоили энергию Четырёх Королей Стихий (+3,500 Ци / +2,000 Опыта)."
    },
    "awakening_system.weapon.plum_blossom_sword.moves_24": {
        "es": "§d§l[24 Movimientos de Flor de Ciruelo] §f¡Torbellino de pétalos de Qi desatado!",
        "en": "§d§l[24 Plum Blossom Movements] §fWhirlwind of Qi petals unleashed!",
        "ru": "§d§l[24 Движения Цветка Сливы] §fВихрь лепестков Ци пробуждён!"
    },
    "awakening_system.weapon.plum_blossom_sword.bloom_burst": {
        "es": "§d§l✦ ¡FLORECIMIENTO TOTAL DEL MONTE HUA! §f(+15 Daño Qi Explosivo)",
        "en": "§d§l✦ TOTAL BLOOM OF MOUNT HUA! §f(+15 Explosive Qi Damage)",
        "ru": "§d§l✦ ПОЛНОЕ ЦВЕТЕНИЕ ГОРЫ ХУА! §f(+15 Взрывного урона Ци)"
    },
    "awakening_system.weapon.radiant_light_sword.light_dance": {
        "es": "§e§l[Danza de Luz Radiante] §f¡Purificación sagrada y corte cegador desatados!",
        "en": "§e§l[Radiant Light Dance] §fHoly purification and blinding slash unleashed!",
        "ru": "§e§l[Танец Лучезарного Света] §fСвятое очищение и ослепительный разрез пробуждены!"
    },
    # World & Blocks
    "awakening_system.dimension.isekai_welcome": {
        "es": "§d[Mundo Isekai] §fBienvenido de vuelta al Gran Valle de Yggdrasil.",
        "en": "§d[Isekai World] §fWelcome back to the Great Valley of Yggdrasil.",
        "ru": "§d[Мир Исекай] §fДобро пожаловать обратно в Великую долину Иггдрасиля."
    },
    "awakening_system.dimension.murim_welcome": {
        "es": "§6[Reino Murim] §fBienvenido de vuelta a la Gran Secta Inmortal.",
        "en": "§6[Murim Realm] §fWelcome back to the Great Immortal Sect.",
        "ru": "§6[Царство Мурим] §fДобро пожаловать обратно в Великую Бессмертную Секту."
    },
    "awakening_system.dimension.murim_martial_trial": {
        "es": "§6§l[JUICIO MARCIAL] §fAbre el Pabellón de Cultivo §e[H] §fpara despertar tu Constitución Celestial innata.",
        "en": "§6§l[MARTIAL TRIAL] §fOpen the Cultivation Pavilion §e[H] §fto awaken your innate Celestial Constitution.",
        "ru": "§6§l[ВОИНСКОЕ ИСПЫТАНИЕ] §fОткройте Павильон Культивации §e[H]§f, чтобы пробудить врождённую Небесную Конституцию."
    },
    "awakening_system.command.reset_success": {
        "es": "§c§l[SISTEMA]§r Tu progreso ha sido completamente restablecido.",
        "en": "§c§l[SYSTEM]§r Your progress has been completely reset.",
        "ru": "§c§l[СИСТЕМА]§r Ваш прогресс был полностью сброшен."
    },
    "awakening_system.command.constellation_reverted": {
        "es": "§c[STAR STREAM] Has dejado de ser una Constelación. Has regresado al estatus de encarnación.",
        "en": "§c[STAR STREAM] You are no longer a Constellation. You returned to incarnation status.",
        "ru": "§c[STAR STREAM] Вы больше не Созвездие. Вы вернулись к статусу воплощения."
    },
    "awakening_system.block.elemental_altar.all_seals_broken": {
        "es": "§5§l[CASTILLO CENTRAL] §f¡Los 4 Sellos Elementales han caído! La Gran Barrera de Éter se ha disipado y el Salón del Trono del Rey Demonio está abierto.",
        "en": "§5§l[CENTRAL CASTLE] §fAll 4 Elemental Seals have fallen! The Great Aether Barrier has dissolved and the Demon King's Throne Room is open.",
        "ru": "§5§l[ЦЕНТРАЛЬНЫЙ ЗАМОК] §fВсе 4 Стихийные Печати пали! Великий Эфирный Барьер рассеян, и Тронный зал Короля демонов открыт."
    },
    "awakening_system.cauldron.need_mandrake": {
        "es": "§e[Se necesita 1x Raíz de Mandrágora en el inventario para completar la píldora]",
        "en": "§e[1x Mandrake Root required in inventory to complete pill]",
        "ru": "§e[Требуется 1x Корень мандрагоры в инвентаре для завершения пилюли]"
    },
    "awakening_system.cauldron.need_ginseng": {
        "es": "§e[Se necesita 1x Ginseng Milenario en el inventario para completar la píldora]",
        "en": "§e[1x Thousand-Year Ginseng required in inventory to complete pill]",
        "ru": "§e[Требуется 1x Тысячелетний женьшень в инвентаре для завершения пилюли]"
    },
    "awakening_system.cauldron.need_jade": {
        "es": "§e[Se necesita 1x Jade Espiritual o Fragmento de Jade en el inventario]",
        "en": "§e[1x Spirit Jade or Jade Fragment required in inventory]",
        "ru": "§e[Требуется 1x Духовный нефрит или осколок нефрита в инвентаре]"
    },
    "awakening_system.cauldron.need_adamantite": {
        "es": "§e[Se necesita 1x Lingote de Adamantita en el inventario para forjar el elixir]",
        "en": "§e[1x Adamantite Ingot required in inventory to forge elixir]",
        "ru": "§e[Требуется 1x Адамантитовый слиток в инвентаре для создания эликсира]"
    },
    "awakening_system.cauldron.need_fire": {
        "es": "§c[El caldero requiere fuego espiritual bajo su base]",
        "en": "§c[The cauldron requires spiritual fire beneath its base]",
        "ru": "§c[Котёл требует духовного огня под основанием]"
    },
    "awakening_system.cauldron.missing_ingredients_1": {
        "es": "§e[Faltan ingredientes: Ginseng Salvaje o Raíz de Mandrágora]",
        "en": "§e[Missing ingredients: Wild Ginseng or Mandrake Root]",
        "ru": "§e[Не хватает ингредиентов: Дикий женьшень или Корень мандрагоры]"
    },
    "awakening_system.cauldron.missing_ingredients_2": {
        "es": "§e[Faltan ingredientes: Loto de Nieve o Ginseng Milenario]",
        "en": "§e[Missing ingredients: Snow Lotus or Thousand-Year Ginseng]",
        "ru": "§e[Не хватает ингредиентов: Снежный лотос или Тысячелетний женьшень]"
    },
    "awakening_system.cauldron.missing_ingredients_3": {
        "es": "§e[Faltan ingredientes: Ginseng Milenario o Jade Espiritual]",
        "en": "§e[Missing ingredients: Thousand-Year Ginseng or Spirit Jade]",
        "ru": "§e[Не хватает ингредиентов: Тысячелетний женьшень или Духовный нефрит]"
    },
    "awakening_system.cauldron.missing_ingredients_4": {
        "es": "§e[Faltan ingredientes: Raíz de Mandrágora o Lingote de Adamantita]",
        "en": "§e[Missing ingredients: Mandrake Root or Adamantite Ingot]",
        "ru": "§e[Не хватает ингредиентов: Корень мандрагоры или Адамантитовый слиток]"
    },
    "awakening_system.coin_entity.heads": {
        "es": "§6§l[CARA - SOL DORADO] §e¡La fortuna te favorece! (+Suerte II y Rapidez)",
        "en": "§6§l[HEADS - GOLDEN SUN] §eFortune favors you! (+Luck II and Speed)",
        "ru": "§6§l[ОРЁЛ - ЗОЛОТОЕ СОЛНЦЕ] §eУдача на вашей стороне! (+Удача II и Скорость)"
    },
    "awakening_system.coin_entity.tails": {
        "es": "§b§l[CRUZ - LUNA SOMBRÍA] §f¡El destino te protege! (+Escudo de Absorción)",
        "en": "§b§l[TAILS - SHADOW MOON] §fDestiny protects you! (+Absorption Shield)",
        "ru": "§b§l[РЕШКА - ТЕНЕВАЯ ЛУНА] §fСудьба защищает вас! (+Щит поглощения)"
    },
    "awakening_system.entity.blood_qi_totem.all_banners_fallen": {
        "es": "§c§l✦ ¡TODOS LOS ESTANDARTES HAN CAÍDO! ✦\n§fLa barrera de Qi del Gran Patriarca ha colapsado.",
        "en": "§c§l✦ ALL BANNERS HAVE FALLEN! ✦\n§fThe Grand Patriarch's Qi barrier has collapsed.",
        "ru": "§c§l✦ ВСЕ ЗНАМЁНА ПАЛИ! ✦\n§fБарьер Ци Великого Патриарха рухнул."
    },
    "awakening_system.entity.celestial_dragon.recognized": {
        "es": "§6§l✦ ¡EL DRAGÓN CELESTIAL RECONOCE TU ALMA SUPREMA! ✦",
        "en": "§6§l✦ THE CELESTIAL DRAGON RECOGNIZES YOUR SUPREME SOUL! ✦",
        "ru": "§6§l✦ НЕБЕСНЫЙ ДРАКОН ПРИЗНАЁТ ВАШУ ВЫСШУЮ ДУШУ! ✦"
    },
    "awakening_system.entity.celestial_dragon.pearl_restored": {
        "es": "§6§l✦ ¡LA PERLA DEL DRAGÓN CELESTIAL HA RESTAURADO TOTALMENTE AL DRAGÓN! ✦",
        "en": "§6§l✦ THE CELESTIAL DRAGON PEARL HAS FULLY RESTORED THE DRAGON! ✦",
        "ru": "§6§l✦ ЖЕМЧУЖИНА НЕБЕСНОГО ДРАКОНА ПОЛНОСТЬЮ ИСЦЕЛИЛА ДРАКОНА! ✦"
    },
    "awakening_system.entity.celestial_dragon.resting": {
        "es": "§6§l✦ EL DRAGÓN CELESTIAL ENTRA EN REPOSO SERPENTINO ✦",
        "en": "§6§l✦ THE CELESTIAL DRAGON ENTERS SERPENTINE REST ✦",
        "ru": "§6§l✦ НЕБЕСНЫЙ ДРАКОН ПОГРУЖАЕТСЯ В ЗМЕИНЫЙ ПОКОЙ ✦"
    },
    "awakening_system.entity.celestial_dragon.taking_flight": {
        "es": "§6§l✦ EL DRAGÓN CELESTIAL SE PREPARA PARA ELEVARSE ✦",
        "en": "§6§l✦ THE CELESTIAL DRAGON PREPARES TO TAKE FLIGHT ✦",
        "ru": "§6§l✦ НЕБЕСНЫЙ ДРАКОН ГОТОВИТСЯ К ВЗЛЁТУ ✦"
    },
    "awakening_system.entity.celestial_dragon.creative_tamed": {
        "es": "§6§l✦ ¡EL DRAGÓN CELESTIAL FUE DOMESTICADO (MODO CREATIVO)! ✦",
        "en": "§6§l✦ THE CELESTIAL DRAGON WAS TAMED (CREATIVE MODE)! ✦",
        "ru": "§6§l✦ НЕБЕСНЫЙ ДРАКОН ПРИРУЧЁН (ТВОРЧЕСКИЙ РЕЖИМ)! ✦"
    },
    "awakening_system.entity.demon_king.defeated": {
        "es": "§6§l[REY DEMONIO DERROTADO] §f¡El Soberano Elemental ha caído! Has conquistado los Castillos Flotantes de Isekai.",
        "en": "§6§l[DEMON KING DEFEATED] §fThe Elemental Sovereign has fallen! You conquered the Floating Castles of Isekai.",
        "ru": "§6§l[КОРОЛЬ ДЕМОНОВ ПОВЕРЖЕН] §fВладыка Стихий пал! Вы покорили Парящие замки Исекая."
    },
    "awakening_system.entity.elemental_lord.all_seals_broken": {
        "es": "§5§l[CASTILLO CENTRAL] §f¡Los 4 Sellos Elementales han caído! La Gran Barrera de Éter se ha disipado y el Salón del Trono del Rey Demonio está abierto.",
        "en": "§5§l[CENTRAL CASTLE] §fAll 4 Elemental Seals have fallen! The Great Aether Barrier has dissolved and the Demon King's Throne Room is open.",
        "ru": "§5§l[ЦЕНТРАЛЬНЫЙ ЗАМОК] §fВсе 4 Стихийные Печати пали! Великий Эфирный Барьер рассеян, и Тронный зал Короля демонов открыт."
    },
    "awakening_system.entity.mount_hua_disciple.talisman_given": {
        "es": "§d[Discípulo del Monte Hua] §a«Toma este Talismán Taoísta de Sellado. Que el sendero de la Espada de Ciruelo guíe tu cultivo.»",
        "en": "§d[Mount Hua Disciple] §a«Take this Taoist Sealing Talisman. May the path of the Plum Sword guide your cultivation.»",
        "ru": "§d[Ученик горы Хуа] §a«Возьми этот даосский талисман запечатывания. Пусть путь Сливового меча ведёт твою культивацию.»"
    },
    "awakening_system.entity.mount_hua_disciple.greeting": {
        "es": "§d[Discípulo del Monte Hua] §f«Saludos, compañero cultivador. Las flores de ciruelo del Monte Hua florecen firmes contra la corrupción demoníaca.»",
        "en": "§d[Mount Hua Disciple] §f«Greetings, fellow cultivator. Mount Hua's plum blossoms bloom firm against demonic corruption.»",
        "ru": "§d[Ученик горы Хуа] §f«Приветствую, собрат по культивации. Цветы сливы горы Хуа цветут стойко против демонической скверны.»"
    },
    "awakening_system.furnace.upgrade_removed": {
        "es": "§e✦ Mejora desinstalada. Horno restaurado a modo estándar.",
        "en": "§e✦ Upgrade removed. Furnace restored to standard mode.",
        "ru": "§e✦ Улучшение снято. Печь возвращена в стандартный режим."
    },
    "awakening_system.wuxing.already_open": {
        "es": "§a[Wu Xing] §fEl Ciclo de los Cinco Elementos ya está en perfecta armonía y la compuerta sagrada permanece abierta.",
        "en": "§a[Wu Xing] §fThe Five Elements Cycle is already in perfect harmony and the sacred gate remains open.",
        "ru": "§a[У-Син] §fКруговорот Пяти Стихий уже в совершенной гармонии, и священные врата открыты."
    },
    "awakening_system.wuxing.gate_opened": {
        "es": "§6§l[GRUTA ANCESTRAL] §f¡El Ciclo de los Cinco Elementos está en armonía perfecta! La Gran Compuerta de Jade se ha abierto.",
        "en": "§6§l[ANCESTRAL GROTTO] §fThe Five Elements Cycle is in perfect harmony! The Great Jade Gate has opened.",
        "ru": "§6§l[ДРЕВНИЙ ГРОТ] §fКруговорот Пяти Стихий в совершенной гармонии! Великие Нефритовые Врата открылись."
    },
    # HUD Overlay
    "awakening_system.hud.level_prefix": {
        "es": "Nv.",
        "en": "Lv.",
        "ru": "Ур."
    },
    "awakening_system.realm.compact.0": {
        "es": "Mor",
        "en": "Mort",
        "ru": "Смерт"
    },
    "awakening_system.realm.compact.1": {
        "es": "Cond",
        "en": "Cond",
        "ru": "Конд"
    },
    "awakening_system.realm.compact.2": {
        "es": "Cim",
        "en": "Fnd",
        "ru": "Осн"
    },
    "awakening_system.realm.compact.3": {
        "es": "Núc",
        "en": "Core",
        "ru": "Ядро"
    },
    "awakening_system.realm.compact.4": {
        "es": "Alma",
        "en": "Soul",
        "ru": "Душа"
    },
    "awakening_system.realm.compact.5": {
        "es": "Asc",
        "en": "Asc",
        "ru": "Возв"
    },
    "awakening_system.realm.compact.6": {
        "es": "Trib",
        "en": "Trib",
        "ru": "Исп"
    },
    "awakening_system.realm.compact.7": {
        "es": "Inm",
        "en": "Imm",
        "ru": "Бессм"
    }
}

print(f"Loaded {len(TRANSLATIONS)} screen translations.")

# 2. Update all 11 lang files
lang_dir = "src/main/resources/assets/awakening_system/lang"
lang_files = [f for f in os.listdir(lang_dir) if f.endswith(".json")]

for fname in lang_files:
    fpath = os.path.join(lang_dir, fname)
    with open(fpath, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    lang_type = "en" if fname == "en_us.json" else ("ru" if fname == "ru_ru.json" else "es")
    
    added = 0
    for k, v in TRANSLATIONS.items():
        val = v.get(lang_type, v["es"])
        if k not in data or data[k] != val:
            data[k] = val
            added += 1
            
    with open(fpath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Updated {fname}: added/updated {added} keys. Total keys now: {len(data)}")

print("Language files updated successfully!")
