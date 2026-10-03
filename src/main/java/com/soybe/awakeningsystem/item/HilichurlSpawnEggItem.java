package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.abyss.SpiralAbyssManager;
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
 * Huevo de invocación especializado para Hilichurls de Genshin Impact.
 * Genera Hilichurls con máscara de hueso blanca, pelaje tribal marrón
 * y variantes de combate (Berserker, Guardia con escudo o Tirador).
 */
public class HilichurlSpawnEggItem extends DeferredSpawnEggItem {

    public HilichurlSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type,
                                 int backgroundColor, int highlightColor,
                                 Properties properties) {
        super(type, backgroundColor, highlightColor, properties);
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

        Mob hilichurl = SpiralAbyssManager.spawnRandomHilichurl(serverLevel, spawnPos);
        if (hilichurl != null) {
            hilichurl.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }

            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, spawnPos);
            return InteractionResult.CONSUME;
        }

        return super.useOn(context);
    }
}
