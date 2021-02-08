package agency.highlysuspect.absolution.mixin;

import agency.highlysuspect.absolution.ClientInit;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow private int scaledWidth;
	@Shadow private int scaledHeight;
	
	@Inject(
		method = "renderCrosshair",
		at = @At("HEAD")
	)
	private void crosshairPre(MatrixStack matrices, CallbackInfo ci) {
		double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		Mouse mouse = MinecraftClient.getInstance().mouse;
		
		//Using RenderSystem on purpose.
		//The f3 "axes" debug cursor calls RenderSystem directly instead of using matrix stack. 
		
		RenderSystem.pushMatrix();
		RenderSystem.translated(-scaledWidth / 2d + mouse.getX() / scaleFactor, -scaledHeight / 2f + mouse.getY() / scaleFactor, 0);
	}
	
	@Inject(
		method = "renderCrosshair",
		at = @At("RETURN")
	)
	private void crosshairPost(MatrixStack matrices, CallbackInfo ci) {
		RenderSystem.popMatrix();
		//(if i ever bind a texture, remember to bind it back for bossbars btw)
		
		//Draw the circle while we're at it. Blend mode's already set up
		//TODO: Narrator: The blend mode was not already set up (try f1, f3, f5)
		//TODO it doesn't honor the scaledPixels setting yet either
		RenderSystem.disableTexture();
		RenderSystem.pushMatrix();
		RenderSystem.translated(scaledWidth / 2d, scaledHeight / 2d, 0);
		RenderSystem.scaled(ClientInit.CIRCLE_RADIUS, ClientInit.CIRCLE_RADIUS, 1);
		RenderSystem.lineWidth(3f);
		
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION);
		
		int VERTEX_COUNT = 45;
		double RADS_PER_VERT = (Math.PI * 2 / VERTEX_COUNT);
		
		for(int i = 0; i < VERTEX_COUNT; i++) {
			double ang = RADS_PER_VERT * i;
			b.vertex(Math.cos(ang), Math.sin(ang), 0).next();
		}
		//closing vertex
		b.vertex(1, 0, 0).next();
		
		t.draw();
		
		RenderSystem.popMatrix();
		RenderSystem.enableTexture();
	}
}
