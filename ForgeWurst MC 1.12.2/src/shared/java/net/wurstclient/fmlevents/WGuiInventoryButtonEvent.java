/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class WGuiInventoryButtonEvent
{
	public static final class Init extends Event
	{
		private final List<GuiButton> buttonList;
		
		public Init(List<GuiButton> buttonList)
		{
			this.buttonList = buttonList;
		}
		
		public List<GuiButton> getButtonList()
		{
			return buttonList;
		}
	}
	
	public static final class Press extends Event
	{
		private final GuiButton button;
		
		public Press(GuiButton button)
		{
			this.button = button;
		}
		
		public GuiButton getButton()
		{
			return button;
		}
	}
}
