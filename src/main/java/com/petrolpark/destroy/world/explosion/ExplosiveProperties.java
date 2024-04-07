package com.petrolpark.destroy.world.explosion;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.petrolpark.destroy.util.DestroyLang;
import com.petrolpark.destroy.util.DestroyReloadListener;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

public class ExplosiveProperties extends EnumMap<ExplosiveProperties.ExplosiveProperty, Float> {

    public static final Map<Item, ExplosiveProperties> ITEM_EXPLOSIVE_PROPERTIES = new HashMap<>();

    public static final ExplosivePropertyCondition

    CAN_EXPLODE = new ExplosivePropertyCondition(ExplosiveProperty.SENSITIVTY, 0f, "destroy.explosion_condition.can_explode"),
    EXPLODES_RANDOMLY = new ExplosivePropertyCondition(ExplosiveProperty.SENSITIVTY, 10f, "destroy.explosion_condition.explodes_randomly"),
    EVAPORATES_FLUIDS = new ExplosivePropertyCondition(ExplosiveProperty.TEMPERATURE, 5f, "destroy.explosion_condition.evaporates_fluids"),
    OBLITERATES = new ExplosivePropertyCondition(ExplosiveProperty.BRISANCE, 5f, "destroy.explosion_condition.obliterates"),
    SILK_TOUCH = new ExplosivePropertyCondition(ExplosiveProperty.BRISANCE, -5f, "destroy.explosion_condition.silk_touch");

    public Set<ExplosivePropertyCondition> conditions = new HashSet<>();
  
    public ExplosiveProperties() {
        super(Arrays.stream(ExplosiveProperty.values()).collect(Collectors.toMap(p -> p, p -> 0f)));
    };

    public ExplosiveProperties withConditions(ExplosivePropertyCondition ...conditions) {
        this.conditions.clear();
        for (ExplosivePropertyCondition condition : conditions) this.conditions.add(condition);
        return this;
    };

    public boolean fulfils(ExplosivePropertyCondition condition) {
        if (!conditions.contains(condition)) return false;
        float value = get(condition.property);
        return condition.negative() ? value <= condition.threshhold : value >= condition.threshhold;
    };

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        forEach((p, f) -> {
            if (f != 0f) tag.putFloat(p.name(), f);
        });
        return tag;
    };

    public static ExplosiveProperties read(CompoundTag tag) {
        ExplosiveProperties properties = new ExplosiveProperties();
        for (ExplosiveProperty property : ExplosiveProperty.values()) {
            properties.put(property, tag.getFloat(property.name()));
        };
        return properties;
    };

    public static ExplosiveProperties fromJson(JsonObject json) {
        ExplosiveProperties properties = new ExplosiveProperties();
        for (ExplosiveProperty property : ExplosiveProperty.values()) {
            if (json.has(property.name())) {
                try { properties.put(property, json.get(property.name()).getAsFloat()); } catch (Throwable e) {};
            };
        };
        return properties;
    };

    public static class ExplosivePropertyCondition {

        public final ExplosiveProperty property;
        public final float threshhold;
        protected final String translationKey;

        public ExplosivePropertyCondition(ExplosiveProperty property, float threshhold, String translationKey) {
            this.property = property;
            this.threshhold = threshhold;
            this.translationKey = translationKey;
        };

        public boolean negative() {
            return threshhold < 0f;
        };

        public Component getDescription() {
            return Component.translatable(translationKey);
        };
    };

    public static enum ExplosiveProperty {

        ENERGY,
        OXYGEN_BALANCE,
        TEMPERATURE,
        BRISANCE,
        SENSITIVTY;

        public Component getName() {
            return DestroyLang.translate("explosive_property."+name()).component();
        };
    };

    public static class Listener extends DestroyReloadListener {

        public final IContext context;

        public Listener(IContext context) {
            super();
            this.context = context;
        };

        @Override
        public String getPath() {
            return "destroy_compat/explosive_items";
        };

        @Override
        public void beforeReload() {
            ITEM_EXPLOSIVE_PROPERTIES.clear();
            super.beforeReload();
        };

        @Override
        @SuppressWarnings("deprecation")
        public void forEachNameSpaceJsonFile(JsonObject jsonObject) {
            for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                JsonObject object = entry.getValue().getAsJsonObject();
                if (!CraftingHelper.processConditions(object, "conditions", this.context)) return;

                Optional<? extends Holder<Item>> itemOptional = BuiltInRegistries.ITEM.asLookup().get(ResourceKey.create(Registries.ITEM, new ResourceLocation(entry.getKey())));
                if (itemOptional.isEmpty()) throw new IllegalStateException("Invalid item ID: "+entry.getKey());
                ITEM_EXPLOSIVE_PROPERTIES.put(itemOptional.get().value(), fromJson(object));
            };
        };

    };
};