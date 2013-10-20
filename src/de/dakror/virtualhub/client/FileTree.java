package de.dakror.virtualhub.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class FileTree extends JTree implements DropTargetListener, DragSourceListener, DragGestureListener
{
	private static final long serialVersionUID = 1L;
	
	DropTarget dropTarget = new DropTarget(this, this);
	DragSource dragSource = DragSource.getDefaultDragSource();
	
	public FileTree(DefaultTreeModel dtm)
	{
		super(dtm);
		
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}
	
	@Override
	public void dragGestureRecognized(DragGestureEvent dge)
	{}
	
	@Override
	public void dragEnter(DragSourceDragEvent dsde)
	{}
	
	@Override
	public void dragOver(DragSourceDragEvent dsde)
	{}
	
	@Override
	public void dropActionChanged(DragSourceDragEvent dsde)
	{}
	
	@Override
	public void dragExit(DragSourceEvent dse)
	{}
	
	@Override
	public void dragDropEnd(DragSourceDropEvent dsde)
	{}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{
		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}
	
	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{
		int row = getRowForLocation(dtde.getLocation().x, dtde.getLocation().y);
		((FileTreeCellRenderer) getCellRenderer()).highlightedRow = row;
		repaint();
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{}
	
	@Override
	public void dragExit(DropTargetEvent dte)
	{}
	
	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		int highlightedRow = ((FileTreeCellRenderer) getCellRenderer()).highlightedRow;
		if (highlightedRow == -1)
		{
			dtde.rejectDrop();
			return;
		}
		
		Transferable transferable = dtde.getTransferable();
		if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			try
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				@SuppressWarnings("unchecked")
				List<File> selected = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				
				DefaultTreeModel dtm = (DefaultTreeModel) Client.currentClient.frame.catalog.getModel();
				
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) Client.currentClient.frame.catalog.getSelectionPath().getLastPathComponent();
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) getPathForRow(highlightedRow).getLastPathComponent();
				
				Client.currentClient.frame.catalog.expandPath(new TreePath(targetNode.getPath()));
				
				File targetFile = new File(Assistant.getNodePath(targetNode));
				
				boolean copy = dtde.getDropAction() == DnDConstants.ACTION_COPY;
				
				FileMover mover = new FileMover(Client.currentClient.frame, copy, targetFile, selected.toArray(new File[] {}));
				if (!mover.canceled)
				{
					for (int i = 0; i < parent.getChildCount(); i++)
					{
						for (File f : selected)
						{
							if (!f.isDirectory()) continue;
							
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
							if (node.getUserObject().toString().equals(f.getName()))
							{
								if (!copy) parent.remove(i);
								if (!Assistant.containsNode(targetNode, node))
								{
									DefaultMutableTreeNode clone = (DefaultMutableTreeNode) node.clone();
									if (Assistant.hasSubDirectories(f)) clone.add(new DefaultMutableTreeNode("CONTENT"));
									targetNode.add(clone);
								}
							}
						}
					}
					
					Assistant.sortNodeChildren(targetNode);
					
					dtm.reload(parent);
					dtm.reload(targetNode);
					
					Client.currentClient.frame.catalog.setSelectionPath(new TreePath(parent.getPath()));
				}
				
				dtde.getDropTargetContext().dropComplete(true);
				((FileTreeCellRenderer) getCellRenderer()).highlightedRow = -1;
				repaint();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				dtde.rejectDrop();
			}
		}
	}
}
