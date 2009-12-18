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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

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
    protected boolean changedSinceLastSave=false;
    protected String place="Nursery";
    protected double hourlyRate=1.0; 
    
    protected DVector dropOffs=null;//new DVector(3); //1=mob/baby, 2=owner, 3=time
    protected DVector payments=new DVector(2); //1=payer, 2=amount paid
    // dynamic list of who belongs to what, before they leave
    // and get added to official drop-offs.
    protected DVector associations=new DVector(2);
    protected DVector sayLaters=new DVector(2);
    
    public double getPaidBy(MOB mob)
    {
    	if(mob==null) return 0.0;
    	double amt=0.0;
    	for(int d=0;d<payments.size();d++)
    	{
    		if(payments.elementAt(d,1)==mob)
    			amt+=((Double)payments.elementAt(d,2)).doubleValue();
    	}
    	return amt;
    }
    
    public void addPayment(MOB mob,double amt)
    {
    	if(mob==null) return;
    	for(int d=0;d<payments.size();d++)
    	{
    		if(payments.elementAt(d,1)==mob)
    		{
    			amt+=((Double)payments.elementAt(d,2)).doubleValue();
    			payments.setElementAt(d,2,Double.valueOf(amt));
    			return;
    		}
    	}
    	payments.addElement(mob,Double.valueOf(amt));
    }
    
    public void clearTheSlate(MOB mob)
    {
    	if(mob==null) return;
    	for(int d=payments.size()-1;d>=0;d--)
    	{
    		if(payments.elementAt(d,1)==mob)
    			payments.removeElementAt(d);
    	}
    	for(int d=dropOffs.size()-1;d>=0;d--)
    	{
    		if(dropOffs.elementAt(d,2)==mob)
    		{
    			if(!associations.contains(dropOffs.elementAt(d,1)))
    				associations.addElement(dropOffs.elementAt(d,1),mob);
    			dropOffs.removeElementAt(d);
    			changedSinceLastSave=true;
    		}
    	}
    }
    
    public double getAllOwedBy(MOB mob)
    {
    	if(mob==null) return 0.0;
    	Room R=mob.location();
    	if(R==null) return 0.0;
    	Area A=R.getArea();
    	if(A==null) return 0.0;
    	double amt=0.0;
    	for(int d=0;d<dropOffs.size();d++)
    	{
    		if(dropOffs.elementAt(d,2)==mob)
    		{
    			Long time=(Long)dropOffs.elementAt(d,3);
    			long t=System.currentTimeMillis()-time.longValue();
    			t=Math.round(Math.ceil(CMath.div(t,Tickable.TIME_MILIS_PER_MUDHOUR)));
    			if(t>0) amt+=(t*hourlyRate);
    		}
    	}
    	return amt;
    }

    public Vector getAllOwedFor(MOB mob)
    {
    	Vector V=new Vector();
    	if(mob!=null)
    	for(int d=0;d<dropOffs.size();d++)
    		if(dropOffs.elementAt(d,2)==mob)
        		V.addElement(dropOffs.elementAt(d,1));
    	return V;
    }
    
    public String getPronoun(Vector V)
    {
    	if(V.size()==0) return "your stuff";
    	int babies=0;
    	int friends=0;
    	int objects=0;
    	int mounts=0;
    	for(int v=0;v<V.size();v++)
    	{
    		Environmental E=(Environmental)V.elementAt(v);
    		if(CMLib.flags().isBaby(E)||CMLib.flags().isChild(E))
    			babies++;
    		else
    		if(isMount(E))
    			mounts++;
    		else
    		if(E instanceof Item)
    			objects++;
    		else
    			friends++;
    	}
    	Vector pros=new Vector();
    	if(babies>0) pros.addElement("little one"+((babies>1)?"s":""));
    	if(mounts>0) pros.addElement("mount"+((babies>1)?"s":""));
    	if(friends>0) pros.addElement("friend"+((babies>1)?"s":""));
    	if(objects>0) pros.addElement("thing"+((babies>1)?"s":""));
    	StringBuffer list=new StringBuffer("");
    	for(int p=0;p<pros.size();p++)
    	{
    		list.append((String)pros.elementAt(p));
			if((pros.size()>1)&&(p==pros.size()-2))
				list.append(", and ");
			else
			if(pros.size()>1)
				list.append(", ");
    	}
    	return list.toString().trim();
    }
    
    public String getOwedFor(String currency, Environmental E)
    {
    	for(int d=0;d<dropOffs.size();d++)
    	{
    		if(dropOffs.elementAt(d,1)==E)
    		{
    			Long time=(Long)dropOffs.elementAt(d,3);
    			long t=System.currentTimeMillis()-time.longValue();
    			t=Math.round(Math.floor(CMath.div(t,Tickable.TIME_MILIS_PER_MUDHOUR)));
    			if(t>0) return CMLib.beanCounter().abbreviatedPrice(currency, (t+hourlyRate))+" for watching "+E.name();
    		}
    	}
    	return "";
    }
    
    public String getAllOwedBy(String currency, MOB mob)
    {
    	if(mob==null) return "";
    	Room R=mob.location();
    	if(R==null) return "";
    	Area A=R.getArea();
    	if(A==null) return "";
    	StringBuffer owed=new StringBuffer("");
    	Environmental E=null;
    	for(int d=0;d<dropOffs.size();d++)
    	{
    		E=(Environmental)dropOffs.elementAt(d,1);
    		if(dropOffs.elementAt(d,2)==mob)
    		{
    			Long time=(Long)dropOffs.elementAt(d,3);
    			long t=System.currentTimeMillis()-time.longValue();
    			t=Math.round(Math.ceil(CMath.div(t,Tickable.TIME_MILIS_PER_MUDHOUR)));
    			if(t>0) owed.append(CMLib.beanCounter().abbreviatedPrice(currency, (t*hourlyRate))+" for "+E.name()+", ");
    		}
    	}
    	String s=owed.toString();
    	if(s.endsWith(", "))s=s.substring(0,s.length()-2);
    	return s;
    }
    
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
        	if(isDropOffable(msg.tool()))
        	{
        		String pronoun=this.getPronoun(CMParms.makeVector(msg.tool()));
        		msg.source().tell(msg.source(),host,msg.tool(),"<T-NAME> won't accept <O-NAME>.  You should probably leave your "+pronoun+" here.");
        		return false;
        	}
        	if(msg.tool() instanceof Coins)
        	{
        		Coins C=(Coins)msg.tool();
        		String myCurrency=CMLib.beanCounter().getCurrency(host);
        		if(!C.getCurrency().equalsIgnoreCase(myCurrency))
        		{
        			if(host instanceof MOB)
						CMLib.commands().postSay((MOB)host,msg.source(),"I'm don't accept "+CMLib.beanCounter().getDenominationName(C.getCurrency(),C.getDenomination())+".  I can only accept "+CMLib.beanCounter().getDenominationName(myCurrency)+".");
        			else
        				msg.source().tell("The "+place+" doesn't accept "+CMLib.beanCounter().getDenominationName(C.getCurrency(),C.getDenomination())+".  It only accepts "+CMLib.beanCounter().getDenominationName(myCurrency)+".");
        			return false;
        		}
        	}
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
			Environmental obj=getDroppedOffObjIfAny(msg.target());
    		String amt=getOwedFor(CMLib.beanCounter().getCurrency(host),obj);
        	if((msg.source().location()==CMLib.map().roomLocation(host))
        	&&(host instanceof MOB))
        	{
        		if(amt.length()>0)
					CMLib.commands().postSay((MOB)host,msg.source(),"I'm afraid that "+place+" fees of "+amt+" are still owed.");
        		else
        			CMLib.commands().postSay((MOB)host,msg.source(),"I'm afraid that "+place+" fees are still owed on "+obj.name()+".");
        	}
        	else
    		if(amt.length()>0)
        		msg.source().tell("You'll need to pay "+place+" fees of "+amt+" first.");
    		else
        		msg.source().tell("You'll need to pay your "+place+" fees  for "+obj.name()+" first.");
        	return false;
        }
        else
        if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS)
        	||CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
        &&((getDroppedOffObjIfAny(msg.target())!=null)||(msg.target()==host)||(msg.target()==CMLib.map().roomLocation(host))))
        {
        	if(msg.source()!=host)
        	{
	        	if((host instanceof MOB)&&(msg.source().location()==CMLib.map().roomLocation(host)))
	        	{
					CMLib.commands().postSay((MOB)host,msg.source(),"Not in my "+place+" you dont!");
					MOB victim=msg.source().getVictim();
					if(victim!=null) victim.makePeace();
					msg.source().makePeace();
	        	}
	        	else
	        		msg.source().tell("You can't do that here.");
        	}
			return false;
        }
        else
		if((msg.sourceMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target()==CMLib.map().roomLocation(host)))
		{
			Environmental obj=getDroppedOffObjIfAny(msg.source());
			if(obj!=null)
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
				else
				{
		    		String amt=getOwedFor(CMLib.beanCounter().getCurrency(host),obj);
		        	if((msg.source().location()==CMLib.map().roomLocation(host))
		        	&&(host instanceof MOB))
		        	{
		        		if(amt.length()>0)
							CMLib.commands().postSay((MOB)host,msg.source(),"I'm afraid that "+place+" fees of "+amt+" are still owed on "+obj.name()+".");
		        		else
		        			CMLib.commands().postSay((MOB)host,msg.source(),"I'm afraid that "+place+" fees are still owed on "+obj.name()+".");
		        	}
		        	else
		    		if(amt.length()>0)
		        		msg.source().tell("You'll need to pay "+place+" fees of "+amt+" for "+obj.name()+" first.");
		    		else
		        		msg.source().tell("You'll need to pay your "+place+" fees  for "+obj.name()+" first.");
				}
				return false;
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
        if(E instanceof MOB) 
            ultimateFollowing=((MOB)E).amUltimatelyFollowing();
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

    public Vector myCurrentAssocs(MOB mob)
    {
    	Vector V=new Vector();
    	if(mob!=null)
    	for(int a=0;a<associations.size();a++)
    	{
    		if(associations.elementAt(a,2)==mob)
    			V.add(associations.elementAt(a,1));
    	}
    	return V;
    }
    
    public void executeMsg(Environmental host, CMMsg msg)
    {
        super.executeMsg(host,msg);
        if(dropOffs==null) return;
        
        if((msg.targetMinor()==CMMsg.TYP_ENTER)
        &&(msg.target()==CMLib.map().roomLocation(host)))
        {
			String currency=CMLib.beanCounter().getCurrency(host);
        	HashSet H=msg.source().getGroupMembers(new HashSet());
            msg.source().getRideBuddies(H);
            if(!H.contains(msg.source())) H.add(msg.source());
        	HashSet H2 = null;
        	do {
                H2 = (HashSet)H.clone();
            	for(Iterator i = H2.iterator(); i.hasNext(); ) {
            	    Environmental E = (Environmental)i.next();
            	    if(E instanceof Rideable)
            	    {
            	        Rideable R = (Rideable)E;
            	        for(int r = 0; r<R.numRiders(); r++)
            	            if(!H.contains(R.fetchRider(r)))
            	                H.add(R.fetchRider(r));
            	    }
            	}
        	} while(H.size() > H2.size());
        	
        	addAssociationsIfNecessary(H);
    		Vector myAssocs=myCurrentAssocs(msg.source());
    		StringBuffer list=new StringBuffer("");
    		for(int m=0;m<myAssocs.size();m++)
        	{
    			list.append(((Environmental)myAssocs.elementAt(m)).name());
    			if((myAssocs.size()>1)&&(m==myAssocs.size()-2))
    				list.append(", and ");
    			else
    			if(myAssocs.size()>1)
    				list.append(", ");
        	}
    		if(list.length()>0)
    			sayLaters.addElement(msg.source(),"Welcome to my "+place+", "+msg.source().name()+"! You are welcome to leave " +
							list.toString()+" here under my care and protection.  Be aware that I charge "
							+CMLib.beanCounter().abbreviatedPrice(currency,hourlyRate)+" per hour, each.  " +
							"No payment is due until you return to fetch your "+getPronoun(myAssocs)+".");
        	
    		double owed=getAllOwedBy(msg.source());
    		double paid=getPaidBy(msg.source());
    		if(owed>0)
    		{
    			Vector myStuff=getAllOwedFor(msg.source());
    			String pronoun=getPronoun(myStuff);
    			sayLaters.addElement(msg.source(),"Welcome back, "+msg.source().name()+"! If are here for your "+pronoun
								+", the total bill is: "+getAllOwedBy(currency, msg.source())
								+" ("+CMLib.beanCounter().abbreviatedPrice(currency,owed-paid)+"). "
								+"You can just give me the money to settle the bill.");
    		}
        }
        else
        if((msg.target()==host)
        &&(msg.targetMinor()==CMMsg.TYP_GIVE)
        &&(msg.tool() instanceof Coins))
        {
        	addPayment(msg.source(),((Coins)msg.tool()).getTotalValue());
        	double owed=getAllOwedBy(msg.source());
        	double paid=getPaidBy(msg.source());
			String currency=CMLib.beanCounter().getCurrency(host);
        	if((paid>owed)&&(host instanceof MOB))
        	{
	        	double change=paid-owed;
	            Coins C=CMLib.beanCounter().makeBestCurrency(currency,change);
	            MOB source=msg.source();
				if((change>0.0)&&(C!=null))
				{
	                // this message will actually end up triggering the hand-over.
					CMMsg newMsg=CMClass.getMsg((MOB)host,source,C,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Heres your change.' to <T-NAMESELF>.^?");
	                C.setOwner((MOB)host);
	                long num=C.getNumberOfCoins();
	                String curr=C.getCurrency();
	                double denom=C.getDenomination();
	                C.destroy();
	                C.setNumberOfCoins(num);
	                C.setCurrency(curr);
	                C.setDenomination(denom);
					msg.addTrailerMsg(newMsg);
				}
				else
					CMLib.commands().postSay((MOB)host,source,"Gee, thanks. :)",true,false);
        	}
            ((Coins)msg.tool()).destroy();
            if(paid>=owed)
            {
            	Vector V=this.getAllOwedFor(msg.source());
            	Environmental E=null;
            	for(int v=0;v<V.size();v++)
            	{
            		E=(Environmental)V.elementAt(v);
            		if(E instanceof MOB)
            		{
            			CMLib.commands().postFollow((MOB)E,msg.source(),false);
            			if(CMath.bset(((MOB)E).getBitmap(), MOB.ATT_AUTOGUARD))
            			    ((MOB)E).setBitmap(CMath.unsetb(((MOB)E).getBitmap(), MOB.ATT_AUTOGUARD));
            			if(((MOB)E).amFollowing()!=msg.source())
            			{
        					CMLib.commands().postSay((MOB)host,msg.source(),"Hmm, '"+E.name()+"' doesn't seem ready to leave.  Now get along!",true,false);
        					msg.source().location().send((MOB)E,CMClass.getMsg((MOB)E,msg.source(),null,CMMsg.MSG_FOLLOW|CMMsg.MASK_ALWAYS,"<S-NAME> follow(s) <T-NAMESELF>."));
                			if(((MOB)E).amFollowing()!=msg.source())
	        					((MOB)E).setFollowing(msg.source());
            			}
            		}
            	}
            	clearTheSlate(msg.source());
            	sayLaters.addElement(msg.source(),"Thanks, come again!");
            }
            else
            	sayLaters.addElement(msg.source(),"Thanks, but you still owe "+CMLib.beanCounter().abbreviatedPrice(currency,owed-paid)+".");
        }
        else
        if((msg.source()==host)
        &&(msg.targetMinor()==CMMsg.TYP_SPEAK)
        &&(msg.target() instanceof MOB)
        &&(msg.tool() instanceof Coins)
        &&(((Coins)msg.tool()).amDestroyed())
        &&(!msg.source().isMine(msg.tool()))
        &&(!((MOB)msg.target()).isMine(msg.tool())))
            CMLib.beanCounter().giveSomeoneMoney(msg.source(),(MOB)msg.target(),((Coins)msg.tool()).getTotalValue());
    }
    
    public int getNameIndex(Vector V, String name)
    {
    	int index=0;
    	for(int v=0;v<V.size();v++)
    	{
    		if(((String)V.elementAt(v)).equals(name))
    			index++;
    	}
    	return index;
    }
    
    public String getParms()
    {
    	StringBuffer parms=new StringBuffer("");
    	parms.append("RATE="+hourlyRate+" ");
    	parms.append("NAME=\""+place+"\" ");
    	parms.append("WATCHES=\"");
    	if(watchesBabies) parms.append("Babies,");
    	if(watchesChildren) parms.append("Children,");
    	if(watchesMounts) parms.append("Mounts,");
    	if(watchesWagons) parms.append("Wagons,");
    	if(watchesCars) parms.append("Cars,");
    	if(watchesBoats) parms.append("Boats,");
    	if(watchesAirCars) parms.append("AirCars,");
    	if(watchesMOBFollowers) parms.append("Followers,");
    	parms.append("\"");
    	if(dropOffs!=null)
    	{
	    	parms.append(" |~| ");
	    	Vector oldNames=new Vector();
	    	Environmental E=null;
	    	MOB owner=null;
	    	Long time=null;
	    	String eName=null;
	    	String oName=null;
	    	for(int d=0;d<dropOffs.size();d++)
	    	{
	    		parms.append("<DROP>");
	    		E=(Environmental)dropOffs.elementAt(d,1);
	    		owner=(MOB)dropOffs.elementAt(d,2);
	    		time=(Long)dropOffs.elementAt(d,3);
	    		eName=E.Name();
	    		oName=owner.Name();
	    		if(oldNames.contains(eName))
	    			eName=getNameIndex(oldNames,eName)+"."+eName;
	    		parms.append(CMLib.xml().convertXMLtoTag("ENAM",CMLib.xml().parseOutAngleBrackets(eName)));
	    		parms.append(CMLib.xml().convertXMLtoTag("ONAM",CMLib.xml().parseOutAngleBrackets(oName)));
	    		parms.append(CMLib.xml().convertXMLtoTag("TIME",time.longValue()));
	    		parms.append("</DROP>");
	    	}
    	}
    	return parms.toString().trim();
    }

    public void setParms(String parms)
    {
		super.setParms(parms);
    	int x=super.parms.indexOf("|~|");
    	if(x>0) dropOffs=null;
    	hourlyRate=CMParms.getParmDouble(parms,"RATE",2.0);
    	place=CMParms.getParmStr(parms,"NAME","nursery");
    	Vector watches=CMParms.parseCommas(CMParms.getParmStr(parms,"WATCHES","Babies,Children").toUpperCase(),true);
    	String watch=null;
    	watchesBabies=false;
    	watchesChildren=false;
    	watchesMounts=false;
    	watchesWagons=false;
    	watchesCars=false;
    	watchesBoats=false;
    	watchesAirCars=false;
    	watchesMOBFollowers=false;
    	for(int w=0;w<watches.size();w++)
    	{
    		watch=(String)watches.elementAt(w);
	    	if(watch.startsWith("BAB")) watchesBabies=true;
	    	if(watch.startsWith("CHI")) watchesChildren=true;
	    	if(watch.startsWith("MOU")) watchesMounts=true;
	    	if(watch.startsWith("WAG")) watchesWagons=true;
	    	if(watch.startsWith("CAR")) watchesCars=true;
	    	if(watch.startsWith("BOA")) watchesBoats=true;
	    	if(watch.startsWith("AIR")) watchesAirCars=true;
	    	if(watch.startsWith("FOL")) watchesMOBFollowers=true;
    	}
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
        ||((!(ticking instanceof Environmental))))
        	return true;
        if(dropOffs==null)
        {
        	int x=super.parms.indexOf("|~|");
    		dropOffs=new DVector(3);
        	if(x>0)
        	{
        		String codes=super.parms.substring(x+3);
        		parms=parms.substring(0,3);
        		if(codes.trim().length()>0)
        		{
	        		Vector V=CMLib.xml().parseAllXML(codes);
	        		XMLLibrary.XMLpiece P=null;
	        		Hashtable parsedPlayers=new Hashtable();
	    	    	long time=0;
	    	    	String eName=null;
	    	    	Environmental E=null;
	    	    	String oName=null;
	    	    	Room R=CMLib.map().roomLocation((Environmental)ticking);
	    	    	MOB O=null;
	        		if((V!=null)&&(R!=null))
	        		for(int v=0;v<V.size();v++)
	        		{
	        			P=((XMLLibrary.XMLpiece)V.elementAt(v));
	        			if((P!=null)&&(P.contents!=null)&&(P.contents.size()==3)&&(P.tag.equalsIgnoreCase("DROP")))
	        			{
	        				eName=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(P.contents,"ENAM"));
	        				oName=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(P.contents,"ONAM"));
	        				time=CMLib.xml().getLongFromPieces(P.contents,"TIME");
	        				if(parsedPlayers.get(oName) instanceof MOB)
	        					O=(MOB)parsedPlayers.get(oName);
	        				else
	        				if(parsedPlayers.get(oName) instanceof String)
	        					continue;
	        				else
	        				{
	        					O=CMLib.players().getLoadPlayer(oName);
	        					if(O==null) 
	        						parsedPlayers.put(oName,"");
	        					else
	        						parsedPlayers.put(oName,O);
	        				}
	        				E=R.fetchInhabitant(eName);
	        				if(E==null) E=R.fetchAnyItem(eName);
	        				if(E==null)
		        				Log.errOut("Nanny","Unable to find "+eName+" for "+oName+"!!");
	        				else
	        				if(!dropOffs.contains(E))
	        					dropOffs.addElement(E,O,Long.valueOf(time));
	        			}
	        			else
        	    		if(P!=null)
	        				Log.errOut("Nanny","Unable to parse: "+codes+", specifically: "+P.value);
	        		}
        		}
        	}
			changedSinceLastSave=false;
        }

        for(int s=sayLaters.size()-1;s>=0;s--)
        {
    		if(ticking instanceof MOB)
    			CMLib.commands().postSay((MOB)ticking,(MOB)sayLaters.elementAt(s,1),(String)sayLaters.elementAt(s,2));
    		else
    			((MOB)sayLaters.elementAt(s,1)).tell((String)sayLaters.elementAt(s,2));
    		sayLaters.removeElementAt(s);
        }
        
        Room R=CMLib.map().roomLocation((Environmental)ticking);
        Environmental owner=null;
        Environmental E=null;
        if(R!=null)
        for(int a=associations.size()-1;a>=0;a--)
        {
        	owner=(Environmental)associations.elementAt(a,2);
        	E=(Environmental)associations.elementAt(a,1);
        	if(R.isHere(E))
        	{
            	if((CMLib.map().roomLocation(owner)!=R)
            	||(!CMLib.flags().isInTheGame(owner,true)))
            	{
            		if(!dropOffs.contains(E))
            		{
            			if((E instanceof MOB)&&(((MOB)E).amFollowing()!=null))
            				((MOB)E).setFollowing(null);
            			dropOffs.addElement(E,owner,Long.valueOf(System.currentTimeMillis()));
            			associations.removeElementsAt(a);
    	    			changedSinceLastSave=true;
            		}
            	}
        	}
        	else
        	    associations.removeElementAt(a);
        }
        
        if(!changedSinceLastSave)
    	for(int m=dropOffs.size()-1;m>=0;m--)
    		if((dropOffs.elementAt(m,1) instanceof MOB)
    		&&(R!=null)
    		&&(!R.isInhabitant((MOB)dropOffs.elementAt(m,1))))
    		{
    			dropOffs.removeElementsAt(m);
    			changedSinceLastSave=true;
    		}
    	for(int m=dropOffs.size()-1;m>=0;m--)
    		if((dropOffs.elementAt(m,1) instanceof Item)
    		&&(R!=null)
    		&&(!R.isContent((Item)dropOffs.elementAt(m,1))))
    		{
    			dropOffs.removeElementsAt(m);
    			changedSinceLastSave=true;
    		}
    			
        if(changedSinceLastSave)
        {
        	Vector mobsToSave=new Vector();
        	if(ticking instanceof MOB) 
        		mobsToSave.addElement((MOB)ticking);
        	MOB M=null;
    		if(R!=null)
    		{
	        	for(int i=0;i<R.numInhabitants();i++)
	        	{
	        		M=R.fetchInhabitant(i);
	        		if((M!=null)
	        		&&(M.savable())
	        		&&(CMLib.flags().isMobile(M))
	        		&&(M.getStartRoom()==R)
	        		&&(!mobsToSave.contains(M)))
	        			mobsToSave.addElement(M);
	        	}
	        	for(int m=0;m<dropOffs.size();m++)
	        	{
	        		E=(Environmental)dropOffs.elementAt(m,1);
	        		if((E instanceof MOB)
	        		&&(R.isInhabitant((MOB)E))
	        		&&(!mobsToSave.contains(E)))
	        			mobsToSave.addElement(E);
	        	}
	        	CMLib.database().DBUpdateTheseMOBs(R,mobsToSave);
    		}
        	
        	
        	Vector itemsToSave=new Vector();
        	if(ticking instanceof Item) 
        		itemsToSave.addElement((Item)ticking);
        	Item I=null;
    		if(R!=null)
    		{
	        	for(int i=0;i<R.numItems();i++)
	        	{
	        		I=R.fetchItem(i);
	        		if((I!=null)
	        		&&(I.savable())
					&&((!CMLib.flags().isGettable(I))||(I.displayText().length()==0))
	        		&&(!itemsToSave.contains(I)))
	        			itemsToSave.addElement(I);
	        	}
	        	for(int m=0;m<dropOffs.size();m++)
	        	{
	        		E=(Environmental)dropOffs.elementAt(m,1);
	        		if((E instanceof Item)
	        		&&(R.isContent((Item)E))
	        		&&(!itemsToSave.contains(E)))
	        			itemsToSave.addElement(E);
	        	}
	        	CMLib.database().DBUpdateTheseItems(R,itemsToSave);
    		}
        	if(ticking instanceof Room)
        		CMLib.database().DBUpdateRoom((Room)ticking);
        	changedSinceLastSave=false;
        }
        
        if((dropOffs.size()>0)&&(ticking instanceof MOB)&&(CMLib.dice().rollPercentage()<10)&&(R!=null))
        {
        	MOB mob=(MOB)ticking;
        	E=(Environmental)dropOffs.elementAt(CMLib.dice().roll(1,dropOffs.size(),-1),1);
        	if(CMLib.flags().isBaby(E))
        	{
    			if(E.fetchEffect("Soiled")!=null)
        		{
	    			R.show(mob, E, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> change(s) <T-YOUPOSS> diaper.");
    				E.delEffect(E.fetchEffect("Soiled"));
        		}
    			else
        		if(CMLib.dice().rollPercentage()>50)
	    			R.show(mob, E, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> play(s) with <T-NAME>.");
        		else
	    			R.show(mob, E, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> go(es) 'coochie-coochie coo' to <T-NAME>.");
        		
        	}
        	else
        	if(CMLib.flags().isChild(E))
        	{
        		if(CMLib.dice().rollPercentage()>20)
	    			R.show(mob, E, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> play(s) with <T-NAME>.");
        		else
        		{
	    			R.show(mob, E, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> groom(s) <T-NAME>.");
	    			if(E.fetchEffect("Soiled")!=null)
	    				E.delEffect(E.fetchEffect("Soiled"));
        		}
        	}
        	else
        	if(isMount(E))
        	{
        		if(E instanceof MOB)
        		{
	        		if((!CMLib.flags().isAnimalIntelligence((MOB)E))
	        		&&(CMLib.flags().canSpeak(mob)))
	        			R.show(mob, E, CMMsg.MSG_NOISE,"<S-NAME> speak(s) quietly with <T-NAME>.");
	        		else
	        		{
	        			Vector V=((MOB)E).charStats().getMyRace().myResources();
	        			boolean comb=false;
	        			if(V!=null)
	        			for(int v=0;v<V.size();v++)
	        				if(((Item)V.elementAt(v)).material()==RawMaterial.RESOURCE_FUR)
	        					comb=true;
	        			if(comb)
		        			R.show(mob, E, CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> groom(s) <T-NAME>.");
	        			else
		        			R.show(mob, E, CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> pet(s) <T-NAME>.");
	        		}
        		}
        		else
	    			R.show(mob, E, CMMsg.MSG_LOCK,"<S-NAME> admire(s) <T-NAME>.");
        	}
        	else
        	if(E instanceof MOB)
        	{
        		if(CMLib.flags().isAnimalIntelligence((MOB)E))
        			R.show(mob, E, CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> smile(s) and pet(s) <T-NAME>.");
        		else
        		if(CMLib.flags().canSpeak(mob))
        			R.show(mob, E, CMMsg.MSG_NOISE,"<S-NAME> speak(s) quietly with <T-NAME>.");
        	}
        }
        return true;
    }
}