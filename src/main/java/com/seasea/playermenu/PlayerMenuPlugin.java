package com.seasea.playermenu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OP専用の管理ダイアログ(PlayerMenu)を開くプラグイン。
 * バニラのDialog APIを使っているため、クライアントMod無しで
 * 通常のゲームメニューと同じ見た目のウィンドウが開く。
 */
public class PlayerMenuPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        var cmd = getCommand("playermenu");
        if (cmd != null) {
            cmd.setExecutor(this);
        }
        getLogger().info("PlayerMenu が有効になりました。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはゲーム内から実行してください。");
            return true;
        }
        if (!player.hasPermission("playermenu.use")) {
            player.sendMessage(Component.text("この操作を行う権限がありません。", NamedTextColor.RED));
            return true;
        }
        player.showDialog(buildMainMenu());
        return true;
    }

    // ============ メインメニュー ============

    private Dialog buildMainMenu() {
        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(categoryButton("ゲームモード", "creative,survival,adventure,spectatorへの変更", p -> p.showDialog(buildGameModeMenu())));
        buttons.add(categoryButton("時間", "昼/夜/正午/深夜への変更", p -> p.showDialog(buildTimeMenu())));
        buttons.add(categoryButton("天候", "晴れ/雨/雷雨への変更", p -> p.showDialog(buildWeatherMenu())));
        buttons.add(categoryButton("状態回復", "体力/満腹度の全回復", p -> p.showDialog(buildHealMenu())));
        buttons.add(categoryButton("インベントリ", "所持アイテムのクリア", p -> p.showDialog(buildInventoryMenu())));
        buttons.add(categoryButton("移動", "ワールドスポーンへテレポート", p -> p.showDialog(buildTeleportMenu())));
        buttons.add(categoryButton("難易度", "ワールド難易度の変更", p -> p.showDialog(buildDifficultyMenu())));
        buttons.add(categoryButton("サーバー情報", "オンラインプレイヤー・TPSの確認", p -> p.showDialog(buildServerInfoDialog())));

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("PlayerMenu", NamedTextColor.AQUA))
                        .body(List.of(io.papermc.paper.registry.data.dialog.body.DialogBody.plainMessage(
                                Component.text("操作したいカテゴリを選んでください。", NamedTextColor.GRAY)
                        )))
                        .build())
                .type(DialogType.multiAction(buttons).build())
        );
    }

    private ActionButton categoryButton(String label, String tooltip, java.util.function.Consumer<Player> onClick) {
        return ActionButton.builder(Component.text(label))
                .tooltip(Component.text(tooltip, NamedTextColor.GRAY))
                .width(200)
                .action(DialogAction.customClick(
                        (view, audience) -> {
                            if (audience instanceof Player p) {
                                onClick.accept(p);
                            }
                        },
                        ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()
                ))
                .build();
    }

    // ============ ゲームモード ============

    private Dialog buildGameModeMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("サバイバル", GameMode.SURVIVAL.name(), p -> {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.sendMessage(Component.text("ゲームモードをサバイバルに変更しました。", NamedTextColor.GREEN));
                }),
                actionButton("クリエイティブ", GameMode.CREATIVE.name(), p -> {
                    p.setGameMode(GameMode.CREATIVE);
                    p.sendMessage(Component.text("ゲームモードをクリエイティブに変更しました。", NamedTextColor.GREEN));
                }),
                actionButton("アドベンチャー", GameMode.ADVENTURE.name(), p -> {
                    p.setGameMode(GameMode.ADVENTURE);
                    p.sendMessage(Component.text("ゲームモードをアドベンチャーに変更しました。", NamedTextColor.GREEN));
                }),
                actionButton("スペクテイター", GameMode.SPECTATOR.name(), p -> {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage(Component.text("ゲームモードをスペクテイターに変更しました。", NamedTextColor.GREEN));
                })
        );
        return simpleActionDialog("ゲームモードを選択", buttons);
    }

    // ============ 時間 ============

    private Dialog buildTimeMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("朝(0時)", "time set day", p -> setTime(p, 0)),
                actionButton("正午(6000)", "time set noon", p -> setTime(p, 6000)),
                actionButton("夜(13000)", "time set night", p -> setTime(p, 13000)),
                actionButton("深夜(18000)", "time set midnight", p -> setTime(p, 18000))
        );
        return simpleActionDialog("時間を選択", buttons);
    }

    private void setTime(Player p, long ticks) {
        World world = p.getWorld();
        world.setTime(ticks);
        p.sendMessage(Component.text("時間を変更しました。", NamedTextColor.GREEN));
    }

    // ============ 天候 ============

    private Dialog buildWeatherMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("晴れ", "weather clear", p -> {
                    World w = p.getWorld();
                    w.setStorm(false);
                    w.setThundering(false);
                    p.sendMessage(Component.text("天候を晴れに変更しました。", NamedTextColor.GREEN));
                }),
                actionButton("雨", "weather rain", p -> {
                    World w = p.getWorld();
                    w.setStorm(true);
                    w.setThundering(false);
                    p.sendMessage(Component.text("天候を雨に変更しました。", NamedTextColor.GREEN));
                }),
                actionButton("雷雨", "weather thunder", p -> {
                    World w = p.getWorld();
                    w.setStorm(true);
                    w.setThundering(true);
                    p.sendMessage(Component.text("天候を雷雨に変更しました。", NamedTextColor.GREEN));
                })
        );
        return simpleActionDialog("天候を選択", buttons);
    }

    // ============ 状態回復 ============

    private Dialog buildHealMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("体力を全回復", "heal", p -> {
                    var maxHealthAttr = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                    double max = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
                    p.setHealth(max);
                    p.sendMessage(Component.text("体力を全回復しました。", NamedTextColor.GREEN));
                }),
                actionButton("満腹度を全回復", "feed", p -> {
                    p.setFoodLevel(20);
                    p.setSaturation(20f);
                    p.sendMessage(Component.text("満腹度を全回復しました。", NamedTextColor.GREEN));
                }),
                actionButton("体力+満腹度 両方", "heal & feed", p -> {
                    var maxHealthAttr = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                    double max = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
                    p.setHealth(max);
                    p.setFoodLevel(20);
                    p.setSaturation(20f);
                    p.sendMessage(Component.text("体力と満腹度を全回復しました。", NamedTextColor.GREEN));
                })
        );
        return simpleActionDialog("状態回復", buttons);
    }

    // ============ インベントリ ============

    private Dialog buildInventoryMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("インベントリをクリア", "clear", p -> {
                    p.getInventory().clear();
                    p.sendMessage(Component.text("インベントリをクリアしました。", NamedTextColor.GREEN));
                })
        );
        return simpleActionDialog("インベントリ操作", buttons);
    }

    // ============ 移動 ============

    private Dialog buildTeleportMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("ワールドスポーンへ", "tp spawn", p -> {
                    p.teleport(p.getWorld().getSpawnLocation());
                    p.sendMessage(Component.text("ワールドスポーンへテレポートしました。", NamedTextColor.GREEN));
                })
        );
        return simpleActionDialog("移動", buttons);
    }

    // ============ 難易度 ============

    private Dialog buildDifficultyMenu() {
        List<ActionButton> buttons = List.of(
                actionButton("ピースフル", "difficulty peaceful", p -> setDifficulty(p, org.bukkit.Difficulty.PEACEFUL)),
                actionButton("イージー", "difficulty easy", p -> setDifficulty(p, org.bukkit.Difficulty.EASY)),
                actionButton("ノーマル", "difficulty normal", p -> setDifficulty(p, org.bukkit.Difficulty.NORMAL)),
                actionButton("ハード", "difficulty hard", p -> setDifficulty(p, org.bukkit.Difficulty.HARD))
        );
        return simpleActionDialog("難易度を選択", buttons);
    }

    private void setDifficulty(Player p, org.bukkit.Difficulty difficulty) {
        p.getWorld().setDifficulty(difficulty);
        p.sendMessage(Component.text("難易度を " + difficulty.name() + " に変更しました。", NamedTextColor.GREEN));
    }

    // ============ サーバー情報(通知ダイアログ) ============

    private Dialog buildServerInfoDialog() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        String names = online.stream().map(Player::getName).collect(Collectors.joining(", "));
        if (names.isEmpty()) {
            names = "(オンラインプレイヤーなし)";
        }
        double tps = Bukkit.getServer().getTPS()[0];

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("サーバー情報", NamedTextColor.AQUA))
                        .body(List.of(
                                io.papermc.paper.registry.data.dialog.body.DialogBody.plainMessage(
                                        Component.text(String.format("TPS: %.2f", tps), NamedTextColor.YELLOW)
                                ),
                                io.papermc.paper.registry.data.dialog.body.DialogBody.plainMessage(
                                        Component.text("オンライン: " + names, NamedTextColor.WHITE)
                                )
                        ))
                        .build())
                .type(DialogType.notice())
        );
    }

    // ============ 共通ヘルパー ============

    /** 実行系のボタンを作る。クリックすると action を実行し、ダイアログを閉じる。 */
    private ActionButton actionButton(String label, String tooltip, java.util.function.Consumer<Player> action) {
        return ActionButton.builder(Component.text(label))
                .tooltip(Component.text(tooltip, NamedTextColor.GRAY))
                .width(200)
                .action(DialogAction.customClick(
                        (view, audience) -> {
                            if (audience instanceof Player p) {
                                if (!p.hasPermission("playermenu.use")) {
                                    p.sendMessage(Component.text("権限がありません。", NamedTextColor.RED));
                                    return;
                                }
                                action.accept(p);
                            }
                        },
                        ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()
                ))
                .build();
    }

    /** 複数のアクションボタンを並べただけのシンプルなダイアログを作る。 */
    private Dialog simpleActionDialog(String title, List<ActionButton> buttons) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(title, NamedTextColor.AQUA)).build())
                .type(DialogType.multiAction(buttons).build())
        );
    }
}
