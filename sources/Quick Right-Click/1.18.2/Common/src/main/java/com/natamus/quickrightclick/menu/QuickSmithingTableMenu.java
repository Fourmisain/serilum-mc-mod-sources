/*
 * This is the latest source code of Quick Right-Click.
 * Minecraft version: 1.18.2.
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

package com.natamus.quickrightclick.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import org.jetbrains.annotations.NotNull;

public class QuickSmithingTableMenu extends SmithingMenu {
	public QuickSmithingTableMenu(int id, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(id, inventory, containerLevelAccess);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}
}