/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WCameraTransformViewBobbingEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.compatibility.WVec3d;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class ChestEspHack extends Hack
{
	private final CheckboxSetting tracers =
		new CheckboxSetting("Tracers", "Draws lines to chests.", false);
	
	private final ArrayList<AxisAlignedBB> basicChests = new ArrayList<>();
	private final ArrayList<AxisAlignedBB> trappedChests = new ArrayList<>();
	private final ArrayList<AxisAlignedBB> enderChests = new ArrayList<>();
	private final ArrayList<Entity> minecarts = new ArrayList<>();
	
	private int greenBox;
	private int orangeBox;
	private int cyanBox;
	private int normalChests;
	
	public ChestEspHack()
	{
		super("ChestESP",
			"Highlights nearby chests.\n"
				+ "\u00a7agreen\u00a7r - normal chests\n"
				+ "\u00a76orange\u00a7r - trapped chests\n"
				+ "\u00a7bcyan\u00a7r - ender chests");
		setCategory(Category.RENDER);
		addSetting(tracers);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		AxisAlignedBB bb = new AxisAlignedBB(BlockPos.ORIGIN);
		
		greenBox = GL11.glGenLists(1);
		GL11.glNewList(greenBox, GL11.GL_COMPILE);
		GL11.glColor4f(0, 1, 0, 0.25F);
		GL11.glBegin(GL11.GL_QUADS);
		RenderUtils.drawSolidBox(bb);
		GL11.glEnd();
		GL11.glColor4f(0, 1, 0, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(bb);
		GL11.glEnd();
		GL11.glEndList();
		
		orangeBox = GL11.glGenLists(1);
		GL11.glNewList(orangeBox, GL11.GL_COMPILE);
		GL11.glColor4f(1, 0.5F, 0, 0.25F);
		GL11.glBegin(GL11.GL_QUADS);
		RenderUtils.drawSolidBox(bb);
		GL11.glEnd();
		GL11.glColor4f(1, 0.5F, 0, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(bb);
		GL11.glEnd();
		GL11.glEndList();
		
		cyanBox = GL11.glGenLists(1);
		GL11.glNewList(cyanBox, GL11.GL_COMPILE);
		GL11.glColor4f(0, 1, 1, 0.25F);
		GL11.glBegin(GL11.GL_QUADS);
		RenderUtils.drawSolidBox(bb);
		GL11.glEnd();
		GL11.glColor4f(0, 1, 1, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(bb);
		GL11.glEnd();
		GL11.glEndList();
		
		normalChests = GL11.glGenLists(1);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		
		GL11.glDeleteLists(greenBox, 1);
		greenBox = 0;
		GL11.glDeleteLists(orangeBox, 1);
		orangeBox = 0;
		GL11.glDeleteLists(cyanBox, 1);
		cyanBox = 0;
		GL11.glDeleteLists(normalChests, 1);
		normalChests = 0;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		World world = WPlayer.getWorld(event.getPlayer());
		
		basicChests.clear();
		trappedChests.clear();
		enderChests.clear();
		
		for(TileEntity tileEntity : world.loadedTileEntityList)
			if(tileEntity instanceof TileEntityChest)
			{
				// ignore other block in double chest
				TileEntityChest chest = (TileEntityChest)tileEntity;
				if(chest.adjacentChestXPos != null
					|| chest.adjacentChestZPos != null)
					continue;
				
				// get hitbox
				BlockPos pos = chest.getPos();
				AxisAlignedBB bb = BlockUtils.getBoundingBox(pos);
				
				// larger box for double chest
				if(chest.adjacentChestXNeg != null)
				{
					BlockPos pos2 = chest.adjacentChestXNeg.getPos();
					AxisAlignedBB bb2 = BlockUtils.getBoundingBox(pos2);
					bb = bb.union(bb2);
					
				}else if(chest.adjacentChestZNeg != null)
				{
					BlockPos pos2 = chest.adjacentChestZNeg.getPos();
					AxisAlignedBB bb2 = BlockUtils.getBoundingBox(pos2);
					bb = bb.union(bb2);
				}
				
				// add to appropriate list
				switch(chest.getChestType())
				{
					case BASIC:
					basicChests.add(bb);
					break;
					
					case TRAP:
					trappedChests.add(bb);
					break;
				}
				
			}else if(tileEntity instanceof TileEntityEnderChest)
			{
				BlockPos pos = ((TileEntityEnderChest)tileEntity).getPos();
				AxisAlignedBB bb = BlockUtils.getBoundingBox(pos);
				enderChests.add(bb);
			}
		
		GL11.glNewList(normalChests, GL11.GL_COMPILE);
		renderBoxes(basicChests, greenBox);
		renderBoxes(trappedChests, orangeBox);
		renderBoxes(enderChests, cyanBox);
		GL11.glEndList();
		
		// minecarts
		minecarts.clear();
		for(Entity entity : world.loadedEntityList)
			if(entity instanceof EntityMinecartChest)
				minecarts.add(entity);
	}
	
	@SubscribeEvent
	public void onCameraTransformViewBobbing(
		WCameraTransformViewBobbingEvent event)
	{
		if(tracers.isChecked())
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		
		// minecart interpolation
		double partialTicks = event.getPartialTicks();
		ArrayList<AxisAlignedBB> minecartBoxes =
			new ArrayList<>(minecarts.size());
		minecarts.forEach(e -> {
			double offsetX = -(e.posX - e.lastTickPosX)
				+ (e.posX - e.lastTickPosX) * partialTicks;
			double offsetY = -(e.posY - e.lastTickPosY)
				+ (e.posY - e.lastTickPosY) * partialTicks;
			double offsetZ = -(e.posZ - e.lastTickPosZ)
				+ (e.posZ - e.lastTickPosZ) * partialTicks;
			minecartBoxes.add(
				e.getRenderBoundingBox().offset(offsetX, offsetY, offsetZ));
		});
		
		GL11.glCallList(normalChests);
		renderBoxes(minecartBoxes, greenBox);
		
		if(tracers.isChecked())
		{
			Vec3d start = RotationUtils.getClientLookVec()
				.addVector(0, WMinecraft.getPlayer().getEyeHeight(), 0)
				.addVector(TileEntityRendererDispatcher.staticPlayerX,
					TileEntityRendererDispatcher.staticPlayerY,
					TileEntityRendererDispatcher.staticPlayerZ);
			
			GL11.glBegin(GL11.GL_LINES);
			
			GL11.glColor4f(0, 1, 0, 0.5F);
			renderLines(start, basicChests);
			renderLines(start, minecartBoxes);
			
			GL11.glColor4f(1, 0.5F, 0, 0.5F);
			renderLines(start, trappedChests);
			
			GL11.glColor4f(0, 1, 1, 0.5F);
			renderLines(start, enderChests);
			
			GL11.glEnd();
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private void renderBoxes(ArrayList<AxisAlignedBB> boxes, int displayList)
	{
		for(AxisAlignedBB bb : boxes)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(bb.minX, bb.minY, bb.minZ);
			GL11.glScaled(bb.maxX - bb.minX, bb.maxY - bb.minY,
				bb.maxZ - bb.minZ);
			GL11.glCallList(displayList);
			GL11.glPopMatrix();
		}
	}
	
	private void renderLines(Vec3d start, ArrayList<AxisAlignedBB> boxes)
	{
		for(AxisAlignedBB bb : boxes)
		{
			Vec3d end = bb.getCenter();
			
			GL11.glVertex3d(WVec3d.getX(start), WVec3d.getY(start),
				WVec3d.getZ(start));
			GL11.glVertex3d(WVec3d.getX(end), WVec3d.getY(end),
				WVec3d.getZ(end));
		}
	}
}
