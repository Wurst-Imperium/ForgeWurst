/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.List;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WChatInputEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.MathUtils;

public final class AntiSpamHack extends Hack
{
	public AntiSpamHack()
	{
		super("AntiSpam",
			"Blocks chat spam by adding a\n" + "counter to repeated messages.");
		setCategory(Category.CHAT);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onChatInput(WChatInputEvent event)
	{
		List<ChatLine> chatLines = event.getChatLines();
		if(chatLines.isEmpty())
			return;
		
		GuiNewChat chat = mc.ingameGUI.getChatGUI();
		int maxTextLenght =
			MathUtils.floor(chat.getChatWidth() / chat.getChatScale());
		List<ITextComponent> newLines =
			GuiUtilRenderComponents.splitText(event.getComponent(),
				maxTextLenght, WMinecraft.getFontRenderer(), false, false);
		
		int spamCounter = 1;
		int matchingLines = 0;
		
		for(int i = chatLines.size() - 1; i >= 0; i--)
		{
			String oldLine =
				chatLines.get(i).getChatComponent().getUnformattedText();
			
			if(matchingLines <= newLines.size() - 1)
			{
				String newLine =
					newLines.get(matchingLines).getUnformattedText();
				
				if(matchingLines < newLines.size() - 1)
				{
					if(oldLine.equals(newLine))
						matchingLines++;
					else
						matchingLines = 0;
					
					continue;
				}
				
				if(!oldLine.startsWith(newLine))
				{
					matchingLines = 0;
					continue;
				}
				
				if(i > 0 && matchingLines == newLines.size() - 1)
				{
					String twoLines = oldLine + chatLines.get(i - 1)
						.getChatComponent().getUnformattedText();
					String addedText = twoLines.substring(newLine.length());
					
					if(addedText.startsWith(" [x") && addedText.endsWith("]"))
					{
						String oldSpamCounter =
							addedText.substring(3, addedText.length() - 1);
						
						if(MathUtils.isInteger(oldSpamCounter))
						{
							spamCounter += Integer.parseInt(oldSpamCounter);
							matchingLines++;
							continue;
						}
					}
				}
				
				if(oldLine.length() == newLine.length())
					spamCounter++;
				else
				{
					String addedText = oldLine.substring(newLine.length());
					if(!addedText.startsWith(" [x") || !addedText.endsWith("]"))
					{
						matchingLines = 0;
						continue;
					}
					
					String oldSpamCounter =
						addedText.substring(3, addedText.length() - 1);
					if(!MathUtils.isInteger(oldSpamCounter))
					{
						matchingLines = 0;
						continue;
					}
					
					spamCounter += Integer.parseInt(oldSpamCounter);
				}
			}
			
			for(int i2 = i + matchingLines; i2 >= i; i2--)
				chatLines.remove(i2);
			matchingLines = 0;
		}
		
		if(spamCounter > 1)
			event.getComponent().appendText(" [x" + spamCounter + "]");
	}
}
