package fr.darkbow_.speedrunnervshunter;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.tozymc.spigot.api.title.TitleApi;

import java.util.Objects;

public class CommandSpeedRunnerVSHunter implements CommandExecutor {
    private final SpeedRunnerVSHunter main;
    
    public CommandSpeedRunnerVSHunter(SpeedRunnerVSHunter vaguesdemonstres){this.main = vaguesdemonstres;}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if(args.length == 0){
            boolean do_action = true;
            if(SpeedRunnerVSHunter.needpermission){
                if(!sender.hasPermission("speedrunnervshunter.player")){
                    do_action = false;
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("DontHavePermissionToPlay")).replace("&", "§"));
                }
            }

            if(do_action){
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    player.openInventory(SpeedRunnerVSHunter.choixcamp);
                } else {
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("CommandRequiresToBeAPlayer")).replace("&", "§"));
                }
            }
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("start")){
                if(sender.hasPermission("speedrunnervshunter.admin")){
                    if(main.isGameStarted()){
                        sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheGameHasAlreadyBegun")).replace("&", "§"));
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

                        for(World world : Bukkit.getWorlds()){
                            world.getWorldBorder().setSize(60000000);
                        }

                        Bukkit.broadcastMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheGameHasStarted")).replace("&", "§"));
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(main.getHunters().containsKey(pls)){
                                pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("HuntersCompassInfo")).replace("&", "§"));
                            }

                            TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("StartingGameTitle.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("StartingGameTitle.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("StartingGameTitle.FadeIn"), main.getLanguageConfig().getInt("StartingGameTitle.Stay"), main.getLanguageConfig().getInt("StartingGameTitle.FadeOut"));
                        }

                        if(main.getConfig().getBoolean("GameOptions.AssassinsMode")){
                            SpeedRunnerVSHunter.task = new Task(main).runTaskTimer(main, 1L, 1L);
                            Task.isRunning = true;
                        }
                    }
                } else {
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouCantExecuteThatCommand")).replace("&", "§"));
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
                        Bukkit.broadcastMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AdminStoppedGame")).replace("&", "§"));
                        for(Player pls : Bukkit.getOnlinePlayers()){
                            TitleApi.sendTitle(pls, Objects.requireNonNull(main.getLanguageConfig().getString("ForceStopGameTitle.Title")).replace("&", "§"), Objects.requireNonNull(main.getLanguageConfig().getString("ForceStopGameTitle.SubTitle")).replace("&", "§"), main.getLanguageConfig().getInt("ForceStopGameTitle.FadeIn"), main.getLanguageConfig().getInt("ForceStopGameTitle.Stay"), main.getLanguageConfig().getInt("ForceStopGameTitle.FadeOut"));
                        }

                        if(main.getConfig().getBoolean("GameOptions.AssassinsMode")){
                            Task.isRunning = false;
                        }
                    } else {
                        sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheGameHasntStartedYet")).replace("&", "§"));
                    }
                } else {
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouCantExecuteThatCommand")).replace("&", "§"));
                }
            }

            if(args[0].equalsIgnoreCase("cible") || args[0].equalsIgnoreCase("target")){
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    if(main.getHunters().containsKey(player)){
                        if(main.getSpeedRunners().containsValue(true)){
                            if(main.getHunters().containsKey(player)){
                                player.openInventory(SpeedRunnerVSHunter.speedrunnersinv);
                            }
                        } else {
                            if(!Objects.requireNonNull(main.getLanguageConfig().getString("NoSpeedRunnerRemains")).isEmpty()){
                                player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("NoSpeedRunnerRemains")).replace("&", "§"));
                            }
                        }
                    } else {
                        if(!Objects.requireNonNull(main.getLanguageConfig().getString("YouAreNotAHunter")).isEmpty()){
                            player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouAreNotAHunter")).replace("&", "§"));
                        }
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

                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouLeftTheHuntersTeam")).replace("&", "§"));
                        player.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("ASpeedRunnerLeftHisTeam")).replace("%player%", player.getName()).replace("&", "§"));
                            }
                        }
                    } else if(main.getSpeedRunners().containsKey(player)){
                        main.removeSpeedRunner(player);
                        player.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouLeftTheSpeedRunnersTeam")).replace("&", "§"));

                        for(Player pls : Bukkit.getOnlinePlayers()){
                            if(pls != player){
                                pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("ASpeedRunnerLeftHisTeam")).replace("%player%", player.getName()).replace("&", "§"));
                            }
                        }
                    } else {
                        sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouCantLeaveYourTeamBecauseYouAreNotInTheGame")).replace("&", "§"));
                    }
                } else {
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SpeedRunnerLeavePlayer_ConsoleUsage")).replace("&", "§"));
                }
            }
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("leave") && sender.hasPermission("speedrunnervshunter.admin")){
                Player cible = Bukkit.getPlayer(args[1]);
                if(cible == null){
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SpecifiedPlayerDoesNotExist")).replace("&", "§"));
                } else {
                    if(Bukkit.getOnlinePlayers().contains(cible)){
                        if(main.getHunters().containsKey(cible)){
                            main.getHunters().remove(cible);
                            cible.setPlayerListName(cible.getName());
                            cible.setDisplayName(cible.getName());

                            cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorForcedYouToLeaveYourTeam")).replace("%sender%", sender.getName()).replace("&", "§"));
                            cible.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorKickedAHunter")).replace("%sender%", sender.getName()).replace("%player%", cible.getName()).replace("&", "§"));
                                }
                            }
                        } else if(main.getSpeedRunners().containsKey(cible)){
                            main.removeSpeedRunner(cible);
                            cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorForcedYouToLeaveYourTeam")).replace("%sender%", sender.getName()).replace("&", "§"));

                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorKickedASpeedRunner")).replace("%sender%", sender.getName()).replace("%player%", cible.getName()).replace("&", "§"));
                                }
                            }
                        } else {
                            sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheSpecifiedPlayerCantLeaveHisTeamBecauseHeIsNotInTheGame")).replace("&", "§"));
                        }
                    } else {
                        sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheSpecifiedPlayerIsNotOnline")).replace("&", "§"));
                    }
                }
            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("join") && sender.hasPermission("speedrunnervshunter.admin")){
                Player cible = Bukkit.getPlayer(args[2]);
                if(cible == null){
                    sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SpecifiedPlayerDoesNotExist")).replace("%player%", args[2]).replace("&", "§"));
                } else {
                    if(Bukkit.getOnlinePlayers().contains(cible)){
                        if(args[1].equalsIgnoreCase("speedrunners")){
                            if(main.getHunters().containsKey(cible)){
                                main.getHunters().remove(cible);
                                cible.setPlayerListName(cible.getName());
                                cible.setDisplayName(cible.getName());

                                cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorForcedYouToLeaveYourTeam")).replace("%sender%",sender.getName()).replace("&", "§"));
                                cible.getInventory().remove(new ItemStack(Material.COMPASS, 1));

                                for(Player pls : Bukkit.getOnlinePlayers()){
                                    if(pls != cible){
                                        pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorKickedAHunter")).replace("%sender%", sender.getName()).replace("%player%", cible.getName()).replace("&", "§"));
                                    }
                                }
                            }

                            main.addSpeedRunner(cible);
                            cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouJoinedTheSpeedRunnersTeam")).replace("&", "§"));
                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SomeoneJoinedTheSpeedRunnerTeam")).replace("%player%", cible.getName()).replace("&", "§"));
                                }
                            }
                        } else if(args[1].equalsIgnoreCase("chasseurs") || args[1].equalsIgnoreCase("hunters")){
                            if(main.getSpeedRunners().containsKey(cible)){
                                main.removeSpeedRunner(cible);
                                cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorForcedYouToLeaveYourTeam")).replace("%sender%", sender.getName()).replace("&", "§"));

                                for(Player pls : Bukkit.getOnlinePlayers()){
                                    if(pls != cible){
                                        pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("AnOperatorKickedASpeedRunner")).replace("%sender%", sender.getName()).replace("%player%", cible.getName()).replace("&", "§"));
                                    }
                                }
                            }

                            main.getHunters().put(cible, cible);
                            cible.setPlayerListName(Objects.requireNonNull(main.getLanguageConfig().getString("Hunters_PlayerListName")).replace("%player%", cible.getName()).replace("&", "§"));
                            cible.setDisplayName(Objects.requireNonNull(main.getLanguageConfig().getString("Hunters_PlayerDisplayName")).replace("%player%", cible.getName()).replace("&", "§"));
                            cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("YouJoinedTheHuntersTeam")).replace("&", "§"));
                            for(Player pls : Bukkit.getOnlinePlayers()){
                                if(pls != cible){
                                    pls.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("SomeoneJoinedTheHuntersTeam")).replace("%player%", cible.getName()).replace("&", "§"));
                                }
                            }

                            ItemStack compass = new ItemStack(Material.COMPASS, 1);

                            cible.getInventory().addItem(compass);
                            if(!main.getSpecialPlayerHunterTrack().containsKey(cible)){
                                main.getSpecialPlayerHunterTrack().put(cible, false);
                                cible.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("HunterTargetCommandInfo")).replace("&", "§"));
                            }
                        } else {
                            sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("ThisTeamDoesNotExist")).replace("&", "§"));
                        }
                    } else {
                        sender.sendMessage(Objects.requireNonNull(main.getLanguageConfig().getString("TheSpecifiedPlayerIsNotOnline")).replace("%player%", cible.getName()).replace("&", "§"));
                    }
                }
            }
        }

        return false;
    }
}