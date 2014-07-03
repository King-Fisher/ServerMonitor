package com.kingfisher.servermonitor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author KingFisher
 */
public final class ServerMonitorConfiguration {

	private final int _precision;
	private final boolean _debug;
	private final boolean _useUUIDs;
	private final int _maxSeniority;
	private final boolean _purgeOnLoad;

	private final boolean _blocks;
	private final boolean _chunks;
	private final boolean _classLoading;
	private final boolean _entities;
	private final boolean _experience;
	private final boolean _garbageCollector;
	private final boolean _items;
	private final boolean _livingEntities;
	private final boolean _memory;
	private final boolean _operatingSystem;
	private final boolean _players;
	private final boolean _redstone;
	private final boolean _threads;
	private final boolean _tps;

	private final int _viewerPort;
	private final String _viewerPassword;

	private final String _sqlDatabase;
	private final String _sqlType;
	private final String _sqlIP;
	private final int _sqlPort;
	private final String _sqlUser;
	private final String _sqlPassword;

	public ServerMonitorConfiguration(FileConfiguration config) throws ServerMonitorConfigurationException {
		_precision = config.getInt("precision") * 1000;
		if (_precision <= 0) {
			throw new ServerMonitorConfigurationException("precision must be higher than 0.");
		}
		_debug = config.getBoolean("debug");
		_useUUIDs = config.getBoolean("use-player-uuids");
		_maxSeniority = config.getInt("maximum-data-seniority");
		_purgeOnLoad = config.getBoolean("purge-on-load");

		_blocks = config.getBoolean("blocks");
		_chunks = config.getBoolean("chunks");
		_classLoading = config.getBoolean("class-loading");
		_entities = config.getBoolean("entities");
		_experience = config.getBoolean("experience");
		_garbageCollector = config.getBoolean("garbage-collector");
		_items = config.getBoolean("items");
		_livingEntities = config.getBoolean("living-entities");
		_memory = config.getBoolean("memory");
		_operatingSystem = config.getBoolean("operating-system");
		_players = config.getBoolean("players");
		_redstone = config.getBoolean("redstone");
		_threads = config.getBoolean("threads");
		_tps = config.getBoolean("tps");

		_viewerPort = config.getInt("access-port");
		_viewerPassword = config.getString("access-password");

		_sqlDatabase = config.getString("sql-database");
		_sqlType = config.getString("sql-type");
		_sqlIP = config.getString("sql-ip");
		_sqlPort = config.getInt("sql-port");
		_sqlUser = config.getString("sql-user");
		_sqlPassword = config.getString("sql-password");
	}

	public int getPrecision() {
		return _precision;
	}

	public boolean getDebug() {
		return _debug;
	}

	public boolean getUsePlayerUUIDs() {
		return _useUUIDs;
	}

	public int getMaximumDataSeniority() {
		return _maxSeniority;
	}

	public boolean getPurgeOnLoad() {
		return _purgeOnLoad;
	}

	public boolean getMonitorBlocks() {
		return _blocks;
	}

	public boolean getMonitorChunks() {
		return _chunks;
	}

	public boolean getMonitorClassLoading() {
		return _classLoading;
	}

	public boolean getMonitorEntities() {
		return _entities;
	}

	public boolean getMonitorExperience() {
		return _experience;
	}

	public boolean getMonitorGarbageCollector() {
		return _garbageCollector;
	}

	public boolean getMonitorItems() {
		return _items;
	}

	public boolean getMonitorLivingEntities() {
		return _livingEntities;
	}

	public boolean getMonitorMemory() {
		return _memory;
	}

	public boolean getMonitorOperatingSystem() {
		return _operatingSystem;
	}

	public boolean getMonitorPlayers() {
		return _players;
	}

	public boolean getMonitorRedstone() {
		return _redstone;
	}

	public boolean getMonitorThreads() {
		return _threads;
	}

	public boolean getMonitorTPS() {
		return _tps;
	}

	public int getDataViewerPort() {
		return _viewerPort;
	}

	public String getDataViewerPassword() {
		return _viewerPassword;
	}

	public String getSQLDatabase() {
		return _sqlDatabase;
	}

	public String getSQLType() {
		return _sqlType;
	}

	public String getSQLIP() {
		return _sqlIP;
	}

	public int getSQLPort() {
		return _sqlPort;
	}

	public String getSQLUser() {
		return _sqlUser;
	}

	public String getSQLPassword() {
		return _sqlPassword;
	}
}