package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Law.TreasurySet;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class DefaultLawSet implements Law
{
	@Override
	public String ID()
	{
		return "DefaultLawSet";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(final Exception e)
		{
			return new DefaultLawSet();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (DefaultLawSet)this.clone();
		}
		catch(final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	private boolean namesModifiable=false;
	private boolean lawsModifiable=false;
	private LegalBehavior legalDetails=null;

	private final List<List<String>>	otherCrimes=new Vector<List<String>>();
	private final List<String[]> 		otherBits=new Vector<String[]>();
	private final List<List<String>> 	bannedSubstances=new Vector<List<String>>();
	private final List<String[]> 		bannedBits=new Vector<String[]>();
	private final Map<String,String[]>	abilityCrimes=new Hashtable<String,String[]>();
	private final Map<String,String[]>	basicCrimes=new Hashtable<String,String[]>();
	private final Map<String, Object>	taxLaws=new Hashtable<String, Object>();

	private List<String> chitChat=new Vector<String>();
	private List<String> chitChat2=new Vector<String>();
	private List<String> chitChat3=new Vector<String>();
	private List<String> jailRooms=new Vector<String>();
	private List<String> releaseRooms=new Vector<String>();
	private List<String> officerNames=new Vector<String>();
	private List<String> judgeNames=new Vector<String>();
	private String[] 	 messages=new String[Law.MSG_TOTAL];

	private boolean activated=true;

	private final SVector<LegalWarrant> oldWarrants=new SVector<LegalWarrant>();
	private final SVector<LegalWarrant> warrants=new SVector<LegalWarrant>();

	private boolean arrestMobs=false;

	private Properties theLaws=null;
	private int lastMonthChecked=-1;

	private final String[] paroleMessages=new String[4];
	private final Integer[] paroleTimes=new Integer[4];

	private final String[] jailMessages=new String[4];
	private final Integer[] jailTimes=new Integer[4];

	@Override
	public void initialize(LegalBehavior details, Properties laws, boolean modifiableNames, boolean modifiableLaws)
	{
		legalDetails=details;
		namesModifiable=modifiableNames;
		lawsModifiable=modifiableLaws;
		resetLaw(laws);
	}

	@Override 
	public List<List<String>> otherCrimes() 
	{ 
		return otherCrimes;
	}
	
	@Override 
	public List<String[]> otherBits() 
	{ 
		return otherBits;
	}
	
	@Override 
	public List<List<String>> bannedSubstances() 
	{ 
		return bannedSubstances;
	}
	
	@Override 
	public List<String[]> bannedBits() 
	{ 
		return bannedBits;
	}
	
	@Override 
	public Map<String,String[]> abilityCrimes()
	{ 
		return abilityCrimes;
	}
	
	@Override 
	public Map<String,String[]> basicCrimes()
	{ 
		return basicCrimes;
	}
	
	@Override 
	public Map<String, Object> taxLaws()
	{
		return taxLaws;
	}

	@Override
	public boolean hasModifiableNames()
	{
		return namesModifiable;
	}
	
	@Override 
	public boolean hasModifiableLaws()
	{
		return lawsModifiable;
	}

	@Override 
	public List<String> chitChat()
	{ 
		return chitChat;
	}
	
	@Override 
	public List<String> chitChat2()
	{ 
		return chitChat2;
	}
	
	@Override 
	public List<String> chitChat3()
	{ 
		return chitChat3;
	}
	
	@Override 
	public List<String> jailRooms()
	{ 
		return jailRooms;
	}
	
	@Override 
	public List<String> releaseRooms()
	{ 
		return releaseRooms;
	}
	
	@Override 
	public List<String> officerNames()
	{ 
		return officerNames;
	}
	
	@Override 
	public List<String> judgeNames()
	{ 
		return judgeNames;
	}
	
	@Override 
	public String[] messages()
	{ 
		return messages;
	}

	@Override 
	public List<LegalWarrant> oldWarrants()
	{ 
		return oldWarrants;
	}
	
	@Override 
	public List<LegalWarrant> warrants()
	{ 
		return warrants;
	}

	@Override public boolean arrestMobs()
	{ 
		return arrestMobs;
	}

	@Override 
	public String[] paroleMessages()
	{ 
		return paroleMessages;
	}
	
	@Override 
	public Integer[] paroleTimes()
	{ 
		return paroleTimes;
	}

	@Override 
	public String[] jailMessages()
	{ 
		return jailMessages;
	}
	
	@Override 
	public Integer[] jailTimes()
	{ 
		return jailTimes;
	}

	@Override
	public void changeStates(LegalWarrant W, int state)
	{
		if((W==null)||(W.criminal()==null))
			return;
		if(warrants.contains(W))
		{
			for(int w=0;w<warrants.size();w++)
			{
				final LegalWarrant W2=warrants.elementAt(w);
				if(W2.criminal()==W.criminal())
					W2.setState(state);
			}
		}
	}

	@Override
	public TreasurySet getTreasuryNSafe(Area A)
	{
		Room treasuryR=null;
		Container container=null;
		final String tres=(String)taxLaws().get("TREASURY");
		Item I=null;
		if((tres!=null)&&(tres.length()>0))
		{
			final List<String> V=CMParms.parseSemicolons(tres,false);
			if(V.size()>0)
			{
				Room R=null;
				final String room=V.get(0);
				String item="";
				if(V.size()>1)
					item=CMParms.combine(V,1);
				if(!room.equalsIgnoreCase("*"))
				{
					treasuryR=CMLib.map().getRoom(room);
					if(treasuryR!=null)
					{
						I=treasuryR.findItem(item);
						if(I instanceof Container)
						container=(Container)I;
					}
				}
				else
				if(item.length()>0)
				for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
				{
					R=e.nextElement();
					I=R.findItem(item);
					if(I instanceof Container)
					{
						container=(Container)I;
						treasuryR=R;
						break;
					}
				}
				if((room.length()>0)&&(treasuryR==null))
					treasuryR=A.getRandomMetroRoom();
			}
		}
		return new Law.TreasurySet(treasuryR,container);
	}

	protected boolean sendGameMail(final String mailBox, final String subject, String message)
	{
		if(CMProps.getIntVar(CMProps.Int.MAXMAILBOX)>0)
		{
			final int count=CMLib.database().DBCountJournal(CMProps.getVar(CMProps.Str.MAILBOX),null,mailBox);
			if(count>=CMProps.getIntVar(CMProps.Int.MAXMAILBOX))
				return false;
		}
		message+=CMLib.lang().L("\n\r\n\rThis message was sent through the @x1 mail server at @x2, port @x3"
				+".  Please contact the administrators regarding any abuse of this system.\n\r",
				CMProps.getVar(CMProps.Str.MUDNAME),CMProps.getVar(CMProps.Str.MUDDOMAIN),CMProps.getVar(CMProps.Str.MUDPORTS));
		CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX), mailBox, mailBox, subject, message);
		return true;
	}

	protected boolean notifyPlayer(final String ownerName, String owerName, final double owed, final String fourWord, final String subject, String message)
	{
		MOB M=CMLib.players().getPlayer(ownerName);
		if((M!=null)&&(CMLib.flags().isInTheGame(M, true)))
		{
			final String amountOwed = CMLib.beanCounter().nameCurrencyLong(M, owed);
			if(owerName.length()==0)
				owerName=M.Name();
			M.tell(CMLib.lang().L(message,owerName,amountOwed,CMProps.getVar(CMProps.Str.MUDNAME),fourWord));
		}
		else
		{
			M=CMLib.players().getLoadPlayer(ownerName);
			if(M!=null)
			{
				if(owerName.length()==0)
					owerName=M.Name();
				final String amountOwed = CMLib.beanCounter().nameCurrencyLong(M, owed);
				final String subj = CMLib.lang().L(subject,owerName,amountOwed,CMProps.getVar(CMProps.Str.MUDNAME),fourWord);
				final String msg = CMLib.lang().L(message,owerName,amountOwed,CMProps.getVar(CMProps.Str.MUDNAME),fourWord);
				return sendGameMail(M.Name(), subj, msg);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void propertyTaxTick(Area A, boolean debugging)
	{
		if(lastMonthChecked!=A.getTimeObj().getMonth())
		{
			lastMonthChecked=A.getTimeObj().getMonth();
			double tax=CMath.s_double((String)taxLaws.get("PROPERTYTAX"));
			if(tax==0.0)
				return;
			tax=CMath.div(tax,100.0);
			List<LandTitle> titles=CMLib.law().getAllUniqueLandTitles(A.getMetroMap(),"*",false);
			final Hashtable<String,Vector<LandTitle>> owners=new Hashtable<String,Vector<LandTitle>>();
			for(final LandTitle T : titles)
			{
				Vector<LandTitle> D=owners.get(T.getOwnerName());
				if(D==null)
				{
					D=new Vector<LandTitle>();
					owners.put(T.getOwnerName(),D);
				}
				D.addElement(T);
			}
			titles=null;
			final Law.TreasurySet treas=getTreasuryNSafe(A);
			final Room treasuryR=treas.room;
			final Container container=treas.container;
			final String[] evasionBits=(String[])taxLaws().get("TAXEVASION");

			for(final String owner : owners.keySet())
			{
				MOB responsibleMob=null;
				final Clan C=CMLib.clans().getClan(owner);
				if(C!=null)
					responsibleMob=C.getResponsibleMember();
				else
					responsibleMob=CMLib.players().getLoadPlayer(owner);
				final Vector<LandTitle> particulars=owners.get(owner);

				double totalValue=0;
				double paid=0;
				double owed=0;
				final StringBuffer properties=new StringBuffer("");
				LandTitle T=null;
				List<Room> propertyRooms=null;

				for(int p=0;p<particulars.size();p++)
				{
					if(p>0)
						properties.append(", ");
					T=(particulars.elementAt(p));
					propertyRooms=T.getAllTitledRooms();
					if((propertyRooms.size()<2)
					||(CMLib.map().getArea(T.landPropertyID())!=null))
						properties.append(T.landPropertyID());
					else
						properties.append("around "+CMLib.map().getExtendedRoomID(propertyRooms.get(0)));
					totalValue+=T.getPrice();
					if(T.backTaxes()>0)
					{
						totalValue+=T.backTaxes();
						owed+=T.backTaxes();
					}
				}
				owed+=CMath.mul(totalValue,tax);

				if(owed>0)
				{
					for(int p=0;p<particulars.size();p++)
					{
						T=(particulars.elementAt(p));
						if(T.backTaxes()<0)
						{
							if((-T.backTaxes())>=owed)
							{
								paid+=owed;
								T.setBackTaxes((int)Math.round((T.backTaxes())+owed));
								T.updateTitle();
								break;
							}
							paid+=(-T.backTaxes());
							T.setBackTaxes(0);
							T.updateTitle();
						}
					}
				}
				if(owed>0)
				{
					owed-=paid;
					if((owed>0)
					&&(!CMLib.beanCounter().modifyLocalBankGold(A,
						owner,
						CMLib.utensils().getFormattedDate(A)+": Withdrawal of "+owed+": Taxes on property: "+properties.toString(),
						CMLib.beanCounter().getCurrency(A),
						-owed)))
					{
						boolean owesButNotConfiscated=false;
						for(int p=0;p<particulars.size();p++)
						{
							T=particulars.elementAt(p);
							double owedOnThisLand=CMath.mul(T.getPrice(),tax);
							owedOnThisLand-=(paid/particulars.size());
							if(owedOnThisLand>0)
							{
								int oldBackTaxes = T.backTaxes();
								T.setBackTaxes((int)Math.round(oldBackTaxes+owedOnThisLand));
								if(CMath.div(T.getPrice(),T.backTaxes())<2.0)
								{
									final Clan clanC=CMLib.clans().getClan(T.getOwnerName());
									if(clanC!=null)
									{
										final MOB M=clanC.getResponsibleMember();
										final List<Pair<Clan,Integer>> clanSet=new Vector<Pair<Clan,Integer>>();
										clanSet.add(new Pair<Clan,Integer>(C,Integer.valueOf(0)));
										final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
										for(int i=0;i<channels.size();i++)
											CMLib.commands().postChannel(channels.get(i),clanSet,CMLib.lang().L("@x1 has lost the title to @x2 due to failure to pay property taxes.",T.getOwnerName(),T.landPropertyID()),false);
										if(M!=null)
										{
											notifyPlayer(M.Name(),clanC.name(),owed,"","@x1 lost property on @x3.",
													"@x1 has lost the title to @x4 due to failure to pay property taxes.");
										}
									}
									else
									{
										notifyPlayer(T.getOwnerName(),"",owed,T.landPropertyID(),"@x1 property lost on @x3.",
												"@x1 has lost the title to @x4 due to failure to pay property taxes.");
									}
									T.setBackTaxes(0);
									T.setOwnerName("");
									T.updateTitle();
								}
								else
								if(T.backTaxes() > oldBackTaxes)
								{
									owesButNotConfiscated=true;
									T.updateTitle();
								}
							}
						}
						if(owesButNotConfiscated)
						{
							final Clan clanC=CMLib.clans().getClan(owner);
							if(clanC!=null)
							{
								final MOB M=clanC.getResponsibleMember();
								final String amountOwed = CMLib.beanCounter().nameCurrencyLong(M, owed);
								final List<Pair<Clan,Integer>> clanSet=new Vector<Pair<Clan,Integer>>();
								clanSet.add(new Pair<Clan,Integer>(C,Integer.valueOf(0)));
								final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
								for(int i=0;i<channels.size();i++)
								{
									CMLib.commands().postChannel(channels.get(i),clanSet,CMLib.lang().L("@x1 owes @x2 in back taxes.  Sufficient funds were not found in a local bank account."
											+ "  Failure to pay could result in loss of property. ",clanC.name(),amountOwed),false);
								}
								if(M!=null)
								{
									notifyPlayer(M.Name(),clanC.name(),owed,"","Taxes Owed by @x1 on @x3.",
											"@x1 owes @x2 in back taxes.  Sufficient funds were not found in a local bank account.  Failure to pay could result in loss of property.");
								}
							}
							else
							{
								notifyPlayer(owner,"",owed,"","Taxes Owed by @x1 on @x3.",
										"@x1 owes @x2 in back taxes.  Sufficient were not found in a local bank account.  Failure to pay could result in loss of property.");
							}
							if((evasionBits!=null)
							&&(evasionBits[Law.BIT_CRIMENAME].length()>0)
							&&(responsibleMob!=null))
							{
								legalDetails.fillOutWarrant(responsibleMob,
															this,
															A,
															null,
															evasionBits[Law.BIT_CRIMELOCS],
															evasionBits[Law.BIT_CRIMEFLAGS],
															evasionBits[Law.BIT_CRIMENAME],
															evasionBits[Law.BIT_SENTENCE],
															evasionBits[Law.BIT_WARNMSG]);
							}
						}
					}
					else
					{
						for(int p=0;p<particulars.size();p++)
						{
							T=particulars.elementAt(p);
							if(T.backTaxes()>0)
							{
								T.setBackTaxes(0);
								T.updateTitle();
							}
						}
						if(owed<0)
							owed=0;
						if((treasuryR!=null)&&((owed+paid)>0))
						{
							final List<Coins> V=CMLib.beanCounter().makeAllCurrency(CMLib.beanCounter().getCurrency(A),owed+paid);
							for(int v=0;v<V.size();v++)
							{
								final Coins COIN=V.get(v);
								COIN.setContainer(container);
								treasuryR.addItem(COIN);
								COIN.putCoinsBack();
							}
						}
						if((evasionBits!=null)
						&&(evasionBits[Law.BIT_CRIMENAME].length()>0)
						&&(responsibleMob!=null))
						{
							while(getWarrant(responsibleMob,evasionBits[Law.BIT_CRIMENAME],true,debugging)!=null)
							{
							}
						}
					}
				}
			}

		}
	}

	@Override
	public String getMessage(int which)
	{
		if((which>=0)&&(which<messages.length)&&(messages[which]!=null))
			return messages[which];
		return "";
	}

	@Override
	public String paroleMessages(int which)
	{
		if((which>=0)
		&&(which<paroleMessages.length)
		&&(paroleMessages[which]!=null))
			return paroleMessages[which];
		return "";
	}

	@Override
	public int paroleTimes(int which)
	{
		if((which>=0)
		&&(which<paroleTimes.length)
		&&(paroleTimes[which]!=null))
			return paroleTimes[which].intValue();
		return 0;
	}

	@Override
	public String jailMessages(int which)
	{
		if((which>=0)
		&&(which<jailMessages.length)
		&&(jailMessages[which]!=null))
			return jailMessages[which];
		return "";
	}

	@Override
	public int jailTimes(int which)
	{
		if((which>=0)
		&&(which<jailTimes.length)
		&&(jailTimes[which]!=null))
			return jailTimes[which].intValue();
		return 0;
	}

	@Override
	public String getInternalStr(String msg)
	{
		if((theLaws!=null)&&(theLaws.get(msg)!=null))
			return (String)theLaws.get(msg);
		return "";
	}

	@Override
	public boolean isInternalStr(String msg)
	{
		if((theLaws!=null)&&(theLaws.get(msg)!=null))
			return true;
		return false;
	}

	@Override
	public void setInternalStr(String tag, String value)
	{
		if(theLaws!=null)
		{
			if(theLaws.get(tag)!=null)
				theLaws.remove(tag);
			theLaws.put(tag,value);
		}
	}

	@Override 
	public boolean lawIsActivated()
	{ 
		return activated;
	}

	@Override
	public void resetLaw()
	{
		if(theLaws!=null)
			resetLaw(theLaws);
	}

	private void resetLaw(Properties laws)
	{
		theLaws=laws;
		activated=(!getInternalStr("ACTIVATED").equalsIgnoreCase("FALSE"));
		officerNames=CMParms.parse(getInternalStr("OFFICERS"));
		chitChat=CMParms.parse(getInternalStr("CHITCHAT"));
		chitChat2=CMParms.parse(getInternalStr("CHITCHAT2"));
		chitChat3=CMParms.parse(getInternalStr("CHITCHAT3"));
		judgeNames=CMParms.parse(getInternalStr("JUDGE"));

		arrestMobs=getInternalStr("ARRESTMOBS").equalsIgnoreCase("true");

		messages=new String[Law.MSG_TOTAL];
		messages[Law.MSG_PREVOFF]=getInternalStr("PREVOFFMSG");
		messages[Law.MSG_WARNING]=getInternalStr("WARNINGMSG");
		messages[Law.MSG_THREAT]=getInternalStr("THREATMSG");
		messages[Law.MSG_EXECUTE]=getInternalStr("EXECUTEMSG");
		messages[Law.MSG_COPKILLER]=isInternalStr("COPKILLERMSG")?getInternalStr("COPKILLERMSG"):"COPKILLER!!!! ARGH!!!!!!";
		messages[Law.MSG_PROTECTEDMASK]=getInternalStr("PROTECTED");
		messages[Law.MSG_TRESPASSERMASK]=getInternalStr("TRESPASSERS");
		messages[Law.MSG_RESISTFIGHT]=getInternalStr("RESISTFIGHTMSG");
		messages[Law.MSG_NORESIST]=getInternalStr("NORESISTMSG");
		messages[Law.MSG_RESISTWARN]=getInternalStr("RESISTWARNMSG");
		messages[Law.MSG_PAROLEDISMISS]=getInternalStr("PAROLEDISMISS");
		messages[Law.MSG_RESIST]=getInternalStr("RESISTMSG");
		messages[Law.MSG_LAWFREE]=getInternalStr("LAWFREE");

		paroleMessages[0]=getInternalStr("PAROLE1MSG");
		paroleMessages[1]=getInternalStr("PAROLE2MSG");
		paroleMessages[2]=getInternalStr("PAROLE3MSG");
		paroleMessages[3]=getInternalStr("PAROLE4MSG");
		paroleTimes[0]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE1TIME")));
		paroleTimes[1]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE2TIME")));
		paroleTimes[2]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE3TIME")));
		paroleTimes[3]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE4TIME")));

		jailMessages[0]=getInternalStr("JAIL1MSG");
		jailMessages[1]=getInternalStr("JAIL2MSG");
		jailMessages[2]=getInternalStr("JAIL3MSG");
		jailMessages[3]=getInternalStr("JAIL4MSG");
		jailTimes[0]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL1TIME")));
		jailTimes[1]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL2TIME")));
		jailTimes[2]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL3TIME")));
		jailTimes[3]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL4TIME")));

		jailRooms=CMParms.parseSemicolons(getInternalStr("JAIL"),true);
		releaseRooms=CMParms.parseSemicolons(getInternalStr("RELEASEROOM"),true);

		taxLaws.clear();
		String taxLaw=getInternalStr("PROPERTYTAX");
		if(taxLaw.length()>0)
			taxLaws.put("PROPERTYTAX",taxLaw);
		taxLaw=getInternalStr("TAXEVASION");
		if(taxLaw.length()>0)
			taxLaws.put("TAXEVASION",getInternalBits(taxLaw));
		taxLaw=getInternalStr("TREASURY");
		if(taxLaw.length()>0)
			taxLaws.put("TREASURY",taxLaw);
		taxLaw=getInternalStr("SALESTAX");
		if(taxLaw.length()>0)
			taxLaws.put("SALESTAX",taxLaw);
		taxLaw=getInternalStr("CITTAX");
		if(taxLaw.length()>0)
			taxLaws.put("CITTAX",taxLaw);

		basicCrimes.clear();
		String basicLaw=getInternalStr("MURDER");
		if(basicLaw.length()>0)
			basicCrimes.put("MURDER",getInternalBits(basicLaw));
		basicLaw=getInternalStr("RESISTINGARREST");
		if(basicLaw.length()>0)
			basicCrimes.put("RESISTINGARREST",getInternalBits(basicLaw));
		basicLaw=getInternalStr("NUDITY");
		if(basicLaw.length()>0)
			basicCrimes.put("NUDITY",getInternalBits(basicLaw));
		basicLaw=getInternalStr("ASSAULT");
		if(basicLaw.length()>0)
			basicCrimes.put("ASSAULT",getInternalBits(basicLaw));
		basicLaw=getInternalStr("ARMED");
		if(basicLaw.length()>0)
			basicCrimes.put("ARMED",getInternalBits(basicLaw));
		basicLaw=getInternalStr("TRESPASSING");
		if(basicLaw.length()>0)
			basicCrimes.put("TRESPASSING",getInternalBits(basicLaw));
		basicLaw=getInternalStr("PROPERTYROB");
		if(basicLaw.length()>0)
			basicCrimes.put("PROPERTYROB",getInternalBits(basicLaw));

		abilityCrimes.clear();
		otherCrimes.clear();
		otherBits.clear();
		bannedSubstances.clear();
		bannedBits.clear();
		for(final Enumeration<Object> e=laws.keys();e.hasMoreElements();)
		{
			final String key=(String)e.nextElement();
			final String words=(String)laws.get(key);
			final int x=words.indexOf(';');
			if(x>=0)
			{
				if(key.startsWith("CRIME"))
				{
					otherCrimes.add(CMParms.parse(words.substring(0,x)));
					final String[] bits=new String[Law.BIT_NUMBITS];
					final List<String> parsed=CMParms.parseSemicolons(words.substring(x+1),false);
					for(int i=0;i<Law.BIT_NUMBITS;i++)
					{
						if(i<parsed.size())
						{
							bits[i]=parsed.get(i);
						}
						else
						{
							bits[i]="";
						}
					}
					otherBits.add(bits);
				}
				else
				if(key.startsWith("BANNED"))
				{
					bannedSubstances.add(CMParms.parse(words.substring(0,x)));
					final String[] bits=new String[Law.BIT_NUMBITS];
					final List<String> parsed=CMParms.parseSemicolons(words.substring(x+1),false);
					for(int i=0;i<Law.BIT_NUMBITS;i++)
					{
						if(i<parsed.size())
						{
							bits[i]=parsed.get(i);
						}
						else
						{
							bits[i]="";
						}
					}
					bannedBits.add(bits);
				}
				else
				if((key.startsWith("$")&&(CMClass.getAbility(key.substring(1))!=null))
				||(CMClass.getAbility(key)!=null)
				||(CMParms.containsIgnoreCase(Ability.ACODE_DESCS_,key))
				||(key.startsWith("$")&&CMParms.containsIgnoreCase(Ability.ACODE_DESCS_,key.substring(1)))
				||(CMParms.containsIgnoreCase(Ability.DOMAIN_DESCS,key))
				||(key.startsWith("$")&&CMParms.containsIgnoreCase(Ability.DOMAIN_DESCS,key.substring(1))))
				{
					abilityCrimes.put(key.toUpperCase(),getInternalBits(words));
				}
			}
		}
	}

	@Override
	public String rawLawString()
	{
		if(theLaws!=null)
		{
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			try
			{
				 theLaws.store(out,"");
			}
			catch(final IOException e)
			{
			}
			String s=CMStrings.replaceAll(out.toString(),"\n\r","~");
			s=CMStrings.replaceAll(s,"\r\n","~");
			s=CMStrings.replaceAll(s,"\n","~");
			s=CMStrings.replaceAll(s,"\r","~");
			s=CMStrings.replaceAll(s,"'","`");
			return s;
		}
		return "";
	}

	private String[] getInternalBits(String bitStr)
	{
		final String[] bits=new String[Law.BIT_NUMBITS];
		final List<String> parsed=CMParms.parseSemicolons(bitStr,false);
		for(int i=0;i<Law.BIT_NUMBITS;i++)
		{
			if(i<parsed.size())
				bits[i]=parsed.get(i);
			else
				bits[i]="";
		}
		return bits;
	}

	public LegalWarrant getWarrant(MOB criminal,
								   String crime,
								   boolean pull,
								   boolean debugging)
	{
		LegalWarrant W=null;
		for(int i=0;i<warrants.size();i++)
		{
			final LegalWarrant W2=warrants.elementAt(i);
			if((W2.criminal()==criminal)
			&&(W2.crime().equals(crime))
			&&(legalDetails.isStillACrime(W2,debugging)))
			{
				W=W2;
				if(pull)
					warrants.removeElement(W2);
				break;
			}
		}
		return W;
	}

	@Override
	public LegalWarrant getCopkiller(Area A, LegalBehavior behav, MOB mob)
	{
		final String[] copKillerInfo=basicCrimes().get("MURDER");
		if(copKillerInfo!=null)
		{
			for(int i=0;i<warrants.size();i++)
			{
				final LegalWarrant W=warrants.elementAt(i);
				if((W.criminal()==mob)
				&&(W.crime().equals(copKillerInfo[Law.BIT_CRIMENAME]))
				&&(W.victim()!=null)
				&&(behav!=null)
				&&(behav.isStillACrime(W,false))
				&&(behav.isAnyOfficer(A,W.victim())))
					return W;
			}
		}
		return null;
	}

	@Override
	public LegalWarrant getLawResister(Area A, LegalBehavior behav, MOB mob)
	{
		final String[] lawResistInfo=basicCrimes().get("RESISTINGARREST");
		if(lawResistInfo!=null)
		for(int i=0;i<warrants.size();i++)
		{
			final LegalWarrant W=warrants.elementAt(i);
			if((W.criminal()==mob)
			&&(W.crime().equals(lawResistInfo[Law.BIT_CRIMENAME]))
			&&(W.victim()!=null)
			&&(behav.isStillACrime(W,false))
			&&(behav.isAnyOfficer(A,W.victim())))
				return W;
		}
		return null;
	}

	@Override
	public LegalWarrant getWarrant(MOB mob, int which)
	{
		int one=0;
		for(int i=0;i<warrants.size();i++)
		{
			final LegalWarrant W=warrants.elementAt(i);
			if(W.criminal()==mob)
			{
				if(which==one)
					return W;
				one++;
			}
		}
		return null;
	}

	@Override
	public LegalWarrant getOldWarrant(MOB criminal, String crime, boolean pull)
	{
		LegalWarrant W=null;
		for(int i=0;i<oldWarrants.size();i++)
		{
			final LegalWarrant W2=oldWarrants.elementAt(i);
			if((W2.criminal()==criminal)&&(W2.crime().equals(crime)))
			{
				W=W2;
				if(pull)
					oldWarrants.removeElement(W2);
				break;
			}
		}
		return W;
	}

}
