package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.DotaLabel;
import firok.tool.alloywrench.bean.YoloLabel;
import firok.tool.alloywrench.util.DotaReader;
import firok.tool.alloywrench.util.Files;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static firok.tool.alloywrench.task.ConvertDotaYoloTask._max;
import static firok.tool.alloywrench.task.ConvertDotaYoloTask._min;

public class RendererDotaTask extends FxBase
{
	public static void execute()
	{
		launch();
	}

	Text textOffset;
	Text textKeymap;
	Text textUIStatus;
	Text textImageInfo;
	Text textLabelInfo;
	Pane pane;
	Scene scene;
	Canvas canvas;
	List<BoxEntry> listBox = new ArrayList<>();
	record BoxEntry(Node poly, Text text, Object label, DecimalPoint min, DecimalPoint max) { }

	double preX = 0, preY = 0;
	int offsetX = 0, offsetY = 0;
	void relocateImage(int ox, int oy)
	{
		offsetX = ox;
		offsetY = oy;
		textOffset.setText(offsetX + ", " + offsetY);
		canvas.setLayoutX(offsetX);
		canvas.setLayoutY(offsetY);
		for(var box : listBox)
		{
			box.text.setLayoutX(offsetX + box.min.x.doubleValue());
			box.text.setLayoutY(offsetY + box.min.y.doubleValue());
			box.poly.setLayoutX(offsetX);
			box.poly.setLayoutY(offsetY);
		}
	}

	record ImageInfo(String url, Image image) { }
	ImageInfo ii = null;

	interface LabelInfo { void print(Canvas canvas); }
	class DotaLabelInfo implements LabelInfo
	{
		List<DotaLabel> labels;

		@Override
		public void print(Canvas canvas)
		{
			;
		}
	}
	class YoloLabelInfo implements LabelInfo
	{
		List<YoloLabel> labels;
		Map<Integer, String> mapping;

		@Override
		public void print(Canvas canvas)
		{
			;
		}
	}
	LabelInfo li = null;
	boolean isDisplayLabelInfo = true;

	boolean isDisplayUI = true;

	void clickLoadImage()
	{
		try
		{
			var file = showFileChooser("Select image", IMAGES, ALL);
			var url = toURI(file);
			if(url == null) return;

			var image = new Image(url);
			var exception = image.getException();
			if(exception != null) throw exception;

			ii = new ImageInfo(url, image);
			textImageInfo.setText("Image: [%s]; [%d × %d]".formatted(url, (int) image.getWidth(), (int) image.getHeight()));

			var gra = canvas.getGraphicsContext2D();
			gra.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

			canvas.setWidth(image.getWidth());
			canvas.setHeight(image.getHeight());
			gra.drawImage(image, 0, 0);
		}
		catch (Exception any)
		{
			showError(any);
		}
	}
	void clickLoadDotaLabels()
	{
		try
		{
			var file = showFileChooser("Select text", TEXTS, ALL);
			if(file == null) return;

			var children = pane.getChildren();

			for(var box : listBox)
			{
				children.remove(box.poly);
				children.remove(box.text);
			}
			listBox.clear();

			var raw = Files.read(file);
			var listLabel = DotaReader.read(raw, 0, 0);
			var info = new DotaLabelInfo();
			info.labels = listLabel;

			for(var label : listLabel)
			{
				DecimalPoint pt1 = label.pt1(), pt2 = label.pt2(),
						pt3 = label.pt3(), pt4 = label.pt4();

				var poly = new Polygon(
						pt1.x.doubleValue(),
						pt1.y.doubleValue(),
						pt2.x.doubleValue(),
						pt2.y.doubleValue(),
						pt3.x.doubleValue(),
						pt3.y.doubleValue(),
						pt4.x.doubleValue(),
						pt4.y.doubleValue()
				);
				var text = new Text(label.catalog());
				children.add(poly);
				children.add(text);

				DecimalPoint min = new DecimalPoint(_min, _min), max = new DecimalPoint(_max, _max);
				DecimalPoint.range(min, max, pt1, pt2, pt3, pt4);
				var box = new BoxEntry(poly, text, label, min, max);
				listBox.add(box);
			}
			relocateImage(offsetX, offsetY);
			li = info;
			textLabelInfo.setText("Labels: [%s]; [%d]".formatted(file.getAbsolutePath(), listLabel.size()));
		}
		catch (Exception any)
		{
			showError(any);
		}
	}
	void clickLoadYoloLabels()
	{
		try
		{
			if(true)
			throw new UnsupportedOperationException("not implemented");

			var file = showFileChooser("Select text", TEXTS, ALL);
			if(file == null) return;

			var raw = Files.read(file);
//			var listLabel = YoloReader.read(raw);
//			var info = new DotaLabelInfo();
//			info.labels = listLabel;
//			li = info;
		}
		catch (Exception any)
		{
			showError(any);
		}
	}
	void clickSwitchLabeledBoxesDisplay()
	{
		isDisplayLabelInfo = !isDisplayLabelInfo;
		textUIStatus.setText(genUIStatus());
		for(var box : listBox)
		{
			box.poly.setVisible(isDisplayLabelInfo);
			box.text.setVisible(isDisplayLabelInfo);
		}
	}
	void clickSwitchUIDisplay()
	{
		isDisplayUI = !isDisplayUI;
		textUIStatus.setText(genUIStatus());
		textUIStatus.setVisible(isDisplayUI);
		textKeymap.setVisible(isDisplayUI);
		textLabelInfo.setVisible(isDisplayUI);
		textImageInfo.setVisible(isDisplayUI);
		textOffset.setVisible(isDisplayUI);
	}
	void clickResetImageOffset()
	{
		relocateImage(0, 0);
	}
	void clickClearLoadedResources()
	{
		li = null;
		ii = null;
		textImageInfo.setText("Image: none");
		textLabelInfo.setText("Labels: none");
	}
	String genUIStatus()
	{
		StringBuilder ret = new StringBuilder();
		ret.append("UI: ").append(isDisplayUI ? "on" : "off").append('\n');
		ret.append("Labels: ").append(isDisplayLabelInfo ? "on" : "off");
		return ret.toString();
	}

