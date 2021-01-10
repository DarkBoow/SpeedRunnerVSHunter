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
            if(sender instanceof Player){
                Player player = (Player) sender;
                player.openInventory(SpeedRunnerVSHunter.choixcamp);
            } else {
                sender.sendMessage("§cSeuls les Joueurs peuvent exécuter cette commande !");
            }
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("start")){
                if(sender.hasPermission("speedrunnervshunter.admin")){
                    if(main.isGameStarted()){
                        sender.sendMessage("§cLa chasse a déjà commencé !");
                    } else {
                        main.setGameStarted(true);
                        Bukkit.broadcastMessage("§6§lLa chasse aux SpeedRunners peut maintenant commencer...");
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(main.getHunters().containsKey(pls)){
                                pls.sendMessage("§bVotre boussole de chasseur pointe initialement le SpeedRunner le plus proche de vous.\n§aExécutez la commande §2§l/speedrunner start §apour modifier votre SpeedRunner cible.");
                            }
                            main.title.sendTitle(pls, "§b§lSpeedRunner", "§6La Chasse Peut Commencer !!", 20);
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
                        Bukkit.broadcastMessage("§c§lLa chasse a été stoppée par un administrateur !");
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