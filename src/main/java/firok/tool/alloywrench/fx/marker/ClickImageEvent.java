package firok.tool.alloywrench.fx.marker;

import firok.tool.alloywrench.fx.Event;

/**
 * 用户点击图片某一点
 * */
public class ClickImageEvent extends Event
{
	public final int realX, realY, clickCount;
	public final javafx.event.Event event;
	public ClickImageEvent(int realX, int realY, int clickCount, javafx.event.Event evt)
	{
		this.realX = realX;
		this.realY = realY;
		this.clickCount = clickCount;
		this.event = evt;
	}
}
