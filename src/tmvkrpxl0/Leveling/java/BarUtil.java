package tmvkrpxl0.Leveling.java;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Bukkit/Spigot/Bungee Java Plugin Maker.
 * EnervateD. All rights reserved.
 */
//Thanks! EnervateD!
public class BarUtil {

	private static Map<String, Object> dragons = new ConcurrentHashMap<>();
	private static Class<?> CraftPlayer;
	private static Class<?> EntityEnderDragon;
	protected BarUtil() {
		try {
			CraftPlayer = Reflection.getClass("{cb}.entity.CraftPlayer");
			EntityEnderDragon = Reflection.getClass("{nms}.EntityEnderDragon");
			LevelingMain.sender.sendMessage("constructor executed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void setBar(Player p, String text, float healthPercent) {
		LevelingMain.sender.sendMessage("setting..");
		if(CraftPlayer==null)new BarUtil();
		try {
			LevelingMain.sender.sendMessage("trying..");
			Location loc = p.getLocation();
			Class<?> PacketPlayOutSpawnEntityLiving;
			Class<?> DataWatcher = Reflection.getClass("{nms}.DataWatcher");
			PacketPlayOutSpawnEntityLiving = Reflection.getClass("{nms}.PacketPlayOutSpawnEntityLiving");
			Method gethandle = Reflection.getClass("{cb}.CraftWorld").getDeclaredMethod("getHandle");
			gethandle.setAccessible(true);
			Object world = gethandle.invoke(Reflection.getClass("{cb}.CraftWorld").cast(p.getLocation().getWorld()));
			Object dragon = EntityEnderDragon.getConstructor(Reflection.getClass("{nms}.World")).newInstance(world);
			Method setLocation = Reflection.getClass("{nms}.Entity").getDeclaredMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
			setLocation.setAccessible(true);
			setLocation.invoke(EntityEnderDragon.cast(dragon), loc.getX(), loc.getY() - 100, loc.getZ(), 0, 0);
			Object packet = PacketPlayOutSpawnEntityLiving.getConstructor(Reflection.getClass("{nms}.EntityLiving")).newInstance(EntityEnderDragon.cast(dragon));
			Object watcher = DataWatcher.getConstructor(Reflection.getClass("{nms}.Entity")).newInstance(new Object[] {null});
			Method a = DataWatcher.getDeclaredMethod("a", int.class, Object.class);
			a.setAccessible(true);
			a.invoke(watcher, 0, (byte) 0x20);
			a.invoke(watcher, 6, (healthPercent * 200) / 100);
			a.invoke(watcher, 10, text);
			a.invoke(watcher, 2, text);
			a.invoke(watcher, 11, (byte) 1);
			a.invoke(watcher, 3, (byte) 1);
			Field t = PacketPlayOutSpawnEntityLiving.getDeclaredField("l");
			t.setAccessible(true);
			t.set(packet, watcher);
			dragons.put(p.getName(), dragon);
			Reflection.sendPlayerPacket(p, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeBar(Player p) {
		try {
			if(dragons.containsKey(p.getName())) {
				Class<?> PacketPlayOutEntityDestroy = Reflection.getClass("{nms}.PacketPlayOutEntityDestroy");
				Method getId = Reflection.getClass("{nms}.Entity").getDeclaredMethod("getId");
				getId.setAccessible(true);
				Object packet = PacketPlayOutEntityDestroy.getConstructor(Array.newInstance(int.class, 0).getClass()).newInstance(new int[] {(int)getId.invoke(dragons.get(p.getName()))});
				dragons.remove(p.getName());
				Reflection.sendPlayerPacket(p, packet);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void teleportBar(Player p) {
		try {
			if(dragons.containsKey(p.getName())) {
				Class<?> PacketPlayOutEntityTeleport = Reflection.getClass("{nms}.PacketPlayOutEntityTeleport");
				Method getId = Reflection.getClass("{nms}.Entity").getDeclaredMethod("getId");
				getId.setAccessible(true);
				Location loc = p.getLocation();
				Object packet = PacketPlayOutEntityTeleport.getConstructor(int.class, int.class, int.class, 
						int.class, byte.class, byte.class, boolean.class)
						.newInstance((int)getId.invoke(EntityEnderDragon.cast(dragons.get(p.getName()))),
								(int) loc.getX() * 32, (int) (loc.getY() - 100) * 32, (int) loc.getZ() * 32,
								(byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360), false);
				Reflection.sendPlayerPacket(p, packet);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateText(Player p, String text) {
		updateBar(p, text, -1);
	}

	public static void updateHealth(Player p, float healthPercent) {
		updateBar(p, null, healthPercent);
	}

	public static void updateBar(Player p, String text, float healthPercent) {
		try {
			if(dragons.containsKey(p.getName())) {
				Class<?> DataWatcher = Reflection.getClass("{nms}.DataWatcher");
				Method a = DataWatcher.getDeclaredMethod("a", int.class, Object.class);
				a.setAccessible(true);
				Object watcher = DataWatcher.getConstructor(Reflection.getClass("{nms}.Entity")).newInstance(new Object[] {null});
				a.invoke(watcher, 0, (byte) 0x20);
				if (healthPercent != -1) a.invoke(watcher, 6, (healthPercent * 200) / 100);
				if (text != null) {
					a.invoke(watcher, 10, text);
					a.invoke(watcher, 2, text);
				}
				a.invoke(watcher, 11, (byte) 1);
				a.invoke(watcher, 3, (byte) 1);
				Class<?> PacketPlayOutEntityMetadata = Reflection.getClass("{nms}.PacketPlayOutEntityMetadata");
				Method getId = Reflection.getClass("{nms}.Entity").getDeclaredMethod("getId");
				getId.setAccessible(true);
				Object packet = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class)
						.newInstance((int)getId.invoke(dragons.get(p.getName())), watcher, true);
				Reflection.sendPlayerPacket(p, packet);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static Set<String> getPlayers() {
		Set<String> set = new HashSet<>();
		for(Map.Entry<String, Object> entry : dragons.entrySet()) {
			set.add(entry.getKey());
		}

		return set;
	}

}
