/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WEntity;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.EntityFakePlayer;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class KillauraHack extends Hack
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 10, 0.05, ValueDisplay.DECIMAL);
	
	private final EnumSetting<Priority> priority = new EnumSetting<>("Priority",
		"Determines which entity will be attacked first.\n"
			+ "\u00a7lDistance\u00a7r - Attacks the closest entity.\n"
			+ "\u00a7lAngle\u00a7r - Attacks the entity that requires\n"
			+ "the least head movement.\n"
			+ "\u00a7lHealth\u00a7r - Attacks the weakest entity.",
		Priority.values(), Priority.ANGLE);
	
	private final CheckboxSetting filterPlayers = new CheckboxSetting(
		"Filter players", "Won't attack other players.", false);
	private final CheckboxSetting filterSleeping = new CheckboxSetting(
		"Filter sleeping", "Won't attack sleeping players.", false);
	private final SliderSetting filterFlying =
		new SliderSetting("Filter flying",
			"Won't attack players that\n" + "are at least the given\n"
				+ "distance above ground.",
			0, 0, 2, 0.05,
			v -> v == 0 ? "off" : ValueDisplay.DECIMAL.getValueString(v));
	
	private final CheckboxSetting filterMonsters = new CheckboxSetting(
		"Filter monsters", "Won't attack zombies, creepers, etc.", false);
	private final CheckboxSetting filterPigmen = new CheckboxSetting(
		"Filter pigmen", "Won't attack zombie pigmen.", false);
	private final CheckboxSetting filterEndermen =
		new CheckboxSetting("Filter endermen", "Won't attack endermen.", false);
	
	private final CheckboxSetting filterAnimals = new CheckboxSetting(
		"Filter animals", "Won't attack pigs, cows, etc.", false);
	private final CheckboxSetting filterBabies =
		new CheckboxSetting("Filter babies",
			"Won't attack baby pigs,\n" + "baby villagers, etc.", false);
	private final CheckboxSetting filterPets =
		new CheckboxSetting("Filter pets",
			"Won't attack tamed wolves,\n" + "tamed horses, etc.", false);
	
	private final CheckboxSetting filterVillagers = new CheckboxSetting(
		"Filter villagers", "Won't attack villagers.", false);
	private final CheckboxSetting filterGolems =
		new CheckboxSetting("Filter golems",
			"Won't attack iron golems,\n" + "snow golems and shulkers.", false);
	
	private final CheckboxSetting filterInvisible = new CheckboxSetting(
		"Filter invisible", "Won't attack invisible entities.", false);
	
	private EntityLivingBase target;
	
	public KillauraHack()
	{
		super("Killaura", "Automatically attacks entities around you.");
		setCategory(Category.COMBAT);
		addSetting(range);
		addSetting(priority);
		addSetting(filterPlayers);
		addSetting(filterSleeping);
		addSetting(filterFlying);
		addSetting(filterMonsters);
		addSetting(filterPigmen);
		addSetting(filterEndermen);
		addSetting(filterAnimals);
		addSetting(filterBabies);
		addSetting(filterPets);
		addSetting(filterVillagers);
		addSetting(filterGolems);
		addSetting(filterInvisible);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		target = null;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityPlayerSP player = event.getPlayer();
		World world = WPlayer.getWorld(player);
		
		if(player.getCooledAttackStrength(0) < 1)
			return;
		
		double rangeSq = Math.pow(range.getValue(), 2);
		Stream<EntityLivingBase> stream = world.loadedEntityList
			.parallelStream().filter(e -> e instanceof EntityLivingBase)
			.map(e -> (EntityLivingBase)e)
			.filter(e -> !e.isDead && e.getHealth() > 0)
			.filter(e -> WEntity.getDistanceSq(player, e) <= rangeSq)
			.filter(e -> e != player)
			.filter(e -> !(e instanceof EntityFakePlayer));
		
		if(filterPlayers.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityPlayer));
		
		if(filterSleeping.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityPlayer
				&& ((EntityPlayer)e).isPlayerSleeping()));
		
		if(filterFlying.getValue() > 0)
			stream = stream.filter(e -> {
				
				if(!(e instanceof EntityPlayer))
					return true;
				
				AxisAlignedBB box = e.getEntityBoundingBox();
				box = box.union(box.offset(0, -filterFlying.getValue(), 0));
				// Using expand() with negative values doesn't work in 1.10.2.
				return world.collidesWithAnyBlock(box);
			});
		
		if(filterMonsters.isChecked())
			stream = stream.filter(e -> !(e instanceof IMob));
		
		if(filterPigmen.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityPigZombie));
		
		if(filterEndermen.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityEnderman));
		
		if(filterAnimals.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityAnimal
				|| e instanceof EntityAmbientCreature
				|| e instanceof EntityWaterMob));
		
		if(filterBabies.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityAgeable
				&& ((EntityAgeable)e).isChild()));
		
		if(filterPets.isChecked())
			stream = stream
				.filter(e -> !(e instanceof EntityTameable
					&& ((EntityTameable)e).isTamed()))
				.filter(e -> !WEntity.isTamedHorse(e));
		
		if(filterVillagers.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityVillager));
		
		if(filterGolems.isChecked())
			stream = stream.filter(e -> !(e instanceof EntityGolem));
		
		if(filterInvisible.isChecked())
			stream = stream.filter(e -> !e.isInvisible());
		
		target = stream.min(priority.getSelected().comparator).orElse(null);
		if(target == null)
			return;
		
		RotationUtils
			.faceVectorPacket(target.getEntityBoundingBox().getCenter());
		mc.playerController.attackEntity(player, target);
		player.swingArm(EnumHand.MAIN_HAND);
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(target == null)
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		
		AxisAlignedBB box = new AxisAlignedBB(BlockPos.ORIGIN);
		float p = (target.getMaxHealth() - target.getHealth())
			/ target.getMaxHealth();
		float red = p * 2F;
		float green = 2 - red;
		
		GL11.glTranslated(target.posX, target.posY, target.posZ);
		GL11.glTranslated(0, 0.05, 0);
		GL11.glScaled(target.width, target.height, target.width);
		GL11.glTranslated(-0.5, 0, -0.5);
		
		if(p < 1)
		{
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glScaled(p, p, p);
			GL11.glTranslated(-0.5, -0.5, -0.5);
		}
		
		GL11.glColor4f(red, green, 0, 0.25F);
		GL11.glBegin(GL11.GL_QUADS);
		RenderUtils.drawSolidBox(box);
		GL11.glEnd();
		
		GL11.glColor4f(red, green, 0, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(box);
		GL11.glEnd();
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private enum Priority
	{
		DISTANCE("Distance",
			e -> WEntity.getDistanceSq(WMinecraft.getPlayer(), e)),
		
		ANGLE("Angle",
			e -> RotationUtils
				.getAngleToLookVec(e.getEntityBoundingBox().getCenter())),
		
		HEALTH("Health", e -> e.getHealth());
		
		private final String name;
		private final Comparator<EntityLivingBase> comparator;
		
		private Priority(String name,
			ToDoubleFunction<EntityLivingBase> keyExtractor)
		{
			this.name = name;
			comparator = Comparator.comparingDouble(keyExtractor);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
