package de.dakror.virtualhub.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.dnd.DragSource;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class CatalogTreeCellRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;
	
	ClientFrame frame;
	
	public CatalogTreeCellRenderer(ClientFrame frame)
	{
		this.frame = frame;
		
		setLeafIcon(CFG.FOLDER);
		setOpenIcon(CFG.FOLDER);
		setClosedIcon(CFG.FOLDER);
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		JLabel tce = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		if (row < 0 || row >= tree.getRowCount()) return tce;
		
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) frame.catalog.getPathForRow(row).getLastPathComponent();
		
		if (frame.dragged != null)
		{
			int mouseRow = (frame.mouse.y + frame.catalogWrap.getVerticalScrollBar().getValue() - tce.getPreferredSize().height * 2) / tce.getPreferredSize().height;
			if (mouseRow == row)
			{
				if (!setFrameCursor(dmtn))
				{
					frame.targetNode = dmtn;
				}
				
				selected = true;
				tce.setForeground(Color.white);
				
				if (!leaf && frame.mouse.x < 20 * dmtn.getLevel() && frame.mouse.x > 20 * dmtn.getLevel() - 20) tree.expandRow(row);
				
				this.hasFocus = true;
			}
		}
		return tce;
	}
	
	private boolean setFrameCursor(DefaultMutableTreeNode node)
	{
		File nodeFile = new File(Assistant.getNodePath(node));
		
		File[] selected = frame.getSelectedFiles();
		boolean sameFile = false;
		
		for (File f : selected)
		{
			if (nodeFile.getPath().replace("\\", "/").startsWith(f.getPath().replace("\\", "/"))) sameFile = true;
			if (nodeFile.equals(f.getParentFile())) sameFile = true;
		}
		
		Cursor c = frame.copy ? sameFile ? DragSource.DefaultCopyNoDrop : DragSource.DefaultCopyDrop : sameFile ? DragSource.DefaultMoveNoDrop : DragSource.DefaultMoveDrop;
		
		frame.setCursor(c);
		
		return sameFile;
	}
}
