/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import net.wurstclient.forge.Command;
import net.wurstclient.forge.ForgeWurst;

public abstract class WCommandList
{
	private static IForgeRegistry<Command> registry;
	{
		if(registry != null)
			throw new IllegalStateException(
				"Multiple instances of CommandList!");
		
		RegistryBuilder<Command> registryBuilder = new RegistryBuilder<>();
		registryBuilder.setName(new ResourceLocation(ForgeWurst.MODID, "cmds"));
		registryBuilder.setType(Command.class);
		registryBuilder.setIDRange(0, Integer.MAX_VALUE - 1);
		registry = registryBuilder.create();
	}
	
	protected final <T extends Command> T register(T cmd)
	{
		cmd.setRegistryName(ForgeWurst.MODID, cmd.getName().toLowerCase());
		registry.register(cmd);
		return cmd;
	}
	
	public final IForgeRegistry<Command> getRegistry()
	{
		return registry;
	}
	
	public final Collection<Command> getValues()
	{
		return registry.getValues();
	}
	
	public final Command get(String name)
	{
		ResourceLocation location =
			new ResourceLocation(ForgeWurst.MODID, name.toLowerCase());
		return registry.getValue(location);
	}
}
