package fr.darkbow_.speedrunnervshunter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Task extends BukkitRunnable {
    public static boolean isRunning = false;

    private final SpeedRunnerVSHunter main;

    public Task(SpeedRunnerVSHunter speedrunnervshunter){this.main = speedrunnervshunter;}

    @Override
    public void run() {
        if(main.getSpeedRunners().size() > 0){
            Map<UUID, Boolean> frozenhunters = new HashMap<>();
            for(Player hunter : main.getFrozenHunters()){
                frozenhunters.put(hunter.getUniqueId(), false);
            }

            for(Player pls : main.getSpeedRunners().keySet()){
                Location eye = pls.getEyeLocation();
                for(Entity e : pls.getNearbyEntities(80, 80, 80)){
                    if(e instanceof Player){
                        Player player = (Player) e;
                        if(main.getHunters().containsKey(player)){
                            Vector toEntity = player.getEyeLocation().toVector().subtract(eye.toVector());
                            double dot = toEntity.normalize().dot(eye.getDirection());
                            List<Block> blocks = main.getLineOfSight(main.getHashSet(), 80, 1, pls);
                            Block targetblock = blocks.get(0);

                            if(dot > 0.99 && pls.getEyeLocation().distance(player.getLocation()) <= pls.getEyeLocation().distance(targetblock.getLocation())){
                                main.getFrozenHunters().add(player);
                                main.spawnParticles(main.getHashSet(), (new Double(pls.getEyeLocation().distance(player.getLocation()))).intValue(), (new Double(pls.getEyeLocation().distance(player.getLocation()))).intValue(), pls);
                                frozenhunters.put(player.getUniqueId(), true);
                            } else {
                                main.getFrozenHunters().remove(player);
                            }
                        }
                    }
                }
            }

            for(Map.Entry<UUID, Boolean> frozenhuntersmap : frozenhunters.entrySet()){
                if(!frozenhuntersmap.getValue()){
                    main.getFrozenHunters().remove(Bukkit.getPlayer(frozenhuntersmap.getKey()));
                }
            }
        }

        if(!main.isGameStarted()){
            isRunning = false;
            cancel();
        }
    }
}