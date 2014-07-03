package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Table;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 *
 * @author KingFisher
 */
public abstract class DataMonitor {

	private final ServerMonitor _serverMonitor;
	private int _taskID = -1;

	protected DataMonitor(ServerMonitor serverMonitor) {
		_serverMonitor = serverMonitor;
	}

	public final ServerMonitor getServerMonitor() {
		return _serverMonitor;
	}

	protected final String getPlayer(Player player) {
		return _serverMonitor.getConfiguration().getUsePlayerUUIDs() ? player.getUniqueId().toString() : player.getName();
	}

	public abstract Table[] getTables();

	public final void createTables() throws SQLException {
		for (Table table : getTables()) {
			_serverMonitor.getSQLBridge().executeNow(table.createIfNotExists());
		}
	}

	public final void deleteTables() throws SQLException {
		for (Table table : getTables()) {
			_serverMonitor.getSQLBridge().executeNow(table.deleteIfExists());
		}
	}

	public final void register() {
		if (this instanceof Runnable) {
			_taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(_serverMonitor, (Runnable) this, 0, 0);
		}
		if (this instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) this, _serverMonitor);
		}
	}

	public final void unregister() {
		if (this instanceof Runnable) {
			Bukkit.getScheduler().cancelTask(_taskID);
		}
		if (this instanceof Listener) {
			for (Method method : getClass().getDeclaredMethods()) {
				if (method.isAnnotationPresent(EventHandler.class)) {
					try {
						((HandlerList) method.getParameterTypes()[0].getMethod("getHandlerList").invoke(null)).unregister((Listener) this);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						_serverMonitor.runtimeException("An error occured, please contact the developer.", ex);
					}
				}
			}
		}
	}

	public abstract void initializeData();

	public abstract void storeData(String date) throws SQLException;

	public final void removeDataTooOld(String date) throws SQLException {
		for (Table table : getTables()) {
			_serverMonitor.getSQLBridge().executeAsync("DELETE FROM " + table.getName() + " WHERE date > " + date + ";");
		}
	}
}