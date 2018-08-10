/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.wurstclient.forge.analytics.AnalyticsConfigData;
import net.wurstclient.forge.analytics.AnalyticsRequestData;
import net.wurstclient.forge.analytics.JGoogleAnalyticsTracker;
import net.wurstclient.forge.analytics.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import net.wurstclient.forge.analytics.VisitorData;
import net.wurstclient.forge.utils.JsonUtils;

public final class GoogleAnalytics
{
	private final String trackingId;
	private final String hostname;
	private final Path path;
	
	private boolean enabled = true;
	private AnalyticsConfigData configData;
	private JGoogleAnalyticsTracker tracker;
	
	public GoogleAnalytics(String trackingId, String hostname, Path path)
	{
		this.trackingId = trackingId;
		this.hostname = hostname;
		this.path = path;
		configData =
			new AnalyticsConfigData(trackingId, VisitorData.newVisitor());
		tracker = new JGoogleAnalyticsTracker(configData,
			GoogleAnalyticsVersion.V_4_7_2);
	}
	
	public void loadConfig()
	{
		JsonObject json;
		try(BufferedReader reader = Files.newBufferedReader(path))
		{
			json = JsonUtils.jsonParser.parse(reader).getAsJsonObject();
			
		}catch(NoSuchFileException e)
		{
			saveConfig();
			return;
			
		}catch(Exception e)
		{
			System.out.println("Failed to load " + path.getFileName());
			e.printStackTrace();
			
			saveConfig();
			return;
		}
		
		JsonElement jsonEnabled = json.get("enabled");
		if(jsonEnabled != null && jsonEnabled.isJsonPrimitive()
			&& jsonEnabled.getAsJsonPrimitive().isBoolean())
			enabled = jsonEnabled.getAsBoolean();
		
		JsonElement jsonId = json.get("id");
		if(jsonId == null || !jsonId.isJsonPrimitive()
			|| !jsonId.getAsJsonPrimitive().isNumber())
		{
			saveConfig();
			return;
		}
		
		JsonElement jsonFirstLaunch = json.get("first_launch");
		if(jsonFirstLaunch == null || !jsonFirstLaunch.isJsonPrimitive()
			|| !jsonFirstLaunch.getAsJsonPrimitive().isNumber())
		{
			saveConfig();
			return;
		}
		
		JsonElement jsonLastLaunch = json.get("last_launch");
		if(jsonLastLaunch == null || !jsonLastLaunch.isJsonPrimitive()
			|| !jsonLastLaunch.getAsJsonPrimitive().isNumber())
		{
			saveConfig();
			return;
		}
		
		JsonElement jsonLaunches = json.get("launches");
		if(jsonLaunches == null || !jsonLaunches.isJsonPrimitive()
			|| !jsonLaunches.getAsJsonPrimitive().isNumber())
		{
			saveConfig();
			return;
		}
		
		VisitorData visitorData = VisitorData.newSession(jsonId.getAsInt(),
			jsonFirstLaunch.getAsLong(), jsonLastLaunch.getAsLong(),
			jsonLaunches.getAsInt());
		configData = new AnalyticsConfigData(trackingId, visitorData);
		tracker = new JGoogleAnalyticsTracker(configData,
			GoogleAnalyticsVersion.V_4_7_2);
		
		saveConfig();
	}
	
	private void saveConfig()
	{
		JsonObject json = new JsonObject();
		VisitorData data = configData.getVisitorData();
		
		json.addProperty("enabled", enabled);
		json.addProperty("id", data.getVisitorId());
		json.addProperty("first_launch", data.getTimestampFirst());
		json.addProperty("last_launch", data.getTimestampCurrent());
		json.addProperty("launches", data.getVisits());
		
		try(BufferedWriter writer = Files.newBufferedWriter(path))
		{
			JsonUtils.prettyGson.toJson(json, writer);
			
		}catch(IOException e)
		{
			System.out.println("Failed to save " + path.getFileName());
			e.printStackTrace();
		}
	}
	
	public void trackPageView(String url, String title)
	{
		if(!enabled)
			return;
		
		tracker.trackPageView(url, title, hostname);
	}
	
	public void trackPageViewFromReferrer(String url, String title,
		String referrerSite, String referrerPage)
	{
		if(!enabled)
			return;
		
		tracker.trackPageViewFromReferrer(url, title, hostname, referrerSite,
			referrerPage);
	}
	
	public void trackPageViewFromSearch(String url, String title,
		String searchSource, String keywords)
	{
		if(!enabled)
			return;
		
		tracker.trackPageViewFromSearch(url, title, hostname, searchSource,
			keywords);
	}
	
	public void trackEvent(String category, String action)
	{
		if(!enabled)
			return;
		
		tracker.trackEvent(category, action);
	}
	
	public void trackEvent(String category, String action, String label)
	{
		if(!enabled)
			return;
		
		tracker.trackEvent(category, action, label);
	}
	
	public void trackEvent(String category, String action, String label,
		Integer value)
	{
		if(!enabled)
			return;
		
		tracker.trackEvent(category, action, label, value);
	}
	
	public void makeCustomRequest(AnalyticsRequestData data)
	{
		if(!enabled)
			return;
		
		tracker.makeCustomRequest(data);
	}
	
	public AnalyticsConfigData getConfigData()
	{
		return configData;
	}
}
