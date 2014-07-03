package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import com.kingfisher.servermonitor.mysql.formats.VarCharFormat;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 *
 * @author KingFisher
 */
public final class ChunksMonitor extends DataMonitor implements Runnable {

	private int _i;
	private int _total;
	private int _min;
	private int _max;
	private final Map<String, Integer> _byWorldsTotal = new HashMap<>();
	private final Map<String, Integer> _byWorldsMin = new HashMap<>();
	private final Map<String, Integer> _byWorldsMax = new HashMap<>();

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_chunks", new Column("date", DateTimeFormat.FORMAT), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT)),
		new Table("server_monitor_chunks_worlds", new Column("date", DateTimeFormat.FORMAT), new Column("world", new VarCharFormat((short) 255)), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT))
	};

	ChunksMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void run() {
		int t = 0;
		for (World world : Bukkit.getWorlds()) {
			int tt = world.getLoadedChunks().length;
			t += tt;
			String name = world.getName();
			if (_byWorldsTotal.containsKey(name)) {
				_byWorldsTotal.put(name, _byWorldsTotal.get(name) + tt);
				if (tt < _byWorldsMin.get(name)) {
					_byWorldsMin.put(name, tt);
				}
				if (tt > _byWorldsMax.get(name)) {
					_byWorldsMax.put(name, tt);
				}
			} else {
				_byWorldsTotal.put(name, tt);
				_byWorldsMin.put(name, tt);
				_byWorldsMax.put(name, tt);
			}
		}
		_i++;
		_total += t;
		if (t < _min) {
			_min = t;
		}
		if (t > _max) {
			_max = t;
		}
	}

	@Override
	public void initializeData() {
		_total = 0;
		_i = 0;
		_min = Integer.MAX_VALUE;
		_max = Integer.MIN_VALUE;
		_byWorldsTotal.clear();
		_byWorldsMin.clear();
		_byWorldsMax.clear();
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("mean", ((float) _total) / _i);
		row.setValue("min", _min);
		row.setValue("max", _max);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		for (String world : _byWorldsTotal.keySet()) {
			row = _tables[1].insert();
			row.setValue("date", date);
			row.setValue("world", '\'' + world + '\'');
			row.setValue("mean", ((float) _byWorldsTotal.get(world)) / _i);
			row.setValue("min", _byWorldsMin.get(world));
			row.setValue("max", _byWorldsMax.get(world));
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}