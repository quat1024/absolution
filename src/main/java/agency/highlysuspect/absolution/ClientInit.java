package agency.highlysuspect.absolution;

import agency.highlysuspect.absolution.mixin.GameRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ClientInit implements ClientModInitializer {
	//put these in a config file
	public static double CIRCLE_RADIUS = 50d;
	public static boolean CIRCLE_USES_SCALED_PIXELS = true;
	public static double SENSITIVITY = 0.06d;
	
	@Override
	public void onInitializeClient() {
		
	}
	
	public static Vec3d fudgeLookVec(Vec3d in) {
		//TODO This is the wrong approach.
		// It works ok for small angles, but it basically maps X motion directly to yaw and Y motion directly to pitch.
		// That's not true, you have to change both pitch *and* yaw to point the camera at the thing on the left-center of your screen if you're not looking at the horizon.
		// oof
		// Also, it is based on the false assumption that i could add directly to the X and Y components of the look vector.
		// That is not how look vectors work at all lol. I wrote this without looking at Entity#getRotationVec (which does some trig)
		
		MinecraftClient client = MinecraftClient.getInstance();
		Window window = client.getWindow();
		Mouse mouse = client.mouse;
		
		//Mouse X and Y in the range -1, 1
		double xRemapped = (mouse.getX() / window.getWidth()) * 2 - 1;
		double yRemapped = (mouse.getY() / window.getHeight()) * 2 - 1;
		
		//How do I explain this
		//Remap so the bottom end of the range is the farthest negative you can look, and the top end of the range is the farthest positive you can look
		//so like if you have 90 degree fov, the left end of the range is -45 degrees and the right is 45 degrees
		//also same for y (but i need to calculate vertical fov)
		double xFov = getCameraHorizontalFov();
		double yFov = xFov * ((double) window.getFramebufferHeight() / window.getFramebufferWidth());
		xRemapped *= xFov;
		yRemapped *= yFov;
		
		//Uhh fuck idk, just add it? Maybe it'll even work?
		return in.add(xRemapped, yRemapped, 0);
	}
	
	public static double getCameraHorizontalFov() {
		MinecraftClient client = MinecraftClient.getInstance();
		float tickDelta = client.getTickDelta();
		GameRenderer renderer = client.gameRenderer;
		Camera camera = renderer.getCamera();
		
		return ((GameRendererAccessor) renderer).callGetFov(camera, tickDelta, true);
	}
	
	//Based on copy of Entity#raycast.
	//Slapped in the middle of this class instead of a mixin so i can hotreload it
	public static HitResult raycastFudged(Entity receiver, double maxDistance, float tickDelta, boolean includeFluids) {
		//Todo this method is fucked up lmao
		Vec3d cameraPos = receiver.getCameraPosVec(tickDelta);
		//Vec3d look = receiver.getRotationVec(tickDelta);
		
		///uhh idk based on internals of getrotationvec
		
		Vec3d funny = fudgeLookVec(new Vec3d(0, 0, 0)); //sorry
		//JUST FLIP THE SIGNS AROUND UNTIL SHIT WORKS
		double f = (receiver.getPitch(tickDelta) + funny.y) * 0.017453292d;
		double g = (-receiver.getYaw(tickDelta) - funny.x) * 0.017453292d;
		double h = Math.cos(g);
		double i = Math.sin(g);
		double j = Math.cos(f);
		double k = Math.sin(f);
		Vec3d look = new Vec3d(i * j, -k, h * j);
		
		Vec3d ray = cameraPos.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
		return receiver.world.raycast(new RaycastContext(cameraPos, ray, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, receiver));
	}
}
