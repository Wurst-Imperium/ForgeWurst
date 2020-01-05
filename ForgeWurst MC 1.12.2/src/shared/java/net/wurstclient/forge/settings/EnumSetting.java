/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.settings;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.clickgui.ComboBox;
import net.wurstclient.forge.clickgui.Component;

public final class EnumSetting<T extends Enum<?>> extends Setting
{
	private final T[] values;
	private T selected;
	private final T defaultSelected;
	
	public EnumSetting(String name, String description, T[] values, T selected)
	{
		super(name, description);
		this.values = Objects.requireNonNull(values);
		this.selected = Objects.requireNonNull(selected);
		defaultSelected = selected;
	}
	
	public EnumSetting(String name, T[] values, T selected)
	{
		this(name, null, values, selected);
	}
	
	public T[] getValues()
	{
		return values;
	}
	
	public T getSelected()
	{
		return selected;
	}
	
	public T getDefaultSelected()
	{
		return defaultSelected;
	}
	
	public void setSelected(T selected)
	{
		this.selected = Objects.requireNonNull(selected);
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}
	
	public void setSelected(String selected)
	{
		for(T value : values)
		{
			if(!value.toString().equalsIgnoreCase(selected))
				continue;
			
			setSelected(value);
			break;
		}
	}
	
	@Override
	public Component getComponent()
	{
		return new ComboBox(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!json.isJsonPrimitive())
			return;
		
		JsonPrimitive primitive = json.getAsJsonPrimitive();
		if(!primitive.isString())
			return;
		
		setSelected(primitive.getAsString());
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(selected.toString());
	}
}
