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
import net.wurstclient.forge.clickgui.Checkbox;
import net.wurstclient.forge.clickgui.Component;

public final class CheckboxSetting extends Setting
{
	private boolean checked;
	private final boolean checkedByDefault;
	
	public CheckboxSetting(String name, String description, boolean checked)
	{
		super(name, description);
		this.checked = checked;
		checkedByDefault = checked;
	}
	
	public CheckboxSetting(String name, boolean checked)
	{
		this(name, null, checked);
	}
	
	public boolean isChecked()
	{
		return checked;
	}
	
	public boolean isCheckedByDefault()
	{
		return checkedByDefault;
	}
	
	public void setChecked(boolean checked)
	{
		this.checked = checked;
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}
	
	@Override
	public Component getComponent()
	{
		return new Checkbox(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!json.isJsonPrimitive())
			return;
		
		JsonPrimitive primitive = json.getAsJsonPrimitive();
		if(!primitive.isBoolean())
			return;
		
		setChecked(primitive.getAsBoolean());
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(checked);
	}
}
