/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.runescape.utility.applications.shop;

import com.runescape.cache.Cache;
import com.runescape.game.content.economy.shopping.StoreInstance;
import com.runescape.game.world.item.Item;
import com.runescape.utility.external.gson.loaders.StoreLoader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler
 */
@SuppressWarnings("serial")
public class ShopsEditor extends JFrame {

	/**
	 * Creates new form ShopsEditor
	 */
	public ShopsEditor() throws IOException {
		this.loader = new StoreLoader();
		loader.initialize();
		setShopList(loader.generateList());
		initComponents();
	}

	/**
	 * Decorates the tree with all the components that should be included in it
	 */
	protected DefaultMutableTreeNode decorateTree() {
		System.err.println("ShopsEditor.decorateTree()");
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Shops");
		for (StoreInstance shop : getShopList()) {
			System.err.println("\t" + shop.getName());
			root.add(new DefaultMutableTreeNode(shop.getName()));
		}
		return root;
	}

	/**
	 * Refreshes elements in the tree
	 */
	protected void refreshTree() {
		DefaultTreeModel model = (DefaultTreeModel) shopTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		model.reload(root);
	}

	protected void setTree(JTree tree) {
		this.shopTree = tree;
	}

	/**
	 *
	 */
	private void addTableDragListener() {
		shopContentTable.setDragEnabled(true);
		shopContentTable.setDropMode(DropMode.INSERT_ROWS);
		shopContentTable.setTransferHandler(new TableRowTransferHandler(shopContentTable));
	}

