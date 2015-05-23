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
 

package de.dakror.virtualhub.client;

/**
 * @author Dakror
 */
public class Synchronizer extends Thread {
	ClientFrame frame;
	
	public Synchronizer() {
		frame = Client.currentClient.frame;;
		
		// start();
	}
	
	@Override
	public void run() {
		while (true) {
			// check sync if fileView
			if (frame.getSelectedTreeFile() != null) {
				// if (frame.fileView.getComponentCount() != Assistant.getLegitFileCount(frame.getSelectedTreeFile()) && frame.directoryLoader.synced) frame.directoryLoader.fireUpdate();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
