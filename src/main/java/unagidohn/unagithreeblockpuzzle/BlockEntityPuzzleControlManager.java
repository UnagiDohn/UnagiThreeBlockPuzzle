package unagidohn.unagithreeblockpuzzle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityPuzzleControlManager extends BlockEntity {
    int gameFrame;

    public BlockEntityPuzzleControlManager(BlockPos pos, BlockState state) {
        super(UnagiThreeBlockPuzzle.PUZZLE_CONTROL_MANAGER_ENTITY.get(), pos, state);

        gameFrame = 0;
    }

    public void tick() {
        System.out.println("Game Frame " + gameFrame++);

        UnagiThreeBlockPuzzle.puzzleManager.Tick(gameFrame);

        gameFrame = gameFrame % 1000;
    }

    public void SetLevel(Level level){
        UnagiThreeBlockPuzzle.puzzleManager.SetLevel(level);
    }
}
