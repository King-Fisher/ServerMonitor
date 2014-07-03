package com.kingfisher.servermonitor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author KingFisher
 */
public final class DataViewerServer implements Closeable {

	private final ServerMonitor _serverMonitor;
	private final ServerSocket _socket;
	private final DataViewerServerThread _thread = new DataViewerServerThread();
	private final String _password;

	public DataViewerServer(ServerMonitor serverMonitor) throws IOException {
		_serverMonitor = serverMonitor;
		_socket = new ServerSocket(_serverMonitor.getConfiguration().getDataViewerPort());
		_password = _serverMonitor.getConfiguration().getDataViewerPassword();
		_thread.start();
	}

	@Override
	public void close() throws IOException {
		_thread.interrupt();
		_socket.close();
	}

	public boolean isOpen() {
		return _thread.isAlive() && !_socket.isClosed();
	}

	public boolean isClosed() {
		return !_thread.isAlive() || _socket.isClosed();
	}

	private final class DataViewerServerThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					new DataViewerSessionThread(_socket.accept()).start();
				} catch (IOException ex) {
					if (!isInterrupted()) {
						_serverMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
					}
				}
			}
		}
	}

	private final class DataViewerSessionThread extends Thread {

		private final Socket _socket;

		public DataViewerSessionThread(Socket socket) {
			_socket = socket;
		}

		private Map<String, String> parseRequest(String request) {
			if ((request != null) && (request.startsWith("GET /"))) {
				String url = request.substring("GET /".length(), request.lastIndexOf(" HTTP/"));
				Map<String, String> r = new HashMap<>();
				if (url.contains("?")) {
					String[] s = url.split("\\?");
					r.put("", s[0]);
					String[] args = s[1].split("&");
					for (String arg : args) {
						String[] kv = arg.split("=");
						try {
							r.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
						} catch (UnsupportedEncodingException ex) {
							_serverMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
						}
					}
				} else {
					r.put("", url);
				}
				return r;
			} else {
				return null;
			}
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				Map<String, String> request = parseRequest(reader.readLine());
				if (request != null) {
					String link = request.get("");
					switch (link) {
						case "favicon.ico":
							sendResource("/favicon.ico");
							break;
						case "password.php":
							if (request.containsKey("password") && request.get("password").equals(_password)) {
								StringBuilder function = new StringBuilder();
								StringBuilder body = new StringBuilder();
								try {
									if (_serverMonitor.getConfiguration().getMonitorBlocks()) {
										function.append("            new google.visualization.LineChart(document.getElementById('blocks_broken')).draw(new google.visualization.arrayToDataTable([['Date', 'Broken']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_blocks");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("broken")).append(']');
										}
										function.append("]), {'title':'Broken blocks', 'height':440, 'colors':['red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"blocks_broken\"></p>\n");
										function.append("            new google.visualization.LineChart(document.getElementById('blocks_placed')).draw(new google.visualization.arrayToDataTable([['Date', 'Placed']");
										result.beforeFirst();
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("placed")).append(']');
										}
										function.append("]), {'title':'Placed blocks', 'height':440, 'colors':['green'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"blocks_placed\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorChunks()) {
										function.append("            new google.visualization.LineChart(document.getElementById('chunks')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_chunks");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Loaded chunks', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"chunks\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorClassLoading()) {
										function.append("            new google.visualization.LineChart(document.getElementById('class_loading')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_class_loading");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("loaded_mean")).append(", ").append(result.getInt("loaded_min")).append(", ").append(result.getInt("loaded_max")).append(']');
										}
										function.append("]), {'title':'Loaded classes', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"class_loading\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorEntities()) {
										function.append("            new google.visualization.LineChart(document.getElementById('entities')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_entities");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Entities', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"entities\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorExperience()) {
										//
									}
									if (_serverMonitor.getConfiguration().getMonitorGarbageCollector()) {
										function.append("            new google.visualization.LineChart(document.getElementById('garbage_collector')).draw(new google.visualization.arrayToDataTable([['Date', 'Collected objects']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_garbage_collector");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getLong("collected")).append(']');
										}
										function.append("]), {'title':'Garbage collector', 'height':440, 'colors':['blue'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"garbage_collector\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorItems()) {
										//
									}
									if (_serverMonitor.getConfiguration().getMonitorLivingEntities()) {
										//
									}
									if (_serverMonitor.getConfiguration().getMonitorMemory()) {
										function.append("            new google.visualization.LineChart(document.getElementById('memory')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_memory");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("total_mean")).append(", ").append(result.getInt("total_min")).append(", ").append(result.getInt("total_max")).append(']');
										}
										function.append("]), {'title':'Used memory', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"memory\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorOperatingSystem()) {
										function.append("            new google.visualization.LineChart(document.getElementById('operating_system')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_operating_system");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("load_mean")).append(", ").append(result.getInt("load_min")).append(", ").append(result.getInt("load_max")).append(']');
										}
										function.append("]), {'title':'System load average', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"operating_system\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorPlayers()) {
										function.append("            new google.visualization.LineChart(document.getElementById('players')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_players");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Players', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"players\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorRedstone()) {
										function.append("            new google.visualization.LineChart(document.getElementById('redstone')).draw(new google.visualization.arrayToDataTable([['Date', 'Redstone use']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_redstone");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("redstone")).append(']');
										}
										function.append("]), {'title':'Redstone use', 'height':440, 'colors':['red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"redstone\"></p>\n");
										function.append("            new google.visualization.LineChart(document.getElementById('pistons')).draw(new google.visualization.arrayToDataTable([['Date', 'Pistons use']");
										result.beforeFirst();
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("pistons")).append(']');
										}
										function.append("]), {'title':'Pistons use', 'height':440, 'colors':['red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"pistons\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorTPS()) {
										function.append("            new google.visualization.LineChart(document.getElementById('tps')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_tps");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Ticks per second', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"tps\"></p>\n");
									}
									if (_serverMonitor.getConfiguration().getMonitorThreads()) {
										function.append("            new google.visualization.LineChart(document.getElementById('threads')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_threads");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("all_mean")).append(", ").append(result.getInt("all_min")).append(", ").append(result.getInt("all_max")).append(']');
										}
										function.append("]), {'title':'Threads', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{axis:'horizontal'}});\n");
										body.append("        <p id=\"threads\"></p>\n");
									}
								} catch (SQLException ex) {
									_serverMonitor.runtimeException("An unexpected error related to the database.", ex);
								}
								sendString(getResourceAsString("/access.html").replace("//%FUNCTION%", function.toString()).replace("<!-- %BODY% -->", body.toString()));
							} else {
								sendResource("/invalid.html");
							}	break;
						default:
							sendResource("/password.html");
							break;
					}
				}
			} catch (IOException ex) {
				_serverMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
			} finally {
				try {
					_socket.close();
				} catch (IOException ex) {
					_serverMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
				}
			}
		}

		private void sendResource(String path) throws IOException {
			sendString(getResourceAsString(path));
		}

		private void sendString(String toSend) throws IOException {
			OutputStream writer = _socket.getOutputStream();
			writer.write(toSend.getBytes());
			writer.flush();
		}

		private String getResourceAsString(String path) throws IOException {
			InputStream resource = getClass().getResourceAsStream(path);
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = resource.read()) != -1) {
				builder.append((char) c);
			}
			return builder.toString();
		}
	}
}