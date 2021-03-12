package fr.darkbow_.speedrunnervshunter;

import fr.darkbow_.speedrunnervshunter.scoreboard.ScoreboardSign;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

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
        }
        return false;
    }
}