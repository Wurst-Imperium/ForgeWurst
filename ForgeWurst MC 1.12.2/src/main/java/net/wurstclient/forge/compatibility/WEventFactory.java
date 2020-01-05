/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.wurstclient.fmlevents.*;

@Mod.EventBusSubscriber
public final class WEventFactory
{
	@SubscribeEvent
	public static void onPlayerPreTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase != Phase.START)
			return;
		
		EntityPlayer player = event.player;
		if(player != WMinecraft.getPlayer())
			return;
		
		if(!WPlayer.getWorld(player).isRemote)
			return;
		
		MinecraftForge.EVENT_BUS.post(new WUpdateEvent((EntityPlayerSP)player));
	}
	
	@SubscribeEvent
	public static void onClientSentMessage(ClientChatEvent event)
	{
		WChatOutputEvent event2 =
			new WChatOutputEvent(event.getOriginalMessage());
		if(MinecraftForge.EVENT_BUS.post(event2))
			event.setCanceled(true);
		event.setMessage(event2.getMessage());
	}
	
	public static boolean onClientReceivedMessage(WChatInputEvent event)
	{
		return !MinecraftForge.EVENT_BUS.post(event);
	}
	
	public static Packet<?> onSendPacket(Packet<?> packet)
	{
		WPacketOutputEvent event = new WPacketOutputEvent(packet);
		return MinecraftForge.EVENT_BUS.post(event) ? null : event.getPacket();
	}
	
	public static boolean onReceivePacket(Packet<?> packet)
	{
		WPacketInputEvent event = new WPacketInputEvent(packet);
		return !MinecraftForge.EVENT_BUS.post(event);
	}
	
	public static void onPreMotion(EntityPlayerSP player)
	{
		MinecraftForge.EVENT_BUS.post(new WPreMotionEvent(player));
	}
	
	public static void onPostMotion(EntityPlayerSP player)
	{
		MinecraftForge.EVENT_BUS.post(new WPostMotionEvent(player));
	}
	
	public static void onPlayerMove(EntityPlayerSP player)
	{
		MinecraftForge.EVENT_BUS.post(new WPlayerMoveEvent(player));
	}
	
	public static boolean entityPlayerJump(EntityPlayer player)
	{
		return !MinecraftForge.EVENT_BUS
			.post(new WEntityPlayerJumpEvent(player));
	}
	
	public static boolean shouldLiquidBeSolid()
	{
		WGetLiquidCollisionBoxEvent event = new WGetLiquidCollisionBoxEvent();
		MinecraftForge.EVENT_BUS.post(event);
		return event.isSolidCollisionBox();
	}
	
	public static float getAmbientOcclusionLightValue(float f,
		IBlockState state)
	{
		WGetAmbientOcclusionLightValueEvent event =
			new WGetAmbientOcclusionLightValueEvent(state, f);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getLightValue();
	}
	
	public static boolean isNormalCube(IBlockState state)
	{
		return !MinecraftForge.EVENT_BUS.post(new WIsNormalCubeEvent(state));
	}
	
	public static boolean shouldSideBeRendered(boolean b, IBlockState state)
	{
		WShouldSideBeRenderedEvent event =
			new WShouldSideBeRenderedEvent(state, b);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isRendered();
	}
	
	public static boolean renderBlockModel(IBlockState state)
	{
		WRenderBlockModelEvent event = new WRenderBlockModelEvent(state);
		return !MinecraftForge.EVENT_BUS.post(event);
	}
	
	public static boolean renderTileEntity(TileEntity tileEntity)
	{
		WRenderTileEntityEvent event = new WRenderTileEntityEvent(tileEntity);
		return !MinecraftForge.EVENT_BUS.post(event);
	}
	
	public static boolean setOpaqueCube()
	{
		return !MinecraftForge.EVENT_BUS.post(new WSetOpaqueCubeEvent());
	}
	
	public static void onPlayerDamageBlock(BlockPos pos, EnumFacing facing)
	{
		MinecraftForge.EVENT_BUS.post(new WPlayerDamageBlockEvent(pos, facing));
	}
	
	public static boolean hurtCameraEffect()
	{
		return !MinecraftForge.EVENT_BUS.post(new WHurtCameraEffectEvent());
	}
	
	public static boolean cameraTransformViewBobbing()
	{
		return !MinecraftForge.EVENT_BUS
			.post(new WCameraTransformViewBobbingEvent());
	}
	
	public static void onGuiInventoryInit(List<GuiButton> buttonList)
	{
		MinecraftForge.EVENT_BUS
			.post(new WGuiInventoryButtonEvent.Init(buttonList));
	}
	
	public static void onGuiInventoryButtonPress(GuiButton button)
	{
		MinecraftForge.EVENT_BUS
			.post(new WGuiInventoryButtonEvent.Press(button));
	}
}
