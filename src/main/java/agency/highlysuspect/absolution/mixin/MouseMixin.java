package agency.highlysuspect.absolution.mixin;

import agency.highlysuspect.absolution.ClientInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
	@Shadow private boolean cursorLocked;
	@Shadow @Final private MinecraftClient client;
	@Shadow private boolean hasResolutionChanged;
	@Shadow private double x;
	@Shadow private double y;
	
	@Shadow private double cursorDeltaX;
	
	@Shadow private double cursorDeltaY;
	
	/**
	 * It doesn't make sense to "lock" the cursor of an absolute pointing device.
	 * @author quaternary
	 */
	@Overwrite
	public void lockCursor() {
		//btw this is the ol "copy-paste overwrite"
		// TODO make it a good mixin (although i'm not really sure what for)
		
		if (this.client.isWindowFocused()) {
			if (!this.cursorLocked) {
				if (!MinecraftClient.IS_SYSTEM_MAC) {
					KeyBinding.updatePressedStates();
				}
				
				this.cursorLocked = true;
				
				// Do not change the cursor position
				//this.x = (double)(this.client.getWindow().getWidth() / 2);
				//this.y = (double)(this.client.getWindow().getHeight() / 2);
				
				// Merely hide the cursor instead of "disabling" it
				InputUtil.setCursorParameters(this.client.getWindow().getHandle(), GLFW.GLFW_CURSOR_HIDDEN, this.x, this.y);
				this.client.openScreen(null);
				
				// This has protected access and i dont' wanna AW it lmao hope this works
				//this.client.attackCooldown = 10000;
				
				this.hasResolutionChanged = true;
			}
		}
	}
	
	/**
	 * It doesn't make sense to "unlock" the cursor of an absolute pointing device.
	 * @author quaternary
	 */
	@Overwrite
	public void unlockCursor() {
		if (this.cursorLocked) {
			this.cursorLocked = false;
			// Do not change the cursor position
			//this.x = (double)(this.client.getWindow().getWidth() / 2);
			//this.y = (double)(this.client.getWindow().getHeight() / 2);
			InputUtil.setCursorParameters(this.client.getWindow().getHandle(), GLFW.GLFW_CURSOR_NORMAL, this.x, this.y);
		}
	}
	
	@Inject(
		method = "updateMouse",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/options/GameOptions;mouseSensitivity:D"
		),
		cancellable = true
	)
	private void onUpdateMouse(CallbackInfo ci) {
		ci.cancel();
		
		cursorDeltaX = 0;
		cursorDeltaY = 0;
		
		if(Screen.hasAltDown()) return;
		
		Window window = client.getWindow();
		double mx, my, width, height;
		if(ClientInit.CIRCLE_USES_SCALED_PIXELS) {
			mx = x / window.getScaleFactor();
			my = y / window.getScaleFactor();
			width = window.getScaledWidth();
			height = window.getScaledHeight();
		} else {
			mx = x;
			my = y;
			width = window.getWidth();
			height = window.getHeight();
		}
		
		double circleX = width / 2;
		double circleY = height / 2;
		
		double distance = Math.sqrt((mx - circleX) * (mx - circleX) + (my - circleY) * (my - circleY));
		if(distance < ClientInit.CIRCLE_RADIUS) return;
		
		double angleBetween = MathHelper.atan2(my - circleY, mx - circleX);
		
		double dYaw = Math.cos(angleBetween) * (distance - ClientInit.CIRCLE_RADIUS) * ClientInit.SENSITIVITY;
		double dPitch = Math.sin(angleBetween) * (distance - ClientInit.CIRCLE_RADIUS) * ClientInit.SENSITIVITY;
		
		if(client.player != null) {
			client.player.changeLookDirection(dYaw, dPitch);
		}
	}
}
