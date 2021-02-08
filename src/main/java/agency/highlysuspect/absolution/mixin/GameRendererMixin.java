package agency.highlysuspect.absolution.mixin;

import agency.highlysuspect.absolution.ClientInit;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Redirect(
		method = "updateTargetedEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"
		)
	)
	private HitResult raycastBois(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
		return ClientInit.raycastFudged(entity, maxDistance, tickDelta, includeFluids);
	}
	
	@Redirect(
		method = "updateTargetedEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"
		)
	)
	private Vec3d lookBois(Entity entity, float tickDelta) {
		return ClientInit.fudgeLookVec(entity.getRotationVec(tickDelta));
	}
}
