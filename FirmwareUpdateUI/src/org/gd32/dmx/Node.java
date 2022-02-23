/**
 * @file Node.java
 *
 */
/* Copyright (C) 2022 by Arjan van Vught mailto:info@gd32-dmx.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.gd32.dmx;

public class Node {
	private static final String[] NODE_NAMES = {"Bootloader TFTP", "Art-Net", "sACN E1.31", "OSC Server"};

	private boolean isValid = false;
	private boolean isBootloader = false;
	private String ipAddress = null;
	private String nodeName = null;
	private String outputName = null;
	private String displayName = "";

	public Node(String arg) {
		super();
		System.out.println("arg [" + arg + "]");
		
		String[] values = arg.split(",");
			
		if (values.length >= 4) {		
			isValid = isValidNodeName(values[1]);
			if (isValid) {
				if (values[1].equals(NODE_NAMES[0])) {
					isValid = values[2].equals("Config") && values[3].equals("0");
					isBootloader = isValid;
				}
				if (isValid) {
					ipAddress = values[0];
					nodeName = values[1];
					outputName = values[2];
				}
			}
		}
		
		if (isValid) {
			if (values.length == 5) {
				displayName = values[4];
			} else {
				displayName = "";
			}
		}
	}
	
	private boolean isValidNodeName(String name) {
		for (int i = 0; i < NODE_NAMES.length; i++) {
			if (name.equals(NODE_NAMES[i])) {
				return true;
			}
		}
		return false;
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean isBootLoader() {
		return isBootloader;
	}
	
	public String getIpAdress() {
		return ipAddress;
	}
	
	public String getOutputName() {
		return outputName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String toString() {
		if ((ipAddress != null) && (nodeName != null)) {
			return ipAddress + " | " + nodeName ;
		}	
		return "Unknown";
	}
}
