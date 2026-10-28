package top.earthstudio.nextgenbedwars.core.command.edit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.command.edit.EditCommand;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;

import top.earthstudio.nextgenbedwars.api.util.ColorUtil;
import top.earthstudio.nextgenbedwars.core.game.GameManager;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EditCommandImpl implements EditCommand { // TODO: i18n
    public static LiteralCommandNode<CommandSourceStack> root;

    private static EditCommandImpl handler;

    public EditCommandImpl() {
        EditCommand.set(this);
        handler = this;
    }

    @Override
    public void addSubCommand(LiteralArgumentBuilder<CommandSourceStack> subCommandBuilder) {
        var existingReq = subCommandBuilder.getRequirement();
        subCommandBuilder.requires(source ->
                handler.requireInEditingWorld(source) && (existingReq == null || existingReq.test(source))
        );
        root.addChild(subCommandBuilder.build());
        notifyEditingPlayers();
    }

    public static void build(LiteralArgumentBuilder<CommandSourceStack> root) {
        new EditCommandImpl();

        handler.registerInternalSubCommands(root);

        root.then(
                Commands.literal("setDisplayName")
                        .requires(handler::requireInEditingWorld)
                        .then(
                                Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(handler::executeSetDisplayName)
                        )
        ).then(
                Commands.literal("setMaxPlayers")
                        .requires(handler::requireInEditingWorld)
                        .then(
                                Commands.argument("number", IntegerArgumentType.integer())
                                        .executes(handler::executeSetMaxPlayers)
                        )
        ).then(
                Commands.literal("setMinPlayers")
                        .requires(handler::requireInEditingWorld)
                        .then(
                                Commands.argument("number", IntegerArgumentType.integer())
                                        .executes(handler::executeSetMinPlayers)
                        )
        ).then(
                Commands.literal("setLobby")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeSetLobby)
        ).then(
                Commands.literal("setSpectator")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeSetSpectator)
        ).then(
                Commands.literal("save")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeSave)
        ).then(
                Commands.literal("exit")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeExit)
        );
    }

    private void registerInternalSubCommands(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(
                Commands.literal("spawner")
                        .requires(handler::requireInEditingWorld)
                        .then(
                        Commands.literal("add").then(
                                Commands.argument("type", StringArgumentType.word())
                                        .suggests(this::suggestSpawnerTypes)
                                        .executes(this::executeAddSpawner)
                        )
                ).then(
                        Commands.literal("remove").executes(this::executeRemoveSpawner)
                )
        ).then(
                Commands.literal("shop")
                        .requires(handler::requireInEditingWorld)
                        .then(
                                Commands.literal("add")
                                        .executes(this::executeAddShopDefault)
                                        .then(
                                                Commands.argument("type", StringArgumentType.word())
                                                        .suggests(this::suggestShopTypes)
                                                        .executes(this::executeAddShopWithType)
                                        )
                        ).then(
                                Commands.literal("remove").executes(this::executeRemoveShop)
                        )
        ).then(
                Commands.literal("team")
                        .requires(handler::requireInEditingWorld)
                        .then(
                                Commands.literal("add").then(
                                        Commands.argument("color", StringArgumentType.word())
                                                .suggests(this::suggestColors)
                                                .executes(this::executeAddTeam)
                                )
                        ).then(
                                Commands.literal("remove").then(
                                        Commands.argument("color", StringArgumentType.word())
                                                .suggests(this::suggestExistingTeams)
                                                .executes(this::executeRemoveTeam)
                                )
                        ).then(
                                Commands.literal("setBed").then(
                                        Commands.argument("color", StringArgumentType.word())
                                                .suggests(this::suggestExistingTeams)
                                                .executes(this::executeSetBed)
                                )
                        ).then(
                                Commands.literal("setRespawn").then(
                                        Commands.argument("color", StringArgumentType.word())
                                                .suggests(this::suggestExistingTeams)
                                                .executes(this::executeSetRespawn)
                                )
                        )
        );
    }

    // ============ Suggestion ============

    private boolean requireInEditingWorld(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player player)) return false;
        if (!player.hasPermission("nextgenbedwars.admin.edit")) return false;

        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        return instance != null && instance.isEditMode();
    }

    private CompletableFuture<Suggestions> suggestExistingTeams(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        if (instance == null) return builder.buildFuture();

        TeamManager teamManager = instance.get(TeamManager.class);
        String remaining = builder.getRemaining().toLowerCase();

        for (Team team : teamManager.getAll()) {
            if (team.teamColor != null) {
                DyeColor dye = DyeColor.getByColor(team.teamColor);
                String name = dye != null ? dye.name().toLowerCase() : String.format("#%06X", team.teamColor.asRGB());
                if (name.startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
        }

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestColors(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        if (instance == null) return builder.buildFuture();

        TeamManager teamManager = instance.get(TeamManager.class);
        String remaining = builder.getRemaining().toLowerCase();

        Set<Color> existingColors = new ObjectOpenHashSet<>();
        for (Team team : teamManager.getAll()) {
            if (team.teamColor != null) {
                existingColors.add(team.teamColor);
            }
        }

        for (DyeColor dye : DyeColor.values()) {
            String name = dye.name().toLowerCase();
            if (name.startsWith(remaining) && !existingColors.contains(dye.getColor())) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSpawnerTypes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        if (instance == null) return builder.buildFuture();

        String remaining = builder.getRemaining().toLowerCase();
        SpawnerManager spawnerManager = instance.get(SpawnerManager.class);

        for (String type : spawnerManager.getTypes()) {
            if (type.toLowerCase().startsWith(remaining)) {
                builder.suggest(type);
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestShopTypes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) { // TODO: more type
        String remaining = builder.getRemaining().toLowerCase();
        List<String> types = List.of("item_shop");
        for (String type : types) {
            if (type.startsWith(remaining)) {
                builder.suggest(type);
            }
        }
        return builder.buildFuture();
    }

    // ============ Team ============

    private int executeAddTeam(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        TeamManager teamManager = instance.get(TeamManager.class);

        String colorStr = StringArgumentType.getString(ctx, "color");
        DyeColor dyeColor = parseDyeColor(colorStr);
        if (dyeColor == null) {
            player.sendMessage(Component.text("未知的颜色类型: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        Color color = dyeColor.getColor();
        if (teamManager.get(color) != null) {
            player.sendMessage(Component.text("该颜色的队伍已存在: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        Team team = new Team();
        team.teamColor = color;
        team.maxPlayerCount = 4;
        team.respawnTick = 100;
        team.respawnLocation = player.getLocation();
        team.displayName = Component.text(colorStr.toUpperCase() + "队").color(TextColor.color(color.asRGB()));

        teamManager.add(team);
        player.sendMessage(Component.text("成功创建队伍: ").color(NamedTextColor.GREEN).append(team.displayName));
        return Command.SINGLE_SUCCESS;
    }

    private int executeRemoveTeam(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        TeamManager teamManager = instance.get(TeamManager.class);

        String colorStr = StringArgumentType.getString(ctx, "color");
        DyeColor dyeColor = parseDyeColor(colorStr);
        if (dyeColor == null) {
            player.sendMessage(Component.text("未知的颜色类型: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        Team team = teamManager.get(dyeColor.getColor());
        if (team == null) {
            player.sendMessage(Component.text("未找到该颜色的队伍: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        if (team.uuid != null) {
            teamManager.remove(team.uuid);
        }
        player.sendMessage(Component.text("已成功移除队伍: ").color(NamedTextColor.GREEN).append(team.displayName));
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetBed(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        TeamManager teamManager = instance.get(TeamManager.class);

        String colorStr = StringArgumentType.getString(ctx, "color");
        DyeColor dyeColor = parseDyeColor(colorStr);
        if (dyeColor == null) {
            player.sendMessage(Component.text("未知的颜色类型: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        Team team = teamManager.get(dyeColor.getColor());
        if (team == null) {
            player.sendMessage(Component.text("未找到该颜色的队伍: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        Block block = player.getTargetBlockExact(5);
        if (block == null || !Tag.BEDS.isTagged(block.getType())) {
            player.sendMessage(Component.text("请将准星对准 5 格内的床方块！").color(NamedTextColor.RED));
            return 0;
        }

        if (!(block.getBlockData() instanceof Bed bedData)) {
            player.sendMessage(Component.text("目标方块不是有效的床！").color(NamedTextColor.RED));
            return 0;
        }

        Block headBlock = bedData.getPart() == Bed.Part.HEAD
                ? block
                : block.getRelative(bedData.getFacing());
        Block footBlock = bedData.getPart() == Bed.Part.FOOT
                ? block
                : block.getRelative(bedData.getFacing().getOppositeFace());

        long headLong = BlockPosUtil.asLong(headBlock);
        long footLong = BlockPosUtil.asLong(footBlock);

        team.bedBlockLongs = LongLongPair.of(headLong, footLong);
        team.isBedAlive = true;

        player.sendMessage(Component.text("已成功将所视床设置为 ").color(NamedTextColor.GREEN)
                .append(team.displayName)
                .append(Component.text(" 的床！").color(NamedTextColor.GREEN)));
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetRespawn(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        TeamManager teamManager = instance.get(TeamManager.class);

        String colorStr = StringArgumentType.getString(ctx, "color");
        DyeColor dyeColor = parseDyeColor(colorStr);
        if (dyeColor == null) {
            player.sendMessage(Component.text("未知的颜色类型: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        Team team = teamManager.get(dyeColor.getColor());
        if (team == null) {
            player.sendMessage(Component.text("未找到该颜色的队伍: " + colorStr).color(NamedTextColor.RED));
            return 0;
        }

        team.respawnLocation = player.getLocation();
        player.sendMessage(Component.text("已将当前位置设为 ").color(NamedTextColor.GREEN)
                .append(team.displayName)
                .append(Component.text(" 的重生点！").color(NamedTextColor.GREEN)));
        return Command.SINGLE_SUCCESS;
    }

    // ============ Shop ============

    private int executeAddShopDefault(CommandContext<CommandSourceStack> ctx) {
        return doAddShop(ctx, "item_shop");
    }

    private int executeAddShopWithType(CommandContext<CommandSourceStack> ctx) {
        String type = StringArgumentType.getString(ctx, "type");
        return doAddShop(ctx, type);
    }

    private int doAddShop(CommandContext<CommandSourceStack> ctx, String shopType) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;

        ShopManager shopManager = instance.get(ShopManager.class);
        Shop shop = new Shop();
        Location location = player.getLocation();
        location.setPitch(0);

        shop.entityType = EntityType.VILLAGER;
        shop.location = location;
        shop.shopType = shopType;
        shop.title = Component.text("物品商店");
        shop.subTitle = Component.text("右键打开");

        shopManager.add(shop);
        player.sendMessage(Component.text("成功放置商店村民 (" + shopType + ")！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeRemoveShop(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;

        ShopManager shopManager = instance.get(ShopManager.class);
        List<Shop> allShops = shopManager.getAll();

        Shop target = findNearestIn3x3(player.getLocation(), allShops, shop -> shop.location);

        if (target == null) {
            player.sendMessage(Component.text("附近 3x3 空间内未找到任何商店！").color(NamedTextColor.RED));
            return 0;
        }

        shopManager.remove(target.uuid);
        player.sendMessage(Component.text("已成功移除附近的商店！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    // ============ Spawner ============

    private int executeAddSpawner(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        SpawnerManager spawnerManager = instance.get(SpawnerManager.class);

        String typeName = StringArgumentType.getString(ctx, "type");
        SpawnerType spawnerType = spawnerManager.getSpawnerType(typeName);

        if (spawnerType == null) {
            player.sendMessage(Component.text("未知的生成器类型: " + typeName).color(NamedTextColor.RED));
            return 0;
        }

        Spawner spawner = new Spawner();
        spawner.type = typeName;
        spawner.location = player.getLocation();
        spawner.level = 0;
        spawner.spawnerType = spawnerType;

        spawnerManager.add(spawner);
        player.sendMessage(Component.text("成功放置生成器: " + typeName).color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeRemoveSpawner(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;

        SpawnerManager spawnerManager = instance.get(SpawnerManager.class);
        List<Spawner> allSpawners = spawnerManager.getAll();

        Spawner target = findNearestIn3x3(player.getLocation(), allSpawners, s -> s.location);

        if (target == null) {
            player.sendMessage(Component.text("附近 3x3 空间内未找到任何生成器！").color(NamedTextColor.RED));
            return 0;
        }

        spawnerManager.remove(target.uuid);
        player.sendMessage(Component.text("已成功移除附近的生成器: " + target.type).color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    // ============ Game ============

    private int executeSetMinPlayers(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        int number = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "number");
        Player player = (Player) commandSourceStackCommandContext.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        if (number < 2) {
            player.sendMessage(Component.text("最低人数不能小于 2！").color(NamedTextColor.RED));
            return 0;
        }

        assert instance != null;
        instance.getGame().minPlayers = number;

        player.sendMessage(Component.text("最低人数已设置为：").color(NamedTextColor.GREEN).append(Component.text(number)));

        if (number > instance.getGame().maxPlayers) {
            instance.getGame().maxPlayers = number;
            player.sendMessage(Component.text("由于最小人数超过最大人数，最大人数已设置为：").color(NamedTextColor.YELLOW).append(Component.text(number)));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeSetDisplayName(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");
        Player player = (Player) commandSourceStackCommandContext.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        assert instance != null;
        instance.getGame().displayName = ColorUtil.toTextComponent(name);

        player.sendMessage(Component.text("实例显示名称已设置为：").color(NamedTextColor.GREEN).append(instance.getGame().displayName));

        return Command.SINGLE_SUCCESS;
    }

    private int executeSetMaxPlayers(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        int number = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "number");
        Player player = (Player) commandSourceStackCommandContext.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        assert instance != null;
        instance.getGame().maxPlayers = number;

        if (number < 2) {
            player.sendMessage(Component.text("最大人数不能小于 2！").color(NamedTextColor.RED));
            return 0;
        }

        player.sendMessage(Component.text("最大人数已设置为：").color(NamedTextColor.GREEN).append(Component.text(number)));

        if (number < instance.getGame().minPlayers) {
            instance.getGame().minPlayers = number;
            player.sendMessage(Component.text("由于最大人数少于最小人数，最小人数已设置为：").color(NamedTextColor.YELLOW).append(Component.text(number)));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeSetLobby(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        instance.getGame().waitingLobby = player.getLocation();
        player.sendMessage(Component.text("已将当前位置设为等待大厅 (waitingLobby)！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetSpectator(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        instance.getGame().spectatorRespawnPoint = player.getLocation();
        player.sendMessage(Component.text("已将当前位置设为观察者重生点 (spectatorRespawnPoint)！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    // ============ IO and Exit ============

    private int executeSave(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        assert instance != null;
        instance.saveGame();
        player.sendMessage(Component.text("已成功保存地图与所有子系统配置！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeExit(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        player.sendMessage(Component.text("正在退出编辑模式...").color(NamedTextColor.YELLOW));

        assert instance != null;
        GameManager.removeInstanceByName(instance.getGame().name);

        Location lobby = Bukkit.getWorlds().getFirst().getSpawnLocation();
        player.teleport(lobby);

        player.sendMessage(Component.text("已退出编辑模式").color(NamedTextColor.GREEN));

        player.updateCommands();

        return Command.SINGLE_SUCCESS;
    }

    // ============ Helper ============

    private void notifyEditingPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
            if (instance != null && instance.isEditMode()) {
                player.updateCommands();
            }
        }
    }

    /**
     * 在以 center 为中心、3x3 空间范围内寻找距离最近的目标对象
     *
     * @param center 玩家坐标
     * @param candidates 候选集合（例如所有 Spawner 或所有 Shop）
     * @param locationGetter 从对象中获取 Location 的函数
     * @return 3x3 空间内最近的对象，若范围内没有任何对象则返回 null
     */
    private <T> T findNearestIn3x3(Location center, Collection<T> candidates, java.util.function.Function<T, Location> locationGetter) {
        T nearest = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (T item : candidates) {
            Location loc = locationGetter.apply(item);
            if (loc == null || loc.getWorld() != center.getWorld()) continue;

            double dx = Math.abs(loc.getX() - center.getX());
            double dy = Math.abs(loc.getY() - center.getY());
            double dz = Math.abs(loc.getZ() - center.getZ());

            if (dx <= 1.5 && dz <= 1.5 && dy <= 1.5) {
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    nearest = item;
                }
            }
        }

        return nearest;
    }

    private static DyeColor parseDyeColor(String name) {
        if (name == null) return null;
        try {
            return DyeColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}