	@Override
	void postStart()
	{
		canvas = new Canvas();
		textKeymap = new Text("""
				[P] - Load image
				[1] - Load DOTA labels
				[2] - Load YOLO labels
				[L] - Switch labeled boxes display
				[U] - Switch UI display
				[R] - Reset image offset
				[C] - Clear loaded resources
				""");
		textKeymap.setLayoutX(0);
		textKeymap.setLayoutY(15);
		textOffset = new Text("0, 0");
		textOffset.setLayoutX(0);
		textOffset.setLayoutY(130);
		textUIStatus = new Text(genUIStatus());
		textUIStatus.setLayoutX(220);
		textUIStatus.setLayoutY(15);
		textImageInfo = new Text("Image: none");
		textImageInfo.setLayoutX(0);
		textImageInfo.setLayoutY(275);
		textLabelInfo = new Text("Labels: none");
		textLabelInfo.setLayoutX(0);
		textLabelInfo.setLayoutY(288);


		pane = new Pane();
		var children = pane.getChildren();
		children.add(canvas);
		children.add(textKeymap);
		children.add(textUIStatus);
		children.add(textLabelInfo);
		children.add(textImageInfo);
		children.add(textOffset);

		stage.setTitle("Alloy Wrench - Renderer DOTA");
		stage.setMinWidth(400);
		stage.setMinHeight(300);
		scene = new Scene(pane);

		scene.setOnKeyTyped(event -> {
			var chRaw = event.getCharacter();
			var ch = chRaw.length() == 1 ? Character.toLowerCase(chRaw.charAt(0)) : 0;
			switch (ch)
			{
				case 'p' -> clickLoadImage();
				case '1' -> clickLoadDotaLabels();
				case '2' -> clickLoadYoloLabels();
				case 'l' -> clickSwitchLabeledBoxesDisplay();
				case 'u' -> clickSwitchUIDisplay();
				case 'r' -> clickResetImageOffset();
				case 'c' -> clickClearLoadedResources();
			}
		});
		var onResize = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				var width = (int) scene.getWidth();
				var height = (int) scene.getHeight();
				textUIStatus.setLayoutX(width - 80);
				textUIStatus.setLayoutY(15);
				textImageInfo.setLayoutY(height - 25);
				textLabelInfo.setLayoutY(height - 12);
			}
		};
		scene.widthProperty().addListener(onResize);
		scene.heightProperty().addListener(onResize);
		scene.setOnMousePressed(event -> {
			preX = event.getX();
			preY = event.getY();
		});
		scene.setOnMouseReleased(event -> {
			var curX = event.getX();
			var curY = event.getY();
			relocateImage(offsetX + (int)(curX - preX), offsetY + (int)(curY - preY));
		});
		stage.setScene(scene);
	}
}
