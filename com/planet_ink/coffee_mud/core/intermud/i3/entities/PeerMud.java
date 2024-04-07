package com.planet_ink.coffee_mud.core.intermud.i3.entities;

import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.coffee_mud.core.intermud.i3.net.NetPeer;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class PeerMud extends NetPeer
{
	private I3MudX mud;

	public long lastPing = System.currentTimeMillis();

	public PeerMud(final String mudName, final Socket sock)
	{
		super(sock);
		mud = new I3MudX(mudName);
	}

	public void setMud(final I3MudX mud)
	{
		this.mud = mud;
	}

	public I3MudX getMud()
	{
		return this.mud;
	}

}
