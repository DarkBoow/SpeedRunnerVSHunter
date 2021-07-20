package fr.darkbow_.speedrunnervshunter;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandSpeedRunnerVSHunter implements CommandExecutor {
    private SpeedRunnerVSHunter main;
    
    public CommandSpeedRunnerVSHunter(SpeedRunnerVSHunter vaguesdemonstres){this.main = vaguesdemonstres;}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if(args.length == 0){
            boolean do_action = true;
            if(SpeedRunnerVSHunter.needpermission){
                if(!sender.hasPermission("speedrunnervshunter.player")){
                    do_action = false;
                    sender.sendMessage("§cTu n'as pas la permission de rejoindre une partie.");
                }
            }

            if(do_action){
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    player.openInventory(SpeedRunnerVSHunter.choixcamp);
                } else {
                    sender.sendMessage("§cSeuls les Joueurs peuvent exécuter cette commande !");
                }
            }
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("start")){
                if(sender.hasPermission("speedrunnervshunter.admin")){
                    if(main.isGameStarted()){
                        sender.sendMessage("§cLa chasse a déjà commencé !");
                    } else {
                        main.setGameStarted(true);
                        if(main.getConfigurationoptions().containsKey("WorldStartTime")){
                            if(Long.parseLong(main.getConfigurationoptions().get("WorldStartTime")) >= 0){
                                for(World world : Bukkit.getWorlds()){
                                    world.setTime(Long.parseLong(String.valueOf(Long.parseLong(main.getConfigurationoptions().get("WorldStartTime")))));
                                }
                            }
                        }

                        if(main.getConfigurationoptions().containsKey("Disable_DayLight_Cycle")){
                            if(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_DayLight_Cycle"))){
                                for(World world : Bukkit.getWorlds()){
                                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                                }
                            }
                        }

                        if(main.getConfig().getDouble("OffGameProtection.StartWorldBorder") >= 0.0){
                            for(World world : Bukkit.getWorlds()){
                                world.getWorldBorder().setSize(60000000);
                            }
                        }


                        Bukkit.broadcastMessage("§6§lLa chasse aux SpeedRunners Peut Maintenant §e§lCOMMENCER§6§l...");
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(main.getHunters().containsKey(pls)){
                                pls.sendMessage("§bVotre boussole de chasseur pointe initialement le SpeedRunner le plus proche de vous.\n§aExécutez la commande §2§l/speedrunner cible §apour modifier votre SpeedRunner cible.");
                            }
                            main.title.sendTitle(pls, "§b§lSpeedRunner", "§6La Chasse Peut §e§lCOMMENCER !!", 20);
                        }
                    }
                } else {
                    sender.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                }
            }

            if(args[0].equalsIgnoreCase("stop")){
                if(sender.hasPermission("speedrunnervshunter.admin")){
                    if(main.isGameStarted()){
                        main.setGameStarted(false);
                        if(main.getConfigurationoptions().containsKey("Disable_DayLight_Cycle")){
                            if(Boolean.parseBoolean(main.getConfigurationoptions().get("Disable_DayLight_Cycle"))){
                                for(World world : Bukkit.getWorlds()){
                                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                                }
                            }
                        }
                        Bukkit.broadcastMessage("§cLa chasse a été stoppée par un §4§lAdministrateur §c!");
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            main.title.sendTitle(pls, "§b§lSpeedRunner", "§cChasse Stoppée par un Admin !", 20);
                        }
                    } else {
                        sender.sendMessage("§cLa chasse n'est pas en cours !");
                    }
                } else {
                    sender.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                }
            }

            if(args[0].equalsIgnoreCase("cible")){
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    if(main.getHunters().containsKey(player)){
                        if(main.getSpeedRunners().containsValue(true)){
                            if(main.getHunters().containsKey(player)){
                                player.openInventory(SpeedRunnerVSHunter.speedrunnersinv);
                            }
                        } else {
                            player.sendMessage("§cAucun SpeedRunner n'est encore en course !");
                        }
                    } else {
                        player.sendMessage("§cTu n'es pas un Chasseur !");
                    }
                }
            }

            if(args[0].equalsIgnoreCase("leave")){
                if(sender instanceof Player){
                    Player player = (Player) sender;
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
                    } else if(main.getSpeedRunners().containsKey(player)){
                        main.removeSpeedRunner(player);
                        player.sendMessage("§cTu as quitté l'Équipe des SpeedRunners.");

                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage("§b[SpeedRunner] §6" + player.getName() + "§c a quitté l'Équipe des SpeedRunners.");
                            }
                        }
                    } else {
                        sender.sendMessage("§cTu n'es dans Aucun Camp.");
                    }
                } else {
                    sender.sendMessage("§cUsage Console : §6/speedrunner leave <player>");
                }
            }
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("leave") && sender.hasPermission("speedrunnervshunter.admin")){
                Player cible = Bukkit.getPlayer(args[1]);
                if(cible == null){
                    sender.sendMessage("§cLe Joueur Spécifié n'Existe Pas !");
                } else {
                    if(Bukkit.getOnlinePlayers().contains(cible)){
                        if(main.getHunters().containsKey(cible)){
                            main.getHunters().remove(cible);
                            cible.setPlayerListName(cible.getName());
                            cible.setDisplayName(cible.getName());

                            cible.sendMessage(sender.getName() + " §ct'a forcé à quitter ton Équipe.");
                            cible.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage("§b[SpeedRunner] §a" + sender.getName() + " §ca expulsé §6" + cible.getName() + "§c de l'Équipe des Chasseurs.");
                                }
                            }
                        } else if(main.getSpeedRunners().containsKey(cible)){
                            main.removeSpeedRunner(cible);
                            cible.sendMessage(sender.getName() + " §ct'a forcé à quitter ton Équipe.");

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage("§b[SpeedRunner] §a" + sender.getName() + " §ca expulsé §6" + cible.getName() + "§c de l'Équipe des SpeedRunners.");
                                }
                            }
                        } else {
                            sender.sendMessage("§cLe Joueur Spécifié n'est dans Aucun Camp.");
                        }
                    } else {
                        sender.sendMessage("§cLe Joueur Spécifié N'Est Pas Connecté !");
                    }
                }
            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("join") && sender.hasPermission("speedrunnervshunter.admin")){
                Player cible = Bukkit.getPlayer(args[2]);
                if(cible == null){
                    sender.sendMessage("§cLe Joueur Spécifié n'Existe Pas !");
                } else {
                    if(Bukkit.getOnlinePlayers().contains(cible)){
                        if(args[1].equalsIgnoreCase("speedrunners")){
                            if(main.getHunters().containsKey(cible)){
                                main.getHunters().remove(cible);
                                cible.setPlayerListName(cible.getName());
                                cible.setDisplayName(cible.getName());

                                cible.sendMessage(sender.getName() + " §ct'a forcé à quitter ton Équipe.");
                                cible.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                                for(Player pls : Bukkit.getOnlinePlayers()){
                                    if(pls != cible){
                                        pls.sendMessage("§b[SpeedRunner] §a" + sender.getName() + " §ca expulsé §6" + cible.getName() + "§c de l'Équipe des Chasseurs.");
                                    }
                                }
                            }

                            main.addSpeedRunner(cible);
                            cible.sendMessage("§aTu as rejoins l'Équipe des SpeedRunners !");
                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage("§b[SpeedRunner] §6" + cible.getName() + "§a a rejoint l'Équipe des SpeedRunners !");
                                }
                            }
                        } else if(args[1].equalsIgnoreCase("chasseurs")){
                            if(main.getSpeedRunners().containsKey(cible)){
                                main.removeSpeedRunner(cible);
                                cible.sendMessage(sender.getName() + " §ct'a forcé à quitter ton Équipe.");

                                for(Player pls : Bukkit.getOnlinePlayers()){
                                    if(pls != cible){
                                        pls.sendMessage("§b[SpeedRunner] §a" + sender.getName() + " §ca expulsé §6" + cible.getName() + "§c de l'Équipe des SpeedRunners.");
                                    }
                                }
                            }

                            main.getHunters().put(cible, cible);
                            cible.setPlayerListName("§b[Chasseur] §r" + cible.getName());
                            cible.setDisplayName("§b[Chasseur] §r" + cible.getName());
                            cible.sendMessage("§aTu as rejoins l'Équipe des Chasseurs !");
                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage("§b[SpeedRunner] §6" + cible.getName() + "§a a rejoint l'Équipe des Chasseurs !");
                                }
                            }

                            ItemStack compass = new ItemStack(Material.COMPASS, 1);

                            cible.getInventory().addItem(compass);
                            if(!main.getSpecialPlayerHunterTrack().containsKey(cible)){
                                main.getSpecialPlayerHunterTrack().put(cible, false);
                                cible.sendMessage("§6Exécutes la commande §b/speedrunner cible §6pour choisir une Cible Précise, sinon ta boussole traquera le Joueur le plus proche §2§lQuand tu cliqueras avec §6!");
                            }
                        } else {
                            sender.sendMessage("§cLe camp spécifié n'existe Pas !");
                        }
                    } else {
                        sender.sendMessage("§cLe Joueur Spécifié N'Est Pas Connecté !");
                    }
                }
            }
        }

        return false;
    }
}