package fr.darkbow_.speedrunnervshunter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.Map;

public class SpeedRunnerVSHunterEvenement implements Listener {
    private SpeedRunnerVSHunter main;

    public SpeedRunnerVSHunterEvenement(SpeedRunnerVSHunter vaguesdemonstres){this.main = vaguesdemonstres;}

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getItem() == null){ return; }

        if(event.getItem().getType() == Material.COMPASS){
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
                                player.setCompassTarget(tracked.getLocation());
                            }

                            if(tracked.getWorld().getEnvironment() == World.Environment.NETHER){
                                CompassMeta compass = (CompassMeta)event.getItem();
                                compass.setLodestone(tracked.getLocation());
                                compass.setLodestoneTracked(false);

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
                if(current.getType() == Material.SUGAR && current.getItemMeta().getDisplayName().equals("§2§lSpeedRunner")){
                    if(main.getSpeedRunners().containsKey(player)){
                        player.sendMessage("§cTu es déjà SpeedRunner.");
                    } else {
                        if(main.getHunters().containsKey(player)){
                            main.getHunters().remove(player);
                            player.sendMessage("§cTu as quitté l'Équipe des Chasseurs.");
                            player.getInventory().remove(new ItemStack(Material.COMPASS, 1));
                        }

                        main.addSpeedRunner(player);
                        player.sendMessage("§aTu as rejoins l'Équipe des SpeedRunners !");
                    }
                }

                if(current.getType() == Material.IRON_SWORD && current.getItemMeta().getDisplayName().equals("§c§lChasseur")){
                    if(main.getHunters().containsKey(player)){
                        player.sendMessage("§cTu es déjà Chasseur.");
                    } else {
                        if(main.getSpeedRunners().containsKey(player)){
                            main.removeSpeedRunner(player);
                            player.sendMessage("§cTu as quitté l'Équipe des SpeedRunners.");
                        }

                        main.getHunters().put(player, player);
                        player.sendMessage("§aTu as rejoins l'Équipe des Chasseurs !");

                        player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
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
                if(current.getItemMeta().getDisplayName().equals("§b§lRandom")){
                    if(main.getSpecialPlayerHunterTrack().get(player)){
                        main.getSpecialPlayerHunterTrack().put(player, false);
                        player.sendMessage("§bMode de cible changé à §6§lAléatoire §b!");
                        player.closeInventory();
                    }
                } else {
                    for(Map.Entry<Player, ItemStack> map : main.getSpedRunnerPlayerHeads().entrySet()){
                        if(current.getItemMeta().getDisplayName().equals(map.getValue().getItemMeta().getDisplayName()) && main.getHunters().get(player) != map.getKey()){
                            Player tracked = map.getKey();

                            main.getSpecialPlayerHunterTrack().put(player, true);
                            main.getHunters().put(player, tracked);
                            player.sendMessage("§aTu as défini §2§l" + tracked.getName() + "§a comme nouvelle cible SpeedRunner !");
                            player.closeInventory();
                        }
                    }
                }
            }

            if(current.getType() == Material.STONE){ //Random (choisir l'item ensuite)
                if(main.getHunters().containsKey(player)){
                    main.getSpecialPlayerHunterTrack().put(player, false);
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
