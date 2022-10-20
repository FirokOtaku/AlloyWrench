package firok.tool.alloywrench.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MarkerScene implements IScene
{
	private final Consumer<Event> bus;

	static FontIcon icon(String key, Color fill)
	{
		var ret = new FontIcon(key);
		ret.setIconColor(fill);
		return ret;
	}
	static FontIcon icon(String key)
	{
		return icon(key, Color.BLACK);
	}

	public BooleanProperty isShowRightList = new SimpleBooleanProperty(true);
	public IntegerProperty viewportScale = new SimpleIntegerProperty(100);
	public void zoomIn()
	{
		int old = viewportScale.get();
		if(old >= 500) return;
		viewportScale.setValue(old + 20);
	}
	public void zoomOut()
	{
		int old = viewportScale.get();
		if(old < 20) return;
		viewportScale.setValue(old - 10);
	}

	private MenuBar createMenuBar()
	{
		var menuFile = new Menu("文件", icon("mdi2f-file-outline"));
		var menuFile_openImage = new MenuItem("加载图片", icon("mdi2f-file-image-outline"));
		menuFile_openImage.setOnAction(event -> {
			System.out.println("加载图片");
			var image = new Image("C:\\Users\\a3517\\Pictures\\80567870_p0.jpg");
			iv.setImage(image);
		});
		var menuFile_openLabel = new MenuItem("加载标签", icon("mdi2f-file-document-outline"));
		var menuFile_openJson = new MenuItem("加载综合 JSON", icon("mdi2f-file-star-outline"));
		var menuFile_saveJson = new MenuItem("保存综合 JSON");
		menuFile.getItems().addAll(
				menuFile_openImage,
				menuFile_openLabel,
				menuFile_openJson,
				new SeparatorMenuItem(),
				menuFile_saveJson
		);

		var menuView = new Menu("视图", icon("mdi2e-eye-outline"));
		var menuView_reset = new MenuItem("重置所有", icon("mdi2b-backup-restore"));
		menuView_reset.setOnAction(event -> {
			viewportScale.setValue(100);
			mid.setHvalue(0);
			mid.setVvalue(0);
		});
		var menuView_backStart = new MenuItem("回到左上", icon("mdi2a-arrow-top-left"));
		var menuView_zoomIn = new MenuItem("放大", icon("mdi2m-magnify-plus-outline"));
		menuView_zoomIn.setOnAction(event -> zoomIn());
		var menuView_zoomOut = new MenuItem("缩小", icon("mdi2m-magnify-minus-outline"));
		menuView_zoomOut.setOnAction(event -> zoomOut());
		var menuView_switchRightList = new MenuItem("显示/隐藏右侧列表", icon("mdi2p-page-layout-sidebar-right"));
		menuView_switchRightList.setOnAction(event -> {
			var isShow = isShowRightList.get();
			isShowRightList.setValue(!isShow);
		});
		menuView.getItems().addAll(
				menuView_reset,
				menuView_backStart,
				menuView_zoomIn,
				menuView_zoomOut,
				new SeparatorMenuItem(),
				menuView_switchRightList
		);

		var menuLabel = new Menu("标签", icon("mdi2l-label-outline"));
		var menuLabel_editSelected = new MenuItem("编辑选中", icon("mdi2p-pencil-outline"));
		var menuLabel_deleteSelected = new MenuItem("删除选中", icon("mdi2d-delete-outline"));
		var menuLabel_rotateInner = new MenuItem("旋转起始点", icon("mdi2r-reload"));
		menuLabel.getItems().addAll(
				menuLabel_editSelected,
				menuLabel_deleteSelected,
				new SeparatorMenuItem(),
				menuLabel_rotateInner
		);

		var menubar = new MenuBar();
		menubar.getMenus().addAll(menuFile, menuView, menuLabel);
		return menubar;
	}
	Text textImageWidth;
	Text textImageHeight;
	Text textViewportScale;
	Text textViewportX;
	Text textViewportY;
	private BorderPane createToolBar()
	{
		var boxToolbar = new BorderPane();

		var toolbarTask = new ToolBar();
		var textTask = new Text("无任务");
		var progressTask = new ProgressBar(0.5);
		toolbarTask.getItems().addAll(
				textTask, progressTask
		);
		toolbarTask.setMinHeight(24);
		toolbarTask.setMaxHeight(24);

		var toolbarEmpty = new ToolBar();
		toolbarEmpty.setMinHeight(24);
		toolbarEmpty.setMaxHeight(24);
		toolbarEmpty.setMinWidth(0);

		var toolbarImageInfo = new ToolBar();
		// 标签信息
		var textLabelCount = new Text("0");
		// 图像信息
		textImageWidth = new Text("0");
		var textImageCross = new Text("×");
		textImageHeight = new Text("0");
		// 视角信息
		textViewportScale = new Text("100%");
		textViewportX = new Text("0");
		var textViewportCross = new Text(",");
		textViewportY = new Text("0");
		toolbarImageInfo.getItems().addAll(
				icon("mdi2l-label-multiple-outline", Color.DIMGRAY),
				textLabelCount,
				icon("mdi2v-vector-rectangle", Color.DIMGRAY),
				textImageWidth, textImageCross, textImageHeight,
				icon("mdi2e-eye-outline", Color.DIMGRAY),
				textViewportX, textViewportCross, textViewportY,
				icon("mdi2m-magnify", Color.DIMGRAY),
				textViewportScale
		);
		toolbarImageInfo.setMinHeight(24);
		toolbarImageInfo.setMaxHeight(24);

		boxToolbar.setLeft(toolbarTask);
		boxToolbar.setCenter(toolbarEmpty);
		boxToolbar.setRight(toolbarImageInfo);

		return boxToolbar;
	}
	private BorderPane createRightList()
	{
		var right = new BorderPane();
		right.setMinWidth(150);
		right.visibleProperty().bind(isShowRightList);
		isShowRightList.addListener(evnet -> {
			int width = isShowRightList.get() ? 150 : 0;
			right.setMinWidth(width);
			right.setMaxWidth(width);
		});

		var list = new ListView<MarkerPoly>();
		list.setEditable(false);
		list.setCellFactory(param -> new MarkerPolyCell());
//		IntStream.range(0, 100).forEach(list.getItems()::add);
		list.setMinHeight(50);
		right.setCenter(list);

		return right;
	}
	private ImageView iv;
	private void ivFit()
	{
		if(iv == null) return;

		var image = iv.getImage();
		if(image == null) return;

		var width = image.getWidth();
		var height = image.getHeight();
		var scale = viewportScale.get();
		iv.setFitWidth(width * scale / 100);
		iv.setFitHeight(height * scale / 100);
	}
	private ScrollPane createMidImage()
	{
		var pane = new ScrollPane();

		iv = new ImageView();
		iv.setPreserveRatio(true);
		var onResize = (ChangeListener<Number>) (source, oldValue, newValue) -> ivFit();
		pane.widthProperty().addListener(onResize);
		pane.heightProperty().addListener(onResize);
		viewportScale.addListener(onResize);

		pane.setContent(iv);

		return pane;
	}
	private ScrollPane mid;
	public MarkerScene(Consumer<Event> bus)
	{
		this.bus = bus;

		var pane = new BorderPane();

		var menubar = createMenuBar();
		pane.setTop(menubar);

		mid = createMidImage();
		pane.setCenter(mid);

		var right = createRightList();
		pane.setRight(right);

		var boxToolbar = createToolBar();
		pane.setBottom(boxToolbar);

		iv.imageProperty().addListener((source, valueOld, valueNew) -> {
			var image = iv.getImage();
			var width = image == null ? 0 : image.getWidth();
			var height = image == null ? 0 : image.getHeight();
			textImageWidth.setText(String.valueOf((int) width));
			textImageHeight.setText(String.valueOf((int) height));
			ivFit();
		});
		iv.setOnScroll(event -> {

			var image = iv.getImage();
			if(image == null) return;
//			var width = image.getWidth();
//			var height = image.getHeight();
			var fitWidth = iv.getFitWidth();
			var fitHeight = iv.getFitHeight();

//			var x = event.getDeltaX();
			var y = event.getDeltaY();

			if(event.isControlDown()) // 按下 ctrl = 缩放
			{
				if(y > 0) zoomIn();
				else zoomOut();
			}
			else // 移动视角
			{
				if(event.isAltDown()) // 横向移动
				{
					var h = mid.getHvalue();

					if(y > 0)
					{
						var value = (h * fitWidth - 50) / fitWidth;
						mid.setHvalue(Math.max(value, 0));
					}
					else
					{
						var value = (h * fitWidth + 50) / fitWidth;
						mid.setHvalue(Math.min(1, value));
					}
				}
				else // 纵向移动
				{
					var v = mid.getVvalue();

					if(y > 0)
					{
						var value = (v * fitHeight - 50) / fitHeight;
						mid.setVvalue(Math.max(value, 0));
					}
					else
					{
						var value = (v * fitHeight + 50) / fitHeight;
						mid.setVvalue(Math.min(1, value));
					}
				}
			}

			event.consume();
		});


		this.scene = new Scene(pane);

	}

	/**
	 * 正在处理的图片
	 * */
	private Image imagePicture;

	private class PolyRegion
	{
		/**
		 * 最主要的图形
		 * */
		Polygon poly;

		/**
		 * 标记开始点用的
		 * */
		Rectangle rectStart;

		public PolyRegion()
		{
			poly = new Polygon();
			rectStart = new Rectangle(4, 4);
			this.poly.setOnMouseClicked(this::onClick);
			this.rectStart.setOnMouseClicked(this::onClick);
			setVisible(false);
		}

		private Consumer<MouseEvent> onClick;
		public void setOnClick(Consumer<MouseEvent> listener)
		{
			this.onClick = listener;
		}
		private void onClick(MouseEvent event)
		{
			if(this.onClick != null)
				this.onClick.accept(event);
		}

		/**
		 * 添加到面板上
		 * */
		public void addOn(Pane node)
		{
			node.getChildren().addAll(rectStart, poly);
		}
		/**
		 * 从面板移除
		 * */
		public void removeFrom(Pane node)
		{
			node.getChildren().removeAll(rectStart, poly);
		}

		public void addPoint(double x, double y)
		{
			var points = this.poly.getPoints();
			points.addAll(x, y);
			if(points.size() == 2)
			{
				this.rectStart.setLayoutX(x);
				this.rectStart.setLayoutY(y);
			}
		}

		public void setVisible(boolean value)
		{
			this.poly.setVisible(value);
			this.rectStart.setVisible(value);
		}

		private static final Color colorHighlight = Color.color(0.0667, 0.9450, 0.4314, 0.4);
		public void setHighlight(boolean value)
		{
			if(value)
			{
				poly.setFill(colorHighlight);
				poly.setStroke(Color.AQUA);
				poly.setStrokeWidth(3.5);
			}
			else
			{
				poly.setFill(Color.TRANSPARENT);
				poly.setStroke(Color.DEEPSKYBLUE);
				poly.setStrokeWidth(2);
			}
		}

		public void setPointStart(double x, double y)
		{
			;
		}
	}

	private Pane pane;

	@Getter
	private Scene scene;

}
