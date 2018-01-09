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
	private static final int	EQUATOR_$			= 0;
	private static final int	EQUATOR_EQ			= 1;
	private static final int	EQUATOR_NEQ			= 2;
	private static final int	EQUATOR_GT			= 3;
	private static final int	EQUATOR_LT			= 4;
	private static final int	EQUATOR_LTEQ		= 5;
	private static final int	EQUATOR_GTEQ		= 6;
	
	private static final Map<Object,Integer> EQUATORS=CMStrings.makeNumericHash(new String[]{"$","=","!=",">","<","<=",">="});

	public static String getStat(Environmental E, String stat)
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
			return E.getStat(stat);
		}
		return "";
	}
	
	public static String[] splitClassParms(String value)
	{
		if(value.endsWith(")"))
		{
			int x=value.indexOf('(');
			if(x>0)
			{
				return new String[]{value.substring(0, x),value.substring(x+1,value.length()-1)};
			}
		}
		return new String[]{value,""};
	}

	public static Environmental setStat(Environmental E, String stat, String value)
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
					final Environmental newObj=(Environmental)CMClass.getByType(value, type);
					if((newObj instanceof Physical)&&(E instanceof Physical))
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
			if((stat.equalsIgnoreCase("GENDER"))
			&&(E instanceof MOB))
				((MOB)E).baseCharStats().setStat(CharStats.STAT_GENDER,Character.toUpperCase(value.charAt(0)));
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
				((Affectable)E).delEffect(((Affectable)E).fetchEffect(value));
			else
			if((stat.equalsIgnoreCase("DELBEHAVIOR"))
			&&(E instanceof Behavable))
				((Behavable)E).delBehavior(((Behavable)E).fetchBehavior(value));
			else
				E.setStat(stat,value);
		}
		return E;
	}

	public static void gmodifydebugtell(MOB mob, String msg)
	{
		if(mob!=null)
			mob.tell(msg);
		Log.sysOut("GMODIFY",msg);
	}

	private static Environmental tryModfy(MOB mob, Room room, Environmental E, DVector changes, DVector onfields, boolean noisy)
	{
		if((mob.session()==null)||(mob.session().isStopped()))
			return null;
		try
		{
			Thread.sleep(1);
		}
		catch (final Exception e)
		{
			mob.session().stopSession(false, false, false);
			return null;
		}
		boolean didAnything=false;
		if(noisy)
			gmodifydebugtell(mob,E.name()+"/"+CMClass.classID(E));
		String field=null;
		String value=null;
		String equator=null;
		String stat=null;
		int codes=-1;
		Pattern pattern=null;
		boolean checkedOut=true;
		Matcher M=null;
		final DVector matches=new DVector(3);
		int lastCode=FLAG_AND;
		for(int i=0;i<onfields.size();i++)
		{
			field=(String)onfields.get(i,1);
			equator=(String)onfields.get(i,2);
			value=(String)onfields.get(i,3);
			codes=((Integer)onfields.get(i,4)).intValue();
			pattern=(Pattern)onfields.get(i,5);
			if(noisy)
				gmodifydebugtell(mob,field+"/"+getStat(E,field)+"/"+value+"/"+getStat(E,field).equals(value));
			int matchStart=-1;
			int matchEnd=-1;
			stat=getStat(E,field);
			final Integer EQ=EQUATORS.get(equator);
			if(EQ!=null)
			{
				switch(EQ.intValue())
				{
				case EQUATOR_$:
					if(pattern!=null)
					{
						if(!CMath.bset(codes,FLAG_SUBSTRING))
						{
							if(stat.matches(value))
							{
								matchStart=0;
								matchEnd=stat.length();
							}
						}
						else
						{
							M=pattern.matcher(stat);
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
					if(!CMath.bset(codes,FLAG_CASESENSITIVE))
					{
						stat=stat.toLowerCase();
						value=value.toLowerCase();
					}
					if(CMath.bset(codes,FLAG_SUBSTRING))
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
					if(!CMath.bset(codes,FLAG_CASESENSITIVE))
					{
						stat=stat.toLowerCase();
						value=value.toLowerCase();
					}
					if(CMath.bset(codes,FLAG_SUBSTRING))
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
					if(!CMath.bset(codes,FLAG_CASESENSITIVE))
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
					if(!CMath.bset(codes,FLAG_CASESENSITIVE))
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
					if(!CMath.bset(codes,FLAG_CASESENSITIVE))
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
					if(!CMath.bset(codes,FLAG_CASESENSITIVE))
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
			}
			if(matchStart>=0)
				matches.add(field,Integer.valueOf(matchStart),Integer.valueOf(matchEnd));
			if(CMath.bset(lastCode,FLAG_AND))
				checkedOut=checkedOut&&(matchStart>=0);
			else
			if(CMath.bset(lastCode,FLAG_OR))
				checkedOut=checkedOut||(matchStart>=0);
			else
				checkedOut=(matchStart>=0);
			lastCode=codes;
		}
		if(checkedOut)
		{
			if(changes.size()==0)
				mob.tell(CMLib.lang().L("Matched on @x1 from @x2.",E.name(),CMLib.map().getExtendedRoomID(room)));
			else
			for(int i=0;i<changes.size();i++)
			{
				field=(String)changes.get(i,1);
				value=(String)changes.get(i,3);
				codes=((Integer)changes.get(i,4)).intValue();
				if(field.equalsIgnoreCase("DESTROY"))
				{
					if(CMath.s_bool(value))
					{
						if(E instanceof Room)
						{
							Log.sysOut("GMODIFY","Room "+CMLib.map().getDescriptiveExtendedRoomID((Room)E)+" was DESTROYED by "+mob.Name()+"!");
							CMLib.map().obliterateRoom((Room)E);
						}
						else
						{
							Log.sysOut("GMODIFY",E.Name()+" in "+room.roomID()+" was DESTROYED by "+mob.Name()+"!");
							E.destroy();
						}
						return E;
					}
					continue;
				}
				if(noisy)
					gmodifydebugtell(mob,E.name()+" wants to change "+field+" value "+getStat(E,field)+" to "+value+"/"+(!getStat(E,field).equals(value)));
				if(CMath.bset(codes,FLAG_SUBSTRING))
				{
					int matchStart=-1;
					int matchEnd=-1;
					for(int m=0;m<matches.size();m++)
					{
						if(((String)matches.get(m,1)).equals(field))
						{
							matchStart=((Integer)matches.get(m,2)).intValue();
							matchEnd=((Integer)matches.get(m,3)).intValue();
						}
					}
					if(matchStart>=0)
					{
						stat=getStat(E,field);
						value=stat.substring(0,matchStart)+value+stat.substring(matchEnd);
					}
				}
				if(!getStat(E,field).equals(value))
				{
					Log.sysOut("GMODIFY","The "+CMStrings.capitalizeAndLower(field)+" field on "+E.Name()+" in "+room.roomID()+" was changed from "+getStat(E,field)+" to "+value+".");
					E=setStat(E,field,value);
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

	public void addEnumeratedStatCodes(Enumeration<? extends Modifiable> e, Set<String> allKnownFields, StringBuffer allFieldsMsg)
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

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final boolean noisy=CMSecurity.isDebugging(CMSecurity.DbgFlag.GMODIFY);
		Vector<Places> placesToDo=new Vector<Places>();
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
			final StringBuffer allFieldsMsg=new StringBuffer("");
			final Set<String> allKnownFields=new TreeSet<String>();
			addEnumeratedStatCodes(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.basicItems(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.weapons(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.armor(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			addEnumeratedStatCodes(CMClass.tech(),allKnownFields,allFieldsMsg);
			allFieldsMsg.append("CLASSTYPE ADDABILITY DELABILITY ADDBEHAVIOR DELBEHAVIOR ADDAFFECT DELAFFECT REJUV GENDER DESTROY ");
			mob.tell(L("Valid field names are @x1",allFieldsMsg.toString()));
			return false;
		}
		
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("room"))
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
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("area"))
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
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("world"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.GMODIFY))
			{
				mob.tell(L("You are not allowed to do that."));
				return false;
			}
			commands.remove(0);
			placesToDo=new Vector<Places>();
		}
		else
			placesToDo.add(mob.location());
		final DVector changes=new DVector(5);
		final DVector onfields=new DVector(5);
		DVector use=null;
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
		allFieldsMsg.append("CLASSTYPE REJUV DESTROY ADDABILITY DELABILITY ADDBEHAVIOR DELBEHAVIOR ADDAFFECT DELAFFECT ");
		allKnownFields.addAll(Arrays.asList(new String[]{"CLASSTYPE","REJUV","GENDER","DESTROY","ADDABILITY","DELABILITY","ADDBEHAVIOR","DELBEHAVIOR","ADDAFFECT","DELAFFECT"}));
		
		use=onfields;
		final Vector<String> newSet=new Vector<String>();
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
				Integer code=Integer.valueOf(0);
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
				final String equator=str.substring(eq,eq+divLen);
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
						code=Integer.valueOf(code.intValue()|FLAG_AND);
					else
					if(attach.equals("||"))
						code=Integer.valueOf(code.intValue()|FLAG_OR);
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
						code=Integer.valueOf(code.intValue()|FLAG_SUBSTRING);
					if(cd.equalsIgnoreCase("cs"))
						code=Integer.valueOf(code.intValue()|FLAG_CASESENSITIVE);
					val=val.substring(x2+1);
				}
				if(equator.equals("$"))
				{
					int patCodes=Pattern.DOTALL;
					if(!CMath.bset(code.intValue(),FLAG_CASESENSITIVE))
						patCodes=patCodes|Pattern.CASE_INSENSITIVE;
					P=Pattern.compile(val,patCodes);
				}
				key=key.toUpperCase().trim();
				if(allKnownFields.contains(key))
					use.add(key,equator,val,code,P);
				else
				{
					mob.tell(L("'@x1' is an unknown field name.  Valid fields include: @x2",key,allFieldsMsg.toString()));
					return false;
				}
			}
		}
		if((onfields.size()==0)&&(changes.size()==0))
		{
			mob.tell(L("You must specify either WHEN, or CHANGES parameters for valid matches to be made."));
			return false;
		}
		if(placesToDo.size()==0)
		for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if(A.getCompleteMap().hasMoreElements()
			&&CMSecurity.isAllowed(mob,(A.getCompleteMap().nextElement()),CMSecurity.SecFlag.GMODIFY))
				placesToDo.add(A);
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
				placesToDo.removeElement(A);
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
				if(mob.session()!=null)
					mob.session().rawPrint(".");
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
				mob.session().safeRawPrint(L("Searching, modifying and saving..."));
		}
		if(noisy)
			gmodifydebugtell(mob,"Rooms to do: "+placesToDo.size());
		if(noisy)
			gmodifydebugtell(mob,"When fields="+CMParms.toListString(onfields.getDimensionList(1)));
		if(noisy)
			gmodifydebugtell(mob,"Change fields="+CMParms.toListString(changes.getDimensionList(1)));
		Log.sysOut("GModify",mob.Name()+" "+whole+".");
		for(int r=0;r<placesToDo.size();r++)
		{
			Room R=(Room)placesToDo.get(r);
			if(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.GMODIFY))
				continue;
			if((R==null)||(R.roomID()==null)||(R.roomID().length()==0))
				continue;
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				if((mob.session()==null)||(mob.session().isStopped())||(R==null)||(R.getArea()==null))
					return false;
				final Area A=R.getArea();
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
				final Room possNewRoom=(Room)tryModfy(mob,R,R,changes,onfields,noisy);
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
					final Item newI=(Item)tryModfy(mob,R,I,changes,onfields,noisy);
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
						final MOB newM=(MOB)tryModfy(mob,R,M,changes,onfields,noisy);
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
								final Item newI=(Item)tryModfy(mob,R,I,changes,onfields,noisy);
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
								List<CoffeeShop.ShelfProduct> removeThese=new ArrayList<CoffeeShop.ShelfProduct>();
								for(final Iterator<CoffeeShop.ShelfProduct> i=SK.getShop().getStoreShelves();i.hasNext();)
								{
									final CoffeeShop.ShelfProduct P=i.next();
									final Environmental E2=P.product;
									if((E2 instanceof Item)||(E2 instanceof MOB))
									{
										final Environmental E3=tryModfy(mob,R,E2,changes,onfields,noisy);
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
									SK.getShop().deleteShelfProduct(P);
							}
						}
					}
				}
				if(saveroom)
					CMLib.database().DBUpdateRoom(R);
				if(saveitems)
					CMLib.database().DBUpdateItems(R);
				if(savemobs)
				{
					for(Enumeration<MOB> r1=R.inhabitants();r1.hasMoreElements();)
						System.out.println(r1.nextElement().fetchBehavior("VeryAggressive"));
					CMLib.database().DBUpdateMOBs(R);
				}
				if((mob.session()!=null)&&(changes.size()>0))
					mob.session().rawPrint(".");
				A.setAreaState(oldFlag);
				if(changes.size()==0)
					R.destroy();
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

		if(mob.session()!=null)
			mob.session().rawPrintln(L("!\n\rDone!"));
		Area A=null;
		for(int i=0;i<placesToDo.size();i++)
		{
			A=((Room)placesToDo.get(i)).getArea();
			if((A!=null)&&(A.getAreaState()!=Area.State.ACTIVE))
				A.setAreaState(Area.State.ACTIVE);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.GMODIFY);
	}
}
