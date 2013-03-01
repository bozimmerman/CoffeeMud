package com.planet_ink.coffee_mud.Behaviors;
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

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
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
	
	protected List<DropOff> dropOffs=null;
	protected List<Payment> payments=new SVector<Payment>();
	protected DVector sayLaters=new DVector(2);
	// dynamic list of who belongs to what, before they leave
	// and get added to official drop-offs.
	protected List<DropOff> associations=new SVector<DropOff>();
	
	public String accountForYourself()
	{ 
		return "caretaking and babysitting for a fee";
	}

	private static class DropOff
	{
		public MOB mommyM;
		public PhysicalAgent baby;
		public long dropOffTime;
		public DropOff(MOB momM, PhysicalAgent baby, long dropOff){mommyM=momM;this.baby=baby; dropOffTime=dropOff;}
	}
	
	private static class Payment
	{
		public MOB mommyM;
		public double paid;
		public Payment(MOB M, double d){mommyM=M; paid=d;}
	}
	
	public double getPaidBy(MOB mob)
	{
		if(mob==null) return 0.0;
		double amt=0.0;
		for(Payment P : payments)
			if(P.mommyM==mob)
				amt+=P.paid;
		return amt;
	}
	
	public boolean isDroppedOff(PhysicalAgent P)
	{
		if(P==null) return false;
		for(DropOff D : dropOffs)
			if(D.baby==P)
				return true;
		return false;
	}
	
	public boolean isAssociated(PhysicalAgent P)
	{
		if(P==null) return false;
		for(DropOff D : associations)
			if((D.mommyM==P)||(D.baby==P))
				return true;
		return false;
	}
	
	public void addPayment(MOB mob,double amt)
	{
		if(mob==null) return;
		for(Payment P : payments)
			if(P.mommyM==mob)
			{
				P.paid += amt;
				return;
			}
		payments.add(new Payment(mob,amt));
	}
	
	public void clearTheSlate(MOB mob)
	{
		if(mob==null) return;
		for(Payment P : payments)
			if(P.mommyM==mob)
				payments.remove(P);
		if(dropOffs != null)
			for(DropOff D : dropOffs)
				if(D.mommyM==mob)
				{
					boolean found=false;
					for(DropOff A : associations) 
						found = found || (A.mommyM==D.mommyM);
					if(!found)
						associations.add(D);
					dropOffs.remove(D);
					changedSinceLastSave=true;
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
		for(DropOff D : dropOffs)
			if(D.mommyM==mob)
			{
				long t=System.currentTimeMillis()-D.dropOffTime;
				t=Math.round(Math.ceil(CMath.div(t,CMProps.getMillisPerMudHour())));
				if(t>0) amt+=(t*hourlyRate);
			}
		return amt;
	}

	public List<PhysicalAgent> getAllOwedFor(MOB mob)
	{
		List<PhysicalAgent> V=new Vector<PhysicalAgent>();
		if(mob!=null)
			for(DropOff D : dropOffs)
				if(D.mommyM==mob)
					V.add(D.baby);
		return V;
	}
	
	public String getPronoun(List<PhysicalAgent> V)
	{
		if(V.size()==0) return "your stuff";
		int babies=0;
		int friends=0;
		int objects=0;
		int mounts=0;
		for(int v=0;v<V.size();v++)
		{
			PhysicalAgent E=V.get(v);
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
	
	public String getOwedFor(String currency, PhysicalAgent P)
	{
		for(DropOff D : dropOffs)
			if(D.baby==P)
			{
				long t=System.currentTimeMillis()-D.dropOffTime;
				t=Math.round(Math.floor(CMath.div(t,CMProps.getMillisPerMudHour())));
				if(t>0) return CMLib.beanCounter().abbreviatedPrice(currency, (t+hourlyRate))+" for watching "+P.name();
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
		for(DropOff D : dropOffs)
			if(D.mommyM==mob)
			{
				long t=System.currentTimeMillis()-D.dropOffTime;
				t=Math.round(Math.ceil(CMath.div(t,CMProps.getMillisPerMudHour())));
				if(t>0) owed.append(CMLib.beanCounter().abbreviatedPrice(currency, (t*hourlyRate))+" for "+D.baby.name()+", ");
			}
		String s=owed.toString();
		if(s.endsWith(", "))s=s.substring(0,s.length()-2);
		return s;
	}
	
	public PhysicalAgent getDroppedOffObjIfAny(PhysicalAgent P)
	{
		if(P==null) return null;
		if(isDroppedOff(P)) return P;
		if(P instanceof Container)
		{
			List<Item> V=((Container)P).getContents();
			Item I=null;
			for(int v=0;v<V.size();v++)
			{
				I=V.get(v);
				P=getDroppedOffObjIfAny(I);
				if(P!=null) return P;
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
				String pronoun=this.getPronoun(new XVector(msg.tool()));
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
		&&(!msg.targetMajor(CMMsg.MASK_INTERMSG))
		&&(getDroppedOffObjIfAny((Item)msg.target()))!=null)
		{
			PhysicalAgent obj=getDroppedOffObjIfAny((Item)msg.target());
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
		if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS)
			||CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.target() instanceof PhysicalAgent)
		&&((getDroppedOffObjIfAny((PhysicalAgent)msg.target())!=null)
				||(msg.target()==host)
				||(msg.target()==CMLib.map().roomLocation(host))))
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
			PhysicalAgent obj=getDroppedOffObjIfAny(msg.source());
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
	
	public MOB getMommyOf(Physical P)
	{
		if((P instanceof Item)
		&&(((Item)P).owner() instanceof MOB)
		&&(!((MOB)((Item)P).owner()).isMonster()))
			return (MOB)((Item)P).owner();
		if((P instanceof MOB)
		&&(((MOB)P).amFollowing()!=null)
		&&(!((MOB)P).amFollowing().isMonster()))
			return ((MOB)P).amFollowing();
		if((P instanceof MOB)
		&&(ultimateFollowing(P)!=null)
		&&(!ultimateFollowing(P).isMonster()))
			return ultimateFollowing(P);
		if(P instanceof Rideable)
		{
			Rideable R=(Rideable)P;
			Environmental E2=null;
			for(int r=0;r<R.numRiders();r++)
			{
				E2=R.fetchRider(r);
				if((E2 instanceof MOB)
				&&(!((MOB)E2).isMonster()))
					return (MOB)E2;
			}
		}
		if((P instanceof Rider)
		&&(((Rider)P).riding()!=null))
			return getMommyOf(((Rider)P).riding());
		return null;
	}

	
	public void addAssociationsIfNecessary(Set<PhysicalAgent> H)
	{
		PhysicalAgent P=null;
		for(Object o : H)
			if(o instanceof PhysicalAgent)
			{
				P=(PhysicalAgent)o;
				if((P instanceof Rider)&&(((Rider)P).riding()!=null)&&(!H.contains(((Rider)P).riding())))
					H.add(P);
			}
		for(Object o : H)
			if(o instanceof PhysicalAgent)
			{
				P=(PhysicalAgent)o;
				if((isDropOffable(P))&&(!isAssociated(P)))
				{
					MOB source=getMommyOf(P);
					if(source!=null)
						associations.add(new DropOff(source,P,System.currentTimeMillis()));
				}
				if(P instanceof MOB)
				{
					MOB mob=(MOB)P;
					for(int t=0;t<mob.numItems();t++)
					{
						Item I=mob.getItem(t);
						if(isDropOffable(I)&&(!isAssociated(I)))
						{
							MOB source=getMommyOf(I);
							if(source!=null)
								associations.add(new DropOff(source,I,System.currentTimeMillis()));
						}
					}
				}
			}
	}

	public List<PhysicalAgent> myCurrentAssocs(MOB mob)
	{
		Vector<PhysicalAgent> V=new Vector<PhysicalAgent>();
		if(mob!=null)
			for(DropOff A : associations)
				if(A.mommyM==mob)
					V.add(A.baby);
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
			Set H=msg.source().getGroupMembers(new HashSet<MOB>());
			msg.source().getRideBuddies(H);
			if(!H.contains(msg.source())) 
				H.add(msg.source());
			HashSet<Environmental> H2 = null;
			do {
				H2 = new HashSet<Environmental>();
				H2.addAll(H);
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
			List<PhysicalAgent> myAssocs=myCurrentAssocs(msg.source());
			StringBuffer list=new StringBuffer("");
			for(int m=0;m<myAssocs.size();m++)
			{
				list.append(myAssocs.get(m).name());
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
				List<PhysicalAgent> myStuff=getAllOwedFor(msg.source());
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
				List<PhysicalAgent> V=getAllOwedFor(msg.source());
				PhysicalAgent P=null;
				for(int v=0;v<V.size();v++)
				{
					P=V.get(v);
					if(P instanceof MOB)
					{
						CMLib.commands().postFollow((MOB)P,msg.source(),false);
						if(CMath.bset(((MOB)P).getBitmap(), MOB.ATT_AUTOGUARD))
							((MOB)P).setBitmap(CMath.unsetb(((MOB)P).getBitmap(), MOB.ATT_AUTOGUARD));
						if(((MOB)P).amFollowing()!=msg.source())
						{
							CMLib.commands().postSay((MOB)host,msg.source(),"Hmm, '"+P.name()+"' doesn't seem ready to leave.  Now get along!",true,false);
							msg.source().location().send((MOB)P,CMClass.getMsg((MOB)P,msg.source(),null,CMMsg.MSG_FOLLOW|CMMsg.MASK_ALWAYS,"<S-NAME> follow(s) <T-NAMESELF>."));
							if(((MOB)P).amFollowing()!=msg.source())
								((MOB)P).setFollowing(msg.source());
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
			Vector<String> oldNames=new Vector<String>();
			String eName=null;
			String oName=null;
			for(DropOff D : dropOffs)
			{
				parms.append("<DROP>");
				
				eName=D.baby.Name();
				oName=D.mommyM.Name();
				if(oldNames.contains(eName))
					eName=getNameIndex(oldNames,eName)+"."+eName;
				parms.append(CMLib.xml().convertXMLtoTag("ENAM",CMLib.xml().parseOutAngleBrackets(eName)));
				parms.append(CMLib.xml().convertXMLtoTag("ONAM",CMLib.xml().parseOutAngleBrackets(oName)));
				parms.append(CMLib.xml().convertXMLtoTag("TIME",D.dropOffTime));
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
			dropOffs=new SVector<DropOff>();
			if(x>0)
			{
				String codes=super.parms.substring(x+3);
				parms=parms.substring(0,3);
				if(codes.trim().length()>0)
				{
					List<XMLLibrary.XMLpiece> V=CMLib.xml().parseAllXML(codes);
					XMLLibrary.XMLpiece P=null;
					Hashtable parsedPlayers=new Hashtable();
					long time=0;
					String eName=null;
					PhysicalAgent PA=null;
					String oName=null;
					Room R=CMLib.map().roomLocation((Environmental)ticking);
					MOB M=null;
					if((V!=null)&&(R!=null))
					for(int v=0;v<V.size();v++)
					{
						P=(V.get(v));
						if((P!=null)&&(P.contents!=null)&&(P.contents.size()==3)&&(P.tag.equalsIgnoreCase("DROP")))
						{
							eName=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(P.contents,"ENAM"));
							oName=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(P.contents,"ONAM"));
							time=CMLib.xml().getLongFromPieces(P.contents,"TIME");
							if(parsedPlayers.get(oName) instanceof MOB)
								M=(MOB)parsedPlayers.get(oName);
							else
							if(parsedPlayers.get(oName) instanceof String)
								continue;
							else
							{
								M=CMLib.players().getLoadPlayer(oName);
								if(M==null) 
									parsedPlayers.put(oName,"");
								else
									parsedPlayers.put(oName,M);
							}
							PA=R.fetchInhabitant(eName);
							if(PA==null) PA=R.findItem(eName);
							if(PA==null)
								Log.errOut("Nanny","Unable to find "+eName+" for "+oName+"!!");
							else
							if(!isDroppedOff(PA))
								dropOffs.add(new DropOff(M,PA,time));
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
		if(R!=null)
		for(DropOff D : associations)
		{
			if(R.isHere(D.baby))
			{
				if((CMLib.map().roomLocation(D.mommyM)!=R)
				||(!CMLib.flags().isInTheGame(D.mommyM,true)))
				{
					if(!isDroppedOff(D.baby))
					{
						if((D.baby instanceof MOB)&&(((MOB)D.baby).amFollowing()!=null))
							((MOB)D.baby).setFollowing(null);
						D.dropOffTime=System.currentTimeMillis();
						dropOffs.add(D);
						associations.remove(D);
						changedSinceLastSave=true;
					}
				}
			}
			else
				associations.remove(D);
		}
		
		if(!changedSinceLastSave)
			for(DropOff D : dropOffs)
				if((D.baby instanceof MOB)
				&&(R!=null)
				&&(!R.isInhabitant((MOB)D.baby)))
				{
					dropOffs.remove(D);
					changedSinceLastSave=true;
				}
		for(DropOff D : dropOffs)
			if((D.baby instanceof Item)
			&&(R!=null)
			&&(!R.isContent((Item)D.baby)))
			{
				dropOffs.remove(D);
				changedSinceLastSave=true;
			}
				
		if(changedSinceLastSave)
		{
			if(R!=null)
			{
				Vector<MOB> mobsToSave=new Vector<MOB>();
				if(ticking instanceof MOB) 
					mobsToSave.addElement((MOB)ticking);
				MOB M=null;
				for(int i=0;i<R.numInhabitants();i++)
				{
					M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.isSavable())
					&&(CMLib.flags().isMobile(M))
					&&(M.getStartRoom()==R)
					&&(!mobsToSave.contains(M)))
						mobsToSave.addElement(M);
				}
				for(DropOff D : dropOffs)
				{
					if((D.baby instanceof MOB)
					&&(R.isInhabitant((MOB)D.baby))
					&&(!mobsToSave.contains(D.baby)))
						mobsToSave.addElement((MOB)D.baby);
				}
				CMLib.database().DBUpdateTheseMOBs(R,mobsToSave);
			}
			
			
			Vector<Item> itemsToSave=new Vector<Item>();
			if(ticking instanceof Item) 
				itemsToSave.addElement((Item)ticking);
			Item I=null;
			if(R!=null)
			{
				for(int i=0;i<R.numItems();i++)
				{
					I=R.getItem(i);
					if((I!=null)
					&&(I.isSavable())
					&&((!CMLib.flags().isGettable(I))||(I.displayText().length()==0))
					&&(!itemsToSave.contains(I)))
						itemsToSave.addElement(I);
				}
				for(DropOff D : dropOffs)
				{
					if((D.baby instanceof Item)
					&&(R.isContent((Item)D.baby))
					&&(!itemsToSave.contains(D.baby)))
						itemsToSave.addElement((Item)D.baby);
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
			PhysicalAgent PA=dropOffs.get(CMLib.dice().roll(1,dropOffs.size(),-1)).baby;
			if(CMLib.flags().isBaby(PA))
			{
				if(PA.fetchEffect("Soiled")!=null)
				{
					R.show(mob, PA, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> change(s) <T-YOUPOSS> diaper.");
					PA.delEffect(PA.fetchEffect("Soiled"));
				}
				else
				if(CMLib.dice().rollPercentage()>50)
					R.show(mob, PA, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> play(s) with <T-NAME>.");
				else
					R.show(mob, PA, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> go(es) 'coochie-coochie coo' to <T-NAME>.");
				
			}
			else
			if(CMLib.flags().isChild(PA))
			{
				if(CMLib.dice().rollPercentage()>20)
					R.show(mob, PA, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> play(s) with <T-NAME>.");
				else
				{
					R.show(mob, PA, CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> groom(s) <T-NAME>.");
					if(PA.fetchEffect("Soiled")!=null)
						PA.delEffect(PA.fetchEffect("Soiled"));
				}
			}
			else
			if(isMount(PA))
			{
				if(PA instanceof MOB)
				{
					if((!CMLib.flags().isAnimalIntelligence((MOB)PA))
					&&(CMLib.flags().canSpeak(mob)))
						R.show(mob, PA, CMMsg.MSG_NOISE,"<S-NAME> speak(s) quietly with <T-NAME>.");
					else
					{
						List<RawMaterial> V=((MOB)PA).charStats().getMyRace().myResources();
						boolean comb=false;
						if(V!=null)
						for(int v=0;v<V.size();v++)
							if(((Item)V.get(v)).material()==RawMaterial.RESOURCE_FUR)
								comb=true;
						if(comb)
							R.show(mob, PA, CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> groom(s) <T-NAME>.");
						else
							R.show(mob, PA, CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> pet(s) <T-NAME>.");
					}
				}
				else
					R.show(mob, PA, CMMsg.MSG_LOCK,"<S-NAME> admire(s) <T-NAME>.");
			}
			else
			if(PA instanceof MOB)
			{
				if(CMLib.flags().isAnimalIntelligence((MOB)PA))
					R.show(mob, PA, CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> smile(s) and pet(s) <T-NAME>.");
				else
				if(CMLib.flags().canSpeak(mob))
					R.show(mob, PA, CMMsg.MSG_NOISE,"<S-NAME> speak(s) quietly with <T-NAME>.");
			}
		}
		return true;
	}
}
