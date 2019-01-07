/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.wurstclient.forge.ForgeWurst;

public final class KeyBindingUtils
{
	private static ForgeWurst wurst = ForgeWurst.getForgeWurst();
	
	public static void setPressed(KeyBinding binding, boolean pressed)
	{
		try
		{
			Field field = binding.getClass().getDeclaredField(
				wurst.isObfuscated() ? "field_74513_e" : "pressed");
			field.setAccessible(true);
			field.setBoolean(binding, pressed);
			
		}catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static void resetPressed(KeyBinding binding)
	{
		setPressed(binding, GameSettings.isKeyDown(binding));
	}
}
