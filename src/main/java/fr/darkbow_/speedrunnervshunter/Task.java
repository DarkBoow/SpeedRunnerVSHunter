package fr.darkbow_.speedrunnervshunter;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Task extends BukkitRunnable {
    public static boolean isRunning = false;

    private final SpeedRunnerVSHunter main;

    public Task(SpeedRunnerVSHunter speedrunnervshunter){this.main = speedrunnervshunter;}

    @Override
    public void run() {
        if(main.getSpeedRunners().size() > 0){
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
                            } else {
                                main.getFrozenHunters().remove(player);
                            }
                        }
                    }
                }
            }
        }

        if(!main.isGameStarted()){
            isRunning = false;
            cancel();
        }
    }
}