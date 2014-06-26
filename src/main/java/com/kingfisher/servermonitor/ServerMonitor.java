package com.kingfisher.servermonitor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author KingFisher
 */
public final class ServerMonitor extends JavaPlugin implements Listener {

	private ServerMonitorConfiguration _configuration;
	private SQLBridge _sql;
	private DataViewerServer _access;
	private MonitorRunnable _monitorRunnable;
	private BlockPlaceExecutor _blockPlaceExecutor;
	private BlockBreakExecutor _blockBreakExecutor;
	private RedstoneExecutor _redstoneExecutor;
	private PistonsExecutor _pistonsExecutor;
	private ItemSpawnExecutor _itemSpawnExecutor;
	private ItemDespawnExecutor _itemDespawnExecutor;
	private ItemDropExecutor _itemDropExecutor;
	private ItemPickupExecutor _itemPickupExecutor;
	private CreatureSpawnExecutor _creatureSpawnExecutor;
	private EntityDeathExecutor _entityDeathExecutor;
	private PlayerDeathExecutor _playerDeathExecutor;
	private EntityCountMonitorRunnable _entityCountRunnable;
	private PlayerCountMonitorRunnable _playerCountRunnable;
	private MemoryMonitorRunnable _memoryRunnable;
	private TPSMonitorRunnable _tpsRunnable;

