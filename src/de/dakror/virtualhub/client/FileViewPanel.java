package de.dakror.virtualhub.client;

import java.awt.LayoutManager;
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

import javax.swing.JPanel;

/**
 * @author Dakror
 */
public class FileViewPanel extends JPanel implements DropTargetListener, DragSourceListener, DragGestureListener
{
	private static final long serialVersionUID = 1L;
	
	DropTarget dropTarget = new DropTarget(this, this);
	DragSource dragSource = DragSource.getDefaultDragSource();
	
	public FileViewPanel(LayoutManager layout)
	{
		super(layout);
		
		dropTarget.setActive(false);
		
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
	{}
	
	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{}
	
	@Override
	public void dragExit(DropTargetEvent dte)
	{}
	
	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		if (getComponentCount() == 0)
		{
			dtde.rejectDrop();
			return;
		}
		
		// try
		// {
		// Transferable tr = dtde.getTransferable();
		// if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		// {
		// dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		// java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
		// Iterator iterator = fileList.iterator();
		// while (iterator.hasNext())
		// {
		// File file = (File) iterator.next();
		// CFG.p("Received: " + file);
		// }
		// dtde.getDropTargetContext().dropComplete(true);
		// }
		// else
		// {
		// CFG.p("Rejected");
		// dtde.rejectDrop();
		// }
		// }
		// catch (IOException io)
		// {
		// io.printStackTrace();
		// dtde.rejectDrop();
		// }
		// catch (UnsupportedFlavorException ufe)
		// {
		// ufe.printStackTrace();
		// dtde.rejectDrop();
		// }
	}
}
