package com.soybe.awakeningsystem.trial;

import com.soybe.awakeningsystem.DungeonManager;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lógica del Generador de Desafío: espera a un cazador, lanza tres oleadas (la última con un
 * élite), y al vaciarse reparte una Llave de Cámara y un núcleo de monstruo a cada participante.
 * Después descansa media hora de juego antes de poder repetirse.
 */
public class TrialSpawnerBlockEntity extends BlockEntity {

    public static final int THEME_HALL = 0, THEME_MURIM = 1, THEME_SHADOW = 2, THEME_ELEMENTAL = 3;

    private static final int IDLE = 0, ACTIVE = 1, COOLDOWN = 2;
    private static final double ACTIVATE_RANGE = 8.0D;
    private static final double ARENA_RANGE = 20.0D;
    private static final int TOTAL_WAVES = 3;
    private static final long COOLDOWN_TICKS = 36000L;      // 30 minutos
    private static final int ABANDON_TICKS = 600;           // 30 segundos sin nadie cerca
    private static final int WAVE_DELAY_TICKS = 60;
    private static final ResourceLocation ELITE_HEALTH_ID = ResourceLocation.fromNamespaceAndPath("awakening_system", "trial_elite_health");
    private static final ResourceLocation ELITE_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath("awakening_system", "trial_elite_damage");

    private int theme = THEME_HALL;
    private int state = IDLE;
    private int wave = 0;
    private int tier = 1;
    private long cooldownUntil = 0L;
    private int delay = 0;
    private int abandon = 0;
    private final List<UUID> mobs = new ArrayList<>();
    private final Set<UUID> participants = new HashSet<>();
    /** Huecos de las puertas que se cierran con barreras de fuerza durante la prueba. */
    private final List<BlockPos> barriers = new ArrayList<>();

    public void addBarrier(BlockPos pos) {
        barriers.add(pos.immutable());
        setChanged();
    }

