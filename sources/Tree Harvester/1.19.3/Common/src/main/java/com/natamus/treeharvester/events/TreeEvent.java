/*
 * This is the latest source code of Tree Harvester.
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

package com.natamus.treeharvester.events;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.functions.BlockFunctions;
import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.services.Services;
import com.natamus.treeharvester.config.ConfigHandler;
import com.natamus.treeharvester.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TreeEvent {
	private static boolean setupBlacklistRan = false;

	private static final HashMap<Level, CopyOnWriteArrayList<List<BlockPos>>> processleaves = new HashMap<Level, CopyOnWriteArrayList<List<BlockPos>>>();
	private static final HashMap<Pair<Level, Player>, Pair<Date, Integer>> harvestSpeedCache = new HashMap<Pair<Level, Player>, Pair<Date, Integer>>();

	public static void setupBlacklist() {
		if (setupBlacklistRan) {
			return;
		}
		setupBlacklistRan = true;

		try {
			Util.setupAxeBlacklist();
		}
		catch(Exception ex) {
			System.out.println("[Tree Harvester] Something went wrong setting up the axe blacklist file.");
		}
	}
	
	public static void onWorldTick(ServerLevel level) {
		if (processleaves.computeIfAbsent(level, k -> new CopyOnWriteArrayList<List<BlockPos>>()).size() == 0) {
			return;
		}
		
		for (List<BlockPos> leaves : processleaves.get(level)) {
			BlockPos lasttr = null;
			int size = leaves.size();
			if (size > 0) {
				for (int i = 0; i < ConfigHandler.amountOfLeavesBrokenPerTick; i++) {
					if (leaves.isEmpty()) {
						break;
					}

					BlockPos tr = leaves.get(0);

					BlockFunctions.dropBlock(level, tr);

					leaves.remove(0);
					lasttr = tr.immutable();
				}
			}
			
			if (leaves.size() == 0) {
				processleaves.get(level).remove(leaves);
				if (lasttr != null) {
					if (ConfigHandler.replaceSaplingOnTreeHarvest) {
						if (Util.lowerlogs.size() > 0) {
							BlockPos lowerlasttrpos = new BlockPos(lasttr.getX(), 1, lasttr.getZ());
							for (Pair<BlockPos, CopyOnWriteArrayList<BlockPos>> pair : Util.lowerlogs) {
								BlockPos breakpos = pair.getFirst();
								if (BlockPosFunctions.withinDistance(lowerlasttrpos, new BlockPos(breakpos.getX(), 1, breakpos.getZ()), 5)) {
									Util.replaceSapling(level, breakpos, pair.getSecond(), 1, null);
									Util.lowerlogs.remove(pair);
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static boolean onTreeHarvest(Level level, Player player, BlockPos bpos, BlockState state, BlockEntity blockEntity) {
		if (level.isClientSide) {
			return true;
		}

		Block block = level.getBlockState(bpos).getBlock();
		if (!Util.isTreeLog(block)) {
			return true;
		}

		if (ConfigHandler.treeHarvestWithoutSneak) {
			if (player.isCrouching()) {
				return true;
			}
		}
		else {
			if (!player.isCrouching()) {
				return true;
			}
		}
		
		ItemStack hand = player.getItemInHand(InteractionHand.MAIN_HAND);
		Item handitem = hand.getItem();
		if (ConfigHandler.mustHoldAxeForTreeHarvest) {
			if (!Services.TOOLFUNCTIONS.isAxe(hand)) {
				return true;
			}

			if (!Util.allowedAxes.contains(handitem)) {
				return true;
			}
		}
		
		if (ConfigHandler.automaticallyFindBottomBlock) {
			BlockPos temppos = bpos.immutable();
			while (level.getBlockState(temppos.below()).getBlock().equals(block)) {
				temppos = temppos.below().immutable();
			}

			for (BlockPos belowpos : BlockPos.betweenClosed(temppos.getX()-1, temppos.getY()-1, temppos.getZ()-1, temppos.getX()+1, temppos.getY()-1, temppos.getZ()+1)) {
				if (level.getBlockState(belowpos).getBlock().equals(block)) {
					temppos = belowpos.immutable();
					while (level.getBlockState(temppos.below()).getBlock().equals(block)) {
						temppos = temppos.below().immutable();
					}
					break;
				}
			}

			bpos = temppos.immutable();
		}
		
		int logcount = Util.isTreeAndReturnLogAmount(level, bpos);
		if (logcount < 0) {
			return true;
		}
		
		int durabilitylosecount = (int)Math.ceil(1.0 / ConfigHandler.loseDurabilityModifier);
		int durabilitystartcount = -1;

		ServerPlayer serverPlayer = (ServerPlayer)player;

		BlockPos highestlog = bpos.immutable();
		List<BlockPos> logstobreak = Util.getAllLogsToBreak(level, bpos, logcount, block);
		for (BlockPos logpos : logstobreak) {
			if (logpos.getY() > highestlog.getY()) {
				highestlog = logpos.immutable();
			}

			BlockState logstate = level.getBlockState(logpos);
			Block log = logstate.getBlock();

			BlockFunctions.dropBlock(level, logpos);
			//ForgeEventFactory.onEntityDestroyBlock(player, logpos, logstate);

			if (!player.isCreative()) {
				if (ConfigHandler.loseDurabilityPerHarvestedLog) {
					if (durabilitystartcount == -1) {
						durabilitystartcount = durabilitylosecount;
						ItemFunctions.itemHurtBreakAndEvent(hand, serverPlayer, InteractionHand.MAIN_HAND, 1);
					}
					else {
						durabilitylosecount -= 1;

						if (durabilitylosecount == 0) {
							ItemFunctions.itemHurtBreakAndEvent(hand, serverPlayer, InteractionHand.MAIN_HAND, 1);
							durabilitylosecount = durabilitystartcount;
						}
					}
				}
				if (ConfigHandler.increaseExhaustionPerHarvestedLog) {
					player.causeFoodExhaustion(0.025F * (float)ConfigHandler.increaseExhaustionModifier);
				}
			}
		}
		
		if (logstobreak.size() == 0) {
			return true;
		}
		
		if (ConfigHandler.enableFastLeafDecay || ConfigHandler.instantBreakLeavesAround) {
			List<BlockPos> logs = new ArrayList<BlockPos>();
			List<BlockPos> leaves = new ArrayList<BlockPos>();

			for (BlockPos next : BlockPos.betweenClosed(bpos.getX() - 8, bpos.getY(), bpos.getZ() - 8, bpos.getX() + 8, Util.highestleaf.get(bpos), bpos.getZ() + 8)) {
				Block nextblock = level.getBlockState(next).getBlock();
				if (Util.isTreeLog(nextblock)) {
					if (nextblock.equals(block) || Util.areEqualLogTypes(block, nextblock)) {
						logs.add(next.immutable());
					}
				}
			}

			Pair<Integer, Integer> hv = Util.getHorizontalAndVerticalValue(level, bpos, block, logcount);
			int h = hv.getFirst();

			CopyOnWriteArrayList<BlockPos> leftoverleaves = new CopyOnWriteArrayList<BlockPos>();

			Block leafblock = level.getBlockState(highestlog.above()).getBlock();
			for (BlockPos next : BlockPos.betweenClosed(bpos.getX() - h, bpos.getY(), bpos.getZ() - h, bpos.getX() + h, Util.highestleaf.get(bpos), bpos.getZ() + h)) {
				Block nextblock = level.getBlockState(next).getBlock();

				if (!leafblock.equals(nextblock) && !(ConfigHandler.enableNetherTrees && nextblock.equals(Blocks.SHROOMLIGHT)) && !(Util.isAzaleaLeaf(leafblock) && Util.isAzaleaLeaf(nextblock))) {
					continue;
				}

				if (CompareBlockFunctions.isTreeLeaf(nextblock, ConfigHandler.enableNetherTrees) || Util.isGiantMushroomLeafBlock(nextblock)) {
					boolean logclose = false;
					for (BlockPos log : logs) {
						double distance = log.distSqr(next);
						if (BlockPosFunctions.withinDistance(log, next, 3)) {
							logclose = true;
							break;
						}
					}

					if (!logclose) {
						leaves.add(next.immutable());
					}
					else {
						leftoverleaves.add(next.immutable());
					}
				}
			}

			for (BlockPos leftoverleaf : leftoverleaves) {
				if (leftoverleaves.isEmpty()) {
					break;
				}

				Pair<Boolean, List<BlockPos>> connectedpair = Util.isConnectedToLogs(level, leftoverleaf);
				if (connectedpair.getFirst()) {
					for (BlockPos connectedpos : connectedpair.getSecond()) {
						leftoverleaves.remove(connectedpos);
					}
				}
				else {
					for (BlockPos connectedpos : connectedpair.getSecond()) {
						if (!leaves.contains(connectedpos)) {
							leaves.add(connectedpos.immutable());
						}

						leftoverleaves.remove(connectedpos);
					}
				}
			}

			if (ConfigHandler.instantBreakLeavesAround) {
				for (BlockPos leafPos : leaves) {
					level.destroyBlock(leafPos, true);
				}
			}
			else {
				Collections.shuffle(leaves);
				processleaves.computeIfAbsent(level, k -> new CopyOnWriteArrayList<List<BlockPos>>()).add(leaves);
			}

			Util.highestleaf.remove(bpos);

			if (ConfigHandler.increaseHarvestingTimePerLog) {
				Pair<Level, BlockPos> keypair = new Pair<Level, BlockPos>(level, bpos);
				harvestSpeedCache.remove(keypair);
			}
		}

		return false;
	}

	public static float onHarvestBreakSpeed(Level level, Player player, float digSpeed, BlockState state) {
		if (!ConfigHandler.increaseHarvestingTimePerLog) {
			return digSpeed;
		}

		Block block = state.getBlock();
		if (!Util.isTreeLog(block)) {
			return digSpeed;
		}

		if (ConfigHandler.treeHarvestWithoutSneak) {
			if (player.isCrouching()) {
				return digSpeed;
			}
		}
		else {
			if (!player.isCrouching()) {
				return digSpeed;
			}
		}

		ItemStack hand = player.getItemInHand(InteractionHand.MAIN_HAND);
		Item handitem = hand.getItem();
		if (ConfigHandler.mustHoldAxeForTreeHarvest) {
			if (!Services.TOOLFUNCTIONS.isAxe(hand)) {
				return digSpeed;
			}

			if (!Util.allowedAxes.contains(handitem)) {
				return digSpeed;
			}
		}

		int logcount = -1;

		Date now = new Date();
		Pair<Level, Player> keypair = new Pair<Level, Player>(level, player);
		if (harvestSpeedCache.containsKey(keypair)) {
			Pair<Date, Integer> valuepair = harvestSpeedCache.get(keypair);
			long ms = (now.getTime()-valuepair.getFirst().getTime());

			if (ms < 1000) {
				logcount = valuepair.getSecond();
			}
			else {
				harvestSpeedCache.remove(keypair);
			}
		}

		BlockPos bpos = null;

		HitResult hitResult = player.pick(20.0D, 0.0F, false);
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			bpos = ((BlockHitResult)hitResult).getBlockPos();
		}

		if (bpos == null) {
			return digSpeed;
		}

		if (logcount < 0) {
			if (Util.isTreeAndReturnLogAmount(level, bpos) < 0) {
				return digSpeed;
			}

			logcount = BlockPosFunctions.getBlocksNextToEachOtherMaterial(level, bpos, Arrays.asList(Material.WOOD), 25).size(); // Util.isTreeAndReturnLogAmount(level, bpos);
			if (logcount == 0) {
				return digSpeed;
			}

			harvestSpeedCache.put(keypair, new Pair<Date, Integer>(now, logcount));
		}

		return digSpeed/(1+(logcount * (float)ConfigHandler.increasedHarvestingTimePerLogModifier));
	}
}