/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.clickgui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;

public final class Checkbox extends Component
{
	private final CheckboxSetting setting;
	
	public Checkbox(CheckboxSetting setting)
	{
		this.setting = setting;
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		if(mouseButton == 0)
			setting.setChecked(!setting.isChecked());
		else if(mouseButton == 1)
			setting.setChecked(setting.isCheckedByDefault());
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
		int x3 = x1 + 11;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		int scroll = getParent().isScrollingEnabled()
			? getParent().getScrollOffset() : 0;
		boolean hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2
			&& mouseY < y2 && mouseY >= -scroll
			&& mouseY < getParent().getHeight() - 13 - scroll;
		
		// tooltip
		if(hovering && mouseX >= x3)
			gui.setTooltip(setting.getDescription());
		
		// background
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x3, y1);
		GL11.glVertex2i(x3, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		
		// box
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2],
			hovering ? opacity * 1.5F : opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x3, y2);
		GL11.glVertex2i(x3, y1);
		GL11.glEnd();
		GL11.glColor4f(acColor[0], acColor[1], acColor[2], 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x3, y2);
		GL11.glVertex2i(x3, y1);
		GL11.glEnd();
		
		if(setting.isChecked())
		{
			double xc1 = x1 + 2.5;
			double xc2 = x1 + 3.5;
			double xc3 = x1 + 4.5;
			double xc4 = x1 + 7.5;
			double xc5 = x1 + 8.5;
			double yc1 = y1 + 2.5;
			double yc2 = y1 + 3.5;
			double yc3 = y1 + 5.5;
			double yc4 = y1 + 6.5;
			double yc5 = y1 + 8.5;
			
			// check
			GL11.glColor4f(0, hovering ? 1 : 0.85F, 0, 1);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2d(xc2, yc3);
			GL11.glVertex2d(xc3, yc4);
			GL11.glVertex2d(xc3, yc5);
			GL11.glVertex2d(xc1, yc4);
			GL11.glVertex2d(xc4, yc1);
			GL11.glVertex2d(xc5, yc2);
			GL11.glVertex2d(xc3, yc5);
			GL11.glVertex2d(xc3, yc4);
			GL11.glEnd();
			
			// outline
			GL11.glColor4f(0.0625F, 0.0625F, 0.0625F, 0.5F);
			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex2d(xc2, yc3);
			GL11.glVertex2d(xc3, yc4);
			GL11.glVertex2d(xc4, yc1);
			GL11.glVertex2d(xc5, yc2);
			GL11.glVertex2d(xc3, yc5);
			GL11.glVertex2d(xc1, yc4);
			GL11.glEnd();
		}
		
		// checkbox name
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		FontRenderer fr = WMinecraft.getFontRenderer();
		fr.drawString(setting.getName(), x3 + 2, y1 + 2, 0xf0f0f0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public int getDefaultWidth()
	{
		return WMinecraft.getFontRenderer().getStringWidth(setting.getName())
			+ 13;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 11;
	}
}
