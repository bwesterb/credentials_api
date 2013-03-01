/**
 * Attributes.java
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) Pim Vullers, Radboud University Nijmegen, May 2012,
 * Copyright (C) Wouter Lueks, Radboud University Nijmegen, August 2012.
 */

package org.irmacard.credentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A generic container class for attributes. Possibly this will just manage 
 * attribute id and value pairs.
 */
public class Attributes implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// TODO: provide an implementation for attribute storage.
	private Map<String, byte[]> attributes;
	
	public Attributes() {
		attributes = new HashMap<String, byte[]>();
	}
	
	public void add(String id, byte[] value) {
		attributes.put(id, value);
	}
	
	public byte[] get(String id) {
		return attributes.get(id);
	}

	public Set<String> getIdentifiers() {
		return attributes.keySet();
	}

	public void print() {
		for(String k : attributes.keySet() ) {
			System.out.println(k + ": " + new String(get(k)));
		}
	}
	
	public String toString() {
		String res = "[";
		for(String k : attributes.keySet() ) {
			res += k + ": " + new String(get(k)) + ", ";
		}
		res += "]";
		return res;
	}
}
