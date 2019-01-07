/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.clickgui.Component;
import net.wurstclient.forge.clickgui.Slider;
import net.wurstclient.forge.utils.MathUtils;

public final class SliderSetting extends Setting
{
	private double value;
	private final double defaultValue;
	private final double min;
	private final double max;
	private final double increment;
	private final ValueDisplay display;
	
	public SliderSetting(String name, String description, double value,
		double min, double max, double increment, ValueDisplay display)
	{
		super(name, description);
		this.value = value;
		defaultValue = value;
		this.min = min;
		this.max = max;
		this.increment = increment;
		this.display = display;
	}
	
	public SliderSetting(String name, double value, double min, double max,
		double increment, ValueDisplay display)
	{
		this(name, null, value, min, max, increment, display);
	}
	
	public double getValue()
	{
		return value;
	}
	
	public float getValueF()
	{
		return (float)value;
	}
	
	public int getValueI()
	{
		return (int)value;
	}
	
	public String getValueString()
	{
		return display.getValueString(value);
	}
	
	public double getDefaultValue()
	{
		return defaultValue;
	}
	
	public void setValue(double value)
	{
		value = Math.round(value / increment) * increment;
		value = MathUtils.clamp(value, min, max);
		
		this.value = value;
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}
	
	public double getMin()
	{
		return min;
	}
	
	public double getMax()
	{
		return max;
	}
	
	@Override
	public Component getComponent()
	{
		return new Slider(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!json.isJsonPrimitive())
			return;
		
		JsonPrimitive primitive = json.getAsJsonPrimitive();
		if(!primitive.isNumber())
			return;
		
		setValue(primitive.getAsDouble());
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(Math.round(value * 1e6) / 1e6);
	}
	
	public static interface ValueDisplay
	{
		public static final ValueDisplay DECIMAL =
			v -> Math.round(v * 1e6) / 1e6 + "";
		public static final ValueDisplay INTEGER = v -> (int)v + "";
		public static final ValueDisplay PERCENTAGE =
			v -> (int)(Math.round(v * 1e8) / 1e6) + "%";
		public static final ValueDisplay DEGREES = v -> (int)v + "°";
		public static final ValueDisplay NONE = v -> "";
		
		public String getValueString(double value);
	}
}
