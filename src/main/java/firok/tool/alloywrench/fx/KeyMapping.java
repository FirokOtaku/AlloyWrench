package firok.tool.alloywrench.fx;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyMapping
{
	public final KeyCombination kcLoadImage;
	public final KeyCombination kcLoadDotaLabel;
	public final KeyCombination kcLoadYoloLabel;
	public final KeyCombination kcLoadJson;
	public final KeyCombination kcResetViewport;
	public final KeyCombination kcZoomIn;
	public final KeyCombination kcZoomOut;
	public final KeyCombination kcSwitchRightList;
	public final KeyCombination kcSwitchBottomBar;
	public final KeyCombination kcViewMove_moveBottom;
	public final KeyCombination kcViewMove_moveTop;
	public final KeyCombination kcViewMove_moveLeft;
	public final KeyCombination kcViewMove_moveRight;
	public final KeyCombination kcViewMove_jumpBottom;
	public final KeyCombination kcViewMove_jumpTop;
	public final KeyCombination kcViewMove_jumpLeft;
	public final KeyCombination kcViewMove_jumpRight;

	public KeyMapping()
	{
		kcLoadImage = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN);
		kcLoadDotaLabel = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN);
		kcLoadYoloLabel = new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN);
		kcLoadJson = new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN);
		kcResetViewport = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
		kcZoomIn = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
		kcZoomOut = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
		kcSwitchRightList = new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.CONTROL_DOWN);
		kcSwitchBottomBar = new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN);
		kcViewMove_moveBottom = new KeyCodeCombination(KeyCode.S);
		kcViewMove_moveTop = new KeyCodeCombination(KeyCode.W);
		kcViewMove_moveLeft = new KeyCodeCombination(KeyCode.A);
		kcViewMove_moveRight = new KeyCodeCombination(KeyCode.D);
		kcViewMove_jumpBottom = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.SHIFT_DOWN);
		kcViewMove_jumpTop = new KeyCodeCombination(KeyCode.W, KeyCodeCombination.SHIFT_DOWN);
		kcViewMove_jumpLeft = new KeyCodeCombination(KeyCode.A, KeyCodeCombination.SHIFT_DOWN);
		kcViewMove_jumpRight = new KeyCodeCombination(KeyCode.D, KeyCodeCombination.SHIFT_DOWN);
	}
}
