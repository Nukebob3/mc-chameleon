package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.accessor.ArmWidthSetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
	private void getTextureLocation(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
		if (Minecraft.getInstance().level!=null) {
			Entity entity = Minecraft.getInstance().level.getEntity(state.id);
			if (entity != null) {
				String uuid = entity.getStringUUID();
				if (Minecraft.getInstance().getTextureManager().getTexture(MCChameleon.idSkin(uuid)) instanceof DynamicTexture) {
					if(!ArmWidthSetter.CANVAS_SKINS_LIST.contains(state.skin)) ArmWidthSetter.CANVAS_SKINS_LIST.add(state.skin);
					cir.setReturnValue(MCChameleon.idSkin(uuid));
				} else {
					ArmWidthSetter.CANVAS_SKINS_LIST.remove(state.skin);
				}
			}
		}
	}
}