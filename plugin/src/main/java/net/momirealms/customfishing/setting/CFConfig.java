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

package net.momirealms.customfishing.setting;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.OffsetUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CFConfig {

    // config version
    public static String configVersion = "32";
    // Debug mode
    public static boolean debug;
    // language
    public static String language;

    // update checker
    public static boolean updateChecker;

    // BStats
    public static boolean metrics;

    // fishing event priority
    public static EventPriority eventPriority;

    // thread pool settings
    public static int corePoolSize;
    public static int maximumPoolSize;
    public static int keepAliveTime;

    // detection order for item id
    public static List<String> itemDetectOrder;
    public static List<String> blockDetectOrder;

    // fishing bag
    public static boolean enableFishingBag;

    // Fishing wait time
    public static boolean overrideVanilla;
    public static int waterMinTime;
    public static int waterMaxTime;
    // Lava fishing
    public static int lavaMinTime;
    public static int lavaMaxTime;

    // Competition
    public static boolean redisRanking;
    public static String serverGroup;
    public static int placeholderLimit;

    // Data save interval
    public static int dataSaveInterval;
    // Lock data on join
    public static boolean lockData;
    public static boolean logDataSaving;

    public static boolean restrictedSizeRange;

    // Legacy color code support
    public static boolean legacyColorSupport;
    // Durability lore
    public static List<String> durabilityLore;

    public static boolean globalShowInFinder;
    public static boolean globalDisableStats;
    public static boolean globalDisableGame;
    public static boolean globalInstantGame;

    public static int multipleLootSpawnDelay;

    public static void load() {
        try {
            YamlDocument.create(
                    new File(CustomFishingPlugin.getInstance().getDataFolder(), "config.yml"),
                    Objects.requireNonNull(CustomFishingPlugin.getInstance().getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings
                            .builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings
                            .builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .addIgnoredRoute(configVersion, "mechanics.mechanic-requirements", '.')
                            .addIgnoredRoute(configVersion, "mechanics.global-events", '.')
                            .addIgnoredRoute(configVersion, "mechanics.global-effects", '.')
                            .addIgnoredRoute(configVersion, "mechanics.fishing-bag.collect-actions", '.')
                            .addIgnoredRoute(configVersion, "mechanics.fishing-bag.full-actions", '.')
                            .addIgnoredRoute(configVersion, "other-settings.placeholder-register", '.')
                            .build()
            );
            loadSettings(CustomFishingPlugin.getInstance().getConfig("config.yml"));
        } catch (IOException e) {
            LogUtils.warn(e.getMessage());
        }
    }

    private static void loadSettings(YamlConfiguration config) {
        debug = config.getBoolean("debug", false);

        language = config.getString("lang", "english");
        updateChecker = config.getBoolean("update-checker");
        metrics = config.getBoolean("metrics");
        eventPriority = EventPriority.valueOf(config.getString("other-settings.event-priority", "NORMAL").toUpperCase(Locale.ENGLISH));

        corePoolSize = config.getInt("other-settings.thread-pool-settings.corePoolSize", 1);
        maximumPoolSize = config.getInt("other-settings.thread-pool-settings.maximumPoolSize", 1);
        keepAliveTime = config.getInt("other-settings.thread-pool-settings.keepAliveTime", 30);

        itemDetectOrder = config.getStringList("other-settings.item-detection-order");
        blockDetectOrder = config.getStringList("other-settings.block-detection-order");

        enableFishingBag = config.getBoolean("mechanics.fishing-bag.enable", true);

        overrideVanilla = config.getBoolean("mechanics.fishing-wait-time.override-vanilla", false);
        waterMinTime = config.getInt("mechanics.fishing-wait-time.min-wait-time", 100);
        waterMaxTime = config.getInt("mechanics.fishing-wait-time.min-wait-time", 600);

        lavaMinTime = config.getInt("mechanics.lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("mechanics.lava-fishing.max-wait-time", 600);

        restrictedSizeRange = config.getBoolean("mechanics.size.restricted-size-range", true);

        globalShowInFinder = config.getBoolean("mechanics.global-loot-property.show-in-fishfinder", true);
        globalDisableStats = config.getBoolean("mechanics.global-loot-property.disable-stat", false);
        globalDisableGame = config.getBoolean("mechanics.global-loot-property.disable-game", false);
        globalInstantGame = config.getBoolean("mechanics.global-loot-property.instant-game", false);

        redisRanking = config.getBoolean("mechanics.competition.redis-ranking", false);
        placeholderLimit = config.getInt("mechanics.competition.placeholder-limit", 3);
        serverGroup = config.getString("mechanics.competition.server-group","default");

        multipleLootSpawnDelay = config.getInt("mechanics.multiple-loot-spawn-delay", 0);

        dataSaveInterval = config.getInt("other-settings.data-saving-interval", 600);
        logDataSaving = config.getBoolean("other-settings.log-data-saving", true);
        lockData = config.getBoolean("other-settings.lock-data", true);
        legacyColorSupport = config.getBoolean("other-settings.legacy-color-code-support", false);

        durabilityLore = config.getStringList("other-settings.custom-durability-format").stream().map(it -> "<!i>" + it).toList();

        OffsetUtils.loadConfig(config.getConfigurationSection("other-settings.offset-characters"));
    }
}
