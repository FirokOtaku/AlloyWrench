package firok.tool.alloywrench.fx;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public interface IScene extends AutoCloseable
{
	Font fontUI = Font.font("Microsoft Yahei", FontWeight.NORMAL, FontPosture.REGULAR, 16);
	Insets paddingBtn = new Insets(8, 16, 8, 16);

	Scene getScene();

	default void close() throws Exception { }
}
