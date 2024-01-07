package unagidohn.unagithreeblockpuzzle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemMoveLeft extends Item {
    public ItemMoveLeft(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if(level.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
        }
        UnagiThreeBlockPuzzle.puzzleManager.MoveLeft();

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(interactionHand), level.isClientSide());
    }
}
