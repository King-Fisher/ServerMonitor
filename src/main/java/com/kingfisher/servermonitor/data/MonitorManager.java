package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author KingFisher
 */
public final class MonitorManager {

	private final ServerMonitor _serverMonitor;
	private final Map<Class<? extends DataMonitor>, DataMonitor> _monitors = new ConcurrentHashMap<>();

	public MonitorManager(ServerMonitor serverMonitor) {
		_serverMonitor = serverMonitor;
	}

	public void register(Class<? extends DataMonitor> monitor) {
		if (!_monitors.containsKey(monitor)) {
			try {
				DataMonitor m = monitor.getDeclaredConstructor(_serverMonitor.getClass()).newInstance(_serverMonitor);
				_monitors.put(monitor, m);
				m.createTables();
				m.initializeData();
				m.register();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SQLException ex) {
				_serverMonitor.runtimeException("An error occured, please contact the developer.", ex);
			}
		}
	}

	private void unregister(Class<? extends DataMonitor> monitor) {
		DataMonitor m = _monitors.remove(monitor);
		m.unregister();
	}

	public void unregisterAll() {
		for (Class<? extends DataMonitor> monitor : _monitors.keySet()) {
			unregister(monitor);
		}
	}

	private void store(Class<? extends DataMonitor> monitor, String date) throws SQLException {
		DataMonitor m = _monitors.get(monitor);
		m.storeData(date);
		m.initializeData();
	}

	public void storeAll(String date) throws SQLException {
		for (Class<? extends DataMonitor> monitor : _monitors.keySet()) {
			store(monitor, date);
		}
	}

	public void purgeDB() throws SQLException {
		_serverMonitor.debug("Purging database...");
		for (DataMonitor monitor : _monitors.values()) {
			monitor.deleteTables();
			monitor.createTables();
		}
		_serverMonitor.debug("Database purged.");
	}

	public void removeDataTooOld(String date) throws SQLException {
		for (DataMonitor monitor : _monitors.values()) {
			monitor.removeDataTooOld(date);
		}
	}
}