	@Override
	public void onEnable() {
		System.out.print("Not finished yet! The functionalities are not all implemented yet and the performances will be improved.");
		saveDefaultConfig();
		try {
			_configuration = new ServerMonitorConfiguration(getConfig());
		} catch (ServerMonitorConfigurationException ex) {
			runtimeException("An error occurred while parsing the congiguration.", ex);
		}
		debug("Configuration parsed.");
		try {
			debug("Connecting to the SQL database...");
			_sql = new SQLBridge(_configuration.getSQLType(), _configuration.getSQLIP(), _configuration.getSQLPort(), _configuration.getSQLUser(), _configuration.getSQLPassword());
			debug("Connected.");
			_sql.executeNow("CREATE DATABASE IF NOT EXISTS server_monitor");
			_sql.executeNow("USE server_monitor");
		} catch (SQLException ex) {
			runtimeException("An unexpected error occured while establishing the connection with the SQL database, this can be due to your configuration, the database, or the plugin itself.", ex);
		}
		debug("Opening the access data server...");
		try {
			_access = new DataViewerServer(_configuration.getAccessPort(), _configuration.getAccessPassword(), _sql);
		} catch (IOException ex) {
			runtimeException("An unexpected error occured while opening the access data viewer, this can be due to your configuration, your connection, or the plugin itself.", ex);
		}
		debug("Opened.");
		debug("Starting monitoring...");
		try {
			if (_configuration.getMonitorRedstone()) {
				_sql.executeNow("CREATE TABLE IF NOT EXISTS redstone (date DATETIME, total INT);");
				_redstoneExecutor = new RedstoneExecutor();
				Bukkit.getPluginManager().registerEvent(BlockRedstoneEvent.class, this, EventPriority.MONITOR, _redstoneExecutor, this, true);
			}
			if (_configuration.getMonitorPistons()) {
				_pistonsExecutor = new PistonsExecutor();
				_sql.executeNow("CREATE TABLE IF NOT EXISTS pistons (date DATETIME, total INT);");
				Bukkit.getPluginManager().registerEvent(BlockPistonExtendEvent.class, this, EventPriority.MONITOR, _pistonsExecutor, this, true);
				Bukkit.getPluginManager().registerEvent(BlockPistonRetractEvent.class, this, EventPriority.MONITOR, _pistonsExecutor, this, true);
			}
			if (_configuration.getMonitorEntityCount()) {
				_entityCountRunnable = new EntityCountMonitorRunnable();
				_sql.executeNow("CREATE TABLE IF NOT EXISTS entity_count (date DATETIME, mean FLOAT, min MEDIUMINT, max MEDIUMINT);");
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _entityCountRunnable, 0, 0);
			}
			if (_configuration.getMonitorPlayerCount()) {
				_playerCountRunnable = new PlayerCountMonitorRunnable();
				_sql.executeNow("CREATE TABLE IF NOT EXISTS player_count (date DATETIME, mean FLOAT, min MEDIUMINT, max MEDIUMINT);");
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _playerCountRunnable, 0, 0);
			}
			if (_configuration.getMonitorMemory()) {
				_memoryRunnable = new MemoryMonitorRunnable();
				_sql.executeNow("CREATE TABLE IF NOT EXISTS memory (date DATETIME, mean DOUBLE, min BIGINT, max BIGINT);");
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _memoryRunnable, 0, 0);
			}
			if (_configuration.getMonitorTPS()) {
				_tpsRunnable = new TPSMonitorRunnable();
				_sql.executeNow("CREATE TABLE IF NOT EXISTS tps (date DATETIME, mean FLOAT, min FLOAT, max FLOAT);");
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _tpsRunnable, 0, 0);
			}
		} catch (SQLException ex) {
			runtimeException("An unexpected error related to the database occured while starting monitoring, this can be due to the database or the plugin itself.", ex);
		}
		//
		if (_configuration.getMonitorBlockPlace()) {
			_blockPlaceExecutor = new BlockPlaceExecutor();
			Bukkit.getPluginManager().registerEvent(BlockPlaceEvent.class, this, EventPriority.MONITOR, _blockPlaceExecutor, this, true);
		}
		if (_configuration.getMonitorBlockBreak()) {
			_blockBreakExecutor = new BlockBreakExecutor();
			Bukkit.getPluginManager().registerEvent(BlockBreakEvent.class, this, EventPriority.MONITOR, _blockBreakExecutor, this, true);
		}
		if (_configuration.getMonitorCreatureSpawn()) {
			_creatureSpawnExecutor = new CreatureSpawnExecutor();
			Bukkit.getPluginManager().registerEvent(CreatureSpawnEvent.class, this, EventPriority.MONITOR, _creatureSpawnExecutor, this, true);
		}
		if (_configuration.getMonitorEntityDeath()) {
			_entityDeathExecutor = new EntityDeathExecutor();
			Bukkit.getPluginManager().registerEvent(EntityDeathEvent.class, this, EventPriority.MONITOR, _entityDeathExecutor, this, true);
		}
		if (_configuration.getMonitorPlayerDeath()) {
			_playerDeathExecutor = new PlayerDeathExecutor();
			Bukkit.getPluginManager().registerEvent(PlayerDeathEvent.class, this, EventPriority.MONITOR, _playerDeathExecutor, this, true);
		}
		if (_configuration.getMonitorItemSpawn()) {
			_itemSpawnExecutor = new ItemSpawnExecutor();
			Bukkit.getPluginManager().registerEvent(ItemSpawnEvent.class, this, EventPriority.MONITOR, _itemSpawnExecutor, this, true);
		}
		if (_configuration.getMonitorItemDespawn()) {
			_itemDespawnExecutor = new ItemDespawnExecutor();
			Bukkit.getPluginManager().registerEvent(ItemDespawnEvent.class, this, EventPriority.MONITOR, _itemDespawnExecutor, this, true);
		}
		if (_configuration.getMonitorItemDrop()) {
			_itemDropExecutor = new ItemDropExecutor();
			Bukkit.getPluginManager().registerEvent(PlayerDropItemEvent.class, this, EventPriority.MONITOR, _itemDropExecutor, this, true);
		}
		if (_configuration.getMonitorItemPickup()) {
			_itemPickupExecutor = new ItemPickupExecutor();
			Bukkit.getPluginManager().registerEvent(PlayerPickupItemEvent.class, this, EventPriority.MONITOR, _itemPickupExecutor, this, true);
		}
		//
		_monitorRunnable = new MonitorRunnable();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _monitorRunnable, 0, 0);
		debug("Started.");
	}

	@Override
	public void onDisable() {
		debug("Stopping monitoring...");
		Bukkit.getScheduler().cancelTasks(this);
		_entityCountRunnable = null;
		_playerCountRunnable = null;
		_memoryRunnable = null;
		_tpsRunnable = null;
		if (_blockPlaceExecutor != null) {
			BlockPlaceEvent.getHandlerList().unregister((Plugin) this);
			_blockPlaceExecutor = null;
		}
		if (_blockBreakExecutor != null) {
			BlockBreakEvent.getHandlerList().unregister((Plugin) this);
			_blockBreakExecutor = null;
		}
		if (_redstoneExecutor != null) {
			BlockRedstoneEvent.getHandlerList().unregister((Plugin) this);
			_redstoneExecutor = null;
		}
		if (_pistonsExecutor != null) {
			BlockPistonExtendEvent.getHandlerList().unregister((Plugin) this);
			BlockPistonRetractEvent.getHandlerList().unregister((Plugin) this);
			_pistonsExecutor = null;
		}
		if (_creatureSpawnExecutor != null) {
			CreatureSpawnEvent.getHandlerList().unregister((Plugin) this);
			_creatureSpawnExecutor = null;
		}
		if ((_entityDeathExecutor != null) || (_playerDeathExecutor != null)) {
			EntityDeathEvent.getHandlerList().unregister((Plugin) this);
			_entityDeathExecutor = null;
			_playerDeathExecutor = null;
		}
		if (_itemSpawnExecutor != null) {
			ItemSpawnEvent.getHandlerList().unregister((Plugin) this);
			_itemSpawnExecutor = null;
		}
		if (_itemDespawnExecutor != null) {
			ItemDespawnEvent.getHandlerList().unregister((Plugin) this);
			_itemDespawnExecutor = null;
		}
		if (_itemDropExecutor != null) {
			PlayerDropItemEvent.getHandlerList().unregister((Plugin) this);
			_itemDropExecutor = null;
		}
		if (_itemPickupExecutor != null) {
			PlayerPickupItemEvent.getHandlerList().unregister((Plugin) this);
			_itemPickupExecutor = null;
		}
		debug("Stopped.");
		debug("Closing the data viewer...");
		if (_access != null) {
			try {
				_access.close();
			} catch (IOException ex) {
				runtimeException("An unexpected error occured while closing the data viewer, this can be due to your connection or the plugin itself.", ex);
			}
		}
		debug("Closed.");
		debug("Closing the SQL connection...");
		if (_sql != null) {
			try {
				_sql.close();
			} catch (IOException ex) {
				runtimeException("An unexpected error occured while closing the connection to the SQL database, this can be due to the database or the plugin itself.", ex);
			}
		}
		debug("Closed.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch (command.getName().toLowerCase()) {
			case "purgemonitor":
				purgeDB();
				return true;
			case "reloadmonitor":
				onEnable();
				onDisable();
				sender.sendMessage(ChatColor.GREEN + "ServerMonitor reloaded." + ChatColor.RESET);
				return true;
			default:
				return false;
		}
	}

	public void purgeDB() {
		System.out.print("Not implemented yet.");
		//todo
	}

	protected static void runtimeException(String message, Throwable cause) {
		throw new RuntimeException(message + " Please contact the developer if you think that the error is due to the plugin.\nHere is the error message: " + cause.getMessage() + "\nHere is the stacktrace:", cause);
	}

	protected void debug(String message) {
		if (_configuration.getDebug()) {
			getLogger().info(message);
		}
	}

	private String getDate(long millis) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(millis);
		return String.format("'%04d-%02d-%02d %02d:%02d:%02d'", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
	}

	private final class MonitorRunnable implements Runnable {

		private long _last = System.currentTimeMillis();

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			if ((time - _last) >= _configuration.getPrecision()) {
				debug("Storing collected data...");
				String date = getDate(_last);
				try {
					if (_configuration.getMonitorRedstone()) {
						_sql.executeAsync("INSERT INTO redstone (date, total) VALUES (" + date + ", " + _redstoneExecutor.getTotal() + ");");
						_redstoneExecutor.ini();
					}
					if (_configuration.getMonitorPistons()) {
						_sql.executeAsync("INSERT INTO pistons (date, total) VALUES (" + date + ", " + _pistonsExecutor.getTotal() + ");");
						_pistonsExecutor.ini();
					}
					if (_configuration.getMonitorEntityCount()) {
						_sql.executeAsync("INSERT INTO entity_count (date, mean, min, max) VALUES (" + date + ", " + _entityCountRunnable.getMean() + ", " + _entityCountRunnable.getMin() + ", " + _entityCountRunnable.getMax() + ");");
						_entityCountRunnable.ini();
					}
					if (_configuration.getMonitorPlayerCount()) {
						_sql.executeAsync("INSERT INTO player_count (date, mean, min, max) VALUES (" + date + ", " + _playerCountRunnable.getMean() + ", " + _playerCountRunnable.getMin() + ", " + _playerCountRunnable.getMax() + ");");
						_playerCountRunnable.ini();
					}
					if (_configuration.getMonitorMemory()) {
						_sql.executeAsync("INSERT INTO memory (date, mean, min, max) VALUES (" + date + ", " + _memoryRunnable.getMean() + ", " + _memoryRunnable.getMin() + ", " + _memoryRunnable.getMax() + ");");
						_memoryRunnable.ini();
					}
					if (_configuration.getMonitorTPS()) {
						_sql.executeAsync("INSERT INTO tps (date, mean, min, max) VALUES (" + date + ", " + _tpsRunnable.getMean() + ", " + _tpsRunnable.getMin() + ", " + _tpsRunnable.getMax() + ");");
						_tpsRunnable.ini();
					}
				} catch (SQLException ex) {
					runtimeException("An unexpected error related to the database occured while storing the data, this can be due to the database or the plugin itself.", ex);
				}
				_last = time;
				debug("Stored.");
			}
		}

		public long getLast() {
			return _last;
		}
	}

	private final class BlockPlaceExecutor implements EventExecutor {

		private int _total;
		private final Map<Material, Integer> _byTypes = new EnumMap(Material.class);
		private final Map<UUID, Integer> _byPlayers = new HashMap<>();

		public BlockPlaceExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			BlockPlaceEvent event = (BlockPlaceEvent) arg1;
			if (!event.isCancelled()) {
				Material type = event.getBlock().getType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
					UUID player = event.getPlayer().getUniqueId();
					_byPlayers.put(player, _byPlayers.get(player) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
			_byPlayers.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<Material, Integer> getByTypes() {
			return _byTypes;
		}

		public Map<UUID, Integer> getByPlayers() {
			return _byPlayers;
		}
	}

	private final class BlockBreakExecutor implements EventExecutor {

		private int _total;
		private final Map<Material, Integer> _byTypes = new EnumMap(Material.class);
		private final Map<UUID, Integer> _byPlayers = new HashMap<>();

		public BlockBreakExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			BlockBreakEvent event = (BlockBreakEvent) arg1;
			if (!event.isCancelled()) {
				Material type = event.getBlock().getType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
					UUID player = event.getPlayer().getUniqueId();
					_byPlayers.put(player, _byPlayers.get(player) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
			_byPlayers.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<Material, Integer> getByTypes() {
			return _byTypes;
		}

		public Map<UUID, Integer> getByPlayers() {
			return _byPlayers;
		}
	}

	private final class ItemSpawnExecutor implements EventExecutor {

		private int _total;
		private final Map<Material, Integer> _byTypes = new EnumMap(Material.class);

		public ItemSpawnExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			ItemSpawnEvent event = (ItemSpawnEvent) arg1;
			if (!event.isCancelled()) {
				Material type = event.getEntity().getItemStack().getType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<Material, Integer> getByTypes() {
			return _byTypes;
		}
	}

	private final class ItemDespawnExecutor implements EventExecutor {

		private int _total;
		private final Map<Material, Integer> _byTypes = new EnumMap(Material.class);

		public ItemDespawnExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			ItemDespawnEvent event = (ItemDespawnEvent) arg1;
			if (!event.isCancelled()) {
				Material type = event.getEntity().getItemStack().getType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<Material, Integer> getByTypes() {
			return _byTypes;
		}
	}

	private final class ItemDropExecutor implements EventExecutor {

		private int _total;
		private final Map<Material, Integer> _byTypes = new EnumMap(Material.class);
		private final Map<UUID, Integer> _byPlayers = new HashMap<>();

		public ItemDropExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			PlayerDropItemEvent event = (PlayerDropItemEvent) arg1;
			if (!event.isCancelled()) {
				Material type = event.getItemDrop().getItemStack().getType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
					UUID player = event.getPlayer().getUniqueId();
					_byPlayers.put(player, _byPlayers.get(player) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
			_byPlayers.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<Material, Integer> getByTypes() {
			return _byTypes;
		}

		public Map<UUID, Integer> getByPlayers() {
			return _byPlayers;
		}
	}

	private final class ItemPickupExecutor implements EventExecutor {

		private int _total;
		private final Map<Material, Integer> _byTypes = new EnumMap(Material.class);
		private final Map<UUID, Integer> _byPlayers = new HashMap<>();

		public ItemPickupExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			PlayerPickupItemEvent event = (PlayerPickupItemEvent) arg1;
			if (!event.isCancelled()) {
				Material type = event.getItem().getItemStack().getType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
					UUID player = event.getPlayer().getUniqueId();
					_byPlayers.put(player, _byPlayers.get(player) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
			_byPlayers.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<Material, Integer> getByTypes() {
			return _byTypes;
		}

		public Map<UUID, Integer> getByPlayers() {
			return _byPlayers;
		}
	}

	private final class CreatureSpawnExecutor implements EventExecutor {

		private int _total;
		private final Map<EntityType, Integer> _byTypes = new EnumMap(EntityType.class);

		public CreatureSpawnExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			CreatureSpawnEvent event = (CreatureSpawnEvent) arg1;
			if (!event.isCancelled()) {
				EntityType type = event.getEntityType();
				if (_byTypes.containsKey(type)) {
					_total++;
					_byTypes.put(type, _byTypes.get(type) + 1);
				}
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<EntityType, Integer> getByTypes() {
			return _byTypes;
		}
	}

	private final class EntityDeathExecutor implements EventExecutor {

		private int _total;
		private final Map<EntityType, Integer> _byTypes = new EnumMap(EntityType.class);

		public EntityDeathExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			EntityDeathEvent event = (EntityDeathEvent) arg1;
			EntityType type = event.getEntityType();
			if (_byTypes.containsKey(type)) {
				_total++;
				_byTypes.put(type, _byTypes.get(type) + 1);
			}
		}

		public void ini() {
			_total = 0;
			_byTypes.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<EntityType, Integer> getByTypes() {
			return _byTypes;
		}
	}

	private final class PlayerDeathExecutor implements EventExecutor {

		private int _total;
		private final Map<UUID, Integer> _byPlayers = new HashMap<>();

		public PlayerDeathExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			if (arg1 instanceof PlayerDeathEvent) {
				_total++;
				UUID player = ((EntityEvent) arg1).getEntity().getUniqueId();
				_byPlayers.put(player, _byPlayers.get(player) + 1);
			}
		}

		public void ini() {
			_total = 0;
			_byPlayers.clear();
		}

		public int getTotal() {
			return _total;
		}

		public Map<UUID, Integer> getByPlayers() {
			return _byPlayers;
		}
	}

	private final class RedstoneExecutor implements EventExecutor {

		private int _total;

		public RedstoneExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			_total++;
		}

		public void ini() {
			_total = 0;
		}

		public int getTotal() {
			return _total;
		}
	}

	private final class PistonsExecutor implements EventExecutor {

		private int _total;

		public PistonsExecutor() {
			ini();
		}

		@Override
		public void execute(Listener arg0, Event arg1) throws EventException {
			_total++;
		}

		public void ini() {
			_total = 0;
		}

		public int getTotal() {
			return _total;
		}
	}

	private final class EntityCountMonitorRunnable implements Runnable {

		private long _total;
		private double _i;
		private int _min;
		private int _max;

		public EntityCountMonitorRunnable() {
			ini();
		}

		@Override
		public void run() {
			int t = 0;
			for (World world : Bukkit.getWorlds()) {
				t += world.getEntities().size();
			}
			_total += t;
			_i++;
			if (t < _min) {
				_min = t;
			}
			if (t > _max) {
				_max = t;
			}
		}

		public void ini() {
			_total = 0L;
			_i = 0D;
			_min = Integer.MAX_VALUE;
			_max = 0;
		}

		public float getMean() {
			return (float) (_total / _i);
		}

		public int getMin() {
			return _min;
		}

		public int getMax() {
			return _max;
		}
	}

	private final class PlayerCountMonitorRunnable implements Runnable {

		private long _total;
		private double _i;
		private int _min;
		private int _max;

		public PlayerCountMonitorRunnable() {
			ini();
		}

		@Override
		public void run() {
			int t = Bukkit.getOnlinePlayers().length;
			_total += t;
			_i++;
			if (t < _min) {
				_min = t;
			}
			if (t > _max) {
				_max = t;
			}
		}

		public void ini() {
			_total = 0L;
			_i = 0D;
			_min = Integer.MAX_VALUE;
			_max = 0;
		}

		public float getMean() {
			return (float) (_total / _i);
		}

		public int getMin() {
			return _min;
		}

		public int getMax() {
			return _max;
		}
	}

	private final class MemoryMonitorRunnable implements Runnable {

		private BigDecimal _total;
		private long _i;
		private long _min;
		private long _max;

		public MemoryMonitorRunnable() {
			ini();
		}

		@Override
		public void run() {
			Runtime rt = Runtime.getRuntime();
			long t = rt.totalMemory() - rt.freeMemory();
			_total = _total.add(BigDecimal.valueOf(t));
			_i++;
			if (t < _min) {
				_min = t;
			}
			if (t > _max) {
				_max = t;
			}
		}

		public void ini() {
			_total = BigDecimal.ZERO;
			_i = 0L;
			_min = Long.MAX_VALUE;
			_max = 0L;
		}

		public double getMean() {
			return _total.divide(BigDecimal.valueOf(_i), MathContext.DECIMAL64).doubleValue();
		}

		public long getMin() {
			return _min;
		}

		public long getMax() {
			return _max;
		}
	}

	private final class TPSMonitorRunnable implements Runnable {

		private static final long TICK_DURATION = 50L;
		private static final double TICKS_PER_SECOND = 1D / TICK_DURATION;

		private double _total;
		private long _i;
		private double _min;
		private double _max;
		private long _lastTick;

		public TPSMonitorRunnable() {
			ini();
			_lastTick = System.currentTimeMillis();
		}

		@Override
		public void run() {
			long t = System.currentTimeMillis();
			long diff = t - _lastTick;
			if (diff >= TICK_DURATION) {
				double tps = (TICKS_PER_SECOND / diff) * TICK_DURATION;
				_total += tps;
				_i++;
				if (tps < _min) {
					_min = tps;
				}
				if (tps > _max) {
					_max = tps;
				}
			}
			_lastTick = t;
		}

		public void ini() {
			_total = 0D;
			_i = 0L;
			_min = Double.MAX_VALUE;
			_max = 0D;
		}

		public float getMean() {
			return (float) (_total / _i);
		}

		public float getMin() {
			return (float) _min;
		}

		public float getMax() {
			return (float) _max;
		}
	}
}