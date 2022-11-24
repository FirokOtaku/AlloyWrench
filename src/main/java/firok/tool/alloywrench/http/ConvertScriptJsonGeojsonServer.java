package firok.tool.alloywrench.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import firok.tool.alloywrench.bean.Border4;
import firok.tool.alloywrench.bean.ScriptJsonData;
import firok.tool.alloywrench.task.ConvertScriptJsonGeojsonTask;
import firok.tool.alloywrench.util.Jsons;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @deprecated 内部使用
 * */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class ConvertScriptJsonGeojsonServer implements AutoCloseable
{
	private final HttpServer server;
	private boolean isStop = false;
	public ConvertScriptJsonGeojsonServer(int port) throws IOException
	{
		this(new InetSocketAddress(port));
	}
	public ConvertScriptJsonGeojsonServer(InetSocketAddress na) throws IOException
	{
		this.server = HttpServer.create(na, 0);
		this.server.createContext("/convert", this::convert);
		this.server.createContext("/b4", this::border4);
		this.server.start();
	}

	private static Map<String, String> params(HttpExchange exchange)
	{
		var uri = exchange.getRequestURI();
		var query = uri.getQuery();
		var ret = new HashMap<String, String>();
		for(var queryParam : query.split("&"))
		{
			var ws = queryParam.split("=");
			ret.put(ws[0], ws[1]);
		}
		return ret;
	}

	private static void writeJson(HttpExchange exchange, JsonNode node) throws IOException
	{
		var bytes = node.toString().getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders()
				.add("Content-Type", "application/json; charset=UTF-8");
		exchange.sendResponseHeaders(200, bytes.length);
		exchange.getResponseBody().write(bytes);
	}

	private void convert(HttpExchange exchange)
	{
		var om = Jsons.omDecimal();
		try(exchange)
		{
			var params = params(exchange);
			int imageWidth = Integer.parseInt(params.get("width")), imageHeight = Integer.parseInt(params.get("height"));
			BigDecimal left = new BigDecimal(params.get("left")), right = new BigDecimal(params.get("right")),
					top = new BigDecimal(params.get("top")), bottom = new BigDecimal(params.get("bottom"));

			var sjd = om.readValue(exchange.getRequestBody(), ScriptJsonData.class);
			var gjd = ConvertScriptJsonGeojsonTask.convert(
					sjd,
					new Border4(top, bottom, left, right, imageWidth, imageHeight),
					params
			);
			var json = om.valueToTree(gjd);
			writeJson(exchange, json);
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.err.println("Inspark Error: " + any.getLocalizedMessage());
		}
	}

	private void border4(HttpExchange exchange)
	{
		var om = Jsons.omDecimal();
		try(exchange)
		{
			var params = params(exchange);
			int imageWidth = Integer.parseInt(params.get("width")), imageHeight = Integer.parseInt(params.get("height"));
			BigDecimal left = new BigDecimal(params.get("left")), right = new BigDecimal(params.get("right")),
					top = new BigDecimal(params.get("top")), bottom = new BigDecimal(params.get("bottom"));
			int cutFromX = Integer.parseInt(params.get("cutFromX")), cutFromY = Integer.parseInt(params.get("cutFromY")),
					cutToX = Integer.parseInt(params.get("cutToX")), cutToY = Integer.parseInt(params.get("cutToY"));
			var b4 = new Border4(
					top, bottom, left, right,
					imageWidth, imageHeight
			);
			var b42 = b4.cut(cutFromX, cutFromY, cutToX, cutToY);
			var json = om.valueToTree(b42);
			writeJson(exchange, json);
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.err.println("Inspark Error: " + any.getLocalizedMessage());
		}
	}

	@Override
	public synchronized void close()
	{
		if(isStop)
			return;
		this.server.stop(0);
		this.isStop = true;
	}

	@SuppressWarnings("all")
	public static void main(String[] args) throws Exception
	{
		new ConvertScriptJsonGeojsonServer(28117);
		System.out.println("服务器启动");
	}
}
