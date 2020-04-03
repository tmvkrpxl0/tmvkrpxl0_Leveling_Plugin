package tmvkrpxl0.Leveling.java;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LevelingMain extends JavaPlugin implements Listener, CommandExecutor{
	private static Hashtable<UUID, LevelingConfig> levels;
	private static Hashtable<String, String> confirms;
	private static Plugin plugin;
	protected static ConsoleCommandSender sender;
	private static String prefix;
	private static String guititle;
	private static String [] first = {"확인", "지정", "도움말", "초기화", "방식", "버전", "빛", "어둠", "업그레이드", "저장"};
	protected static Hashtable<Player, DisplayThread> displayingThreads;
	private static boolean bossBarApi = true;
	private static ScriptEngine engine;
	private static boolean isActionbarPossible = true;
	private static LinkedList<Player> openedGui;
	private static String text;
	private static ItemStack BLANK;
	private static ItemStack ATTACK;
	private static ItemStack DEFENSE;
	private static ItemStack HEALTH;
	private static ItemStack AGILITY;
	private static FileConfiguration equations;
	private static FileConfiguration configuration;
	@Override
	public void onEnable() {
		plugin = this;
		sender = this.getServer().getConsoleSender();
		confirms = new Hashtable<>();
		levels = new Hashtable<>();
		displayingThreads = new Hashtable<>();
		engine = new ScriptEngineManager().getEngineByName("JavaScript");
		openedGui = new LinkedList<>();
		try {
			File leveling = new File(plugin.getDataFolder() + File.separator + "레벨링.cfg");
			if(!leveling.exists()){
				leveling.getParentFile().mkdirs();
				leveling.createNewFile();
			}
			List<String> lines = Files.readAllLines(leveling.toPath());
			if(lines.size()>0 && lines.get(0).startsWith("\uFEFF")) {
				lines.set(0, lines.get(0).substring(1));
			}
			//781387f8-59f2-4594-b500-db93916deea1:level=23 exp=66 statPoint=0 skillPoint=23 attack=5 defense=2 health=23 agility=5 side=true previous=0 maxLevel=100 useActionbar=true
			int temp;
			int IdxTemp;
			for(String s : lines) {
				levels.put(UUID.fromString(s.substring(0, s.indexOf(":"))), new LevelingConfig(
						(IdxTemp=s.indexOf("level="))>0?Integer.parseInt(s.substring(IdxTemp+6, ((temp=s.indexOf(" ", IdxTemp+6))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("exp="))>0?Integer.parseInt(s.substring(IdxTemp+4, ((temp=s.indexOf(" ", IdxTemp+4))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("statPoint="))>0?Integer.parseInt(s.substring(IdxTemp+10, ((temp=s.indexOf(" ", IdxTemp+10))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("skillPoint="))>0?Integer.parseInt(s.substring(IdxTemp+11, ((temp=s.indexOf(" ", IdxTemp+11))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("attack="))>0?Integer.parseInt(s.substring(IdxTemp+7, ((temp=s.indexOf(" ", IdxTemp+7))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("defense="))>0?Integer.parseInt(s.substring(IdxTemp+8, ((temp=s.indexOf(" ", IdxTemp+8))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("health="))>0?Integer.parseInt(s.substring(IdxTemp+7, ((temp=s.indexOf(" ", IdxTemp+7))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("agility="))>0?Integer.parseInt(s.substring(IdxTemp+8, ((temp=s.indexOf(" ", IdxTemp+8))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("side="))<=0||Boolean.parseBoolean(s.substring(IdxTemp+5, ((temp=s.indexOf(" ", IdxTemp+5))>0?temp:s.length()-1))),
						(IdxTemp=s.indexOf("previous="))>0?Integer.parseInt(s.substring(IdxTemp+9, ((temp=s.indexOf(" ", IdxTemp+9))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("maxLevel="))>0?Integer.parseInt(s.substring(IdxTemp+9, ((temp=s.indexOf(" ", IdxTemp+9))>0?temp:s.length()-1))):0,
						(IdxTemp=s.indexOf("useActionbar="))>0&&Boolean.parseBoolean(s.substring(IdxTemp+13, ((temp=s.indexOf(" ", IdxTemp+13))>0?temp:s.length()-1)))));
			}
			File equationFile = new File(plugin.getDataFolder() + File.separator + "equations.yml");
			if(!equationFile.exists())	Files.copy(Objects.requireNonNull(plugin.getResource("equations.yml")), Paths.get(plugin.getDataFolder() + File.separator + "equations.yml"));
			equations = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "equations.yml"));
			YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("equations.yml"))));
			for(String key : tempConfig.getKeys(false)) {
				if (tempConfig.isConfigurationSection(key)) {
					if (equations.getConfigurationSection(key) == null) {
						equations.createSection(key);
						for (String sectionKey : Objects.requireNonNull(tempConfig.getConfigurationSection(key)).getKeys(false)) {
							equations.set(key + "." + sectionKey, tempConfig.get(key + "." + sectionKey));
						}
					}
				}
			}
			File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
			if(!configFile.exists())plugin.saveDefaultConfig();
			configuration = Objects.requireNonNull(YamlConfiguration.loadConfiguration(configFile));
		} catch (IOException e) {
			sender.sendMessage("플러그인 데이터 파일을 불러오는 도중 문제가 발생했습니다!");
			e.printStackTrace();
		}
		try{
			prefix = parseText(Objects.requireNonNull(configuration.getString("prefix")));
		} catch (NullPointerException e){
			e.printStackTrace();
			sender.sendMessage("오류! config.yml의 prefix 값이 잘못되었습니다!");
			if(configuration.getDefaults()==null)sender.sendMessage("default");
			else if(configuration.getDefaults().getString("prefix")==null)sender.sendMessage("string");
			prefix = parseText(Objects.requireNonNull(Objects.requireNonNull(configuration.getDefaults()).getString("prefix")));
		}
		try {
			guititle = parseText(Objects.requireNonNull(configuration.getString("guititle")));
		} catch (NullPointerException e){
			e.printStackTrace();
			sender.sendMessage("오류! config.yml의 guititle 값이 잘못되었습니다!");
			guititle = parseText(Objects.requireNonNull(Objects.requireNonNull(configuration.getDefaults()).getString("guititle")));
		}
		//BLANK
		try{
			BLANK = new ItemStack(Material.valueOf(configuration.getString("BLANK.Material")), configuration.getInt("BLANK.Count"));
		} catch(IllegalArgumentException e){
			e.printStackTrace();
			sender.sendMessage(prefix + ChatColor.RED + configuration.getString("BLANK.Material") + "이라는 아이템을 찾을 수 없습니다!");
			BLANK = new ItemStack(Material.valueOf(Objects.requireNonNull(configuration.getDefaults()).getString("BLANK.Material")), configuration.getDefaults().getInt("BLANK.Count"));
		}
		Objects.requireNonNull(BLANK.getItemMeta()).setLore(parseLore("BLANK.Lore"));
		//ATTACK
		try{
			ATTACK= new ItemStack(Material.valueOf(configuration.getString("ATTACK.Material")), configuration.getInt("ATTACK.Count"));
		} catch(IllegalArgumentException e){
			sender.sendMessage(prefix + configuration.getString("ATTACK.Material") + "이라는 아이템을 찾을 수 없습니다!");
			ATTACK= new ItemStack(Material.valueOf(Objects.requireNonNull(configuration.getDefaults()).getString("ATTACK.Material")), configuration.getDefaults().getInt("ATTACK.Count"));
		}
		Objects.requireNonNull(ATTACK.getItemMeta()).setLore(parseLore("ATTACK.Lore"));
		//DEFENSE
		try{
			DEFENSE = new ItemStack(Material.valueOf(configuration.getString("DEFENSE.Material")), configuration.getInt("DEFENSE.Count"));
		} catch(IllegalArgumentException e){
			sender.sendMessage(prefix + configuration.getString("DEFENSE.Material") + "이라는 아이템을 찾을 수 없습니다!");
			DEFENSE = new ItemStack(Material.valueOf(Objects.requireNonNull(configuration.getDefaults()).getString("DEFENSE.Material")), configuration.getDefaults().getInt("DEFENSE.Count"));
		}
		Objects.requireNonNull(DEFENSE.getItemMeta()).setLore(parseLore("DEFENSE.Lore"));
		//HEALTH
		try{
			HEALTH = new ItemStack(Material.valueOf(configuration.getString("HEALTH.Material")), configuration.getInt("HEALTH.Count"));
		} catch(IllegalArgumentException e){
			sender.sendMessage(prefix + configuration.getString("HEALTH.Material") + "이라는 아이템을 찾을 수 없습니다!");
			HEALTH = new ItemStack(Material.valueOf(Objects.requireNonNull(configuration.getDefaults()).getString("HEALTH.Material")), configuration.getDefaults().getInt("HEALTH.Count"));
		}
		Objects.requireNonNull(HEALTH.getItemMeta()).setLore(parseLore("HEALTH.Lore"));
		//AGILITY
		try{
			AGILITY = new ItemStack(Material.valueOf(configuration.getString("AGILITY.Material")), configuration.getInt("AGILITY.Count"));
		} catch(IllegalArgumentException e){
			sender.sendMessage(prefix + configuration.getString("AGILITY.Material") + "이라는 아이템을 찾을 수 없습니다!");
			AGILITY = new ItemStack(Material.valueOf(Objects.requireNonNull(configuration.getDefaults()).getString("AGILITY.Material")), configuration.getDefaults().getInt("AGILITY.Count"));
		}
		Objects.requireNonNull(AGILITY.getItemMeta()).setLore(parseLore("AGILITY.Lore"));
		//END
		Objects.requireNonNull(getCommand("레벨링")).setExecutor(this);
		getServer().getPluginManager().registerEvents(this, plugin);
		sender.sendMessage(prefix + ChatColor.GREEN + "레벨링 플러그인 v" + plugin.getDescription().getVersion() + "을 실행합니다");
		text = parseText(Objects.requireNonNull(configuration.getString("text")));
		try {
			Class.forName("org.bukkit.boss.BossBar");
		} catch (ClassNotFoundException e) {
			bossBarApi = false;
		}
		if(Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].replace("1_", "").replaceAll("_R\\d", "").replace("v", ""))<=7) {
			isActionbarPossible = false;
		}
		LevelingConfig config;
		for(Player p : getOnlinePlayers()) {
			if(!levels.containsKey(p.getUniqueId())) {
				p.sendMessage(prefix + "이 서버는 레벨링 플러그인을 사용하고 있습니다.");
				p.sendMessage(prefix + "이 플러그인은 빛과 어둠 시스템을 사용하고 있습니다. 플레이어들이 어느쪽을 선택했느냐에 따라 그들의 성장에 큰 영향을 미치게 됩니다");
				p.sendMessage(prefix + "빛을 선택하시면 돈을 지불하여 레벨제한을 10만큼 올릴 수 있습니다");
				p.sendMessage(prefix + ChatColor.BLACK + "어둠" + ChatColor.RESET + "을 선택하시면 레벨을 무료로 다시 0으로 낯출수 있습니다. 이는 레벨업을 통하여 얻어온 그 어떤것에도 영향을 끼치지 않으며, 그저 레벨을 다시 0으로 낯춰 레벨업 보상을 다시 얻을때 사용합니다.");
				p.sendMessage(prefix + "그러나 레벨을 다시 0으로 낯추고 나서 받는 레벨업 보상은 처음 받을때보다 적습니다");
				p.sendMessage(prefix + "둘중 아무것도 선택하시지 않으시면 레벨링 플러그인을 사용하실 수 없습니다");
				p.sendMessage(prefix + "선택하는법: [/레벨링 빛] 또는 [/레벨링 어둠]");
				p.sendMessage(prefix + ChatColor.RED + "신중하게 결정하세요, 이는 중간에 변경하실 수 없습니다");
			}else{
				config = levels.get(p.getUniqueId());
				if(!config.isActionbar()) {
					if(bossBarApi) {
						displayingThreads.put(p, createBossBarThread(p));
					}else
						BarUtil.setBar(p, text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())), (float)config.getExp() / ((float)getExpNeeded(config.getLevel())) * 100.0F);
				}else {
					if(!isActionbarPossible){
						p.sendMessage("이 서버에서는 액션바 사용이 불가능합니다. 대체 어떻게 액션바를 키신건지는 모르겠지만, 비활성화 하도록 하겠습니다.");
						config.setActionbar(false);
						displayingThreads.put(p, createBossBarThread(p));
					}else displayingThreads.put(p, Objects.requireNonNull(createActionbarThread(p)));
				}
			}
		}
	}

	private DisplayThread createActionbarThread(Player p) {
		if(displayingThreads.containsKey(p)) displayingThreads.remove(p).delete();
		if(!isActionbarPossible)return null;
		DisplayThread actionbarThread = new DisplayThread() {
			Class<?> PacketPlayOutChat = Reflection.getClass("{nms}.PacketPlayOutChat");
			Method ChatSerializera = null;
			Class<?> clazz = Reflection.getClass("{nms}.ChatSerializer", false);
			Object packet = null;
			LevelingConfig config;
			@Override
			public void run() {
				if(clazz==null)clazz = Reflection.getClass("{nms}.IChatBaseComponent$ChatSerializer");
				try {
					ChatSerializera = clazz.getDeclaredMethod("a", String.class);
					ChatSerializera.setAccessible(true);
				} catch (NoSuchMethodException | SecurityException e1) {
					e1.printStackTrace();
				}
				while(!this.isInterrupted()) {
					update();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						interrupt();
					}
				}
			}

			@Override
			public void update() {
				config = levels.get(p.getUniqueId());
				try {
					try {
						packet = PacketPlayOutChat.getConstructor(Reflection.getClass("{nms}.IChatBaseComponent"), byte.class)
								.newInstance(ChatSerializera.invoke(null, "{\"text\":\"" + text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())) + "\"}"), (byte)2);
					} catch(NoSuchMethodException e) {
						packet = PacketPlayOutChat.getConstructor(Reflection.getClass("{nms}.IChatBaseComponent"),
								Reflection.getClass("{nms}.ChatMessageType")).newInstance(ChatSerializera.invoke(null, "{\"text\":\"" + text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())) + "\"}"),
								Reflection.getClass("{nms}.ChatMessageType")
										.getDeclaredMethod("a", byte.class).invoke(null, (byte)2));
					}
					Reflection.sendPlayerPacket(p, packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void delete() {
				try {
					try {
						packet = PacketPlayOutChat.getConstructor(Reflection.getClass("{nms}.IChatBaseComponent"), byte.class)
								.newInstance(ChatSerializera.invoke(null, "{\"text\":\"" + "\"}"), (byte)2);
					} catch(NoSuchMethodException e) {
						packet = PacketPlayOutChat.getConstructor(Reflection.getClass("{nms}.IChatBaseComponent"),
								Reflection.getClass("{nms}.ChatMessageType")).newInstance(ChatSerializera.invoke(null, "{\"text\":\"" + "\"}"),
								Reflection.getClass("{nms}.ChatMessageType")
										.getDeclaredMethod("a", byte.class).invoke(null, (byte)2));
					}
					Reflection.sendPlayerPacket(p, packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
				interrupt();
				try {
					sleep(1);
				} catch (InterruptedException e) {
					interrupt();
				}
			}
		};
		if(displayingThreads.contains(p))displayingThreads.remove(p).delete();
		actionbarThread.start();
		return actionbarThread;
	}

	private DisplayThread createBossBarThread(Player p) {
		if(displayingThreads.containsKey(p))displayingThreads.remove(p).delete();
		DisplayThread bossBarThread = new DisplayThread() {
			Object bossBar;
			LevelingConfig config;
			@Override
			public void update() {
				if(bossBarApi){
					((BossBar)bossBar).setProgress(((double)config.getExp())/((double)getExpNeeded(config.getLevel())));
					((BossBar)bossBar).setTitle(text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())));
				}else{
					BarUtil.updateBar(p, text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())), getExpNeeded(config.getLevel()));
				}
			}

			@Override
			public void delete() {
				if(bossBarApi)((BossBar)bossBar).removeAll();
				else BarUtil.removeBar(p);
				interrupt();
				try{
					sleep(1);
				} catch (InterruptedException e){
					interrupt();
				}
			}

			@Override
			public void run() {
				try{
					config = levels.get(p.getUniqueId());
					if(bossBarApi){
						bossBar = Bukkit.createBossBar(text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())), BarColor.BLUE, BarStyle.SOLID);
						((BossBar)bossBar).setProgress(((double)config.getExp())/((double)getExpNeeded(config.getLevel())));
						((BossBar)bossBar).addPlayer(p);
					}
					else{
						BarUtil.setBar(p, text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())), (float)getExpNeeded(config.getLevel())/(float)config.getExp());
						while(!this.isInterrupted()) {
							try {
								BarUtil.teleportBar(p);
								update();
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								interrupt();
							}
						}
					}
				} catch (Exception e){
					e.printStackTrace();
				}
			}

		};
		bossBarThread.start();
		return bossBarThread;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	protected static Collection<? extends Player> getOnlinePlayers() {
		try {
			Method onlines = Bukkit.getServer().getClass().getMethod("getOnlinePlayers");
			onlines.setAccessible(true);
			Object obj = onlines.invoke(Bukkit.getServer());
			if(obj.getClass().isArray()) {
				return Arrays.asList((Player[])obj);
			}else {
				return (Collection<? extends Player>) obj;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return new LinkedList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args == null || args.length == 0) {
			printUsage(sender);
			return false;
		}else {
			Player p = null;
			if(sender instanceof Player)p = (Player)sender;
			switch(args[0]) {
				case "확인":
					if(args.length==1) {
						sender.sendMessage(prefix + "플레이어의 레벨과 경험치를 확인합니다");
						sender.sendMessage(prefix + "사용법: /레벨링 확인 <플레이어이름>");
					}else {
						Player target = Bukkit.getPlayerExact(args[1]);
						if(target!=null) {
							LevelingConfig config = levels.get(target.getUniqueId());
							sender.sendMessage(args[1] + "님의 레벨은" + config.getLevel() + " , 경험치는 " + config.getExp() + " 입니다");
						}else sender.sendMessage(args[1] + "이라는 사람을 찾을 수 없습니다!");
					}
					break;
				case "지정":
					if(args.length==1) {
						sender.sendMessage(prefix + "플레이어의 레벨과 경험치를 변경합니다");
						sender.sendMessage(prefix + "사용법: /레벨링 지정 <플레이어이름> <레벨> [경험치]");
					}else if(args.length==2){
						if(Bukkit.getPlayerExact(args[1])!=null) {
							sender.sendMessage(prefix + "사용법: /레벨링 지정 " + args[1] + ChatColor.RED + " <레벨> " + ChatColor.RESET + "[경험치]");
						}else sender.sendMessage(args[1] + "이라는 사람을 찾을 수 없습니다!");
					}else {
						Player target = Bukkit.getPlayerExact(args[1]);
						if(target!=null) {
							LevelingConfig config = levels.get(target.getUniqueId());
							try {
								int level = Integer.parseInt(args[2]);
								int exp = (args.length>3?Integer.parseInt(args[3]):config.getExp());
								config.setLevel(level);
								config.setExp(exp);
							} catch(NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "오직 숫자만 적을 수 있습니다!");
							}
						}else sender.sendMessage(args[1] + ChatColor.RED + "이라는 사람을 찾을 수 없습니다!");
					}
					break;
				case "초기화":
					if(args.length==1) {
						sender.sendMessage(prefix + "플레이어의 레벨과 경험치를 초기화합니다");
						sender.sendMessage(prefix + "사용법: /레벨링 초기화 <플레이어이름>");
					}else {
						Player target = Bukkit.getPlayerExact(args[1]);
						if(target!=null) {
							sender.sendMessage(prefix + ChatColor.RED + "정말로 " + args[1] + "님의 레벨을 초기화 하시겠습니까? 이 작업은 되돌릴 수 없습니다");
							sender.sendMessage("예/아니요");
							confirms.put(sender.getName(), args[1]);
						}sender.sendMessage(args[1] + ChatColor.RED + "이라는 사람을 찾을 수 없습니다!");
					}
					break;
				case "버전":
					sender.sendMessage(prefix + plugin.getDescription().getVersion());
					break;
				case "방식":
					if(p!=null){
						LevelingConfig config = levels.get(p.getUniqueId());
						if(args.length==1) {
							sender.sendMessage(prefix + "레벨 및 경험치 표시 방식을 바꿉니다. 현제 방식은 " + (config.isActionbar()?"액션바":"보스바") + "입니다");
							sender.sendMessage(prefix + "사용법: /레벨링 방식 <액션바|보스바>");
						}else {
							switch(args[1]) {
								case "액션바":
									if(!isActionbarPossible)sender.sendMessage(ChatColor.RED + "이 버전에서는 액션바를 사용할 수 없습니다!");
									else {
										config.setActionbar(true);
										displayingThreads.put(p, Objects.requireNonNull(createActionbarThread(p)));
										sender.sendMessage(prefix + "레벨 표시 방식을 액션바로 변경했습니다");
									}
									break;
								case "보스바":
									displayingThreads.put(p, createBossBarThread(p));
									config.setActionbar(false);
									sender.sendMessage(prefix + "레벨 표시 방식을 보스바로 변경했습니다");
									break;
								default:
									sender.sendMessage(ChatColor.RED + "사용법: /레벨링 방식 <액션바|보스바>");
									break;
							}
						}
					}else sender.sendMessage("콘솔에서 사용할 수 없는 명령어입니다!");
					break;
				case "빛":
				case "어둠":
					if(p!=null){
						if(levels.containsKey(p.getUniqueId()))sender.sendMessage(ChatColor.RED + "당신은 이미 빛과 어둠중에서 선택하셨습니다!");
						else{
							levels.put(p.getUniqueId(), new LevelingConfig(1, 0, 0, 1, 0, 0, 0, 0, args[0].equals("빛"), 0, 100, isActionbarPossible));
							displayingThreads.put(p, Objects.requireNonNull(isActionbarPossible?createActionbarThread(p):createBossBarThread(p)));
							sender.sendMessage("당신은 " + args[0] + "을 선택하셨습니다!");
						}
					}else sender.sendMessage("콘솔에서 사용할 수 없는 명령어입니다!");
					break;
				case "업그레이드":
					if(p!=null && levels.get(p.getUniqueId()).getStatPoint()>0)openGUI(p);
					else if(p!=null)p.sendMessage("남아있는 스탯 포인트가 없습니다!");
					else sender.sendMessage("콘솔에서 사용할 수 없는 명령어 입니다!");
					break;
				default:
					sender.sendMessage(ChatColor.RED + args[0] + "이란 명령어를 찾을 수 없습니다!");
					printUsage(sender);
					break;
			}
			return true;
		}
	}

	private static void printUsage(CommandSender sender) {
		sender.sendMessage(prefix + " 사용 가능한 명령어:");
		for(String args : first) {
			sender.sendMessage(prefix + args);
		}
	}

	private static void save(){
		plugin.saveConfig();
		try {
			File f = new File(plugin.getDataFolder() + File.separator + "레벨링.cfg");
			if(!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					sender.sendMessage("플러그인 설정 파일 생성에 실패하였습니다! 파일 이름: 레벨링.cfg");
					e.printStackTrace();
				}
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			for(UUID uuid : levels.keySet()) {
				////781387f8-59f2-4594-b500-db93916deea1:level=23 exp=66 statPoint=0 skillPoint=23 attack=5 defense=2 health=23 agility=5 side=true previous=0 maxLevel=100 useActionbar=true
				LevelingConfig config = levels.get(uuid);
				writer.write(uuid.toString() + ":level=" + config.getLevel() + " exp=" + config.getExp() + " statPoint=" + config.getStatPoint() + " skillPoint=" + config.getSkillPoint() +
						" attack=" + config.getAttack() + " defense=" + config.getDefense() + " health=" + config.getHealth() + " agility=" + config.getAgility() + " side=" + config.getSide() +
						" previous=" + config.getPrevious() + " maxLevel=" + config.getMaxLevel() + " useActionbar=" + config.isActionbar() + "\n");
			}
			writer.close();
			sender.sendMessage(prefix + ChatColor.GREEN + "레벨링 플러그인" + plugin.getDescription().getVersion() + "을 저장했습니다");
		} catch (FileNotFoundException e) {
			sender.sendMessage(ChatColor.RED + "불가능한 문제가 발생했습니다!");
			e.printStackTrace();

		} catch (IOException e) {
			sender.sendMessage(ChatColor.RED + "파일을 저장하는데에 문제가 발생하였습니다!");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		for(Player p : getOnlinePlayers()) {
			if(!bossBarApi)BarUtil.removeBar(p);
			if(displayingThreads.containsKey(p)) displayingThreads.remove(p).delete();
		}
		save();
	}

	@EventHandler
	@SuppressWarnings("deprecation")
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		sender.sendMessage("Joined");
		if(levels.containsKey(event.getPlayer().getUniqueId())) {
			sender.sendMessage("Contains");
			LevelingConfig config = levels.get(p.getUniqueId());
			if(!config.isActionbar()) {
				sender.sendMessage("BossBar");
				if(bossBarApi)displayingThreads.put(p, createBossBarThread(p));
				else BarUtil.setBar(p, text.replace("#level#", ""+config.getLevel()).replace("#exp#", ""+config.getExp()).replace("#expNeeded#", ""+getExpNeeded(config.getLevel())), (float)config.getExp() / ((float)getExpNeeded(config.getLevel())) * 100.0F);
			}else if(!isActionbarPossible){
				sender.sendMessage("impossible");
				p.sendMessage("이 서버에서는 액션바 사용이 불가능합니다. 대체 어떻게 액션바를 키신건지는 모르겠지만, 비활성화 하도록 하겠습니다.");
				config.setActionbar(false);
				displayingThreads.put(p, createBossBarThread(p));
			}else {
				sender.sendMessage("ActionBar");
				displayingThreads.put(p, Objects.requireNonNull(createActionbarThread(p)));
			}
			try {
				event.getPlayer().setMaxHealth((double)engine.eval(getRange("health", config.getHealth()).replace("PlayerHealth", "20").replace("health", ""+config.getHealth())));
			} catch (ScriptException e) {
				sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: health, key:" + getRange("health", config.getHealth()));
			}
			try{
				event.getPlayer().setWalkSpeed(((Double)engine.eval(getRange("walkspeed", config.getAgility()).replace("DefaultSpeed", "0.2").replace("speed", ""+config.getAttack()).replace("PlayerSpeed", ""+event.getPlayer().getWalkSpeed()))).floatValue());
			} catch(ScriptException e){
				sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: agility, key:" + getRange("walkspeed", config.getAgility()));
			}
		}else {
			p.sendMessage(prefix + "이 서버는 레벨링 플러그인을 사용하고 있습니다.");
			p.sendMessage(prefix + "이 플러그인은 빛과 어둠 시스템을 사용하고 있습니다. 플레이어들이 어느쪽을 선택했느냐에 따라 그들의 성장에 큰 영향을 미치게 됩니다");
			p.sendMessage(prefix + "빛을 선택하시면 돈을 지불하여 레벨제한을 10만큼 올릴 수 있습니다");
			p.sendMessage(prefix + ChatColor.BLACK + "어둠" + ChatColor.RESET + "을 선택하시면 레벨을 무료로 다시 0으로 낯출수 있습니다. 이는 레벨업을 통하여 얻어온 그 어떤것에도 영향을 끼치지 않으며, 그저 레벨을 다시 0으로 낯춰 레벨업 보상을 다시 얻을때 사용합니다.");
			p.sendMessage(prefix + "그러나 레벨을 다시 0으로 낯추고 나서 받는 레벨업 보상은 처음 받을때보다 적습니다");
			p.sendMessage(prefix + "둘중 아무것도 선택하시지 않으시면 레벨링 플러그인을 사용하실 수 없습니다");
			p.sendMessage(prefix + "선택하는법: [/레벨링 빛] 또는 [/레벨링 어둠]");
			p.sendMessage(prefix + ChatColor.RED + "신중하게 결정하세요, 이는 중간에 변경하실 수 없습니다");
		}
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		BarUtil.removeBar(event.getPlayer());
		if(displayingThreads.containsKey(event.getPlayer())) displayingThreads.remove(event.getPlayer()).delete();
	}

	@EventHandler
	@SuppressWarnings("deprecation")
	public void onInventoryClick(InventoryClickEvent event){
		Player p = (Player) event.getWhoClicked();
		if(openedGui.contains(p)){
			if(event.getSlot() < 27) {
				if (event.getAction().name().contains("PICKUP")) {
					LevelingConfig config = levels.get(event.getWhoClicked().getUniqueId());
					if (config.getStatPoint() > 0) {
						ItemStack clicked = Objects.requireNonNull(event.getCurrentItem());
						if (clicked.equals(ATTACK)) {
							config.setAttack(config.getAttack() + 1);
							p.sendMessage(prefix + ChatColor.RED + "공격력" + ChatColor.GREEN + "스탯이 " + (config.getAttack() - 1) + "에서" + config.getAttack() + "으로 업그레이드 되었습니다!");
						} else if (clicked.equals(DEFENSE)) {
							config.setDefense(config.getDefense() + 1);
							p.sendMessage(prefix + ChatColor.GRAY + "방어력" + ChatColor.GREEN + "스탯이 " + (config.getDefense() - 1) + "에서" + config.getDefense() + "으로 업그레이드 되었습니다!");
						} else if (clicked.equals(HEALTH)) {
							config.setHealth(config.getHealth() + 1);
							try {
								p.setMaxHealth((double) engine.eval(getRange("health", config.getHealth()).replace("PlayerHealth", "20").replace("health", "" + config.getHealth())));
							} catch (ScriptException e) {
								sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: health, key:" + getRange("health", config.getHealth()));
							}
							p.sendMessage(prefix + ChatColor.RED + "공격력" + ChatColor.GREEN + "스탯이 " + (config.getAttack() - 1) + "에서" + config.getAttack() + "으로 업그레이드 되었습니다!");
						} else if (clicked.equals(AGILITY)) {
							config.setAgility(config.getAgility() + 1);
							try {
								p.setWalkSpeed((float) engine.eval(getRange("walkspeed", config.getAgility()).replace("DefaultSpeed", "0.2").replace("speed", "" + config.getAttack())));
							} catch (ScriptException e) {
								sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: agility, key:" + getRange("walkspeed", config.getAgility()));
							}
							p.sendMessage(prefix + ChatColor.RED + "공격력" + ChatColor.GREEN + "스탯이 " + (config.getAttack() - 1) + "에서" + config.getAttack() + "으로 업그레이드 되었습니다!");
						}
						if (clicked != BLANK){
							config.setStatPoint(config.getStatPoint() - 1);
							event.setCancelled(true);
							p.closeInventory();
							openGUI(p);
						}
					} else {
						p.sendMessage(ChatColor.RED + "더이상 남아있는 스탯포인트가 없습니다!");
						p.closeInventory();
					}
				}
				event.setCancelled(true);
			}else if (event.getAction().name().equals("MOVE_TO_OTHER_INVENTORY")) {
				sender.sendMessage("" + event.getRawSlot());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Player p = (Player) event.getPlayer();
		openedGui.remove(p);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if(confirms.containsKey(event.getPlayer().getName())) {
			switch(event.getMessage()) {
				case "예":
					Player p = Bukkit.getPlayerExact(confirms.remove(event.getPlayer().getName()));
					LevelingConfig config = levels.get(Objects.requireNonNull(p).getUniqueId());
					config.setExp(0);
					config.setLevel(0);
					p.sendMessage("당신의 레벨과 경험치가 " + event.getPlayer().getName() + "님에 의해 초기화되었습니다");
					event.getPlayer().sendMessage(ChatColor.GOLD + p.getName() + "님의 레벨과 경험치를 초기화했습니다");
					break;
				case "아니요":
				case "아니오":
					confirms.remove(event.getPlayer().getName());
					event.getPlayer().sendMessage(prefix + ChatColor.GREEN + "취소되었습니다");
					break;
				default:
					event.getPlayer().sendMessage(ChatColor.RED + "오직 예 또는 아니오만 가능합니다");
					break;
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent event) {
		if(event.getEntity().getKiller() !=null && levels.containsKey(event.getEntity().getKiller().getUniqueId())){
			sender.sendMessage(event.getEntity().getName());
			if(event.getEntity() instanceof Monster && event.getEntity().getKiller() != null && event.getEntity().getName().contains("레벨")) {
				StringBuilder Builder = new StringBuilder();
				char[] charArray = event.getEntity().getName().toCharArray();
				out:for(int i = 0;i<charArray.length;i++){
					while(charArray[i]==ChatColor.COLOR_CHAR){
						i+=2;
						if(i>=charArray.length)break out;//그냥 while 끝나고 확인해도 되는데 계속 노란색 떠서 :(
					}
					Builder.append(charArray[i]);
				}
				//최악의 상황:"레벨:123"
				String bTemp = Builder.toString();
				Builder.setLength(0);
				boolean digitFound=false;
				for(int i = bTemp.indexOf("레벨")+2;i<bTemp.length();i++){
					if(i>bTemp.indexOf("레벨")+4 && !digitFound)break;
					else if(Character.isDigit(bTemp.charAt(i))){
						Builder.append(bTemp.charAt(i));
						digitFound=true;
					}else digitFound=false;
				}
				try{
					int level = Integer.parseInt(Builder.toString());
					Player p = event.getEntity().getKiller();
					LevelingConfig config = levels.get(p.getUniqueId());
					try {
						config.setExp((int)engine.eval(getRange("exp", config.getLevel()).replace("MobLevel", ""+level).replace("exp", ""+config.getExp()).replace("level", ""+config.getLevel())));
					} catch (ScriptException e) {
						e.printStackTrace();
						return;
					}
					int temp;
					boolean LevelUpped = false;
					while((temp=getExpNeeded(config.getLevel()))<=config.getExp()) {//만약 방금 얻은 경험치가 레벨업할때 필요한 경험치는 훌쩍 뛰어넘고 다음레벨까지 한번에 가도 버그 안나게
						config.setExp(config.getExp() - temp);
						config.setLevel(config.getLevel()+1);
						config.setSkillPoint(config.getSkillPoint()+1);
						config.setStatPoint(config.getStatPoint()+(config.getLevel()>config.getPrevious()?configuration.getInt("defaultStatPointPerLevelUp"):configuration.getInt("StatPointPerLevelUpAfterSettingLevelTo0")));
						LevelUpped = true;
					}
					if(LevelUpped){
						p.sendMessage(ChatColor.GREEN + "당신은 레벨 " + config.getLevel() + "로 레벨업 했습니다!");
						p.sendMessage(ChatColor.GREEN + "[/레벨링 업그레이드] 를 사용해 얻은 스탯포인트를 사용하실 수 있습니다!");
					}
					displayingThreads.get(p).update();
				} catch (NumberFormatException ignored){

				}
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Player && levels.contains(event.getDamager().getUniqueId())){
			try{
				event.setDamage((double)engine.eval(getRange("attack", levels.get(event.getDamager().getUniqueId()).getAttack()).replace("AttackDamage", ""+event.getFinalDamage()).replace("damage", ""+levels.get(event.getDamager().getUniqueId()).getAttack())));
			} catch(ScriptException e) {
				sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: attack, key:" + getRange("attack", levels.get(event.getDamager().getUniqueId()).getAttack()));
			}
		}
		if(event.getEntity() instanceof Player && levels.contains(event.getEntity().getUniqueId())){
			try{
				if(new Random().nextDouble()%100<=(double)engine.eval(getRange("dodgechance", levels.get(event.getEntity().getUniqueId()).getAgility()).replace("speed", ""+levels.get(event.getEntity().getUniqueId()).getAgility()))){
					event.getEntity().sendMessage(ChatColor.YELLOW + "회피했습니다!");
					event.getDamager().sendMessage(ChatColor.RED + "상대방이 회피했습니다!");
				}
			} catch(ScriptException e){
				sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: defense, key:" + getRange("defense", levels.get(event.getEntity().getUniqueId()).getDefense()));
			}
			try{
				event.setDamage((double)engine.eval(getRange("defense", levels.get(event.getEntity().getUniqueId()).getDefense()).replace("AttackDamage", ""+event.getFinalDamage()).replace("defense", ""+levels.get(event.getEntity().getUniqueId()).getDefense())));
			} catch(ScriptException e){
				sender.sendMessage("equations.yml 의 식이 잘못되었습니다! Section: defense, key:" + getRange("defense", levels.get(event.getEntity().getUniqueId()).getDefense()));
			}
		}
	}

	protected static int getExpNeeded(int level){
		try{
			return (int)engine.eval(getRange("levels", level).replace("level", ""+level));
		} catch(ScriptException | ClassCastException e){
			throw new NullPointerException("equations.yml 의 식이 잘못되었습니다! 레벨:" + level);
		}
	}



	protected static void openGUI(Player p){
		openedGui.remove(p);
		char[] gui = Objects.requireNonNull(configuration.getString("gui")).toCharArray();
		Inventory inventory = Bukkit.createInventory(null, 27, guititle);
		ItemStack[] items = new ItemStack[27];
		for(int i = 0;i<gui.length;i++){
			if(gui[i]=='#')items[i] = BLANK;
			else if(gui[i]=='A')items[i] = ATTACK;
			else if(gui[i]=='D')items[i] = DEFENSE;
			else if(gui[i]=='H')items[i] = HEALTH;
			else if(gui[i]=='S')items[i] = AGILITY;
		}
		inventory.setContents(items);
		p.openInventory(inventory);
		openedGui.add(p);
	}

	protected static String getRange(String Section, int n){
		ConfigurationSection section = equations.getConfigurationSection(Section);
		for(String key : Objects.requireNonNull(section).getKeys(false)){
			try{
				if(Integer.parseInt(key.substring(0, key.indexOf('~')))<=n && n<=Integer.parseInt(key.substring(key.indexOf('~')+1))){
					return section.getString(key);
				}
			} catch (NumberFormatException e){
				sender.sendMessage("equations.yml 가 잘못되었습니다! 잘못되었습니다!");
				throw new NullPointerException(Section + ":" + key + ": " + section.getString(key));
			}
		}
		throw new NullPointerException(Section + "에는 " + n + "에 해당하는 식이 없습니다!");
	}

	protected static String parseText(String textOriginal){
		StringBuilder colorTemp = new StringBuilder();
		StringBuilder textTemp = new StringBuilder();
		boolean metDollor = false;
		for(int i = 0;i<textOriginal.length();i++){
			if(textOriginal.charAt(i)=='$'){
				if(metDollor){
					try {
						textTemp.append(ChatColor.valueOf(colorTemp.toString()));
					} catch (IllegalArgumentException e){
						textTemp.append(ChatColor.RED).append("오류: ").append(colorTemp.toString()).append("이란 색 코드를 알 수가 없습니다! 버킷을 확인해 주세요!");
						sender.sendMessage("사용가능한 색 코드 목록:");
						for(ChatColor color : ChatColor.values()){
							sender.sendMessage(color.name());
						}
					}
					colorTemp.setLength(0);
					metDollor = false;
				}else{
					metDollor = true;
				}
			}else if(metDollor){
				colorTemp.append(textOriginal.charAt(i));
			}else{
				textTemp.append(textOriginal.charAt(i));
			}
		}
		return textTemp.toString();
	}

	private List<String> parseLore(String path){
		try{
			List<String> temp = Objects.requireNonNull(configuration.getStringList(path));
			for(int i = 0;i<temp.size();i++){
				temp.set(i, parseText(temp.get(i)));
			}
			return temp;
		} catch(NullPointerException e){
			e.printStackTrace();
			sender.sendMessage("오류! " + path + "값이 잘못되었습니다!");
			List<String> temp = Objects.requireNonNull(Objects.requireNonNull(configuration.getDefaults()).getStringList(path));
			for(int i = 0;i<temp.size();i++){
				temp.set(i, parseText(temp.get(i)));
			}
			return temp;
		}
	}
}
