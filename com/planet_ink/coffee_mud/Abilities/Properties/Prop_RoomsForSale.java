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

	public Vector getPropertyRooms()
	{
		Vector V=new Vector();
		Room R=null;
		if(affected instanceof Room)
			R=(Room)affected;
		else
			R=CMMap.getRoom(landPropertyID());
		if(R!=null)	fillCluster(R,V);
		return V;
	}


	// update title, since it may affect room clusters, worries about EVERYONE
	public void updateTitle()
	{
		Vector V=getPropertyRooms();
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			LandTitle A=(LandTitle)R.fetchEffect(ID());
			if((A!=null)
			&&((!A.landOwner().equals(landOwner()))
			   ||(A.landPrice()!=landPrice())))
			{
				A.setLandOwner(landOwner());
				A.setLandPrice(landPrice());
				CMClass.DBEngine().DBUpdateRoom(R);
			}
		}
	}

	// update lot, since its called for all rooms by savethread, ONLY worries about itself
	public void updateLot()
	{
		if(affected instanceof Room)
			lastItemNums=updateLotWithThisData((Room)affected,this,false,lastItemNums);
	}
}