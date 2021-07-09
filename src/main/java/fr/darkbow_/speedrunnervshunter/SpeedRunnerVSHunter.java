package fr.darkbow_.speedrunnervshunter;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private Map<String, String> configurationoptions;
    private boolean gameStarted = false;
    public static Inventory speedrunnersinv = Bukkit.createInventory(null, 54, "§2§lSpeedRunners");
    public static Inventory choixcamp = Bukkit.createInventory(null, 9, "§9§lChoisis ton Camp");
    public static boolean needpermission = false;

    public SpeedRunnerVSHunter getInstance() {
        return this.instance;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;

        this.speedrunners = new HashMap<>();
        this.hunters = new HashMap<>();
        this.speedrunnersplayerheads = new HashMap<>();
        this.itemsByName = new HashMap<>();
        this.specialplayertrack = new HashMap<>();
        this.hunterscompass = new HashMap<>();
        this.configurationoptions = new HashMap<>();

        needpermission = getConfig().getBoolean("Play_Permission");

        ConfigurationSection GameProtectionSection = getConfig().getConfigurationSection("OffGameProtection");
        for(String protectionvalue : GameProtectionSection.getKeys(false)){
            configurationoptions.put(protectionvalue, GameProtectionSection.getString(protectionvalue));
        }

        if(configurationoptions.containsKey("Disable_DayLight_Cycle")){
            if(Boolean.parseBoolean(configurationoptions.get("Disable_DayLight_Cycle"))){
                for(World world : Bukkit.getWorlds()){
                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                }
            }
        }

        if(configurationoptions.containsKey("WorldStartTime")){
            if(Long.parseLong(configurationoptions.get("WorldStartTime")) >= 0){
                for(World world : Bukkit.getWorlds()){
                    world.setTime(Long.parseLong(String.valueOf(Long.parseLong(configurationoptions.get("WorldStartTime")))));
                }
            }
        }

        createInventory();

        getCommand("speedrunnervshunter").setExecutor(new CommandSpeedRunnerVSHunter(this));
        getServer().getPluginManager().registerEvents(new SpeedRunnerVSHunterEvenement(this), this);

        // All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 10640; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

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
        /*playerheadmeta.setOwner("Hynity");*/
        if(playerheadmeta != null){
            playerheadmeta.setDisplayName("§b§lRandom");
            randomhead.setItemMeta(playerheadmeta);
        }

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

        speedrunnersinv.setItem(speedrunnersinv.getSize()-1, randomhead);
    }

    public void addSpeedRunner(Player player){
        ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD, 1);

        SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
        if(playerheadmeta != null){
            playerheadmeta.setOwningPlayer(Bukkit.getPlayer(player.getName()));
            playerheadmeta.setDisplayName("§a" + player.getName());
            playerhead.setItemMeta(playerheadmeta);
        }

        this.speedrunners.put(player, true);
        player.setPlayerListName("§a[SpeedRunner] §r" + player.getName());
        player.setDisplayName("§a[SpeedRunner] §r" + player.getName());

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
        player.setPlayerListName(player.getName());
        player.setDisplayName(player.getName());
    }

    public void SpeedRunnerHorsCourse(Player player){
        this.speedrunners.put(player, false);
        player.setPlayerListName(player.getName());
        player.setDisplayName(player.getName());
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

        if(this.speedrunners.containsValue(true)){
            if(getConfig().getBoolean("GameOptions.SpeedRunnersBecomesHuntersAtDeath")){
                speedrunners.remove(player);

                getHunters().put(player, player);
                player.setPlayerListName("§b[Chasseur] §r" + player.getName());
                player.setDisplayName("§b[Chasseur] §r" + player.getName());
                player.sendMessage("§aTu devient Chasseur suite à ta Mort !");
                for(Player pls : Bukkit.getOnlinePlayers()){
                    if(pls != player){
                        pls.sendMessage("§b[SpeedRunner] Le SpeedRunner §6" + player.getName() + "§a est devenu Chasseur suite à sa Mort !");
                    }
                }

                ItemStack compass = new ItemStack(Material.COMPASS, 1);

                player.getInventory().addItem(compass);
                if(!getSpecialPlayerHunterTrack().containsKey(player)){
                    getSpecialPlayerHunterTrack().put(player, false);
                    player.sendMessage("§6Exécutes la commande §b/speedrunner cible §6pour choisir une Cible Précise, sinon ta boussole traquera le Joueur le plus proche §2§lQuand tu cliqueras avec §6!");
                }
            }
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

        ItemMeta itM;

        if(it != null){
            itM = it.getItemMeta();

            if(itM != null){
                if(displayName != null) {
                    itM.setDisplayName(displayName);
                }
                if(lore != null) {
                    itM.setLore(lore);
                }
                if(enchname != null) {
                    itM.addEnchant(enchname, enchpower, enchdisplaying);
                }
                if(hideEnchants != null) {
                    itM.addItemFlags(hideEnchants);
                }
                if(itemFlag != null) {
                    itM.addItemFlags(itemFlag);
                }
                it.setItemMeta(itM);
            }
        }

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

    public Map<String, String> getConfigurationoptions() {
        return configurationoptions;
    }
}