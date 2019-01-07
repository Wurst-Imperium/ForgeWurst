/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.clickgui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.SliderSetting;

public final class Slider extends Component
{
	private final SliderSetting setting;
	private boolean dragging;
	
	public Slider(SliderSetting setting)
	{
		this.setting = setting;
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		if(mouseY < getY() + 11)
			return;
		
		if(mouseButton == 0)
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
				|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
				Minecraft.getMinecraft().displayGuiScreen(new EditSliderScreen(
					Minecraft.getMinecraft().currentScreen, setting));
			else
				dragging = true;
		else if(mouseButton == 1)
			setting.setValue(setting.getDefaultValue());
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		// dragging
		if(dragging)
			if(Mouse.isButtonDown(0))
			{
				double mousePercentage =
					(mouseX - (getX() + 2)) / (double)(getWidth() - 4);
				double value = setting.getMin()
					+ (setting.getMax() - setting.getMin()) * mousePercentage;
				setting.setValue(value);
				
			}else
				dragging = false;
			
		ClickGui gui = ForgeWurst.getForgeWurst().getGui();
		float[] bgColor = gui.getBgColor();
		float[] acColor = gui.getAcColor();
		float opacity = gui.getOpacity();
		
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x1 + 2;
		int x4 = x2 - 2;
		int y1 = getY();
		int y2 = y1 + getHeight();
		int y3 = y1 + 11;
		int y4 = y3 + 4;
		int y5 = y2 - 4;
		
		int scroll = getParent().isScrollingEnabled()
			? getParent().getScrollOffset() : 0;
		boolean hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2
			&& mouseY < y2 && mouseY >= -scroll
			&& mouseY < getParent().getHeight() - 13 - scroll;
		boolean hSlider = hovering && mouseY >= y3 || dragging;
		
		// tooltip
		if(hovering && mouseY < y3)
			gui.setTooltip(setting.getDescription());
		
		// background
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y4);
		GL11.glVertex2i(x2, y4);
		GL11.glVertex2i(x2, y1);
		GL11.glVertex2i(x1, y5);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y5);
		GL11.glVertex2i(x1, y4);
		GL11.glVertex2i(x1, y5);
		GL11.glVertex2i(x3, y5);
		GL11.glVertex2i(x3, y4);
		GL11.glVertex2i(x4, y4);
		GL11.glVertex2i(x4, y5);
		GL11.glVertex2i(x2, y5);
		GL11.glVertex2i(x2, y4);
		GL11.glEnd();
		
		// rail
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2],
			hSlider ? opacity * 1.5F : opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x3, y4);
		GL11.glVertex2i(x3, y5);
		GL11.glVertex2i(x4, y5);
		GL11.glVertex2i(x4, y4);
		GL11.glEnd();
		GL11.glColor4f(acColor[0], acColor[1], acColor[2], 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2i(x3, y4);
		GL11.glVertex2i(x3, y5);
		GL11.glVertex2i(x4, y5);
		GL11.glVertex2i(x4, y4);
		GL11.glEnd();
		
		double percentage = (setting.getValue() - setting.getMin())
			/ (setting.getMax() - setting.getMin());
		double xk1 = x1 + (x2 - x1 - 8) * percentage;
		double xk2 = xk1 + 8;
		double yk1 = y3 + 1.5;
		double yk2 = y2 - 1.5;
		
		// knob
		float f = (float)(2 * percentage);
		GL11.glColor4f(f, 2 - f, 0, hSlider ? 1 : 0.75F);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2d(xk1, yk1);
		GL11.glVertex2d(xk1, yk2);
		GL11.glVertex2d(xk2, yk2);
		GL11.glVertex2d(xk2, yk1);
		GL11.glEnd();
		
		// outline
		GL11.glColor4f(0.0625F, 0.0625F, 0.0625F, 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2d(xk1, yk1);
		GL11.glVertex2d(xk1, yk2);
		GL11.glVertex2d(xk2, yk2);
		GL11.glVertex2d(xk2, yk1);
		GL11.glEnd();
		
		// slider name & value
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		FontRenderer fr = WMinecraft.getFontRenderer();
		fr.drawString(setting.getName(), x1, y1 + 2, 0xf0f0f0);
		fr.drawString(setting.getValueString(),
			x2 - fr.getStringWidth(setting.getValueString()), y1 + 2, 0xf0f0f0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public int getDefaultWidth()
	{
		FontRenderer fr = WMinecraft.getFontRenderer();
		return fr.getStringWidth(setting.getName())
			+ fr.getStringWidth(setting.getValueString()) + 6;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 22;
	}
}
