/*
 * This is the latest source code of GUI Followers.
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

package com.natamus.guifollowers.neoforge.events;

import com.natamus.guifollowers.data.Variables;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class NeoForgeKeyMappingRegister {
	@SubscribeEvent
	public static void registerKeyBinding(RegisterKeyMappingsEvent e) {
		Variables.clearlist_hotkey = new KeyMapping("guifollowers.key.clearlist", 92, "key.categories.misc");
		e.register(Variables.clearlist_hotkey);
	}
}