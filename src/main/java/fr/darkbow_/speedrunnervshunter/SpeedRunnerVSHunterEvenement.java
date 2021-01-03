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
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;

public class SpeedRunnerVSHunterEvenement implements Listener {
    private SpeedRunnerVSHunter main;

    public SpeedRunnerVSHunterEvenement(SpeedRunnerVSHunter vaguesdemonstres){this.main = vaguesdemonstres;}

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getItem() == null){ return; }

        if(event.getItem().getType() == Material.COMPASS){
            if(event.getAction().toString().contains("LEFT")){
                if(main.getSpeedRunners().containsValue(true)){
                    if(main.getHunters().containsKey(player)){
                        player.openInventory(SpeedRunnerVSHunter.speedrunnersinv);
                    }
                } else {
                    player.sendMessage("§cAucun SpeedRunner n'est encore en course !");
                }
            }

            if(event.getAction().toString().contains("RIGHT")){
                if(main.getHunters().containsKey(player)){
                    Player tracked = null;
                    if(main.getSpecialPlayerHunterTrack().get(player)){
                        if(main.getHunters().get(player) == player){
                            player.sendMessage("§cTu dois d'abord cliquer gauche avec la boussole pour choisir ta cible, en raison de ton mode de traquage.");
                        } else {
                            if(main.getSpeedRunners().containsKey(main.getHunters().get(player))){
                                tracked = main.getHunters().get(player);
                                player.sendMessage("§aTa nouvelle cible sera maintenant " + tracked.getName() + " !\n§bClique droit avec ta Boussole quand tu souhaiteras actualiser sa direction vers ta nouvelle cible.");
                            } else {
                                player.sendMessage("§cLe joueur " + main.getHunters().get(player).getName() + " que tu avais ciblé n'est plus connecté ou n'est plus SpeedRunner !\n§c§lVeille à choisir le mode de ciblage de Proximité ou Pointer un autre SpeedRunner si ton précédent choix ne redevient pas SpeedRunner ! (en faisant un Clic Gauche avec ta Boussole en main)");
                            }
                        }
                    } else {
                        double lastDistance = Double.MAX_VALUE;
                        for(Player speedrunner : main.getSpeedRunners().keySet()){
                            if(main.getSpeedRunners().get(speedrunner)){
                                double distance = player.getLocation().distance(speedrunner.getLocation());
                                if(distance < lastDistance){
                                    lastDistance = distance;
                                    tracked = speedrunner;
                                }
                            }
                        }
                    }

                    if(tracked != null){
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

                        player.sendMessage("§aTa boussole pointe vers " + tracked.getName() + ".");
                    }
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

        if(event.getView().getTitle().equals("§b§lChoisis ton Camp")){
            event.setCancelled(true);
            player.closeInventory();
            if(main.isGameStarted()){
                player.sendMessage("§cLa chasse a déjà commencé, donc tu ne peux plus choisir ton camp.\n§cLa seule manière de choisir ton camp est de stopper la chasse.");
            } else {
                if(current.getType() == Material.SUGAR && current.getItemMeta().getDisplayName().equals("§b§lSpeedRunner")){
                    if(main.getSpeedRunners().containsKey(player)){
                        player.sendMessage("§cTu es déjà SpeedRunner.");
                    } else {
                        if(main.getHunters().containsKey(player)){
                            main.getHunters().remove(player);
                            player.sendMessage("§cTu as quitté l'Équipe des Chasseurs.");
                        }

                        main.addSpeedRunner(player);

                        player.sendMessage("§aTu as rejoins l'Équipe des SpeedRunners !");
                        player.getInventory().remove(new ItemStack(Material.COMPASS, 1));
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
                            player.sendMessage("§6Clique Gauche avec ta Boussole pour choisir une Cible précise, sinon ta boussole traquera le Joueur le plus proche quand tu feras un clic droit avec !");
                        }
                    }
                }
            }
        }

        if(event.getView().getTitle().equals("§b§lSpeedRunners")){
            event.setCancelled(true);
            if(current.getType() == Material.PLAYER_HEAD){
                for(ItemStack speedrunners : SpeedRunnerVSHunter.speedrunnersinv.getContents()){
                    String trackedname = speedrunners.getItemMeta().getDisplayName();
                    trackedname.replaceFirst("§a", "");
                    Player tracked = Bukkit.getPlayer(trackedname);
                    if(tracked == null || !tracked.isOnline()){
                        return;
                    }

                    if(main.getSpeedRunners().containsKey(tracked)){
                        main.getSpecialPlayerHunterTrack().put(player, true);
                        main.getHunters().put(player, tracked);
                        player.sendMessage("§aTu as défini " + trackedname + " comme nouvelle cible SpeedRunner !");
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
        if(main.getSpeedRunners().containsKey(player)){
            if(player.getWorld().getEnvironment() == World.Environment.THE_END){
                Bukkit.getScheduler().runTaskLater(main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if(!main.PlayerhasAdvancement(player, "minecraft:end/dragon_breath")){
                            Bukkit.broadcastMessage("§cLe SpeedRunner " + player.getName() + " est maintenant hors course !");
                            main.getSpeedRunners().put(player, false);
                            if(!main.getSpeedRunners().containsValue(true)){
                                Bukkit.broadcastMessage("§bLes Chasseurs ont Gagné !!");
                                for(Map.Entry<Player, Boolean> map : main.getSpeedRunners().entrySet()){
                                    map.setValue(true);
                                }
                                main.setGameStarted(false);
                            }
                        }
                    }
                }, 600L);
            }
        }
    }

    @EventHandler
    public void onGotAchievement(PlayerAdvancementDoneEvent event){
        Player player = event.getPlayer();
        if(main.getSpeedRunners().containsKey(player) && main.getSpeedRunners().get(player)){
            if(main.PlayerhasAdvancement(player, "minecraft:end/dragon_breath")){
                Bukkit.broadcastMessage("§6Les SpeedRunners ont GAGNÉ la CHASSE !");
                main.title.sendTitle(player, "§6§lBravo " + player.getName() + " !!", "§bTu as fait gagner les SpeedRunners !", 20);
                for(Map.Entry<Player, Boolean> map : main.getSpeedRunners().entrySet()){
                    map.setValue(true);
                    if(map.getKey() != player){
                        main.title.sendTitle(map.getKey(), "§b§bGG, Tu as Gagné !", "§bLes SpeedRunners ont gagné !", 20);
                    }
                }
                main.setGameStarted(false);
            }
        }
    }
}
