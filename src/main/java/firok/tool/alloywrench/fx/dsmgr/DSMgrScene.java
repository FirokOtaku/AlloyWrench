package firok.tool.alloywrench.fx.dsmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import firok.tool.alloywrench.AlloyWrench;
import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.fx.Event;
import firok.tool.alloywrench.fx.IScene;
import firok.tool.alloywrench.util.Jsons;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.SneakyThrows;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class DSMgrScene implements IScene
{
	private final Consumer<Event> bus;
	@Getter
	private final Scene scene;


	@SneakyThrows
	public DSMgrScene(Consumer<Event> bus)
	{
		this.bus = bus;

		var loader = new FXMLLoader(AlloyWrench.class.getResource("/firok/tool/alloy-wrench/ds-mgr.fxml"));

		this.scene = new Scene(loader.load(), 400, 300);
	}
}
