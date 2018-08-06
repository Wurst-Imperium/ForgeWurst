/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
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
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.Hack;

public abstract class WHackList
{
	private static IForgeRegistry<Hack> registry;
	{
		if(registry != null)
			throw new IllegalStateException("Multiple instances of HackList!");
		
		RegistryBuilder<Hack> registryBuilder = new RegistryBuilder<>();
		registryBuilder.setName(new ResourceLocation(ForgeWurst.MODID, "hax"));
		registryBuilder.setType(Hack.class);
		registryBuilder.setIDRange(0, Integer.MAX_VALUE - 1);
		registry = registryBuilder.create();
	}
	
	protected final <T extends Hack> T register(T hack)
	{
		hack.setRegistryName(ForgeWurst.MODID, hack.getName().toLowerCase());
		registry.register(hack);
		return hack;
	}
	
	public final IForgeRegistry<Hack> getRegistry()
	{
		return registry;
	}
	
	public final Collection<Hack> getValues()
	{
		return registry.getValues();
	}
	
	public final Hack get(String name)
	{
		ResourceLocation location =
			new ResourceLocation(ForgeWurst.MODID, name.toLowerCase());
		return registry.getValue(location);
	}
}
