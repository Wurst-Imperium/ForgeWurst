/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPostMotionEvent;
import net.wurstclient.fmlevents.WPreMotionEvent;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WVec3d;

@Mod.EventBusSubscriber
public final class RotationUtils
{
	private static boolean fakeRotation;
	private static float serverYaw;
	private static float serverPitch;
	private static float realYaw;
	private static float realPitch;
	
	@SubscribeEvent
	public static void onPreMotion(WPreMotionEvent event)
	{
		if(!fakeRotation)
			return;
		
		EntityPlayer player = event.getPlayer();
		realYaw = player.rotationYaw;
		realPitch = player.rotationPitch;
		player.rotationYaw = serverYaw;
		player.rotationPitch = serverPitch;
	}
	
	@SubscribeEvent
	public static void onPostMotion(WPostMotionEvent event)
	{
		if(!fakeRotation)
			return;
		
		EntityPlayer player = event.getPlayer();
		player.rotationYaw = realYaw;
		player.rotationPitch = realPitch;
		fakeRotation = false;
	}
	
	public static Vec3d getEyesPos()
	{
		return new Vec3d(WMinecraft.getPlayer().posX,
			WMinecraft.getPlayer().posY + WMinecraft.getPlayer().getEyeHeight(),
			WMinecraft.getPlayer().posZ);
	}
	
	public static Vec3d getClientLookVec()
	{
		EntityPlayerSP player = WMinecraft.getPlayer();
		
		float f =
			MathHelper.cos(-player.rotationYaw * 0.017453292F - (float)Math.PI);
		float f1 =
			MathHelper.sin(-player.rotationYaw * 0.017453292F - (float)Math.PI);
		
		float f2 = -MathHelper.cos(-player.rotationPitch * 0.017453292F);
		float f3 = MathHelper.sin(-player.rotationPitch * 0.017453292F);
		
		return new Vec3d(f1 * f2, f3, f * f2);
	}
	
	private static float[] getNeededRotations(Vec3d vec)
	{
		Vec3d eyesPos = getEyesPos();
		
		double diffX = WVec3d.getX(vec) - WVec3d.getX(eyesPos);
		double diffY = WVec3d.getY(vec) - WVec3d.getY(eyesPos);
		double diffZ = WVec3d.getZ(vec) - WVec3d.getZ(eyesPos);
		
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		
		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));
		
		return new float[]{MathHelper.wrapDegrees(yaw),
			MathHelper.wrapDegrees(pitch)};
	}
	
	public static double getAngleToLookVec(Vec3d vec)
	{
		float[] needed = getNeededRotations(vec);
		
		EntityPlayerSP player = WMinecraft.getPlayer();
		float currentYaw = MathHelper.wrapDegrees(player.rotationYaw);
		float currentPitch = MathHelper.wrapDegrees(player.rotationPitch);
		
		float diffYaw = currentYaw - needed[0];
		float diffPitch = currentPitch - needed[1];
		
		return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
	}
	
	public static double getAngleToLastReportedLookVec(Vec3d vec)
	{
		float[] needed = getNeededRotations(vec);
		
		EntityPlayerSP player = WMinecraft.getPlayer();
		float lastReportedYaw;
		float lastReportedPitch;
		try
		{
			Field yawField = EntityPlayerSP.class
				.getDeclaredField(ForgeWurst.getForgeWurst().isObfuscated()
					? "field_175164_bL" : "lastReportedYaw");
			yawField.setAccessible(true);
			lastReportedYaw = MathHelper.wrapDegrees(yawField.getFloat(player));
			
			Field pitchField = EntityPlayerSP.class
				.getDeclaredField(ForgeWurst.getForgeWurst().isObfuscated()
					? "field_175165_bM" : "lastReportedPitch");
			pitchField.setAccessible(true);
			lastReportedPitch =
				MathHelper.wrapDegrees(pitchField.getFloat(player));
			
		}catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
		
		float diffYaw = lastReportedYaw - needed[0];
		float diffPitch = lastReportedPitch - needed[1];
		
		return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
	}
	
	public static boolean faceVectorPacket(Vec3d vec)
	{
		float[] rotations = getNeededRotations(vec);
		
		fakeRotation = true;
		serverYaw = rotations[0];
		serverPitch = rotations[1];
		
		return Math.abs(serverYaw - rotations[0]) < 1F;
	}
	
	public static void faceVectorForWalking(Vec3d vec)
	{
		float[] needed = getNeededRotations(vec);
		
		EntityPlayerSP player = WMinecraft.getPlayer();
		player.rotationYaw = needed[0];
		player.rotationPitch = 0;
	}
}
