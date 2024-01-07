package unagidohn.unagithreeblockpuzzle;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class itemPuzzlePutKey extends Item {
    public itemPuzzlePutKey(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        return UnagiThreeBlockPuzzle.puzzleManager.Init(useOnContext);
    }


}
