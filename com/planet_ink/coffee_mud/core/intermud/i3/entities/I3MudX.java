package com.planet_ink.coffee_mud.core.intermud.i3.entities;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Copyright (c)2024-2024 Bo Zimmerman
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
public class I3MudX extends I3Mud
{
	private static final long serialVersionUID = 1L;

	public String	address			= "";
	public int		password		= 0;
	public int		mudListId		= 0;
	public int		channelListId	= 0;

	public final Map<String,Integer> services = new Hashtable<String,Integer>();
	public final Map<String,String> other = new Hashtable<String,String>();

	public I3MudX(final I3MudX otherMud)
	{
		super(otherMud);
		address			= otherMud.address;
		password		= otherMud.password;
		mudListId		= otherMud.mudListId;
		channelListId	= otherMud.channelListId;
		services.putAll(otherMud.services);
		other.putAll(otherMud.other);
	}

	public I3MudX(final String mudName)
	{
		super();
		super.mud_name = mudName;
	}
}
