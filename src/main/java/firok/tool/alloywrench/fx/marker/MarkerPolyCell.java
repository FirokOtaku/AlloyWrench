package firok.tool.alloywrench.fx.marker;

import javafx.scene.control.*;
import javafx.util.Callback;

import static firok.tool.alloywrench.fx.FxApp.icon;

class MarkerPolyCell extends ListCell<MarkerPoly> implements Callback<Class<?>, Object>
{
	private final ContextMenu menu;
	public MarkerPolyCell()
	{
		menu = new ContextMenu();
		var menu_goto = new MenuItem("查看", icon("mdi2m-map-marker-outline"));
		menu_goto.setOnAction(event -> {
			System.out.println("goto: " + getItem());
		});
		var menu_edit = new MenuItem("编辑", icon("mdi2s-square-edit-outline"));
		menu_edit.setOnAction(event -> {
			System.out.println("edit: " + getItem());
		});
		var menu_delete = new MenuItem("删除", icon("mdi2d-delete-outline"));
		menu_delete.setOnAction(event -> {
			System.out.println("delete: " + getItem());
		});
		menu.getItems().addAll(menu_goto, menu_edit, menu_delete);
	}

	@Override
	protected void updateItem(MarkerPoly item, boolean empty)
	{
		super.updateItem(item, empty);
		if(empty || item == null)
		{
			setText(null);
			setContextMenu(null);
		}
		else
		{
			setText(item.toString());
			setContextMenu(menu);
		}
	}

	@Override
	public Object call(Class<?> param)
	{
		return this;
	}
}
