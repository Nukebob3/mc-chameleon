package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.nukebob.chameleon.MCChameleon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void getTextureLocation(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
		if (Minecraft.getInstance().level!=null) {
			Entity entity = Minecraft.getInstance().level.getEntity(state.id);
			if (entity != null) {
				String uuid = entity.getStringUUID();
				if(Minecraft.getInstance().getResourceManager().getResource(MCChameleon.idSkin(uuid)).isPresent())
					cir.setReturnValue(MCChameleon.idSkin(uuid));
			}
		}
	}

	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static boolean forceWide(boolean slimSteve) {
		return false;
	}
}