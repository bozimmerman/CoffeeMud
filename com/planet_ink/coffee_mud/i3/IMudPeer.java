package com.planet_ink.coffee_mud.i3;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.i3.packets.*;
import com.planet_ink.coffee_mud.i3.persist.*;

public class IMudPeer implements PersistentPeer
{
	Object myobj=null;
	boolean isRestoring=false;
	String myID="";
	
    /**
     * Gets data about this peer from storage and gives it
     * back to the object for which this peer exists.
     * @exception imaginary.persist.PersistenceException if an error occurs during restore
     */
	public void restore() throws PersistenceException
	{
		isRestoring=true;
		if(myobj instanceof Intermud)
		{
			try{
				File F=new File("resources/ppeer."+myID);
				ObjectInputStream in=new ObjectInputStream(new FileInputStream(F));
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
     * @exception imaginary.persist.PersistenceException if a problem occurs in saving
     */
    public void save() throws PersistenceException
	{
		if(myobj instanceof Intermud)
		{
			try{
				File F=new File("resources/ppeer."+myID);
				try{F.delete();}catch(Exception e){}
				ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(F));
				out.writeObject(new Integer(((Intermud)myobj).password));
				out.writeObject(((Intermud)myobj).banned);
				out.writeObject(((Intermud)myobj).channels);
				out.writeObject(((Intermud)myobj).muds);
				out.writeObject(((Intermud)myobj).name_servers);
			}
			catch(Exception e){
				//Log.errOut("IMudPeer",e.getMessage());
			}
		}
	}

    /**
     * Assigns a persistent object to this peer for
     * persistence operations.
     * @param ob the implementation of imaginary.persist.Persistent that this is a peer for
     * @see imaginary.persist.Persistent
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