    private void raiseBarriers(ServerLevel level) {
        for (BlockPos p : barriers) {
            if (level.getBlockState(p).isAir()) level.setBlock(p, TrialRegistry.FORCE_BARRIER.get().defaultBlockState(), 3);
        }
        if (!barriers.isEmpty()) level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.6F);
    }

    private void lowerBarriers(ServerLevel level) {
        for (BlockPos p : barriers) {
            if (level.getBlockState(p).is(TrialRegistry.FORCE_BARRIER.get())) level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
        if (!barriers.isEmpty()) level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.6F);
    }

    /**
     * Niebla de la sala de Sombras: quien no lleve una antorcha o un farol en la mano queda casi a
     * ciegas. Es lo que pide el roadmap: antorchas tácticas.
     */
    private void applyShadowFog(ServerLevel level, BlockPos pos) {
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || p.isCreative()) continue;
            double dx = p.getX() - (pos.getX() + 0.5D), dz = p.getZ() - (pos.getZ() + 0.5D), dy = p.getY() - pos.getY();
            if (dx * dx + dz * dz > 11.0D * 11.0D || Math.abs(dy) > 7.0D) continue;
            if (holdsLight(p)) continue;
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 60, 0, false, false));
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0, false, false));
            if (level.getGameTime() % 100 == 0) p.displayClientMessage(Component.translatable("awakening_system.trial.shadow_fog"), true);
        }
    }

    private static boolean holdsLight(ServerPlayer p) {
        for (ItemStack s : new ItemStack[]{p.getMainHandItem(), p.getOffhandItem()}) {
            if (s.is(net.minecraft.world.item.Items.TORCH) || s.is(net.minecraft.world.item.Items.SOUL_TORCH)
                    || s.is(net.minecraft.world.item.Items.LANTERN) || s.is(net.minecraft.world.item.Items.SOUL_LANTERN)) return true;
        }
        return false;
    }

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(TrialRegistry.SPAWNER_ENTITY.get(), pos, state);
    }

    public void setTheme(int theme) {
        this.theme = theme;
        setChanged();
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, TrialSpawnerBlockEntity be) {
        if (level instanceof ServerLevel sl) be.tick(sl, pos);
    }

    private void tick(ServerLevel level, BlockPos pos) {
        long now = level.getGameTime();
        if (theme == THEME_SHADOW && now % 20 == 0) applyShadowFog(level, pos);
        switch (state) {
            case COOLDOWN -> {
                if (now >= cooldownUntil) {
                    state = IDLE;
                    setActiveLook(level, pos, false);
                    setChanged();
                } else if (now % 40 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, 2, 0.2D, 0.1D, 0.2D, 0.0D);
                }
            }
            case IDLE -> {
                if (now % 10 == 0) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 1, 0.25D, 0.2D, 0.25D, 0.0D);
                }
                if (now % 20 == 0) {
                    List<ServerPlayer> near = playersWithin(level, pos, ACTIVATE_RANGE);
                    if (!near.isEmpty()) start(level, pos, near);
                }
            }
            case ACTIVE -> {
                if (now % 10 != 0) return;
                mobs.removeIf(id -> {
                    Entity e = level.getEntity(id);
                    return e == null || !e.isAlive();
                });

                List<ServerPlayer> inArena = playersWithin(level, pos, ARENA_RANGE);
                inArena.forEach(p -> participants.add(p.getUUID()));
                abandon = inArena.isEmpty() ? abandon + 10 : 0;
                if (abandon >= ABANDON_TICKS) {
                    reset(level, pos);
                    return;
                }

                level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 3, 0.3D, 0.3D, 0.3D, 0.02D);
                if (!mobs.isEmpty()) return;
                if (delay > 0) {
                    delay -= 10;
                } else if (wave < TOTAL_WAVES) {
                    spawnWave(level, pos);
                } else {
                    complete(level, pos);
                }
            }
            default -> state = IDLE;
        }
    }

    private static List<ServerPlayer> playersWithin(ServerLevel level, BlockPos pos, double range) {
        Vec3 center = Vec3.atCenterOf(pos);
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            if (p.distanceToSqr(center) <= range * range) out.add(p);
        }
        return out;
    }

    private void start(ServerLevel level, BlockPos pos, List<ServerPlayer> players) {
        participants.clear();
        tier = 1;
        for (ServerPlayer p : players) {
            participants.add(p.getUUID());
            SystemData data = SystemEvents.data(p);
            if (data != null) tier = Math.max(tier, DungeonManager.getRankTier(data.evaluatedRank()));
        }
        state = ACTIVE;
        wave = 0;
        delay = 30;
        abandon = 0;
        setActiveLook(level, pos, true);
        raiseBarriers(level);
        level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.playSound(null, pos, SoundEvents.WITHER_AMBIENT, SoundSource.BLOCKS, 0.5F, 1.4F);
        notifyParticipants(level, Component.translatable("awakening_system.trial.started"));
        setChanged();
    }

    private void spawnWave(ServerLevel level, BlockPos pos) {
        wave++;
        int count = 2 + participants.size() + tier / 3 + wave;
        boolean eliteWave = wave == TOTAL_WAVES;
        for (int i = 0; i < count; i++) {
            spawnMob(level, pos, eliteWave && i == 0);
        }
        delay = WAVE_DELAY_TICKS;
        level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0F, 0.9F);
        notifyParticipants(level, Component.translatable("awakening_system.trial.wave", wave, TOTAL_WAVES));
        setChanged();
    }

    private EntityType<? extends Mob> pickType(ServerLevel level) {
        List<EntityType<? extends Mob>> pool = switch (theme) {
            case THEME_MURIM -> List.of(EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.ZOMBIE);
            case THEME_SHADOW -> List.of(EntityType.WITHER_SKELETON, EntityType.SKELETON, EntityType.ZOMBIE);
            case THEME_ELEMENTAL -> List.of(EntityType.BLAZE, EntityType.STRAY, EntityType.HUSK);
            default -> List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.STRAY);
        };
        return pool.get(level.random.nextInt(pool.size()));
    }

    private void spawnMob(ServerLevel level, BlockPos pos, boolean elite) {
        Mob mob = pickType(level).create(level);
        if (mob == null) return;
        BlockPos spot = findSpawnSpot(level, pos);
        mob.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.TRIAL_SPAWNER, null);
        mob.setPersistenceRequired();
        mob.addTag("trial_mob");

        if (elite) {
            var health = mob.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.addPermanentModifier(new AttributeModifier(ELITE_HEALTH_ID, 2.0D + tier * 0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            var damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addPermanentModifier(new AttributeModifier(ELITE_DAMAGE_ID, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            mob.setHealth(mob.getMaxHealth());
            mob.setCustomName(Component.translatable("awakening_system.trial.elite", mob.getType().getDescription()));
            mob.setCustomNameVisible(true);
            mob.setGlowingTag(true);
        }

        ServerPlayer target = nearestParticipant(level, spot);
        if (target != null) mob.setTarget(target);

        level.addFreshEntityWithPassengers(mob);
        mobs.add(mob.getUUID());
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spot.getX() + 0.5D, spot.getY() + 0.5D, spot.getZ() + 0.5D, 15, 0.3D, 0.5D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, spot.getX() + 0.5D, spot.getY() + 0.5D, spot.getZ() + 0.5D, 6, 0.3D, 0.4D, 0.3D, 0.01D);
    }

    /** Un hueco libre de dos bloques sobre suelo firme, a unos bloques del generador. */
    private BlockPos findSpawnSpot(ServerLevel level, BlockPos pos) {
        for (int attempt = 0; attempt < 16; attempt++) {
            int dx = level.random.nextInt(11) - 5, dz = level.random.nextInt(11) - 5;
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos p = pos.offset(dx, dy, dz);
                if (level.getBlockState(p.below()).isSolid() && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                    return p;
                }
            }
        }
        return pos.above();
    }

    private ServerPlayer nearestParticipant(ServerLevel level, BlockPos from) {
        ServerPlayer best = null;
        double bestDist = Double.MAX_VALUE;
        for (UUID id : participants) {
            if (level.getPlayerByUUID(id) instanceof ServerPlayer p && p.isAlive()) {
                double d = p.distanceToSqr(Vec3.atCenterOf(from));
                if (d < bestDist) {
                    bestDist = d;
                    best = p;
                }
            }
        }
        return best;
    }

    private void complete(ServerLevel level, BlockPos pos) {
        state = COOLDOWN;
        cooldownUntil = level.getGameTime() + COOLDOWN_TICKS;
        setActiveLook(level, pos, false);
        lowerBarriers(level);

        Item core = coreForTier(tier);
        for (UUID id : participants) {
            if (!(level.getPlayerByUUID(id) instanceof ServerPlayer p)) continue;
            give(p, new ItemStack(TrialRegistry.TRIAL_KEY.get()));
            give(p, new ItemStack(core, 1 + level.random.nextInt(2)));
            p.displayClientMessage(Component.translatable("awakening_system.trial.completed"), false);
        }
        ExperienceOrb.award(level, Vec3.atCenterOf(pos).add(0, 1, 0), 30 + tier * 10);
        level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.8F, 1.1F);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 40, 0.5D, 0.6D, 0.5D, 0.2D);
        participants.clear();
        mobs.clear();
        setChanged();
    }

    /** El núcleo que se gana depende del rango del cazador más fuerte del grupo. */
    private static Item coreForTier(int tier) {
        if (tier >= 7) return SystemItems.MONSTER_CORE_S.get();
        if (tier == 6) return SystemItems.MONSTER_CORE_A.get();
        if (tier == 5) return SystemItems.MONSTER_CORE_B.get();
        if (tier == 4) return SystemItems.MONSTER_CORE_C.get();
        if (tier == 3) return SystemItems.MONSTER_CORE_D.get();
        return SystemItems.MONSTER_CORE_E.get();
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    /** Si todos se van, la prueba se cancela y los monstruos que quedaban desaparecen. */
    private void reset(ServerLevel level, BlockPos pos) {
        for (UUID id : mobs) {
            Entity e = level.getEntity(id);
            if (e != null) e.discard();
        }
        mobs.clear();
        participants.clear();
        state = IDLE;
        wave = 0;
        abandon = 0;
        setActiveLook(level, pos, false);
        lowerBarriers(level);
        setChanged();
    }

    private void notifyParticipants(ServerLevel level, Component message) {
        for (UUID id : participants) {
            if (level.getPlayerByUUID(id) instanceof ServerPlayer p) p.displayClientMessage(message, true);
        }
    }

    private void setActiveLook(ServerLevel level, BlockPos pos, boolean active) {
        BlockState st = level.getBlockState(pos);
        if (st.hasProperty(TrialSpawnerBlock.ACTIVE) && st.getValue(TrialSpawnerBlock.ACTIVE) != active) {
            level.setBlock(pos, st.setValue(TrialSpawnerBlock.ACTIVE, active), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Theme", theme);
        tag.putInt("State", state);
        tag.putInt("Wave", wave);
        tag.putInt("Tier", tier);
        tag.putLong("CooldownUntil", cooldownUntil);
        tag.putInt("Delay", delay);
        ListTag mobList = new ListTag();
        mobs.forEach(id -> mobList.add(NbtUtils.createUUID(id)));
        tag.put("Mobs", mobList);
        ListTag playerList = new ListTag();
        participants.forEach(id -> playerList.add(NbtUtils.createUUID(id)));
        tag.put("Participants", playerList);
        tag.putLongArray("Barriers", barriers.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        theme = tag.getInt("Theme");
        state = tag.getInt("State");
        wave = tag.getInt("Wave");
        tier = Math.max(1, tag.getInt("Tier"));
        cooldownUntil = tag.getLong("CooldownUntil");
        delay = tag.getInt("Delay");
        mobs.clear();
        for (Tag t : tag.getList("Mobs", Tag.TAG_INT_ARRAY)) mobs.add(NbtUtils.loadUUID(t));
        participants.clear();
        for (Tag t : tag.getList("Participants", Tag.TAG_INT_ARRAY)) participants.add(NbtUtils.loadUUID(t));
        barriers.clear();
        for (long l : tag.getLongArray("Barriers")) barriers.add(BlockPos.of(l));
    }
}
