package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public enum NpcProfile {
    SUNG_JINWOO(
            "sung_jinwoo",
            "Sung Jin-Woo",
            "Shadow Monarch",
            "Solo Leveling",
            "§5§l",
            List.of(
                    "Arise... Those who fall under my command will serve the shadows for eternity.",
                    "Are you pushing yourself in your dungeons? The System does not reward weakness.",
                    "My army never sleeps. Stay calm and keep leveling up.",
                    "I only kneel before those who protect the ones who cannot fight."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.sung_jinwoo.blessing_title", "awakening_system.npc.sung_jinwoo.blessing_desc", 0xFF8A2BE2);
        }
    },

    CHA_HAEIN(
            "cha_haein",
            "Cha Hae-In",
            "S-Rank Hunter",
            "Solo Leveling",
            "§e§l",
            List.of(
                    "Your scent isn't like the other hunters'... Are you connected to the System?",
                    "The sword stance requires absolute focus. Never hesitate at the decisive moment.",
                    "Don't neglect your physical training; agility is everything when a Red Gate opens.",
                    "Whenever you draw your blade, remember who you are fighting for."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.cha_haein.blessing_title", "awakening_system.npc.cha_haein.blessing_desc", 0xFFFFD700);
        }
    },

    KIM_DOKJA(
            "kim_dokja",
            "Kim Dokja",
            "Demon King of Salvation",
            "Omniscient Reader's Viewpoint",
            "§f§l",
            List.of(
                    "Looking for a guide to the next scenario? It's all written if you read between the lines.",
                    "Don't blindly trust Star Stream Constellations; they charge heavily for every miracle.",
                    "This world is not a tragedy if we fight to the very last chapter.",
                    "Take some System coins. In this scenario, information and resources are everything."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 2, false, true));
            var data = SystemEvents.data(player);
            if (data != null) {
                data.addCoins(250);
                AwakeningSystemMod.sync(player);
            }
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.2D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.kim_dokja.blessing_title", "awakening_system.npc.kim_dokja.blessing_desc", 0xFF70E8FF);
        }
    },

    YOO_JOONGHYUK(
            "yoo_joonghyuk",
            "Yoo Joonghyuk",
            "The Supreme Regressor",
            "Omniscient Reader's Viewpoint",
            "§8§l",
            List.of(
                    "If you are weak, you will die before scenario 10. Stay out of my path.",
                    "I have seen this world perish hundreds of times. I will not repeat past mistakes.",
                    "Think your resolve is enough? Prove it with your blade on the battlefield.",
                    "Do not die in some random dungeon. A pathetic death deserves no remembrance."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.yoo_joonghyuk.blessing_title", "awakening_system.npc.yoo_joonghyuk.blessing_desc", 0xFFFF3333);
        }
    },

    LLOYD_FRONTERA(
            "lloyd_frontera",
            "Lloyd Frontera",
            "The Supreme Engineer",
            "The Greatest Estate Developer",
            "§6§l",
            List.of(
                    "Money moves the world, but cement, water, and shovels build it!",
                    "Warm ondol in winter, clean aqueducts in summer! That's real quality of life!",
                    "Got System coins? I've got blueprints, contracts, and top-tier materials!",
                    "Javier, invoice this adventurer! Every square meter of land has its price!"
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 12000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 12000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 100, 1, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.lloyd_frontera.blessing_title", "awakening_system.npc.lloyd_frontera.blessing_desc", 0xFFFFD866);
        }
    },

    JAVIER_ASRAHAN(
            "javier_asrahan",
            "Javier Asrahan",
            "The Holy Knight",
            "The Greatest Estate Developer",
            "§b§l",
            List.of(
                    "Young Master Lloyd looks greedy, but his projects always save people in the end.",
                    "The Sword of Asrahan protects the innocent and strikes down all evil.",
                    "Keep your guard up. A true warrior never lets down their vigilance in unknown lands.",
                    "If you need help with monsters threatening your lands, call upon my sword."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 25, 0.4D, 0.7D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.javier_asrahan.blessing_title", "awakening_system.npc.javier_asrahan.blessing_desc", 0xFF55FFFF);
        }
    },

    CHEON_YEOWOON(
            "cheon_yeowoon",
            "Cheon Yeo-woon",
            "Lord of the Demonic Cult",
            "Nano Machine",
            "§4§l",
            List.of(
                    "Nano, scan this hunter's meridians... Biological and energetic structure acceptable.",
                    "The Heavenly Demonic Cult bows its head to neither god nor emperor.",
                    "Absolute power needs no excuses; it is forged with millimeter precision and ruthless steel.",
                    "Cut down your enemy before their first thought of betrayal can form."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.7D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.cheon_yeowoon.blessing_title", "awakening_system.npc.cheon_yeowoon.blessing_desc", 0xFFFF2222);
        }
    },

    ARTHUR_LEYWIN(
            "arthur_leywin",
            "Arthur Leywin",
            "Ruler of Aether",
            "The Beginning After The End",
            "§9§l",
            List.of(
                    "Mana fills the body, but aether governs the very dimensions of space and reality.",
                    "I fight not for titles or vain glory; I fight to ensure my loved ones have a tomorrow.",
                    "Purify your core. The clarity of your intent defines the strength of your awakening.",
                    "Learn to flow with your surroundings; brute force without control only breeds ruin."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.arthur_leywin.blessing_title", "awakening_system.npc.arthur_leywin.blessing_desc", 0xFF3388FF);
        }
    },

    CHA_YEONWOO(
            "cha_yeonwoo",
            "Cha Yeon-woo",
            "The Hunter Cain",
            "Second Life Ranker",
            "§c§l",
            List.of(
                    "My brother left me a pocket watch and a diary. Every floor of this Tower will be conquered and purged.",
                    "The Black Dragon's wings devour false gods of the heavens.",
                    "If you scale the Tower of Desires, prepare never to look back.",
                    "Weakness is a sin in a realm governed by Constellations."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.7D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.cha_yeonwoo.blessing_title", "awakening_system.npc.cha_yeonwoo.blessing_desc", 0xFFCC0000);
        }
    },

    CHUNG_MYUNG(
            "chung_myung",
            "Chung Myung",
            "Divine Dragon of Mount Hua",
            "Return of the Mount Hua Sect",
            "§a§l",
            List.of(
                    "Where's the plum blossom wine? And more importantly, where's Shaolin's money?!",
                    "At Mount Hua we don't surrender; we strike twice as hard until the enemy gets it!",
                    "Think 10,000 thrusts a day is hard? Back in my day we did 100,000 before breakfast!",
                    "Draw your sword. If you wield a blade, cut through doubts like plum blossoms in deep winter."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.15D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.chung_myung.blessing_title", "awakening_system.npc.chung_myung.blessing_desc", 0xFF55FF55);
        }
    },

    RYU_HANBIN(
            "ryu_hanbin",
            "Ryu Hanbin",
            "The Valter Sword King",
            "Survival Story of a Sword King",
            "§c§l",
            List.of(
                    "I spent over twenty years trapped in the Rocky Mountain because of a glitched guideline... Nothing scares me anymore.",
                    "My level? Still says 'Level 5', but my strength can split a mountain in two.",
                    "Don't overcomplicate things. If a beast attacks you, grab a bigger sword and smash it.",
                    "Sometimes I miss fried chicken and ramen from Earth... This fantasy world is pure chaos."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 3, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5D, player.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.ryu_hanbin.blessing_title", "awakening_system.npc.ryu_hanbin.blessing_desc", 0xFFFF3333);
        }
    },

    TWENTY_FIFTH_BAAM(
            "twenty_fifth_baam",
            "Vigesimoquinto Baam",
            "The Tower's Irregular",
            "Tower of God",
            "§b§l",
            List.of(
                    "What are you seeking at the top of the Tower? Wealth, glory, or the ones you cherish?",
                    "Shinsoo isn't a weapon to possess; it is the world's breath that allows us to live together.",
                    "I won't let anyone hurt my friends again. If I must break the Tower's rules, I will.",
                    "The Thorn beats within me... A power that defies the very fate of the Great Families."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.15D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.twenty_fifth_baam.blessing_title", "awakening_system.npc.twenty_fifth_baam.blessing_desc", 0xFF00FFFF);
        }
    },

    ELENA_RECEPTIONIST(
            "elena_receptionist",
            "Elena",
            "Guild Receptionist",
            "Adventurers' Guild",
            "§6§l",
            List.of(
                    "Welcome to the Adventurers Guild! Check the Bounty Board on the wall for hunting contracts.",
                    "Seeking glory and rightful rewards? Complete contracts to rank up all the way to mythical Rank S.",
                    "Don't forget to upkeep your gear at the upstairs smithing tables before heading into the wild.",
                    "Every beast slain in the guild's name earns you Adventurer Medals and realm-wide prestige."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.elena_receptionist.blessing_title", "awakening_system.npc.elena_receptionist.blessing_desc", 0xFFFFD700);
        }
    },

    BLACK_MARKET_DEALER(
            "black_market_dealer",
            "Valerius the Silent",
            "Black Market Dealer",
            "Clandestine Underworld",
            "§5§l",
            List.of(
                    "Black chips leave no trace... and Mora asks no questions.",
                    "Looking for clandestine machinery or a pool duel where stakes have no ceiling?",
                    "What happens in this crypt stays hidden in underworld shadows.",
                    "Your eyes saw nothing, your hands only counted chips."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 3600, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.black_market_dealer.blessing_title", "awakening_system.npc.black_market_dealer.blessing_desc", 0xFFA855F7);
        }
    },

    CASINO_RECEPTIONIST(
            "casino_receptionist",
            "Madame Valerie",
            "Receptionist & VIP Hostess",
            "Gran Casino Real",
            "§6§l",
            List.of(
                    "Welcome to the Grand Royal Casino of Teyvat! Here fortune and glamour converge under golden lights.",
                    "Looking to test your luck at the high-stakes tables? Get your VIP Pass to enter the exclusive high-roller lounge.",
                    "At our counter you can exchange Mora for Official Chips and request your VIP accreditation.",
                    "Remember that in VIP Blackjack and VIP Roulette, only the most coveted stakes in the entire realm are wagered."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 12000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 12000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.casino_receptionist.blessing_title", "awakening_system.npc.casino_receptionist.blessing_desc", 0xFFFFD700);
        }
    },

    RAIDEN_SHOGUN(
            "raiden_shogun",
            "Raiden Shogun",
            "Electro Archon",
            "Genshin Impact",
            "§5§l",
            List.of(
                    "Thunder roars and illusions fade. Only Eternity remains unchanging.",
                    "Do you seek to defy heavenly decrees? Prove the strength of your will.",
                    "Do not fear the lightning; fear hesitation at the moment of supreme judgment.",
                    "The Engulfing Lightning shall guide your path through the storm."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.raiden_shogun.blessing_title", "awakening_system.npc.raiden_shogun.blessing_desc", 0xFF9933FF);
        }
    },

    ZHONGLI(
            "zhongli",
            "Zhongli",
            "Rex Lapis • Geo Archon",
            "Genshin Impact",
            "§6§l",
            List.of(
                    "Contracts are carved in stone and gold. Those who break their promises shall suffer the Wrath of the Rock.",
                    "Osmanthus wine tastes the same as I remember... but where are those who share the memory?",
                    "The steadfastness of a shield lies not in the ore, but in the unshakable calm of the spirit.",
                    "Walk Teyvat with steady footsteps; history never forgets those who forge their own destiny."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 8000, 4, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.zhongli.blessing_title", "awakening_system.npc.zhongli.blessing_desc", 0xFFFFD700);
        }
    },

    HUTAO(
            "hu_tao",
            "Hu Tao",
            "Wangsheng Funeral Parlor Director",
            "Genshin Impact",
            "§c§l",
            List.of(
                    "Aiya! Customer spotted! Need funeral coupons? Buy two, get the third free!",
                    "There's a fine line between life and death... make sure to enjoy every single step!",
                    "Spirit butterflies dance in the night; the Staff of Homa purifies restless souls.",
                    "Don't fret about danger! If you fall in battle, Wangsheng Funeral Parlor will take good care of you, haha!"
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.7D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.hu_tao.blessing_title", "awakening_system.npc.hu_tao.blessing_desc", 0xFFFF4500);
        }
    },

    GANYU(
            "ganyu",
            "Ganyu",
            "General Secretary of the Qixing",
            "Genshin Impact",
            "§b§l",
            List.of(
                    "Dawn dew soothes the heart... We must not delay Liyue's pending duties.",
                    "Amos' Bow whispers with the icy wind; a clean, true shot dispels any threat.",
                    "Have you rested enough? Overexertion clouds a hunter's precision.",
                    "For the peace of mortals and the thousand-year pact of the Adepti."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.ganyu.blessing_title", "awakening_system.npc.ganyu.blessing_desc", 0xFF80D8FF);
        }
    },

    NEUVILLETTE(
            "neuvillette",
            "Neuvillette",
            "Iudex of Fontaine",
            "Genshin Impact",
            "§9§l",
            List.of(
                    "The tides of Fontaine cleanse every stain of iniquity. May justice guide your judgments.",
                    "Water holds memories, and the heavens weep when the scales of truth are shattered.",
                    "Do you hear the rain? It is the dragon's judgment purifying the land.",
                    "Maintain composure in the face of chaos. Primordial order shall prevail."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 12000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 6000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.neuvillette.blessing_title", "awakening_system.npc.neuvillette.blessing_desc", 0xFF00B0FF);
        }
    },

    MARCH_7TH(
            "march_7th",
            "Siete de Marzo",
            "Astral Express Archer",
            "Honkai: Star Rail",
            "§d§l",
            List.of(
                    "Check out that pose! Smile for the Astral Express camera before battle starts!",
                    "A six-phased ice arrow for every monster trying to ruin our journey!",
                    "Traveling among the stars is thrilling, but you always have to be ready for adventure!",
                    "Don't fall behind! The next world is waiting for us!"
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.march_7th.blessing_title", "awakening_system.npc.march_7th.blessing_desc", 0xFFFF80DF);
        }
    },

    ACHERON(
            "acheron",
            "Acheron",
            "Emanator of Nihility",
            "Honkai: Star Rail",
            "§5§l",
            List.of(
                    "I have walked along countless crimson rivers... Past and future fade into nothingness.",
                    "Rain never ceases along this path. Draw your blade only when prepared to shoulder the void.",
                    "Dreams and waking life intertwine. Remember your name before crossing the horizon.",
                    "Fear not the darkness; even in deepest nihility, a fleeting spark persists."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.acheron.blessing_title", "awakening_system.npc.acheron.blessing_desc", 0xFF8A2BE2);
        }
    },

    HOSHIMI_MIYABI(
            "hoshimi_miyabi",
            "Hoshimi Miyabi",
            "Chief of Section 6",
            "Zenless Zone Zero",
            "§3§l",
            List.of(
                    "Section 6 in position. Every etheric distortion within the Hollows shall be eradicated.",
                    "The Shunguang Blade does not hesitate. Cutting speed surpasses thought itself.",
                    "Maintain composure before ethereals. Discipline is an operative's sharpest weapon.",
                    "Mission received. Proceeding with immediate target neutralization."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 2, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.hoshimi_miyabi.blessing_title", "awakening_system.npc.hoshimi_miyabi.blessing_desc", 0xFF00CED1);
        }
    },

    ELLEN_JOE(
            "ellen_joe",
            "Ellen Joe",
            "Cleaner of Victoria Housekeeping",
            "Zenless Zone Zero",
            "§b§l",
            List.of(
                    "Ugh... More overtime work? I just want my coffee candies and a nap.",
                    "If you get between me and the exit, I'll slice you to pieces before my shift ends.",
                    "Victoria Housekeeping leaves no loose ends or stains on the carpet.",
                    "Let's wrap this up fast... I hate it when work interrupts my break time."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 120, 1, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.ellen_joe.blessing_title", "awakening_system.npc.ellen_joe.blessing_desc", 0xFF1E90FF);
        }
    },

    DILUC(
            "diluc",
            "Diluc Ragnvindr",
            "Tycoon of Dawn",
            "Genshin Impact",
            "§c§l",
            List.of(
                    "Welcome to Dawn Winery. A glass of grape juice before you continue on your way?",
                    "The Knights of Favonius are far too sluggish. Certain threats can only be quelled from the shadows.",
                    "The fire of dawn never flickers. Never let your guard down when night falls over Teyvat.",
                    "If you seek intel or respite in these lands, this winery's doors remain open."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.2D, player.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.02D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.diluc.blessing_title", "awakening_system.npc.diluc.blessing_desc", 0xFFFF4500);
        }
    };

    private final String id;
    private final String displayName;
    private final String title;
    private final String series;
    private final String titleColor;
    private final List<String> dialogues;

    NpcProfile(String id, String displayName, String title, String series, String titleColor, List<String> dialogues) {
        this.id = id;
        this.displayName = displayName;
        this.title = title;
        this.series = series;
        this.titleColor = titleColor;
        this.dialogues = dialogues;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String title() { return title; }
    public String series() { return series; }
    public String titleColor() { return titleColor; }
    public Component getTitleComponent() {
        return Component.translatable("awakening_system.npc." + id + ".title");
    }

    public Component getDisplayNameComponent() {
        return Component.translatable("entity.awakening_system." + id);
    }

    public Component getDialogueComponent(int index) {
        return Component.translatable("awakening_system.npc." + id + ".dialogue." + index);
    }

    public List<String> dialogues() { return dialogues; }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/npc/" + id + ".png");
    }

    public String getRandomDialogue() {
        if (dialogues.isEmpty()) return "Greetings, hunter.";
        int idx = (int) (Math.random() * dialogues.size());
        return dialogues.get(idx);
    }

    public abstract void applyBlessing(ServerPlayer player);

    public ItemStack getSignatureWeapon() {
        return switch (this) {
            case SUNG_JINWOO -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : ItemStack.EMPTY;
            case CHA_HAEIN -> SystemItems.RADIANT_LIGHT_SWORD.isBound() ? new ItemStack(SystemItems.RADIANT_LIGHT_SWORD.get()) : ItemStack.EMPTY;
            case JAVIER_ASRAHAN -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : ItemStack.EMPTY;
            case CHEON_YEOWOON -> SystemItems.SKY_DEMON_SWORD.isBound() ? new ItemStack(SystemItems.SKY_DEMON_SWORD.get()) : ItemStack.EMPTY;
            case CHA_YEONWOO -> SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()) : ItemStack.EMPTY;
            case CHUNG_MYUNG -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : ItemStack.EMPTY;
            case RYU_HANBIN -> SystemItems.GIGANTIC_GREATSWORD.isBound() ? new ItemStack(SystemItems.GIGANTIC_GREATSWORD.get()) : ItemStack.EMPTY;
            case TWENTY_FIFTH_BAAM -> SystemItems.TRANSCENDENT_THORN.isBound() ? new ItemStack(SystemItems.TRANSCENDENT_THORN.get()) : ItemStack.EMPTY;
            case RAIDEN_SHOGUN -> SystemItems.ENGULFING_LIGHTNING.isBound() ? new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()) : ItemStack.EMPTY;
            case ZHONGLI -> SystemItems.VORTEX_VANQUISHER.isBound() ? new ItemStack(SystemItems.VORTEX_VANQUISHER.get()) : ItemStack.EMPTY;
            case HUTAO -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : ItemStack.EMPTY;
            case GANYU -> SystemItems.AMOS_BOW.isBound() ? new ItemStack(SystemItems.AMOS_BOW.get()) : ItemStack.EMPTY;
            case MARCH_7TH -> SystemItems.ASTRAL_ARCHERS_BOW.isBound() ? new ItemStack(SystemItems.ASTRAL_ARCHERS_BOW.get()) : ItemStack.EMPTY;
            case HOSHIMI_MIYABI -> SystemItems.SHUNGUANG_SWORD.isBound() ? new ItemStack(SystemItems.SHUNGUANG_SWORD.get()) : ItemStack.EMPTY;
            case DILUC -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : ItemStack.EMPTY;
            case BLACK_MARKET_DEALER -> SystemItems.CASINO_CHIP_BLACK.isBound() ? new ItemStack(SystemItems.CASINO_CHIP_BLACK.get()) : ItemStack.EMPTY;
            case CASINO_RECEPTIONIST -> SystemItems.CASINO_VIP_PASS.isBound() ? new ItemStack(SystemItems.CASINO_VIP_PASS.get()) : ItemStack.EMPTY;
            default -> ItemStack.EMPTY;
        };
    }

    public net.minecraft.sounds.SoundEvent getVoiceSound() {
        return switch (this) {
            case SUNG_JINWOO -> com.soybe.awakeningsystem.ModSounds.COMPANION_SUNG_JINWOO_AMBIENT.get();
            case CHA_HAEIN -> com.soybe.awakeningsystem.ModSounds.COMPANION_CHA_HAEIN_AMBIENT.get();
            case KIM_DOKJA -> com.soybe.awakeningsystem.ModSounds.COMPANION_KAZUHA_AMBIENT.get();
            case YOO_JOONGHYUK -> com.soybe.awakeningsystem.ModSounds.COMPANION_JIYAN_AMBIENT.get();
            case LLOYD_FRONTERA -> com.soybe.awakeningsystem.ModSounds.COMPANION_YUJI_ITADORI_AMBIENT.get();
            case JAVIER_ASRAHAN -> com.soybe.awakeningsystem.ModSounds.COMPANION_ARCHER_AMBIENT.get();
            case CHEON_YEOWOON -> com.soybe.awakeningsystem.ModSounds.COMPANION_XIAO_AMBIENT.get();
            case ARTHUR_LEYWIN -> com.soybe.awakeningsystem.ModSounds.COMPANION_TANJIRO_AMBIENT.get();
            case CHA_YEONWOO -> com.soybe.awakeningsystem.ModSounds.COMPANION_IGRIS_AMBIENT.get();
            case CHUNG_MYUNG -> com.soybe.awakeningsystem.ModSounds.COMPANION_RENGOKU_AMBIENT.get();
            case RYU_HANBIN -> com.soybe.awakeningsystem.ModSounds.COMPANION_SUKUNA_AMBIENT.get();
            case TWENTY_FIFTH_BAAM -> com.soybe.awakeningsystem.ModSounds.COMPANION_DAN_HENG_IL_AMBIENT.get();
            case RAIDEN_SHOGUN -> com.soybe.awakeningsystem.ModSounds.COMPANION_RAIDEN_SHOGUN_AMBIENT.get();
            case ZHONGLI -> com.soybe.awakeningsystem.ModSounds.COMPANION_ZHONGLI_AMBIENT.get();
            case HUTAO -> com.soybe.awakeningsystem.ModSounds.COMPANION_HU_TAO_AMBIENT.get();
            case GANYU -> com.soybe.awakeningsystem.ModSounds.COMPANION_GANYU_AMBIENT.get();
            case NEUVILLETTE -> com.soybe.awakeningsystem.ModSounds.COMPANION_NEUVILLETTE_AMBIENT.get();
            case MARCH_7TH -> com.soybe.awakeningsystem.ModSounds.COMPANION_MARCH_7TH_AMBIENT.get();
            case ACHERON -> com.soybe.awakeningsystem.ModSounds.COMPANION_ACHERON_AMBIENT.get();
            case HOSHIMI_MIYABI -> com.soybe.awakeningsystem.ModSounds.COMPANION_HOSHIMI_MIYABI_AMBIENT.get();
            case ELLEN_JOE -> com.soybe.awakeningsystem.ModSounds.COMPANION_ELLEN_JOE_AMBIENT.get();
            case DILUC -> com.soybe.awakeningsystem.ModSounds.COMPANION_DILUC_AMBIENT.get();
            case ELENA_RECEPTIONIST -> com.soybe.awakeningsystem.ModSounds.COMPANION_NOELLE_AMBIENT.get();
            case BLACK_MARKET_DEALER -> com.soybe.awakeningsystem.ModSounds.COMPANION_AVENTURINE_AMBIENT.get();
            case CASINO_RECEPTIONIST -> com.soybe.awakeningsystem.ModSounds.COMPANION_ROBIN_AMBIENT.get();
            default -> com.soybe.awakeningsystem.ModSounds.NPC_INTERACT.get();
        };
    }

    public static NpcProfile getById(String id) {
        if (id == null) return SUNG_JINWOO;
        String clean = id.toLowerCase(Locale.ROOT).trim();
        for (NpcProfile p : values()) {
            if (p.id.equals(clean)) return p;
        }
        return SUNG_JINWOO;
    }
}
