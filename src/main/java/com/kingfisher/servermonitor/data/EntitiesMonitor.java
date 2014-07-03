package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.EnumFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import com.kingfisher.servermonitor.mysql.formats.VarCharFormat;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 *
 * @author KingFisher
 */
public final class EntitiesMonitor extends DataMonitor implements Runnable {

	private int _i;
	private int _total;
	private int _min;
	private int _max;
	private final Map<EntityType, Integer> _total_types = new EnumMap<>(EntityType.class);
	private final Map<EntityType, Integer> _min_types = new EnumMap<>(EntityType.class);
	private final Map<EntityType, Integer> _max_types = new EnumMap<>(EntityType.class);
	private final Map<String, Integer> _total_worlds = new HashMap<>();
	private final Map<String, Integer> _min_worlds = new HashMap<>();
	private final Map<String, Integer> _max_worlds = new HashMap<>();

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_entities", new Column("date", DateTimeFormat.FORMAT), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT)),
		new Table("server_monitor_entities_types", new Column("date", DateTimeFormat.FORMAT), new Column("type", new EnumFormat((Object[]) EntityType.values())), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT)),
		new Table("server_monitor_entities_worlds", new Column("date", DateTimeFormat.FORMAT), new Column("world", new VarCharFormat((short) 255)), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT))
	};

	EntitiesMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void run() {
		Map<EntityType, Integer> _count_types = new EnumMap<>(EntityType.class);
		for (EntityType type : EntityType.values()) {
			_count_types.put(type, 0);
		}
		int total = 0;
		_i++;
		for (World world : Bukkit.getWorlds()) {
			String w = world.getName();
			List<Entity> entities = world.getEntities();
			int count = world.getEntities().size();
			total += count;
			if (_total_worlds.containsKey(w)) {
				_total_worlds.put(w, _total_worlds.get(w) + count);
				if (count < _min_worlds.get(w)) {
					_min_worlds.put(w, count);
				}
				if (count > _max_worlds.get(w)) {
					_max_worlds.put(w, count);
				}
			} else {
				_total_worlds.put(w, count);
				_min_worlds.put(w, count);
				_max_worlds.put(w, count);
			}
			for (Entity entity : entities) {
				EntityType type = entity.getType();
				_count_types.put(type, _count_types.get(type) + 1);
			}
		}
		for (Entry<EntityType, Integer> entry : _count_types.entrySet()) {
			_total_types.put(entry.getKey(), _total_types.get(entry.getKey()) + entry.getValue());
			if (entry.getValue() < _min_types.get(entry.getKey())) {
				_min_types.put(entry.getKey(), entry.getValue());
			}
			if (entry.getValue() > _max_types.get(entry.getKey())) {
				_max_types.put(entry.getKey(), entry.getValue());
			}
		}
		_total += total;
		if (total < _min) {
			_min = total;
		}
		if (total > _max) {
			_max = total;
		}
	}

	@Override
	public void initializeData() {
		_i = 0;
		_total = 0;
		_min = Integer.MAX_VALUE;
		_max = Integer.MIN_VALUE;
		for (EntityType type : EntityType.values()) {
			_total_types.put(type, 0);
			_min_types.put(type, Integer.MAX_VALUE);
			_max_types.put(type, Integer.MIN_VALUE);
		}
		_total_worlds.clear();
		_min_worlds.clear();
		_max_worlds.clear();
}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("mean", ((float) _total) / _i);
		row.setValue("min", _min);
		row.setValue("max", _max);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		for (EntityType type : EntityType.values()) {
			row = _tables[1].insert();
			row.setValue("date", date);
			row.setValue("type", '\'' + type.toString() + '\'');
			row.setValue("mean", ((float) _total_types.get(type)) / _i);
			row.setValue("min", _min_types.get(type));
			row.setValue("max", _max_types.get(type));
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
		for (String world : _total_worlds.keySet()) {
			row = _tables[2].insert();
			row.setValue("date", date);
			row.setValue("world", '\'' + world + '\'');
			row.setValue("mean", ((float) _total_worlds.get(world)) / _i);
			row.setValue("min", _min_worlds.get(world));
			row.setValue("max", _max_worlds.get(world));
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}