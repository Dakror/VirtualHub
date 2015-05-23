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
 

package de.dakror.virtualhub.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author Dakror
 */
public class FileSelection extends Vector<File> implements Transferable {
	private static final long serialVersionUID = 1L;
	
	final static int FILE = 0;
	final static int STRING = 1;
	DataFlavor flavors[] = { DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor };
	
	public FileSelection(File... files) {
		addAll(Arrays.asList(files));
	}
	
	@Override
	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		boolean b = false;
		b |= flavor.equals(flavors[FILE]);
		b |= flavor.equals(flavors[STRING]);
		return (b);
	}
	
	@Override
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(flavors[FILE])) {
			return this;
		} else if (flavor.equals(flavors[STRING])) {
			return elementAt(0).getAbsolutePath();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}
