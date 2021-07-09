package fr.darkbow_.speedrunnervshunter;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.Map;
import java.util.Objects;

public class SpeedRunnerVSHunterEvenement implements Listener {
    private SpeedRunnerVSHunter main;

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
        if(event.getItem() == null){ return; }

        if(event.getItem().getType() == Material.COMPASS){
            NBTItem nbti = new NBTItem(event.getItem());
            if(main.getHunters().containsKey(player)){
                Player tracked = null;
                boolean CibleValide = false;
                if(main.getSpecialPlayerHunterTrack().get(player)){
                    if(main.getHunters().get(player) == player){
                        player.sendMessage("§cTu dois d'abord exécuter la commande §l/speedrunner cible §cpour choisir ta cible, en raison de ton mode de ciblage.");
                    } else {
                        if(main.getSpeedRunners().containsKey(main.getHunters().get(player))){
                            tracked = main.getHunters().get(player);
                            CibleValide = true;
                        } else {
                            player.sendMessage("§cLe joueur " + main.getHunters().get(player).getName() + " que tu avais ciblé n'est plus connecté ou n'est plus SpeedRunner !\n§c§lVeille à choisir le mode de ciblage de Proximité ou Pointer un autre SpeedRunner si ton précédent choix ne redevient pas SpeedRunner ! (en faisant un Clic Gauche avec ta Boussole en main)");
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

                    player.sendMessage("§aTa boussole pointe vers §2§l" + tracked.getName() + "§a.");
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

        if(event.getView().getTitle().equals("§9§lChoisis ton Camp")){
            event.setCancelled(true);
            player.closeInventory();
            if(main.isGameStarted()){
                player.sendMessage("§cLa chasse a déjà commencé, donc tu ne peux plus choisir ton camp.\n§cLa seule manière de choisir ton camp est de stopper la chasse.");
            } else {
                if(current.getType() == Material.SUGAR && Objects.requireNonNull(current.getItemMeta()).getDisplayName().equals("§2§lSpeedRunner")){
                    if(main.getSpeedRunners().containsKey(player)){
                        player.sendMessage("§cTu es déjà SpeedRunner.");
                    } else {
                        if(main.getHunters().containsKey(player)){
                            main.getHunters().remove(player);
                            player.setPlayerListName(player.getName());
                            player.setDisplayName(player.getName());

                            player.sendMessage("§cTu as quitté l'Équipe des Chasseurs.");
                            player.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != player){
                                    pls.sendMessage("§b[SpeedRunner] §6" + player.getName() + "§c a quitté l'Équipe des Chasseurs.");
                                }
                            }
                        }

                        main.addSpeedRunner(player);
                        player.sendMessage("§aTu as rejoins l'Équipe des SpeedRunners !");
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage("§b[SpeedRunner] §6" + player.getName() + "§a a rejoint l'Équipe des SpeedRunners !");
                            }
                        }
                    }
                }

                if(current.getType() == Material.IRON_SWORD && Objects.requireNonNull(current.getItemMeta()).getDisplayName().equals("§c§lChasseur")){
                    if(main.getHunters().containsKey(player)){
                        player.sendMessage("§cTu es déjà Chasseur.");
                    } else {
                        if(main.getSpeedRunners().containsKey(player)){
                            main.removeSpeedRunner(player);
                            player.sendMessage("§cTu as quitté l'Équipe des SpeedRunners.");

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != player){
                                    pls.sendMessage("§b[SpeedRunner] §6" + player.getName() + "§c a quitté l'Équipe des SpeedRunners.");
                                }
                            }
                        }

                        main.getHunters().put(player, player);
                        player.setPlayerListName("§b[Chasseur] §r" + player.getName());
                        player.setDisplayName("§b[Chasseur] §r" + player.getName());
                        player.sendMessage("§aTu as rejoins l'Équipe des Chasseurs !");
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage("§b[SpeedRunner] §6" + player.getName() + "§a a rejoint l'Équipe des Chasseurs !");
                            }
                        }

                        ItemStack compass = new ItemStack(Material.COMPASS, 1);

                        player.getInventory().addItem(compass);
                        if(!main.getSpecialPlayerHunterTrack().containsKey(player)){
                            main.getSpecialPlayerHunterTrack().put(player, false);
                            player.sendMessage("§6Exécutes la commande §b/speedrunner cible §6pour choisir une Cible Précise, sinon ta boussole traquera le Joueur le plus proche §2§lQuand tu cliqueras avec §6!");
                        }
                    }
                }
            }
        }

        if(event.getView().getTitle().equals("§2§lSpeedRunners")){
            event.setCancelled(true);
            if(current.getType() == Material.PLAYER_HEAD && main.getHunters().containsKey(player)){
                if(Objects.requireNonNull(current.getItemMeta()).getDisplayName().equals("§b§lRandom")){
                    if(main.getSpecialPlayerHunterTrack().get(player)){
                        main.getSpecialPlayerHunterTrack().put(player, false);
                        player.sendMessage("§bMode de cible changé à §6§lAléatoire §b!");
                    } else {
                        player.sendMessage("§cMode de cible déjà en §6§lAléatoire §c!");
                    }
                    player.closeInventory();
                } else {
                    for(Map.Entry<Player, ItemStack> map : main.getSpedRunnerPlayerHeads().entrySet()){
                        if(current.isSimilar(map.getValue())){
                            Player tracked = map.getKey();
                            if(main.getSpecialPlayerHunterTrack().get(player) && main.getHunters().get(player) == tracked){
                                player.sendMessage("§cTa cible est déjà définie sur §b§l" + main.getHunters().get(player).getName() + " §c!");
                            } else {
                                main.getSpecialPlayerHunterTrack().put(player, true);
                                main.getHunters().put(player, tracked);
                                player.sendMessage("§aTu as défini §2§l" + tracked.getName() + "§a comme nouvelle cible SpeedRunner !");
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
                    Bukkit.getScheduler().runTaskLater(main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            if(!main.PlayerhasAdvancement(player, "end/kill_dragon")){
                                Bukkit.broadcastMessage("§cLe SpeedRunner " + player.getName() + " est maintenant hors course !");
                                main.SpeedRunnerHorsCourse(player);
                                if(!main.getSpeedRunners().containsValue(true)){
                                    Bukkit.broadcastMessage("§bLes Chasseurs ont Gagné !!");
                                    for(Player pls : Bukkit.getOnlinePlayers()){
                                        if(main.getSpeedRunners().containsKey(pls)){
                                            main.getSpeedRunners().put(pls, true);
                                            main.title.sendTitle(pls, "§c§lDÉFAITE", "§bLes Chasseurs ont Gagné...", 20);
                                        }

                                        if(main.getHunters().containsKey(pls)){
                                            main.title.sendTitle(pls, "§6§lVICTOIRE", "§bLes Chasseurs ont Gagné !!", 20);
                                        }
                                    }

                                    main.setGameStarted(false);
                                }
                            }
                        }
                    }, 600L);
                } else {
                    Bukkit.broadcastMessage("§cLe SpeedRunner " + player.getName() + " est maintenant hors course !");
                    main.SpeedRunnerHorsCourse(player);
                    if(!main.getSpeedRunners().containsValue(true)){
                        Bukkit.broadcastMessage("§bLes Chasseurs ont Gagné !!");
                        Player killer = null;
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(main.getSpeedRunners().containsKey(pls)){
                                main.getSpeedRunners().put(pls, true);
                                main.title.sendTitle(pls, "§c§lDÉFAITE", "§bLes Chasseurs ont Gagné...", 20);
                            }

                            if(main.getHunters().containsKey(pls)){
                                main.title.sendTitle(pls, "§6§lVICTOIRE", "§bLes Chasseurs ont Gagné !!", 20);
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
                            main.title.sendTitle(pls, "§6§lVICTOIRE", "§bLes SpeedRunners ont Gagné !!", 20);
                        }

                        if(main.getHunters().containsKey(pls)){
                            main.title.sendTitle(pls, "§c§lDÉFAITE", "§bLes SpeedRunners ont Gagné...", 20);
                        }
                    }
                    main.setGameStarted(false);
                }
            }
        }
    }
}
