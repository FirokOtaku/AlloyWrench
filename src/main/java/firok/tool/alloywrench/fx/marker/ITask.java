package firok.tool.alloywrench.fx.marker;

import firok.topaz.Maths;

public interface ITask
{
	/**
	 * 显示名称
	 * */
	String getDisplayName();
	/**
	 * 总进度
	 * */
	long getProgressTotal();
	/**
	 * 当前进度
	 * */
	long getProgressNow();
	/**
	 * 进度百分比
	 * */
	default double getProgressPercent()
	{
		return Maths.range(1D * getProgressNow() / getProgressTotal(), 0, 1);
	}

	default int getProgressPercent100()
	{
		var percent = getProgressPercent();
		return (int)(percent * 100);
	}
}
