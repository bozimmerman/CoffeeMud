package com.planet_ink.coffee_mud.core.intermud;
import com.planet_ink.coffee_mud.core.intermud.packets.*;
import com.planet_ink.coffee_mud.core.intermud.persist.*;
import com.planet_ink.coffee_mud.core.intermud.server.*;
import com.planet_ink.coffee_mud.core.intermud.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
@SuppressWarnings("unchecked")
public class IMudPeer implements PersistentPeer
{
	Object myobj=null;
	boolean isRestoring=false;
	String myID="";
	
    /**
     * Gets data about this peer from storage and gives it
     * back to the object for which this peer exists.
     * @exception com.planet_ink.coffee_mud.core.intermud.persist.PersistenceException if an error occurs during restore
     */
	public void restore() throws PersistenceException
	{
		isRestoring=true;
		if(myobj instanceof Intermud)
		{
			try{
                
				ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(new CMFile("resources/ppeer."+myID,null,false).raw()));
				Object newobj;
				newobj=in.readObject();
				if(newobj instanceof Integer)
					((Intermud)myobj).password=((Integer)newobj).intValue();
				newobj=in.readObject();
				if(newobj instanceof Hashtable)
					((Intermud)myobj).banned=(Hashtable)newobj;
				newobj=in.readObject();
				if(newobj instanceof ChannelList)
					((Intermud)myobj).channels=(ChannelList)newobj;
				newobj=in.readObject();
				if(newobj instanceof MudList)
					((Intermud)myobj).muds=(MudList)newobj;
				newobj=in.readObject();
				if(newobj instanceof Vector)
				((Intermud)myobj).name_servers=(Vector)newobj;
			}
			catch(Exception e){
				//Log.errOut("IMudPeer",e.getMessage());
			}
		}
		isRestoring=false;
	}

    /**
     * Triggers a save of its peer.  Implementing classes
     * should do whatever it takes to save the object in
     * this method.
     * @exception com.planet_ink.coffee_mud.core.intermud.persist.PersistenceException if a problem occurs in saving
     */
    public void save() throws PersistenceException
	{
		if(myobj instanceof Intermud)
		{
			try{
                ByteArrayOutputStream bout=new ByteArrayOutputStream();
				ObjectOutputStream out=new ObjectOutputStream(bout);
				out.writeObject(Integer.valueOf(((Intermud)myobj).password));
				out.writeObject(((Intermud)myobj).banned);
				out.writeObject(((Intermud)myobj).channels);
				out.writeObject(((Intermud)myobj).muds);
				out.writeObject(((Intermud)myobj).name_servers);
                out.flush();
                bout.flush();
                new CMFile("::resources/ppeer."+myID,null,false).saveRaw(bout.toByteArray());
                out.close();
                bout.close();
			}
			catch(Exception e){
				//Log.errOut("IMudPeer",e.getMessage());
			}
		}
	}

    /**
     * Assigns a persistent object to this peer for
     * persistence operations.
     * @param ob the implementation of com.planet_ink.coffee_mud.core.intermud.persist.Persistent that this is a peer for
     * @see com.planet_ink.coffee_mud.core.intermud.persist.Persistent
     */
    public void setPersistent(Persistent ob)
	{
		myobj=ob;
		myID=ob.getClass().getName().substring(ob.getClass().getName().lastIndexOf('.')+1);
	}
		

    /**
     * An implementation uses this to tell its Persistent
     * that it is in the middle of restoring.
     * @return true if a restore operation is in progress
     */
    public boolean isRestoring()
	{return isRestoring;}
}
