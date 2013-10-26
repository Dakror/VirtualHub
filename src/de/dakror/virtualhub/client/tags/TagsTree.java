package de.dakror.virtualhub.client.tags;

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Dakror
 */
public class TagsTree extends JTree implements DragSourceListener, DragGestureListener
{
	private static final long serialVersionUID = 1L;
	
	public DragSource dragSource = DragSource.getDefaultDragSource();
	
	public TagsTree(DefaultTreeModel dtm)
	{
		super(dtm);
		
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_LINK, this);
	}
	
	@Override
	public void dragGestureRecognized(DragGestureEvent dge)
	{
		if (getRowForPath(getSelectionPath()) > 0)
		{
			StringSelection transferable = new StringSelection(getSelectionPath().getLastPathComponent().toString());
			
			dge.startDrag(DragSource.DefaultLinkDrop, transferable);
		}
	}
	
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
}
