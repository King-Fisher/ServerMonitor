package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.EnumFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import com.kingfisher.servermonitor.mysql.formats.VarCharFormat;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author KingFisher
 */
public final class BlocksMonitor extends DataMonitor implements Listener {

	private static final Set<Material> VALID_MATERIALS = EnumSet.noneOf(Material.class);

	static {
		for (Material material : Material.values()) {
			if (material.isBlock()) {
				VALID_MATERIALS.add(material);
			}
			VALID_MATERIALS.remove(Material.AIR);
		}
	}

	private int _broken;
	private final Map<String, Integer> _broken_players = new HashMap<>();
	private final Map<Material, Integer> _broken_types = new EnumMap<>(Material.class);
	private final Map<String, Integer> _broken_worlds = new HashMap<>();
	private int _placed;
	private final Map<String, Integer> _placed_players = new HashMap<>();
	private final Map<Material, Integer> _placed_types = new EnumMap<>(Material.class);
	private final Map<String, Integer> _placed_worlds = new HashMap<>();

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_blocks", new Column("date", DateTimeFormat.FORMAT), new Column("broken", IntFormat.FORMAT), new Column("placed", IntFormat.FORMAT)),
		new Table("server_monitor_blocks_players", new Column("date", DateTimeFormat.FORMAT), new Column("player", new VarCharFormat((short) 255)), new Column("broken", IntFormat.FORMAT), new Column("placed", IntFormat.FORMAT)),
		new Table("server_monitor_blocks_types", new Column("date", DateTimeFormat.FORMAT), new Column("type", new EnumFormat(VALID_MATERIALS)), new Column("broken", IntFormat.FORMAT), new Column("placed", IntFormat.FORMAT)),
		new Table("server_monitor_blocks_worlds", new Column("date", DateTimeFormat.FORMAT), new Column("world", new VarCharFormat((short) 255)), new Column("broken", IntFormat.FORMAT), new Column("placed", IntFormat.FORMAT))
	};

	BlocksMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		_broken++;
		String player = getPlayer(event.getPlayer());
		_broken_players.put(player, _broken_players.containsKey(player) ? _broken_players.get(player) + 1 : 1);
		Material type = event.getBlock().getType();
		_broken_types.put(type, _broken_types.get(type) + 1);
		String world = event.getBlock().getWorld().getName();
		_broken_worlds.put(world, _broken_worlds.containsKey(world) ? _broken_worlds.get(world) + 1 : 1);
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		_placed++;
		String player = getPlayer(event.getPlayer());
		_placed_players.put(player, _placed_players.containsKey(player) ? _placed_players.get(player) + 1 : 1);
		Material type = event.getBlock().getType();
		_placed_types.put(type, _placed_types.get(type) + 1);
		String world = event.getBlock().getWorld().getName();
		_placed_worlds.put(world, _placed_worlds.containsKey(world) ? _placed_worlds.get(world) + 1 : 1);
	}

	@Override
	public void initializeData() {
		_broken = 0;
		_broken_players.clear();
		_broken_worlds.clear();
		_placed = 0;
		_placed_players.clear();
		_placed_worlds.clear();
		for (Material material : VALID_MATERIALS) {
			_broken_types.put(material, 0);
			_placed_types.put(material, 0);
		}
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("broken", _broken);
		row.setValue("placed", _placed);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		Set<String> players = new HashSet<>(_broken_players.keySet());
		for (String player : _placed_players.keySet()) {
			if (!players.contains(player)) {
				players.add(player);
			}
		}
		for (String player : players) {
			row = _tables[1].insert();
			row.setValue("date", date);
			row.setValue("player", '\'' + player + '\'');
			row.setValue("broken", _broken_players.containsKey(player) ? _broken_players.get(player) : 0);
			row.setValue("placed", _placed_players.containsKey(player) ? _placed_players.get(player) : 0);
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
		for (Material material : VALID_MATERIALS) {
			row = _tables[2].insert();
			row.setValue("type", '\'' + material.toString() + '\'');
			row.setValue("broken", _broken_types.get(material));
			row.setValue("placed", _placed_types.get(material));
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
		Set<String> worlds = new HashSet<>(_broken_worlds.keySet());
		for (String world : _placed_worlds.keySet()) {
			if (!players.contains(world)) {
				players.add(world);
			}
		}
		for (String world : worlds) {
			row = _tables[3].insert();
			row.setValue("date", date);
			row.setValue("world", '\'' + world + '\'');
			row.setValue("broken", _broken_worlds.containsKey(world) ? _broken_worlds.get(world) : 0);
			row.setValue("placed", _placed_worlds.containsKey(world) ? _placed_worlds.get(world) : 0);
			getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		}
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}