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

		private String getP(String id) {
			return "        <p id=\"" + id + "\" style=\"border: solid 1px #000000\"></p>\n";
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
									if (_serverMonitor.getConfiguration().getMonitorPlayers()) {
										function.append("            new google.visualization.LineChart(document.getElementById('players')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_players");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Players monitoring (player count)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("players"));
									}
									if (_serverMonitor.getConfiguration().getMonitorEntities()) {
										function.append("            new google.visualization.LineChart(document.getElementById('entities')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_entities");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Entities monitoring (entity count)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("entities"));
									}
									if (_serverMonitor.getConfiguration().getMonitorBlocks()) {
										function.append("            new google.visualization.LineChart(document.getElementById('blocks')).draw(new google.visualization.arrayToDataTable([['Date', 'Broken blocks', 'Placed blocks']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_blocks");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("broken")).append(", ").append(result.getInt("placed")).append(']');
										}
										function.append("]), {'title':'Blocks monitoring', 'height':440, 'colors':['red', 'green'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("blocks"));
									}
									if (_serverMonitor.getConfiguration().getMonitorChunks()) {
										function.append("            new google.visualization.LineChart(document.getElementById('chunks')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_chunks");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
										}
										function.append("]), {'title':'Chunks monitoring (loaded chunks)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("chunks"));
									}
									if (_serverMonitor.getConfiguration().getMonitorRedstone()) {
										function.append("            new google.visualization.LineChart(document.getElementById('redstone')).draw(new google.visualization.arrayToDataTable([['Date', 'Redstone use', 'Pistons use']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_redstone");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("redstone")).append(", ").append(result.getInt("pistons")).append(']');
										}
										function.append("]), {'title':'Redstone monitoring', 'height':440, 'colors':['red', 'green'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("redstone"));
									}
									if (_serverMonitor.getConfiguration().getMonitorLivingEntities()) {
										//
									}
									if (_serverMonitor.getConfiguration().getMonitorItems()) {
										//
									}
									if (_serverMonitor.getConfiguration().getMonitorExperience()) {
										//
									}
									if (_serverMonitor.getConfiguration().getMonitorTPS()) {
										function.append("            new google.visualization.LineChart(document.getElementById('tps')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_tps");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getFloat("min")).append(", ").append(result.getFloat("max")).append(']');
										}
										function.append("]), {'title':'TPS monitoring (Ticks per second)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("tps"));
									}
									if (_serverMonitor.getConfiguration().getMonitorMemory()) {
										function.append("            new google.visualization.LineChart(document.getElementById('memory')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_memory");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("total_mean")).append(", ").append(result.getFloat("total_min")).append(", ").append(result.getFloat("total_max")).append(']');
										}
										function.append("]), {'title':'Memory monitoring (total used memory)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("memory"));
									}
									if (_serverMonitor.getConfiguration().getMonitorGarbageCollector()) {
										function.append("            new google.visualization.LineChart(document.getElementById('garbage_collector')).draw(new google.visualization.arrayToDataTable([['Date', 'Collection count']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_garbage_collector");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getLong("collected")).append(']');
										}
										function.append("]), {'title':'Garbage collector monitoring', 'height':440, 'colors':['blue'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("garbage_collector"));
									}
									if (_serverMonitor.getConfiguration().getMonitorClassLoading()) {
										function.append("            new google.visualization.LineChart(document.getElementById('class_loading')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_class_loading");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("loaded_mean")).append(", ").append(result.getInt("loaded_min")).append(", ").append(result.getInt("loaded_max")).append(']');
										}
										function.append("]), {'title':'Class loading monitoring (loaded classes)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("class_loading"));
									}
									if (_serverMonitor.getConfiguration().getMonitorThreads()) {
										function.append("            new google.visualization.LineChart(document.getElementById('threads')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_threads");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("all_mean")).append(", ").append(result.getInt("all_min")).append(", ").append(result.getInt("all_max")).append(']');
										}
										function.append("]), {'title':'Threads monitoring (thread count)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("threads"));
									}
									if (_serverMonitor.getConfiguration().getMonitorOperatingSystem()) {
										function.append("            new google.visualization.LineChart(document.getElementById('operating_system')).draw(new google.visualization.arrayToDataTable([['Date', 'Mean', 'Minimum', 'Maximum']");
										ResultSet result = _serverMonitor.getSQLBridge().executeQuery("SELECT * FROM server_monitor_operating_system");
										while (result.next()) {
											function.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("load_mean")).append(", ").append(result.getFloat("load_min")).append(", ").append(result.getFloat("load_max")).append(']');
										}
										function.append("]), {'title':'Operating system monitoring (system load average)', 'height':440, 'colors':['green', 'blue', 'red'], 'explorer':{keepInBounds:true, maxZoomIn:0.125}, 'vAxis':{minValue:0}});\n");
										body.append(getP("operating_system"));
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