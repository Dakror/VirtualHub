/*******************************************************************************
 * Copyright 2015 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.dakror.virtualhub.client.file;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;

import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class FileViewPanel extends JPanel implements DropTargetListener, DragSourceListener, DragGestureListener {
	private static final long serialVersionUID = 1L;
	
	public DropTarget dropTarget = new DropTarget(this, this);
	DragSource dragSource = DragSource.getDefaultDragSource();
	
	public FileViewPanel(LayoutManager layout) {
		super(layout);
		
		dropTarget.setActive(false);
		
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}
	
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {}
	
	@Override
	public void dragEnter(DragSourceDragEvent dsde) {}
	
	@Override
	public void dragOver(DragSourceDragEvent dsde) {}
	
	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {}
	
	@Override
	public void dragExit(DragSourceEvent dse) {}
	
	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {}
	
	@Override
	public void dragOver(DropTargetDragEvent dtde) {}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {}
	
	@Override
	public void dragExit(DropTargetEvent dte) {}
	
	@Override
	public void drop(DropTargetDropEvent dtde) {
		if (getComponentCount() == 0) {
			dtde.rejectDrop();
			return;
		}
		
		try {
			Transferable tr = dtde.getTransferable();
			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<?> fileList = (List<?>) tr.getTransferData(DataFlavor.javaFileListFlavor);
				
				File parent = new File(Assistant.getNodePath((EticetableTreeNode) Client.currentClient.frame.catalog.getSelectionPath().getLastPathComponent()));
				
				new FileMover(Client.currentClient.frame, false, dtde.getDropAction() == DnDConstants.ACTION_COPY, parent, fileList.toArray(new File[] {}));
				Client.currentClient.frame.directoryLoader.fireUpdate();
				
				dtde.getDropTargetContext().dropComplete(true);
			} else {
				CFG.p("Rejected");
				dtde.rejectDrop();
			}
		} catch (IOException io) {
			io.printStackTrace();
			dtde.rejectDrop();
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
			dtde.rejectDrop();
		}
	}
}
