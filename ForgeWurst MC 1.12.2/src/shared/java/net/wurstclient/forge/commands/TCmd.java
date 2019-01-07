/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.Hack;

public final class TCmd extends Command
{
	public TCmd()
	{
		super("t", "Toggles a hack.", "Syntax: .t <hack> [on|off]");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1 || args.length > 2)
			throw new CmdSyntaxError();
		
		Hack hack = wurst.getHax().get(args[0]);
		if(hack == null)
			throw new CmdError("Unknown hack: " + args[0]);
		
		if(args.length == 1)
			hack.setEnabled(!hack.isEnabled());
		else
			switch(args[1].toLowerCase())
			{
				case "on":
				hack.setEnabled(true);
				break;
				
				case "off":
				hack.setEnabled(false);
				break;
				
				default:
				throw new CmdSyntaxError();
			}
	}
}
