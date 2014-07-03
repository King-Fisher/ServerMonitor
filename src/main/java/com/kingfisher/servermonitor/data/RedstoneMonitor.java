package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import com.kingfisher.servermonitor.mysql.formats.VarCharFormat;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 *
 * @author KingFisher
 */
public final class RedstoneMonitor extends DataMonitor implements Listener {

	private int _redstone;
	private final Map<String, Integer> _redstone_worlds = new HashMap<>();
	private int _pistons;
	private final Map<String, Integer> _pistons_worlds = new HashMap<>();

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_redstone", new Column("date", DateTimeFormat.FORMAT), new Column("redstone", IntFormat.FORMAT), new Column("pistons", IntFormat.FORMAT)),
		new Table("server_monitor_redstone_worlds", new Column("date", DateTimeFormat.FORMAT), new Column("world", new VarCharFormat((short) 255)), new Column("redstone", IntFormat.FORMAT), new Column("pistons", IntFormat.FORMAT))
	};

	public RedstoneMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@EventHandler
	public void onRedstone(BlockRedstoneEvent event) {
		_redstone++;
		String world = event.getBlock().getWorld().getName();
		_redstone_worlds.put(world, _redstone_worlds.containsKey(world) ? _redstone_worlds.get(world) + 1 : 1);
	}

	@EventHandler
	public void onExtend(BlockPistonExtendEvent event) {
		_pistons++;
		String world = event.getBlock().getWorld().getName();
		_pistons_worlds.put(world, _pistons_worlds.containsKey(world) ? _pistons_worlds.get(world) + 1 : 1);
	}

	@EventHandler
	public void onRetract(BlockPistonRetractEvent event) {
		_pistons++;
		String world = event.getBlock().getWorld().getName();
		_pistons_worlds.put(world, _pistons_worlds.containsKey(world) ? _pistons_worlds.get(world) + 1 : 1);
	}

	@Override
	public void initializeData() {
		_redstone = 0;
		_redstone_worlds.clear();
		_pistons = 0;
		_pistons_worlds.clear();
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("redstone", _redstone);
		row.setValue("pistons", _pistons);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		for (String world : _redstone_worlds.keySet()) {
			row = _tables[1].insert();
			row.setValue("date", date);
			row.setValue("world", '\'' + world + '\'');
			row.setValue("redstone", _redstone_worlds.get(world));
			row.setValue("pistons", _pistons_worlds.containsKey(world) ? _pistons_worlds.get(world) : 0);
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}