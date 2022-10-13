package firok.tool.alloywrench.task;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;

public abstract class FxBase extends Application
{
	protected Stage stage;
	@Override
	public final void start(Stage primaryStage) throws Exception
	{
		this.stage = primaryStage;
		postStart();
		stage.show();
	}

	protected void showError(Exception exception)
	{
		exception.printStackTrace(System.err);
		var alert = new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.CLOSE);
		alert.setTitle("Error");
		alert.setWidth(300);
		alert.setHeight(200);
		alert.showAndWait();
	}
	protected static final FileChooser.ExtensionFilter ALL
			= new FileChooser.ExtensionFilter("All files", "*.*");
	protected static final FileChooser.ExtensionFilter IMAGES
			= new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.img");
	protected static final FileChooser.ExtensionFilter TEXTS
			= new FileChooser.ExtensionFilter("Text files", "*.txt");

	protected final File showFileChooser(String title, FileChooser.ExtensionFilter... filters) throws MalformedURLException
	{
		var fc = new FileChooser();
		fc.setTitle(title);
		fc.getExtensionFilters().addAll(filters == null ? new FileChooser.ExtensionFilter[] { ALL } : filters);
		return fc.showOpenDialog(stage);
	}
	protected final String toURI(File file) throws MalformedURLException
	{
		return file == null ? null : file.toURI().toURL().toString();
	}

	abstract void postStart();
}
