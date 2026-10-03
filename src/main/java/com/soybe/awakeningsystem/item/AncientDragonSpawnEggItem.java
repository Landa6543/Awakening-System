package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

/**
 * Huevo de invocación especializado para el Dragón Ancestral.
 * Aplica directamente la variante elemental (Fuego, Hielo, Bosque, Sombra)
 * al dragón invocado.
 */
public class AncientDragonSpawnEggItem extends DeferredSpawnEggItem {
    private final int variant;

    public AncientDragonSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type,
                                    int backgroundColor, int highlightColor,
                                    int variant, Properties properties) {
        super(type, backgroundColor, highlightColor, properties);
        this.variant = variant;
    }

    public int getVariant() {
        return this.variant;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState state = level.getBlockState(clickedPos);

        BlockPos spawnPos = state.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(face);

        AncientDragonEntity dragon = ModEntities.ANCIENT_DRAGON.get().create(serverLevel);
        if (dragon != null) {
            dragon.setVariant(this.variant);
            dragon.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);
            serverLevel.addFreshEntity(dragon);

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }

            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, spawnPos);
            return InteractionResult.CONSUME;
        }

        return super.useOn(context);
    }
}
