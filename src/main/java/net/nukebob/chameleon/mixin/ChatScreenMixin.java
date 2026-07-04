package net.nukebob.chameleon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Inject(method = "init", at = @At("RETURN"))
    private void mc_chameleon$repositionInput(CallbackInfo ci) {
        ChatScreen self = (ChatScreen)(Object)this;
        this.input.setX(self.width/2+30);
        this.input.setY(self.height/2-77-15);
        this.input.setWidth(self.width- self.width/2-30 -4);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void mc_chameleon$fillMovedChat(GuiGraphicsExtractor instance, int x0, int y0, int x1, int y1, int col, Operation<Void> original) {
        original.call(instance, input.getX()-2, input.getY()-2, input.getX()+input.getWidth(), input.getY()+input.getHeight(), col);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
    private void mc_chameleon$moveSuggestions(CommandSuggestions instance, GuiGraphicsExtractor graphics, int mouseX, int mouseY, Operation<Void> original) {
        ChatScreen self = (ChatScreen)(Object)this;
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, -self.height/2+12-77-15);
        original.call(instance, graphics, mouseX, mouseY);
        graphics.pose().popMatrix();
    }
}
