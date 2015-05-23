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
 

package de.dakror.virtualhub.net.packet;

import java.io.File;

/**
 * @author Dakror
 */
public class Packet4Rename extends Packet {
	File o, n;
	
	public Packet4Rename(File o, File n) {
		super(4);
		
		this.o = o;
		this.n = n;
	}
	
	public Packet4Rename(byte[] data) {
		super(4);
		
		String[] parts = readData(data).split("\\[");
		o = new File(parts[0]);
		n = new File(parts[1]);
	}
	
	public File getOldFile() {
		return o;
	}
	
	public File getNewFile() {
		return n;
	}
	
	@Override
	protected byte[] getPacketData() {
		return (o.getPath().replace("\\", "/") + "[" + n.getPath().replace("\\", "/")).getBytes();
	}
	
}
