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
import net.wurstclient.forge.settings.Setting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.MathUtils;

public final class SetSliderCmd extends Command
{
	public SetSliderCmd()
	{
		super("setslider", "Modifies a slider setting.",
			"Syntax: .setslider <hack> <slider> <value>");
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
		
		if(!(setting instanceof SliderSetting))
			throw new CmdError(
				hack.getName() + " " + setting.getName() + " is not a slider.");
		SliderSetting slider = (SliderSetting)setting;
		
		if(MathUtils.isDouble(args[2]))
			slider.setValue(Double.parseDouble(args[2]));
		else if(args[2].startsWith("~")
			&& MathUtils.isDouble(args[2].substring(1)))
			slider.setValue(
				slider.getValue() + Double.parseDouble(args[2].substring(1)));
		else
			throw new CmdSyntaxError("Not a number: " + args[2]);
	}
}
