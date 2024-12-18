package com.planet_ink.coffee_mud.Common;

/*
   Copyright 2024 github.com/toasted323

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class RoomState {
	private String roomId;
	private Integer roomHash;

	public RoomState(String roomId, Integer roomHash) {
		this.roomId = roomId;
		this.roomHash = roomHash;
	}

	public String getRoomId() {
		return roomId;
	}

	public Integer getRoomHash() {
		return roomHash;
	}

	public String encode() {
		return roomId + ":" + (roomHash != null ? roomHash : "null");
	}

	public static RoomState decode(String encoded) {
		if (!canDecode(encoded)) {
			throw new IllegalArgumentException("Invalid encoded RoomState format");
		}

		String[] parts = encoded.split(":");
		String id = parts[0];
		Integer hash = "null".equals(parts[1]) ? null : Integer.parseInt(parts[1]);
		return new RoomState(id, hash);
	}

	public boolean isValid() {
		return roomId != null && !roomId.isEmpty() && (roomHash != null);
	}

	public static boolean canDecode(String encoded) {
		if (encoded == null || !encoded.contains(":")) {
			return false;
		}

		String[] parts = encoded.split(":");
		return parts.length == 2;
	}
}