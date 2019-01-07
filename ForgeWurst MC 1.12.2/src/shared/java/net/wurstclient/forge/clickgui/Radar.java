/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.clickgui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.hacks.RadarHack;

public final class Radar extends Component
{
	private final RadarHack hack;
	
	public Radar(RadarHack hack)
	{
		this.hack = hack;
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		ClickGui gui = ForgeWurst.getForgeWurst().getGui();
		float[] bgColor = gui.getBgColor();
		float[] acColor = gui.getAcColor();
		float opacity = gui.getOpacity();
		
		int x1 = getX();
		int x2 = x1 + getWidth();
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		int scroll = getParent().isScrollingEnabled()
			? getParent().getScrollOffset() : 0;
		boolean hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2
			&& mouseY < y2 && mouseY >= -scroll
			&& mouseY < getParent().getHeight() - 13 - scroll;
		
		// tooltip
		if(hovering)
			gui.setTooltip(null);
		
		// background
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		
		double middleX = (x1 + x2) / 2.0;
		double middleY = (y1 + y2) / 2.0;
		
		GL11.glPushMatrix();
		GL11.glTranslated(middleX, middleY, 0);
		EntityPlayerSP player = WMinecraft.getPlayer();
		if(!hack.isRotateEnabled())
			GL11.glRotated(180 + player.rotationYaw, 0, 0, 1);
		
		double xa1 = 0;
		double xa2 = 2;
		double xa3 = -2;
		double ya1 = -2;
		double ya2 = 2;
		double ya3 = 1;
		
		// arrow
		GL11.glColor4f(acColor[0], acColor[1], acColor[2], opacity);
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glVertex2d(xa1, ya1);
		GL11.glVertex2d(xa2, ya2);
		GL11.glVertex2d(xa1, ya3);
		GL11.glVertex2d(xa3, ya2);
		GL11.glEnd();
		
		// outline
		GL11.glColor4f(0.0625F, 0.0625F, 0.0625F, 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2d(xa1, ya1);
		GL11.glVertex2d(xa2, ya2);
		GL11.glVertex2d(xa1, ya3);
		GL11.glVertex2d(xa3, ya2);
		GL11.glEnd();
		
		GL11.glPopMatrix();
		
		// points
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glPointSize(2);
		GL11.glBegin(GL11.GL_POINTS);
		for(Entity e : hack.getEntities())
		{
			double diffX = e.prevPosX + (e.posX - e.prevPosX) * partialTicks
				- (player.prevPosX
					+ (player.posX - player.prevPosX) * partialTicks);
			double diffZ = e.prevPosZ + (e.posZ - e.prevPosZ) * partialTicks
				- (player.prevPosZ
					+ (player.posZ - player.prevPosZ) * partialTicks);
			double distance = Math.sqrt(diffX * diffX + diffZ * diffZ)
				* (getWidth() * 0.5 / hack.getRadius());
			double neededRotation = Math.toDegrees(Math.atan2(diffZ, diffX));
			double angle;
			if(hack.isRotateEnabled())
				angle =
					Math.toRadians(player.rotationYaw - neededRotation - 90);
			else
				angle = Math.toRadians(180 - neededRotation - 90);
			double renderX = Math.sin(angle) * distance;
			double renderY = Math.cos(angle) * distance;
			
			if(Math.abs(renderX) > getWidth() / 2.0
				|| Math.abs(renderY) > getHeight() / 2.0)
				continue;
			
			int color;
			if(e instanceof EntityPlayer)
				color = 0xFF0000;
			else if(e instanceof IMob)
				color = 0xFF8000;
			else if(e instanceof EntityAnimal
				|| e instanceof EntityAmbientCreature
				|| e instanceof EntityWaterMob)
				color = 0x00FF00;
			else
				color = 0x808080;
			
			GL11.glColor4f((color >> 16 & 255) / 255F,
				(color >> 8 & 255) / 255F, (color & 255) / 255F, 1);
			GL11.glVertex2d(middleX + renderX, middleY + renderY);
		}
		GL11.glEnd();
	}
	
	@Override
	public int getDefaultWidth()
	{
		return 96;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 96;
	}
}
