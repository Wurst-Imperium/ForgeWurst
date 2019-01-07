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
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.Setting;

public final class SetCheckboxCmd extends Command
{
	public SetCheckboxCmd()
	{
		super("setcheckbox", "Modifies a checkbox setting.",
			"Syntax: .setcheckbox <hack> <checkbox> <value>");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Hack hack = wurst.getHax().get(args[0]);
		if(hack == null)
			throw new CmdError("Hack \"" + args[0] + "\" could not be found.");
		
		Setting setting =
			hack.getSettings().get(args[1].toLowerCase().replace("_", " "));
		if(setting == null)
			throw new CmdError("Setting \"" + args[0] + " " + args[1]
				+ "\" could not be found.");
		
		if(!(setting instanceof CheckboxSetting))
			throw new CmdError(hack.getName() + " " + setting.getName()
				+ " is not a checkbox.");
		CheckboxSetting e = (CheckboxSetting)setting;
		
		if(!args[2].equalsIgnoreCase("true")
			&& !args[2].equalsIgnoreCase("false"))
			throw new CmdSyntaxError("Not a boolean: " + args[2]);
		
		e.setChecked(Boolean.parseBoolean(args[2]));
	}
}
