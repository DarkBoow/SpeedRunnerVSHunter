package fr.darkbow_.speedrunnervshunter;

import com.google.common.collect.Sets;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpeedRunnerVSHunter extends JavaPlugin {
    private SpeedRunnerVSHunter instance;
    private HashMap<Player, Boolean> speedrunners;
    private HashMap<Player, Player> hunters;
    private HashMap<Player, Boolean> specialplayertrack;
    private HashMap<Player, ItemStack> speedrunnersplayerheads;
    private Map<String, ItemStack> itemsByName;
    private Map<String, String> configurationoptions;
    private List<Player> frozenhunters;
    private boolean gameStarted = false;
    public static Inventory speedrunnersinv;
    public static Inventory choixcamp;
    public static boolean needpermission = false;

    public static BukkitTask task;
    private Set<Material> HashSet;

    public String sversion;

    private File languagefile;
    private FileConfiguration languageconfig;

    public SpeedRunnerVSHunter getInstance() {
        return this.instance;
    }

    @Override
    public void onEnable() {
        if(!setupManager()){
            getLogger().severe("Failed to setup the SpeedRunnerVSHunter plugin! Running non-compatible version!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        instance = this;

        this.speedrunners = new HashMap<>();
        this.hunters = new HashMap<>();
        this.speedrunnersplayerheads = new HashMap<>();
        this.itemsByName = new HashMap<>();
        this.specialplayertrack = new HashMap<>();
        this.configurationoptions = new HashMap<>();
        this.frozenhunters = new ArrayList<>();

        createLanguageFile();

        speedrunnersinv = Bukkit.createInventory(null, 54, Objects.requireNonNull(getLanguageConfig().getString("SpeedRunners_InventoryTitle")).replace("&", "§"));
        choixcamp = Bukkit.createInventory(null, 9, Objects.requireNonNull(getLanguageConfig().getString("ChoseYourSide_InventoryTitle")).replace("&", "§"));

        this.HashSet = new HashSet<>();
        for(Material mat : Material.values()){
            if(!mat.isSolid()){
                HashSet.add(mat);
            }
        }

        needpermission = getConfig().getBoolean("Play_Permission");

        ConfigurationSection GameProtectionSection = getConfig().getConfigurationSection("OffGameProtection");
        if(GameProtectionSection != null){
            for(String protectionvalue : GameProtectionSection.getKeys(false)){
                configurationoptions.put(protectionvalue, GameProtectionSection.getString(protectionvalue));
            }
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

        System.out.println(Objects.requireNonNull(getLanguageConfig().getString("Plugin_Enabled")).replace("&", "§"));

        createInventory();

        for(World world : Bukkit.getWorlds()){
            if(Objects.requireNonNull(getConfig().getString("OffGameProtection.StartWorldBorder")).equalsIgnoreCase("SpawnRadiusGamerule")){
                world.getWorldBorder().setSize(Objects.requireNonNull(world.getGameRuleValue(GameRule.SPAWN_RADIUS)).doubleValue());
            } else {
                double bordersize = getConfig().getDouble("OffGameProtection.StartWorldBorder");

                if(bordersize >= 0.0){
                    world.getWorldBorder().setSize(getConfig().getDouble("OffGameProtection.StartWorldBorder"));
                } else {
                    world.getWorldBorder().setSize(60000000);
                }
            }
        }

        Objects.requireNonNull(getCommand("speedrunnervshunter")).setExecutor(new CommandSpeedRunnerVSHunter(this));
        getServer().getPluginManager().registerEvents(new SpeedRunnerVSHunterEvenement(this), this);

        // All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 10640; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        System.out.println(Objects.requireNonNull(getLanguageConfig().getString("Plugin_Enabled")).replace("&", "§"));
    }

    @Override
    public void onDisable() {
        System.out.println(Objects.requireNonNull(getLanguageConfig().getString("Plugin_Disabled")).replace("&", "§"));
    }

    private boolean setupManager(){
        sversion = "N/A";

        try{
            sversion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e){
            return false;
        }

        return (sversion.equals("v1_16_R1") || sversion.equals("v1_16_R2") || sversion.equals("v1_16_R3") || sversion.equals("v1_17_R1") || sversion.startsWith("v1_12"));
    }

    public FileConfiguration getLanguageConfig(){
        return this.languageconfig;
    }

    public File getLanguageFile(){
        return this.languagefile;
    }

    public void createLanguageFile(){
        languagefile = new File(getDataFolder(), "lang.yml");
        if(!languagefile.exists()){
            if(!languagefile.getParentFile().exists()){languagefile.getParentFile().mkdirs();}
            saveResource("lang.yml", false);
        }

        languageconfig = new YamlConfiguration();
        try {
            languageconfig.load(languagefile);
        } catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

    public HashMap<Player, Boolean> getSpeedRunners() {
        return this.speedrunners;
    }

    public HashMap<Player, Player> getHunters() {
        return this.hunters;
    }

    public List<String> getColoredStringList(List<String> list){
        List<String> newlist = new ArrayList<>();
        for(String line : list){
            newlist.add(line.replace("&", "§"));
        }

        return newlist;
    }

    public void createInventory(){
        choixcamp.setItem(3, getItem(Material.SUGAR, 1, (byte)0, Objects.requireNonNull(getLanguageConfig().getString("SpeedRunner_ItemName")).replace("&", "§"), getColoredStringList(getLanguageConfig().getStringList("SpeedRunner_ItemDescription")), null, 0, false, null, null));
        choixcamp.setItem(5, getItem(Material.IRON_SWORD, 1, (byte)0, Objects.requireNonNull(getLanguageConfig().getString("Hunter_ItemName")).replace("&", "§"), getColoredStringList(getLanguageConfig().getStringList("Hunter_ItemDescription")), null, 0, false, null, null));


        ItemStack randomhead = getItem(Material.PLAYER_HEAD, 1, (byte)0, Objects.requireNonNull(getLanguageConfig().getString("NearestSpeedRunner_ItemName")).replace("&", "§"), getColoredStringList(getLanguageConfig().getStringList("NearestSpeedRunner_ItemDescription")), null, 0, false, null, null);

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
        player.setPlayerListName(Objects.requireNonNull(getLanguageConfig().getString("SpeedRunners_PlayerListName")).replace("%player%", player.getName()).replace("&", "§"));
        player.setDisplayName(Objects.requireNonNull(getLanguageConfig().getString("SpeedRunners_PlayerDisplayName")).replace("%player%", player.getName()).replace("&", "§"));

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
        if(getConfig().getBoolean("GameOptions.SpectatorAfterSpeedRunnerRealDeath")){
            if(getSpeedRunners().size() > 1){
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

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
                player.setPlayerListName(Objects.requireNonNull(getLanguageConfig().getString("Hunters_PlayerListName")).replace("%player%", player.getName()).replace("&", "§"));
                player.setDisplayName(Objects.requireNonNull(getLanguageConfig().getString("Hunters_PlayerDisplayName")).replace("%player%", player.getName()).replace("&", "§"));
                player.sendMessage(Objects.requireNonNull(getLanguageConfig().getString("YouBecameAHunter")).replace("&", "§"));
                for(Player pls : Bukkit.getOnlinePlayers()){
                    if(pls != player){
                        pls.sendMessage(Objects.requireNonNull(getLanguageConfig().getString("ASpeedRunnerBecomesAHunter")).replace("%player%", player.getName()).replace("&", "§"));
                    }
                }

                ItemStack compass = new ItemStack(Material.COMPASS, 1);

                player.getInventory().addItem(compass);
                if(!getSpecialPlayerHunterTrack().containsKey(player)){
                    getSpecialPlayerHunterTrack().put(player, false);
                    player.sendMessage(Objects.requireNonNull(getLanguageConfig().getString("HunterTargetCommandInfo")).replace("&","§"));
                }
            }
        } else {
            for(Map.Entry<Player, Boolean> map : this.speedrunners.entrySet()){
                map.setValue(true);
            }

            setGameStarted(false);
            for(Player pls : this.speedrunners.keySet()){
                if(pls.isOnline()){
                    pls.sendMessage(Objects.requireNonNull(getLanguageConfig().getString("YouLostTheChase")).replace("%player%", player.getName()).replace("&", "§"));
                }
            }

            for(Player pls : this.hunters.keySet()){
                if(pls.isOnline()){
                    pls.sendMessage(Objects.requireNonNull(getLanguageConfig().getString("YouWonTheChase")).replace("%player%", player.getName()).replace("&", "§"));
                }
            }

            Bukkit.broadcastMessage(Objects.requireNonNull(getLanguageConfig().getString("TheHuntersWonTheGame")).replace("&", "§"));
            Bukkit.broadcastMessage(Objects.requireNonNull(getLanguageConfig().getString("TheSpeedRunnersLost")).replace("&", "§"));
        }
    }

    public Map<String, ItemStack> getItems() {
        return this.itemsByName;
    }

    public ItemStack getItem(final Material material, final int number, final byte data, final String displayName, final List<String> lore, final Enchantment enchname, final int enchpower, final boolean enchdisplaying, final ItemFlag hideEnchants, final ItemFlag itemFlag) {
        ItemStack it = null;
        if (material != null) {
            it = new ItemStack(material, number, data);
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

    public boolean PlayerhasAdvancement(Player player, String achname){
        Advancement ach = null;
        for (Iterator<Advancement> iter = Bukkit.getServer().advancementIterator(); iter.hasNext(); ) {
            Advancement adv = iter.next();
            if (adv.getKey().getKey().equalsIgnoreCase(achname)){
                ach = adv;
                break;
            }
        }

        if(ach != null){
            AdvancementProgress prog = player.getAdvancementProgress(ach);
            return prog.isDone();
        }

        return false;
    }

    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance, int maxLength, Player player) {
        if (transparent == null) {
            transparent = Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
        }

        if (maxDistance > 120) {
            maxDistance = 120;
        }

        ArrayList<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(player, maxDistance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (maxLength != 0 && blocks.size() > maxLength) {
                blocks.remove(0);
            }
            Material material = block.getType();
            if (!transparent.contains(material)) {
                break;
            }
        }
        return blocks;
    }

    public void spawnParticles(Set<Material> transparent, int maxDistance, int maxLength, Player player) {
        if (transparent == null) {
            transparent = Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
        }

        if (maxDistance > 120) {
            maxDistance = 120;
        }

        ArrayList<Block> blocks = new ArrayList<>();
        Iterator<Block> itr = new BlockIterator(player, maxDistance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (maxLength != 0 && blocks.size() > maxLength) {
                blocks.remove(0);
            }
            Material material = block.getType();
            if (!transparent.contains(material)) {
                break;
            }
        }

        for(Block block : blocks){
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
            block.getWorld().spawnParticle(Particle.REDSTONE, block.getLocation(), 1, dustOptions);
        }
    }

    public Set<Material> getHashSet(){
        return HashSet;
    }

    public Map<String, String> getConfigurationoptions() {
        return configurationoptions;
    }

    public List<Player> getFrozenHunters() {
        return frozenhunters;
    }
}