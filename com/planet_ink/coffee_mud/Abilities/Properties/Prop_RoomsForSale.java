package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomsForSale extends Prop_RoomForSale
{
	public String ID() { return "Prop_RoomsForSale"; }
	public String name(){ return "Putting a cluster of rooms up for sale";}
	public Environmental newInstance(){	return new Prop_RoomsForSale();}

	private void fillCluster(Room R, Vector V)
	{
		V.addElement(R);
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R2=R.getRoomInDir(d);
			if((R2!=null)&&(R2.roomID().length()>0)&&(!V.contains(R2)))
			{
				Ability A=R2.fetchEffect(ID());
				if((R2.getArea()==R.getArea())&&(A!=null))
					fillCluster(R2,V);
				else
				{
					V.removeElement(R);
					V.insertElementAt(R,0);
				}
			}
		}
	}

	public Vector getRooms()
	{
		Room R=CMMap.getRoom(landRoomID());
		if(R==null) return new Vector();
		Vector V=new Vector();
		fillCluster(R,V);
		return V;
	}


	public void updateTitle()
	{
		Vector V=getRooms();
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			LandTitle A=(LandTitle)R.fetchEffect(ID());
			if(A!=null)
			{
				A.setLandOwner(landOwner());
				A.setLandPrice(landPrice());
			}
			CMClass.DBEngine().DBUpdateRoom(R);
		}
	}

	public String landRoomID(){
		if((affected!=null)&&(affected instanceof Room))
			return CMMap.getExtendedRoomID((Room)affected);
		return "";
	}

	public void justUpdateLot(Room R, LandTitle T)
	{
		super.updateLot(R,T);
	}

	public void updateLot(Room R, LandTitle T)
	{
		if(R==null) R=CMMap.getRoom(landRoomID());
		if(R==null) return;
		if(T==null) T=getLandTitle(R);
		if(T==null) return;
		if(T instanceof Prop_RoomsForSale)
		{
			Vector V=((Prop_RoomsForSale)T).getRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R2=(Room)V.elementAt(v);
				Prop_RoomsForSale T2=(Prop_RoomsForSale)R2.fetchEffect(ID());
				if(T2!=null)
					T2.justUpdateLot(R2,T2);
			}
		}
		else
			super.updateLot(R,T);
	}
}