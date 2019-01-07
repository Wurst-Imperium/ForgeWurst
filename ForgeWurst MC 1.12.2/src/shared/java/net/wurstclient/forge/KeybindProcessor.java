/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import org.lwjgl.input.Keyboard;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public final class KeybindProcessor
{
	private final HackList hax;
	private final KeybindList keybinds;
	private final CommandProcessor cmdProcessor;
	
	public KeybindProcessor(HackList hax, KeybindList keybinds,
		CommandProcessor cmdProcessor)
	{
		this.hax = hax;
		this.keybinds = keybinds;
		this.cmdProcessor = cmdProcessor;
	}
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event)
	{
		int keyCode = Keyboard.getEventKey();
		if(keyCode == 0 || !Keyboard.getEventKeyState())
			return;
		
		String commands = keybinds.getCommands(Keyboard.getKeyName(keyCode));
		if(commands == null)
			return;
		
		commands = commands.replace(";", "\u00a7").replace("\u00a7\u00a7", ";");
		for(String command : commands.split("\u00a7"))
		{
			command = command.trim();
			
			if(command.startsWith("."))
				cmdProcessor.runCommand(command.substring(1));
			else if(command.contains(" "))
				cmdProcessor.runCommand(command);
			else
			{
				Hack hack = hax.get(command);
				
				if(hack != null)
					hack.setEnabled(!hack.isEnabled());
				else
					cmdProcessor.runCommand(command);
			}
		}
	}
}
