package com.runescape.utility.applications.shop;

import com.runescape.game.content.economy.shopping.StoreInstance;
import com.runescape.game.world.item.Item;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class ReorderableTableModel extends DefaultTableModel implements Reorderable {
	private static final long serialVersionUID = -2555506085061662299L;

	public ReorderableTableModel(ShopsEditor editor) {
		super();
		this.editor = editor;
	}

	@Override
	public void reorder(int fromIndex, int toIndex) {
		int selectedItemRow = editor.shopContentTable.getSelectedRow();
		StoreInstance shop = editor.getSelectedShop();
		List<Item> shopItems = new ArrayList<>(shop.getStock());
		Item selected = shopItems.get(selectedItemRow);
		shopItems.remove(fromIndex);
		// only possible if we're moving to last slot
		if (toIndex > shopItems.size()) {
			List<Item> newList = shopItems.stream().collect(Collectors.toList());
			newList.add(selected);
			shopItems = newList;
		} else {
			shopItems.add(toIndex, selected);
		}
		shop.setStock(shopItems);
		editor.refreshList(shop);
		editor.displayShopItems(false);
	}
	
	private final ShopsEditor editor;

}
