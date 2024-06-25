package com.petrolpark.destroy.compat.createbigcannons.block;

import static com.petrolpark.destroy.Destroy.REGISTRATE;

import com.petrolpark.destroy.compat.createbigcannons.item.CustomExplosiveMixChargeBlockItem;
import com.tterrag.registrate.util.entry.BlockEntry;

import rbasamoyai.createbigcannons.index.CBCBlocks;

public class CreateBigCannonsBlocks {

    public static final BlockEntry<CustomExplosiveMixChargeBlock> CUSTOM_EXPLOSIVE_MIX_CHARGE = REGISTRATE.block("custom_explosive_mix_charge", CustomExplosiveMixChargeBlock::new)
        .initialProperties(CBCBlocks.POWDER_CHARGE)
        .properties(p -> p
            .noLootTable() // Handled in CustomExplosiveMixChargeBlock class
        ).item(CustomExplosiveMixChargeBlockItem::new)
        .build()
        .register();

    public static final BlockEntry<CustomExplosiveMixShellBlock> CUSTOM_EXPLOSIVE_MIX_SHELL = REGISTRATE.block("custom_explosive_mix_shell", CustomExplosiveMixShellBlock::new)
        .initialProperties(CBCBlocks.FLUID_SHELL)
        .properties(p -> p
            .noLootTable() // Handled in CustomExplosiveMixChargeBlock class
        ).item(CustomExplosiveMixChargeBlockItem::new)
        .build()
        .register();    
    
    public static void register() {};
};