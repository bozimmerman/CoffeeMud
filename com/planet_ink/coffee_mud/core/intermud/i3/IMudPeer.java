package com.planet_ink.coffee_mud.core.intermud.i3;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
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

import java.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
public class IMudPeer implements PersistentPeer
{
	Object myobj=null;
	boolean isRestoring=false;
	String myID="";

	/**
	 * Gets data about this peer from storage and gives it
	 * back to the object for which this peer exists.
	 * @exception com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException if an error occurs during restore
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void restore() throws PersistenceException
	{
		isRestoring=true;
		if(myobj instanceof I3Client)
		{
			try
			{
				final CMFile F=new CMFile("resources/ppeer."+myID,null);
				if(!F.exists())
					return;

				final ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(F.raw()));
				Integer passI = null;
				Hashtable<String,String> banned = null;
				ChannelList chanL = null;
				MudList mudL = null;
				Object newobj;
				newobj=in.readObject();
				if(newobj instanceof Integer)
					passI=(Integer)newobj;
				newobj=in.readObject();
				if(newobj instanceof Hashtable)
					banned=(Hashtable<String,String>)newobj;
				newobj=in.readObject();
				if(newobj instanceof ChannelList)
					chanL=(ChannelList)newobj;
				newobj=in.readObject();
				if(newobj instanceof MudList)
					mudL=(MudList)newobj;
				newobj=in.readObject();
				if(newobj instanceof List)
				{
					final List<NameServer> nlist = (List<NameServer>)newobj;
					final List<NameServer> olist = ((I3Client)myobj).name_servers;
					if(olist.size() != nlist.size())
						return;
					for(final NameServer o : olist)
						if(!nlist.contains(o))
							return;
					for(final NameServer o : nlist)
						if(!olist.contains(o))
							return;
				}
				if(passI != null)
					((I3Client)myobj).password = passI.intValue();
				if(banned != null)
					((I3Client)myobj).banned = banned;
				if(chanL != null)
					((I3Client)myobj).channels = chanL;
				if(mudL != null)
					((I3Client)myobj).muds = mudL;
			}
			catch(final Exception e)
			{
				Log.errOut("IMudPeer","Error reading /resources/ppeer."+myID+": "+e.getMessage());
			}
		}
		isRestoring=false;
	}

	/**
	 * Triggers a save of its peer.  Implementing classes
	 * should do whatever it takes to save the object in
	 * this method.
	 * @exception com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException if a problem occurs in saving
	 */
	@Override
	public void save() throws PersistenceException
	{
		if(myobj instanceof I3Client)
		{
			try
			{
				final ByteArrayOutputStream bout=new ByteArrayOutputStream();
				final ObjectOutputStream out=new ObjectOutputStream(bout);
				out.writeObject(Integer.valueOf(((I3Client)myobj).password));
				out.writeObject(((I3Client)myobj).banned);
				out.writeObject(((I3Client)myobj).channels);
				out.writeObject(((I3Client)myobj).muds);
				out.writeObject(((I3Client)myobj).name_servers);
				out.flush();
				bout.flush();
				new CMFile("::resources/ppeer."+myID,null).saveRaw(bout.toByteArray());
				out.close();
				bout.close();
			}
			catch(final Exception e)
			{
				Log.errOut("IMudPeer",e.getMessage());
			}
		}
	}

	/**
	 * Assigns a persistent object to this peer for
	 * persistence operations.
	 * @param ob the implementation of com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent that this is a peer for
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent
	 */
	@Override
	public void setPersistent(final Persistent ob)
	{
		myobj=ob;
		myID=ob.getClass().getName().substring(ob.getClass().getName().lastIndexOf('.')+1);
	}

	/**
	 * An implementation uses this to tell its Persistent
	 * that it is in the middle of restoring.
	 * @return true if a restore operation is in progress
	 */
	@Override
	public boolean isRestoring()
	{
		return isRestoring;
	}
}
