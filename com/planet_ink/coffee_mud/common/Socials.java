package com.planet_ink.coffee_mud.common;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

// requires nothing to load
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
						socobj.setSourceCode(Affect.MSG_SPEAK);
						break;
					case 'M':
						socobj.setSourceCode(Affect.MSG_HANDS);
						break;
					case 'S':
						socobj.setSourceCode(Affect.MSG_NOISE);
						break;
					case 'O':
						socobj.setSourceCode(Affect.MSG_NOISYMOVEMENT);
						break;
					default:
						socobj.setSourceCode(Affect.MSG_HANDS);
						break;
					}
					if(s.length()>1)
					switch(s.charAt(1))
					{
					case 'T':
						socobj.setOthersCode(Affect.MSG_HANDS);
						socobj.setTargetCode(Affect.MSG_HANDS);
						break;
					case 'S':
						socobj.setOthersCode(Affect.MSG_NOISE);
						socobj.setTargetCode(Affect.MSG_NOISE);
						break;
					case 'W':
						socobj.setOthersCode(Affect.MSG_SPEAK);
						socobj.setTargetCode(Affect.MSG_SPEAK);
						break;
					case 'V':
						socobj.setOthersCode(Affect.MSG_NOISYMOVEMENT);
						socobj.setTargetCode(Affect.MSG_NOISYMOVEMENT);
						break;
					case 'O':
						socobj.setOthersCode(Affect.MSG_OK_VISUAL);
						socobj.setTargetCode(Affect.MSG_OK_VISUAL);
						break;
					default:
						socobj.setOthersCode(Affect.MSG_NOISYMOVEMENT);
						socobj.setTargetCode(Affect.MSG_NOISYMOVEMENT);
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
			for(Enumeration e=soc.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				if(key.startsWith(SocialName.toUpperCase()))
				{	SocialName=key; break;}
			}
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
				case Affect.MSG_SPEAK:
					buf.append('w');
					break;
				case Affect.MSG_HANDS:
					buf.append('m');
					break;
				case Affect.MSG_NOISE:
					buf.append('s');
					break;
				case Affect.MSG_NOISYMOVEMENT:
					buf.append('o');
					break;
				default:
					buf.append(' ');
					break;
				}
				switch(I.targetCode())
				{
				case Affect.MSG_HANDS:
					buf.append('t');
					break;
				case Affect.MSG_NOISE:
					buf.append('s');
					break;
				case Affect.MSG_SPEAK:
					buf.append('w');
					break;
				case Affect.MSG_NOISYMOVEMENT:
					buf.append('v');
					break;
				case Affect.MSG_OK_VISUAL:
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