	private void addTableMouseListener() {
		shopContentTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int r = shopContentTable.rowAtPoint(e.getPoint());
				if (r >= 0 && r < shopContentTable.getRowCount()) {
					shopContentTable.setRowSelectionInterval(r, r);
				} else {
					shopContentTable.clearSelection();
				}

				int rowindex = shopContentTable.getSelectedRow();
				if (rowindex < 0) {
					return;
				}
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(listener -> {
						int selectedItemRow = shopContentTable.getSelectedRow();
						StoreInstance shop = getSelectedShop();
						List<Item> shopItems = new ArrayList<>(shop.getStock());
						Item selected = shopItems.get(selectedItemRow);
						shopItems.remove(selected);
						shop.setStock(shopItems);
						refreshList(shop);
						displayShopItems(false);
					});
					popup.add(delete);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	protected void defineTree() {
		shopTreeScrollPane = new JScrollPane();
		shopTree = new JTree(decorateTree());

		MouseListener mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				TreePath selectionPath = shopTree.getPathForLocation(x, y);
				if (selectionPath == null) {
					return;
				}
				setCurrentSelectedPath(selectionPath);
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu popup = new JPopupMenu(getSelectedShop().getName());
					Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0, popup.getForeground());
					TitledBorder labelBorder = BorderFactory.createTitledBorder(titleUnderline, popup.getLabel(), TitledBorder.CENTER, TitledBorder.ABOVE_TOP, popup.getFont(), popup.getForeground());
					popup.setBorder(BorderFactory.createCompoundBorder(popup.getBorder(), labelBorder));
					popup.add(new JMenuItem("Add Item")).addActionListener(listener -> {
						StoreInstance shop = getSelectedShop();
						String itemIdString = JOptionPane.showInputDialog("Enter item id");
						if (itemIdString == null || itemIdString.equalsIgnoreCase("null")) {
							return;
						}
						Integer itemId = Integer.valueOf(itemIdString);
						List<Item> mainStock = new ArrayList<>(shop.getStock());
						mainStock.add(new Item(itemId, 1));
						shop.setStock(mainStock);
						displayShopItems(true);
					});
					JMenu edit = new JMenu("Edit");

					JMenuItem currencyName = new JMenuItem("Currency Name");
					edit.add(currencyName);

					currencyName.addActionListener(listener -> {
						StoreInstance shop = getSelectedShop();
						String currency = JOptionPane.showInputDialog("Enter new currency class name.\nCurrently:\t" + shop.getCurrencyName());
						shop.setCurrencyName(currency);
					});

					JMenuItem shopName = new JMenuItem("Shop Name");
					edit.add(shopName);

					shopName.addActionListener(listener -> {
						StoreInstance shop = getSelectedShop();
						String name = JOptionPane.showInputDialog("Enter new shop name.");
						if (name.length() < 1 || name == null) {
							System.out.println("ShopsEditor.defineTree().new MouseAdapter() {...}.mousePressed()");
							return;
						}
						shop.setName(name);

						TreePath selectedPath = getCurrentSelectedPath();
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
						node.setUserObject(name);
						DefaultTreeModel model = (DefaultTreeModel) shopTree.getModel();
						model.reload();
					});

					JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem("Sell Back");
					checkBox.setSelected(getSelectedShop().canSellBack());
					edit.add(checkBox);

					checkBox.addActionListener(listener -> {
						StoreInstance shop = getSelectedShop();
						shop.setCanSellBack(!shop.canSellBack());
					});

					popup.add(edit);

					popup.addSeparator();
					popup.add(new JMenuItem("Delete")).addActionListener(listener -> {
						shopList.remove(getSelectedShop());
						TreePath selectedPath = getCurrentSelectedPath();
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
						DefaultTreeModel model = (DefaultTreeModel) (shopTree.getModel());
						model.removeNodeFromParent(node);
						model.reload();
					});
					popup.show(shopTree, x, y);
				} else if (SwingUtilities.isLeftMouseButton(e)) {
					displayShopItems(true);
				}
			}
		};
		shopTree.addMouseListener(mouseListener);
		shopTreeScrollPane.setViewportView(shopTree);
		System.out.println("ShopsEditor.defineTree()");
	}

	/**
	 * Displays the items
	 *
	 * @param scrollToBottom
	 * 		If the frame should scroll to the bottom of the list of items
	 */
	protected void displayShopItems(boolean scrollToBottom) {
		StoreInstance shop = getSelectedShop();
		if (shop == null) {
			return;
		}
		ReorderableTableModel model = new ReorderableTableModel(this) {
			private static final long serialVersionUID = 4890109891417882803L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 0;
			}
		};
		model.addColumn("Index");
		model.addColumn("Name");
		model.addColumn("Id");
		model.addColumn("Amount");
		if (shop.getStock() != null) {
			for (Item item : shop.getStock()) {
				if (item == null) {
					continue;
				}
				model.addRow(new Object[] { item.getDefinitions().getName(), item.getDefinitions().id, item.getAmount() });
			}
		}
		shopContentTable.setModel(model);
		if (scrollToBottom) {
			shopContentTable.scrollRectToVisible(shopContentTable.getCellRect(shopContentTable.getRowCount() - 1, shopContentTable.getColumnCount(), true));
		}
		setTableModelListener(shop);
	}

	/**
	 * Sets the table model listener
	 */
	private void setTableModelListener(StoreInstance shop) {
		shopContentTable.getModel().addTableModelListener(e -> {
			if (e.getType() == TableModelEvent.UPDATE) {
				Item item = shop.getStock().get(e.getFirstRow());
				if (e.getColumn() == 1) {
					item.setId((short) Integer.parseInt((String) shopContentTable.getModel().getValueAt(e.getFirstRow(), e.getColumn())));
				} else {
					item.setAmount(Integer.parseInt((String) shopContentTable.getModel().getValueAt(e.getFirstRow(), e.getColumn())));
				}
				displayShopItems(false);
				refreshList(shop);
			}
		});
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	protected void initComponents() {
		itemPanel = new JPanel();

		jScrollPane2 = new JScrollPane();
		shopContentTable = new JTable();
		addTableMouseListener();
		addTableDragListener();
		menuBar = new JMenuBar();
		fileMenu = new JMenu();
		exitItem = new JMenuItem();
		newButton = new JButton();
		saveMenu = new JButton();
		saveMenu.addActionListener(listener -> {
			loader.save(shopList);
			System.out.println("Saved all shops.");
		});
		newButton.addActionListener(listener -> {
			String name = JOptionPane.showInputDialog("Enter Shop Name");
			if (name == null || name.length() < 1) {
				return;
			}
			shopList.add(new StoreInstance(name));
			DefaultTreeModel model = (DefaultTreeModel) shopTree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
			root.add(new DefaultMutableTreeNode(name));
			model.reload(root);
		});
		defineTree();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);

		itemPanel.setBorder(BorderFactory.createCompoundBorder());

		jScrollPane2.setViewportView(shopContentTable);

		GroupLayout itemPanelLayout = new GroupLayout(itemPanel);
		itemPanel.setLayout(itemPanelLayout);
		itemPanelLayout.setHorizontalGroup(itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(itemPanelLayout.createSequentialGroup().addComponent(shopTreeScrollPane, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 513, GroupLayout.PREFERRED_SIZE)));
		itemPanelLayout.setVerticalGroup(itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(shopTreeScrollPane).addComponent(jScrollPane2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE));

		fileMenu.setText("File");

		exitItem.setText("Exit");
		exitItem.addActionListener(listener -> {
			System.exit(-1);
		});
		fileMenu.add(exitItem);

		menuBar.add(fileMenu);

		newButton.setText("New Shop");
		menuBar.add(newButton);

		saveMenu.setText("Save");
		menuBar.add(saveMenu);

		setJMenuBar(menuBar);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(itemPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(itemPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		pack();

		setLocationRelativeTo(null);
	}// </editor-fold>

	/**
	 * @param args
	 * 		the command line arguments
	 */
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Cache.init();
					setSingleton(new ShopsEditor());
					getSingleton().setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * @return the loader
	 */
	public StoreLoader getLoader() {
		return loader;
	}

	/**
	 * @return the singleton
	 */
	public static ShopsEditor getSingleton() {
		return singleton;
	}

	/**
	 * @param singleton
	 * 		the singleton to set
	 */
	public static void setSingleton(ShopsEditor singleton) {
		ShopsEditor.singleton = singleton;
	}

	/**
	 * @return the currentSelectedPath
	 */
	public TreePath getCurrentSelectedPath() {
		return currentSelectedPath;
	}

	/**
	 * @param currentSelectedPath
	 * 		the currentSelectedPath to set
	 */
	public void setCurrentSelectedPath(TreePath currentSelectedPath) {
		this.currentSelectedPath = currentSelectedPath;
	}

	/**
	 * @return the shopList
	 */
	public List<StoreInstance> getShopList() {
		return shopList;
	}

	/**
	 * @param shopList
	 * 		the shopList to set
	 */
	public void setShopList(List<StoreInstance> shopList) {
		this.shopList = shopList;
	}

	/**
	 * Parses the current selected shop from the {@link #currentSelectedPath}
	 */
	public StoreInstance getSelectedShop() {
		if (currentSelectedPath == null) {
			throw new IllegalStateException();
		}
		if (currentSelectedPath.getPath().length != 2) {
			return null;
		}
		String shopName = String.valueOf(currentSelectedPath.getPath()[1]);
		for (StoreInstance shop : shopList) {
			if (shop.getName().equals(shopName)) {
				return shop;
			}
		}
		return null;
	}

	/**
	 * @return the root
	 */
	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	/**
	 * @param root
	 * 		the root to set
	 */
	public void setRoot(DefaultMutableTreeNode root) {
		this.root = root;
	}

	/**
	 * @param selectedShop
	 */
	public void refreshList(StoreInstance selectedShop) {
		int indexOfShop = shopList.indexOf(selectedShop);
		if (indexOfShop > shopList.size()) {
			throw new IllegalStateException();
		}
		shopList.set(indexOfShop, selectedShop);
	}

	// Variables declaration - do not modify
	private JMenu fileMenu;

	private JPanel itemPanel;

	private JMenuItem exitItem;

	private JScrollPane shopTreeScrollPane;

	private JScrollPane jScrollPane2;

	private JMenuBar menuBar;

	private JButton saveMenu, newButton;

	protected JTable shopContentTable;

	protected JTree shopTree;

	private DefaultMutableTreeNode root;

	private List<StoreInstance> shopList;

	private final StoreLoader loader;

	private TreePath currentSelectedPath;
	// End of variables declaration

	private static ShopsEditor singleton;
}
