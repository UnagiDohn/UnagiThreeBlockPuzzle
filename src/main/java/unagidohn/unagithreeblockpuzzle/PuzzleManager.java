package unagidohn.unagithreeblockpuzzle;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PuzzleManager {
    boolean isGameEnabled = false;
    final int CELL_NUM_X = 11;
    final int CELL_NUM_Y = 21;

    final int ARRAY_NUM_X = 17;
    final int ARRAY_NUM_Y = 27;

    final int CELL_START_X = 3;
    final int CELL_START_Y = 3;

    int[][] cellArray;
    int[][] renderCellArray;

    final int BLOCK_EMPTY = 4;
    final int BLOCK_INVALID = 5;

    final int ROTATION_NUM = 4;

    int axisXDir = -1;

    int axisYDir = 1;

    BlockPos managerBlockPos;
    BlockPos puzzleBasePos;
    Level puzzleExistLevel;
    Player puzzleCreatedPlayer;

    enum GameState{
        START,
        CALL_PuzzleBlock,
        FALL_PuzzleBlock,
        END_PuzzleBlock,
        END,
        IDLE
    };
    GameState gameState;

    enum PuzzleBlock{
        Puzzle1,
        Puzzle2,
        Puzzle3,
        Puzzle4,
        Puzzle5,
        Num
    }

    PuzzleBlock fallingPuzzleBlock;
    int fallingPuzzleBlockX;
    int fallingPuzzleBlockY;
    int fallingPuzzleBlockRotation;
    int fallingPuzzleBlockBlockId;
    int nextPuzzleBlockBlockId;
    int lineCompleteNum;
    int gameFrame;

    public PuzzleManager(){
        cellArray = new int[ARRAY_NUM_Y][];
        renderCellArray = new int[ARRAY_NUM_Y][];
        for(int yIndex = 0; yIndex < ARRAY_NUM_Y; yIndex++ ){
            cellArray[yIndex] = new int[ARRAY_NUM_X];
            renderCellArray[yIndex] = new int[ARRAY_NUM_X];
        }

        ClearBlock();
    }

    private void ClearBlock(){
        for(int yIndex = 0; yIndex < ARRAY_NUM_Y; yIndex++ ){
            for(int xIndex = 0; xIndex < ARRAY_NUM_X; xIndex++ ){
                cellArray[yIndex][xIndex] = BLOCK_INVALID;
            }
        }

        for(int yIndex = CELL_START_Y; yIndex < CELL_START_Y+ CELL_NUM_Y; yIndex++ ){
            for(int xIndex = CELL_START_X; xIndex < CELL_START_X + CELL_NUM_X; xIndex++ ){
                cellArray[yIndex][xIndex] = BLOCK_EMPTY;
            }
        }

        for(int yIndex = CELL_START_Y+ CELL_NUM_Y; yIndex < ARRAY_NUM_Y; yIndex++ ){
            for(int xIndex = CELL_START_X; xIndex < CELL_START_X + CELL_NUM_X; xIndex++ ){
                cellArray[yIndex][xIndex] = BLOCK_EMPTY;
            }
        }
    }
    public InteractionResult Init(UseOnContext useOnContext){
        gameState = GameState.IDLE;

        Level level = useOnContext.getLevel();
        BlockPos blockpos = useOnContext.getClickedPos();
        Player player = useOnContext.getPlayer();
        BlockState blockstate = level.getBlockState(blockpos);
        ItemStack itemstack = useOnContext.getItemInHand();

        if(level.isClientSide()) {
           return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
        }

        puzzleExistLevel = level;
        managerBlockPos = blockpos;
        puzzleBasePos = new BlockPos(managerBlockPos.getX(), managerBlockPos.getY() + 1, managerBlockPos.getZ());
        puzzleCreatedPlayer = player;

        BlockPos createBlockPos = managerBlockPos;
        BlockState createBlockState = null;

        createBlockPos = new BlockPos(managerBlockPos.getX(), managerBlockPos.getY(), managerBlockPos.getZ());
        createBlockState = GetPuzzleBlockState(999);

        level.setBlock(createBlockPos, createBlockState, 11);
        level.gameEvent(GameEvent.BLOCK_CHANGE, createBlockPos, GameEvent.Context.of(player, createBlockState));

        InitPuzzleBlock();

        // 開始時点ロゴを書く.
        WriteLogo();
        lineCompleteNum = 0;
        gameFrame = 0;

        RefreshCell();

        return InteractionResult.sidedSuccess(level.isClientSide);
    }



    private void WriteLogo(){
        ClearBlock();

        WriteAlphabet(CELL_START_X + 1, CELL_START_Y + 14, 'P');
        WriteAlphabet(CELL_START_X + 1, CELL_START_Y + 8, 'U');
        WriteAlphabet(CELL_START_X + 1, CELL_START_Y + 2, 'Z');

        WriteAlphabet(CELL_START_X + 6, CELL_START_Y + 14, 'Z');
        WriteAlphabet(CELL_START_X + 6, CELL_START_Y + 8, 'L');
        WriteAlphabet(CELL_START_X + 6, CELL_START_Y + 2, 'E');
    }

    private void WriteGameOver(){
        ClearBlock();

        cellArray[CELL_START_Y + 2][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 2][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 2][CELL_START_X + 3] = 0;
        cellArray[CELL_START_Y + 3][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 3][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 4][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 4][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 5][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 5][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 6][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 6][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 6][CELL_START_X + 3] = 0;

        cellArray[CELL_START_Y + 8][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 8][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 9][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 9][CELL_START_X + 3] = 0;
        cellArray[CELL_START_Y + 9][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 10][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 10][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 10][CELL_START_X + 3] = 0;
        cellArray[CELL_START_Y + 10][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 11][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 11][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 11][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 12][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 12][CELL_START_X + 4] = 0;

        cellArray[CELL_START_Y + 14][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 14][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 14][CELL_START_X + 3] = 0;
        cellArray[CELL_START_Y + 14][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 15][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 16][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 16][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 16][CELL_START_X + 3] = 0;
        cellArray[CELL_START_Y + 16][CELL_START_X + 4] = 0;
        cellArray[CELL_START_Y + 17][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 18][CELL_START_X + 1] = 0;
        cellArray[CELL_START_Y + 18][CELL_START_X + 2] = 0;
        cellArray[CELL_START_Y + 18][CELL_START_X + 3] = 0;
        cellArray[CELL_START_Y + 18][CELL_START_X + 4] = 0;

        WriteNumber(CELL_START_X + 6, CELL_START_Y + 14, (lineCompleteNum / 100) % 10);
        WriteNumber(CELL_START_X + 6, CELL_START_Y + 8, (lineCompleteNum / 10) % 10);
        WriteNumber(CELL_START_X + 6, CELL_START_Y + 2, lineCompleteNum % 10);
    }

    private void WriteAlphabet(int baseX, int baseY, char moji){
        switch(moji){
            case 'P':
                cellArray[baseY + 0][baseX + 0] = 0;
                cellArray[baseY + 0][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 0] = 0;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 0] = 0;
                cellArray[baseY + 2][baseX + 1] = 0;
                cellArray[baseY + 2][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 0] = 0;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 0;
                cellArray[baseY + 4][baseX + 0] = 0;
                cellArray[baseY + 4][baseX + 1] = 0;
                cellArray[baseY + 4][baseX + 2] = BLOCK_EMPTY;
                break;
            case 'U':
                cellArray[baseY + 0][baseX + 0] = 0;
                cellArray[baseY + 0][baseX + 1] = 0;
                cellArray[baseY + 0][baseX + 2] = 0;
                cellArray[baseY + 1][baseX + 0] = 0;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 0;
                cellArray[baseY + 2][baseX + 0] = 0;
                cellArray[baseY + 2][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 2] = 0;
                cellArray[baseY + 3][baseX + 0] = 0;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 0;
                cellArray[baseY + 4][baseX + 0] = 0;
                cellArray[baseY + 4][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 2] = 0;
                break;
            case 'Z':
                cellArray[baseY + 0][baseX + 0] = 0;
                cellArray[baseY + 0][baseX + 1] = 0;
                cellArray[baseY + 0][baseX + 2] = 0;
                cellArray[baseY + 1][baseX + 0] = 0;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 1] = 0;
                cellArray[baseY + 2][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 0;
                cellArray[baseY + 4][baseX + 0] = 0;
                cellArray[baseY + 4][baseX + 1] = 0;
                cellArray[baseY + 4][baseX + 2] = 0;
                break;
            case 'L':
                cellArray[baseY + 0][baseX + 0] = 0;
                cellArray[baseY + 0][baseX + 1] = 0;
                cellArray[baseY + 0][baseX + 2] = 0;
                cellArray[baseY + 1][baseX + 0] = 0;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 0] = 0;
                cellArray[baseY + 2][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 0] = 0;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 0] = 0;
                cellArray[baseY + 4][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 2] = BLOCK_EMPTY;
                break;
            case 'E':
                cellArray[baseY + 0][baseX + 0] = 0;
                cellArray[baseY + 0][baseX + 1] = 0;
                cellArray[baseY + 0][baseX + 2] = 0;
                cellArray[baseY + 1][baseX + 0] = 0;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 0] = 0;
                cellArray[baseY + 2][baseX + 1] = 0;
                cellArray[baseY + 2][baseX + 2] = 0;
                cellArray[baseY + 3][baseX + 0] = 0;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 0] = 0;
                cellArray[baseY + 4][baseX + 1] = 0;
                cellArray[baseY + 4][baseX + 2] = 0;
                break;
        }

    }

    private void WriteNumber(int baseX, int baseY, int number){
        switch (number){
            case 1:
                cellArray[baseY + 0][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 2:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = 1;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 3:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 4:
                cellArray[baseY + 0][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 5:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 6:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = 1;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 7:
                cellArray[baseY + 0][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 8:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = 1;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 9:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = 1;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            case 0:
                cellArray[baseY + 0][baseX + 0] = 1;
                cellArray[baseY + 0][baseX + 1] = 1;
                cellArray[baseY + 0][baseX + 2] = 1;
                cellArray[baseY + 1][baseX + 0] = 1;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = 1;
                cellArray[baseY + 2][baseX + 0] = 1;
                cellArray[baseY + 2][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 2] = 1;
                cellArray[baseY + 3][baseX + 0] = 1;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = 1;
                cellArray[baseY + 4][baseX + 0] = 1;
                cellArray[baseY + 4][baseX + 1] = 1;
                cellArray[baseY + 4][baseX + 2] = 1;
                break;
            default:
                cellArray[baseY + 0][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 0][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 1][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 2][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 3][baseX + 2] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 0] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 1] = BLOCK_EMPTY;
                cellArray[baseY + 4][baseX + 2] = BLOCK_EMPTY;
                break;
        }
    }

    public void StartGame(){
        isGameEnabled = true;
        lineCompleteNum = 0;

        ClearBlock();
        RefreshCell();

        gameState = GameState.START;
    }

    private void RefreshCell(){
        BlockPos createBlockPos = managerBlockPos;
        BlockState createBlockState = null;

        for(int yIndex = 0; yIndex < ARRAY_NUM_Y; yIndex++ ){
            for(int xIndex = 0; xIndex < ARRAY_NUM_X; xIndex++ ){
                renderCellArray[yIndex][xIndex] = cellArray[yIndex][xIndex];
            }
        }

        RenderFallingPuzzleBlock();

        if(true){
            for (int yIndex = 0; yIndex < CELL_NUM_Y; yIndex++) {
                for (int xIndex = 0; xIndex < CELL_NUM_X; xIndex++) {
                    createBlockPos = new BlockPos(puzzleBasePos.getX() + axisXDir * xIndex, puzzleBasePos.getY() + axisYDir * yIndex, puzzleBasePos.getZ());
                    createBlockState = GetPuzzleBlockState(renderCellArray[CELL_START_Y + yIndex][CELL_START_X + xIndex]);

                    puzzleExistLevel.setBlock(createBlockPos, createBlockState, 11);
                    puzzleExistLevel.gameEvent(GameEvent.BLOCK_CHANGE, createBlockPos, GameEvent.Context.of(puzzleCreatedPlayer, createBlockState));
                }
            }
        }else {
            for (int yIndex = 0; yIndex < ARRAY_NUM_Y; yIndex++) {
                for (int xIndex = 0; xIndex < ARRAY_NUM_X; xIndex++) {
                    createBlockPos = new BlockPos(puzzleBasePos.getX() + axisXDir * xIndex, puzzleBasePos.getY() + axisYDir * yIndex, puzzleBasePos.getZ());
                    createBlockState = GetPuzzleBlockState(renderCellArray[yIndex][xIndex]);

                    puzzleExistLevel.setBlock(createBlockPos, createBlockState, 11);
                    puzzleExistLevel.gameEvent(GameEvent.BLOCK_CHANGE, createBlockPos, GameEvent.Context.of(puzzleCreatedPlayer, createBlockState));
                }
            }
        }

    }

    private void RenderFallingPuzzleBlock(){
        if(gameState == GameState.IDLE || gameState == GameState.START || gameState == GameState.CALL_PuzzleBlock){
            return;
        }
        for(int yIndex = 0; yIndex < 3; yIndex++){
            for(int xIndex = 0; xIndex < 3; xIndex++) {
                if(PuzzleBlockArray[fallingPuzzleBlock.ordinal()][fallingPuzzleBlockRotation][yIndex][xIndex] == 1){
                    renderCellArray[fallingPuzzleBlockY + yIndex][fallingPuzzleBlockX + xIndex] = fallingPuzzleBlockBlockId;
                }
            }
        }
    }

    public void SetLevel(Level level){
        puzzleExistLevel = level;
    }

    public void Tick(int currentGameFrame){
        if(currentGameFrame % 12 != 0){
            return;
        }
        gameFrame = currentGameFrame;

        if(!isGameEnabled){
            return;
        }
        UpdateGame();
        RefreshCell();
    }

    private void UpdateGame(){
        switch(gameState){
            case IDLE:

                break;
            case START:
                gameState = GameState.CALL_PuzzleBlock;
                break;
            case CALL_PuzzleBlock:
                fallingPuzzleBlockX = 7;
                fallingPuzzleBlockY = 22;
                fallingPuzzleBlockRotation = 0;
                fallingPuzzleBlockBlockId = nextPuzzleBlockBlockId % 4;
                nextPuzzleBlockBlockId++;
                PuzzleBlock[] values = PuzzleBlock.values();
                fallingPuzzleBlock = values[gameFrame % PuzzleBlock.Num.ordinal()];
                gameState = GameState.FALL_PuzzleBlock;
            case FALL_PuzzleBlock:
            {
                int nextFallingPuzzleBlockY = fallingPuzzleBlockY - 1;
                boolean isValid = IsValidPuzzleBlockPos(fallingPuzzleBlockX, nextFallingPuzzleBlockY, fallingPuzzleBlock);

                if(!isValid){
                    WriteCurrentPuzzleBlockToCell();
                    gameState = GameState.END_PuzzleBlock;
                }else{
                    fallingPuzzleBlockY = nextFallingPuzzleBlockY;
                }
            }
            break;
            case END_PuzzleBlock:
                if(CheckGameOver()){
                    gameState = gameState.END;
                }else{
                    CheckLine();
                    gameState = GameState.CALL_PuzzleBlock;
                }
                break;
            case END:
                WriteGameOver();
                gameState = gameState.IDLE;
                break;
            default:
                break;
        }
    }

    private BlockState GetPuzzleBlockState(int id){
        switch(id){
            case 0:
                return UnagiThreeBlockPuzzle.BLOCK_0.get().defaultBlockState();
            case 1:
                return UnagiThreeBlockPuzzle.BLOCK_1.get().defaultBlockState();
            case 2:
                return UnagiThreeBlockPuzzle.BLOCK_2.get().defaultBlockState();
            case 3:
                return UnagiThreeBlockPuzzle.BLOCK_3.get().defaultBlockState();
            case 4:
                return UnagiThreeBlockPuzzle.BLOCK_4.get().defaultBlockState();
            case 5:
                return UnagiThreeBlockPuzzle.BLOCK_0.get().defaultBlockState();
            case 999:
                return UnagiThreeBlockPuzzle.PUZZLE_CONTROL_MANAGER.get().defaultBlockState();
            default:
                return UnagiThreeBlockPuzzle.BLOCK_0.get().defaultBlockState();
        }
    }

    private boolean IsValidPuzzleBlockPos(int baseX, int baseY, PuzzleBlock PuzzleBlock){
        for(int yIndex = 0; yIndex < 3; yIndex++){
            for(int xIndex = 0; xIndex < 3; xIndex++) {
                if(PuzzleBlockArray[PuzzleBlock.ordinal()][fallingPuzzleBlockRotation][yIndex][xIndex] == 1){
                    if(cellArray[baseY + yIndex][baseX + xIndex] != BLOCK_EMPTY){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void WriteCurrentPuzzleBlockToCell(){
        for(int yIndex = 0; yIndex < 3; yIndex++){
            for(int xIndex = 0; xIndex < 3; xIndex++) {
                if(PuzzleBlockArray[fallingPuzzleBlock.ordinal()][fallingPuzzleBlockRotation][yIndex][xIndex] == 1){
                    cellArray[fallingPuzzleBlockY + yIndex][fallingPuzzleBlockX + xIndex] = fallingPuzzleBlockBlockId;
                }
            }
        }
    }

    private void CheckLine(){
        int yIndex = 0;
        int lineCheckMax = CELL_NUM_Y * 3;

        for (int lineCheckCounter = 0; lineCheckCounter < lineCheckMax; lineCheckCounter++) {
            boolean isLineComplete = true;
            for (int xIndex = 0; xIndex < CELL_NUM_X; xIndex++) {
                if (cellArray[CELL_START_Y + yIndex][CELL_START_X + xIndex] == BLOCK_EMPTY) {
                    isLineComplete = false;
                    break;
                }
            }

            if(isLineComplete){
                for(int yIndexMover = yIndex; yIndexMover < CELL_NUM_Y; yIndexMover++){
                    for (int xIndex = 0; xIndex < CELL_NUM_X; xIndex++) {
                        cellArray[CELL_START_Y + yIndexMover][CELL_START_X + xIndex] = cellArray[CELL_START_Y + yIndexMover + 1][CELL_START_X + xIndex];
                    }
                }
                lineCompleteNum++;
                if(lineCompleteNum > 999){
                    lineCompleteNum = 999;
                }
            }else{
                yIndex++;
                if(yIndex >= CELL_NUM_Y){
                    break;
                }
            }
        }
    }

    private boolean CheckGameOver(){
        boolean isGameOver = false;
        for(int yIndex = CELL_START_Y + CELL_NUM_Y; yIndex < ARRAY_NUM_Y; yIndex++){
            for (int xIndex = 0; xIndex < CELL_NUM_X; xIndex++) {
                if(cellArray[yIndex][CELL_START_X + xIndex] != BLOCK_EMPTY){
                    isGameOver = true;
                }
            }
        }
        return isGameOver;
    }

    public void MoveLeft(){
        if(gameState == GameState.IDLE){
            StartGame();
            return;
        }

        int nextFallingPuzzleBlockX = fallingPuzzleBlockX - 1;
        boolean isValid = IsValidPuzzleBlockPos(nextFallingPuzzleBlockX, fallingPuzzleBlockY, fallingPuzzleBlock);
        if(isValid){
            fallingPuzzleBlockX = nextFallingPuzzleBlockX;
        }
    }
    public void MoveRight(){
        if(gameState == GameState.IDLE){
            StartGame();
            return;
        }

        int nextFallingPuzzleBlockX = fallingPuzzleBlockX + 1;
        boolean isValid = IsValidPuzzleBlockPos(nextFallingPuzzleBlockX, fallingPuzzleBlockY, fallingPuzzleBlock);
        if(isValid){
            fallingPuzzleBlockX = nextFallingPuzzleBlockX;
        }
    }
    public void Rotation(){
        if(gameState == GameState.IDLE){
            StartGame();
            return;
        }

        fallingPuzzleBlockRotation = (fallingPuzzleBlockRotation + 1) % ROTATION_NUM;

        //FallBlock();
    }

    public void FallBlock(){
        for(int fallY = fallingPuzzleBlockY; fallY > 0; fallY --){
            int nextFallingPuzzleBlockY = fallingPuzzleBlockY - 1;
            boolean isValid = IsValidPuzzleBlockPos(fallingPuzzleBlockX, nextFallingPuzzleBlockY, fallingPuzzleBlock);
            if(isValid){
                fallingPuzzleBlockY = nextFallingPuzzleBlockY;
            }else{
                break;
            }
        }
    }

    int[][][][] PuzzleBlockArray;
    private void InitPuzzleBlock(){
        PuzzleBlockArray = new int[PuzzleBlock.Num.ordinal()][][][];
        PuzzleBlockArray[PuzzleBlock.Puzzle1.ordinal()] = new int [][][]{
                {
                        {0, 1, 0},
                        {0, 1, 0},
                        {0, 1, 0}
                },
                {
                        {0, 0, 0},
                        {1, 1, 1},
                        {0, 0, 0}
                },
                {
                        {0, 1, 0},
                        {0, 1, 0},
                        {0, 1, 0}
                },
                {
                        {0, 0, 0},
                        {1, 1, 1},
                        {0, 0, 0}
                }
        };

        PuzzleBlockArray[PuzzleBlock.Puzzle2.ordinal()] = new int[][][]{
                {
                        {0, 0, 0},
                        {0, 1, 1},
                        {0, 1, 0}
                },
                {
                        {0, 0, 0},
                        {1, 1, 0},
                        {0, 1, 0}
                },
                {
                        {0, 1, 0},
                        {1, 1, 0},
                        {0, 0, 0}
                },
                {
                        {0, 1, 0},
                        {0, 1, 1},
                        {0, 0, 0}
                }

        };
        PuzzleBlockArray[PuzzleBlock.Puzzle3.ordinal()] = new int[][][]{
                {
                        {0, 0, 0},
                        {1, 0, 1},
                        {0, 1, 0}
                },
                {
                        {0, 1, 0},
                        {1, 0, 0},
                        {0, 1, 0}
                },
                {
                        {0, 1, 0},
                        {1, 0, 1},
                        {0, 0, 0}
                },
                {
                        {0, 1, 0},
                        {0, 0, 1},
                        {0, 1, 0}
                }
        };
        PuzzleBlockArray[PuzzleBlock.Puzzle4.ordinal()] = new int[][][]{
                {
                        {0, 0, 0},
                        {1, 1, 0},
                        {0, 0, 1}
                },
                {
                        {0, 1, 0},
                        {0, 1, 0},
                        {1, 0, 0}
                },
                {
                        {1, 0, 0},
                        {0, 1, 1},
                        {0, 0, 0}
                },
                {
                        {0, 0, 1},
                        {0, 1, 0},
                        {0, 1, 0}
                }
        };
        PuzzleBlockArray[PuzzleBlock.Puzzle5.ordinal()] = new int[][][]{
                {
                        {0, 0, 0},
                        {0, 1, 1},
                        {1, 0, 0}
                },
                {
                        {1, 0, 0},
                        {0, 1, 0},
                        {0, 1, 0}
                },
                {
                        {0, 0, 1},
                        {1, 1, 0},
                        {0, 0, 0}
                },
                {
                        {0, 1, 0},
                        {0, 1, 0},
                        {0, 0, 1}
                }
        };
        fallingPuzzleBlockX = 5;
        fallingPuzzleBlockY = 10;
        fallingPuzzleBlock = PuzzleBlock.Puzzle1;
        fallingPuzzleBlockBlockId = 0;
        nextPuzzleBlockBlockId = 0;
    }
}
