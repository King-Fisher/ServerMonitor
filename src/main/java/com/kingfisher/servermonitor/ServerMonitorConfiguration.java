package com.kingfisher.servermonitor;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

/**
 *
 * @author KingFisher
 */
public final class ServerMonitorConfiguration {

	private final int _precision;
	private final boolean _debug;

	private final boolean _blockPlace;
	private final boolean _blockBreak;
	private final boolean _redstone;
	private final boolean _pistons;
	private final boolean _entityCount;
	private final boolean _playerCount;
	private final boolean _creatureSpawn;
	private final boolean _entityDeath;
	private final boolean _playerDeath;
	private final boolean _itemSpawn;
	private final boolean _itemDespawn;
	private final boolean _itemDrop;
	private final boolean _itemPickup;
	private final boolean _memory;
	private final boolean _tps;

	private final int _accessPort;
	private final String _accessPassword;

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

		_blockPlace = config.getBoolean("block-place");
		_blockBreak = config.getBoolean("block-break");
		_redstone = config.getBoolean("redstone");
		_pistons = config.getBoolean("pistons");
		_entityCount = config.getBoolean("entity-count");
		_playerCount = config.getBoolean("entity-count");
		_creatureSpawn = config.getBoolean("creature-spawn");
		_entityDeath = config.getBoolean("entity-death");
		_playerDeath = config.getBoolean("player-death");
		_itemSpawn = config.getBoolean("item-spawn");
		_itemDespawn = config.getBoolean("item-despawn");
		_itemDrop = config.getBoolean("item-drop");
		_itemPickup = config.getBoolean("item-pickup");
		_memory = config.getBoolean("memory");
		_tps = config.getBoolean("tps");

		_accessPort = config.getInt("access-port");
		_accessPassword = config.getString("access-password");

		_sqlType = config.getString("sql-type");
		_sqlIP = config.getString("sql-ip");
		_sqlPort = config.getInt("sql-port");
		_sqlUser = config.getString("sql-user");
		_sqlPassword = config.getString("sql-password");
	}

	private static Material getMaterial(String string) throws ServerMonitorConfigurationException {
		try {
			return Material.valueOf(string.toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new ServerMonitorConfigurationException("Invalid material: " + string + ".", ex);
		}
	}

	private static EntityType getEntityType(String string) throws ServerMonitorConfigurationException {
		try {
			return EntityType.valueOf(string.toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new ServerMonitorConfigurationException("Invalid entity type: " + string + ".", ex);
		}
	}

	public int getPrecision() {
		return _precision;
	}

	public boolean getDebug() {
		return _debug;
	}

	public boolean getMonitorBlockPlace() {
		return _blockPlace;
	}

	public boolean getMonitorBlockBreak() {
		return _blockBreak;
	}

	public boolean getMonitorCreatureSpawn() {
		return _creatureSpawn;
	}

	public boolean getMonitorEntityDeath() {
		return _entityDeath;
	}

	public boolean getMonitorPlayerDeath() {
		return _playerDeath;
	}

	public boolean getMonitorItemSpawn() {
		return _itemSpawn;
	}

	public boolean getMonitorItemDespawn() {
		return _itemDespawn;
	}

	public boolean getMonitorItemDrop() {
		return _itemDrop;
	}

	public boolean getMonitorItemPickup() {
		return _itemPickup;
	}

	public boolean getMonitorEntityCount() {
		return _entityCount;
	}

	public boolean getMonitorPlayerCount() {
		return _playerCount;
	}

	public boolean getMonitorRedstone() {
		return _redstone;
	}

	public boolean getMonitorPistons() {
		return _pistons;
	}

	public boolean getMonitorMemory() {
		return _memory;
	}

	public boolean getMonitorTPS() {
		return _tps;
	}

	public int getAccessPort() {
		return _accessPort;
	}

	public String getAccessPassword() {
		return _accessPassword;
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