/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.clickgui.BlockListEditButton;
import net.wurstclient.forge.clickgui.Component;
import net.wurstclient.forge.utils.BlockUtils;

public final class BlockListSetting extends Setting
{
	private final ArrayList<String> blockNames = new ArrayList<>();
	private final String[] defaultNames;
	
	public BlockListSetting(String name, String description, Block... blocks)
	{
		super(name, description);
		
		Arrays.stream(blocks).parallel().map(b -> BlockUtils.getName(b))
			.distinct().sorted().forEachOrdered(s -> blockNames.add(s));
		defaultNames = blockNames.toArray(new String[0]);
	}
	
	public BlockListSetting(String name, Block... blocks)
	{
		this(name, null, blocks);
	}
	
	public List<String> getBlockNames()
	{
		return Collections.unmodifiableList(blockNames);
	}
	
	public void add(Block block)
	{
		String name = BlockUtils.getName(block);
		if(Collections.binarySearch(blockNames, name) >= 0)
			return;
		
		blockNames.add(name);
		Collections.sort(blockNames);
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}
	
	public void remove(int index)
	{
		if(index < 0 || index >= blockNames.size())
			return;
		
		blockNames.remove(index);
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}
	
	public void resetToDefaults()
	{
		blockNames.clear();
		blockNames.addAll(Arrays.asList(defaultNames));
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}
	
	@Override
	public Component getComponent()
	{
		return new BlockListEditButton(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!json.isJsonArray())
			return;
		
		blockNames.clear();
		StreamSupport.stream(json.getAsJsonArray().spliterator(), true)
			.filter(e -> e.isJsonPrimitive())
			.filter(e -> e.getAsJsonPrimitive().isString())
			.map(e -> Block.getBlockFromName(e.getAsString()))
			.filter(Objects::nonNull).map(b -> BlockUtils.getName(b)).distinct()
			.sorted().forEachOrdered(s -> blockNames.add(s));
	}
	
	@Override
	public JsonElement toJson()
	{
		JsonArray json = new JsonArray();
		blockNames.forEach(s -> json.add(new JsonPrimitive(s)));
		return json;
	}
}
