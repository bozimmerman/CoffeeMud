package com.planet_ink.coffee_mud.Behaviors;
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

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class Nanny extends StdBehavior
{
    public String ID(){return "Nanny";}
    protected int canImproveCode(){return Behavior.CAN_MOBS;}
    protected boolean watchesBabies=true;
    protected boolean watchesChildren=true;
    protected boolean watchesMounts=false;
    protected boolean watchesWagons=false;
    protected boolean watchesCars=false;
    protected boolean watchesBoats=false;
    protected boolean watchesAirCars=false;
    protected boolean watchesMOBFollowers=false;
    protected String place="Nursery";
    
    protected DVector dropOffs=null;//new DVector(2); //1=mob/baby, 2=owner
    protected DVector payments=new DVector(2); //1=payer, 2=amount paid
    // dynamic list of who belongs to what, before they leave
    // and get added to official drop-offs.
    protected DVector associations=new DVector(2);
    
    

    public Environmental getDroppedOffObjIfAny(Environmental E)
    {
    	if(E==null) return null;
    	if(dropOffs.contains(E)) return E;
    	if(E instanceof Container)
    	{
    		Vector V=((Container)E).getContents();
        	Environmental E2=null;
    		for(int v=0;v<V.size();v++)
    		{
    			E2=(Environmental)V.elementAt(v);
    			E2=getDroppedOffObjIfAny(E2);
    			if(E2!=null) return E2;
    		}
    	}
    	return null;
    }
    
    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if(!super.okMessage(host,msg))
            return false;
        if(dropOffs==null) return true;
        int targMinor=msg.targetMinor();
        if((msg.target()==host)
        &&(targMinor==CMMsg.TYP_GIVE))
        {
        	
        }
        else
        if(((targMinor==CMMsg.TYP_GET)
            ||(targMinor==CMMsg.TYP_PULL)
            ||(targMinor==CMMsg.TYP_PUSH)
            ||(targMinor==CMMsg.TYP_CAST_SPELL))
        &&(msg.target() instanceof Item)
        &&((msg.targetMessage()==null)||(!msg.targetMessage().equalsIgnoreCase("GIVE")))
        &&(getDroppedOffObjIfAny(msg.target()))!=null)
        {
        	if((msg.source().location()==CMLib.map().roomLocation(host))
        	&&(host instanceof MOB))
				msg.addTrailerMsg(CMClass.getMsg((MOB)host,msg.source(),CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'I'm afraid that "+place+" fees are still owed on "+msg.target().name()+"' to <T-NAME> ^?"));
        	else
        		msg.source().tell("You'll need to pay first.");
        		
        }
        else
        if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS)
        	||CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
        &&((getDroppedOffObjIfAny(msg.target())!=null)||(msg.target()==host)||(msg.target()==CMLib.map().roomLocation(host))))
        {
        	if(msg.source()!=host)
        	{
	        	if(host instanceof MOB)
					msg.addTrailerMsg(CMClass.getMsg((MOB)host,msg.source(),CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Not in my "+place+" you dont!' to <T-NAME> ^?"));
	        	else
	        		msg.source().tell("You can't do that here.");
        	}
			return false;
        }
        else
		if((msg.sourceMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target()==CMLib.map().roomLocation(host)))
		{
			if(getDroppedOffObjIfAny(msg.source())!=null)
			{
				if((msg.tool() instanceof Ability)
				&&(msg.source().location()!=null))
				{
					Room R=CMLib.map().roomLocation(host);
					boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
					boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
					boolean shere=(msg.source().location()==R);
					if((!shere)&&((summon)||(teleport)))
					{
						if((msg.source().location()!=null)&&(msg.source().location()!=R))
							msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
						R.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
						return false;
					}
				}
			}
		}
        return true;
    }

    public boolean isMount(Environmental E)
    {
		if((E instanceof MOB)
		&&(E instanceof Rideable)
		&&((((Rideable)E).rideBasis()==Rideable.RIDEABLE_LAND)
				||(((Rideable)E).rideBasis()==Rideable.RIDEABLE_AIR)
				||(((Rideable)E).rideBasis()==Rideable.RIDEABLE_WATER)))
			return true;
		return false;
    }
    
    public boolean isDropOffable(Environmental E)
    {
    	if(E==null) return false;
    	if((E instanceof MOB)&&(!((MOB)E).isMonster())) return false;
		if((watchesBabies)&&(CMLib.flags().isBaby(E))) return true;
		if((watchesChildren)&&(CMLib.flags().isChild(E))&&(!CMLib.flags().isBaby(E))) return true;
		if((watchesMounts)&&(isMount(E))) return true;
		if((watchesMOBFollowers)&&(E instanceof MOB)&&(!isMount(E))&&(!CMLib.flags().isChild(E))&&(!CMLib.flags().isBaby(E)))
			return true;
		if((this.watchesWagons)
		&&(E instanceof Rideable)
		&&(((Rideable)E).rideBasis()==Rideable.RIDEABLE_WAGON))
			return true;
		if((this.watchesCars)
		&&(E instanceof Item)
		&&(E instanceof Rideable)
		&&(((Rideable)E).rideBasis()==Rideable.RIDEABLE_LAND))
			return true;
		if((this.watchesBoats)
		&&(E instanceof Item)
		&&(E instanceof Rideable)
		&&(((Rideable)E).rideBasis()==Rideable.RIDEABLE_WATER))
			return true;
		if((this.watchesAirCars)
		&&(E instanceof Item)
		&&(E instanceof Rideable)
		&&(((Rideable)E).rideBasis()==Rideable.RIDEABLE_AIR))
			return true;
    	return false;
    }

    public MOB ultimateFollowing(Environmental E)
    {
        MOB ultimateFollowing=null;
        if(E instanceof MOB) ultimateFollowing=((MOB)E).amFollowing();
        while((ultimateFollowing!=null)&&(ultimateFollowing.amFollowing()!=null))
            ultimateFollowing=ultimateFollowing.amFollowing();
        return ultimateFollowing;
    }
    
    public MOB getAssociateWith(Environmental E)
    {
    	if((E instanceof Item)
    	&&(((Item)E).owner() instanceof MOB)
    	&&(!((MOB)((Item)E).owner()).isMonster()))
    		return (MOB)((Item)E).owner();
    	if((E instanceof MOB)
    	&&(((MOB)E).amFollowing()!=null)
    	&&(!((MOB)E).amFollowing().isMonster()))
    		return ((MOB)E).amFollowing();
    	if((E instanceof MOB)
    	&&(ultimateFollowing(E)!=null)
    	&&(!ultimateFollowing(E).isMonster()))
    		return ultimateFollowing(E);
    	if(E instanceof Rideable)
    	{
    		Rideable R=(Rideable)E;
    		Environmental E2=null;
    		for(int r=0;r<R.numRiders();r++)
    		{
    			E2=R.fetchRider(r);
    			if((E2 instanceof MOB)
    			&&(!((MOB)E2).isMonster()))
    				return (MOB)E2;
    		}
    	}
    	if((E instanceof Rider)
    	&&(((Rider)E).riding()!=null))
    		return getAssociateWith(((Rider)E).riding());
    	return null;
    }

    
    public void addAssociationsIfNecessary(HashSet H)
    {
    	Environmental E=null;
    	for(Iterator i=H.iterator();i.hasNext();)
    	{
    		E=(Environmental)i.next();
    		if((E instanceof Rider)&&(((Rider)E).riding()!=null)&&(!H.contains(((Rider)E).riding())))
    			H.add(E);
    	}
    	for(Iterator i=H.iterator();i.hasNext();)
    	{
    		E=(Environmental)i.next();
    		if((isDropOffable(E))&&(!associations.contains(E)))
    		{
    			MOB source=getAssociateWith(E);
    			if(source!=null)
	    			associations.addElement(E,source);
    		}
			if(E instanceof MOB)
			{
				MOB mob=(MOB)E;
				for(int t=0;t<mob.inventorySize();t++)
				{
					Item I=mob.fetchInventory(t);
		    		if(isDropOffable(I)&&(!associations.contains(I)))
		    		{
		    			MOB source=getAssociateWith(I);
		    			if(source!=null)
			    			associations.addElement(I,source);
		    		}
				}
			}
    	}
    }
    
    public void executeMsg(Environmental host, CMMsg msg)
    {
        super.executeMsg(host,msg);
        if(dropOffs==null) return;
        
        if((msg.targetMinor()==CMMsg.TYP_ENTER)
        &&(msg.target()==CMLib.map().roomLocation(host)))
        {
        	HashSet H=msg.source().getGroupMembers(new HashSet());
        	msg.source().getRideBuddies(H);
        	if(!H.contains(msg.source())) H.add(msg.source());
        	addAssociationsIfNecessary(H);
        	// now give the news to the new people
        	
        	// afterwards, look for old-dropoffs to ask for money regarding
        	
        }
        else
        if((msg.target()==host)
        &&(msg.targetMinor()==CMMsg.TYP_GIVE)
        &&(msg.tool() instanceof Coins))
        {
        	// accept payments -- REMOVE from dropoffs if they pay me off!
        	// dont forget to tell them that ill stop watching after!
        }
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((dropOffs==null)&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)))
        {
        	// parse the parms!
        }
        // now handle diaper changes
        // and play with any kids in the room
        // comb any horses, etc.
        
        return true;
    }
}