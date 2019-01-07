/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import java.util.List;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public final class WChatInputEvent extends Event
{
	private ITextComponent component;
	private final List<ChatLine> chatLines;
	
	public WChatInputEvent(ITextComponent component, List<ChatLine> chatLines)
	{
		this.component = component;
		this.chatLines = chatLines;
	}
	
	public ITextComponent getComponent()
	{
		return component;
	}
	
	public void setComponent(ITextComponent component)
	{
		this.component = component;
	}
	
	public List<ChatLine> getChatLines()
	{
		return chatLines;
	}
}
