package com.planet_ink.coffee_mud.common;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

// requires nothing to load
/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Socials
{
	private Socials() {};

	private static String filename="";
	private static boolean loaded=false;
	private static Hashtable soc=new Hashtable();

	public static boolean isLoaded() { return loaded; }
	public static void put(String name, Social S) { soc.put(name, S); }
	public static void remove(String name) { soc.remove(name); }
	public static void clearAllSocials()
	{
		loaded=false;
		filename="";
		soc=new Hashtable();
		Resources.removeResource("SOCIALS LIST");
		Resources.removeResource("WEB SOCIALS TBL");
	}

	public static void addSocial(Social S)
	{
		soc.put(S.name(),S);
	}

	public static void load(String newFilename)
	{
		filename=newFilename;
		try
		{
			FileInputStream fin=new FileInputStream(filename);
			BufferedReader reader=new BufferedReader(new InputStreamReader(fin));
			String getline=reader.readLine();
			while(getline!=null)
			{
				int x=getline.indexOf("\t");
				if(x>=0)
				{
					Social socobj=new Social();
					String s=getline.substring(0,x).toUpperCase();
					if(s.length()>0)
					switch(s.charAt(0))
					{
					case 'W':
						socobj.setSourceCode(CMMsg.MSG_SPEAK);
						break;
					case 'M':
						socobj.setSourceCode(CMMsg.MSG_HANDS);
						break;
					case 'S':
						socobj.setSourceCode(CMMsg.MSG_NOISE);
						break;
					case 'O':
						socobj.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
						break;
					default:
						socobj.setSourceCode(CMMsg.MSG_HANDS);
						break;
					}
					if(s.length()>1)
					switch(s.charAt(1))
					{
					case 'T':
						socobj.setOthersCode(CMMsg.MSG_HANDS);
						socobj.setTargetCode(CMMsg.MSG_HANDS);
						break;
					case 'S':
						socobj.setOthersCode(CMMsg.MSG_NOISE);
						socobj.setTargetCode(CMMsg.MSG_NOISE);
						break;
					case 'W':
						socobj.setOthersCode(CMMsg.MSG_SPEAK);
						socobj.setTargetCode(CMMsg.MSG_SPEAK);
						break;
					case 'V':
						socobj.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
						socobj.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
						break;
					case 'O':
						socobj.setOthersCode(CMMsg.MSG_OK_VISUAL);
						socobj.setTargetCode(CMMsg.MSG_OK_VISUAL);
						break;
					default:
						socobj.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
						socobj.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
						break;
					}
					getline=getline.substring(x+1);
					x=getline.indexOf("\t");
					if(x>=0)
					{
						socobj.setName(getline.substring(0,x).toUpperCase());
						getline=getline.substring(x+1);
						x=getline.indexOf("\t");
						if(x>=0)
						{
							socobj.setYou_see(getline.substring(0,x));
							getline=getline.substring(x+1);
							x=getline.indexOf("\t");
							if(x>=0)
							{
								socobj.setThird_party_sees(getline.substring(0,x));
								getline=getline.substring(x+1);
								x=getline.indexOf("\t");
								if(x>=0)
								{
									socobj.setTarget_sees(getline.substring(0,x));
									getline=getline.substring(x+1);
									x=getline.indexOf("\t");
									if(x>=0)
										socobj.setSee_when_no_target(getline.substring(0,x));
									else
										socobj.setSee_when_no_target(getline);

								}
							}
							soc.put(socobj.name(),socobj);
						}
					}
				}
				getline=reader.readLine();
			}
			loaded= true;
		}
		catch(IOException e)
		{
			Log.errOut("Socials",e.getMessage());
			loaded= false;
		}
	}

	public static boolean modifySocialInterface(MOB mob, Social soc)
		throws IOException
	{
		String name=soc.name();
		int x=name.toUpperCase().indexOf("<T-NAME>");
		boolean targeted=false;
		boolean self=false;
		if(x>=0)
		{
			targeted=true;
			name=name.substring(0,x).trim().toUpperCase();
		}
		else
		if(name.toUpperCase().endsWith("SELF"))
		{
			self=true;
			name=name.substring(0,name.length()-4).trim().toUpperCase();
		}


		mob.session().rawPrintln("\n\rSocial name '"+name+"' Enter new.");
		String newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			name=newName;
		else
			mob.session().println("(no change)");

		mob.session().rawPrintln("\n\rTarget="+(targeted?"TARGET":(self?"SELF":"NONE")));
		newName=mob.session().choose("Change T)arget, S)elf, N)one: ","TSN","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'T':
				targeted=true;
				self=false;
				break;
				case 'S':
				targeted=false;
				self=true;
				break;
				case 'N':
				targeted=false;
				self=false;
				break;
			}
		}
		else
			mob.session().println("(no change)");

		if(targeted)
			soc.setName(name+" <T-NAME>");
		else
		if(self)
			soc.setName(name+" SELF");
		else
			soc.setName(name);

		mob.session().rawPrintln("\n\rYou see '"+soc.You_see()+"'.  Enter new.");
		newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			soc.setYou_see(newName);
		else
			mob.session().println("(no change)");


		if(soc.sourceCode()==CMMsg.MSG_OK_ACTION)
			soc.setSourceCode(CMMsg.MSG_HANDS);
		mob.session().rawPrintln("\n\rYour action type="+((soc.sourceCode()==CMMsg.MSG_NOISYMOVEMENT)?"LARGE MOVEMENT":((soc.sourceCode()==CMMsg.MSG_SPEAK)?"SPEAKING":((soc.sourceCode()==CMMsg.MSG_HANDS)?"MOVEMENT":"MAKING NOISE"))));
		newName=mob.session().choose("Change W)ords, M)ovement (small), S)ound, L)arge Movement ","WMSL","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.setSourceCode(CMMsg.MSG_SPEAK);
				break;
				case 'M':
				soc.setSourceCode(CMMsg.MSG_HANDS);
				break;
				case 'S':
				soc.setSourceCode(CMMsg.MSG_NOISE);
				break;
				case 'L':
				soc.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
				break;
			}
		}
		else
			mob.session().println("(no change)");

		mob.session().rawPrintln("\n\rOthers see '"+soc.Third_party_sees()+"'.  Enter new.");
		newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			soc.setThird_party_sees(newName);
		else
			mob.session().println("(no change)");

		if(soc.othersCode()==CMMsg.MSG_OK_ACTION)
			soc.setOthersCode(CMMsg.MSG_HANDS);
		mob.session().rawPrintln("\n\rOthers Effect type="+((soc.othersCode()==CMMsg.MSG_HANDS)?"HANDS":((soc.sourceCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((soc.othersCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((soc.othersCode()==CMMsg.MSG_NOISYMOVEMENT)?"SEEING MOVEMENT":"HEARING NOISE")))));
		newName=mob.session().choose("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: ","WMSVH","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'H':
				soc.setOthersCode(CMMsg.MSG_HANDS);
				soc.setTargetCode(CMMsg.MSG_HANDS);
				break;
				case 'W':
				soc.setOthersCode(CMMsg.MSG_SPEAK);
				soc.setTargetCode(CMMsg.MSG_SPEAK);
				break;
				case 'M':
				soc.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
				soc.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
				break;
				case 'S':
				soc.setOthersCode(CMMsg.MSG_NOISE);
				soc.setTargetCode(CMMsg.MSG_NOISE);
				break;
				case 'V':
				soc.setOthersCode(CMMsg.MSG_OK_VISUAL);
				soc.setTargetCode(CMMsg.MSG_OK_VISUAL);
				break;
			}
		}
		else
			mob.session().println("(no change)");



		if(soc.name().indexOf("<T-NAME>")>=0)
		{
			mob.session().rawPrintln("\n\rTarget sees '"+soc.Target_sees()+"'.  Enter new.");
			newName=mob.session().prompt(": ","");
			if((newName!=null)&&(newName.length()>0))
				soc.setTarget_sees(newName);
			else
				mob.session().println("(no change)");


		if(soc.targetCode()==CMMsg.MSG_OK_ACTION)
			soc.setTargetCode(CMMsg.MSG_HANDS);
		mob.session().rawPrintln("\n\rTarget Effect type="+((soc.othersCode()==CMMsg.MSG_HANDS)?"HANDS":((soc.sourceCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((soc.othersCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((soc.othersCode()==CMMsg.MSG_NOISYMOVEMENT)?"SEEING MOVEMENT":"HEARING NOISE")))));
		newName=mob.session().choose("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: ","WMSVH","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.setTargetCode(CMMsg.MSG_SPEAK);
				break;
				case 'M':
				soc.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
				break;
				case 'H':
				soc.setTargetCode(CMMsg.MSG_HANDS);
				break;
				case 'S':
				soc.setTargetCode(CMMsg.MSG_NOISE);
				break;
				case 'V':
				soc.setTargetCode(CMMsg.MSG_OK_VISUAL);
				break;
			}
		}
		else
			mob.session().println("(no change)");



			mob.session().rawPrintln("\n\rYou see when no target '"+soc.See_when_no_target()+"'.  Enter new.");
			newName=mob.session().prompt(": ","");
			if((newName!=null)&&(newName.length()>0))
				soc.setSee_when_no_target(newName);
			else
				mob.session().println("(no change)");
		}
		return true;
	}

	public static Social FetchSocial(String name, boolean exactOnly)
	{
		Social thisOne=(Social)soc.get(name.toUpperCase());
		if((exactOnly)||(thisOne!=null)) return thisOne;
		name=name.toUpperCase();
		for(Enumeration e=soc.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.toUpperCase().startsWith(name))
				return (Social)soc.get(key);
		}
		return null;
	}

	public static Social FetchSocial(Vector C, boolean exactOnly)
	{
		if(C==null) return null;
		if(C.size()==0) return null;

		String SocialName=(String)C.elementAt(0);
		String theRest="";
		if(C.size()>1)
		{
			String Target=((String)C.elementAt(1)).toUpperCase();
			if(!Target.equals("SELF"))
				Target="<T-NAME>";
			theRest=" "+Target;
		}
		Social S=FetchSocial(SocialName+theRest,true);
		if((S==null)&&(!exactOnly))
		{
			String backupSocialName=null;
			for(Enumeration e=soc.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				if((key.startsWith(SocialName.toUpperCase()))&&(key.indexOf(" ")<0))
				{	
					SocialName=key; 
					break;
				}
				else
				if(key.startsWith(SocialName.toUpperCase()))
				{	
					backupSocialName=key; 
					break;
				}
			}
			if(SocialName==null) SocialName=backupSocialName;
			if(SocialName==null) 
				S=null;
			else
				S=FetchSocial(SocialName+theRest,true);
		}
		return S;
	}

	public static int num()
	{
		return soc.size();
	}

	public static Social enum(int index)
	{
		if((index<0)||(index>num())) return null;
		int i=0;
		for (Enumeration e = soc.elements() ; e.hasMoreElements() ; i++)
		{
			Social I=(Social)e.nextElement();
			if(i==index) return I;
		}
		return null;
	}

	public static void save()
	{
		if(loaded==false) return;
		try
		{
			FileWriter writer=new FileWriter(filename,false);
			StringBuffer buf=new StringBuffer("");
			Vector V=new Vector();
			for (Enumeration e = soc.elements() ; e.hasMoreElements() ; )
			{
				Social S1=(Social)e.nextElement();
				for(int i=0;i<V.size();i++)
				{
					Social S2=(Social)V.elementAt(i);
					if(S1.equals(S2))
					{
						V.insertElementAt(S1,i);
						break;
					}
				}
				if(!V.contains(S1))
					V.addElement(S1);
			}
			for(int v=0;v<V.size();v++)
			{
				Social I=(Social)V.elementAt(v);

				switch(I.sourceCode())
				{
				case CMMsg.MSG_SPEAK:
					buf.append('w');
					break;
				case CMMsg.MSG_HANDS:
					buf.append('m');
					break;
				case CMMsg.MSG_NOISE:
					buf.append('s');
					break;
				case CMMsg.MSG_NOISYMOVEMENT:
					buf.append('o');
					break;
				default:
					buf.append(' ');
					break;
				}
				switch(I.targetCode())
				{
				case CMMsg.MSG_HANDS:
					buf.append('t');
					break;
				case CMMsg.MSG_NOISE:
					buf.append('s');
					break;
				case CMMsg.MSG_SPEAK:
					buf.append('w');
					break;
				case CMMsg.MSG_NOISYMOVEMENT:
					buf.append('v');
					break;
				case CMMsg.MSG_OK_VISUAL:
					buf.append('o');
					break;
				default:
					buf.append(' ');
					break;
				}
				String[] stuff=new String[5];
				stuff[0]=I.name();
				stuff[1]=I.You_see();
				stuff[2]=I.Third_party_sees();
				stuff[3]=I.Target_sees();
				stuff[4]=I.See_when_no_target();
				buf.append('\t');
				for(int i=0;i<stuff.length;i++)
				{
					if(stuff[i]==null)
						buf.append("\t");
					else
						buf.append(stuff[i]+"\t");
				}
				buf.setCharAt(buf.length()-1,'\n');
				buf.append('\r');
			}
			writer.write(buf.toString());
			writer.flush();
			writer.close();
			Resources.removeResource("SOCIALS LIST");
			Resources.removeResource("WEB SOCIALS TBL");
		}
		catch(IOException e)
		{
			Log.errOut("Socials",e.getMessage());
			loaded= false;
		}
	}

	public static String getSocialsList()
	{
		StringBuffer socialsList=(StringBuffer)Resources.getResource("SOCIALS LIST");
		if(socialsList!=null) return socialsList.toString();
		socialsList=new StringBuffer("");
		Hashtable uniqueList=new Hashtable();
		for (Enumeration e = soc.elements() ; e.hasMoreElements() ; )
		{
			Social I=(Social)e.nextElement();
			int space=I.name().indexOf(" ");
			String name=null;
			if(space>0)
				name=I.name().substring(0,space).trim().toUpperCase();
			else
				name=I.name().trim().toUpperCase();
			if(uniqueList.get(name)==null)
				uniqueList.put(name,name);
		}
		Vector sortableList=new Vector();
		for(Enumeration e=uniqueList.elements(); e.hasMoreElements();)
			sortableList.addElement(e.nextElement());
		Collections.sort((List)sortableList);
		int col=0;
		for(int i=0;i<sortableList.size();i++)
		{
			if((++col)>4)
			{
				socialsList.append("\n\r");
				col=1;
			}
			socialsList.append(Util.padRight((String)sortableList.elementAt(i),19));
		}
		Resources.submitResource("SOCIALS LIST",socialsList);
		return socialsList.toString();
	}
}
