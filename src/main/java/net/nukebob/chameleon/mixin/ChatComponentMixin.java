package net.nukebob.chameleon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyArg(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"),
            index = 1
    )
    private int mc_chameleon$modifyChatScreenHeight(int screenHeight) {
        return 50; // adjust: chatBottom = floor((50-40)/scale) = ~10px from top
    }

    @ModifyArg(
            method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;updatePose(Ljava/util/function/Consumer;)V")
    )
    private Consumer<Matrix3x2f> mc_chameleon$moveChat(Consumer<Matrix3x2f> original) {
        return pose -> {
            int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
            float scale = this.minecraft.options.chatScale().get().floatValue();
            // no original.accept — we set scale ourselves
            pose.scale(scale, scale);
            pose.translate((screenWidth / 2 + 30+2) / scale, (screenHeight / 2 - 77 - 15 - 13f) / scale);
        };
    }

    @WrapOperation(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(F)I"))
    private int mc_chameleon$setMaxWidth(float v, Operation<Integer> original) {
        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        float scale = this.minecraft.options.chatScale().get().floatValue();
        float chatStartX = screenWidth / 2 + 30 - 2;
        //return (int) ((screenWidth - chatStartX) / scale);*/
        return (int) ((screenWidth-chatStartX-16)/scale);
    }

    @Inject(method = "getWidth()I", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$overrideWidth(CallbackInfoReturnable<Integer> cir) {
        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        float chatStartX = screenWidth / 2f + 30 - 2;
        cir.setReturnValue((int)(screenWidth - chatStartX - 16));
    }
}
