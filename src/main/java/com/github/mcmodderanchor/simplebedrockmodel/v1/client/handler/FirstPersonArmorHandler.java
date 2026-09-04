package com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderArmEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPArmorHandRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 通用的第一人称盔甲手臂渲染处理器。
 * 监听 RenderArmEvent，在玩家手臂上叠加渲染 Bedrock 盔甲模型的手臂部分。
 */
@Environment(EnvType.CLIENT)
public class FirstPersonArmorHandler {

    // Fabric's ArmorRendererRegistry keeps only the drawing lambda, so the renderer object itself is
    // unreachable from here; mods publish it separately (NeoForge took it from getHumanoidArmorModel).
    private static final Map<Item, Supplier<? extends IFPArmorHandRenderer>> CHEST_RENDERERS = new HashMap<>();

    private static HumanoidModel<?> defaultModel;

    public static void register(Item item, Supplier<? extends IFPArmorHandRenderer> renderer) {
        CHEST_RENDERERS.put(item, renderer);
    }

    private static HumanoidModel<?> getDefaultModel() {
        if (defaultModel == null) {
            defaultModel = new HumanoidModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)
            );
            defaultModel.young = false;
        }
        return defaultModel;
    }

    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

        Supplier<? extends IFPArmorHandRenderer> supplier = CHEST_RENDERERS.get(chestStack.getItem());
        if (supplier == null) return;

        IFPArmorHandRenderer armorRenderer = supplier.get();
        armorRenderer.preparePose(player, chestStack, EquipmentSlot.CHEST, getDefaultModel());
        armorRenderer.renderFirstPersonArmorArm(
                player,
                event.getArm(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }
}
