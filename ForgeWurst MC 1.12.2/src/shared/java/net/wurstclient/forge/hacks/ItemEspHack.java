/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WCameraTransformViewBobbingEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.compatibility.WVec3d;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class ItemEspHack extends Hack
{
	private final CheckboxSetting names =
		new CheckboxSetting("Show item names", true);
	private final EnumSetting<Style> style =
		new EnumSetting<>("Style", Style.values(), Style.BOXES);
	
	private int itemBox;
	private final ArrayList<EntityItem> items = new ArrayList<>();
	
	public ItemEspHack()
	{
		super("ItemESP", "Highlights nearby items.");
		setCategory(Category.RENDER);
		addSetting(names);
		addSetting(style);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		itemBox = GL11.glGenLists(1);
		GL11.glNewList(itemBox, GL11.GL_COMPILE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1, 1, 0, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(
			new AxisAlignedBB(-0.175, 0, -0.175, 0.175, 0.35, 0.175));
		GL11.glEnd();
		GL11.glEndList();
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		
		GL11.glDeleteLists(itemBox, 1);
		itemBox = 0;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		World world = WPlayer.getWorld(event.getPlayer());
		
		items.clear();
		for(Entity entity : world.loadedEntityList)
			if(entity instanceof EntityItem)
				items.add((EntityItem)entity);
	}
	
	@SubscribeEvent
	public void onCameraTransformViewBobbing(
		WCameraTransformViewBobbingEvent event)
	{
		if(style.getSelected().lines)
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		// GL settings
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		
		double partialTicks = event.getPartialTicks();
		
		renderBoxes(partialTicks);
		
		if(style.getSelected().lines)
			renderTracers(partialTicks);
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private void renderBoxes(double partialTicks)
	{
		for(EntityItem e : items)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(e.prevPosX + (e.posX - e.prevPosX) * partialTicks,
				e.prevPosY + (e.posY - e.prevPosY) * partialTicks,
				e.prevPosZ + (e.posZ - e.prevPosZ) * partialTicks);
			
			if(style.getSelected().boxes)
				GL11.glCallList(itemBox);
			
			if(names.isChecked())
			{
				ItemStack stack = WItem.getItemStack(e);
				EntityRenderer.drawNameplate(WMinecraft.getFontRenderer(),
					WItem.getStackSize(stack) + "x " + stack.getDisplayName(),
					0, 1, 0, 0, mc.getRenderManager().playerViewY,
					mc.getRenderManager().playerViewX,
					mc.getRenderManager().options.thirdPersonView == 2, false);
				GL11.glDisable(GL11.GL_LIGHTING);
			}
			
			GL11.glPopMatrix();
		}
	}
	
	private void renderTracers(double partialTicks)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1, 1, 0, 0.5F);
		
		Vec3d start = RotationUtils.getClientLookVec()
			.addVector(0, WMinecraft.getPlayer().getEyeHeight(), 0)
			.addVector(TileEntityRendererDispatcher.staticPlayerX,
				TileEntityRendererDispatcher.staticPlayerY,
				TileEntityRendererDispatcher.staticPlayerZ);
		
		GL11.glBegin(GL11.GL_LINES);
		for(EntityItem e : items)
		{
			Vec3d end = e.getEntityBoundingBox().getCenter()
				.subtract(new Vec3d(e.posX, e.posY, e.posZ)
					.subtract(e.prevPosX, e.prevPosY, e.prevPosZ)
					.scale(1 - partialTicks));
			
			GL11.glVertex3d(WVec3d.getX(start), WVec3d.getY(start),
				WVec3d.getZ(start));
			GL11.glVertex3d(WVec3d.getX(end), WVec3d.getY(end),
				WVec3d.getZ(end));
		}
		GL11.glEnd();
	}
	
	private enum Style
	{
		BOXES("Boxes only", true, false),
		LINES("Lines only", false, true),
		LINES_AND_BOXES("Lines and boxes", true, true);
		
		private final String name;
		private final boolean boxes;
		private final boolean lines;
		
		private Style(String name, boolean boxes, boolean lines)
		{
			this.name = name;
			this.boxes = boxes;
			this.lines = lines;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
