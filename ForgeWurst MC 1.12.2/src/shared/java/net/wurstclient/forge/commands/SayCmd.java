/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.minecraft.network.play.client.CPacketChatMessage;
import net.wurstclient.forge.Command;

public final class SayCmd extends Command
{
	public SayCmd()
	{
		super("say", "Sends the given chat message.", "Syntax: .say <message>");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		String message = String.join(" ", args);
		CPacketChatMessage packet = new CPacketChatMessage(message);
		mc.getConnection().sendPacket(packet);
	}
}
