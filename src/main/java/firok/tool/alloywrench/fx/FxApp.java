package firok.tool.alloywrench.fx;

import firok.tool.alloywrench.AlloyWrench;
import firok.tool.alloywrench.task.FxBase;

public class FxApp extends FxBase
{
	/**
	 * 当前正在运行的场景
	 * */
	private IScene scene;

	private void onEvent(Event evt)
	{
		if(evt instanceof SwitchSceneEvent event)
		{
			if(this.scene != null)
			{
				try { this.scene.close(); }
				catch (Exception any) { any.printStackTrace(System.err); }
			}

			switch (event.scene)
			{
				case Scenes.MARKER -> _switchMarker();
				case Scenes.MAIN_MENU -> _switchMainMenu();
			}
		}
	}
	private void _switchMainMenu()
	{
		this.scene = new MainMenuScene(this::onEvent);
		var scene = this.scene.getScene();
		this.stage.setScene(scene);
	}
	private void _switchMarker()
	{
		this.scene = new MarkerScene(this::onEvent);
		var scene = this.scene.getScene();
		this.stage.setScene(scene);
	}

	@Override
	protected void postStart()
	{
		this.onEvent(new SwitchSceneEvent(Scenes.MARKER)); // init event
		super.stage.setMinWidth(450);
		super.stage.setMinHeight(300);
		super.stage.setTitle(AlloyWrench.name + " " + AlloyWrench.version + " by " + AlloyWrench.author);
		super.stage.show();
	}
}