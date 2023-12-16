/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import de.tr7zw.changeme.nbtapi.utils.VersionChecker;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.event.CustomFishingReloadEvent;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.ReflectionUtils;
import net.momirealms.customfishing.command.CommandManagerImpl;
import net.momirealms.customfishing.compatibility.IntegrationManagerImpl;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.libraries.libraryloader.LibraryLoader;
import net.momirealms.customfishing.mechanic.action.ActionManagerImpl;
import net.momirealms.customfishing.mechanic.bag.BagManagerImpl;
import net.momirealms.customfishing.mechanic.block.BlockManagerImpl;
import net.momirealms.customfishing.mechanic.competition.CompetitionManagerImpl;
import net.momirealms.customfishing.mechanic.effect.EffectManagerImpl;
import net.momirealms.customfishing.mechanic.entity.EntityManagerImpl;
import net.momirealms.customfishing.mechanic.fishing.FishingManagerImpl;
import net.momirealms.customfishing.mechanic.game.GameManagerImpl;
import net.momirealms.customfishing.mechanic.hook.HookManagerImpl;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.mechanic.loot.LootManagerImpl;
import net.momirealms.customfishing.mechanic.market.MarketManagerImpl;
import net.momirealms.customfishing.mechanic.misc.ChatCatcherManager;
import net.momirealms.customfishing.mechanic.misc.CoolDownManager;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
import net.momirealms.customfishing.mechanic.statistic.StatisticsManagerImpl;
import net.momirealms.customfishing.mechanic.totem.TotemManagerImpl;
import net.momirealms.customfishing.scheduler.SchedulerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.storage.StorageManagerImpl;
import net.momirealms.customfishing.version.VersionManagerImpl;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.TimeZone;

public class CustomFishingPluginImpl extends CustomFishingPlugin {

    private static ProtocolManager protocolManager;
    private CoolDownManager coolDownManager;
    private ChatCatcherManager chatCatcherManager;

    public CustomFishingPluginImpl() {
        super();
    }

    @Override
    public void onLoad() {
        this.loadDependencies();
    }

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.versionManager = new VersionManagerImpl(this);
        this.disableNBTAPILogs();
        ReflectionUtils.load();

        this.actionManager = new ActionManagerImpl(this);
        this.adventure = new AdventureManagerImpl(this);
        this.bagManager = new BagManagerImpl(this);
        this.blockManager = new BlockManagerImpl(this);
        this.commandManager = new CommandManagerImpl(this);
        this.effectManager = new EffectManagerImpl(this);
        this.fishingManager = new FishingManagerImpl(this);
        this.gameManager = new GameManagerImpl(this);
        this.itemManager = new ItemManagerImpl(this);
        this.lootManager = new LootManagerImpl(this);
        this.marketManager = new MarketManagerImpl(this);
        this.entityManager = new EntityManagerImpl(this);
        this.placeholderManager = new PlaceholderManagerImpl(this);
        this.requirementManager = new RequirementManagerImpl(this);
        this.scheduler = new SchedulerImpl(this);
        this.storageManager = new StorageManagerImpl(this);
        this.competitionManager = new CompetitionManagerImpl(this);
        this.integrationManager = new IntegrationManagerImpl(this);
        this.statisticsManager = new StatisticsManagerImpl(this);
        this.coolDownManager = new CoolDownManager(this);
        this.totemManager = new TotemManagerImpl(this);
        this.hookManager = new HookManagerImpl(this);
        this.chatCatcherManager = new ChatCatcherManager(this);
        this.reload();
        super.initialized = true;

