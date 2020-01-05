/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public final class WPacketInputEvent extends Event
{
	private final Packet<?> packet;
	
	public WPacketInputEvent(Packet<?> packet)
	{
		this.packet = packet;
	}
	
	public Packet<?> getPacket()
	{
		return packet;
	}
}
