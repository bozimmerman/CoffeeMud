package com.planet_ink.coffee_mud.commands;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

// requires nothing to load
public class Socials
{
	String filename="";
	public boolean loaded=false;
	public Hashtable soc=new Hashtable();
	public String socialsList=null;

	public Socials(String newFilename)
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
				if(x>0)
				{
					Social socobj=new Social();
					String s=getline.substring(0,x).toUpperCase();
					switch(s.charAt(0))
					{
					case 'W':
						socobj.sourceCode=Affect.SOUND_WORDS;
						break;
					case 'M':
						socobj.sourceCode=Affect.HANDS_GENERAL;
						break;
					case 'S':
						socobj.sourceCode=Affect.SOUND_NOISE;
						break;
					}
					switch(s.charAt(1))
					{
					case 'T':
						socobj.othersCode=Affect.VISUAL_WNOISE;
						socobj.targetCode=Affect.HANDS_GENERAL;
						break;
					case 'S':
						socobj.othersCode=Affect.SOUND_NOISE;
						socobj.targetCode=Affect.SOUND_NOISE;
						break;
					case 'W':
						socobj.othersCode=Affect.SOUND_WORDS;
						socobj.targetCode=Affect.SOUND_WORDS;
						break;
					case 'V':
						socobj.othersCode=Affect.VISUAL_WNOISE;
						socobj.targetCode=Affect.VISUAL_WNOISE;
						break;
					}
					getline=getline.substring(x+1);
					x=getline.indexOf("\t");
					if(x>=0)
					{
						socobj.Social_name=getline.substring(0,x).toUpperCase();
						getline=getline.substring(x+1);
						x=getline.indexOf("\t");
						if(x>=0)
						{
							socobj.You_see=getline.substring(0,x);
							getline=getline.substring(x+1);
							x=getline.indexOf("\t");
							if(x>=0)
							{
								socobj.Third_party_sees=getline.substring(0,x);
								getline=getline.substring(x+1);
								x=getline.indexOf("\t");
								if(x>=0)
								{
									socobj.Target_sees=getline.substring(0,x);
									getline=getline.substring(x+1);
									x=getline.indexOf("\t");
									if(x>=0)
										socobj.See_when_no_target=getline.substring(0,x);
									else
										socobj.See_when_no_target=getline;

									soc.put(socobj.Social_name,socobj);
								}
							}
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

	public Social FetchSocial(String name)
	{
		Social thisOne=(Social)soc.get(name.toUpperCase());
		return thisOne;
	}

	public Social FetchSocial(Vector C)
	{
		if(C==null) return null;
		if(C.size()==0) return null;

		String SocialName=(String)C.elementAt(0);
		if(C.size()>1)
		{
			String Target=((String)C.elementAt(1)).toUpperCase();
			if(!Target.equals("SELF"))
				Target="<T-NAME>";
			SocialName+=" "+Target;
		}
		return FetchSocial(SocialName);
	}

	public int num()
	{
		return soc.size();
	}

	public Social enum(int index)
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

	public void save()
	{
		if(loaded==false) return;
		try
		{
			FileWriter writer=new FileWriter(filename,false);
			StringBuffer buf=new StringBuffer("");
			for (Enumeration e = soc.elements() ; e.hasMoreElements() ; )
			{
				com.planet_ink.coffee_mud.commands.Social
					I=(com.planet_ink.coffee_mud.commands.Social)e.nextElement();

				switch(I.sourceCode)
				{
				case Affect.SOUND_WORDS:
					buf.append('w');
					break;
				case Affect.HANDS_GENERAL:
					buf.append('m');
					break;
				case Affect.SOUND_NOISE:
					buf.append('s');
					break;
				}
				switch(I.targetCode)
				{
				case Affect.HANDS_GENERAL:
					buf.append('t');
					break;
				case Affect.SOUND_NOISE:
					buf.append('s');
					break;
				case Affect.SOUND_WORDS:
					buf.append('w');
					break;
				case Affect.VISUAL_WNOISE:
					buf.append('v');
					break;
				}
				buf.append('\t');
				buf.append(I.Social_name+"\t");
				buf.append(I.You_see+"\t");
				buf.append(I.Third_party_sees+"\t");
				buf.append(I.Target_sees +"\t");
				buf.append(I.See_when_no_target +"\r\n");
			}
			writer.write(buf.toString());
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
			Log.errOut("Socials",e.getMessage());
			loaded= false;
		}
	}

	public String getSocialsList()
	{
		if(socialsList!=null)
			return socialsList;
		StringBuffer msg=new StringBuffer("");
		Hashtable uniqueList=new Hashtable();
		for (Enumeration e = soc.elements() ; e.hasMoreElements() ; )
		{
			Social I=(Social)e.nextElement();
			int space=I.Social_name.indexOf(" ");
			String name=null;
			if(space>0)
				name=I.Social_name.substring(0,space).trim().toUpperCase();
			else
				name=I.Social_name.trim().toUpperCase();
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
				msg.append("\n\r");
				col=1;
			}

			msg.append(Util.padRight((String)sortableList.elementAt(i),19));
		}
		socialsList=msg.toString();
		return socialsList;
	}
}