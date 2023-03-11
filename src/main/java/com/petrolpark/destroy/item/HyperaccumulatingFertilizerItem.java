package com.petrolpark.destroy.item;

import com.petrolpark.destroy.Destroy;
import com.petrolpark.destroy.util.CropMutation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HyperaccumulatingFertilizerItem extends BoneMealItem {

    public HyperaccumulatingFertilizerItem(Properties properties) {
        super(properties);
    };

    /**
     * Attempts to grow (with Hyperaccumulating Fertilizer) the crop at the given position.
     * @param level The Level the crop is in
     * @param cropPos The position of the crop
     * @return Whether the crop could be successfully grown
     */
    private static boolean grow(Level level, BlockPos cropPos) {

        BlockState cropState = level.getBlockState(cropPos);
        Block cropBlock = cropState.getBlock();
        BlockPos potentialOrePos = cropPos.below(2);
        BlockState potentialOreState = level.getBlockState(potentialOrePos);

        if (cropBlock instanceof BonemealableBlock && cropState.is(BlockTags.CROPS)) {
            CropMutation mutation = CropMutation.getMutation(cropState, potentialOreState);
            if (mutation.isSuccessful()) {
                if (level.isClientSide()) {
                    addGrowthParticles(level, cropPos, 100);
                    return true;
                };
                level.setBlockAndUpdate(cropPos, mutation.getResultantCropSupplier().get());
                if (mutation.isOreSpecific()) {
                    level.setBlockAndUpdate(potentialOrePos, mutation.getResultantBlockUnder(potentialOreState));
                };
                return true;
            };
        };
        return false;
    };

    @Override
    @SuppressWarnings("null")
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        boolean couldGrow = grow(level, context.getClickedPos()); // Try grow the Crop
        if (couldGrow && !level.isClientSide() && context.getPlayer() != null && !context.getPlayer().isCreative()) { // If necessary, use up one Hyperaccumulating Fertilizer
            context.getItemInHand().shrink(1);
        };
        if (couldGrow) return InteractionResult.SUCCESS;
        return super.useOn(context);
    };

    @SuppressWarnings("deprecation") // BoneMealItem.growCrop() is deprecated but it's used in the Bone Meal Dispenser Behaviour so I don't care
    public static void registerDispenserBehaviour() {
        DispenserBlock.registerBehavior(DestroyItems.HYPERACCUMULATING_FERTILIZER.get(), new OptionalDispenseItemBehavior() {
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                Level level = blockSource.getLevel();
                BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
                if (grow(level, blockPos)) { // Try to grow it as Hyperaccumulating Fertilizer
                    stack.shrink(1);
                    this.setSuccess(true);
                } else if (BoneMealItem.growCrop(stack, level, blockPos) || BoneMealItem.growWaterPlant(stack, level, blockPos, (Direction)null)) { // Try to grow as normal Bonemeal
                    // Shrinking the Stack is covered in BoneMealItem.growCrop() or .growWaterPlant()
                    this.setSuccess(true);
                    if (!level.isClientSide()) {
                        level.levelEvent(1505, blockPos, 0); // Don't really know what this does but it's in the Bone Meal Dispenser Behaviour so best to include it
                    };
                } else {
                    this.setSuccess(false);
                };
                return stack;
            };
        });
        Destroy.LOGGER.info("Registered Dispenser Behaviour for Hyperacummulating Fertilizer.");
    };
    
};