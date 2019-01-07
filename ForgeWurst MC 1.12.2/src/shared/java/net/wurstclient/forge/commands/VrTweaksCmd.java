/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.hacks.ClickGuiHack;
import net.wurstclient.forge.utils.ChatUtils;

public final class VrTweaksCmd extends Command
{
	public VrTweaksCmd()
	{
		super("vrtweaks", "Tweaks your settings for ViveCraft.",
			"Syntax: .vrtweaks");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 0)
			throw new CmdSyntaxError();
		
		ChatUtils.message("Adjusting settings for VR...");
		ClickGuiHack gui = wurst.getHax().clickGuiHack;
		
		if(gui.getMaxHeight() == 0)
			ChatUtils.message("Scrolling is already disabled.");
		else
		{
			gui.setMaxHeight(0);
			ChatUtils.message("Disabled scrolling.");
		}
		
		if(gui.isInventoryButton())
			ChatUtils.message("ClickGUI button is already enabled.");
		else
		{
			gui.setInventoryButton(true);
			ChatUtils.message("Enabled ClickGUI button.");
		}
		
		ChatUtils.message("Done!");
	}
}
