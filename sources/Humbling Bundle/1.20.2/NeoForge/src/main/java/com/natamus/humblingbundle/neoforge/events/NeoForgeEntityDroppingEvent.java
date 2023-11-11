/*
 * This is the latest source code of Humbling Bundle.
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

package com.natamus.humblingbundle.neoforge.events;

import com.natamus.humblingbundle.events.EntityDroppingEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NeoForgeEntityDroppingEvent {
	@SubscribeEvent
	public static void mobItemDrop(LivingDropsEvent e) {
		Entity entity = e.getEntity();
		EntityDroppingEvent.mobItemDrop(entity.level(), entity, e.getSource());
	}
}