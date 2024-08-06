package com.petrolpark.destroy.chemistry.legacy.index.genericreaction;

import com.petrolpark.destroy.chemistry.legacy.LegacyMolecularStructure;
import com.petrolpark.destroy.chemistry.legacy.LegacySpecies;
import com.petrolpark.destroy.chemistry.legacy.LegacyReaction;
import com.petrolpark.destroy.chemistry.legacy.LegacyBond.BondType;
import com.petrolpark.destroy.chemistry.legacy.LegacyReaction.ReactionBuilder;
import com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReactant;
import com.petrolpark.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import com.petrolpark.destroy.chemistry.legacy.index.DestroyGroupTypes;
import com.petrolpark.destroy.chemistry.legacy.index.group.SaturatedCarbonGroup;

import net.minecraft.resources.ResourceLocation;

public abstract class ElectrophilicAddition extends SingleGroupGenericReaction<SaturatedCarbonGroup> {

    public final boolean isForAlkynes; // Whether this is a Reaction involving alkynes - if not, it involves alkenes

    public ElectrophilicAddition(String namespace, String name, boolean alkyne) {
        super(new ResourceLocation(namespace, (alkyne ? "alkyne_" : "alkene_") + name), alkyne ? DestroyGroupTypes.ALKYNE : DestroyGroupTypes.ALKENE);
        isForAlkynes = alkyne;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<SaturatedCarbonGroup> reactant) {
        SaturatedCarbonGroup group = reactant.getGroup();
        LegacySpecies substrate = reactant.getMolecule();
        
        LegacySpecies product = moleculeBuilder().structure(substrate
            .shallowCopyStructure()
            .moveTo(group.highDegreeCarbon)
            .replaceBondTo(group.lowDegreeCarbon, isForAlkynes ? BondType.DOUBLE : BondType.SINGLE)
            .addGroup(getHighDegreeGroup(), true)
            .moveTo(group.lowDegreeCarbon)
            .addGroup(getLowDegreeGroup(), true)
        ).build();

        ReactionBuilder builder = reactionBuilder()
            .addReactant(substrate)
            .addProduct(product)
            .activationEnergy(isForAlkynes ? 10f : 25f);
        transform(builder);
        return builder.build();
    };

    /**
     * The group to be added to the carbon with the lower degree.
     */
    public abstract LegacyMolecularStructure getLowDegreeGroup();
    
    /**
     * The group to be added to the carbon with the lower degree.
     */
    public abstract LegacyMolecularStructure getHighDegreeGroup();

    /**
     * Add any other necessary products, reactants, catalysts and rate constants to the Reaction.
     * @param builder The builder with the Alkene reactant and product already added
     */
    public abstract void transform(ReactionBuilder builder);
    
};