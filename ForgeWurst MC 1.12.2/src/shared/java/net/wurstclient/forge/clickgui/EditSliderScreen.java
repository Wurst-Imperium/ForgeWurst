/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.clickgui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.MathUtils;

public final class EditSliderScreen extends GuiScreen
{
	private final GuiScreen prevScreen;
	private final SliderSetting slider;
	
	private GuiTextField valueField;
	private GuiButton doneButton;
	
	public EditSliderScreen(GuiScreen prevScreen, SliderSetting slider)
	{
		this.prevScreen = prevScreen;
		this.slider = slider;
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void initGui()
	{
		valueField = new GuiTextField(1, WMinecraft.getFontRenderer(),
			width / 2 - 100, 60, 200, 20);
		valueField
			.setText(ValueDisplay.DECIMAL.getValueString(slider.getValue()));
		valueField.setSelectionPos(0);
		valueField.setFocused(true);
		
		buttonList.add(doneButton =
			new GuiButton(0, width / 2 - 100, height / 3 * 2, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		String value = valueField.getText();
		if(MathUtils.isDouble(value))
			slider.setValue(Double.parseDouble(value));
		mc.displayGuiScreen(prevScreen);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
		throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		valueField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		valueField.textboxKeyTyped(typedChar, keyCode);
		
		if(keyCode == Keyboard.KEY_RETURN)
			actionPerformed(doneButton);
		else if(keyCode == Keyboard.KEY_ESCAPE)
			mc.displayGuiScreen(prevScreen);
	}
	
	@Override
	public void updateScreen()
	{
		valueField.updateCursorCounter();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();
		drawCenteredString(WMinecraft.getFontRenderer(), slider.getName(),
			width / 2, 20, 0xffffff);
		valueField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
