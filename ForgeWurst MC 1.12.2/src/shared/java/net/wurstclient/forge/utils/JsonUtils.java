/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public final class JsonUtils
{
	public static final Gson gson = new Gson();
	public static final Gson prettyGson =
		new GsonBuilder().setPrettyPrinting().create();
	public static final JsonParser jsonParser = new JsonParser();
}
