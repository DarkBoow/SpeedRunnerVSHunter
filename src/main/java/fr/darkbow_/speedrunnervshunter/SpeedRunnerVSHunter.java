package fr.darkbow_.speedrunnervshunter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public class SpeedRunnerVSHunter extends JavaPlugin {
    private SpeedRunnerVSHunter instance;
    public Titles title = new Titles();
    private HashMap<Player, Boolean> speedrunners;
    private HashMap<Player, Player> hunters;
    private HashMap<Player, Boolean> specialplayertrack;
    private HashMap<Player, ItemStack> speedrunnersplayerheads;
    private Map<String, ItemStack> itemsByName;
    private Map<Player, ItemStack> hunterscompass;
    private boolean gameStarted = false;
    public static Inventory speedrunnersinv = Bukkit.createInventory(null, 54, "§2§lSpeedRunners");
    public static Inventory choixcamp = Bukkit.createInventory(null, 9, "§9§lChoisis ton Camp");

    public SpeedRunnerVSHunter getInstance() {
        return this.instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.speedrunners = new HashMap<>();
        this.hunters = new HashMap<>();
        this.speedrunnersplayerheads = new HashMap<>();
        this.itemsByName = new HashMap<>();
        this.specialplayertrack = new HashMap<>();
        this.hunterscompass = new HashMap<>();

        createInventory();

        getCommand("speedrunnervshunter").setExecutor(new CommandSpeedRunnerVSHunter(this));
        getServer().getPluginManager().registerEvents(new SpeedRunnerVSHunterEvenement(this), this);

        System.out.println("[SpeedRunnerVSHunter] Plugin Activé !!");
    }

    @Override
    public void onDisable() {
        System.out.println("[SpeedRunnerVSHunter] Plugin Désactivé !");
    }

    public HashMap<Player, Boolean> getSpeedRunners() {
        return this.speedrunners;
    }

    public HashMap<Player, Player> getHunters() {
        return this.hunters;
    }

    public void createInventory(){
        choixcamp.setItem(3, getItem(Material.SUGAR, 1, (byte)0, "§2§lSpeedRunner", Arrays.asList("", "§7Rejoindre le camp des SpeedRunners"), null, 0, false, null, null));
        choixcamp.setItem(5, getItem(Material.IRON_SWORD, 1, (byte)0, "§c§lChasseur", Arrays.asList("", "§7Rejoindre le camp des Chasseurs"), null, 0, false, null, null));


        ItemStack randomhead = getItem(Material.PLAYER_HEAD, 1, (byte)0, "§6§lCible la Plus Proche", Arrays.asList("", "§7Votre boussole de Chasseur pointera vers","§7le SpeedRunner le Plus Proche de Vous"), null, 0, false, null, null);

        SkullMeta playerheadmeta = (SkullMeta) randomhead.getItemMeta();
        playerheadmeta.setOwner("Hynity");
        playerheadmeta.setDisplayName("§b§lRandom");

        /*GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNlYjcxM2NkOWNmOTE5MjY0YjYzMWU3MGY1MjhiZDIwYzQzZTc5MjQxNjk1ZDZiZmM5Y2ZjN2RjZDYzZCJ9fX0=").getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try{
            profileField = playerheadmeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(playerheadmeta, profile);
        }catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1){
            e1.printStackTrace();
        }*/
        randomhead.setItemMeta(playerheadmeta);

        speedrunnersinv.setItem(speedrunnersinv.getSize()-1, randomhead);
    }

    public void addSpeedRunner(Player player){
        ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD, 1);

        SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
        playerheadmeta.setOwner(player.getName());
        playerheadmeta.setDisplayName(player.getName());
        playerheadmeta.setDisplayName("§a" + player.getName());
        playerhead.setItemMeta(playerheadmeta);
        this.speedrunners.put(player, true);
        this.speedrunnersplayerheads.put(player, playerhead);
        speedrunnersinv.addItem(playerhead);
    }

    public void removeSpeedRunner(Player player){
        speedrunnersinv.remove(this.speedrunnersplayerheads.get(player));
        List<ItemStack> items = new ArrayList<>();
        boolean space = false;
        for(int i=0; i<speedrunnersinv.getSize(); i++){
            if(space){
                if(speedrunnersinv.getItem(i) != null){
                    items.add(speedrunnersinv.getItem(i));
                    speedrunnersinv.clear(i);
                }
            }

            if(!space && speedrunnersinv.getItem(i) == null){
                space = true;
            }
        }

        if(!items.isEmpty()){
            for(ItemStack it : items){
                speedrunnersinv.addItem(it);
            }

            items.clear();
        }
        this.speedrunnersplayerheads.remove(player);
        this.speedrunners.remove(player);
    }

    public void SpeedRunnerHorsCourse(Player player){
        this.speedrunners.put(player, false);
        speedrunnersinv.remove(this.speedrunnersplayerheads.get(player));
        List<ItemStack> items = new ArrayList<>();
        boolean space = false;
        for(int i=0; i<speedrunnersinv.getSize(); i++){
            if(space){
                if(speedrunnersinv.getItem(i) != null){
                    items.add(speedrunnersinv.getItem(i));
                    speedrunnersinv.clear(i);
                }
            }

            if(!space && speedrunnersinv.getItem(i) == null){
                space = true;
            }
        }

        if(!items.isEmpty()){
            for(ItemStack it : items){
                speedrunnersinv.addItem(it);
            }

            items.clear();
        }

        boolean arespeedrunnersalleliminated = true;
        if(this.speedrunners.containsValue(true)){
            arespeedrunnersalleliminated = false;
        } else {
            for(Map.Entry<Player, Boolean> map : this.speedrunners.entrySet()){
                map.setValue(true);
            }

            setGameStarted(false);
            for(Player pls : this.speedrunners.keySet()){
                if(pls.isOnline()){
                    pls.sendMessage("§cTu as perdu la chasse " + player.getName() + ".");
                }
            }

            for(Player pls : this.hunters.keySet()){
                if(pls.isOnline()){
                    pls.sendMessage("§6Tu as gagné la chasse, Bravo " + player.getName() + " !");
                }
            }

            Bukkit.broadcastMessage("§6§lLa chasse a été gagnée par les Chasseurs !");
            Bukkit.broadcastMessage("§c§lLes SpeedRunners ont Perdu !");
        }
    }

    public Map<String, ItemStack> getItems() {
        return this.itemsByName;
    }

    public ItemStack getItem(final Material material, final int number, final byte data, final String displayName, final List<String> lore, final Enchantment enchname, final int enchpower, final boolean enchdisplaying, final ItemFlag hideEnchants, final ItemFlag itemFlag) {
        ItemStack it = null;
        if (material != null) {
            it = new ItemStack(material, number, (short)data);
        }
        final ItemMeta itM = it.getItemMeta();

        if (displayName != null) {
            itM.setDisplayName(displayName);
        }
        if (lore != null) {
            itM.setLore((List)lore);
        }
        if (enchname != null) {
            itM.addEnchant(enchname, enchpower, enchdisplaying);
        }
        if (hideEnchants != null) {
            itM.addItemFlags(new ItemFlag[] { hideEnchants });
        }
        if (itemFlag != null) {
            itM.addItemFlags(new ItemFlag[] { itemFlag });
        }
        it.setItemMeta(itM);
        this.getItems().put(displayName, it);
        return it;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public HashMap<Player, Boolean> getSpecialPlayerHunterTrack() {
        return specialplayertrack;
    }

    public HashMap<Player, ItemStack> getSpedRunnerPlayerHeads() {
        return speedrunnersplayerheads;
    }

    public Map<Player, ItemStack> getHunterscompass() {
        return hunterscompass;
    }

    public boolean PlayerhasAdvancement(Player player, String achname){
        Advancement ach = null;
        for (Iterator<Advancement> iter = Bukkit.getServer().advancementIterator(); iter.hasNext(); ) {
            Advancement adv = iter.next();
            if (adv.getKey().getKey().equalsIgnoreCase(achname)){
                ach = adv;
                break;
            }
        }
        AdvancementProgress prog = player.getAdvancementProgress(ach);
        if (prog.isDone()){
            return true;
        }
        return false;
    }
}