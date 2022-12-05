package firok.tool.alloywrench.fx.dsmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.util.Jsons;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;

import java.io.File;
import java.io.IOException;

public class DSMgrController
{
	private final ObjectMapper om = Jsons.omDecimal();
	@FXML
	CheckTreeView<CocoData> tree;

	public void loadCoco()
	{
		try
		{
			var coco = om.readValue(new File("V:/221012 影像示例/房地一体/fd-coco.json"), CocoData.class);
			tree.setCheckModel(new CheckModel<TreeItem<CocoData>>()
			{
				@Override
				public int getItemCount()
				{
					return 0;
				}

				@Override
				public ObservableList<TreeItem<CocoData>> getCheckedItems()
				{
					return null;
				}

				@Override
				public void checkAll()
				{

				}

				@Override
				public void clearCheck(TreeItem<CocoData> item)
				{

				}

				@Override
				public void clearChecks()
				{

				}

				@Override
				public boolean isEmpty()
				{
					return false;
				}

				@Override
				public boolean isChecked(TreeItem<CocoData> item)
				{
					return false;
				}

				@Override
				public void check(TreeItem<CocoData> item)
				{

				}

				@Override
				public void toggleCheckState(TreeItem<CocoData> item)
				{

				}
			});
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
