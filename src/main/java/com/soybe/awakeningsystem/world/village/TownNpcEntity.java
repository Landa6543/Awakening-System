package com.soybe.awakeningsystem.world.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A townsperson of the medieval town: a player-shaped person wearing one of the town's skins. They stroll
 * around their home, open doors, look at whoever passes and greet a player who talks to them (right-click).
 * Their trade ({@link #job()}) is kept for the daily routines and shops they will get later.
 */
public class TownNpcEntity extends PathfinderMob {
    /** The skins, by index: textures/entity/town_npc/<name>.png */
    public static final String[] SKINS = {"smith", "bearded", "farmer", "priest", "innkeeper_blonde", "innkeeper_fae", "innkeeper_alice",
            "librarian", "baker", "baker_woman", "merchant", "townsman_green", "townswoman_emily", "townswoman_paula", "farmer2", "farmer_woman"};
    /** Each skin is one person with their own name, so a town never has two of the same. Same order as SKINS. */
    public static final String[] NAMES = {"Borin", "Aldric", "Godwin", "Anselm", "Greta", "Lyria", "Maren",
            "Elandor", "Bram", "Hilda", "Dorian", "Cedric", "Elise", "Rosalind", "Rowan", "Brenna"};
    /** Skins tied to one building (the forge, the tavern, the chapel, the library, the stall): never handed to a plain house. */
    private static final java.util.Set<String> POSTED = java.util.Set.of("smith", "priest", "innkeeper_blonde", "innkeeper_fae",
            "innkeeper_alice", "librarian", "merchant");
    /** Skins drawn with the thin (Alex) arms. */
    private static final java.util.Set<String> SLIM = java.util.Set.of("innkeeper_blonde", "innkeeper_fae", "townswoman_emily", "townswoman_paula",
            "farmer_woman");
    /** Skins of women, so their title reads "Tabernera", "Vecina"... */
    private static final java.util.Set<String> WOMEN = java.util.Set.of("innkeeper_blonde", "innkeeper_fae", "innkeeper_alice", "baker_woman",
            "townswoman_emily", "townswoman_paula", "farmer_woman");

    public static boolean slim(int skin) {
        return SLIM.contains(SKINS[Math.floorMod(skin, SKINS.length)]);
    }

    public static boolean woman(int skin) {
        return WOMEN.contains(SKINS[Math.floorMod(skin, SKINS.length)]);
    }

    public static String nameOf(int skin) {
        return NAMES[Math.floorMod(skin, NAMES.length)];
    }

    /** The role a trade talks as: the workshop trades are smiths, the stall trades merchants, and so on. */
    public static String roleOfJob(String job) {
        return switch (job == null ? "" : job) {
            case "weaponsmith", "toolsmith", "armorer", "mason", "leatherworker", "fletcher" -> "smith";
            case "butcher", "fisherman" -> "merchant";
            case "farmer", "shepherd" -> "farmer";
            case "innkeeper", "barmaid" -> "innkeeper";
            case "baker" -> "baker";
            case "librarian", "cartographer" -> "librarian";
            case "cleric" -> "priest";
            default -> "";
        };
    }

    /**
     * A skin for someone moving into a town where {@code used} skins are already taken: the trade's own skins
     * first, then (for anyone) a plain townsperson's or a farm or bakery skin. Returns -1 when nobody is left,
     * so the town never shows the same person twice.
     */
    public static int uniqueSkin(String job, int seed, java.util.Set<Integer> used) {
        int own = skinFor(job, seed);
        String role = roleOfJob(job);
        java.util.List<Integer> order = new java.util.ArrayList<>();
        order.add(own);
        for (int i = 0; i < SKINS.length; i++) if (!role.isEmpty() && roleOfSkin(i).equals(role)) order.add(i);
        for (int i : new int[]{1, 11, 12, 13}) order.add(i);
        for (int i = 0; i < SKINS.length; i++) if (!POSTED.contains(SKINS[i])) order.add(i);
        for (int i : order) if (!used.contains(i)) return i;
        return -1;
    }

    /** The trade a skin dresses for, for people with no trade of their own (spawn eggs, the plain houses). */
    public static String roleOfSkin(int skin) {
        String s = SKINS[Math.floorMod(skin, SKINS.length)];
        if (s.startsWith("farmer")) return "farmer";
        if (s.startsWith("innkeeper")) return "innkeeper";
        if (s.startsWith("baker")) return "baker";
        return switch (s) {
            case "smith" -> "smith";
            case "priest" -> "priest";
            case "librarian" -> "librarian";
            case "merchant" -> "merchant";
            default -> "villager";
        };
    }

    private static int pick(int seed, int... skins) {
        return skins[Math.floorMod(seed, skins.length)];
    }

    /** The skin for a trade: workshop trades wear the smith, the tavern its barmaids, and so on; anyone else a townsperson. */
    public static int skinFor(String job, int seed) {
        return switch (job == null ? "" : job) {
            case "weaponsmith", "toolsmith", "armorer", "mason", "leatherworker", "fletcher" -> 0;
            case "butcher", "fisherman" -> 10;
            case "farmer", "shepherd" -> pick(seed, 2, 14, 15);
            case "innkeeper" -> 4;
            case "barmaid" -> pick(seed, 5, 6);
            case "baker" -> pick(seed, 8, 9);
            case "librarian", "cartographer" -> 7;
            case "cleric" -> 3;
            default -> pick(seed, 1, 11, 12, 13);
        };
    }
    private static final int GREETINGS = 6;

    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(TownNpcEntity.class, EntityDataSerializers.INT);
    private String job = "";
    private BlockPos home = BlockPos.ZERO;

    public TownNpcEntity(EntityType<? extends TownNpcEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        if (this.getNavigation() instanceof GroundPathNavigation nav) nav.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 0.6));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.45));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(SKIN, 0);
    }

    public int skin() {
        return Math.floorMod(this.entityData.get(SKIN), SKINS.length);
    }

    public String job() {
        return job;
    }

    /** Settles this person at {@code home} with a trade and a skin; they stay within a few houses of it. */
    public void settle(BlockPos home, String job, int skin) {
        this.home = home.immutable();
        this.job = job == null ? "" : job;
        this.entityData.set(SKIN, skin);
        this.setCustomName(Component.literal(nameOf(skin)));
        this.restrictTo(this.home, reach());
    }

    /**
     * How far they go from home: the smith, the innkeepers, the priest, the librarian and the merchant keep to
     * their workplace (a few blocks); everyone else strolls around their part of town.
     */
    private int reach() {
        boolean posted = POSTED.contains(SKINS[skin()]) || job.contains("smith") || job.contains("inn") || job.contains("cleric")
                || job.contains("librarian") || job.contains("priest") || job.contains("barmaid");
        return posted ? 4 : 14;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            // a talk in the RPG style: who they are, what they do, a tip, the town
            this.getNavigation().stop();
            this.playSound(SoundEvents.VILLAGER_AMBIENT, 0.6F, 1.1F);
            // the weaponsmith also sells weapons and forges them to order
            com.soybe.awakeningsystem.dialogue.NpcDialogueManager.startDialogue(sp, this,
                    "weaponsmith".equals(job) ? com.soybe.awakeningsystem.dialogue.SmithForge.DIALOGUE : "town_npc");
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Skin", this.entityData.get(SKIN));
        tag.putString("Job", job);
        tag.putLong("Home", home.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SKIN, tag.getInt("Skin"));
        this.job = tag.getString("Job");
        this.home = BlockPos.of(tag.getLong("Home"));
        if (!home.equals(BlockPos.ZERO)) this.restrictTo(home, reach());
    }
}
