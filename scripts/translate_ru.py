import json
import sys

RU_TRANSLATIONS = {
    # 1. Artifact Messages
    "awakening_system.artifact.msg.companion_equipped": "§a§l✦ [АРТЕФАКТ] §fЭкипирован %s%s §fдля %s!",
    "awakening_system.artifact.msg.companion_unequipped": "§e✦ Артефакт спутника %s снят и помещён в инвентарь.",
    "awakening_system.artifact.msg.cost_coins": "§e(Потрачено %s Монет)",
    "awakening_system.artifact.msg.cost_ore": "§b(Использована 1 Руда усиления)",
    "awakening_system.artifact.msg.equipped": "§a§l✦ [АРТЕФАКТ ЭКИПИРОВАН] §fВы надели %s%s §7(%s).",
    "awakening_system.artifact.msg.insufficient_funds": "§c[Артефакты] Недостаточно средств. Требуется §e%s Монет Пробуждения §cили §b1 Руда усиления §cдля улучшения до +%s.",
    "awakening_system.artifact.msg.max_level": "§e✦ Этот артефакт уже достиг максимального уровня (+20).",
    "awakening_system.artifact.msg.unequipped": "§e✦ [АРТЕФАКТ] Артефакт снят и помещён в инвентарь.",
    "awakening_system.artifact.msg.upgraded": "§6§l✦ [АРТЕФАКТ УЛУЧШЕН] §f%s §fулучшен до §a+%s§f! %s",
    "awakening_system.artifact.msg.wrong_slot": "§cЭтот предмет (%s) нельзя надеть в данный слот.",

    # 2. Artifact Tooltips
    "awakening_system.artifact.tooltip.2p": " §e[2 предмета]: §7%s",
    "awakening_system.artifact.tooltip.4p": " §e[4 предмета]: §7%s",
    "awakening_system.artifact.tooltip.atk": "§c✦ Базовая АТК: §f+%s АТК",
    "awakening_system.artifact.tooltip.atk_pct": "§6✦ Бонус АТК: §f+%s%%",
    "awakening_system.artifact.tooltip.crit_dmg": "§e✦ Крит. урон: §f+%s%%",
    "awakening_system.artifact.tooltip.crit_rate": "§e✦ Шанс крит.: §f+%s%%",
    "awakening_system.artifact.tooltip.elemental": "§b✦ Элементальный урон: §f+%s%%",
    "awakening_system.artifact.tooltip.hint": "§8▸ Экипируется в Системе (Игрок) или у Спутников.",
    "awakening_system.artifact.tooltip.hp": "§a✦ Макс. HP: §f+%s HP",
    "awakening_system.artifact.tooltip.set_bonuses": "§6§l✦ Бонусы комплекта:",
    "awakening_system.artifact.tooltip.type": "§7Тип: §e%s §8| §fУсиление: §a+%s§7/20",

    # 3. Artifact Sets
    "awakening_system.artifact_set.berserker.name": "Берсерк",
    "awakening_system.artifact_set.berserker.bonus2p": "+12% к шансу критического попадания.",
    "awakening_system.artifact_set.berserker.bonus4p": "При HP ниже 70% шанс крит. попадания увеличивается дополнительно на 24%.",
    "awakening_system.artifact_set.bloodstained.name": "Рыцарь крови",
    "awakening_system.artifact_set.bloodstained.bonus2p": "+25% к физическому урону и бронепробитию.",
    "awakening_system.artifact_set.bloodstained.bonus4p": "Победа над противником увеличивает урон следующего удара на 50% и сбивает цель с ног.",
    "awakening_system.artifact_set.brave_heart.name": "Душа храбреца",
    "awakening_system.artifact_set.brave_heart.bonus2p": "+18% к базовой силе атаки.",
    "awakening_system.artifact_set.brave_heart.bonus4p": "Увеличивает урон на 30% против врагов, у которых более 50% HP.",
    "awakening_system.artifact_set.crimson_witch.name": "Горящая алая ведьма",
    "awakening_system.artifact_set.crimson_witch.bonus2p": "+20% к Пиро урону и навыкам огня.",
    "awakening_system.artifact_set.crimson_witch.bonus4p": "Взрывы и горение наносят на 40% больше урона и длятся вдвое дольше.",
    "awakening_system.artifact_set.exile.name": "Изгнанник",
    "awakening_system.artifact_set.exile.bonus2p": "+20% к восстановлению энергии и маны.",
    "awakening_system.artifact_set.exile.bonus4p": "Применение ультимейта или навыка мгновенно восстанавливает ману вам и союзникам.",
    "awakening_system.artifact_set.gladiator.name": "Конец гладиатора",
    "awakening_system.artifact_set.gladiator.bonus2p": "+18% к физической атаке и базовому урону.",
    "awakening_system.artifact_set.gladiator.bonus4p": "+35% к урону при атаке мечами, кинжалами и копьями.",
    "awakening_system.artifact_set.maiden.name": "Возлюбленная юная дева",
    "awakening_system.artifact_set.maiden.bonus2p": "+20% к эффективности лечения и регенерации.",
    "awakening_system.artifact_set.maiden.bonus4p": "Лечение себя или спутника восстанавливает на 25% больше здоровья всем союзникам рядом.",
    "awakening_system.artifact_set.viridescent.name": "Изумрудная тень",
    "awakening_system.artifact_set.viridescent.bonus2p": "+20% к Анемо урону и +15% к скорости передвижения.",
    "awakening_system.artifact_set.viridescent.bonus4p": "Вихри рассеивания снижают физическое и элементальное сопротивление противников на 30%.",

    # 4. Artifact Types
    "awakening_system.artifact_type.circlet.name": "Корона разума",
    "awakening_system.artifact_type.flower.name": "Цветок жизни",
    "awakening_system.artifact_type.goblet.name": "Кубок пространства",
    "awakening_system.artifact_type.plume.name": "Перо смерти",
    "awakening_system.artifact_type.sands.name": "Пески времени",

    # 5. Bosses
    "awakening_system.boss.ancient_celestial_dragon.defeated_state": "§c§l✦ НЕБЕСНЫЙ ДРАКОН СОВЕРЖЕН — НАНЕСИТЕ РЕШАЮЩИЙ УДАР! ✦",
    "awakening_system.boss.ancient_celestial_dragon.name": "§6§l✦ ДРЕВНИЙ НЕБЕСНЫЙ ДРАКОН ДЕВЯТИ НЕБЕС ✦",
    "awakening_system.boss.antares.defeat_quote": "§4§l[АНТАРЕС] §cБудь ты проклят, охотник...! Пламя разрушения и дыхание дракона никогда не угаснут...",
    "awakening_system.boss.ashborn.defeat_quote": "§8§l[ЭШБОРН] §7Ты доказал своё право... Власть тьмы и теневой легион теперь внемлют твоему зову.",
    "awakening_system.boss.shadow_monarch.domain_taunt": "§8§l[ТЕНЕВОЙ МОНАРХ] §5Это царство принадлежит мне! Пусть тьма поглотит ваши души!",
    "awakening_system.boss.shadow_monarch.phase2_taunt": "§8§l[ТЕНЕВОЙ МОНАРХ] §d§lТы думал, что загнал в угол саму Смерть? СКЛОНИСЬ ПЕРЕД ИСТИННЫМ ВЛАДЫКОЙ!",

    # 6. Castles
    "awakening_system.castles.barrier_dispelled": "§5§l[ЦЕНТРАЛЬНЫЙ ЗАМОК] §fВсе 4 стихийные печати пали! Великий эфирный барьер рассеялся, тронный зал Повелителя Демонов открыт.",
    "awakening_system.castles.demon_king_defeated_chat": "§6§l[ПОВЕЛИТЕЛЬ ДЕМОНОВ ПОВЕРЖЕН] §fСтихийный Владыка пал! Вы покорили Парящие Замки другого мира.",
    "awakening_system.castles.elemental_lord_defeated": "§6§l[СТИХИЙНЫЙ ВЛАДЫКА ПОВЕРЖЕН] §fВы одолели %s§f! Алтарь башни освобождён.",

    # 7. Cultivation Realms
    "awakening_system.cultivation.realm.5": "Трансформация души (Верховный Грандмастер)",
    "awakening_system.cultivation.realm.6": "Очищение Пустоты (Шаг в Бессмертие)",
    "awakening_system.cultivation.realm.7": "Интеграция Дао (Слияние духа и тела)",
    "awakening_system.cultivation.realm.8": "Великое Бедствие (Царство Махаяны)",
    "awakening_system.cultivation.realm.9": "Истинное Вознесение в Бессмертные",

    # 8. Dokkaebi / Star Stream Quests
    "awakening_system.dokkaebi.quest_progress": "§8[§eStar Stream§8] §fПрогресс субсценария: §e%s/%s",
    "awakening_system.dokkaebi.quest_started_msg": "§b%s\n§f%s\n§6Награда: §e%s Монет",
    "awakening_system.dokkaebi.quest_started_title": "§e§l✦ [СУБСЦЕНАРИЙ STAR STREAM] ✦",
    "awakening_system.dokkaebi.quest_target_completed_msg": "§fПоговорите с Бихёном, чтобы забрать монеты и награду.",
    "awakening_system.dokkaebi.quest_target_completed_title": "§a§l✦ ЗАДАЧА ВЫПОЛНЕНА! ✦",
    "awakening_system.entity.blood_qi_totem.banner_destroyed": "§4§l[СЕКТА КРОВИ] §fЗнамя уничтожено! (Осталось: §c%s§f)",

    # 9. Artifact GUI Screen
    "awakening_system.screen.artifacts.bonus.circlet": "+% Шанс крит. и Крит. урон",
    "awakening_system.screen.artifacts.bonus.flower": "+Макс. HP",
    "awakening_system.screen.artifacts.bonus.goblet": "+% Элементальный урон",
    "awakening_system.screen.artifacts.bonus.plume": "+Базовая АТК",
    "awakening_system.screen.artifacts.bonus.sands": "+% Бонус АТК",
    "awakening_system.screen.artifacts.btn_max": "МАКС",
    "awakening_system.screen.artifacts.cost": "§7Стоимость: §6%s Монет §7или §b1x Руда усиления",
    "awakening_system.screen.artifacts.current_level": "§7Текущий уровень: §a+%s §8➔ §e+%s §8/ +20",
    "awakening_system.screen.artifacts.empty_slot": "Пустой слот (%s)",
    "awakening_system.screen.artifacts.inv_label": "§7Артефакты в инвентаре (нажмите для экипировки):",
    "awakening_system.screen.artifacts.max_reached": "§a✔ Максимальный уровень (+20) достигнут.",
    "awakening_system.screen.artifacts.no_set_bonus": "§8Нет активного бонуса комплекта (2/4)",
    "awakening_system.screen.artifacts.set_bonus_hint": "§8Экипируйте 2 или 4 предмета одного комплекта",
    "awakening_system.screen.artifacts.set_bonuses": "§6§l✦ БОНУСЫ КОМПЛЕКТОВ",
    "awakening_system.screen.artifacts.stat.atk": "§c✦ Физическая АТК: §f+%s АТК",
    "awakening_system.screen.artifacts.stat.crit_dmg": "§e✦ Крит. урон: §f+%s%%",
    "awakening_system.screen.artifacts.stat.crit_rate": "§e✦ Шанс крит.: §f+%s%%",
    "awakening_system.screen.artifacts.stat.hp": "§a✦ Макс. HP: §f+%s HP",
    "awakening_system.screen.artifacts.tip_left_click": "§8▪ §cЛКМ: §7Снять",
    "awakening_system.screen.artifacts.tip_right_click": "§8▪ §aПКМ: §7Улучшить (+1)",
    "awakening_system.screen.artifacts.title": "АРТЕФАКТЫ МОНАРХА",
    "awakening_system.screen.artifacts.total_stats": "§6§l✦ ОБЩИЕ ХАРАКТЕРИСТИКИ",
    "awakening_system.screen.artifacts.upgrade_guide": "§e💡 Улучшение (+0 до +20): §7Нажмите §a[▲+1] §7или ПКМ. Стоимость: §6Монеты §7или §bРуда§7.",
    "awakening_system.screen.artifacts.upgrade_instruction": "§e✦ ЛКМ по [▲+1] или ПКМ по слоту.",
    "awakening_system.screen.artifacts.upgrade_title": "§6§lУлучшение артефакта (+1)",

    # 10. Cultivation GUI Screen
    "awakening_system.screen.cultivation.breakthrough": "Прорыв царства",
    "awakening_system.screen.cultivation.breakthrough_sublevel": "Прорыв подуровня (%s)",
    "awakening_system.screen.cultivation.max_realm": "Вершина Истинного Бессмертия",
    "awakening_system.screen.cultivation.meditation_tip": "§e☯ Медитация: §7Сядьте в позу лотоса или присядьте в зонах высокой концентрации для культивации Ци.",
    "awakening_system.screen.cultivation.open_btn": "Открыть (%s Ци)",
    "awakening_system.screen.cultivation.opened": "ОТКРЫТО",

    # 11. Subspace GUI
    "awakening_system.screen.dim.subspace.title": "§9✦ §lПространственное подпространство",
    "awakening_system.screen.dim.subspace.desc": "§7Надёжное карманное хранилище, расширяемое сундуками.\n§bНажмите, чтобы открыть инвентарь Системы.",

    # 12. Shop Items
    "awakening_system.shop.item.constellation_nectar": "Нектар созвездия",
    "awakening_system.shop.item.create_andesite_alloy": "Андезитовый сплав (x16)",
    "awakening_system.shop.item.create_brass_casing": "Латунный корпус (x8)",
    "awakening_system.shop.item.create_precision_mechanism": "Точный механизм",
    "awakening_system.shop.item.create_steam_engine": "Паровой двигатель",
    "awakening_system.shop.item.create_track_pack": "Рельсы (x32)",
    "awakening_system.shop.item.create_train_controls": "Управление поездом",
    "awakening_system.shop.item.fable_scroll": "Древний свиток басен",
    "awakening_system.shop.item.fable_star_fragment": "Осколок звезды басни",

    # 13. Skills
    "awakening_system.skill.bookmark_skill.name": "Навык «Закладка»",
    "awakening_system.skill.bookmark_skill.description": "Культовая способность Всеведущего читателя, позволяющая копировать и сохранять навыки персонажей Star Stream.",
    "awakening_system.skill.bookmark_skill.effect": "Открывает слоты тактической адаптации для усиления личных характеристик.",

    "awakening_system.skill.max_level_privilege.name": "Привилегия максимального уровня",
    "awakening_system.skill.max_level_privilege.description": "Абсолютные знания ветерана, доступные лишь тем, кто покорил и изучил все механики игры.",
    "awakening_system.skill.max_level_privilege.effect": "Игнорирует 25% брони цели и даёт шанс нанести мгновенный смертельный удар.",

    "awakening_system.skill.nano_cellular_regeneration.name": "Нано-клеточная регенерация",
    "awakening_system.skill.nano_cellular_regeneration.description": "Наномашины в реальном времени восстанавливают повреждённые ткани клеток на молекулярном уровне.",
    "awakening_system.skill.nano_cellular_regeneration.effect": "Непрерывно восстанавливает здоровье и снимает негативные эффекты.",

    "awakening_system.skill.nano_scan_weakness.name": "Нано-сканирование уязвимостей",
    "awakening_system.skill.nano_scan_weakness.description": "Внедряет наномашины в потоки Ци противника для поиска слепых зон и уязвимых меридианов.",
    "awakening_system.skill.nano_scan_weakness.effect": "Критические удары поражают уязвимые точки, нанося +35% периодического пробивающего урона.",

    "awakening_system.skill.pain_suppression.name": "Подавление боли",
    "awakening_system.skill.pain_suppression.description": "Блокирует нервные сигналы боли и усталости несокрушимой силой воли, позволяя сражаться без колебаний.",
    "awakening_system.skill.pain_suppression.effect": "Даёт высокое сопротивление урону, иммунитет к отбрасыванию и временную регенерацию.",

    "awakening_system.skill.plum_blossom_sword_god.name": "Бог меча цветущей сливы",
    "awakening_system.skill.plum_blossom_sword_god.description": "Вершина трансцендентного искусства меча горы Хуашань. Обрушивает бесконечный шквал острых как бритва алых лепестков.",
    "awakening_system.skill.plum_blossom_sword_god.effect": "Колоссальный урон по площади из 36 одновременных рубящих ударов и молниеносная мобильность.",

    "awakening_system.skill.predator_sense.name": "Чутьё хищника",
    "awakening_system.skill.predator_sense.description": "Обостряет инстинкты охотника, выслеживая жизненные потоки жертвы и предугадывая действия врага.",
    "awakening_system.skill.predator_sense.effect": "Подсвечивает ближайших существ в темноте и повышает скорость передвижения при внезапной атаке.",

    "awakening_system.skill.probability_distortion.name": "Искажение вероятности",
    "awakening_system.skill.probability_distortion.description": "Направляет и искажает правдоподобие Star Stream, склоняя правила сценария в вашу пользу.",
    "awakening_system.skill.probability_distortion.effect": "Снижает получаемый критический урон и даёт шанс абсолютного уклонения.",

    "awakening_system.skill.shonen_conquerors_haki.name": "Королевская воля",
    "awakening_system.skill.shonen_conquerors_haki.description": "Проявление высшей силы воли из One Piece. Подавляет и сокрушает дух противников без физического контакта.",
    "awakening_system.skill.shonen_conquerors_haki.effect": "Ударная волна в 20 блоков: мгновенно оглушает обычных врагов и сокрушает защиту боссов духовным уроном.",

    "awakening_system.skill.shonen_getsuga_tensho.name": "Гецуга Теншо",
    "awakening_system.skill.shonen_getsuga_tensho.description": "Техника духовного рассечения из Блич. Концентрирует духовное давление на лезвии клинка и выпускает разрушительный серп тьмы.",
    "awakening_system.skill.shonen_getsuga_tensho.effect": "Выпускает пробивающий полумесяц тёмно-алого духовного пламени на 26 блоков, нанося сокрушительный рубящий урон и поджигая врагов.",

    "awakening_system.skill.shonen_infinite_void.name": "Расширение территории: Бесконечная пустота",
    "awakening_system.skill.shonen_infinite_void.description": "Высшая техника Сатору Годжо. Разворачивает внутреннее пространство Бесконечности, перегружая разум целей бесконечным объёмом информации.",
    "awakening_system.skill.shonen_infinite_void.effect": "Создаёт купол территории на 24 блока: полностью парализует и обезоруживает врагов на 9 сек., даруя пользователю Скорость IV, Силу III и Регенерацию.",

    "awakening_system.skill.shonen_rasenshuriken.name": "Стихия ветра: Расенсюрикен",
    "awakening_system.skill.shonen_rasenshuriken.description": "Абсолютная техника стихии ветра от Наруто. Соединяет форму чакры и природу ветра в вихревые лезвия, рассекающие на клеточном уровне.",
    "awakening_system.skill.shonen_rasenshuriken.effect": "Мнёт и бросает ветряной сюрикен на 16 блоков вперёд, который взрывается микровихрем, подбрасывая и разрывая всех пойманных врагов.",

    "awakening_system.skill.shonen_zoltraak.name": "Золтраак (Магия уничтожения)",
    "awakening_system.skill.shonen_zoltraak.description": "Магия уничтожения демонов, усовершенствованная Фрирен. Проецирует геометрический магический круг и выпускает концентрированный пробивающий луч.",
    "awakening_system.skill.shonen_zoltraak.effect": "Проецирует вертикальный рунический круг и стреляет лучом на 32 блока, пробивающим любую магическую и физическую броню.",

    "awakening_system.skill.sword_intent.name": "Намерение меча",
    "awakening_system.skill.sword_intent.description": "Концентрирует чистую Ци горы Хуашань вдоль кромки клинка, создавая нематериальные рассечения, преодолевающие твёрдость стали.",
    "awakening_system.skill.sword_intent.effect": "Увеличивает рубящий урон на 40% и пробивает защиту врагов.",

    "awakening_system.skill_tree.branch.fighting_spirit": "Боевой дух",
    "awakening_system.skill_tree.branch.solidity": "Несокрушимость",
    "awakening_system.skill_tree.branch.ultimatum": "Ультиматум",

    # 14. Titles & Descriptions
    "awakening_system.title.create_aeronaut_pioneer": "Первопроходец воздухоплавания",
    "awakening_system.title.description.create_aeronaut_pioneer": "Отважный авиатор, покоривший небеса на своём самом первом дирижабле.",
    "awakening_system.title.create_firmament_pilot": "Пилот небесного свода",
    "awakening_system.title.description.create_firmament_pilot": "Первоклассный пилот, способный вести судно сквозь самые свирепые бури.",
    "awakening_system.title.create_reality_engineer": "Инженер реальности",
    "awakening_system.title.description.create_reality_engineer": "Мастер-созидатель, повелевающий кинематикой, энергией вращения и механизмами мира.",
    "awakening_system.title.create_sky_battleship_captain": "Капитан небесного линкора",
    "awakening_system.title.description.create_sky_battleship_captain": "Верховный главнокомандующий летающих крепостей и небесных боевых кораблей.",
    "awakening_system.title.create_steam_architect": "Паровой архитектор",
    "awakening_system.title.description.create_steam_architect": "Мастер-строитель паровых котлов высокого давления и сложных тепловых двигателей.",
    "awakening_system.title.create_train_master": "Мастер железных дорог",
    "awakening_system.title.description.create_train_master": "Строитель обширных железнодорожных путей и машинист могучих паровозов.",

    # 15. Wu Xing & Blocks / Items
    "awakening_system.wuxing.already_open": "§a[У-син] §fКруг Пяти Стихий уже в гармонии, священные врата открыты.",
    "block.awakening_system.ancient_dragon_egg_celestial": "Яйцо древнего небесного дракона",
    "block.awakening_system.ancient_dragon_egg_lightning": "Яйцо древнего дракона молнии",
    "block.awakening_system.gacha_machine": "Гачапон Тейвата",
    "block.awakening_system.slot_machine": "Небесный игровой автомат",
    "entity.awakening_system.paimon": "Паймон",
    "item.awakening_system.absolution": "Отпущение грехов (5★)",
    "item.awakening_system.acquaint_fate": "Судьбоносные встречи",
    "item.awakening_system.ancient_dragon_egg_celestial": "Яйцо древнего небесного дракона",
    "item.awakening_system.ancient_dragon_egg_lightning": "Яйцо древнего дракона молнии",
    "item.awakening_system.command_spell": "Командные заклинания (Fate/Grand Order 5★)",
    "item.awakening_system.crimson_moons_semblance": "Очертания алой луны (5★)",
    "item.awakening_system.engulfing_lightning": "Сияющая жатва (5★)",
    "item.awakening_system.fleuve_cendre_ferryman": "Перевозчик Флев Сандр (4★)",
    "item.awakening_system.freedom_sworn": "Клятва свободы (5★)",
    "item.awakening_system.gacha_capsule": "Капсула гачапона",
    "item.awakening_system.gacha_machine": "Гачапон Тейвата",
    "item.awakening_system.herta_hammer": "Молот Герты (5★)",
    "item.awakening_system.intertwined_fate": "Переплетающиеся судьбы",
    "item.awakening_system.key_of_khaj_nisut": "Ключ Хадж-нисут (5★)",
    "item.awakening_system.la_doncella": "Дева, что созерцала гибель роз с пепельного трона, пока сад угасал под багровой луной среди чёрных лепестков вечного цветка, чей аромат витал над увядшими цветами, укрывшими корни безымянной розы, чьи шипы оплели сердце девы, превратив её платье в ложе из мёртвых лепестков, пока ночь медленно проникала сквозь ветви сада и пепел опускался на каждый цветок, ещё хранивший память о своём последнем аромате, пока последняя роза не раскрыла свои лепестки над пустошью и не отдала свою красоту тишине, оставив лишь свои шипы, своё благоухание и незавершённый сон смерти, пока этот сон не обратился воспоминанием о весне, которая никогда не вернётся, хранимым долгие годы внутри кристалла, сберёгшего последнюю каплю аромата девы, пока зима не разбила его и не выпустила её благоухание над землёй, пробуждая корни, спавшие под пеплом, и направляя их к тени, где всё ещё жила память о вечном цветке, там, где дева оставила своё имя, прежде чем раствориться в садах тишины",
    "item.awakening_system.light_cone_milky_way": "Световой конус: Ночь на Млечном Пути (5★)",
    "item.awakening_system.mana_fruit": "Плод маны",
    "item.awakening_system.mistsplitter_reforged": "Рассекающий туман (5★)",
    "item.awakening_system.music_disc_chasing_stars.desc": "HOYO-MiX — Chasing Stars Together (Праздник морских фонарей)",
    "item.awakening_system.music_disc_day_of_hope.desc": "HOYO-MiX — A Day of Hope (Star Rail)",
    "item.awakening_system.music_disc_divine_fox.desc": "HOYO-MiX — Blessing of the Divine Fox (Яэ Мико)",
    "item.awakening_system.music_disc_qingce_night.desc": "HOYO-MiX — Peaceful Hike (Ночь в деревне Цинцэ)",
    "item.awakening_system.music_disc_salted_moon.desc": "HOYO-MiX — Salted Moon (Ли Юэ)",
    "item.awakening_system.music_disc_space_walk.desc": "HOYO-MiX — Space Walk (HSR)",
    "item.awakening_system.music_disc_take_the_journey.desc": "HOYO-MiX — Take The Journey (HSR)",
    "item.awakening_system.music_disc_waters_poem.desc": "HOYO-MiX — Poem of All Waters (Фонтейн)",
    "item.awakening_system.music_disc_windrise_night.desc": "HOYO-MiX — Lone Sojourner (Ночь в Долине Ветров)",
    "item.awakening_system.paimon_spawn_egg": "Яйцо призыва Паймон",
    "item.awakening_system.primogem": "Камень Истока",
    "item.awakening_system.primordial_jade_cutter": "Драгоценный омут (5★)",
    "item.awakening_system.primordial_jade_winged_spear": "Нефритовый коршун (5★)",
    "item.awakening_system.qingming_sword": "Меч Цинмин (5★)",
    "item.awakening_system.redhorn_stonethresher": "Краснорогий камнеруб (5★)",
    "item.awakening_system.shiyuan_sword": "Меч Шиюань (4★)",
    "item.awakening_system.shunguang_sword": "Меч Шуньгуан (5★)",
    "item.awakening_system.slot_machine": "Небесный игровой автомат",
    "item.awakening_system.staff_of_homa": "Посох Хомы (5★)",
    "item.awakening_system.sunsettia": "Закатник",
    "item.awakening_system.tailless": "Бесхвостый (5★)",
    "item.awakening_system.the_catch": "«Улов» (4★)",
    "item.awakening_system.tribulation_shield_pill": "Пилюля защиты от небесной кары",
    "item.awakening_system.tribulation_shield_pill.active_desc": "Урон от молнии бедствия снижен на 70% (%s).",
    "item.awakening_system.tribulation_shield_pill.consumed": "§6§l☯ [ПИЛЮЛЯ БЕДСТВИЯ] §aВы приняли Пилюлю защиты! Поглощает 70% урона молнии кары и спасает от смерти.",
    "item.awakening_system.tribulation_shield_pill.desc1": "§6§l✦ Алхимическое сокровище от Небесной Кары",
    "item.awakening_system.tribulation_shield_pill.desc2": "§7Дарует защитную ауру Дао на §b3 минуты§7.",
    "item.awakening_system.tribulation_shield_pill.desc3": "§e✦ Поглощает 70% урона ударов небесного бедствия и предотвращает гибель во время трибуляции.",
    "item.awakening_system.tribulation_shield_pill.title": "Небесный щит активен",
    "item.awakening_system.verdict": "Вердикт (5★)",
    "item.awakening_system.vision_anemo": "Глаз Бога: Анемо",
    "item.awakening_system.vision_cryo": "Глаз Бога: Крио",
    "item.awakening_system.vision_electro": "Глаз Бога: Электро",
    "item.awakening_system.vision_pyro": "Глаз Бога: Пиро",
    "item.awakening_system.vortex_vanquisher": "Покоритель вихря (5★)",
    "item.awakening_system.wolfs_gravestone": "Волчья погибель (5★)",

    # 16. Jukebox Songs
    "jukebox_song.awakening_system.chasing_stars": "HOYO-MiX — Chasing Stars Together",
    "jukebox_song.awakening_system.day_of_hope": "HOYO-MiX — A Day of Hope",
    "jukebox_song.awakening_system.divine_fox": "HOYO-MiX — Blessing of the Divine Fox",
    "jukebox_song.awakening_system.qingce_night": "HOYO-MiX — Qingce Night",
    "jukebox_song.awakening_system.salted_moon": "HOYO-MiX — Salted Moon",
    "jukebox_song.awakening_system.space_walk": "HOYO-MiX — Space Walk",
    "jukebox_song.awakening_system.take_the_journey": "HOYO-MiX — Take The Journey",
    "jukebox_song.awakening_system.waters_poem": "HOYO-MiX — Poem of All Waters",
    "jukebox_song.awakening_system.windrise_night": "HOYO-MiX — Windrise Night",

    # 17. Dao Skills / Messages
    "key.awakening_system.dao_skill": "Боевая техника Дао (Мурим)",
    "message.awakening_system.dao_skill_cooldown": "§c[Дао] Техника восстанавливается. Подождите %s с.",
    "message.awakening_system.dao_skill_domain": "§e✦ [ОБЛАСТЬ ЗОЛОТОГО ЯДРА] §fСвященный барьер развёрнут: подавляет врагов и наполняет вашу Ци!",
    "message.awakening_system.dao_skill_flash": "§5⚡ [ШАГ ПУСТОТЫ] §fМгновенный пространственный рывок с детонирующей грозовой иллюзией!",
    "message.awakening_system.dao_skill_nascent": "§d✨ [АВАТАР ЗАРОЖДАЮЩЕЙСЯ ДУШИ] §fАстральная проекция пробуждена: полёт и божественная защита от гибели!",
    "message.awakening_system.dao_skill_palm": "§6☯ [ЛАДОНЬ ПУСТОТЫ ДЕВЯТИ НЕБЕС] §fСжатие пространства и разрушительная ударная волна ладони!",
    "message.awakening_system.dao_skill_realm_req": "§c[Дао] Недостаточный уровень культивации. Требуется: %s.",
    "message.awakening_system.dao_skill_swords": "§b⚔ [ЛЕТАЮЩИЕ ДУХОВНЫЕ МЕЧИ] §fКлинки Ци настигают и пронзают ряды врагов!",

    # 18. Subtitles
    "subtitles.awakening_system.casino_bonus_prize": "Бонусный приз",
    "subtitles.awakening_system.casino_button": "Кнопка автомата нажата",
    "subtitles.awakening_system.casino_insert_coin": "Монета Моры вставлена",
    "subtitles.awakening_system.casino_jackpot": "Небесный ДЖЕКПОТ!",
    "subtitles.awakening_system.casino_lose": "Нет выигрыша",
    "subtitles.awakening_system.casino_reels_spinning": "Барабаны автомата вращаются",
    "subtitles.awakening_system.casino_win": "Выигрыш в автомате",
    "subtitles.awakening_system.dungeon_break": "ПРОРЫВ ПОДЗЕМЕЛЬЯ: Пространственный барьер рушится!",
    "subtitles.awakening_system.dungeon_gate_open": "Врата подземелья открываются",
    "subtitles.awakening_system.gacha_common_prize": "Обычная награда",
    "subtitles.awakening_system.gacha_legendary_prize": "Легендарная награда!",
    "subtitles.awakening_system.gacha_open_prize": "Капсула гачапона открыта",
    "subtitles.awakening_system.gacha_rare_prize": "Редкая награда",
    "subtitles.awakening_system.gacha_ultrarare_prize": "Ультраредкая награда",
    "subtitles.awakening_system.gacha_uncommon_prize": "Необычная награда",
    "subtitles.awakening_system.gate_ambient": "Врата подземелья гудят от маны",
    "subtitles.awakening_system.gate_travel": "Переход через Врата подземелья",
}

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    with open('src/main/resources/assets/awakening_system/lang/en_us.json', 'r', encoding='utf-8') as f:
        en = json.load(f)

    with open('src/main/resources/assets/awakening_system/lang/ru_ru.json', 'r', encoding='utf-8') as f:
        ru = json.load(f)

    # Apply all translations
    for k, v in RU_TRANSLATIONS.items():
        ru[k] = v

    # Check for any remaining missing keys from en_us
    missing = set(en.keys()) - set(ru.keys())
    if missing:
        print(f"Warning: still missing {len(missing)} keys in ru_ru:")
        for m in sorted(missing)[:10]:
            print("  ", m)
            # Default to en value if somehow any remained
            ru[m] = en[m]

    # Save sorted ru_ru.json
    with open('src/main/resources/assets/awakening_system/lang/ru_ru.json', 'w', encoding='utf-8') as f:
        json.dump(ru, f, ensure_ascii=False, indent=2)

    print(f"Successfully updated ru_ru.json! Total keys: {len(ru)}")

if __name__ == '__main__':
    main()
