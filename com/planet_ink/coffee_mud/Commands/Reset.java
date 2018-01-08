package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.Save.SaveTask;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Reset extends StdCommand
{
	public Reset()
	{
	}

	private final String[]	access	= I(new String[] { "RESET" });

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.RESET);
	}

	private enum helpSets
	{
		SKILLS("/resources/help/skill_help.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if(((classCode == Ability.ACODE_SKILL)
					||(classCode == Ability.ACODE_THIEF_SKILL)
					||(A.ID().startsWith("Paladin_")))
				&&((A.classificationCode()&Ability.ALL_DOMAINS)!=0)
				&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
					return true;
				return false;
			}
		}),
		CHANTS("/resources/help/chant_help.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_CHANT)
				||(A.ID().startsWith("Chant_")))
					return true;
				return false;
			}
		}),
		COMMON("/resources/help/common_help.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if(classCode == Ability.ACODE_COMMON_SKILL)
					return true;
				return false;
			}
		}),
		PRAYERS("/resources/help/prayer_help.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_PRAYER)
				||(A.ID().startsWith("Prayer_")))
					return true;
				return false;
			}
		}),
		SONGS("/resources/help/songs_help.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_SONG)
				||(A.ID().startsWith("Song_"))
				||(A.ID().startsWith("Play_"))
				||(A.ID().startsWith("Dance_")))
					return true;
				return false;
			}
		}),
		SPELLS("/resources/help/spell_help.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_SPELL)
				||(A.ID().startsWith("Spell_")))
					return true;
				return false;
			}
		}),
		TRAPS("/resources/help/traps.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_TRAP)
				||(A.ID().startsWith("Trap_")))
					return true;
				return false;
			}
		}),
		POISONS("/resources/help/poisons.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_POISON)
				||(A.ID().startsWith("Poison_")))
					return true;
				return false;
			}
		}),
		DISEASES("/resources/help/diseases.ini", new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_DISEASE)
				||(A.ID().startsWith("Disease_")))
					return true;
				return false;
			}
		}),
		PROPERTIES("/resources/help/arc_properties.ini", false, new Filterer<Ability>(){
			@Override
			public boolean passesFilter(Ability A)
			{
				final int classCode=A.classificationCode()&Ability.ALL_ACODES;
				if((classCode == Ability.ACODE_PROPERTY)
				||(A.ID().startsWith("Prop_")))
					return true;
				return false;
			}
		}),
		;
		public String file;
		public Filterer<Ability> filter;
		public boolean useName;
		private helpSets(String filePath, Filterer<Ability> filter)
		{
			this(filePath,true,filter);
		}

		private helpSets(String filePath, boolean useName, Filterer<Ability> filter)
		{
			file=filePath;
			this.useName=useName;
			this.filter=filter;
		}
	}
	
	public int resetAreaOramaManaI(MOB mob, Item I, Hashtable<String,Integer> rememberI, String lead)
		throws java.io.IOException
	{
		int nochange=0;
		if(I instanceof AmmunitionWeapon)
		{
			final AmmunitionWeapon W=(AmmunitionWeapon)I;
			if((W.requiresAmmunition())&&(W.ammunitionCapacity()>0))
			{
				String str=mob.session().prompt(L("@x1@x2 requires (@x3): ",lead,I.Name(),W.ammunitionType()));
				if(str.length()>0)
				{
					if((str.trim().length()==0)||(str.equalsIgnoreCase("no")))
					{
						W.setAmmunitionType("");
						W.setAmmoCapacity(0);
						W.setUsesRemaining(100);
						str=mob.session().prompt(L("@x1@x2 new weapon type: ",lead,I.Name()));
						W.setWeaponDamageType(CMath.s_int(str));
					}
					else
						W.setAmmunitionType(str.trim());
					nochange=1;
				}
			}
		}
		final Integer IT=rememberI.get(I.Name());
		if(IT!=null)
		{
			if(IT.intValue()==I.material())
			{
				mob.tell(L("@x1@x2 still @x3",lead,I.Name(),RawMaterial.CODES.NAME(I.material())));
				return nochange;
			}
			I.setMaterial(IT.intValue());
			mob.tell(L("@x1@x2 Changed to @x3",lead,I.Name(),RawMaterial.CODES.NAME(I.material())));
			return 1;
		}
		while(true)
		{
			final String str=mob.session().prompt(lead+I.Name()+"/"+RawMaterial.CODES.NAME(I.material()),"");
			if(str.equalsIgnoreCase("delete"))
				return -1;
			else
			if(str.length()==0)
			{
				rememberI.put(I.Name(),Integer.valueOf(I.material()));
				return nochange;
			}
			if(str.equals("?"))
				mob.tell(I.Name()+"/"+I.displayText()+"/"+I.description());
			else
			{
				final int material=RawMaterial.CODES.FIND_CaseSensitive(str.toUpperCase());
				if(material>=0)
				{
					I.setMaterial(RawMaterial.CODES.GET(material));
					mob.tell(L("@x1Changed to @x2",lead,RawMaterial.CODES.NAME(material)));
					rememberI.put(I.Name(),Integer.valueOf(I.material()));
					return 1;
				}
				final int possMat=RawMaterial.CODES.FIND_StartsWith(str);
				String poss;
				if(possMat<0)
				{
					poss="?";
					for(final String mat : RawMaterial.CODES.NAMES())
						if(mat.indexOf(str.toUpperCase())>=0)
							poss=mat;
				}
				else
					poss=RawMaterial.CODES.NAME(possMat);
				mob.tell(L("@x1'@x2' does not exist.  Try '@x3'.",lead,str,poss));
			}
		}
	}

	public void makeManufacturer(String[] names, TechType[] types)
	{
		if(names.length%3!=0)
			Log.errOut("Test: Not /3 names: "+CMParms.toListString(names));
		else
		{
			for(int i=0;i<6;i++)
			{
				Manufacturer M=CMLib.tech().getManufacturer(names[i]);
				if((M!=null)&&(M!=CMLib.tech().getDefaultManufacturer()))
				{
					Log.errOut("Dup Reset Manufacturer Name: "+names[i]);
					continue;
				}
				M=(Manufacturer)CMClass.getCommon("DefaultManufacturer");
				M.setName(names[i]);
				if(i%3==0)
				{
					M.setMinTechLevelDiff((byte)0);
					M.setMaxTechLevelDiff((byte)(M.getMinTechLevelDiff()+CMLib.dice().roll(1, 3, 2)));
				}
				else
				if(i%3==1)
				{
					M.setMinTechLevelDiff((byte)3);
					M.setMaxTechLevelDiff((byte)(M.getMinTechLevelDiff()+CMLib.dice().roll(1, 3, 2)));
				}
				else
				if(i%3==2)
				{
					M.setMinTechLevelDiff((byte)(8-CMLib.dice().roll(1, 3, 0)));
					M.setMaxTechLevelDiff((byte)10);
				}
				M.setEfficiencyPct(0.75+CMath.div(CMLib.dice().rollNormalDistribution(1, 50, 0),100.0));
				M.setReliabilityPct(0.75+CMath.div(CMLib.dice().rollNormalDistribution(1, 50, 0),100.0));
				M.setManufactureredTypesList(CMParms.toListString(types));
				CMLib.tech().addManufacturer(M);
			}
		}
	}

	protected int rightImportMat(MOB mob, Item I, boolean openOnly)
		throws java.io.IOException
	{
		if((I!=null)&&(I.description().trim().length()>0))
		{
			final int x=I.description().trim().indexOf(' ');
			final int y=I.description().trim().lastIndexOf(' ');
			if((x<0)||((x>0)&&(y==x)))
			{
				String s=I.description().trim().toLowerCase();
				if((mob!=null)&&(mob.session()!=null)&&(openOnly))
				{
					if(mob.session().confirm(L("Clear @x1/@x2/@x3 (Y/n)?",I.name(),I.displayText(),I.description()),"Y"))
					{
						I.setDescription("");
						return I.material();
					}
					return -1;
				}
				int rightMat=-1;
				for (final String[] objDesc : Import.objDescs)
				{
					if(objDesc[0].equals(s))
					{
						rightMat=CMath.s_int(objDesc[1]);
						break;
					}
				}
				s=I.description().trim().toUpperCase();
				if(rightMat<0)
				{
					Log.sysOut("Reset","Unconventional material: "+I.description());
					rightMat = RawMaterial.CODES.FIND_CaseSensitive(s);
				}
				if(rightMat<0)
					Log.sysOut("Reset","Unknown material: "+I.description());
				else
				if(I.material()!=rightMat)
				{
					if(mob!=null)
					{
						if(mob.session().confirm(L("Change @x1/@x2 material to @x3 (y/N)?",I.name(),I.displayText(),RawMaterial.CODES.NAME(rightMat)),"N"))
						{
							I.setMaterial(rightMat);
							I.setDescription("");
							return rightMat;
						}
					}
					else
					{
						Log.sysOut("Reset","Changed "+I.name()+"/"+I.displayText()+" material to "+RawMaterial.CODES.NAME(rightMat)+"!");
						I.setMaterial(rightMat);
						I.setDescription("");
						return rightMat;
					}
				}
				else
				{
					I.setDescription("");
					return rightMat;
				}
			}
		}
		return -1;
	}

	public String resetWarning(MOB mob, Area A)
	{
		Room R=null;
		final StringBuffer warning=new StringBuffer("");
		String roomWarning=null;
		for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
		{
			R=e.nextElement();
			roomWarning=resetWarning(mob,R);
			if(roomWarning!=null)
				warning.append(roomWarning);
		}
		if(warning.length()==0)
			return null;
		return warning.toString();
	}

	public String resetWarning(MOB mob, Room R)
	{
		if((mob==null)||(R==null))
			return null;
		final StringBuffer warning=new StringBuffer("");
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			if((S!=null)&&(S.mob()!=null)&&(S.mob()!=mob)&&(S.mob().location()==R))
				warning.append("A player, '"+S.mob().Name()+"' is in "+CMLib.map().getDescriptiveExtendedRoomID(R)+"\n\r");
		}
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if((I instanceof DeadBody)
			&&(((DeadBody)I).isPlayerCorpse()))
				warning.append("A player corpse, '"+I.Name()+"' is in "+CMLib.map().getDescriptiveExtendedRoomID(R)+"\n\r");
			else
			if((I instanceof PrivateProperty)
			&&(((PrivateProperty)I).getOwnerName().length()>0))
				warning.append("A private property, '"+I.Name()+"' is in "+CMLib.map().getDescriptiveExtendedRoomID(R)+"\n\r");
		}
		if(R instanceof GridLocale)
		{
			final List<Room> rooms=((GridLocale)R).getAllRooms();
			for(int r=0;r<rooms.size();r++)
			{
				final String s=resetWarning(mob,rooms.get(r));
				if(s!=null)
					warning.append(s);
			}
		}
		if(warning.length()==0)
			return null;
		return warning.toString();
	}

	private void reportChangesDestroyNewM(MOB oldM, MOB newM, StringBuffer changes)
	{
		if((changes == null)||(oldM==null))
			return;
		changes.append(newM.name()+":"+newM.basePhyStats().level()+", ");
		for(int i=0;i<oldM.getStatCodes().length;i++)
		{
			if((!oldM.getStat(oldM.getStatCodes()[i]).equals(newM.getStat(newM.getStatCodes()[i]))))
				changes.append(oldM.getStatCodes()[i]+"("+oldM.getStat(oldM.getStatCodes()[i])+"->"+newM.getStat(newM.getStatCodes()[i])+"), ");
		}
		changes.append("\n\r");
		newM.destroy(); // this was a copy
	}

	public boolean fixMob(MOB M, StringBuffer recordedChanges)
	{
		final MOB M2 = CMLib.leveler().fillOutMOB(M.baseCharStats().getCurrentClass(),M.basePhyStats().level());
		if((M.basePhyStats().attackAdjustment() != M2.basePhyStats().attackAdjustment())
		||(M.basePhyStats().armor() != M2.basePhyStats().armor())
		||(M.basePhyStats().damage() != M2.basePhyStats().damage())
		||(M.basePhyStats().speed() != M2.basePhyStats().speed()))
		{
			final MOB oldM=M;
			if(recordedChanges!=null)
				M=(MOB)M.copyOf();
			M.basePhyStats().setAttackAdjustment(M2.basePhyStats().attackAdjustment());
			M.basePhyStats().setArmor(M2.basePhyStats().armor());
			M.basePhyStats().setDamage(M2.basePhyStats().damage());
			M.basePhyStats().setSpeed(M2.basePhyStats().speed());
			M.recoverPhyStats();
			if(recordedChanges!=null)
			{
				reportChangesDestroyNewM(oldM,M,recordedChanges);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public boolean compareRaces(String usefulID, Race race1, Race race2, MOB tellM)
	{
		boolean same = true;
		for(String stat : race1.getStatCodes())
		{
			if(stat.equals("ID")||stat.equals("NAME"))
				continue;
			try
			{
				String val1=race1.getStat(stat);
				String val2=race2.getStat(stat);
				if(!val1.equals(val2))
				{
					same=false;
					if(tellM!=null)
						tellM.tell(usefulID+" "+stat+": '"+val1+"' != '"+val2+"'");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return same;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()<1)
		{
			if(CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDPLAYERS))
				mob.tell(L("Reset this ROOM, the whole AREA, REJUV, or a PASSWORD?"));
			else
				mob.tell(L("Reset this ROOM, the whole AREA, or REJUV?"));
			return false;
		}
		String s=commands.get(0);
		String rest=(commands.size()>1)?CMParms.combine(commands,1):"";
		if(s.equalsIgnoreCase("rejuv"))
		{
			commands.remove(0);
			if(commands.size()<1)
			{
				mob.tell(L("Rejuv this ROOM, or the whole AREA?  You can also specify ITEMS or MOBS after ROOM/AREA."));
				return false;
			}
			s=commands.get(0);
			rest=(commands.size()>1)?CMParms.combine(commands,1):"";
			int tickID=0;
			if(rest.startsWith("MOB"))
				tickID=Tickable.TICKID_MOB;
			if(rest.startsWith("ITEM"))
				tickID=Tickable.TICKID_ROOM_ITEM_REJUV;
			if(s.equalsIgnoreCase("room"))
			{
				CMLib.threads().rejuv(mob.location(),tickID);
				mob.tell(L("Done."));
			}
			else
			if(s.equalsIgnoreCase("area"))
			{
				final Area A=mob.location().getArea();
				for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
					CMLib.threads().rejuv(e.nextElement(),tickID);
				mob.tell(L("Done."));
			}
			else
			{
				mob.tell(L("Rejuv this ROOM, or the whole AREA?"));
				return false;
			}
		}
		else
		if(s.equalsIgnoreCase("relevel"))
		{
			if(mob.location()==null)
				return false;
			final Area A=mob.location().getArea();
			if(A==null)
				return false;
			if(CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))
			{
				mob.tell(L("You can not do that here."));
				return false;
			}
			commands.remove(0);
			if(commands.size()<=0)
			{
				mob.tell(L("You need to specify a new level range X - Y."));
				return false;
			}
			rest=(commands.size()>0)?CMParms.combine(commands,0):"";
			int x=rest.indexOf('-');
			if(x<0)
			{
				mob.tell(L("You need to specify a new level range X - Y."));
				return false;
			}
			final int levelLow = CMath.s_int(rest.substring(0,x).trim());
			final int levelHigh = CMath.s_int(rest.substring(x+1).trim());
			if((levelLow < 1)||(levelHigh<levelLow))
			{
				mob.tell(L("Illegal range "+rest.substring(0,x).trim()+" to "+rest.substring(x+1).trim()));
				return false;
			}
			final Session sess=mob.session();
			if((sess==null)
			||(sess.confirm(L("Re-Level the area '@x1' to between @x2 and @x3 (y/N)?",A.name(),""+levelLow,""+levelHigh),"N")))
			{
				if(sess!=null)
					sess.print(L("Working..."));
				final int[] stats = A.getAreaIStats();
				final int oldMinLevel = stats[Area.Stats.MIN_LEVEL.ordinal()];
				final int oldMaxLevel = stats[Area.Stats.MAX_LEVEL.ordinal()];
				for(final Enumeration<String> r=A.getProperRoomnumbers().getRoomIDs();r.hasMoreElements();)
				{
					if(sess!=null)
						sess.print(".");
					final Room R=CMLib.map().getRoom(r.nextElement());
					if((R!=null)&&(R.roomID()!=null)&&(R.roomID().length()>0))
					{
						final Room room=CMLib.coffeeMaker().makeNewRoomContent(R,false);
						if(room==null)
						{
							if(sess != null)
								sess.println(L("Unable to load room @x1, skipping.",CMLib.map().getExtendedRoomID(R)));
						}
						else
						if(CMLib.percolator().relevelRoom(room, oldMinLevel, oldMaxLevel, levelLow, levelHigh))
						{
							CMLib.database().DBUpdateItems(room);
							CMLib.database().DBUpdateMOBs(room);
							room.destroy();
							CMLib.map().resetRoom(R, true);
						}
						else
							room.destroy();
					}
				}
				if(sess!=null)
					sess.print(L("Done."));
			}
		}
		else
		if(s.equalsIgnoreCase("sorthelp"))
		{
			List<helpSets> sets = new ArrayList<helpSets>();
			if((rest==null)||(rest.length()==0))
			{
				mob.tell("Which? "+CMParms.toListString(helpSets.values())+", ALL");
				return false;
			}
			Boolean skipPrompt=null;
			if(rest.toLowerCase().startsWith("noprompt "))
			{
				rest=rest.substring(9);
				skipPrompt=Boolean.FALSE;
			}
			else
			if(rest.toLowerCase().startsWith("yesprompt "))
			{
				rest=rest.substring(10);
				skipPrompt=Boolean.TRUE;
			}

			if(rest.equalsIgnoreCase("ALL"))
			{
				sets.addAll(Arrays.asList(helpSets.values()));
			}
			else
			{
				final helpSets help=(helpSets)CMath.s_valueOf(helpSets.class, rest.toUpperCase().trim());
				if(help == null)
				{
					mob.tell("Which? "+CMParms.toListString(helpSets.values())+", ALL");
					return false;
				}
				sets.add(help);
			}
			
			for(final helpSets help : sets)
			{
				mob.tell("Processing: "+help.file);
				CMFile F=new CMFile(help.file,mob);
				
				List<String> batch=Resources.getFileLineVector(F.text());
				List<StringBuilder> batches = new ArrayList<StringBuilder>();
				Map<String,StringBuilder> ids=new Hashtable<String,StringBuilder>();
				StringBuilder currentBatch = null;
				boolean continueLine = false;
				for(String s1 : batch)
				{
					int x=s1.indexOf('=');
					if(currentBatch == null)
					{
						if(s1.trim().length()==0)
							continue;
						if(x<0)
						{
							mob.tell("Unstarted batch at "+s1);
							return false;
						}
						else
						{
							currentBatch=new StringBuilder("");
							if(CMStrings.isUpperCase(s1.substring(0, x)))
								ids.put(s1.substring(0, x), currentBatch);
							batches.add(currentBatch);
							currentBatch.append(s1).append("\n\r");
						}
					}
					else
					{
						if(continueLine)
						{
							currentBatch.append(s1).append("\n\r");
							if(!s1.endsWith("\\"))
							{
								currentBatch=null;
								continueLine=false;
							}
						}
						else
						{
							if((x>0)&&(CMStrings.isUpperCase(s1.substring(0, x))))
								ids.put(s1.substring(0, x), currentBatch);
							currentBatch.append(s1).append("\n\r");
							if(s1.endsWith("\\"))
								continueLine=true;
						}
					}
				}
				PairList<String,StringBuilder> skills=new PairVector<String,StringBuilder>();
				for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					final Ability A=e.nextElement();
					if(help.filter.passesFilter(A))
					{
						StringBuilder[] bup=new StringBuilder[10];
						bup[0]=ids.get(A.Name().toUpperCase().replace(' ','_'));
						if(bup[0]==null)
						{
							int xx=A.ID().indexOf('_');
							if(xx>0)
								bup[0]=ids.get((A.ID().substring(0,xx)+"_"+A.Name()).toUpperCase().replace(' ','_'));
						}
						bup[1]=ids.get(A.ID().toUpperCase().replace(' ','_'));
						if((bup[0]==null)&&(bup[1]==null))
						{
							mob.tell("Warning: Not found: "+A.ID());
						}
						else
						if((bup[0]!=null)&&(bup[1]!=null)&&(bup[0]!=bup[1]))
						{
							mob.tell("Warning: Mis found: "+A.ID());
							mob.tell("1: "+bup[0].toString());
							mob.tell("2: "+bup[1].toString());
							return false;
						}
						else
						{
							StringBuilder bp=bup[1];
							if(bp==null)
							{
								bp=bup[0];
								int xx=bp.toString().indexOf("=<ABILITY>");
								if(xx>0)
								{
									ids.put(A.ID().toUpperCase(), bp);
									int yy=bp.toString().lastIndexOf('\r',xx);
									if(yy>0)
									{
										bp.insert(0, A.ID().toUpperCase()+"="+bp.toString().substring(yy+1,xx)+"\n\r");
									}
									else
									{
										bp.insert(0, A.ID().toUpperCase()+"="+bp.toString().substring(0,xx)+"\n\r");
									}
								}
							}
							if(!batches.contains(bp))
							{
								mob.tell("Warning: Re found: "+A.ID());
								mob.tell("Info   : Re found: "+bp.toString());
							}
							else
							{
								if(help.useName)
									skills.add(A.Name().toUpperCase(),bp);
								else
									skills.add(A.ID().toUpperCase(),bp);
								batches.remove(bp);
							}
						}
					}
				}
				for(StringBuilder b : batches)
				{
					int x=b.toString().indexOf('=');
					if(x<0)
					{
						mob.tell("Error: Unused: "+CMStrings.replaceAll(CMStrings.replaceAll(CMStrings.replaceAll(CMStrings.replaceAll(b.substring(0,30),"\n"," "),"\r"," "),"\\n"," "),"\\r"," "));
						return false;
					}
					else
					{
						mob.tell("Warning: Unused: "+CMStrings.replaceAll(CMStrings.replaceAll(CMStrings.replaceAll(CMStrings.replaceAll(b.substring(0,30),"\n"," "),"\r"," "),"\\n"," "),"\\r"," "));
						skills.add(b.substring(0,x).toUpperCase(),b);
					}
				}
				Collections.sort(skills,new Comparator<Pair<String,StringBuilder>>()
				{
					@Override
					public int compare(Pair<String, StringBuilder> o1, Pair<String, StringBuilder> o2)
					{
						return o1.first.compareTo(o2.first);
					}
				});
				StringBuilder finalTxt = new StringBuilder("");
				for(Pair<String,StringBuilder> p : skills)
				{
					finalTxt.append(p.second);
					finalTxt.append("\n\r");
				}
				if((skipPrompt==Boolean.TRUE)
				||((skipPrompt==null)
					&&(mob.session()!=null)
					&&(mob.session().confirm("Save (y/N)?", "N"))))
					F.saveText(finalTxt);
			}
			return true;
		}
		else
		if(s.equalsIgnoreCase("room"))
		{
			final String warning=resetWarning(mob, mob.location());
			if((mob.session()==null)
			||(warning==null)
			||(mob.session().confirm(L("@x1\n\rReset the contents of the room '@x2', OK (Y/n)?",warning,mob.location().displayText(mob)),"Y")))
			{
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S!=null)
					&&(S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(S.mob().location()==mob.location()))
						S.mob().tell(mob,null,null,L("<S-NAME> order(s) this room to normalcy."));
				}
				CMLib.map().resetRoom(mob.location(), true);
				mob.location().giveASky(0);
				mob.tell(L("Done."));
			}
			else
				mob.tell(L("Cancelled."));
		}
		else
		if(s.equalsIgnoreCase("password") 
		&&(CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDPLAYERS)))
		{
			commands.remove(0);
			if(commands.size()<1)
			{
				mob.tell(L("Reset password for what character/account?."));
				return false;
			}
			final String name=CMParms.combine(commands,0);
			String finalName= "";
			AccountStats stat = null;
			MOB M=null;
			if(CMProps.isUsingAccountSystem())
			{
				final PlayerAccount A=CMLib.players().getLoadAccount(name);
				if(A!=null)
				{
					stat=A;
					finalName=A.getAccountName();
				}
			}
			if(stat == null)
			{
				M=CMLib.players().getLoadPlayer(name);
				if(M!=null)
				{
					final PlayerStats pStats=M.playerStats();
					if(pStats!=null)
					{
						if((CMProps.isUsingAccountSystem())
						&&(pStats.getAccount()!=null))
						{
							stat=pStats.getAccount();
							finalName=pStats.getAccount().getAccountName();
						}
						else
						{
							stat=pStats;
							finalName=M.Name();
						}
					}
				}
			}
			if(stat==null)
			{
				mob.tell(L("'@x1' is not a player or account.",name));
				return false;
			}
			if(CMLib.smtp().isValidEmailAddress(stat.getEmail()))
			{
				if((mob.session()==null)||(mob.session().confirm(L("Generate a random password for '@x1' and email to '@x2' (Y/n)?",finalName,stat.getEmail()),"Y")))
				{
					String password=CMLib.encoder().generateRandomPassword();
					stat.setPassword(password);
					if(stat instanceof PlayerAccount)
						CMLib.database().DBUpdateAccount((PlayerAccount)stat);
					if(M!=null)
						CMLib.database().DBUpdatePassword(M.Name(),stat.getPasswordStr());
					CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), finalName, "noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), stat.getEmail(),
							"Password for "+finalName,
							"Your password for "+finalName+" at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" has been reset by "+mob.Name()+".  It is now: '"+password+"'.");
					mob.tell(L("The password has been reset, and this action has been logged."));
					Log.sysOut("Reset","Password for "+finalName+" has been reset by "+mob.Name());
				}
			}
			else
			if((mob.session()==null)||(mob.session().confirm(L("Would you like to set the password for '@x1' to '@x2' (Y/n)?",finalName,finalName.toLowerCase()),"Y")))
			{
				String password=finalName.toLowerCase();
				stat.setPassword(password);
				if(stat instanceof PlayerAccount)
					CMLib.database().DBUpdateAccount((PlayerAccount)stat);
				if(M!=null)
					CMLib.database().DBUpdatePassword(M.Name(),stat.getPasswordStr());
				CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), finalName, "noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), stat.getEmail(),
						"Password for "+finalName,
						"Your password for "+finalName+" at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" has been reset by "+mob.Name()+".  It is now: '"+password+"'.");
				mob.tell(L("The password has been reset, and this action has been logged."));
				Log.sysOut("Reset","Password for "+finalName+" has been reset by "+mob.Name());
			}
			else
				mob.tell(L("Cancelled."));
		}
		else
		if(s.equalsIgnoreCase("INIFILE")||s.equalsIgnoreCase("coffeemud.ini"))
		{
			CMProps.instance().resetSecurityVars();
			CMProps.instance().resetSystemVars();
			mob.tell(L("Done."));
		}
		else
		if(s.equalsIgnoreCase("area"))
		{
			final Area A=mob.location().getArea();
			if(A!=null)
			{
				final String warning=resetWarning(mob, A);
				if(warning!=null)
					mob.tell(warning);
				if((mob.session()==null)||(mob.session().confirm(L("Reset the contents of the area '@x1', OK (Y/n)?",A.name()),"Y")))
				{
					for(final Session S : CMLib.sessions().localOnlineIterable())
					{
						if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(A.inMyMetroArea(S.mob().location().getArea())))
							S.mob().tell(mob,null,null,L("<S-NAME> order(s) this area to normalcy."));
					}
					CMLib.map().resetArea(A);
					mob.tell(L("Done."));
				}
				else
					mob.tell(L("Cancelled."));
			}
		}
		else
		if(CMLib.players().getPlayer(s)!=null)
		{
			final MOB M=CMLib.players().getPlayer(s);
			String what="";
			if(commands.size()>0)
				what=CMParms.combine(commands,1).toUpperCase();
			if(what.startsWith("EXPERTIS"))
			{
				M.delAllExpertises();
				mob.tell(L("Done."));
			}
			else
				mob.tell(L("Can't reset that trait -- as its not defined."));
		}
		else
		if(s.equalsIgnoreCase("roomids")&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS)))
		{
			final Area A=mob.location().getArea();
			boolean somethingDone=false;
			int number=0;
			for(final Enumeration e=A.getCompleteMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				if((R.roomID().length()>0)&&(CMLib.map().getRoom(A.Name()+"#"+(number++))!=null))
				{
					mob.tell(L("Can't renumber rooms -- a number is too low."));
					somethingDone=true;
					break;
				}
			}
			number=0;
			if(!somethingDone)
			for(final Enumeration e=A.getCompleteMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					if(R.roomID().length()>0)
					{
						final String oldID=R.roomID();
						R.setRoomID(A.Name()+"#"+(number++));
						CMLib.database().DBReCreate(R,oldID);
						try
						{
							for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
							{
								Room R2=r.nextElement();
								R2=CMLib.map().getRoom(R2);
								if(R2!=R)
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
								{
									if(R2.rawDoors()[d]==R)
									{
										CMLib.database().DBUpdateExits(R2);
										break;
									}
								}
							}
						}
						catch (final NoSuchElementException nse)
						{
						}
						if(R instanceof GridLocale)
							R.getArea().fillInAreaRoom(R);
						somethingDone=true;
						mob.tell(L("Room @x1 changed to @x2.",oldID,R.roomID()));
					}
				}
			}
			if(!somethingDone)
				mob.tell(L("No rooms were found which needed renaming."));
			else
				mob.tell(L("Done renumbering rooms."));
		}
		else
		if(s.equalsIgnoreCase("arearoomids")&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS)))
		{
			final Area A=mob.location().getArea();
			boolean somethingDone=false;
			for(final Enumeration e=A.getCompleteMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					if((R.roomID().length()>0)
					&&(R.roomID().indexOf('#')>0)
					&&(!R.roomID().startsWith(A.Name())))
					{
						final String oldID=R.roomID();
						R.setRoomID(R.getArea().getNewRoomID(R,-1));
						CMLib.database().DBReCreate(R,oldID);
						try
						{
							for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
							{
								Room R2=r.nextElement();
								R2=CMLib.map().getRoom(R2);
								if(R2!=R)
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
								{
									if(R2.rawDoors()[d]==R)
									{
										CMLib.database().DBUpdateExits(R2);
										break;
									}
								}
							}
						}
						catch (final NoSuchElementException nse)
						{
						}
						if(R instanceof GridLocale)
							R.getArea().fillInAreaRoom(R);
						somethingDone=true;
						mob.tell(L("Room @x1 changed to @x2.",oldID,R.roomID()));
					}
				}
			}
			if(!somethingDone)
				mob.tell(L("No rooms were found which needed renaming."));
			else
				mob.tell(L("Done renumbering rooms."));
		}
		else
		if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.RESETUTILS))
		{
			mob.tell(L("'@x1' is an unknown reset.  Try ROOM, AREA, AREAROOMIDS *.\n\r * = Reset functions which may take a long time to complete.",s));
			return false;
		}
		else
		if(s.equalsIgnoreCase("propertygarbage"))
		{
			if(mob.session().confirm(L("Reset all unowned property to default room descriptions?"), L("N")))
			{
				Room R=null;
				LandTitle T=null;
				for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						T=CMLib.law().getLandTitle(R);
						if((T!=null)
						&&(T.getOwnerName().length()==0))
						{
							T.setOwnerName(mob.Name());
							T.setOwnerName("");
							T.updateLot(new XVector<String>(mob.name()));
						}
					}
				}
			}
		}
		else
		if(s.equalsIgnoreCase("racestatgains")&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES)))
		{
			if(mob.session().confirm(L("Alter the stat gains every generic race automatically?"), L("N")))
			{
				for(final Enumeration e=CMClass.races();e.hasMoreElements();)
				{
					final Race R=(Race)e.nextElement();
					if(R.isGeneric())
					{
						final CharStats ADJSTAT1=(CharStats)CMClass.getCommon("DefaultCharStats");
						ADJSTAT1.setAllValues(0);
						CMLib.coffeeMaker().setCharStats(ADJSTAT1,R.getStat("ASTATS"));
						boolean save=false;
						for(final int i: CharStats.CODES.BASECODES())
						{
							if(ADJSTAT1.getStat(i)>5)
							{
								ADJSTAT1.setStat(i,5);
								save=true;
							}
						}
						if(save)
						{
							R.setStat("ASTATS",CMLib.coffeeMaker().getCharStatsStr(ADJSTAT1));
							mob.tell(L("Modified @x1",R.ID()));
							CMLib.database().DBDeleteRace(R.ID());
							CMLib.database().DBCreateRace(R.ID(),R.racialParms());
						}
					}
				}
			}
		}
		else
		if(s.equalsIgnoreCase("genraceagingcharts")&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES)))
		{
			if(mob.session().confirm(L("Alter the aging charts of every race automatically?"), L("N")))
			{
				for(final Enumeration e=CMClass.races();e.hasMoreElements();)
				{
					final Race R=(Race)e.nextElement();
					if(R.isGeneric())
					{
						final List<Race> racesToBaseFrom=CMLib.utensils().getConstituantRaces(R.ID());
						if(racesToBaseFrom.size()>0)
						{
							final StringBuffer answer=new StringBuffer(R.ID()+": ");
							for(int i=0;i<racesToBaseFrom.size();i++)
								answer.append(racesToBaseFrom.get(i).ID()+" ");
							mob.tell(answer.toString());
							if(racesToBaseFrom.size()>0)
							{
								final long[] ageChart=new long[Race.AGE_ANCIENT+1];
								for(int i=0;i<racesToBaseFrom.size();i++)
								{
									final Race R2=racesToBaseFrom.get(i);
									int lastVal=0;
									for(int x=0;x<ageChart.length;x++)
									{
										int val=R2.getAgingChart()[x];
										if(val>=Integer.MAX_VALUE)
											val=lastVal+(x*1000);
										ageChart[x]+=val;
										lastVal=val;
									}
								}
								for(int x=0;x<ageChart.length;x++)
									ageChart[x]=ageChart[x]/racesToBaseFrom.size();
								int lastVal=0;
								int thisVal=0;
								for(int x=0;x<ageChart.length;x++)
								{
									lastVal=thisVal;
									thisVal=(int)ageChart[x];
									if(thisVal<lastVal)
										thisVal+=lastVal;
									R.getAgingChart()[x]=thisVal;
								}
								CMLib.database().DBDeleteRace(R.ID());
								CMLib.database().DBCreateRace(R.ID(),R.racialParms());
							}
						}
					}
				}
			}
		}
		else
		if(s.equalsIgnoreCase("genmixedracebuilds")&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES)))
		{
			List<Race> racesToDo = new ArrayList<Race>();
			if(rest.length()==0)
			{
				if(mob.session().confirm(L("Recreate all the gen-mixed-races?!"), L("N")))
				{
					for(final Enumeration e=CMClass.races();e.hasMoreElements();)
					{
						final Race R=(Race)e.nextElement();
						if(R.isGeneric())
						{
							final List<Race> racesToBaseFrom=CMLib.utensils().getConstituantRaces(R.ID());
							if(racesToBaseFrom.size()>1)
							{
								racesToDo.add(R);
							}
						}
					}
				}
			}
			else
			{
				final Race R=CMClass.getRace(rest);
				if(R==null)
					mob.tell(L("No such race as ",rest));
				else
					racesToDo.add(R);
			}
				
			if(racesToDo.size()>0)
			{
				for(Race R : racesToDo)
				{
					CMLib.database().DBDeleteRace(R.ID());
					CMClass.delRace(R);
				}
				for(Race R : racesToDo)
				{
					final List<Race> racesToBaseFrom=CMLib.utensils().getConstituantRaces(R.ID());
					Race R1=racesToBaseFrom.get(0);
					for(int r=1;r<racesToBaseFrom.size();r++)
					{
						Race R2=racesToBaseFrom.get(r);
						Race R3=CMLib.utensils().getMixedRace(R1.ID(),R2.ID(), false);
						R1=R3;
					}
					if(!R1.ID().equals(R.ID()))
					{
						if(R1.ID().equalsIgnoreCase(R.ID()))
						{
							String oldID = R.ID();
							R1.setStat("ID", R.ID());
							mob.tell("Name Fixed: "+oldID+" into "+R1.ID());
							CMLib.database().DBDeleteRace(oldID);
							CMLib.database().DBCreateRace(R1.ID(), R1.racialParms());
						}
						else
						{
							Race R1R=(Race)R1.copyOf();
							R1R.setStat("ID", R.ID());
							if(R.ID().length()!=R1R.ID().length())
							{
								mob.tell("Redoing: "+R1R.ID()+" because "+R1.ID()+"!="+R.ID());
								R1=racesToBaseFrom.get(0);
								for(int r=1;r<racesToBaseFrom.size();r++)
								{
									Race R2=racesToBaseFrom.get(r);
									Race R3=CMLib.utensils().getMixedRace(R1.ID(),R2.ID(), true);
									R1=R3;
								}
								if(!R1.ID().equals(R.ID()))
								{
									mob.tell("UGH -- Giving up on "+R.ID()+" and keeping it...");
									CMLib.database().DBCreateRace(R.ID(), R.racialParms());
									CMClass.addRace(R);
								}
							}
							else
							{
								CMLib.database().DBCreateRace(R1R.ID(), R1R.racialParms());
								CMClass.addRace(R1R);
								mob.tell("Duplicated: "+R1.ID()+" into "+R1R.ID());
							}
						}
					}
					else
						mob.tell("Recreated: "+R1.ID());
				}
			}
		}
		else
		if(s.equalsIgnoreCase("bankdata")&&(CMSecurity.isASysOp(mob)))
		{
			final String bank=CMParms.combine(commands,1);
			if(bank.length()==0)
			{
				mob.tell(L("Which bank?"));
				return false;
			}
			if(mob.session().confirm(L("Inspect and update all COIN objects in player bank accounts?"), L("N")))
			{
				final List<JournalEntry> V=CMLib.database().DBReadJournalMsgsByUpdateDate(bank, true);
				for(int v=0;v<V.size();v++)
				{
					final JournalEntry V2=V.get(v);
					final String name=V2.from();
					final String ID=V2.subj();
					String classID=V2.to();
					final String data=V2.msg();
					if(ID.equalsIgnoreCase("COINS"))
						classID="COINS";
					final Item I=(Item)CMClass.getItem("GenItem").copyOf();
					CMLib.database().DBCreatePlayerData(name,bank,""+I,classID+";"+data);
				}
				CMLib.database().DBDeleteJournal(bank,null); // banks are no longer journaled
				mob.tell(L("@x1 records done.",""+V.size()));
			}
		}
		else
		if(s.equalsIgnoreCase("mobstats")&&(CMSecurity.isASysOp(mob)))
		{
			s="room";
			if(commands.size()>1)
				s=commands.get(1);
			if(mob.session()==null)
				return false;
			if(mob.session().confirm(L("Alter every mobs combat stats to system defaults?!"), L("N")))
			{
				mob.session().print(L("working..."));
				StringBuffer recordedChanges=null;
				for(int i=1;i<commands.size();i++)
				{
					if(commands.get(i).equalsIgnoreCase("NOSAVE"))
					{
						recordedChanges=new StringBuffer("");
						break;
					}
				}
				final Vector<Room> rooms=new Vector<Room>();
				if(s.toUpperCase().startsWith("ROOM"))
					rooms.add(mob.location());
				else
				if(s.toUpperCase().startsWith("AREA"))
				{
					try
					{
						for(final Enumeration<Room> e=mob.location().getArea().getCompleteMap();e.hasMoreElements();)
							rooms.add(e.nextElement());
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				else
				if(s.toUpperCase().startsWith("CATALOG"))
				{
					try
					{
						final MOB[] mobs=CMLib.catalog().getCatalogMobs();
						for (final MOB M : mobs)
						{
							if(fixMob(M,recordedChanges))
							{
								mob.tell(L("Catalog mob @x1 done.",M.Name()));
								CMLib.catalog().updateCatalog(M);
							}
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				else
				if(s.toUpperCase().startsWith("WORLD"))
				{
					try
					{
						for(final Enumeration e=CMLib.map().areas();e.hasMoreElements();)
						{
							final Area A=(Area)e.nextElement();
							boolean skip=false;
							for(int i=1;i<commands.size();i++)
							{
								if(commands.get(i).equalsIgnoreCase(A.Name())||rest.equalsIgnoreCase(A.Name()))
								{
									skip=true;
									break;
								}
							}
							if(skip)
								continue;
							for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
								rooms.add(r.nextElement());
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				else
				{
					mob.tell(L("Try ROOM, AREA, CATALOG, or WORLD."));
					return false;
				}
				if(recordedChanges!=null)
					mob.session().println(".");
				for(final Enumeration r=rooms.elements();r.hasMoreElements();)
				{
					Room R=CMLib.map().getRoom((Room)r.nextElement());
					if(R!=null)
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						if(R==null)
							continue;
						if((recordedChanges!=null)&&(recordedChanges.length()>0))
						{
							mob.session().rawOut(recordedChanges.toString());
							recordedChanges.setLength(0);
						}
						R.getArea().setAreaState(Area.State.FROZEN);
						CMLib.map().resetRoom(R, true);
						boolean somethingDone=false;
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M.isSavable())
							&&(!CMLib.flags().isCataloged(M))
							&&(M.getStartRoom()==R))
								somethingDone=fixMob(M,recordedChanges) || somethingDone;
						}
						if(somethingDone)
						{
							mob.tell(L("Room @x1 done.",R.roomID()));
							CMLib.database().DBUpdateMOBs(R);
						}
						if(R.getArea().getAreaState()!=Area.State.ACTIVE)
							R.getArea().setAreaState(Area.State.ACTIVE);
					}
					if(recordedChanges==null)
						mob.session().print(".");
				}
				if((recordedChanges!=null)&&(recordedChanges.length()>0))
					mob.session().rawOut(recordedChanges.toString());
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.equalsIgnoreCase("groundlydoors")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null)
				return false;
			if(mob.session().confirm(L("Alter every door called 'the ground'?!"), L("N")))
			{
				mob.session().print(L("working..."));
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						boolean changed=false;
						if(R.roomID().length()>0)
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							final Exit E=R.getRawExit(d);
							if((E!=null)&&E.hasADoor()&&E.name().equalsIgnoreCase("the ground"))
							{
								E.setName(L("a door"));
								E.setExitParams("door","close","open","a door, closed.");
								changed=true;
							}
						}
						if(changed)
						{
							Log.sysOut("Reset","Groundly doors in "+R.roomID()+" fixed.");
							CMLib.database().DBUpdateExits(R);
						}
						mob.session().print(".");
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.equalsIgnoreCase("allmobarmorfix")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null)
				return false;
			if(mob.session().confirm(L("Change all mobs armor to the codebase defaults?"), L("N")))
			{
				mob.session().print(L("working..."));
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					A.setAreaState(Area.State.FROZEN);
					for(final Enumeration r=A.getCompleteMap();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(R.roomID().length()==0)
							continue;
						synchronized(("SYNC"+R.roomID()).intern())
						{
							R=CMLib.map().getRoom(R);
							CMLib.map().resetRoom(R, true);
							boolean didSomething=false;
							for(int i=0;i<R.numInhabitants();i++)
							{
								final MOB M=R.fetchInhabitant(i);
								if((M.isMonster())
								&&(M.getStartRoom()==R)
								&&(M.basePhyStats().armor()==((100-(M.basePhyStats().level()*7)))))
								{
									final int oldArmor=M.basePhyStats().armor();
									M.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(M));
									M.recoverPhyStats();
									Log.sysOut("Reset","Updated "+M.name()+" in room "+R.roomID()+" from "+oldArmor+" to "+M.basePhyStats().armor()+".");
									didSomething=true;
								}
								else
									Log.sysOut("Reset","Skipped "+M.name()+" in room "+R.roomID());
							}
							mob.session().print(".");
							if(didSomething)
								CMLib.database().DBUpdateMOBs(R);
						}
					}
					if(A.getAreaState()!=Area.State.ACTIVE)
						A.setAreaState(Area.State.ACTIVE);
				}
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.equalsIgnoreCase("goldceilingfixer")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null)
				return false;
			if(mob.session().confirm(L("Alter every mobs money to system defaults?!"), L("N")))
			{
				mob.session().print(L("working..."));
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					A.setAreaState(Area.State.FROZEN);
					for(final Enumeration r=A.getCompleteMap();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(R.roomID().length()==0)
							continue;
						synchronized(("SYNC"+R.roomID()).intern())
						{
							R=CMLib.map().getRoom(R);
							CMLib.map().resetRoom(R, true);
							boolean didSomething=false;
							for(int i=0;i<R.numInhabitants();i++)
							{
								final MOB M=R.fetchInhabitant(i);
								if((M.isMonster())
								&&(M.getStartRoom()==R)
								&&(CMLib.beanCounter().getMoney(M)>(M.basePhyStats().level()+1)))
								{
									CMLib.beanCounter().setMoney(M,CMLib.dice().roll(1,M.basePhyStats().level(),0)+CMLib.dice().roll(1,10,0));
									Log.sysOut("Reset","Updated "+M.name()+" in room "+R.roomID()+".");
									didSomething=true;
								}
							}
							mob.session().print(".");
							if(didSomething)
								CMLib.database().DBUpdateMOBs(R);
						}
					}
					if(A.getAreaState()!=Area.State.ACTIVE)
						A.setAreaState(Area.State.ACTIVE);
				}
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.equalsIgnoreCase("areainstall")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null)
				return false;
			if(commands.size()<2)
			{
				mob.tell(L("You need to specify a property or behavior to install."));
				return false;
			}
			final String ID=commands.get(1);
			Object O=CMClass.getAbility(ID);
			if(O==null)
				O=CMClass.getBehavior(ID);
			if(O==null)
			{
				mob.tell(L("'@x1' is not a known property or behavior.  Try LIST.",ID));
				return false;
			}

			if(mob.session().confirm(L("Add this behavior/property to every Area?"), L("N")))
			{
				mob.session().print(L("working..."));
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					boolean changed=false;
					if((O instanceof Behavior))
					{
						Behavior B=A.fetchBehavior(((Behavior)O).ID());
						if(B==null)
						{
							B=(Behavior)((Behavior)O).copyOf();
							B.setParms(CMParms.combine(commands,2));
							A.addBehavior(B);
							changed=true;
						}
						else
						if(!B.getParms().equals(CMParms.combine(commands,2)))
						{
							B.setParms(CMParms.combine(commands,2));
							changed=true;
						}
					}
					else
					if(O instanceof Ability)
					{
						Ability B=A.fetchEffect(((Ability)O).ID());
						if(B==null)
						{
							B=(Ability)((Ability)O).copyOf();
							B.setMiscText(CMParms.combine(commands,2));
							A.addNonUninvokableEffect(B);
							changed=true;
						}
						else
						if(!B.text().equals(CMParms.combine(commands,2)))
						{
							B.setMiscText(CMParms.combine(commands,2));
							changed=true;
						}
					}
					if(changed)
					{
						CMLib.database().DBUpdateArea(A.Name(),A);
						mob.session().print(".");
					}
				}
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.equalsIgnoreCase("worldmatconfirm")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null)
				return false;
			if(mob.session().confirm(L("Begin scanning and altering the material type of all items?"), L("N")))
			{
				mob.session().print(L("working..."));
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					A.setAreaState(Area.State.FROZEN);
					for(final Enumeration r=A.getCompleteMap();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(R.roomID().length()>0)
						{
							synchronized(("SYNC"+R.roomID()).intern())
							{
								R=CMLib.map().getRoom(R);
								CMLib.map().resetRoom(R, true);
								boolean changedMOBS=false;
								boolean changedItems=false;
								for(int i=0;i<R.numItems();i++)
									changedItems=changedItems||(rightImportMat(null,R.getItem(i),false)>=0);
								for(int m=0;m<R.numInhabitants();m++)
								{
									final MOB M=R.fetchInhabitant(m);
									if(M==mob)
										continue;
									if(!M.isSavable())
										continue;
									for(int i=0;i<M.numItems();i++)
										changedMOBS=changedMOBS||(rightImportMat(null,M.getItem(i),false)>=0);
									final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
									if(SK!=null)
									{
										for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
										{
											final Environmental E=i.next();
											if(E instanceof Item)
											{
												final Item I=(Item)E;
												boolean didSomething=false;
												didSomething=rightImportMat(null,I,false)>=0;
												changedMOBS=changedMOBS||didSomething;
												if(didSomething)
												{
													final int numInStock=SK.getShop().numberInStock(I);
													final int stockPrice=SK.getShop().stockPrice(I);
													SK.getShop().delAllStoreInventory(I);
													SK.getShop().addStoreInventory(I,numInStock,stockPrice);
												}
											}
										}
									}
								}
								if(changedItems)
									CMLib.database().DBUpdateItems(R);
								if(changedMOBS)
									CMLib.database().DBUpdateMOBs(R);
								mob.session().print(".");
							}
						}
					}
					if(A.getAreaState()!=Area.State.ACTIVE)
						A.setAreaState(Area.State.ACTIVE);
				}
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.equalsIgnoreCase("mixedraces")&&(CMSecurity.isASysOp(mob)))
		{
			List<List<Race>> raceSets=new ArrayList<List<Race>>();
			for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				Race R=r.nextElement();
				if(R.isGeneric())
				{
					Set<String> genericOnlyFound=new TreeSet<String>();
					List<String> raceParts = new ArrayList<String>();
					String name=R.name();
					if(name.startsWith("Half "))
					{
						name=name.substring(5);
						raceParts.add("Human");
					}
					raceParts.addAll(Arrays.asList(name.split("-")));
					for(int i=0;i<raceParts.size();i++)
					{
						if(raceParts.get(i).equalsIgnoreCase("Man")
						&&(i<raceParts.size()-1)
						&&raceParts.get(i+1).equalsIgnoreCase("Scorpion"))
						{
							raceParts.set(i, "Man-Scorpion");
							raceParts.remove(i+1);
						}
					}
					List<Race> races = new ArrayList<Race>();
					raceSets.add(races);
					for(int i=0;i<raceParts.size();i++)
					{
						final String racePart=raceParts.get(i);
						Race R1=CMClass.findRace(racePart);
						if(R1!=null)
						{
							if(!R1.isGeneric())
								races.add(R1);
							else
							{
								String lastPart=raceParts.get(i);
								if(lastPart.equals("Whelpling"))
									races.add(R1);
								else
								if(!lastPart.endsWith("ling"))
									races.add(R1);
								else
								if(CMClass.findRace(lastPart.substring(0,lastPart.length()-4))!=null)
									races.add(R1);
								else
									genericOnlyFound.add(R1.ID());
							}
						}
						else
						{
							mob.tell("No race: "+racePart +" from "+R.name()+"("+R.ID()+")");
						}
					}
					if(genericOnlyFound.size()>0)
						mob.tell("Found generic races not handled: "+CMParms.toListString(genericOnlyFound));
				}
			}
			for(int i=raceSets.size()-1;i>=0;i--)
			{
				List<Race> races=raceSets.get(i);
				if(races.size()==1)
				{
					Race originalRace = races.get(0);
					Race race2=races.get(0);
					if((race2.ID().endsWith("ling"))
					&&(!race2.ID().equals("Whelpling"))
					&&race2.isGeneric())
					{
						final Race race1=CMClass.getRace(race2.ID().substring(0,race2.ID().length()-4));
						if(race1!=null)
						{
							race2=CMClass.getRace("Halfling");
							Race finalR=race1.mixRace(race2,originalRace.ID(),originalRace.name());
							if(finalR.isGeneric())
							{
								compareRaces(originalRace.ID(),originalRace,finalR,mob);
							}
							else
								mob.tell("Failed single race:"+race2.ID());
						}
						else
							mob.tell("Undoable single race:"+race2.ID());
					}
					raceSets.remove(i);
				}
			}
			for(int i=0;i<raceSets.size();i++)
			{
				List<Race> races=raceSets.get(i);
				Race lastRace=races.get(races.size()-1);
				for(int r=races.size()-2;r>=0;r--)
				{
					Race race2=lastRace;
					Race race1=races.get(r);
					Race finalR=race1.mixRace(race2,"TempID","TempName");
					if(finalR.isGeneric())
					{
						Race compareR=CMClass.getRace(race1.ID()+race2.ID());
						if(compareR==null)
							compareR=CMClass.getRace(race2.ID()+race1.ID());
						if(compareR==null)
							mob.tell("Can't find "+race1.ID()+"-"+race2.ID());
						else
							compareRaces(race1.ID()+race2.ID(),compareR,finalR,mob);
					}
					else
						mob.tell("Failed single race:"+race2.ID());
				}
			}
			//Race R1=CMLib.utensils().getMixedRace(firstR.ID(),secondR.ID());
		}
		else
		if(s.equalsIgnoreCase("itemstats")&&(CMSecurity.isASysOp(mob)))
		{
			s="room";
			if(commands.size()>1)
				s=commands.get(1);

			if(mob.session()==null)
				return false;
			if(mob.session().confirm(L("Begin scanning and altering all items to system defaults?"), L("N")))
			{
				mob.session().print(L("working..."));
				StringBuffer recordedChanges=null;
				for(int i=1;i<commands.size();i++)
				{
					if(commands.get(i).equalsIgnoreCase("NOSAVE"))
					{
						recordedChanges=new StringBuffer("");
						break;
					}
				}
	
				final Vector<Room> rooms=new Vector<Room>();
				if(s.toUpperCase().startsWith("ROOM"))
					rooms.add(mob.location());
				else
				if(s.toUpperCase().startsWith("AREA"))
				{
					try
					{
						for(final Enumeration<Room> e=mob.location().getArea().getCompleteMap();e.hasMoreElements();)
							rooms.add(e.nextElement());
					}
					catch(final NoSuchElementException nse)
					{
					}
				}
				else
				if(s.toUpperCase().startsWith("CATALOG"))
				{
					try
					{
						final Item[] items=CMLib.catalog().getCatalogItems();
						for (final Item I : items)
						{
							if(CMLib.itemBuilder().itemFix(I,-1,recordedChanges))
							{
								mob.tell(L("Catalog item @x1 done.",I.Name()));
								CMLib.catalog().updateCatalog(I);
							}
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				else
				if(s.toUpperCase().startsWith("WORLD"))
				{
					try
					{
						for(final Enumeration e=CMLib.map().areas();e.hasMoreElements();)
						{
							final Area A=(Area)e.nextElement();
							boolean skip=false;
							for(int i=1;i<commands.size();i++)
							{
								if(commands.get(i).equalsIgnoreCase(A.Name())||rest.equalsIgnoreCase(A.Name()))
								{
									skip=true;
									commands.remove(i);
									break;
								}
							}
							if(skip)
								continue;
							for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
								rooms.add(r.nextElement());
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				else
				{
					mob.tell(L("Try ROOM, AREA, CATALOG, or WORLD."));
					return false;
				}
				if(recordedChanges!=null)
					mob.session().println(".");
				for(final Enumeration r=rooms.elements();r.hasMoreElements();)
				{
					Room R=CMLib.map().getRoom((Room)r.nextElement());
					if((R==null)||(R.getArea()==null)||(R.roomID().length()==0))
						continue;
					final Area A=R.getArea();
					A.setAreaState(Area.State.FROZEN);
					if((recordedChanges!=null)&&(recordedChanges.length()>0))
					{
						mob.session().rawOut(recordedChanges.toString());
						recordedChanges.setLength(0);
					}
					synchronized(("SYNC"+R.roomID()).intern())
					{
	
						R=CMLib.map().getRoom(R);
						CMLib.map().resetRoom(R, true);
						boolean changedMOBS=false;
						boolean changedItems=false;
						for(int i=0;i<R.numItems();i++)
						{
							final Item I=R.getItem(i);
							if(CMLib.itemBuilder().itemFix(I,-1,recordedChanges))
								changedItems=true;
						}
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M==mob)||(!M.isMonster()))
								continue;
							if(!M.isSavable())
								continue;
							for(int i=0;i<M.numItems();i++)
							{
								final Item I=M.getItem(i);
								int lvl=-1;
								if((I.basePhyStats().level()>M.basePhyStats().level())
								||((I.basePhyStats().level()>91)&&((I.basePhyStats().level() + (I.basePhyStats().level()/10))<M.basePhyStats().level())))
									lvl=M.basePhyStats().level();
								if(CMLib.itemBuilder().itemFix(I,lvl,recordedChanges))
									changedMOBS=true;
							}
							final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
							if(SK!=null)
							{
								for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
								{
									final Environmental E=i.next();
									if(E instanceof Item)
									{
										final Item I=(Item)E;
										boolean didSomething=false;
										didSomething=CMLib.itemBuilder().itemFix(I,-1,recordedChanges);
										changedMOBS=changedMOBS||didSomething;
										if(didSomething)
										{
											final int numInStock=SK.getShop().numberInStock(I);
											final int stockPrice=SK.getShop().stockPrice(I);
											SK.getShop().delAllStoreInventory(I);
											SK.getShop().addStoreInventory(I,numInStock,stockPrice);
										}
									}
								}
							}
						}
						if((changedItems)&&(recordedChanges==null))
							CMLib.database().DBUpdateItems(R);
						if((changedMOBS)&&(recordedChanges==null))
							CMLib.database().DBUpdateMOBs(R);
						if(recordedChanges==null)
							mob.session().print(".");
					}
					if(A.getAreaState()!=Area.State.ACTIVE)
						A.setAreaState(Area.State.ACTIVE);
				}
				if((recordedChanges!=null)&&(recordedChanges.length()>0))
					mob.session().rawOut(recordedChanges.toString());
				mob.session().println(L("done!"));
			}
		}
		else
		if(s.startsWith("clantick"))
		{
			mob.session().println(L("clantick: tick clans"));
			CMLib.clans().tickAllClans();
			mob.session().println(L("clantick: clans tick"));
			CMLib.clans().forceTick();
			mob.session().println(L("clantick: done!"));
		}
		else
		if(s.equalsIgnoreCase("VISITATION"))
		{
			try
			{
				if(commands.size()>1)
					s=commands.get(1);
				boolean area=false;
				if(s.equalsIgnoreCase("AREA"))
				{
					area=true;
					if(commands.size()>2)
						s=commands.get(2);
				}
				final MOB M=CMLib.players().getLoadPlayer(s);
				if((M!=null)&&(M.playerStats()!=null))
				{
					if(area)
						M.playerStats().unVisit(M.location().getArea());
					else
						M.playerStats().unVisit(M.location());
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		else
		if(s.equalsIgnoreCase("arearacemat")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session().confirm(L("Begin scanning and altering all item materials and mob races manually?"), L("N")))
			{
				// this is just utility code and will change frequently
				final Area A=mob.location().getArea();
				CMLib.map().resetArea(A);
				A.setAreaState(Area.State.FROZEN);
				final Hashtable<String,Integer> rememberI=new Hashtable<String,Integer>();
				final Hashtable<String,Race> rememberM=new Hashtable<String,Race>();
				try
				{
					for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
					{
						Room R=r.nextElement();
						if(R.roomID().length()>0)
						synchronized(("SYNC"+R.roomID()).intern())
						{
							R=CMLib.map().getRoom(R);
							CMLib.map().resetRoom(R, true);
							boolean somethingDone=false;
							mob.tell(R.roomID()+"/"+R.name()+"/"+R.displayText()+"--------------------");
							for(int i=R.numItems()-1;i>=0;i--)
							{
								final Item I=R.getItem(i);
								if(I.ID().equalsIgnoreCase("GenWallpaper"))
									continue;
								final int returned=resetAreaOramaManaI(mob,I,rememberI," ");
								if(returned<0)
								{
									R.delItem(I);
									somethingDone=true;
									mob.tell(L(" deleted"));
								}
								else
								if(returned>0)
									somethingDone=true;
							}
							if(somethingDone)
								CMLib.database().DBUpdateItems(R);
							somethingDone=false;
							for(int m=0;m<R.numInhabitants();m++)
							{
								final MOB M=R.fetchInhabitant(m);
								if(M==mob)
									continue;
								if(!M.isSavable())
									continue;
								Race R2=rememberM.get(M.Name());
								if(R2!=null)
								{
									if(M.charStats().getMyRace()==R2)
										mob.tell(L(" @x1 still @x2",M.Name(),R2.name()));
									else
									{
										M.baseCharStats().setMyRace(R2);
										R2.setHeightWeight(M.basePhyStats(),(char)M.baseCharStats().getStat(CharStats.STAT_GENDER));
										M.recoverCharStats();
										M.recoverPhyStats();
										mob.tell(L(" @x1 Changed to @x2",M.Name(),R2.ID()));
										somethingDone=true;
									}
								}
								else
								while(true)
								{
									final String str=mob.session().prompt(" "+M.Name()+"/"+M.charStats().getMyRace().ID(),"");
									if(str.length()==0)
									{
										rememberM.put(M.name(),M.baseCharStats().getMyRace());
										break;
									}
									if(str.equals("?"))
										mob.tell(M.Name()+"/"+M.displayText()+"/"+M.description());
									else
									{
										R2=CMClass.getRace(str);
										if(R2==null)
										{
											String poss="";
											if(poss.length()==0)
											{
												for(final Enumeration e=CMClass.races();e.hasMoreElements();)
												{
													final Race R3=(Race)e.nextElement();
													if(R3.ID().toUpperCase().startsWith(str.toUpperCase()))
														poss=R3.name();
												}
											}
											if(poss.length()==0)
											{
												for(final Enumeration e=CMClass.races();e.hasMoreElements();)
												{
													final Race R3=(Race)e.nextElement();
													if(R3.ID().toUpperCase().indexOf(str.toUpperCase())>=0)
														poss=R3.name();
												}
											}
											if(poss.length()==0)
											{
												for(final Enumeration e=CMClass.races();e.hasMoreElements();)
												{
													final Race R3=(Race)e.nextElement();
													if(R3.name().toUpperCase().startsWith(str.toUpperCase()))
														poss=R3.name();
												}
											}
											if(poss.length()==0)
											{
												for(final Enumeration e=CMClass.races();e.hasMoreElements();)
												{
													final Race R3=(Race)e.nextElement();
													if(R3.name().toUpperCase().indexOf(str.toUpperCase())>=0)
														poss=R3.name();
												}
											}
											mob.tell(L(" '@x1' is not a valid race.  Try '@x2'.",str,poss));
											continue;
										}
										mob.tell(L(" Changed to @x1",R2.ID()));
										M.baseCharStats().setMyRace(R2);
										R2.setHeightWeight(M.basePhyStats(),(char)M.baseCharStats().getStat(CharStats.STAT_GENDER));
										M.recoverCharStats();
										M.recoverPhyStats();
										rememberM.put(M.name(),M.baseCharStats().getMyRace());
										somethingDone=true;
										break;
									}
								}
								for(int i=M.numItems()-1;i>=0;i--)
								{
									final Item I=M.getItem(i);
									final int returned=resetAreaOramaManaI(mob,I,rememberI,"   ");
									if(returned<0)
									{
										M.delItem(I);
										somethingDone=true;
										mob.tell(L("   deleted"));
									}
									else
									if(returned>0)
										somethingDone=true;
								}
								final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
								if(SK!=null)
								{
									for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
									{
										final Environmental E=i.next();
										if(E instanceof Item)
										{
											final Item I=(Item)E;
											final int returned=resetAreaOramaManaI(mob,I,rememberI," - ");
											if(returned<0)
											{
												SK.getShop().delAllStoreInventory(I);
												somethingDone=true;
												mob.tell(L("   deleted"));
											}
											else
											if(returned>0)
											{
												somethingDone=true;
												final int numInStock=SK.getShop().numberInStock(I);
												final int stockPrice=SK.getShop().stockPrice(I);
												SK.getShop().delAllStoreInventory(I);
												SK.getShop().addStoreInventory(I,numInStock,stockPrice);
											}
										}
									}
								}
								if(M.fetchAbility("Chopping")!=null)
								{
									somethingDone=true;
									M.delAbility(M.fetchAbility("Chopping"));
								}
								for(final Enumeration<Behavior> e=M.behaviors();e.hasMoreElements();)
								{
									final Behavior B=e.nextElement();
									if((B.ID().equalsIgnoreCase("Mobile"))
									&&(B.getParms().trim().length()>0))
									{
										somethingDone=true;
										B.setParms("");
									}
								}
							}
							if(somethingDone)
								CMLib.database().DBUpdateMOBs(R);
						}
					}
				}
				catch (final java.io.IOException e)
				{
				}
				if(A.getAreaState()!=Area.State.ACTIVE)
					A.setAreaState(Area.State.ACTIVE);
				mob.tell(L("Done."));
			}
		}
		else
		if(s.equalsIgnoreCase("manufacturers"))
		{
			TechType[] types;
			String[] names;
			if(mob.session().confirm(L("Re-create all the manufacturers?"), L("N")))
			{
				final List<Manufacturer> m=new XVector<Manufacturer>(CMLib.tech().manufacterers());
				for(final Manufacturer M : m)
					CMLib.tech().delManufacturer(M);
				types=new TechType[]{TechType.SHIP_SOFTWARE};
				names=new String[]{
				"Nanoapp",
				"ExaSoft",
				"Cryptosoft",
				"M.S. Corp",
				"AmLogic",
				"InnovaSoft",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_COMPUTER};
				names=new String[]{
				"IniProc",
				"picoSystems",
				"cybProc",
				"GuthrieTronics",
				"SolidCorp",
				"DelCorp",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_SPACESHIP};
				names=new String[]{
				"AtomiCorp",
				"FrontierCorp",
				"Vargas",
				"JarviSys",
				"Solar Corp",
				"DynamiCorp",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_ENGINE};
				names=new String[]{
				"Aczev Ltd",
				"ItsukCorp",
				"E.B.H. Ltd",
				"Globomotors",
				"Bowers Eng.",
				"DynamiDrive",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_SENSOR};
				names=new String[]{
				"BunovaCorp",
				"Censys",
				"Dering-Hao",
				"ProgressTek",
				"NetCorp",
				"McNeil Ltd",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_DAMPENER};
				names=new String[]{
				"S.H. Ltd",
				"Malik-Ni",
				"HsuiCorp",
				"RiddleCorp",
				"TotCorp",
				"Apex Ltd",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_ENVIRO_CONTROL};
				names=new String[]{
				"YehCorp",
				"A.O.P. Ltd",
				"Callahan",
				"Innovatak",
				"SharpeyTek",
				"Holtz-Derkova",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_POWER};
				names=new String[]{
				"Optcel",
				"Shinacells",
				"Choe Corp",
				"hyDat Ltd",
				"LumoDigital",
				"Li-Lai Ltd",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_WEAPON};
				names=new String[]{
				"A.S.U. Ltd",
				"Mi-Gievora",
				"A.T.F. Corp",
				"Paragon Systems",
				"Physionetics",
				"DestructoCorp",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_SHIELD};
				names=new String[]{
				"Lee-Lavanchy",
				"Hsaio GmbH",
				"Kikko-Thoran",
				"LarsonTek",
				"ElectriCorp",
				"Grant DefCorp",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.SHIP_SHIELD,TechType.SHIP_WEAPON};
				names=new String[]{
				"Hsaio",
				"Toy-Miyazu",
				"Minova",
				"DiversiCorp",
				"Unidynamics",
				"Nucleomatic",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.ANY};
				names=new String[]{
				"enTek",
				"triNet",
				"ReadyTek",
				"GenTech",
				"GeneralCorp",
				"A.C.M.E.",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.PERSONAL_WEAPON};
				names=new String[]{
				"Chu",
				"Asan",
				"Noda",
				"Ocano",
				"PhaseCorp",
				"Chao-Itsuk",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.PERSONAL_SHIELD};
				names=new String[]{
				"Xiu-Nova",
				"I.B.B. Ltd",
				"M.O.B. Corp",
				"BoltonTek",
				"Maddox",
				"Digital Defense Corp",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.PERSONAL_SENSOR};
				names=new String[]{
				"cenApp",
				"virGenCorp",
				"Umin-Kahi Ltd",
				"Tyrrell",
				"ArcrSys",
				"Tsaka Corp",
				};
				makeManufacturer(names, types);
				types=new TechType[]{TechType.PERSONAL_WEAPON,TechType.PERSONAL_SENSOR,TechType.PERSONAL_SHIELD};
				names=new String[]{
				"MilSec",
				"A.O.H. Corp",
				"GenScience",
				"PersoTek",
				"StrateTek",
				"Maynard",
				};
			}
		}
		else
		{
			String firstPart="'@x1' is an unknown reset.  Try ROOM, AREA,";
			if(CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDPLAYERS))
				firstPart+=" PASSWORD,";
			mob.tell(L(firstPart+" MOBSTATS ROOM, MOBSTATS AREA *, MOBSTATS WORLD *, MOBSTATS CATALOG *, ITEMSTATS ROOM, ITEMSTATS AREA *, ITEMSTATS WORLD *, ITEMSTATS CATALOG *, AREARACEMAT *, AREAROOMIDS *, AREAINSTALL.\n\r * = Reset functions which may take a long time to complete.",s));
		}
		return false;
	}

}
