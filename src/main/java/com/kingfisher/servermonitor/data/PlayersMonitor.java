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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * @author KingFisher
 */
public final class PlayersMonitor extends DataMonitor implements Listener, Runnable {

	private int _i;
	private int _total;
	private int _min;
	private int _max;
	private final Map<String, Integer> _worlds_total = new HashMap<>();
	private final Map<String, Integer> _worlds_min = new HashMap<>();
	private final Map<String, Integer> _worlds_max = new HashMap<>();
	private int _death;
	private final Map<String, Integer> _death_players = new HashMap<>();
	private final Map<String, Integer> _death_worlds = new HashMap<>();

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_players", new Column("date", DateTimeFormat.FORMAT), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT), new Column("death", IntFormat.FORMAT)),
		new Table("server_monitor_players_death", new Column("date", DateTimeFormat.FORMAT), new Column("player", new VarCharFormat((short) 255)), new Column("death", IntFormat.FORMAT)),
		new Table("server_monitor_players_worlds", new Column("date", DateTimeFormat.FORMAT), new Column("world", new VarCharFormat((short) 255)), new Column("mean", FloatFormat.FORMAT), new Column("min", IntFormat.FORMAT), new Column("max", IntFormat.FORMAT), new Column("death", IntFormat.FORMAT))
	};

	public PlayersMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		_death++;
		String player = event.getEntity().getName();
		_death_players.put(player, _death_players.containsKey(player) ? _death_players.get(player) + 1 : 1);
		String world = event.getEntity().getWorld().getName();
		_death_worlds.put(world, _death_worlds.containsKey(world) ? _death_worlds.get(world) + 1 : 1);
	}

	@Override
	public void run() {
		_i++;
		int total = Bukkit.getOnlinePlayers().length;
		_total += total;
		if (total < _min) {
			_min = total;
		}
		if (total > _max) {
			_max = total;
		}
		for (World world : Bukkit.getWorlds()) {
			String w = world.getName();
			int t = world.getPlayers().size();
			if (_worlds_total.containsKey(w)) {
				_worlds_total.put(w, _worlds_total.get(w) + t);
				if (t < _worlds_min.get(w)) {
					_worlds_min.put(w, t);
				}
				if (t > _worlds_max.get(w)) {
					_worlds_max.put(w, t);
				}
			}
		}
	}

	@Override
	public void initializeData() {
		_i = 0;
		_total = 0;
		_min = Integer.MAX_VALUE;
		_max = Integer.MIN_VALUE;
		_worlds_total.clear();
		_worlds_max.clear();
		_worlds_max.clear();
		_death = 0;
		_death_players.clear();
		_death_worlds.clear();
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("mean", ((float) _total) / _i);
		row.setValue("min", _min);
		row.setValue("max", _max);
		row.setValue("death", _death);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		for (Entry<String, Integer> entry : _death_players.entrySet()) {
			row = _tables[1].insert();
			row.setValue("date", date);
			row.setValue("player", '\'' + entry.getKey() + '\'');
			row.setValue("death", entry.getValue());
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
		for (String world : _worlds_total.keySet()) {
			row = _tables[2].insert();
			row.setValue("date", date);
			row.setValue("world", '\'' + world + '\'');
			row.setValue("mean", ((float) _worlds_total.get(world)) / _i);
			row.setValue("min", _worlds_min.get(world));
			row.setValue("max", _worlds_max.get(world));
			row.setValue("death", _death_worlds.get(world));
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}