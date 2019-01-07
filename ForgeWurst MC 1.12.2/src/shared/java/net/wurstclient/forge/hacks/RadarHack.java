/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.clickgui.Radar;
import net.wurstclient.forge.clickgui.Window;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.EntityFakePlayer;

public final class RadarHack extends Hack
{
	private final Window window;
	private final ArrayList<Entity> entities = new ArrayList<>();
	
	private final SliderSetting radius = new SliderSetting("Radius",
		"Radius in blocks.", 100, 1, 100, 1, ValueDisplay.INTEGER);
	private final CheckboxSetting rotate =
		new CheckboxSetting("Rotate with player", true);
	
	private final CheckboxSetting filterPlayers = new CheckboxSetting(
		"Filter players", "Won't show other players.", false);
	private final CheckboxSetting filterSleeping = new CheckboxSetting(
		"Filter sleeping", "Won't show sleeping players.", false);
	private final CheckboxSetting filterMonsters = new CheckboxSetting(
		"Filter monsters", "Won't show zombies, creepers, etc.", false);
	private final CheckboxSetting filterAnimals = new CheckboxSetting(
		"Filter animals", "Won't show pigs, cows, etc.", false);
	private final CheckboxSetting filterInvisible = new CheckboxSetting(
		"Filter invisible", "Won't show invisible entities.", false);
	
	public RadarHack()
	{
		super("Radar",
			"Shows the location of nearby entities.\n" + ChatFormatting.RED
				+ "red" + ChatFormatting.RESET + " - players\n"
				+ ChatFormatting.GOLD + "orange" + ChatFormatting.RESET
				+ " - monsters\n" + ChatFormatting.GREEN + "green"
				+ ChatFormatting.RESET + " - animals\n" + ChatFormatting.GRAY
				+ "gray" + ChatFormatting.RESET + " - others\n");
		setCategory(Category.RENDER);
		addSetting(radius);
		addSetting(rotate);
		addSetting(filterPlayers);
		addSetting(filterSleeping);
		addSetting(filterMonsters);
		addSetting(filterAnimals);
		addSetting(filterInvisible);
		
		window = new Window("Radar");
		window.setPinned(true);
		window.setInvisible(true);
		window.add(new Radar(this));
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		window.setInvisible(false);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		window.setInvisible(true);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityPlayerSP player = event.getPlayer();
		World world = WPlayer.getWorld(player);
		
		entities.clear();
		Stream<Entity> stream = world.loadedEntityList.parallelStream()
			.filter(e -> !e.isDead && e != player)
			.filter(e -> !(e instanceof EntityFakePlayer))
			.filter(e -> e instanceof EntityLivingBase)
			.filter(e -> ((EntityLivingBase)e).getHealth() > 0);
		
		if(filterPlayers.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityPlayer));
		
		if(filterSleeping.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityPlayer
				&& ((EntityPlayer)e).isPlayerSleeping()));
		
		if(filterMonsters.isChecked())
			stream = stream.filter(e -> !(e instanceof IMob));
		
		if(filterAnimals.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityAnimal
				|| e instanceof EntityAmbientCreature
				|| e instanceof EntityWaterMob));
		
		if(filterInvisible.isChecked())
			stream = stream.filter(e -> !e.isInvisible());
		
		entities.addAll(stream.collect(Collectors.toList()));
	}
	
	public Window getWindow()
	{
		return window;
	}
	
	public Iterable<Entity> getEntities()
	{
		return Collections.unmodifiableList(entities);
	}
	
	public double getRadius()
	{
		return radius.getValue();
	}
	
	public boolean isRotateEnabled()
	{
		return rotate.isChecked();
	}
}
