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

import javax.swing.tree.DefaultMutableTreeNode;

import de.dakror.virtualhub.data.Eticet;

/**
 * @author Dakror
 */
public class EticetableTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	
	private Eticet eticet = Eticet.NONE;
	
	public EticetableTreeNode() {}
	
	public EticetableTreeNode(Object userObject) {
		super(userObject);
	}
	
	public EticetableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}
	
	public Eticet getEticet() {
		return eticet;
	}
	
	public void setEticet(Eticet eticet) {
		this.eticet = eticet;
	}
}
