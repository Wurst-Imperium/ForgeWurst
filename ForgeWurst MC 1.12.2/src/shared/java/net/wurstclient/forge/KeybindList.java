/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.wurstclient.forge.utils.JsonUtils;

public final class KeybindList
{
	private final Path path;
	private final ArrayList<Keybind> keybinds = new ArrayList<>();
	
	public KeybindList(Path file)
	{
		path = file;
	}
	
	public void init()
	{
		JsonObject json;
		try(BufferedReader reader = Files.newBufferedReader(path))
		{
			json = JsonUtils.jsonParser.parse(reader).getAsJsonObject();
			
		}catch(NoSuchFileException e)
		{
			loadDefaults();
			return;
			
		}catch(Exception e)
		{
			System.out.println("Failed to load " + path.getFileName());
			e.printStackTrace();
			
			loadDefaults();
			return;
		}
		
		keybinds.clear();
		
		TreeMap<String, String> keybinds2 = new TreeMap<>();
		for(Entry<String, JsonElement> entry : json.entrySet())
		{
			String key = entry.getKey().toUpperCase();
			if(Keyboard.getKeyIndex(key) == Keyboard.KEY_NONE)
				continue;
			
			if(!entry.getValue().isJsonPrimitive()
				|| !entry.getValue().getAsJsonPrimitive().isString())
				continue;
			String commands = entry.getValue().getAsString();
			
			keybinds2.put(key, commands);
		}
		
		for(Entry<String, String> entry : keybinds2.entrySet())
			keybinds.add(new Keybind(entry.getKey(), entry.getValue()));
		
		save();
	}
	
	public void loadDefaults()
	{
		keybinds.clear();
		keybinds.add(new Keybind("B", "fastplace;fastbreak"));
		keybinds.add(new Keybind("C", "fullbright"));
		keybinds.add(new Keybind("G", "flight"));
		keybinds.add(new Keybind("J", "jesus"));
		keybinds.add(new Keybind("LCONTROL", "clickgui"));
		keybinds.add(new Keybind("N", "nuker"));
		keybinds.add(new Keybind("R", "killaura"));
		keybinds.add(new Keybind("RSHIFT", "clickgui"));
		keybinds.add(new Keybind("U", "freecam"));
		keybinds.add(new Keybind("X", "x-ray"));
		keybinds.add(new Keybind("Z", "sneak"));
		save();
	}
	
	private void save()
	{
		JsonObject json = new JsonObject();
		for(Keybind keybind : keybinds)
			json.addProperty(keybind.getKey(), keybind.getCommands());
		
		try(BufferedWriter writer = Files.newBufferedWriter(path))
		{
			JsonUtils.prettyGson.toJson(json, writer);
			
		}catch(IOException e)
		{
			System.out.println("Failed to save " + path.getFileName());
			e.printStackTrace();
		}
	}
	
	public int size()
	{
		return keybinds.size();
	}
	
	public Keybind get(int index)
	{
		return keybinds.get(index);
	}
	
	public String getCommands(String key)
	{
		for(Keybind keybind : keybinds)
		{
			if(!key.equals(keybind.getKey()))
				continue;
			
			return keybind.getCommands();
		}
		
		return null;
	}
	
	public void add(String key, String commands)
	{
		keybinds.removeIf(keybind -> key.equals(keybind.getKey()));
		keybinds.add(new Keybind(key, commands));
		keybinds.sort(Comparator.comparing(Keybind::getKey));
		save();
	}
	
	public void remove(String key)
	{
		keybinds.removeIf(keybind -> key.equals(keybind.getKey()));
		save();
	}
	
	public void removeAll()
	{
		keybinds.clear();
		save();
	}
	
	public static class Keybind
	{
		private final String key;
		private final String commands;
		
		public Keybind(String key, String commands)
		{
			this.key = key;
			this.commands = commands;
		}
		
		public String getKey()
		{
			return key;
		}
		
		public String getCommands()
		{
			return commands;
		}
	}
}
