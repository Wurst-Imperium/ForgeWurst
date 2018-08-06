/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.clickgui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.BlockListSetting;

public final class EditBlockListScreen extends GuiScreen
{
	private final GuiScreen prevScreen;
	private final BlockListSetting blockList;
	
	private ListGui listGui;
	private GuiTextField blockNameField;
	private GuiButton addButton;
	private GuiButton removeButton;
	private GuiButton doneButton;
	
	private Block blockToAdd;
	
	public EditBlockListScreen(GuiScreen prevScreen, BlockListSetting slider)
	{
		this.prevScreen = prevScreen;
		blockList = slider;
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void initGui()
	{
		listGui = new ListGui(mc, this, blockList.getBlockNames());
		
		blockNameField = new GuiTextField(1, WMinecraft.getFontRenderer(), 64,
			height - 55, 150, 18);
		
		buttonList
			.add(addButton = new GuiButton(0, 214, height - 56, 30, 20, "Add"));
		buttonList.add(removeButton = new GuiButton(1, width - 150, height - 56,
			100, 20, "Remove Selected"));
		buttonList.add(
			new GuiButton(2, width - 108, 8, 100, 20, "Reset to Defaults"));
		buttonList.add(doneButton =
			new GuiButton(3, width / 2 - 100, height - 28, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if(!button.enabled)
			return;
		
		switch(button.id)
		{
			case 0:
			blockList.add(blockToAdd);
			blockNameField.setText("");
			break;
			
			case 1:
			blockList.remove(listGui.selected);
			break;
			
			case 2:
			mc.displayGuiScreen(
				new GuiYesNo(this, "Reset to Defaults", "Are you sure?", 0));
			break;
			
			case 3:
			mc.displayGuiScreen(prevScreen);
			break;
		}
	}
	
	@Override
	public void confirmClicked(boolean result, int id)
	{
		if(id == 0 && result)
			blockList.resetToDefaults();
		
		super.confirmClicked(result, id);
		mc.displayGuiScreen(this);
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		int mouseX = Mouse.getEventX() * width / mc.displayWidth;
		int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		listGui.handleMouseInput(mouseX, mouseY);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
		throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		blockNameField.mouseClicked(mouseX, mouseY, mouseButton);
		
		if(mouseX < 50 || mouseX > width - 50 || mouseY < 32
			|| mouseY > height - 64)
			listGui.selected = -1;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		blockNameField.textboxKeyTyped(typedChar, keyCode);
		
		if(keyCode == Keyboard.KEY_RETURN)
			actionPerformed(addButton);
		else if(keyCode == Keyboard.KEY_DELETE)
			actionPerformed(removeButton);
		else if(keyCode == Keyboard.KEY_ESCAPE)
			actionPerformed(doneButton);
	}
	
	@Override
	public void updateScreen()
	{
		blockNameField.updateCursorCounter();
		
		blockToAdd = Block.getBlockFromName(blockNameField.getText());
		addButton.enabled = blockToAdd != null;
		
		removeButton.enabled =
			listGui.selected >= 0 && listGui.selected < listGui.list.size();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();
		drawCenteredString(WMinecraft.getFontRenderer(),
			blockList.getName() + " (" + listGui.getSize() + ")", width / 2, 12,
			0xffffff);
		
		listGui.drawScreen(mouseX, mouseY, partialTicks);
		
		blockNameField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if(blockNameField.getText().isEmpty() && !blockNameField.isFocused())
			drawString(WMinecraft.getFontRenderer(), "block name or ID", 68,
				height - 50, 0x808080);
		
		drawRect(48, height - 56, 64, height - 36, 0xffa0a0a0);
		drawRect(49, height - 55, 64, height - 37, 0xff000000);
		drawRect(214, height - 56, 244, height - 55, 0xffa0a0a0);
		drawRect(214, height - 37, 244, height - 36, 0xffa0a0a0);
		drawRect(244, height - 56, 246, height - 36, 0xffa0a0a0);
		drawRect(214, height - 55, 243, height - 52, 0xff000000);
		drawRect(214, height - 40, 243, height - 37, 0xff000000);
		drawRect(215, height - 55, 216, height - 37, 0xff000000);
		drawRect(242, height - 55, 245, height - 37, 0xff000000);
		
		listGui.renderIconAndGetName(new ItemStack(blockToAdd), height - 52);
	}
	
	private static class ListGui extends GuiScrollingList
	{
		private final Minecraft mc;
		private final List<String> list;
		private int selected = -1;
		
		public ListGui(Minecraft mc, EditBlockListScreen screen,
			List<String> list)
		{
			super(mc, screen.width - 100, screen.height, 32, screen.height - 64,
				50, 16, screen.width, screen.height);
			this.mc = mc;
			this.list = list;
		}
		
		@Override
		protected int getSize()
		{
			return list.size();
		}
		
		@Override
		protected void elementClicked(int index, boolean doubleClick)
		{
			if(index >= 0 && index < list.size())
				selected = index;
		}
		
		@Override
		protected boolean isSelected(int index)
		{
			return index == selected;
		}
		
		@Override
		protected void drawBackground()
		{
			drawRect(50, top, 66, bottom, 0xffffffff);
		}
		
		@Override
		protected void drawSlot(int slotIdx, int entryRight, int slotTop,
			int slotBuffer, Tessellator tess)
		{
			String name = list.get(slotIdx);
			
			ItemStack stack = new ItemStack(Block.getBlockFromName(name));
			FontRenderer fr = WMinecraft.getFontRenderer();
			
			String displayName = renderIconAndGetName(stack, slotTop);
			fr.drawString(displayName + " (" + name + ")", 68, slotTop + 2,
				0xf0f0f0);
		}
		
		private String renderIconAndGetName(ItemStack stack, int y)
		{
			if(WItem.isNullOrEmpty(stack))
			{
				GL11.glPushMatrix();
				GL11.glTranslated(52, y, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				
				RenderHelper.enableGUIStandardItemLighting();
				mc.getRenderItem().renderItemAndEffectIntoGUI(
					new ItemStack(Blocks.GRASS), 0, 0);
				RenderHelper.disableStandardItemLighting();
				GL11.glPopMatrix();
				
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				FontRenderer fr = WMinecraft.getFontRenderer();
				fr.drawString("?", 55, y + 2, 0xf0f0f0, true);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				return ChatFormatting.ITALIC + "unknown block"
					+ ChatFormatting.RESET;
				
			}else
			{
				GL11.glPushMatrix();
				GL11.glTranslated(52, y, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				
				RenderHelper.enableGUIStandardItemLighting();
				mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
				RenderHelper.disableStandardItemLighting();
				
				GL11.glPopMatrix();
				
				return stack.getDisplayName();
			}
		}
	}
}
