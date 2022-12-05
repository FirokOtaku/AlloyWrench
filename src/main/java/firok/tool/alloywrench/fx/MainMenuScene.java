package firok.tool.alloywrench.fx;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.function.Consumer;

public class MainMenuScene implements IScene
{
	VBox menuBox;

	HBox lineMarker;
	Button buttonMarker;
	Button buttonDSManager;

	@Getter
	Scene scene;

	public MainMenuScene(Consumer<Event> bus)
	{
		menuBox = new VBox();
		var menuBoxChildren = menuBox.getChildren();

		lineMarker = new HBox();
//		buttonMarker = new Button("启动图片标注工具");
//		buttonMarker.setFont(fontUI);
//		buttonMarker.setPadding(paddingBtn);
//		buttonMarker.setOnAction(event -> bus.accept(new SwitchSceneEvent(Scenes.MARKER)));
		buttonDSManager = new Button("数据集管理工具");
		buttonDSManager.setFont(fontUI);
		buttonDSManager.setPadding(paddingBtn);
		buttonDSManager.setOnAction(event -> bus.accept(new SwitchSceneEvent(Scenes.DS_MGR)));
		lineMarker.getChildren().addAll(buttonMarker);
		menuBoxChildren.add(lineMarker);

		scene = new Scene(menuBox);
	}

	@Override
	public void close() throws Exception
	{
		;
	}
}
