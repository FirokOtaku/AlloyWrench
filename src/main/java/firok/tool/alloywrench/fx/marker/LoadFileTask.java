package firok.tool.alloywrench.fx.marker;

public class LoadFileTask implements ITask
{
	private final String filename;
	private final long filesize, loadsize;
	public LoadFileTask(String filename, long filesize, long loadsize)
	{
		this.filename = filename;
		this.filesize = filesize;
		this.loadsize = loadsize;
	}

	@Override
	public String getDisplayName()
	{
		return "加载文件: " + filename;
	}

	@Override
	public long getProgressTotal()
	{
		return filesize;
	}

	@Override
	public long getProgressNow()
	{
		return loadsize;
	}
}
