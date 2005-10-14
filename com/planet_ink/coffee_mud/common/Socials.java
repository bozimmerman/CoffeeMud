package com.planet_ink.coffee_mud.common;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

// requires nothing to load
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Socials extends Scriptable
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

    public static void modifySocialOthersCode(MOB mob, Social me, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.session().rawPrintln(showNumber+". Others Effect type: "+((me.othersCode()==CMMsg.MSG_HANDS)?"HANDS":((me.othersCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((me.othersCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((me.othersCode()==CMMsg.MSG_NOISYMOVEMENT)?"SEEING MOVEMENT":"HEARING NOISE")))));
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().choose("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: ","WMSVH","");
        if((newName!=null)&&(newName.length()>0))
        {
            newName=newName.toUpperCase();
            switch(newName.charAt(0))
            {
                case 'H':
                    me.setOthersCode(CMMsg.MSG_HANDS);
                    me.setTargetCode(CMMsg.MSG_HANDS);
                break;
                case 'W':
                    me.setOthersCode(CMMsg.MSG_SPEAK);
                    me.setTargetCode(CMMsg.MSG_SPEAK);
                break;
                case 'M':
                    me.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
                    me.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
                break;
                case 'S':
                    me.setOthersCode(CMMsg.MSG_NOISE);
                    me.setTargetCode(CMMsg.MSG_NOISE);
                break;
                case 'V':
                    me.setOthersCode(CMMsg.MSG_OK_VISUAL);
                    me.setTargetCode(CMMsg.MSG_OK_VISUAL);
                break;
            }
        }
        else
            mob.session().println("(no change)");
    }
    
    public static void modifySocialTargetCode(MOB mob, Social me, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.session().rawPrintln(showNumber+". "+"Target Effect type: "+((me.targetCode()==CMMsg.MSG_HANDS)?"HANDS":((me.targetCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((me.targetCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((me.targetCode()==CMMsg.MSG_NOISYMOVEMENT)?"BEING MOVED ON":"HEARING NOISE")))));
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().choose("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: ","WMSVH","");
        if((newName!=null)&&(newName.length()>0))
        {
            newName=newName.toUpperCase();
            switch(newName.charAt(0))
            {
                case 'W':
                    me.setTargetCode(CMMsg.MSG_SPEAK);
                break;
                case 'M':
                    me.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
                break;
                case 'H':
                    me.setTargetCode(CMMsg.MSG_HANDS);
                break;
                case 'S':
                    me.setTargetCode(CMMsg.MSG_NOISE);
                break;
                case 'V':
                    me.setTargetCode(CMMsg.MSG_OK_VISUAL);
                break;
            }
        }
        else
            mob.session().println("(no change)");
    }
    
    public static void modifySocialSourceCode(MOB mob, Social me, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.session().rawPrintln(showNumber+". "+"Your action type: "+((me.sourceCode()==CMMsg.MSG_NOISYMOVEMENT)?"LARGE MOVEMENT":((me.sourceCode()==CMMsg.MSG_SPEAK)?"SPEAKING":((me.sourceCode()==CMMsg.MSG_HANDS)?"MOVEMENT":"MAKING NOISE"))));
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().choose("Change W)ords, M)ovement (small), S)ound, L)arge Movement: ","WMSL","");
        if((newName!=null)&&(newName.length()>0))
        {
            newName=newName.toUpperCase();
            switch(newName.charAt(0))
            {
                case 'W':
                    me.setSourceCode(CMMsg.MSG_SPEAK);
                break;
                case 'M':
                    me.setSourceCode(CMMsg.MSG_HANDS);
                break;
                case 'S':
                    me.setSourceCode(CMMsg.MSG_NOISE);
                break;
                case 'L':
                    me.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
                break;
            }
        }
        else
            mob.session().println("(no change)");
    }
    
    
	public static boolean modifySocialInterface(MOB mob, String socialString)
		throws IOException
	{
        Vector socials=Util.parse(socialString);
        if(socials.size()==0)
        {
            mob.tell("Which social?");
            return false;
        }
        String name=((String)socials.firstElement()).toUpperCase().trim();
        String rest=socials.size()>1?Util.combine(socials,1):"";
        socials=Socials.getAllSocialObjects((String)socials.firstElement());
        if((socials.size()==0)
        &&((mob.session()==null)
            ||(!mob.session().confirm("The social '"+name+"' does not exist.  Create it (y/N)? ","N"))))
            return false;
        boolean resaveSocials=true;
        while((resaveSocials)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            resaveSocials=false;
            Social soc=null;
            boolean pickNewSocial=true;
            while((pickNewSocial)&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                pickNewSocial=false;
                StringBuffer str=new StringBuffer("\n\rSelect a target:\n\r");
                int selection=-1;
                for(int v=0;v<socials.size();v++)
                {
                    Social S=(Social)socials.elementAt(v);
                    int x=S.Name().indexOf(" ");
                    if(x<0)
                    { 
                        str.append((v+1)+") No Target (NONE)\n\r"); 
                        continue;
                    }
                    if((rest.length()>0)
                    &&(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase(rest.toUpperCase().trim())))
                        selection=(v+1);
                    if(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase("<T-NAME>"))
                    { 
                        str.append((v+1)+") Targeted (TARGET)\n\r"); 
                        continue;
                    }
                    str.append((v+1)+") "+S.Name().substring(x+1).toUpperCase().trim()+"\n\r");
                }
                str.append((socials.size()+1)+") Add a new target\n\r");
                String s=null;
                if((rest.length()>0)&&(selection<0))
                    selection=(socials.size()+1);
                else
                if(selection<0)
                {
                    mob.session().rawPrintln(str.toString());
                    s=mob.session().prompt("\n\rSelect an option or RETURN: ","");
                    if(!Util.isInteger(s))
                    {
                        soc=null;
                        break;
                    }
                    selection=Util.s_int(s);
                }
                if((selection>0)&&(selection<=socials.size()))
                {
                    soc=(Social)socials.elementAt(selection-1);
                    break;
                }
                String newOne=rest;
                if(newOne.length()==0)
                    newOne=mob.session().prompt("\n\rNew target (TARGET,NONE,ALL,SELF): ","").toUpperCase().trim();
                if(newOne.startsWith("<")||newOne.startsWith(">")||(newOne.startsWith("T-")))
                    newOne="TNAME";
                if(newOne.equals("TNAME")) newOne=" <T-NAME>";
                else
                if(newOne.equals("NONE")) newOne="";
                else
                if(!newOne.equals("ALL")&&!newOne.equals("SELF")
                &&!mob.session().confirm("'"+newOne+"' is a non-standard target.  Are you sure (y/N)? ","N"))
                    pickNewSocial=true;
                else
                    newOne=" "+newOne;
                if(!pickNewSocial)
                for(int i=0;i<socials.size();i++)
                    if(((Social)socials.elementAt(i)).Name().equals(name+newOne))
                    {
                        mob.tell("This social already exists.  Pick it off the list above.");
                        pickNewSocial=true;
                        break;
                    }
                if(!pickNewSocial)
                {
                    if((newOne.length()>0)&&(!newOne.startsWith(" ")))
                        newOne=" "+newOne;
                    soc=new Social();
                    soc.setName(name+newOne);
                    if(newOne.trim().length()==0)
                    {
                        soc.setYou_see("You "+name.toLowerCase()+".");
                        soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s.");
                        soc.setSourceCode(CMMsg.MSG_HANDS);
                        soc.setOthersCode(CMMsg.MSG_HANDS);
                    }
                    else
                    if(newOne.trim().equals("ALL"))
                    {
                        soc.setYou_see("You "+name.toLowerCase()+" everyone.");
                        soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s everyone.");
                        soc.setSee_when_no_target(Util.capitalizeAndLower(name)+" who?");
                        soc.setSourceCode(CMMsg.MSG_SPEAK);
                        soc.setOthersCode(CMMsg.MSG_SPEAK);
                    }
                    else
                    if(newOne.trim().equals("<T-NAME>"))
                    {
                        soc.setYou_see("You "+name.toLowerCase()+" <T-NAME>.");
                        soc.setTarget_sees("<S-NAME> "+name.toLowerCase()+"s you.");
                        soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s <T-NAMESELF>.");
                        soc.setSee_when_no_target(Util.capitalizeAndLower(name)+" who?");
                        soc.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
                        soc.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
                        soc.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
                    }
                    else
                    if(newOne.trim().equals("SELF"))
                    {
                        soc.setYou_see("You "+name.toLowerCase()+" yourself.");
                        soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s <S-HIM-HERSELF>.");
                        soc.setSourceCode(CMMsg.MSG_NOISE);
                        soc.setOthersCode(CMMsg.MSG_NOISE);
                    }
                    else
                    {
                        soc.setYou_see("You "+name.toLowerCase()+newOne.toLowerCase()+".");
                        soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s"+newOne.toLowerCase()+".");
                        soc.setSourceCode(CMMsg.MSG_HANDS);
                        soc.setOthersCode(CMMsg.MSG_HANDS);
                    }
                    Socials.addSocial(soc);
                    socials.add(soc);
                    resaveSocials=true;
                }
            }
            if(soc!=null)
            {
                boolean ok=false;
                int showFlag=-1;
                if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
                    showFlag=-999;
                while(!ok)
                {
                    int showNumber=0;
                    soc.setYou_see(EnglishParser.promptText(mob,soc.You_see(),++showNumber,showFlag,"You-see string",false,true));
                    if(soc.sourceCode()==CMMsg.MSG_OK_ACTION) soc.setSourceCode(CMMsg.MSG_HANDS);
                    modifySocialSourceCode(mob,soc,++showNumber,showFlag);
                    soc.setThird_party_sees(EnglishParser.promptText(mob,soc.Third_party_sees(),++showNumber,showFlag,"Others-see string",false,true));
                    if(soc.othersCode()==CMMsg.MSG_OK_ACTION) soc.setOthersCode(CMMsg.MSG_HANDS);
                    modifySocialOthersCode(mob,soc,++showNumber,showFlag);
                    if(soc.Name().endsWith(" <T-NAME>"))
                    {
                        soc.setTarget_sees(EnglishParser.promptText(mob,soc.Target_sees(),++showNumber,showFlag,"Target-sees string",false,true));
                        if(soc.targetCode()==CMMsg.MSG_OK_ACTION) soc.setTargetCode(CMMsg.MSG_HANDS);
                        modifySocialTargetCode(mob,soc,++showNumber,showFlag);
                    }
                    if(soc.Name().endsWith(" <T-NAME>")||(soc.Name().endsWith(" ALL")))
                        soc.setSee_when_no_target(EnglishParser.promptText(mob,soc.See_when_no_target(),++showNumber,showFlag,"You-see when no target",false,true));
                    resaveSocials=true;
                    if(showFlag<-900){ ok=true; break;}
                    if(showFlag>0){ showFlag=-1; continue;}
                    showFlag=Util.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
                    if(showFlag<=0)
                    {
                        showFlag=-1;
                        ok=true;
                    }
                }
            }
            if((resaveSocials)&&(soc!=null))
            {
                Socials.save();
                Log.sysOut("Socials",mob.Name()+" modified social "+soc.name()+".");
                soc=null;
                if(rest.length()>0)
                    break;
            }
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
        Social S=null;
		if(C.size()>1)
		{
			String Target=((String)C.elementAt(1)).toUpperCase();
            S=FetchSocial(SocialName+" "+Target,true);
            if((S==null)
			&&((!Target.equals("SELF"))&&(!Target.equals("ALL"))))
				Target="<T-NAME>";
			theRest=" "+Target;
		}
		if(S==null) S=FetchSocial(SocialName+theRest,true);
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

	public static Social enumSocial(int index)
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
            Vector sorted=new Vector();
            while(V.size()>0)
            {
                Social lowest=(Social)V.firstElement();
                Social S=null;
                for(int i=1;i<V.size();i++)
                {
                    S=(Social)V.elementAt(i);
                    if(S.name().compareToIgnoreCase(lowest.Name())<=0)
                        lowest=S;
                }
                V.remove(lowest);
                sorted.add(lowest);
            }
            V=sorted;
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
				buf.setCharAt(buf.length()-1,'\r');
				buf.append('\n');
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

    public static Vector getAllSocialObjects(String named)
    {
        Vector all=new Vector();
        for (Enumeration e = soc.elements() ; e.hasMoreElements() ; )
        {
            Social I=(Social)e.nextElement();
            int space=I.name().indexOf(" ");
            String name=null;
            if(space>0)
                name=I.name().substring(0,space).trim().toUpperCase();
            else
                name=I.name().trim().toUpperCase();
            if(name.equalsIgnoreCase(named))
                all.addElement(I);
        }
        return all;
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
		Collections.sort(sortableList);
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
