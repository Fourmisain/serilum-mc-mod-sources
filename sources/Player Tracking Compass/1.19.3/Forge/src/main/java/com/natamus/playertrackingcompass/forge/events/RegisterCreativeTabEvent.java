/*
 * This is the latest source code of Player Tracking Compass.
 * Minecraft version: 1.19.3.
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

package com.natamus.playertrackingcompass.forge.events;

import com.natamus.playertrackingcompass.items.CompassVariables;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterCreativeTabEvent {
    @SubscribeEvent
    public void onCreativeTab(CreativeModeTabEvent.BuildContents e) {
        if (e.getTab().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            e.accept(CompassVariables.TRACKING_COMPASS);
        }
    }
}
