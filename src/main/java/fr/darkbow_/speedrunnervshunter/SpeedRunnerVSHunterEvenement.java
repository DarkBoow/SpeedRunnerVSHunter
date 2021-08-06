package fr.darkbow_.speedrunnervshunter;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import xyz.tozymc.spigot.api.title.TitleApi;

import java.util.Map;
import java.util.Objects;

public class SpeedRunnerVSHunterEvenement implements Listener {
    private final SpeedRunnerVSHunter main;

    public SpeedRunnerVSHunterEvenement(SpeedRunnerVSHunter speedrunnervshunter){this.main = speedrunnervshunter;}

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(!main.isGameStarted()){
            if(event.getEntity() instanceof Player){
                if(main.getConfigurationoptions().containsKey("Disable_Player_Damages")){
                    event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Player_Damages")));
                }
            } else {
                if(main.getConfigurationoptions().containsKey("Disable_Entity_Damages")){
                    event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Entity_Damages")));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(main.getConfig().getBoolean("GameOptions.AssassinsMode")){
            if(main.isGameStarted() && event.getDamager() instanceof Player){
                Player damager = (Player) event.getDamager();
                if(main.getHunters().containsKey(damager)){
                    if(event.getEntity() instanceof Damageable){
                        event.setDamage(((Damageable) event.getEntity()).getHealth());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event){
        if(!main.isGameStarted()){
            if(event.getTarget() instanceof Player){
                if(main.getConfigurationoptions().containsKey("Disable_Player_Targets")){
                    event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Player_Targets")));
                }
            } else {
                if(main.getConfigurationoptions().containsKey("Disable_Entity_Targets")){
                    event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Entity_Targets")));
                }
            }
        }
    }

    @EventHandler
    public void onFoodLoose(FoodLevelChangeEvent event){
        if(!main.isGameStarted()){
            if(main.getConfigurationoptions().containsKey("Disable_Loosing_Food")){
                event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Loosing_Food")));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!main.isGameStarted()){
            event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Block_Place")));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(!main.isGameStarted()){
            event.setCancelled(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_Block_Break")));
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if(!main.isGameStarted()){
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
                if(event.getClickedBlock() != null){
                    if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
                        event.setCancelled(main.getConfig().getBoolean("OffGameProtection.Disable_Open_Block_Inventory"));
                    }
                }
            }
        }

        if(event.getItem() == null){ return; }

        if(event.getItem().getType() == Material.COMPASS){
            NBTItem nbti = new NBTItem(event.getItem());
            if(main.getHunters().containsKey(player)){
                Player tracked = null;
                boolean CibleValide = false;
                if(main.getSpecialPlayerHunterTrack().get(player)){
                    if(main.getHunters().get(player) == player){
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouNeedToChoseAGameFirst")).replace("&", "§"));
                    } else {
                        if(main.getSpeedRunners().containsKey(main.getHunters().get(player))){
                            tracked = main.getHunters().get(player);
                            CibleValide = true;
                        } else {
                            player.sendMessage(main.getLanguageConfig().getString("TargetNotOnlineAnymore").replace("%oldtarget%", main.getHunters().get(player).getName()).replace("&", "§"));
                            return;
                        }
                    }
                } else {
                    double lastDistance = Double.MAX_VALUE;
                    for(Player speedrunner : main.getSpeedRunners().keySet()){
                        if(main.getSpeedRunners().get(speedrunner) && player.getWorld().getEnvironment() == speedrunner.getWorld().getEnvironment()){
                            CibleValide = true;
                            double distance = player.getLocation().distance(speedrunner.getLocation());
                            if(distance < lastDistance){
                                lastDistance = distance;
                                tracked = speedrunner;
                            }
                        } else {
                            tracked = speedrunner;
                        }
                    }
                }

                if(tracked != null){
                    if(CibleValide){
                        if(player.getWorld().getEnvironment() == tracked.getWorld().getEnvironment()){
                            if(tracked.getWorld().getEnvironment() == World.Environment.NORMAL){
                                if(nbti.hasKey("LodestoneDimension")){
                                    event.getItem().setItemMeta(null);
                                }

                                player.setCompassTarget(tracked.getLocation());
                            }

                            if(tracked.getWorld().getEnvironment() == World.Environment.NETHER){
                                CompassMeta compass = (CompassMeta)event.getItem().getItemMeta();
                                if(compass != null){
                                    compass.setLodestone(tracked.getLocation());
                                    compass.setLodestoneTracked(false);
                                }

                                event.getItem().setItemMeta(compass);
                            }
                        }
                    }

                    player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YourCompassPointsTo")).replace("%target%", tracked.getName()).replace("&", "§"));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();

        if(current == null){
            return;
        }

        if(event.getView().getTitle().equals(Objects.requireNonNull(main.getLanguageConfig().getString("ChoseYourSide_InventoryTitle")).replace("&", "§"))){
            event.setCancelled(true);
            player.closeInventory();
            if(main.isGameStarted()){
                player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouCantChoseYourSideAnymore")).replace("&", "§"));
            } else {
                if(current.getType() == Material.SUGAR && Objects.requireNonNull(current.getItemMeta()).getDisplayName().equals("§2§lSpeedRunner")){
                    if(main.getSpeedRunners().containsKey(player)){
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouAreAlreadyASpeedRunner")).replace("&", "§"));
                    } else {
                        if(main.getHunters().containsKey(player)){
                            main.getHunters().remove(player);
                            player.setPlayerListName(player.getName());
                            player.setDisplayName(player.getName());

                            player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouLeftTheSpeedRunnersTeam")).replace("&", "§"));
                            player.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != player){
                                    pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("ASpeedRunnerLeftHisTeam")).replace("%player%", player.getName()).replace("&", "§"));
                                }
                            }
                        }

                        main.addSpeedRunner(player);
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouJoinedTheSpeedRunnersTeam")).replace("&", "§"));
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SomeoneJoinedTheSpeedRunnerTeam")).replace("%player%", player.getName()).replace("&", "§"));
                            }
                        }
                    }
                }

                if(current.getType() == Material.IRON_SWORD && Objects.requireNonNull(current.getItemMeta()).getDisplayName().equals("§c§lChasseur")){
                    if(main.getHunters().containsKey(player)){
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouAreAlreadyAHunter")).replace("&", "§"));
                    } else {
                        if(main.getSpeedRunners().containsKey(player)){
                            main.removeSpeedRunner(player);
                            player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouLeftTheSpeedRunnersTeam")).replace("&", "§"));

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != player){
                                    pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("ASpeedRunnerLeftHisTeam")).replace("%player%", player.getName()).replace("&", "§"));
                                }
                            }
                        }

                        main.getHunters().put(player, player);
                        player.setPlayerListName(Objects.requireNonNull(main.getLanguageConfig().getString("Hunters_PlayerListName")).replace("%player%", player.getName()).replace("&", "§"));
                        player.setDisplayName(Objects.requireNonNull(main.getLanguageConfig().getString("Hunters_PlayerDisplayName")).replace("%player%", player.getName()).replace("&", "§"));
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouJoinedTheHuntersTeam")).replace("&", "§"));
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SomeoneJoinedTheHuntersTeam")).replace("%player%", player.getName()).replace("&", "§"));
                            }
                        }

                        ItemStack compass = new ItemStack(Material.COMPASS, 1);

                        player.getInventory().addItem(compass);
                        if(!main.getSpecialPlayerHunterTrack().containsKey(player)){
                            main.getSpecialPlayerHunterTrack().put(player, false);
                            player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("HunterTargetCommandInfo")).replace("&", "§"));
                        }
                    }
                }
            }
        }

        if(event.getView().getTitle().equals(Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunners_InventoryTitle")).replace("&", "§"))){
            event.setCancelled(true);
            if(current.getType() == Material.PLAYER_HEAD && main.getHunters().containsKey(player)){
                if(Objects.requireNonNull(current.getItemMeta()).getDisplayName().equals("§b§lRandom")){
                    if(main.getSpecialPlayerHunterTrack().get(player)){
                        main.getSpecialPlayerHunterTrack().put(player, false);
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("HuntersTargetModeChangedToRandom")).replace("&", "§"));
                    } else {
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("HuntersTargetModeAlreadyRandom")).replace("&", "§"));
                    }
                    player.closeInventory();
                } else {
                    for(Map.Entry<Player, ItemStack> map : main.getSpedRunnerPlayerHeads().entrySet()){
                        if(current.isSimilar(map.getValue())){
                            Player tracked = map.getKey();
                            if(main.getSpecialPlayerHunterTrack().get(player) && main.getHunters().get(player) == tracked){
                                player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TargetAlreadyChosen")).replace("%target%", main.getHunters().get(player).getName()).replace("&", "§"));
                            } else {
                                main.getSpecialPlayerHunterTrack().put(player, true);
                                main.getHunters().put(player, tracked);
                                player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("NewSpeedRunnerTargetChosed")).replace("%target%", tracked.getName()).replace("&", "§"));
                            }
                            player.closeInventory();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        if(main.isGameStarted()){
            if(main.getSpeedRunners().containsKey(player)){
                if(player.getWorld().getEnvironment() == World.Environment.THE_END){
                    Bukkit.getScheduler().runTaskLater(main.getInstance(), () -> {
                        if(!main.PlayerhasAdvancement(player, "end/kill_dragon")){
                            Bukkit.broadcastMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnerOutOfTheGame")).replace("%player%", player.getName()).replace("&", "§"));
                            main.SpeedRunnerHorsCourse(player);
                            if(!main.getSpeedRunners().containsValue(true)){
                                Bukkit.broadcastMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheHuntersWon")).replace("&", "§"));
                                for(Player pls : Bukkit.getOnlinePlayers()){
                                    if(main.getSpeedRunners().containsKey(pls)){
                                        main.getSpeedRunners().put(pls, true);
                                        TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnersLostBecauseHuntersWon_Title.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnersLostBecauseHuntersWon_Title.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("SpeedRunnersLostBecauseHuntersWon_Title.FadeIn"), main.getLanguageConfig().getInt("SpeedRunnersLostBecauseHuntersWon_Title.Stay"), main.getLanguageConfig().getInt("SpeedRunnersLostBecauseHuntersWon_Title.FadeOut"));
                                    }

                                    if(main.getHunters().containsKey(pls)){
                                        TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("HuntersWonBecauseSpeedRunnersLost_Title.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("HuntersWonBecauseSpeedRunnersLost_Title.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("HuntersWonBecauseSpeedRunnersLost_Title.FadeIn"), main.getLanguageConfig().getInt("HuntersWonBecauseSpeedRunnersLost_Title.Stay"), main.getLanguageConfig().getInt("HuntersWonBecauseSpeedRunnersLost_Title.FadeOut"));
                                    }
                                }

                                main.setGameStarted(false);
                            }
                        }
                    }, 600L);
                } else {
                    Bukkit.broadcastMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnerOutOfTheGame")).replace("%player%", player.getName()).replace("&", "§"));
                    main.SpeedRunnerHorsCourse(player);
                    if(!main.getSpeedRunners().containsValue(true)){
                        Bukkit.broadcastMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheHuntersWon")).replace("&", "§"));
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(main.getSpeedRunners().containsKey(pls)){
                                main.getSpeedRunners().put(pls, true);
                                TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnersLostBecauseHuntersWon_Title.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnersLostBecauseHuntersWon_Title.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("SpeedRunnersLostBecauseHuntersWon_Title.FadeIn"), main.getLanguageConfig().getInt("SpeedRunnersLostBecauseHuntersWon_Title.Stay"), main.getLanguageConfig().getInt("SpeedRunnersLostBecauseHuntersWon_Title.FadeOut"));
                            }

                            if(main.getHunters().containsKey(pls)){
                                TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("HuntersWonBecauseSpeedRunnersLost_Title.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("HuntersWonBecauseSpeedRunnersLost_Title.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("HuntersWonBecauseSpeedRunnersLost_Title.FadeIn"), main.getLanguageConfig().getInt("HuntersWonBecauseSpeedRunnersLost_Title.Stay"), main.getLanguageConfig().getInt("HuntersWonBecauseSpeedRunnersLost_Title.FadeOut"));
                            }
                        }
                        main.setGameStarted(false);
                    }
                }
            }

            if(main.getHunters().containsKey(player)){
                if(player.getInventory().contains(Material.COMPASS)){
                    event.getDrops().remove(new ItemStack(Material.COMPASS, 1));
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        if(main.isGameStarted()){
            if(main.getHunters().containsKey(event.getPlayer())){
                ItemStack compass = new ItemStack(Material.COMPASS, 1);

                event.getPlayer().getInventory().setItem(0, compass);
            }
        }
    }

    @EventHandler
    public void onGotAchievement(PlayerAdvancementDoneEvent event){
        Player player = event.getPlayer();
        if(main.isGameStarted()){
            if(main.getSpeedRunners().containsKey(player) && main.getSpeedRunners().get(player)){
                if(event.getAdvancement().getKey().getKey().equals("end/kill_dragon")){
                    Bukkit.broadcastMessage("§6Les SpeedRunners ont GAGNÉ la CHASSE !");
                    for(Player pls : Bukkit.getOnlinePlayers()){
                        if(main.getSpeedRunners().containsKey(pls)){
                            main.getSpeedRunners().put(pls, true);
                            TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnersWonBecauseHuntersLost_Title.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnersWonBecauseHuntersLost_Title.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("SpeedRunnersWonBecauseHuntersLost_Title.FadeIn"), main.getLanguageConfig().getInt("SpeedRunnersWonBecauseHuntersLost_Title.Stay"), main.getLanguageConfig().getInt("SpeedRunnersWonBecauseHuntersLost_Title.FadeOut"));
                            pls.setHealth(pls.getMaxHealth());
                            pls.setFoodLevel(20);
                        }

                        if(main.getHunters().containsKey(pls)){
                            TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("HuntersLostBecauseSpeedRunnersWon_Title.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("HuntersLostBecauseSpeedRunnersWon_Title.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("HuntersLostBecauseSpeedRunnersWon_Title.FadeIn"), main.getLanguageConfig().getInt("HuntersLostBecauseSpeedRunnersWon_Title.Stay"), main.getLanguageConfig().getInt("HuntersLostBecauseSpeedRunnersWon_Title.FadeOut"));
                            pls.setHealth(pls.getMaxHealth());
                            pls.setFoodLevel(20);
                        }
                    }
                    main.setGameStarted(false);
                }
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        if(main.isGameStarted() && main.getConfig().getBoolean("OffGameProtection.Disable_Item_Drop")){
            if(main.getHunters().containsKey(event.getPlayer())){
                if(event.getItemDrop().getItemStack().getType() == Material.COMPASS){
                    int compasscount = 0;
                    for(ItemStack it : event.getPlayer().getInventory().getContents()){
                        if(it != null && it.getType() == Material.COMPASS){
                            compasscount += it.getAmount();
                        }
                    }

                    if(compasscount == 0){
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouCantDropYourLastCompass")).replace("&", "§"));
                    }
                }
            }
        }

        if(!main.isGameStarted()){
            event.setCancelled(main.getConfig().getBoolean("OffGameProtection.Disable_Item_Drop"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(main.getConfig().getBoolean("GameOptions.AssassinsMode")){
            if(main.isGameStarted()){
                if(main.getHunters().containsKey(event.getPlayer())){
                    if(main.getFrozenHunters().contains(event.getPlayer())){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}