package com.kingfisher.servermonitor;

import com.kingfisher.servermonitor.data.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author KingFisher
 */
public final class ServerMonitor extends JavaPlugin {

	private ServerMonitorConfiguration _configuration;
	private SQLBridge _sql;
	private DataViewerServer _viewer;
	private MonitorManager _manager;
	private MonitorRunnable _monitorRunnable;

	@Override
	public void onEnable() {
		//
		getLogger().warning("Not finished yet! The functionalities are not all implemented yet and the performances will be improved.");
		//
		saveDefaultConfig();
		try {
			_configuration = new ServerMonitorConfiguration(getConfig());
		} catch (ServerMonitorConfigurationException ex) {
			runtimeException("An error occurred while parsing the congiguration.", ex);
			return;
		}
		debug("Configuration parsed.");
		try {
			debug("Connecting to the SQL database...");
			_sql = new SQLBridge(this);
			debug("Connected.");
		} catch (SQLException ex) {
			runtimeException("An unexpected error occured while establishing the connection with the SQL database.", ex);
			return;
		}
		try {
			debug("Selecting the database " + _configuration.getSQLDatabase() + "...");
			_sql.executeNow("CREATE DATABASE IF NOT EXISTS " + _configuration.getSQLDatabase());
			_sql.executeNow("USE " + _configuration.getSQLDatabase());
			debug("Database OK.");
		} catch (SQLException ex) {
			runtimeException("An unexpected error occured while selecting the database.", ex);
			return;
		}
		debug("Opening the access data server...");
		try {
			_viewer = new DataViewerServer(this);
		} catch (IOException ex) {
			runtimeException("An unexpected error occured while opening the access data viewer.", ex);
			return;
		}
		debug("Opened.");
		debug("Starting monitoring...");
		try {
			_manager = new MonitorManager(this);
			if (_configuration.getPurgeOnLoad()) {
				_manager.purgeDB();
			}
			if (_configuration.getMonitorBlocks()) {
				_manager.register(BlocksMonitor.class);
			}
			if (_configuration.getMonitorChunks()) {
				_manager.register(ChunksMonitor.class);
			}
			if (_configuration.getMonitorClassLoading()) {
				_manager.register(ClassLoadingMonitor.class);
			}
			if (_configuration.getMonitorEntities()) {
				_manager.register(EntitiesMonitor.class);
			}
			if (_configuration.getMonitorExperience()) {
				//
			}
			if (_configuration.getMonitorGarbageCollector()) {
				_manager.register(GarbageCollectorMonitor.class);
			}
			if (_configuration.getMonitorItems()) {
				//
			}
			if (_configuration.getMonitorLivingEntities()) {
				//
			}
			if (_configuration.getMonitorMemory()) {
				_manager.register(MemoryMonitor.class);
			}
			if (_configuration.getMonitorOperatingSystem()) {
				_manager.register(OperatingSystemMonitor.class);
			}
			if (_configuration.getMonitorPlayers()) {
				_manager.register(PlayersMonitor.class);
			}
			if (_configuration.getMonitorRedstone()) {
				_manager.register(RedstoneMonitor.class);
			}
			if (_configuration.getMonitorThreads()) {
				_manager.register(TPSMonitor.class);
			}
			if (_configuration.getMonitorThreads()) {
				_manager.register(ThreadsMonitor.class);
			}
		} catch (Exception ex) {
			runtimeException("An unexpected error related to the database occured while starting monitoring.", ex);
			return;
		}
		_monitorRunnable = new MonitorRunnable();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _monitorRunnable, 0, 0);
		debug("Started.");
	}

	@Override
	public void onDisable() {
		debug("Stopping monitoring...");
		if (_manager != null) {
			_manager.unregisterAll();
		}
		debug("Stopped.");
		debug("Closing the data viewer...");
		if (_viewer != null) {
			try {
				_viewer.close();
				while (_viewer.isOpen()) {
				}
			} catch (IOException ex) {
				runtimeException("An unexpected error occured while closing the data viewer.", ex);
			}
		}
		debug("Closed.");
		debug("Closing the SQL connection...");
		if (_sql != null) {
			try {
				_sql.close();
				while (_sql.isOpen()) {
				}
			} catch (SQLException | IOException ex) {
				runtimeException("An unexpected error occured while closing the connection to the SQL database.", ex);
			}
		}
		debug("Closed.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch (command.getName().toLowerCase()) {
			case "purgemonitor":
				try {
					_manager.purgeDB();
					sender.sendMessage(ChatColor.GREEN + "Database purged." + ChatColor.RESET);
					if (!(sender instanceof ConsoleCommandSender)) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Database purged." + ChatColor.RESET);
					}
				} catch (SQLException ex) {
					sender.sendMessage(ChatColor.RED + "An unexpected error occured while cleaning the database." + ChatColor.RESET);
					runtimeException("An unexpected error occured while cleaning the database.", ex);
				}
				return true;
			default:
				return false;
		}
	}

	public ServerMonitorConfiguration getConfiguration() {
		return _configuration;
	}

	public SQLBridge getSQLBridge() {
		return _sql;
	}

	public DataViewerServer getDataViewer() {
		return _viewer;
	}

	public MonitorManager getManager() {
		return _manager;
	}

	public void runtimeException(String message, Throwable cause) {
		if ((_configuration == null) || _configuration.getDebug()) {
			throw new RuntimeException(message + " Please contact the developer if you think that the error is due to the plugin.\nHere is the error message: " + cause.getMessage() + "\nHere is the stacktrace:", cause);
		} else {
			getLogger().severe(message + " Please contact the developer if you think that the error is due to the plugin.\nHere is the error message: " + cause.getMessage() + "\nTo get more informations, you can enable debugging in config.yml.");
		}
	}

	public void debug(String message) {
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
				try {
					if (_configuration.getMaximumDataSeniority() > 0) {
						_manager.removeDataTooOld(getDate(_last + _configuration.getMaximumDataSeniority() * 3600000));
					}
					_manager.storeAll(getDate(_last));
				} catch (SQLException ex) {
					runtimeException("An unexpected error related to the database occured while storing the data.", ex);
				}
				_last = time;
				debug("Stored.");
			}
		}

		public long getLast() {
			return _last;
		}
	}
}