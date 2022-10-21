package firok.tool.alloywrench.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

@SuppressWarnings("FieldCanBeLocal")
public class MarkerScene implements IScene
{
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
	public BooleanProperty isShowBottomBar = new SimpleBooleanProperty(true);
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

	private MenuItem miFile_loadImage;
	private MenuItem miFile_loadDotaLabel;
	private MenuItem miFile_loadYoloLabel;
	private MenuItem miFile_loadJson;
	private MenuItem miFile_saveJson;
	private MenuItem miViewMove_moveBottom;
	private MenuItem miViewMove_moveTop;
	private MenuItem miViewMove_moveLeft;
	private MenuItem miViewMove_moveRight;
	private MenuItem miViewMove_jumpBottom;
	private MenuItem miViewMove_jumpTop;
	private MenuItem miViewMove_jumpLeft;
	private MenuItem miViewMove_jumpRight;
	private MenuItem miView_resetViewport;
	private MenuItem miView_zoomIn;
	private MenuItem miView_zoomOut;
	private MenuItem miView_switchRightList;
	private MenuItem miView_switchBottomBar;
	private MenuBar createMenuBar()
	{
		var menuFile = new Menu("文件", icon("mdi2f-file-outline"));
		miFile_loadImage = new MenuItem("加载图片", icon("mdi2f-file-image-outline"));
		miFile_loadImage.setOnAction(event -> {
			System.out.println("加载图片");
			var image = new Image("C:\\Users\\a3517\\Pictures\\80567870_p0.jpg");
			iv.setImage(image);
		});
		miFile_loadDotaLabel = new MenuItem("加载 DOTA 标签", icon("mdi2f-file-document-outline"));
		miFile_loadYoloLabel = new MenuItem("加载 YOLO 标签", icon("mdi2f-file-document-outline"));
		miFile_loadJson = new MenuItem("加载综合 JSON", icon("mdi2f-file-star-outline"));
		miFile_saveJson = new MenuItem("保存综合 JSON");
		menuFile.getItems().addAll(
				miFile_loadImage,
				miFile_loadDotaLabel,
				miFile_loadYoloLabel,
				miFile_loadJson,
				new SeparatorMenuItem(),
				miFile_saveJson
		);

		var menuView = new Menu("视图", icon("mdi2e-eye-outline"));

		var menuViewMove = new Menu("移动", icon("mdi2c-cursor-move"));
		miViewMove_moveBottom = new MenuItem("底部移动", icon("mdi2a-arrow-down"));
		miViewMove_moveBottom.setOnAction(event -> moveDown());
		miViewMove_moveTop = new MenuItem("顶部移动", icon("mdi2a-arrow-up"));
		miViewMove_moveTop.setOnAction(event -> moveUp());
		miViewMove_moveLeft = new MenuItem("左侧移动", icon("mdi2a-arrow-left"));
		miViewMove_moveLeft.setOnAction(event -> moveLeft());
		miViewMove_moveRight = new MenuItem("右侧移动", icon("mdi2a-arrow-right"));
		miViewMove_moveRight.setOnAction(event -> moveRight());
		miViewMove_jumpBottom = new MenuItem("底部跳转", icon("mdi2a-arrow-collapse-down"));
		miViewMove_jumpBottom.setOnAction(event -> mid.setVvalue(1));
		miViewMove_jumpTop = new MenuItem("顶部跳转", icon("mdi2a-arrow-collapse-up"));
		miViewMove_jumpTop.setOnAction(event -> mid.setVvalue(0));
		miViewMove_jumpLeft = new MenuItem("左侧跳转", icon("mdi2a-arrow-collapse-left"));
		miViewMove_jumpLeft.setOnAction(event -> mid.setHvalue(0));
		miViewMove_jumpRight = new MenuItem("右侧跳转", icon("mdi2a-arrow-collapse-right"));
		miViewMove_jumpRight.setOnAction(event -> mid.setHvalue(1));
		menuViewMove.getItems().addAll(
				miViewMove_moveBottom,
				miViewMove_moveTop,
				miViewMove_moveLeft,
				miViewMove_moveRight,
				miViewMove_jumpBottom,
				miViewMove_jumpTop,
				miViewMove_jumpLeft,
				miViewMove_jumpRight
		);

		miView_resetViewport = new MenuItem("重置视角", icon("mdi2b-backup-restore"));
		miView_resetViewport.setOnAction(event -> {
			viewportScale.setValue(100);
			mid.setHvalue(0);
			mid.setVvalue(0);
		});
		miView_zoomIn = new MenuItem("放大", icon("mdi2m-magnify-plus-outline"));
		miView_zoomIn.setOnAction(event -> zoomIn());
		miView_zoomIn.setAccelerator(keyMapping.kcZoomIn);
		miView_zoomOut = new MenuItem("缩小", icon("mdi2m-magnify-minus-outline"));
		miView_zoomOut.setOnAction(event -> zoomOut());
		miView_zoomOut.setAccelerator(keyMapping.kcZoomOut);
		miView_switchRightList = new MenuItem("显示/隐藏右侧列表", icon("mdi2d-dock-right"));
		miView_switchRightList.setOnAction(event -> {
			var isShow = isShowRightList.get();
			isShowRightList.setValue(!isShow);
		});
		miView_switchBottomBar = new MenuItem("显示/隐藏底部状态栏", icon("mdi2d-dock-bottom"));
		miView_switchBottomBar.setOnAction(event -> {
			var isShow = isShowBottomBar.get();
			isShowBottomBar.setValue(!isShow);
		});
		menuView.getItems().addAll(
				miView_resetViewport,
				menuViewMove,
				miView_zoomIn,
				miView_zoomOut,
				new SeparatorMenuItem(),
				miView_switchRightList,
				miView_switchBottomBar
		);

//		var menuLabel = new Menu("标签", icon("mdi2l-label-outline"));
//		var menuLabel_editSelected = new MenuItem("编辑选中", icon("mdi2p-pencil-outline"));
//		var menuLabel_deleteSelected = new MenuItem("删除选中", icon("mdi2d-delete-outline"));
//		var menuLabel_rotateInner = new MenuItem("旋转起始点", icon("mdi2r-reload"));
//		menuLabel.getItems().addAll(
//				menuLabel_editSelected,
//				menuLabel_deleteSelected,
//				new SeparatorMenuItem(),
//				menuLabel_rotateInner
//		);

		var menubar = new MenuBar();
		menubar.getMenus().addAll(menuFile, menuView);
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

		var list = new ListView<MarkerPoly>();
		list.setEditable(false);
		list.setCellFactory(param -> new MarkerPolyCell());
//		IntStream.range(0, 100).forEach(list.getItems()::add);
		list.setMinHeight(50);
		right.setCenter(list);

		return right;
	}
	private ImageView iv;
	public void moveLeft()
	{
		var fitWidth = iv.getFitWidth();
		var h = mid.getHvalue();
		var value = (h * fitWidth - 50) / fitWidth;
		mid.setHvalue(Math.max(value, 0));
	}
	public void moveRight()
	{
		var fitWidth = iv.getFitWidth();
		var h = mid.getHvalue();
		var value = (h * fitWidth + 50) / fitWidth;
		mid.setHvalue(Math.min(1, value));
	}
	public void moveUp()
	{
		var fitHeight = iv.getFitHeight();
		var v = mid.getVvalue();
		var value = (v * fitHeight - 50) / fitHeight;
		mid.setVvalue(Math.max(value, 0));
	}
	public void moveDown()
	{
		var fitHeight = iv.getFitHeight();
		var v = mid.getVvalue();
		var value = (v * fitHeight + 50) / fitHeight;
		mid.setVvalue(Math.min(1, value));
	}
	private ScrollPane createMidImage()
	{
		var sp = new ScrollPane();

		var pane = new Pane();

		iv = new ImageView();
		// 监听图片缩放事件
		iv.setOnScroll(event -> {

			var image = iv.getImage();
			if(image == null) return;
//			var width = image.getWidth();
//			var height = image.getHeight();

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
					if(y > 0) moveLeft();
					else moveRight();
				}
				else // 纵向移动
				{
					if(y > 0) moveUp();
					else moveDown();
				}
			}

			event.consume();
		});
		iv.setPreserveRatio(true);

		sp.setContent(iv);

		return sp;
	}
	private final MenuBar top;
	private final ScrollPane mid;
	private final BorderPane right;
	private final BorderPane bottom;
	/**
	 * 创建各类属性绑定关系
	 * */
	private void bindProperties()
	{
		// 右侧列表的显示属性
		right.visibleProperty().bind(isShowRightList);
		isShowRightList.addListener(event -> {
			int width = isShowRightList.get() ? 150 : 0;
			right.setMinWidth(width);
			right.setMaxWidth(width);
		});

		// 底部栏的显示属性
		bottom.visibleProperty().bind(isShowBottomBar);
		isShowBottomBar.addListener(event -> {
			int height = isShowBottomBar.get() ? 24 : 0;
			bottom.setMaxHeight(height);
			bottom.setMinHeight(height);
		});
		mid.hvalueProperty().addListener(event -> {
			var h = mid.hvalueProperty().get();
			var fitWidth = iv.getFitWidth();
			var pos = "" + (int) (h * fitWidth);
			textViewportX.setText(pos);
		});
		mid.vvalueProperty().addListener(event -> {
			var v = mid.vvalueProperty().get();
			var fitHeight = iv.getFitHeight();
			var pos = "" + (int) (v * fitHeight);
			textViewportY.setText(pos);
		});

		// 同步缩放百分比
		viewportScale.addListener(event -> {
			var scale = viewportScale.get();
			textViewportScale.setText(scale + "%");
			var image = iv.getImage();
			if(image != null && image.getException() == null)
			{
				var width = image.getWidth();
				var height = image.getHeight();
				iv.setFitWidth(1D * width * scale / 100);
				iv.setFitHeight(1D * height * scale / 100);
			}
		});
		// 同步图片大小
		iv.imageProperty().addListener((source, valueOld, valueNew) -> {
			var image = iv.getImage();
			int width, height;
			if(image == null || image.getException() != null)
			{
				width = 0;
				height = 0;
			}
			else
			{
				width = (int) image.getWidth();
				height = (int) image.getHeight();
			}

			textImageWidth.setText(String.valueOf(width));
			textImageHeight.setText(String.valueOf(height));

			var scale = viewportScale.get();
			iv.setFitWidth(1D * width * scale / 100);
			iv.setFitHeight(1D * height * scale / 100);
		});

	}
	private Mnemonic addKey(MenuItem mi, KeyCombination key)
	{
		mi.setAccelerator(key);
		var mn = new Mnemonic(mi.getGraphic(), key);
		this.scene.addMnemonic(mn);
		return mn;
	}
	private Mnemonic mnLoadImage;
	private Mnemonic mnLoadDotaLabel;
	private Mnemonic mnLoadYoloLabel;
	private Mnemonic mnLoadJson;
	private Mnemonic mnResetViewport;
	private Mnemonic mnZoomIn;
	private Mnemonic mnZoomOut;
	private Mnemonic mnSwitchRightList;
	private Mnemonic mnSwitchBottomBar;
	private Mnemonic mnViewMove_moveBottom;
	private Mnemonic mnViewMove_moveTop;
	private Mnemonic mnViewMove_moveLeft;
	private Mnemonic mnViewMove_moveRight;
	private Mnemonic mnViewMove_jumpBottom;
	private Mnemonic mnViewMove_jumpTop;
	private Mnemonic mnViewMove_jumpLeft;
	private Mnemonic mnViewMove_jumpRight;
	/**
	 * 绑定快捷键
	 * */
	private void bindKeys()
	{
		mnLoadImage = addKey(miFile_loadImage, keyMapping.kcLoadImage);
		mnLoadDotaLabel = addKey(miFile_loadDotaLabel, keyMapping.kcLoadDotaLabel);
		mnLoadYoloLabel = addKey(miFile_loadYoloLabel, keyMapping.kcLoadYoloLabel);
		mnLoadJson = addKey(miFile_loadJson, keyMapping.kcLoadJson);
		mnResetViewport = addKey(miView_resetViewport, keyMapping.kcResetViewport);
		mnZoomIn = addKey(miView_zoomIn, keyMapping.kcZoomIn);
		mnZoomOut = addKey(miView_zoomOut, keyMapping.kcZoomOut);
		mnSwitchRightList = addKey(miView_switchRightList, keyMapping.kcSwitchRightList);
		mnSwitchBottomBar = addKey(miView_switchBottomBar, keyMapping.kcSwitchBottomBar);
		mnViewMove_moveBottom = addKey(miViewMove_moveBottom, keyMapping.kcViewMove_moveBottom);
		mnViewMove_moveTop = addKey(miViewMove_moveTop, keyMapping.kcViewMove_moveTop);
		mnViewMove_moveLeft = addKey(miViewMove_moveLeft, keyMapping.kcViewMove_moveLeft);
		mnViewMove_moveRight = addKey(miViewMove_moveRight, keyMapping.kcViewMove_moveRight);
		mnViewMove_jumpBottom = addKey(miViewMove_jumpBottom, keyMapping.kcViewMove_jumpBottom);
		mnViewMove_jumpTop = addKey(miViewMove_jumpTop, keyMapping.kcViewMove_jumpTop);
		mnViewMove_jumpLeft = addKey(miViewMove_jumpLeft, keyMapping.kcViewMove_jumpLeft);
		mnViewMove_jumpRight = addKey(miViewMove_jumpRight, keyMapping.kcViewMove_jumpRight);

	}

	@Getter
	private final Scene scene;
	private final BorderPane layout;
	private final Consumer<Event> bus;
	private final KeyMapping keyMapping = new KeyMapping();
	public MarkerScene(Consumer<Event> bus)
	{
		this.bus = bus;

		layout = new BorderPane();
		this.scene = new Scene(layout);

		top = createMenuBar();
		layout.setTop(top);

		mid = createMidImage();
		layout.setCenter(mid);

		right = createRightList();
		layout.setRight(right);

		bottom = createToolBar();
		layout.setBottom(bottom);

		bindProperties();
		bindKeys();

	}

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
}
