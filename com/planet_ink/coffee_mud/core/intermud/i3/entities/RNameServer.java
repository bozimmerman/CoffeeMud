package com.planet_ink.coffee_mud.core.intermud.i3.entities;

import java.io.Serializable;

/**
 * Copyright (c)2024-2025 Bo Zimmerman
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
public class RNameServer extends NameServer implements Serializable
{
	private static final long serialVersionUID = 1L;

	public ChannelList		channels	= new ChannelList();
	public RMudList			muds		= new RMudList();
	public int				password	= 0;

	public RNameServer(final String addr, final int p, final String nom)
	{
		super(addr, p, nom);
	}

	public RNameServer(final RNameServer other)
	{
		super(other);
		this.channels = other.channels;
		this.muds=other.muds;
		this.password=other.password;
	}
}
