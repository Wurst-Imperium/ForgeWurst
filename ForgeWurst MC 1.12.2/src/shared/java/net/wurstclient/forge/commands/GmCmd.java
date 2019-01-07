/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class GmCmd extends Command
{
	public GmCmd()
	{
		super("gm", "Shortcut for /gamemode.", "Syntax: .gm <gamemode>");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		String message = "/gamemode " + String.join(" ", args);
		WMinecraft.getPlayer().sendChatMessage(message);
	}
}
