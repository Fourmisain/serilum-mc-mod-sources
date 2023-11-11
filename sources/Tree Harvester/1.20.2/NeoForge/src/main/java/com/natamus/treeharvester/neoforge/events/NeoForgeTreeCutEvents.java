/*
 * This is the latest source code of Tree Harvester.
 * Minecraft version: 1.20.2.
 *
 * Please don't distribute without permission.
 * For all Minecraft modding projects, feel free to visit my profile page on CurseForge or Modrinth.
 *  CurseForge: https://curseforge.com/members/serilum/projects
 *  Modrinth: https://modrinth.com/user/serilum
 *  Overview: https://serilum.com/
 *
 * If you are feeling generous and would like to support the development of the mods, you can!
 *  https://ricksouth.com/donate contains all the information. <3
 *
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.treeharvester.neoforge.events;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.treeharvester.events.TreeCutEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NeoForgeTreeCutEvents {
	@SubscribeEvent
	public static void onTreeHarvest(BlockEvent.BreakEvent e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}
		
		TreeCutEvents.onTreeHarvest(level, e.getPlayer(), e.getPos(), e.getState(), null);
	}

	@SubscribeEvent
	public static void onHarvestBreakSpeed(PlayerEvent.BreakSpeed e) {
		Player player = e.getEntity();
		Level level = player.level();

		float originalSpeed = e.getOriginalSpeed();
		float newSpeed = TreeCutEvents.onHarvestBreakSpeed(level, player, originalSpeed, e.getState());

		if (originalSpeed != newSpeed) {
			e.setNewSpeed(newSpeed);
		}
	}
}