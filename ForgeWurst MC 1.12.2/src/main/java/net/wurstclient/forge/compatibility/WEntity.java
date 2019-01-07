/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;

public final class WEntity
{
	public static boolean isCollidedHorizontally(Entity entity)
	{
		return entity.collidedHorizontally;
	}
	
	public static float getDistance(Entity e1, Entity e2)
	{
		return e1.getDistance(e2);
	}
	
	public static double getDistanceSq(Entity e1, Entity e2)
	{
		return e1.getDistanceSq(e2);
	}
	
	public static boolean isTamedHorse(Entity e)
	{
		return e instanceof AbstractHorse && ((AbstractHorse)e).isTame();
	}
}
