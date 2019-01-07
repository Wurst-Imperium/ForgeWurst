/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.KeybindList.Keybind;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.MathUtils;

public final class BindsCmd extends Command
{
	public BindsCmd()
	{
		super("binds", "Manages keybinds.", "Syntax: .binds add <key> <hacks>",
			".binds add <key> <commands>", ".binds remove <key>",
			".binds list [<page>]", ".binds remove-all", ".binds reset",
			"Multiple hacks/commands must be separated by ';'.");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		switch(args[0].toLowerCase())
		{
			case "add":
			add(args);
			break;
			
			case "remove":
			remove(args);
			break;
			
			case "list":
			list(args);
			break;
			
			case "remove-all":
			wurst.getKeybinds().removeAll();
			ChatUtils.message("All keybinds removed.");
			break;
			
			case "reset":
			wurst.getKeybinds().loadDefaults();
			ChatUtils.message("All keybinds reset to defaults.");
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
	
	private void add(String[] args) throws CmdException
	{
		if(args.length < 3)
			throw new CmdSyntaxError();
		
		String key = args[1].toUpperCase();
		if(Keyboard.getKeyIndex(key) == Keyboard.KEY_NONE)
			throw new CmdSyntaxError("Unknown key: " + key);
		
		String commands =
			String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		
		wurst.getKeybinds().add(key, commands);
		ChatUtils.message("Keybind set: " + key + " -> " + commands);
	}
	
	private void remove(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String key = args[1].toUpperCase();
		if(Keyboard.getKeyIndex(key) == Keyboard.KEY_NONE)
			throw new CmdSyntaxError("Unknown key: " + key);
		
		String oldCommands = wurst.getKeybinds().getCommands(key);
		if(oldCommands == null)
			throw new CmdError("Nothing to remove.");
		
		wurst.getKeybinds().remove(key);
		ChatUtils.message("Keybind removed: " + key + " -> " + oldCommands);
	}
	
	private void list(String[] args) throws CmdException
	{
		if(args.length > 2)
			throw new CmdSyntaxError();
		
		int page;
		if(args.length < 2)
			page = 1;
		else if(MathUtils.isInteger(args[1]))
			page = Integer.parseInt(args[1]);
		else
			throw new CmdSyntaxError("Not a number: " + args[1]);
		
		int keybinds = wurst.getKeybinds().size();
		int pages = Math.max((int)Math.ceil(keybinds / 8.0), 1);
		if(page > pages || page < 1)
			throw new CmdSyntaxError("Invalid page: " + page);
		
		ChatUtils.message(
			"Total: " + keybinds + (keybinds == 1 ? " keybind" : " keybinds"));
		ChatUtils.message("Keybind list (page " + page + "/" + pages + ")");
		
		for(int i = (page - 1) * 8; i < Math.min(page * 8, keybinds); i++)
		{
			Keybind k = wurst.getKeybinds().get(i);
			ChatUtils.message(k.getKey() + " -> " + k.getCommands());
		}
	}
}
