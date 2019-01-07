/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.clickgui.ClickGuiScreen;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class IngameHUD
{
	private final Minecraft mc = Minecraft.getMinecraft();
	private final HackList hackList;
	private final ClickGui clickGui;
	
	public IngameHUD(HackList hackList, ClickGui clickGui)
	{
		this.hackList = hackList;
		this.clickGui = clickGui;
	}
	
	@SubscribeEvent
	public void onRenderGUI(RenderGameOverlayEvent.Post event)
	{
		if(event.getType() != ElementType.ALL || mc.gameSettings.showDebugInfo)
			return;
		
		boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);
		
		// color
		clickGui.updateColors();
		int textColor;
		if(hackList.rainbowUiHack.isEnabled())
		{
			float[] acColor = clickGui.getAcColor();
			textColor = (int)(acColor[0] * 256) << 16
				| (int)(acColor[1] * 256) << 8 | (int)(acColor[2] * 256);
		}else
			textColor = 0xffffff;
		
		// title
		GL11.glPushMatrix();
		GL11.glScaled(1.33333333, 1.33333333, 1);
		WMinecraft.getFontRenderer().drawStringWithShadow(
			"ForgeWurst v" + ForgeWurst.VERSION, 3, 3, textColor);
		GL11.glPopMatrix();
		
		// hack list
		int y = 19;
		ArrayList<Hack> hacks = new ArrayList<>();
		hacks.addAll(hackList.getValues());
		hacks.sort(Comparator.comparing(Hack::getName));
		
		for(Hack hack : hacks)
		{
			if(!hack.isEnabled())
				continue;
			
			WMinecraft.getFontRenderer()
				.drawStringWithShadow(hack.getRenderName(), 2, y, textColor);
			y += 9;
		}
		
		// pinned windows
		if(!(mc.currentScreen instanceof ClickGuiScreen))
			clickGui.renderPinnedWindows(event.getPartialTicks());
		
		if(blend)
			GL11.glEnable(GL11.GL_BLEND);
		else
			GL11.glDisable(GL11.GL_BLEND);
	}
}
