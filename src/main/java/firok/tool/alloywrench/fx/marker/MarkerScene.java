package firok.tool.alloywrench.fx.marker;

import firok.tool.alloywrench.fx.Event;
import firok.tool.alloywrench.fx.IScene;
import firok.topaz.math.Maths;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;

import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Consumer;


import static firok.tool.alloywrench.fx.FxApp.icon;

@SuppressWarnings("FieldCanBeLocal")
public class MarkerScene implements IScene
{
	public final BooleanProperty isShowRightList = new SimpleBooleanProperty(true);
	public final BooleanProperty isShowBottomBar = new SimpleBooleanProperty(true);
	public final IntegerProperty viewportScale = new SimpleIntegerProperty(100);
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
	private final ObjectProperty<ITask> taskProperty = new SimpleObjectProperty<>(null);

	private MenuItem miFile_loadImage, miFile_loadDotaLabel, miFile_loadYoloLabel, miFile_loadJson, miFile_saveJson,
			miViewMove_moveBottom, miViewMove_moveTop, miViewMove_moveLeft, miViewMove_moveRight,
			miViewMove_jumpBottom, miViewMove_jumpTop, miViewMove_jumpLeft, miViewMove_jumpRight,
			miView_resetViewport, miView_zoomIn,
			miView_zoomOut, miView_switchRightList, miView_switchBottomBar;
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
	Text textImageWidth, textImageHeight, textViewportScale, textViewportX, textViewportY, textTask;
	ProgressBar progressTask;
	private BorderPane createToolBar()
	{
		var boxToolbar = new BorderPane();

		var toolbarTask = new ToolBar();
		textTask = new Text("无任务");
		progressTask = new ProgressBar(0.5);
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
	private static double r01(double value)
	{
		return Maths.range(value, 0, 1);
	}
	/**
	 * 根据图片真实坐标 移动到相应位置
	 * */
	public void moveRealCord(int x, int y)
	{
		var fitWidth = iv.getFitWidth();
		var fitHeight = iv.getFitHeight();
		mid.setHvalue(r01(x / fitWidth));
		mid.setVvalue(r01(y / fitHeight));
	}
	private Pane midPane;
	private void _handleImageViewScroll(ScrollEvent event)
	{
		var image = iv.getImage();
		if (image == null) return;
		var y = event.getDeltaY();
		if (event.isControlDown()) // 按下 ctrl = 缩放
		{
			if (y > 0) zoomIn();
			else zoomOut();
		}
		else // 移动视角
		{
			if (event.isAltDown()) // 横向移动
			{
				if (y > 0) moveLeft();
				else moveRight();
			}
			else // 纵向移动
			{
				if (y > 0) moveUp();
				else moveDown();
			}
		}
		event.consume();
	}
	private void _handleImageViewClick(MouseEvent evt)
	{
		// 这个 XY 是 iv 空间坐标
		var x = evt.getX();
		var y = evt.getY();
		int clickCount = evt.getClickCount();

		var scale = viewportScale.get();

		var realX = x * 100 / scale;
		var realY = y * 100 / scale;

		this.handleInnerEvent(new ClickImageEvent((int) realX, (int) realY, clickCount, evt));
	}
	private ScrollPane createMidImage()
	{
		var sp = new ScrollPane();

		midPane = new Pane();

		iv = new ImageView();
		iv.setPreserveRatio(true);
		iv.setLayoutX(0);
		iv.setLayoutY(0);

		var children = midPane.getChildren();
		children.add(iv);

		sp.setContent(midPane);

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
		// 临时先这样吧 面板大小=图片大小
		// 后面可能会增加面板大小 允许在图片外标点
		midPane.minWidthProperty().bind(iv.fitWidthProperty());
		midPane.maxWidthProperty().bind(iv.fitWidthProperty());
		midPane.minHeightProperty().bind(iv.fitHeightProperty());
		midPane.maxHeightProperty().bind(iv.fitHeightProperty());

		// 同步任务进度
		taskProperty.addListener(event -> {
			var task = taskProperty.get();
			if(task == null)
			{
				textTask.setVisible(false);
				progressTask.setVisible(false);
			}
			else
			{
				textTask.setVisible(true);

				var progressNow = task.getProgressNow();
				var progressTotal = task.getProgressTotal();
				if(progressNow < 0 || progressTotal < 0)
				{
					progressTask.setVisible(false);
				}
				else
				{
					progressTask.setVisible(true);
					progressTask.progressProperty().set(task.getProgressPercent());
				}
			}
		});

	}
	private Mnemonic addKey(MenuItem mi, KeyCombination key)
	{
		mi.setAccelerator(key);
		var mn = new Mnemonic(mi.getGraphic(), key);
		this.scene.addMnemonic(mn);
		return mn;
	}
	@SuppressWarnings("unused")
	private Mnemonic mnLoadImage, mnLoadDotaLabel, mnLoadYoloLabel, mnLoadJson,
			mnResetViewport, mnZoomIn, mnZoomOut,
			mnSwitchRightList, mnSwitchBottomBar,
			mnViewMove_moveBottom, mnViewMove_moveTop, mnViewMove_moveLeft, mnViewMove_moveRight,
			mnViewMove_jumpBottom, mnViewMove_jumpTop, mnViewMove_jumpLeft, mnViewMove_jumpRight;
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
	/**
	 * 绑定用户操作事件
	 * */
	private void bindOperations()
	{
		iv.setOnScroll(this::_handleImageViewScroll); // 监听图片缩放事件
		iv.setOnMouseClicked(this::_handleImageViewClick); // 监听图片点击事件
	}

	private boolean checkTaskNullOrEqual(Class<? extends ITask> classTask)
	{
		var task = taskProperty.get();
		return classTask == null || classTask.isInstance(task);
	}
	private void checkTaskEqual(Class<? extends ITask> classTask) throws IllegalStateException
	{
		var task = taskProperty.get();
		if(classTask == null) // 当前不能有正在进行的任务
		{
			if(task != null)
				throw new IllegalStateException();
		}
		else // 当前必须正在进行某种类型的任务
		{
			if(!classTask.isInstance(task))
				throw new IllegalStateException();
		}
	}
	private void handleInnerEvent(Event param)
	{
		if(param instanceof ClickImageEvent event)
		{
//			System.out.println("点击图片 " + event.realX + ", " + event.realY + " - " + event.clickCount);
			// todo
			var _t = this.taskProperty.get();
			if(checkTaskNullOrEqual(CreatePolygonTask.class))
			{
				double x = event.realX, y = event.realY;
				List<Double> points;

				if(_t != null)
				{
					var task = (CreatePolygonTask) _t;
					points = new ArrayList<>(task.polygon.getPoints());
					midPane.getChildren().remove(task.polygon);
				}
				else
				{
					points = new ArrayList<>();
				}

				points.add(x);
				points.add(y);
				var polygon = new Polygon();
				polygon.getPoints().addAll(points);
				midPane.getChildren().add(polygon);
				var taskNew = new CreatePolygonTask(polygon);
				this.taskProperty.set(taskNew);
			}
		}
	}

	@Getter
	private final Scene scene;
	private final BorderPane layout;
	private final Consumer<Event> busStage;
	private final KeyMapping keyMapping = new KeyMapping();
	public MarkerScene(Consumer<Event> bus)
	{
		this.busStage = bus;

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
		bindOperations();
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
