package com.planet_ink.coffee_mud.core.intermud.i3.entities;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.io.Serializable;

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
public class RMudList extends MudList implements Serializable
{
	private static final long serialVersionUID = 1L;

	public void addMud(final I3RMud mud)
	{
		super.addMud(mud);
	}

	@Override
	public void removeMud(final I3Mud mud)
	{
		super.removeMud(mud);
	}

	@Override
	public I3RMud getMud(final String mud)
	{
		final I3Mud mud1 = super.getMud(mud);
		if(mud1 instanceof I3RMud)
			return (I3RMud)mud1;
		return null;
	}

	private static final Converter<I3Mud,I3RMud> conv = new Converter<I3Mud,I3RMud>()
	{

		@Override
		public I3RMud convert(final I3Mud obj)
		{
			if(obj instanceof I3RMud)
				return (I3RMud)obj;
			return null;
		}

	};

	public Collection<I3RMud> getMudXList()
	{
		return new ConvertingCollection<I3Mud,I3RMud>(super.list.values(),conv);
	}
}
