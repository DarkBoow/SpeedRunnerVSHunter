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
                        for(World world : Bukkit.getWorlds()){
                            if(world.getGameRuleValue(GameRule.KEEP_INVENTORY) == null || !world.getGameRuleValue(GameRule.KEEP_INVENTORY)){
                                main.getWorldskeepInventoryGamerule().add(world);
                            }

                            world.setGameRuleValue("keepInventory", "true");
                        }

                        main.setGameStarted(true);
                        Bukkit.broadcastMessage("§6§lLa chasse aux SpeedRunners peut maintenant commencer...");
                        for(Player hunter : main.getHunters().keySet()){
                            hunter.sendMessage("§bVotre boussole de chasseur pointe basiquement le SpeedRunner le plus proche de vous.\n§bCliquez §b§lGauche §bavec votre boussole si vous voulez pointer vers un SpeedRunner en particulier !");
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
                        for(World world : main.getWorldskeepInventoryGamerule()){
                            world.setGameRuleValue("keepInventory", "false");
                        }
                        main.getWorldskeepInventoryGamerule().clear();

                        Bukkit.broadcastMessage("§c§lLa chasse a été stoppée par un administrateur !");
                    } else {
                        sender.sendMessage("§cLa chasse n'est pas en cours !");
                    }
                } else {
                    sender.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                }
            }
        }
        return false;
    }
}