        if (CFConfig.metrics) new Metrics(this, 16648);
        if (CFConfig.updateChecker)
            this.versionManager.checkUpdate().thenAccept(result -> {
                if (!result) this.getAdventure().sendConsoleMessage("[CustomFishing] You are using the latest version.");
                else this.getAdventure().sendConsoleMessage("[CustomFishing] Update is available: <u>https://polymart.org/resource/customfishing.2723<!u>");
            });
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) ((AdventureManagerImpl) this.adventure).close();
        if (this.bagManager != null) ((BagManagerImpl) this.bagManager).disable();
        if (this.blockManager != null) ((BlockManagerImpl) this.blockManager).disable();
        if (this.effectManager != null) ((EffectManagerImpl) this.effectManager).disable();
        if (this.fishingManager != null) ((FishingManagerImpl) this.fishingManager).disable();
        if (this.gameManager != null) ((GameManagerImpl) this.gameManager).disable();
        if (this.itemManager != null) ((ItemManagerImpl) this.itemManager).disable();
        if (this.lootManager != null) ((LootManagerImpl) this.lootManager).disable();
        if (this.marketManager != null) ((MarketManagerImpl) this.marketManager).disable();
        if (this.entityManager != null) ((EntityManagerImpl) this.entityManager).disable();
        if (this.requirementManager != null) ((RequirementManagerImpl) this.requirementManager).disable();
        if (this.scheduler != null) ((SchedulerImpl) this.scheduler).shutdown();
        if (this.integrationManager != null) ((IntegrationManagerImpl) this.integrationManager).disable();
        if (this.competitionManager != null) ((CompetitionManagerImpl) this.competitionManager).disable();
        if (this.storageManager != null) ((StorageManagerImpl) this.storageManager).disable();
        if (this.placeholderManager != null) ((PlaceholderManagerImpl) this.placeholderManager).disable();
        if (this.statisticsManager != null) ((StatisticsManagerImpl) this.statisticsManager).disable();
        if (this.actionManager != null) ((ActionManagerImpl) this.actionManager).disable();
        if (this.totemManager != null) ((TotemManagerImpl) this.totemManager).disable();
        if (this.hookManager != null) ((HookManagerImpl) this.hookManager).disable();
        if (this.coolDownManager != null) this.coolDownManager.disable();
        if (this.chatCatcherManager != null) this.chatCatcherManager.disable();
        if (this.commandManager != null) this.commandManager.unload();
        HandlerList.unregisterAll(this);
    }

    /**
     * Reload the plugin
     */
    @Override
    public void reload() {
        CFConfig.load();
        CFLocale.load();
        ((SchedulerImpl) this.scheduler).reload();
        ((RequirementManagerImpl) this.requirementManager).unload();
        ((RequirementManagerImpl) this.requirementManager).load();
        ((ActionManagerImpl) this.actionManager).unload();
        ((ActionManagerImpl) this.actionManager).load();
        ((GameManagerImpl) this.gameManager).unload();
        ((GameManagerImpl) this.gameManager).load();
        ((ItemManagerImpl) this.itemManager).unload();
        ((ItemManagerImpl) this.itemManager).load();
        ((LootManagerImpl) this.lootManager).unload();
        ((LootManagerImpl) this.lootManager).load();
        ((FishingManagerImpl) this.fishingManager).unload();
        ((FishingManagerImpl) this.fishingManager).load();
        ((TotemManagerImpl) this.totemManager).unload();
        ((TotemManagerImpl) this.totemManager).load();
        ((EffectManagerImpl) this.effectManager).unload();
        ((EffectManagerImpl) this.effectManager).load();
        ((MarketManagerImpl) this.marketManager).unload();
        ((MarketManagerImpl) this.marketManager).load();
        ((BagManagerImpl) this.bagManager).unload();
        ((BagManagerImpl) this.bagManager).load();
        ((BlockManagerImpl) this.blockManager).unload();
        ((BlockManagerImpl) this.blockManager).load();
        ((EntityManagerImpl) this.entityManager).unload();
        ((EntityManagerImpl) this.entityManager).load();
        ((CompetitionManagerImpl) this.competitionManager).unload();
        ((CompetitionManagerImpl) this.competitionManager).load();
        ((StorageManagerImpl) this.storageManager).reload();
        ((StatisticsManagerImpl) this.statisticsManager).unload();
        ((StatisticsManagerImpl) this.statisticsManager).load();
        ((PlaceholderManagerImpl) this.placeholderManager).unload();
        ((PlaceholderManagerImpl) this.placeholderManager).load();
        ((HookManagerImpl) this.hookManager).unload();
        ((HookManagerImpl) this.hookManager).load();
        this.commandManager.unload();
        this.commandManager.load();
        this.coolDownManager.unload();
        this.coolDownManager.load();
        this.chatCatcherManager.unload();
        this.chatCatcherManager.load();

        CustomFishingReloadEvent event = new CustomFishingReloadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Load plugin dependencies
     */
    private void loadDependencies() {
        String mavenRepo = TimeZone.getDefault().getID().startsWith("Asia") ?
                "https://maven.aliyun.com/repository/public/" : "https://repo.maven.apache.org/maven2/";
        LibraryLoader.loadDependencies(
                "org.apache.commons:commons-pool2:2.12.0", mavenRepo,
                "redis.clients:jedis:5.1.0", mavenRepo,
                "dev.dejvokep:boosted-yaml:1.3.1", mavenRepo,
                "com.zaxxer:HikariCP:5.0.1", mavenRepo,
                "net.objecthunter:exp4j:0.4.8", mavenRepo,
                "org.mariadb.jdbc:mariadb-java-client:3.3.0", mavenRepo,
                "com.mysql:mysql-connector-j:8.0.33", mavenRepo,
                "commons-io:commons-io:2.14.0", mavenRepo,
                "com.google.code.gson:gson:2.10.1", mavenRepo,
                "com.h2database:h2:2.2.224", mavenRepo,
                "org.mongodb:mongodb-driver-sync:4.11.1", mavenRepo,
                "org.mongodb:mongodb-driver-core:4.11.1", mavenRepo,
                "org.mongodb:bson:4.11.1", mavenRepo,
                "org.xerial:sqlite-jdbc:3.43.2.2", mavenRepo,
                "dev.jorel:commandapi-bukkit-shade:9.3.0", mavenRepo,
                "xyz.xenondevs.invui:invui-core:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r8:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r9:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r10:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r11:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r12:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r13:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r14:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r15:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r16:1.23", "https://repo.xenondevs.xyz/releases/",
                "xyz.xenondevs.invui:inventory-access-r17:1.23", "https://repo.xenondevs.xyz/releases/"
        );
    }

    /**
     * Disable NBT API logs
     */
    private void disableNBTAPILogs() {
        MinecraftVersion.disableBStats();
        MinecraftVersion.disableUpdateCheck();
        VersionChecker.hideOk = true;
        try {
            Field field = MinecraftVersion.class.getDeclaredField("version");
            field.setAccessible(true);
            MinecraftVersion minecraftVersion;
            try {
                minecraftVersion = MinecraftVersion.valueOf(getVersionManager().getServerVersion().replace("v", "MC"));
            } catch (IllegalArgumentException ex) {
                minecraftVersion = MinecraftVersion.UNKNOWN;
            }
            field.set(MinecraftVersion.class, minecraftVersion);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        boolean hasGsonSupport;
        try {
            Class.forName("com.google.gson.Gson");
            hasGsonSupport = true;
        } catch (Exception ex) {
            hasGsonSupport = false;
        }
        try {
            Field field= MinecraftVersion.class.getDeclaredField("hasGsonSupport");
            field.setAccessible(true);
            field.set(Boolean.class, hasGsonSupport);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a YAML configuration from a file within the plugin's data folder.
     *
     * @param file The name of the configuration file.
     * @return A YamlConfiguration object representing the configuration.
     */
    @Override
    public YamlConfiguration getConfig(String file) {
        File config = new File(this.getDataFolder(), file);
        if (!config.exists()) this.saveResource(file, false);
        return YamlConfiguration.loadConfiguration(config);
    }

    /**
     * Checks if a specified plugin is enabled on the Bukkit server.
     *
     * @param plugin The name of the plugin to check.
     * @return True if the plugin is enabled, false otherwise.
     */
    @Override
    public boolean isHookedPluginEnabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    /**
     * Outputs a debugging message if the debug mode is enabled.
     *
     * @param message The debugging message to be logged.
     */
    @Override
    public void debug(String message) {
        if (!CFConfig.debug) return;
        LogUtils.info(message);
    }

    /**
     * Gets the CoolDownManager instance associated with the plugin.
     *
     * @return The CoolDownManager instance.
     */
    public CoolDownManager getCoolDownManager() {
        return coolDownManager;
    }

    /**
     * Gets the ChatCatcherManager instance associated with the plugin.
     *
     * @return The ChatCatcherManager instance.
     */
    public ChatCatcherManager getChatCatcherManager() {
        return chatCatcherManager;
    }

    /**
     * Retrieves the ProtocolManager instance used for managing packets.
     *
     * @return The ProtocolManager instance.
     */
    @NotNull
    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
