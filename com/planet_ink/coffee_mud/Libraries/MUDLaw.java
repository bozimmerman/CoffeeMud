package com.planet_ink.coffee_mud.Libraries;
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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class MUDLaw extends StdLibrary implements LegalLibrary
{
    public String ID(){return "MUDLaw";}
    
	public Law getTheLaw(Room R, MOB mob)
	{
        LegalBehavior B=getLegalBehavior(R);
	    if(B!=null)
	    {
			Area A2=getLegalObject(R.getArea());
            return B.legalInfo(A2);
	    }
	    return null;
	}
	
	public LegalBehavior getLegalBehavior(Area A)
	{
		if(A==null) return null;
		Vector V=CMLib.flags().flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0) return (LegalBehavior)V.firstElement();
        LegalBehavior B=null;
		for(Enumeration e=A.getParents();e.hasMoreElements();)
		{
		    B=getLegalBehavior((Area)e.nextElement());
		    if(B!=null) break;
		}
		return B;
	}
	public LegalBehavior getLegalBehavior(Room R)
	{
		if(R==null) return null;
		Vector V=CMLib.flags().flaggedBehaviors(R,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0) return (LegalBehavior)V.firstElement();
		return getLegalBehavior(R.getArea());
	}
	public Area getLegalObject(Area A)
	{
		if(A==null) return null;
		Vector V=CMLib.flags().flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0) return A;
	    Area A2=null;
	    Area A3=null;
		for(Enumeration e=A.getParents();e.hasMoreElements();)
		{
		    A2=(Area)e.nextElement();
		    A3=getLegalObject(A2);
		    if(A3!=null) return A3;
		}
		return A3;
	}
	public Area getLegalObject(Room R)
	{
		if(R==null) return null;
		return getLegalObject(R.getArea());
	}

    public boolean isACity(Area A)
    {
        int other=0;
        int streets=0;
        int buildings=0;
        Room R=null;
        for(Enumeration e=A.getCompleteMap();e.hasMoreElements();)
        {
            R=(Room)e.nextElement();
            if((R==null)||(R.roomID()==null)||(R.roomID().length()==0)) continue;
            if(R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
                streets++;
            else
            if((R.domainType()==Room.DOMAIN_INDOORS_METAL)
            ||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
            ||(R.domainType()==Room.DOMAIN_INDOORS_WOOD))
                buildings++;
            else
                other++;
        }
        if((streets<(other/2))||((streets+buildings)<other))
            return false;
        return true;
    }
    
	public Vector getAllUniqueTitles(Enumeration e, String owner, boolean includeRentals)
	{
	    Vector V=new Vector();
	    HashSet roomsDone=new HashSet();
	    Room R=null;
	    for(;e.hasMoreElements();)
	    {
	        R=(Room)e.nextElement();
	        LandTitle T=getLandTitle(R);
	        if((T!=null)
	        &&(!V.contains(T))
	        &&(includeRentals||(!T.rentalProperty()))
            &&((owner==null)
                ||(owner.length()==0)
                ||(owner.equals("*")&&(T.landOwner().length()>0))
                ||(T.landOwner().equals(owner))))
	        {
	            Vector V2=T.getPropertyRooms();
	            boolean proceed=true;
	            for(int v=0;v<V2.size();v++)
	            {
	                Room R2=(Room)V2.elementAt(v);
	                if(!roomsDone.contains(R2))
	                    roomsDone.add(R2);
	                else
	                    proceed=false;
	            }
	            if(proceed)
	                V.addElement(T);
	                
	        }
	    }
	    return V;
	}
	
	public LandTitle getLandTitle(Area area)
	{
		if(area==null) return null;
		for(int a=0;a<area.numEffects();a++)
		{
			Ability A=area.fetchEffect(a);
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}
	public LandTitle getLandTitle(Room room)
	{
		if(room==null) return null;
		LandTitle title=getLandTitle(room.getArea());
		if(title!=null) return title;
        Ability A=null;
		for(int a=0;a<room.numEffects();a++)
		{
			A=room.fetchEffect(a);
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}

	public boolean doesHavePriviledgesHere(MOB mob, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.landOwner()==null) return false;
		if(title.landOwner().length()==0) return false;
		if(title.landOwner().equals(mob.Name())) return true;
		if((title.landOwner().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		if(title.landOwner().equals(mob.getClanID())&&(mob.getClanRole() != Clan.POS_APPLICANT))
			return true;
		if(mob.amFollowing()!=null) 
			return doesHavePriviledgesHere(mob.amFollowing(),room);
		return false;
	}
	
	public boolean doesOwnThisProperty(String name, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.landOwner()==null) return false;
		if(title.landOwner().length()==0) return false;
		if(title.landOwner().equals(name)) return true;
		return false;
	}
    
    public Ability getClericInfusion(Environmental room)
    {
        if(room==null) return null;
        Ability A=null;
        for(int e=room.numEffects()-1;e>=0;e--)
        {
            A=room.fetchEffect(e);
            if((A!=null)&&(A.ID().startsWith("Prayer_Infuse")))
                return A;
        }
        return null;
    }
    public Deity getClericInfused(Room room)
    {
        Ability A=getClericInfusion(room);
        if(A==null) return null;
        return CMLib.map().getDeity(A.text());
    }
	
	public boolean doesOwnThisProperty(MOB mob, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.landOwner()==null) return false;
		if(title.landOwner().length()==0) return false;
		if(title.landOwner().equals(mob.Name())) return true;
		if((title.landOwner().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
		   return true;
		if(title.landOwner().equals(mob.getClanID()))
		{
			Clan C=CMLib.clans().getClan(mob.getClanID());
			if((C!=null)&&(C.allowedToDoThis(mob,Clan.FUNC_CLANPROPERTYOWNER)>=0))
				return true;
		}
		if(mob.amFollowing()!=null) 
			return doesOwnThisProperty(mob.amFollowing(),room);
		return false;
	}
	
    public boolean isLegalOfficerHere(MOB mob)
    {
    	if((mob==null)||(mob.location()==null)) return false;
    	Area A=this.getLegalObject(mob.location());
    	if(A==null) return false;
    	LegalBehavior B=this.getLegalBehavior(A);
    	if(B==null) return false;
    	return B.isAnyOfficer(A, mob);
    }
    public boolean isLegalJudgeHere(MOB mob)
    {
    	if((mob==null)||(mob.location()==null)) return false;
    	Area A=this.getLegalObject(mob.location());
    	if(A==null) return false;
    	LegalBehavior B=this.getLegalBehavior(A);
    	if(B==null) return false;
    	return B.isJudge(A, mob);
    }
    public boolean isLegalOfficialHere(MOB mob){ return isLegalOfficerHere(mob)||isLegalJudgeHere(mob);}
}
