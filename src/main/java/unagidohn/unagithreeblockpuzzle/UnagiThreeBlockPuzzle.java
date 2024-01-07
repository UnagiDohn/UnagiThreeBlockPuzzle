package unagidohn.unagithreeblockpuzzle;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(UnagiThreeBlockPuzzle.MODID)
public class UnagiThreeBlockPuzzle
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "unagithreeblockpuzzle";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final RegistryObject<Block> BLOCK_0 = BLOCKS.register("block_0", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLOCK_0_ITEM = ITEMS.register("block_0", () -> new BlockItem(BLOCK_0.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLOCK_1 = BLOCKS.register("block_1", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLOCK_1_ITEM = ITEMS.register("block_1", () -> new BlockItem(BLOCK_1.get(), new Item.Properties()));
    public static final RegistryObject<Block> BLOCK_2 = BLOCKS.register("block_2", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLOCK_2_ITEM = ITEMS.register("block_2", () -> new BlockItem(BLOCK_2.get(), new Item.Properties()));
    public static final RegistryObject<Block> BLOCK_3 = BLOCKS.register("block_3", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLOCK_3_ITEM = ITEMS.register("block_3", () -> new BlockItem(BLOCK_3.get(), new Item.Properties()));


    public static final RegistryObject<Item> PUZZLE_PUT_KEY = ITEMS.register("puzzle_put_key", () -> new itemPuzzlePutKey(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MOVE_LEFT = ITEMS.register("move_left", () -> new ItemMoveLeft(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MOVE_RIGHT = ITEMS.register("move_right", () -> new ItemMoveRight(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ROTATION = ITEMS.register("rotation", () -> new ItemRotation(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FALL_DOWN = ITEMS.register("fall_down", () -> new ItemFallDown(new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Block> BLOCK_4 = BLOCKS.register("block_4", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLOCK_4_ITEM = ITEMS.register("block_4", () -> new BlockItem(BLOCK_4.get(), new Item.Properties()));

    public static final RegistryObject<BlockPuzzleControlManager> PUZZLE_CONTROL_MANAGER = BLOCKS.register("puzzle_control_manager", () -> new BlockPuzzleControlManager(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> PUZZLE_CONTROL_MANAGER_ITEM = ITEMS.register("puzzle_control_manager", () -> new BlockItem(PUZZLE_CONTROL_MANAGER.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<BlockEntityPuzzleControlManager>> PUZZLE_CONTROL_MANAGER_ENTITY = BLOCK_ENTITIES.register("puzzle_control_manager",
            () -> BlockEntityType.Builder.of(BlockEntityPuzzleControlManager::new, PUZZLE_CONTROL_MANAGER.get()).build(null));
    public static PuzzleManager puzzleManager;

    public UnagiThreeBlockPuzzle()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        puzzleManager = new PuzzleManager();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void addCreative(CreativeModeTabEvent.BuildContents event)
    {
        if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(BLOCK_0_ITEM);
            event.accept(BLOCK_1_ITEM);
            event.accept(BLOCK_2_ITEM);
            event.accept(BLOCK_3_ITEM);
            event.accept(BLOCK_4_ITEM);
            event.accept(PUZZLE_CONTROL_MANAGER_ITEM);
        }else if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PUZZLE_PUT_KEY);
            event.accept(MOVE_LEFT);
            event.accept(MOVE_RIGHT);
            event.accept(ROTATION);
            event.accept(FALL_DOWN);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
