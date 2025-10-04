package com.planet_ink.coffee_mud.Commands;
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
import java.util.regex.*;

/*
   Copyright 2005-2025 Bo Zimmerman

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
public class GModify extends StdCommand
{
	public GModify()
	{
	}

	private final String[]	access	= I(new String[] { "GMODIFY" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private static final int	FLAG_CASESENSITIVE	= 1;
	private static final int	FLAG_SUBSTRING		= 2;
	private static final int	FLAG_OR				= 4;
	private static final int	FLAG_AND			= 8;

	private enum GComp
	{
		EQUATOR_$,
		EQUATOR_EQ,
		EQUATOR_NEQ,
		EQUATOR_GT,
		EQUATOR_LT,
		EQUATOR_LTEQ,
		EQUATOR_GTEQ;
		private static String[] choices = new String[]{"$","=","!=",">","<","<=",">="};
		public static GComp get(final String eq)
		{
			final int x = CMParms.indexOf(choices, eq);
			if(x<0)
				return null;
			return GComp.values()[x];
		}
	}

	public static String getStat(final Environmental E, final String stat)
	{
		if((stat!=null)&&(stat.length()>0))
		{
			if((stat.equalsIgnoreCase("REJUV"))
			&&(E instanceof Physical))
			{
				if(((Physical)E).basePhyStats().rejuv()==PhyStats.NO_REJUV)
					return "0";
				return ""+((Physical)E).basePhyStats().rejuv();
			}
			else
			if((stat.equalsIgnoreCase("RESOURCE"))
			&&(E instanceof Item))
			{
				final int mat=((Item)E).material();
				if(mat >=0)
					return RawMaterial.CODES.NAME(mat);
				return "";
			}
			else
			if((stat.equalsIgnoreCase("MATERIALTYPE"))
			&&(E instanceof Item))
			{
				final int mat=((Item)E).material() & RawMaterial.MATERIAL_MASK;
				if((mat >=0)&&(mat<RawMaterial.Material.names().length))
					return RawMaterial.Material.names()[mat];
				return "";
			}
			else
			if((stat.equalsIgnoreCase("GENDER"))
			&&(E instanceof MOB))
			{
				return Character.toString((char)((MOB)E).baseCharStats().getStat(CharStats.STAT_GENDER));
			}
			else
			if(stat.equalsIgnoreCase("CLASSTYPE"))
			{
				return ""+CMClass.getObjectType(E).toString();
			}
			else
			if(E instanceof FactionMember)
			{
				for(final Enumeration<String> fs=((FactionMember)E).factions();fs.hasMoreElements();)
				{
					final String factionID=fs.nextElement();
					if(stat.equalsIgnoreCase(factionID))
						return ""+((FactionMember)E).fetchFaction(factionID);
				}
			}
			if(!E.isStat(stat))
			{
				if((E instanceof Physical) && ((Physical)E).basePhyStats().isStat(stat))
					return ((Physical)E).basePhyStats().getStat(stat);
				if((E instanceof MOB)
				&&(((MOB)E).isPlayer()))
				{
					if(stat.equalsIgnoreCase("CHARCLASS"))
						return ((MOB)E).baseCharStats().getCurrentClass().ID();
					if(((MOB)E).baseCharStats().isStat(stat))
						return ((MOB)E).baseCharStats().getStat(stat);
					if(((MOB)E).baseState().isStat(stat))
						return ((MOB)E).baseState().getStat(stat);
					if(CMLib.coffeeMaker().getGenMobCodeNum(stat)>=0)
						return CMLib.coffeeMaker().getGenMobStat((MOB)E,stat);
				}
			}
			return E.getStat(stat);
		}
		return "";
	}

	public static String[] splitClassParms(final String value)
	{
		if(value.endsWith(")"))
		{
			final int x=value.indexOf('(');
			if(x>0)
			{
				return new String[]{value.substring(0, x),value.substring(x+1,value.length()-1)};
			}
		}
		return new String[]{value,""};
	}

	public static Environmental setStat(final Environmental E, final String stat, final String value)
	{
		if((stat!=null)&&(stat.length()>0))
		{
			if(stat.equals("CLASS")
			&&(E.isStat("CLASS")))
			{
				if(E instanceof Room)
				{
					final Room newRoom=CMClass.getLocale(value);
					if(newRoom!=null)
					{
						final Room R=CMLib.genEd().changeRoomType((Room)E,newRoom);
						R.recoverRoomStats();
						return R;
					}
				}
				else
				{
					final CMClass.CMObjectType type=CMClass.getObjectType(E);
					Environmental newObj=(Environmental)CMClass.getByType(value, type);
					if(newObj == null)
						newObj=CMClass.getUnknown(value);
					if(newObj == null)
						Log.errOut("GModify","Unknown class '"+value+"'");
					if((newObj instanceof Physical)
					&&(E instanceof Physical))
					{
						final Physical P1=(Physical)E;
						final Physical P2=(Physical)newObj;
						P2.basePhyStats().setLevel(P1.basePhyStats().level());
						P2.phyStats().setLevel(P1.basePhyStats().level());
						P2.basePhyStats().setAbility(P1.basePhyStats().ability());
						P2.phyStats().setAbility(P1.basePhyStats().ability());
						P2.setMiscText(P1.text());
						P2.text();
						P2.recoverPhyStats();
						return P2;
					}
				}
				return null;
			}
			else
			if((stat.equalsIgnoreCase("REJUV"))
			&&(E instanceof Physical))
				((Physical)E).basePhyStats().setRejuv(CMath.s_int(value));
			else
			if(stat.equalsIgnoreCase("REBALANCE"))
			{
				int newLevel = 0;
				int oldLevel = 0;
				if(E instanceof Physical)
				{
					newLevel = ((Physical)E).basePhyStats().level();
					oldLevel=newLevel;
					if(CMath.isInteger(value))
					{
						newLevel=CMath.s_int(value);
						((Item) E).basePhyStats().setLevel(newLevel);
						((Item) E).phyStats().setLevel(newLevel);
					}
				}

				if(E instanceof Item)
					CMLib.itemBuilder().balanceItemByLevel((Item)E);
				else
				if(E instanceof MOB)
					CMLib.leveler().fillOutMOB((MOB)E, ((MOB) E).basePhyStats().level());
				if(E instanceof Physical)
				{
					((Item) E).basePhyStats().setLevel(oldLevel);
					((Item) E).phyStats().setLevel(oldLevel);
				}
			}
			else
			if((stat.equalsIgnoreCase("RESOURCE"))
			&&(E instanceof Item))
				E.setStat("MATERIAL", value);
			else
			if((stat.equalsIgnoreCase("MATERIALTYPE"))
			&&(E instanceof Item))
			{
				int mat;
				if(CMath.isInteger(value))
					mat=CMath.s_int(value);
				else
				{
					RawMaterial.Material M = RawMaterial.Material.find(value);
					if(M == null)
						M = RawMaterial.Material.findIgnoreCase(value);
					if(M == null)
						M = RawMaterial.Material.startsWith(value);
					if(M == null)
						M = RawMaterial.Material.startsWithIgnoreCase(value);
					if(M!=null)
						mat=M.mask();
					else
					{
						mat = RawMaterial.CODES.FIND_CaseSensitive(value);
						if(mat < 0)
							mat = RawMaterial.CODES.FIND_IgnoreCase(value);
						if(mat < 0)
							mat = RawMaterial.CODES.FIND_StartsWith(value);
						if(mat < 0)
							return E;
					}
				}
				if((mat&RawMaterial.MATERIAL_MASK) != mat)
					E.setStat("MATERIAL", ""+mat);
				else
					E.setStat("MATERIAL", RawMaterial.CODES.NAME(RawMaterial.CODES.MOST_FREQUENT(mat)));
			}
			else
			if((stat.equalsIgnoreCase("GENDER"))
			&&(E instanceof MOB))
			{
				if(value.charAt(0)=='?')
				{
					if(CMLib.dice().rollPercentage()>50)
						((MOB)E).baseCharStats().setStat(CharStats.STAT_GENDER,'M');
					else
						((MOB)E).baseCharStats().setStat(CharStats.STAT_GENDER,'F');
				}
				else
					((MOB)E).baseCharStats().setStat(CharStats.STAT_GENDER,Character.toUpperCase(value.charAt(0)));
			}
			else
			if((stat.equalsIgnoreCase("ADDABILITY"))
			&&(E instanceof AbilityContainer))
			{
				final String[] classParms = splitClassParms(value);
				Ability A=((AbilityContainer)E).fetchAbility(classParms[0]);
				if(A==null)
					A=CMClass.getAbility(classParms[0]);
				if(A!=null)
				{
					A.setMiscText(classParms[1]);
					((AbilityContainer)E).addAbility(A);
				}
			}
			else
			if((stat.equalsIgnoreCase("ADDAFFECT"))
			&&(E instanceof Affectable))
			{
				final String[] classParms = splitClassParms(value);
				Ability A=((Affectable)E).fetchEffect(classParms[0]);
				if(A==null)
					A=CMClass.getAbility(classParms[0]);
				if(A!=null)
				{
					A.setMiscText(classParms[1]);
					((Affectable)E).addNonUninvokableEffect(A);
				}
			}
			else
			if((stat.equalsIgnoreCase("ADDBEHAVIOR"))
			&&(E instanceof Behavable))
			{
				final String[] classParms = splitClassParms(value);
				Behavior B=((Behavable)E).fetchBehavior(classParms[0]);
				if(B==null)
					B=CMClass.getBehavior(classParms[0]);
				if(B!=null)
				{
					B.setParms(classParms[1]);
					((Behavable)E).addBehavior(B);
				}
			}
			else
			if((stat.equalsIgnoreCase("DELABILITY"))
			&&(E instanceof AbilityContainer))
				((AbilityContainer)E).delAbility(((AbilityContainer)E).fetchAbility(value));
			else
			if((stat.equalsIgnoreCase("DELAFFECT"))
			&&(E instanceof Affectable))
			{
				Ability A = ((Affectable)E).fetchEffect(value);
				if(A == null)
				{
					A = CMClass.getAbility(value);
					if(A != null)
						A = ((Affectable)E).fetchEffect(A.ID());
				}
				if(A != null)
					((Affectable)E).delEffect(A);
			}
			else
			if((stat.equalsIgnoreCase("DELBEHAVIOR"))
			&&(E instanceof Behavable))
				((Behavable)E).delBehavior(((Behavable)E).fetchBehavior(value));
			else
			if((stat.equalsIgnoreCase("DELFACTION"))
			&&(E instanceof FactionMember))
				((FactionMember)E).removeFaction(value.toUpperCase().trim());
			else
			{
				boolean found=false;
				if(E instanceof FactionMember)
				{
					for(final Enumeration<String> fs=((FactionMember)E).factions();fs.hasMoreElements();)
					{
						final String factionID=fs.nextElement();
						if(stat.equalsIgnoreCase(factionID)
						&&(E instanceof FactionMember)
						&&(CMath.isInteger(value)))
						{
							((FactionMember)E).addFaction(factionID, CMath.s_int(value));
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					if(!E.isStat(stat))
					{
						if((E instanceof Physical) && ((Physical)E).basePhyStats().isStat(stat))
						{
							((Physical)E).basePhyStats().setStat(stat, value);
							return E;
						}
						if((E instanceof MOB)
						&&(((MOB)E).isPlayer()))
						{
							if(CMLib.coffeeMaker().getGenMobCodeNum(stat)>=0)
							{
								CMLib.coffeeMaker().setGenMobStat((MOB)E,stat,value);
								return E;
							}
							if(stat.equalsIgnoreCase("CHARCLASS"))
							{
								((MOB)E).baseCharStats().setCurrentClass(CMClass.getCharClass(value));
								return E;
							}
							if(((MOB)E).baseCharStats().isStat(stat))
							{
								((MOB)E).baseCharStats().setStat(stat,value);
								return E;
							}
							if(((MOB)E).baseState().isStat(stat))
							{
								((MOB)E).baseState().setStat(stat,value);
								return E;
							}
						}
					}
					E.setStat(stat,value);
				}
			}
		}
		return E;
	}

	public static void gmodifydebugtell(final MOB mob, final String msg)
	{
		if(mob!=null)
			mob.tell(msg);
		Log.sysOut("GMODIFY",msg);
	}

	private static Environmental tryModfy(final MOB mob, final Session sess, final Room room, Environmental E,
										  final List<GMod> changes, final List<GMod> onfields, final boolean noisy,
										  final String tag)
	{
		if((sess==null)||(sess.isStopped()))
			return null;
		if(tag != null)
		{
			if(!(E instanceof Taggable))
				return null;
			if(!(((Taggable)E).hasTag(tag)))
				return null;
		}
		try
		{
			Thread.sleep(1);
		}
		catch (final Exception e)
		{
			sess.stopSession(true, false, false, false);
			return null;
		}
		boolean didAnything=false;
		if(noisy)
			gmodifydebugtell(mob,E.name()+"/"+CMClass.classID(E));
		boolean checkedOut=true;
		final TriadArrayList<String,Integer,Integer> matches=new TriadArrayList<String,Integer,Integer>();
		int lastCode=FLAG_AND;
		for(final GMod gm : onfields)
		{
			if(noisy)
				gmodifydebugtell(mob,gm.key+"/"+getStat(E,gm.key)+"/"+gm.value+"/"+getStat(E,gm.key).equals(gm.value));
			String value = gm.value;
			int matchStart=-1;
			int matchEnd=-1;
			String stat=getStat(E,gm.key);
			switch(gm.eq)
			{
			case EQUATOR_$:
				if(gm.patt!=null)
				{
					if(!CMath.bset(gm.flag,FLAG_SUBSTRING))
					{
						if(stat.matches(value))
						{
							matchStart=0;
							matchEnd=stat.length();
						}
					}
					else
					{
						final Matcher M=gm.patt.matcher(stat);
						M.reset();
						if(M.find())
						{
							matchStart=M.start();
							matchEnd=M.end();
						}
					}
				}
				break;
			case EQUATOR_EQ:
			{
				if(!CMath.bset(gm.flag,FLAG_CASESENSITIVE))
				{
					stat=stat.toLowerCase();
					value=value.toLowerCase();
				}
				if(CMath.bset(gm.flag,FLAG_SUBSTRING))
				{
					matchStart=stat.indexOf(value);
					matchEnd=matchStart+value.length();
				}
				else
				if(stat.equals(value))
				{
					matchStart=0;
					matchEnd=stat.length();
				}
				break;
			}
			case EQUATOR_NEQ:
			{
				if(!CMath.bset(gm.flag,FLAG_CASESENSITIVE))
				{
					stat=stat.toLowerCase();
					value=value.toLowerCase();
				}
				if(CMath.bset(gm.flag,FLAG_SUBSTRING))
				{
					if(stat.indexOf(value)<0)
					{
						matchStart=0;
						matchEnd=stat.length();
					}
				}
				else
				if(!stat.equals(value))
				{
					matchStart=0;
					matchEnd=stat.length();
				}
				break;
			}
			case EQUATOR_GT:
			{
				if(!CMath.bset(gm.flag,FLAG_CASESENSITIVE))
				{
					stat=stat.toLowerCase();
					value=value.toLowerCase();
				}
				if(CMath.isNumber(stat)&&CMath.isNumber(value))
					matchStart=(CMath.s_long(stat)>CMath.s_long(value))?0:-1;
				else
					matchStart=(stat.compareTo(value)>0)?0:-1;
				break;
			}
			case EQUATOR_LT:
			{
				if(!CMath.bset(gm.flag,FLAG_CASESENSITIVE))
				{
					stat=stat.toLowerCase();
					value=value.toLowerCase();
				}
				if(CMath.isNumber(stat)&&CMath.isNumber(value))
					matchStart=(CMath.s_long(stat)<CMath.s_long(value))?0:-1;
				else
					matchStart=(stat.compareTo(value)<0)?0:-1;
				break;
			}
			case EQUATOR_LTEQ:
			{
				if(!CMath.bset(gm.flag,FLAG_CASESENSITIVE))
				{
					stat=stat.toLowerCase();
					value=value.toLowerCase();
				}
				if(CMath.isNumber(stat)&&CMath.isNumber(value))
					matchStart=(CMath.s_long(stat)<=CMath.s_long(value))?0:-1;
				else
					matchStart=(stat.compareTo(value)<=0)?0:-1;
				break;
			}
			case EQUATOR_GTEQ:
			{
				if(!CMath.bset(gm.flag,FLAG_CASESENSITIVE))
				{
					stat=stat.toLowerCase();
					value=value.toLowerCase();
				}
				if(CMath.isNumber(stat)&&CMath.isNumber(value))
					matchStart=(CMath.s_long(stat)>=CMath.s_long(value))?0:-1;
				else
					matchStart=(stat.compareTo(value)>=0)?0:-1;
				break;
			}
			}
			if(matchStart>=0)
				matches.add(gm.key,Integer.valueOf(matchStart),Integer.valueOf(matchEnd));
			if(CMath.bset(lastCode,FLAG_AND))
				checkedOut=checkedOut&&(matchStart>=0);
			else
			if(CMath.bset(lastCode,FLAG_OR))
				checkedOut=checkedOut||(matchStart>=0);
			else
				checkedOut=(matchStart>=0);
			lastCode=gm.flag;
		}
		if(checkedOut)
		{
			if(changes.size()==0)
				mob.tell(CMLib.lang().L("Matched on @x1 from @x2.",E.name(),CMLib.map().getExtendedRoomID(room)));
			else
			for(final GMod gm : changes)
			{
				String value=gm.value;
				if(gm.key.equalsIgnoreCase("DESTROY"))
				{
					if(CMath.s_bool(value))
					{
						if(E instanceof Room)
						{
							final String msgStr = "Room "+CMLib.map().getDescriptiveExtendedRoomID((Room)E)+" was DESTROYED by "+mob.Name()+"!";
							mob.tell(msgStr);
							Log.sysOut("GMODIFY",msgStr);
							CMLib.map().obliterateMapRoom((Room)E);
						}
						else
						if((E instanceof MOB)&&(((MOB)E).isPlayer()))
						{
							final String msgStr = E.Name()+" in "+room.roomID()+" was DESTROYED by "+mob.Name()+"!";
							mob.tell(msgStr);
							Log.sysOut("GMODIFY",msgStr);
							CMLib.players().obliteratePlayer((MOB)E, true, true);
						}
						else
						{
							final String msgStr = E.Name()+" in "+room.roomID()+" was DESTROYED by "+mob.Name()+"!";
							mob.tell(msgStr);
							Log.sysOut("GMODIFY",msgStr);
							E.destroy();
						}
						return E;
					}
					continue;
				}
				if(noisy)
					gmodifydebugtell(mob,E.name()+" wants to change "+gm.key+" value "+getStat(E,gm.key)+" to "+value+"/"+(!getStat(E,gm.key).equals(value)));
				if(CMath.bset(gm.flag,FLAG_SUBSTRING))
				{
					int matchStart=-1;
					int matchEnd=-1;
					for(int m=0;m<matches.size();m++)
					{
						if(matches.get(m).first.equals(gm.key))
						{
							matchStart=matches.get(m).second.intValue();
							matchEnd=matches.get(m).third.intValue();
						}
					}
					if(matchStart>=0)
					{
						final String stat=getStat(E,gm.key);
						value=stat.substring(0,matchStart)+value+stat.substring(matchEnd);
					}
				}
				if(!getStat(E,gm.key).equals(value))
				{
					final String msgStr="The "+CMStrings.capitalizeAndLower(gm.key)+" field on "+E.Name()+" in "+room.roomID()+" was changed from "+getStat(E,gm.key)+" to "+value+".";
					mob.tell(msgStr);
					Log.sysOut("GMODIFY",msgStr);
					E=setStat(E,gm.key,value);
					if(E==null)
						return null;
					didAnything=true;
				}
			}
		}
		if(didAnything)
		{
			if(E instanceof MOB)
			{
				((MOB)E).recoverPhyStats();
				((MOB)E).recoverCharStats();
				((MOB)E).recoverMaxState();
			}
			else
			if(E instanceof Physical)
				((Physical)E).recoverPhyStats();
			E.text();
			if(CMLib.flags().isCataloged(E) && (E instanceof Physical))
				CMLib.catalog().updateCatalog((Physical)E);
		}
		return didAnything?E:null;
	}

	public void addEnumeratedStatCodes(final Enumeration<? extends Modifiable> e, final Set<String> allKnownFields, final StringBuffer allFieldsMsg)
	{
		for(;e.hasMoreElements();)
		{
			final Modifiable E=e.nextElement();
			final String[] fields=E.getStatCodes();
			for(int x=0;x<fields.length;x++)
			{
				if(!allKnownFields.contains(fields[x]))
				{
					allKnownFields.add(fields[x]);
					allFieldsMsg.append(fields[x]+" ");
				}
			}
		}
	}

	protected Set<String> getAllStatFields()
	{
		return getAllStatKeys().second;
	}

	protected String getAllStatFieldsMsg()
	{
		return getAllStatKeys().first;
	}

	@SuppressWarnings("unchecked")
	protected synchronized Pair<String, Set<String>> getAllStatKeys()
	{
		if(!Resources.isResource("SYSTEM_ALL_STAT_KEYS"))
		{
			final Set<String> allKnownFields=new HashSet<String>();
			final StringBuffer allFieldsMsg=new StringBuffer("");
			addEnumeratedStatCodes(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.basicItems(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.weapons(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.armor(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.tech(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.locales(),allKnownFields,allFieldsMsg);
			for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
			{
				final Faction F=f.nextElement();
				if(F.isPreLoaded())
				{
					allFieldsMsg.append(F.factionID().toUpperCase()).append(" ");
					allKnownFields.add(F.factionID().toUpperCase());
				}
			}
			final PhyStats phy = (PhyStats)CMClass.getCommon("DefaultPhyStats");
			for(final String stat : phy.getStatCodes())
			{
				if(!allKnownFields.contains(stat))
				{
					allKnownFields.add(stat);
					allFieldsMsg.append(stat).append(" ");
				}
			}
			allFieldsMsg.append("CLASSTYPE REJUV DESTROY ADDABILITY DELABILITY ADDBEHAVIOR DELBEHAVIOR ADDAFFECT DELAFFECT "
					+ "DELFACTION RESOURCE MATERIALTYPE REBALANCE");
			allKnownFields.addAll(Arrays.asList(new String[]{"CLASSTYPE","REJUV","RESOURCE","MATERIALTYPE","GENDER","DESTROY",
					"ADDABILITY","DELABILITY","ADDBEHAVIOR","DELBEHAVIOR","ADDAFFECT","DELAFFECT","DELFACTION","REBALANCE"}));
			allFieldsMsg.append(" (** Players Only: ");
			final CharStats chy = (CharStats)CMClass.getCommon("DefaultCharStats");
			allKnownFields.add("CHARCLASS");
			allFieldsMsg.append("CHARCLASS").append(" ");
			for(final String stat : chy.getStatCodes())
			{
				if(!allKnownFields.contains(stat))
				{
					allKnownFields.add(stat);
					allFieldsMsg.append(stat).append(" ");
				}
			}
			final CharState che = (CharState)CMClass.getCommon("DefaultCharState");
			for(final String stat : che.getStatCodes())
			{
				if(!allKnownFields.contains(stat))
				{
					allKnownFields.add(stat);
					allFieldsMsg.append(stat).append(" ");
				}
			}
			allFieldsMsg.append(")");
			Resources.submitResource("SYSTEM_ALL_STAT_KEYS", new Pair<String, Set<String>>(allFieldsMsg.toString(), allKnownFields));
		}
		return (Pair<String, Set<String>>)Resources.getResource("SYSTEM_ALL_STAT_KEYS");
	}

	protected static void fixDeities(final Room R)
	{
		if(R==null)
			return;
		//OK! Keep this weirdness here!  It's necessary because oldRoom will have blown
		//away any real deities with copy deities in the CMMap, and this will restore
		//the real ones.
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M instanceof Deity)
			&&(M.isMonster())
			&&(M.isSavable())
			&&(M.getStartRoom()==R))
				CMLib.map().registerWorldObjectLoaded(R.getArea(), R, M);
		}
	}

	private static class GMod
	{
		final String key;
		final String value;
		final GComp eq;
		final int flag;
		final Pattern patt;

		public GMod(final String key, final GComp eq, final String value, final int flag, final Pattern patt)
		{
			this.key = key;
			this.value = value;
			this.eq = eq;
			this.flag = flag;
			this.patt = patt;
		}

		public static Converter<GMod, String> toKeyConverter= new Converter<GMod, String>()
		{
			@Override
			public String convert(final GMod obj)
			{
				return obj.key;
			}
		};
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final boolean noisy=CMSecurity.isDebugging(CMSecurity.DbgFlag.GMODIFY);
		List<Places> placesToDo=new ArrayList<Places>();
		final String whole=CMParms.combine(commands,0);
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell(L("GModify what?"));
			return false;
		}
		if(mob.isMonster())
		{
			mob.tell(L("No can do."));
			return false;
		}
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("?"))
		{
			mob.tell(L("Valid field names are @x1",this.getAllStatFieldsMsg()));
			return false;
		}

		String tag = null;
		boolean doPlayersOnly=false;
		final String cmd = (commands.size()>0) ? commands.get(0).toLowerCase() : "";
		if(cmd.equals("room"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.GMODIFY))
			{
				mob.tell(L("You are not allowed to do that here."));
				return false;
			}
			commands.remove(0);
			placesToDo.add(mob.location());
		}
		else
		if(cmd.equals("area"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.GMODIFY))
			{
				mob.tell(L("You are not allowed to do that here."));
				return false;
			}
			commands.remove(0);
			placesToDo.add(mob.location().getArea());
		}
		else
		if(cmd.equals("world"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.GMODIFY))
			{
				mob.tell(L("You are not allowed to do that."));
				return false;
			}
			commands.remove(0);
			placesToDo=new ArrayList<Places>();
		}
		else
		if(cmd.equals("players"))
		{
			if((!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.GMODIFY))
			||(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDPLAYERS)))
			{
				mob.tell(L("You are not allowed to do that."));
				return false;
			}
			commands.remove(0);
			placesToDo=new ArrayList<Places>();
			doPlayersOnly=true;
		}
		else
		if(cmd.startsWith("#"))
		{
			tag = commands.get(0).substring(1);
			final List<String> roomIDs = CMLib.database().findTaggedObjectRooms(tag);
			commands.remove(0);
			placesToDo=new ArrayList<Places>();
			for (final String roomID : roomIDs)
			{
				final Room R = CMLib.map().getRoom(roomID);
				if((R != null)&&(!placesToDo.contains(R))&&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.GMODIFY)))
					placesToDo.add(R);
			}
		}
		else
			placesToDo.add(mob.location());
		final List<GMod> changes=new LinkedList<GMod>();
		final List<GMod> onfields=new LinkedList<GMod>();
		List<GMod> use=null;

		use=onfields;
		final List<String> newSet=new ArrayList<String>();
		final Set<String> allKnownFields=this.getAllStatFields();
		final String fieldsMsg = this.getAllStatFieldsMsg();
		StringBuffer s=new StringBuffer("");
		for(int i=0;i<commands.size();i++)
		{
			String str=(commands.get(i));
			if((str.toUpperCase().startsWith("CHANGE="))
			||(str.toUpperCase().startsWith("WHEN=")))
			{
				if(s.length()>0)
					newSet.add(s.toString());
				s=new StringBuffer("");
			}
			if(str.indexOf(' ')>=0)
				str="\""+str+"\"";
			if(s.length()>0)
				s.append(" "+str);
			else
				s.append(str);
		}
		if(s.length()>0)
			newSet.add(s.toString());
		for(int i=0;i<newSet.size();i++)
		{
			String str=newSet.get(i);
			if(str.toUpperCase().startsWith("CHANGE="))
			{
				use=changes;
				str=str.substring(7).trim();
			}
			if(str.toUpperCase().startsWith("WHEN="))
			{
				str=str.substring(5).trim();
				use=onfields;
			}
			while(str.trim().length()>0)
			{
				int eq=-1;
				int divLen=0;
				int code=0;
				while((divLen==0)&&((++eq)<str.length()))
					switch(str.charAt(eq))
					{
					case '!':
						if((eq<(str.length()-1))&&(str.charAt(eq+1)=='='))
						{
							divLen=2;
							break;
						}
						break;
					case '=':
					case '$':
						divLen=1;
						break;
					case '<':
					case '>':
						divLen=1;
						if((eq<(str.length()-1))&&(str.charAt(eq+1)=='='))
						{
							divLen=2;
							break;
						}
						break;
					}
				if(divLen==0)
				{
					mob.tell(L("String '@x1' does not contain an equation divider.  Even CHANGE needs at least an = sign!",str));
					return false;
				}
				final String equatorStr=str.substring(eq,eq+divLen);
				final GComp equator=GComp.get(equatorStr);
				if (equator == null)
				{
					mob.tell(L("'@x1' is not a valid equation divider.  Valid ones are =, !=, >, <, >=, <=, and $ (for regex).", equatorStr));
					return false;
				}
				String val=str.substring(eq+divLen);
				String key=str.substring(0,eq).trim();

				int divBackLen=0;
				eq=-1;
				while((divBackLen==0)&&((++eq)<val.length()))
				{
					switch(val.charAt(eq))
					{
					case '&':
						if((eq<(val.length()-1))&&(val.charAt(eq+1)=='&'))
						{
							divBackLen=2;
							break;
						}
						break;
					case '|':
						if((eq<(val.length()-1))&&(val.charAt(eq+1)=='&'))
						{
							divBackLen=2;
							break;
						}
						break;
					}
				}
				if(divBackLen==0)
					str="";
				else
				{
					final String attach=val.substring(eq,eq+divBackLen).trim();
					if(attach.equals("&&"))
						code=code|FLAG_AND;
					else
					if(attach.equals("||"))
						code=code|FLAG_OR;
					str=val.substring(eq+divBackLen);
					val=val.substring(0,eq);
				}
				Pattern P=null;
				if(use==null)
				{
					mob.tell(L("'@x1' goes to an unknown parameter!",(commands.get(i))));
					return false;
				}
				while(val.trim().startsWith("["))
				{
					final int x2=val.indexOf(']');
					if(x2<0)
						break;
					final String cd=val.trim().substring(1,x2);
					if(cd.length()!=2)
						break;
					if(cd.equalsIgnoreCase("ss"))
						code=code|FLAG_SUBSTRING;
					if(cd.equalsIgnoreCase("cs"))
						code=code|FLAG_CASESENSITIVE;
					val=val.substring(x2+1);
				}
				if(equator == GComp.EQUATOR_$)
				{
					int patCodes=Pattern.DOTALL;
					if(!CMath.bset(code,FLAG_CASESENSITIVE))
						patCodes=patCodes|Pattern.CASE_INSENSITIVE;
					P=Pattern.compile(val,patCodes);
				}
				key=key.toUpperCase().trim();
				if(allKnownFields.contains(key))
					use.add(new GMod(key,equator,val,code,P));
				else
				{
					mob.tell(L("'@x1' is an unknown field name.  Valid fields include: @x2",key,fieldsMsg.toString()));
					return false;
				}
			}
		}
		if((onfields.size()==0)&&(changes.size()==0)&&(tag==null))
		{
			mob.tell(L("You must specify either WHEN, a tag, or CHANGES parameters for valid matches to be made."));
			return false;
		}

		/**
		 * GMODIFY just for players...
		 */
		if(doPlayersOnly)
		{
			final Session sess=mob.session();
			if(sess!=null)
			{
				if(changes.size()==0)
					sess.safeRawPrintln(L("Searching..."));
				else
					sess.safeRawPrint(L("Searching, modifying and saving..."));
			}
			final java.util.List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
			final PlayerLibrary plib=CMLib.players();
			for(final PlayerLibrary.ThinPlayer tP : allUsers)
			{
				if((sess==null)||(sess.isStopped()))
					return false;
				final boolean wasLoaded=plib.isLoadedPlayer(tP.name());
				final MOB M=plib.getLoadPlayer(tP.name());
				if(M!=null)
				{
					final Room room=(M.location()==null)?mob.location():M.location();
					final boolean changed = tryModfy(mob, sess, room, M, changes, onfields, noisy, tag) != null;
					if(!M.amDestroyed())
					{
						if(!wasLoaded)
						{
							if(changed)
							{
								M.delAllEffects(true);
								CMLib.database().DBUpdatePlayer(M);
							}
							final PlayerStats pStats = M.playerStats();
							if(pStats != null)
								pStats.getExtItems().delAllItems(true);
							CMLib.players().delPlayer(M);
							M.destroy();
						}
						else
						if(changed)
							CMLib.database().DBUpdatePlayer(M);
					}
				}
			}
			return true;
		}

		/**
		 * Normal GMODIFY for map is below
		 */
		if(placesToDo.size()==0)
		{
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(A.getCompleteMap().hasMoreElements()
				&&CMSecurity.isAllowed(mob,(A.getCompleteMap().nextElement()),CMSecurity.SecFlag.GMODIFY))
					placesToDo.add(A);
			}
		}
		if(placesToDo.size()==0)
		{
			mob.tell(L("There are no rooms with data to gmodify!"));
			return false;
		}
		for(int i=placesToDo.size()-1;i>=0;i--)
		{
			if(placesToDo.get(i) instanceof Area)
			{
				final Area A=(Area)placesToDo.get(i);
				placesToDo.remove(A);
				for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.GMODIFY))
						placesToDo.add(R);
				}
			}
			else
			if(placesToDo.get(i) instanceof Room)
			{
			}
			else
				return false;
		}
		// now do the modification...
		if(mob.session()!=null)
		{
			if(changes.size()==0)
				mob.session().safeRawPrintln(L("Searching..."));
			else
				mob.session().safeRawPrintln(L("Searching, modifying and saving..."));
		}
		if(noisy)
			gmodifydebugtell(mob,"Rooms to do: "+placesToDo.size());
		if(noisy)
			gmodifydebugtell(mob,"When fields="+CMParms.toListString(new ConvertingList<GMod,String>(onfields, GMod.toKeyConverter)));
		if(noisy)
			gmodifydebugtell(mob,"Change fields="+CMParms.toListString(new ConvertingList<GMod,String>(changes, GMod.toKeyConverter)));
		Log.sysOut("GModify",mob.Name()+" "+whole+".");
		final Session sess=mob.session();

		final Set<Area> areasSeen = new HashSet<Area>();
		for(int r=0;r<placesToDo.size();r++)
		{
			Room R=(Room)placesToDo.get(r);
			if(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.GMODIFY))
				continue;
			if((R==null)||(R.roomID()==null)||(R.roomID().length()==0))
				continue;
			synchronized(CMClass.getSync("SYNC"+R.roomID()))
			{
				R=CMLib.map().getRoom(R);
				if((sess==null)||(sess.isStopped())||(R==null)||(R.getArea()==null))
					return false;
				final Room origR=R;
				final Area A=R.getArea();
				if(!areasSeen.contains(A))
				{
					areasSeen.add(A);
					final Area possNewA = (Area)tryModfy(mob,sess,R,A,changes,onfields,noisy, tag);
					if(possNewA != null)
						CMLib.database().DBUpdateArea(A.Name(), possNewA);
				}
				final Area.State oldFlag=A.getAreaState();
				if(changes.size()==0)
				{
					R=CMLib.coffeeMaker().makeNewRoomContent(R,false);
					if(R!=null)
						A.setAreaState(Area.State.FROZEN);
				}
				else
				{
					A.setAreaState(Area.State.FROZEN);
					CMLib.map().resetRoom(R);
				}
				if(R==null)
					continue;
				boolean savemobs=false;
				boolean saveitems=false;
				boolean saveroom=false;
				final Room possNewRoom=(Room)tryModfy(mob,sess,R,R,changes,onfields,noisy, tag);
				if(possNewRoom!=null)
				{
					R=possNewRoom;
					saveroom=true;
				}
				for(int i=R.numItems()-1;i>=0;i--)
				{
					final Item I=R.getItem(i);
					if(I==null)
						continue;
					final Item newI=(Item)tryModfy(mob,sess,R,I,changes,onfields,noisy, tag);
					if(newI!=null)
					{
						saveitems=true;
						if(newI.amDestroyed() || (newI != I))
							R.delItem(I);
						if(I!=newI)
							R.addItem(newI);
					}
				}
				for(int m=R.numInhabitants()-1;m>=0;m--)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M.isSavable()))
					{
						final MOB newM=(MOB)tryModfy(mob,sess,R,M,changes,onfields,noisy, tag);
						if(newM!=null)
						{
							savemobs=true;
							if(newM.amDestroyed() || (newM != M))
								R.delInhabitant(M);
							if(M!=newM)
								R.addInhabitant(newM);
							M=newM;
						}
						if(!M.amDestroyed())
						{
							for(int i=M.numItems()-1;i>=0;i--)
							{
								final Item I=M.getItem(i);
								if(I==null)
									continue;
								final Item newI=(Item)tryModfy(mob,sess,R,I,changes,onfields,noisy, tag);
								if(newI!=null)
								{
									savemobs=true;
									if(newI.amDestroyed() || (newI != I))
										M.delItem(I);
									if(I!=newI)
										M.addItem(newI);
								}
							}
							final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
							if(SK!=null)
							{
								final List<CoffeeShop.ShelfProduct> removeThese=new ArrayList<CoffeeShop.ShelfProduct>();
								final CoffeeShop shop=(SK instanceof Librarian)?((Librarian)SK).getBaseLibrary():SK.getShop();
								for(final Iterator<CoffeeShop.ShelfProduct> i=shop.getStoreShelves();i.hasNext();)
								{
									final CoffeeShop.ShelfProduct P=i.next();
									final Environmental E2=P.product;
									if((E2 instanceof Item)||(E2 instanceof MOB))
									{
										final Environmental E3=tryModfy(mob,sess,R,E2,changes,onfields,noisy, tag);
										if(E3!=null)
										{
											savemobs=true;
											if(E3.amDestroyed())
												removeThese.add(P);
											else
											if(E2!=E3)
												P.product=E3;
										}
									}
								}
								for(final CoffeeShop.ShelfProduct  P : removeThese)
									shop.deleteShelfProduct(P);
							}
						}
					}
				}
				if(saveroom)
					CMLib.database().DBUpdateRoom(R);
				if(saveitems)
					CMLib.database().DBUpdateItems(R);
				if(savemobs)
					CMLib.database().DBUpdateMOBs(R);
				A.setAreaState(oldFlag);
				if(changes.size()==0)
				{
					R.destroy();
					if(origR!=null)
						fixDeities(origR);
				}
				if(saveroom)
				{
					final Room realR=CMLib.map().getRoom(R);
					if(realR!=null)
					{
						realR.clearSky();
						if(R instanceof GridLocale)
							((GridLocale)R).clearGrid(realR);
					}
				}
			}
		}

		if(sess!=null)
			sess.rawPrintln(L("\n\rDone!"));
		for(int i=0;i<placesToDo.size();i++)
		{
			final Room R=(Room)placesToDo.get(i);
			if(R!=null)
			{
				final Area A=R.getArea();
				if((A!=null)&&(A.getAreaState()!=Area.State.ACTIVE))
					A.setAreaState(Area.State.ACTIVE);
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.GMODIFY);
	}
}
