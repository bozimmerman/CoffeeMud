package com.planet_ink.coffee_mud.application;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.collections.Pair;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class LocalizationHelper
{
	private static Map<String, Set<String>>	found		= new TreeMap<String, Set<String>>();
	private static Set<String>				foundDeDup	= new TreeSet<String>();
	private static List<String>				foundAll	= new ArrayList<String>();
	private static Set<String>				unfound		= new TreeSet<String>();
	private static String					marker		= "L(";


	protected static boolean isCleanString(final String innards)
	{
		boolean cleanString=false;
		if(innards.startsWith("\"") && innards.endsWith("\""))
		{
			cleanString=true;
			for(int i=1;i<innards.length()-1;i++)
			{
				if((innards.charAt(i)=='\"')&&(innards.charAt(i-1)!='\\'))
					cleanString=false;
			}
		}
		return cleanString;
	}

	public static int getCol1(final String str)
	{
		double col = 0;
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\t')
				col = Math.floor(col)+1;
			else
			if(str.charAt(i)==' ')
				col += 0.25;
			else
				break;
		}
		return (int)Math.round(Math.floor(col));
	}

	public static void recordFound(final Stack<Pair<String,Integer>> classes, String str)
	{
		if(str.trim().length()==0)
			return;
		final String clazz = classes.peek().first;
		if(str.indexOf("\"")>=0)
		{
			int x = str.indexOf('"');
			while((x==0)||((x>0)&&(str.charAt(x-1)!='\\')))
			{
				str=str.substring(0,x)+"\\"+str.substring(x);
				x=str.indexOf('"',x+2);
			}
		}
		if(!found.containsKey(clazz))
			found.put(clazz, new TreeSet<String>());
		found.get(clazz).add(str);
		foundDeDup.add(str);
		foundAll.add(str);
	}

	public static void doDir(final File F) throws IOException
	{
		if(F.isDirectory())
		{
			if((F.getAbsolutePath().indexOf(".svn")<0)&&(F.getAbsolutePath().indexOf(".git")<0))
				for(final File f : F.listFiles())
					doDir(f);
		}
		else
		if(F.getName().endsWith(".java") && (!F.getName().equals("LocalizationHelper.java")))
		{
			final BufferedReader br=new BufferedReader(new FileReader(F));
			String s=br.readLine();
			final List<String> lst=new ArrayList<String>();
			while(s!=null)
			{
				lst.add(s);
				s=br.readLine();
			}
			br.close();
			final Stack<Pair<String,Integer>> classes = new Stack<Pair<String,Integer>>();
			for(int l=0;l<lst.size();l++)
			{
				String line = lst.get(l);
				final String tline = line.trim();
				if(tline.startsWith("package ")&&(tline.endsWith(";")))
					classes.add(new Pair<String,Integer>(tline.substring(8,tline.length()-1),Integer.valueOf(-10)));
				else
				if((tline.length()>0)&&(Character.isLetter(tline.charAt(0))))
				{
					if((line.indexOf("class ")>0)&&(line.indexOf("\"")<0)&&((line.indexOf("/*")<0)||(line.indexOf("/*")>line.indexOf("class "))))
					{
						final int x = line.indexOf("class ");
						if(!Character.isLetterOrDigit(line.charAt(x-1)))
						{
							final String classline=line.substring(x+6).trim();
							int y = classline.indexOf(' ');
							if(y<0) y=classline.length();
							final String className = classline.substring(0,y);
							final int col =  getCol1(line);
							classes.push(new Pair<String,Integer>(classes.peek().first+"."+className,Integer.valueOf(col)));
						}
					}
					else
					if((line.indexOf("enum ")>0)&&(line.indexOf("\"")<0)&&(line.indexOf("{")<0))
					{
						final int x = line.indexOf("enum ");
						if(!Character.isLetterOrDigit(line.charAt(x-1)))
						{
							final String classline=line.substring(x+5).trim();
							int y = classline.indexOf(' ');
							if(y<0) y=classline.length();
							final String className = classline.substring(0,y);
							final int col =  getCol1(line);
							classes.push(new Pair<String,Integer>(classes.peek().first+"."+className,Integer.valueOf(col)));
						}
					}
					else
					if((line.indexOf("interface ")>0)&&(line.indexOf("\"")<0))
					{
						final int x = line.indexOf("interface ");
						if(!Character.isLetterOrDigit(line.charAt(x-1)))
						{
							final String classline=line.substring(x+10).trim();
							int y = classline.indexOf(' ');
							if(y<0) y=classline.length();
							final String className = classline.substring(0,y);
							final int col =  getCol1(line);
							classes.push(new Pair<String,Integer>(classes.peek().first+"."+className,Integer.valueOf(col)));
						}
					}
				}
				else
				if((tline.equals("}")||tline.equals("};"))
				&&(getCol1(line)==classes.peek().second.intValue()))
					classes.pop();
				String myMarker = marker;
				int x = line.indexOf(marker);
				if(x>9&&marker.equals("L("))
				{
					if(line.substring(x-9).startsWith("commonTelL("))
					{
						x-=9;
						int z = line.indexOf("\"",x);
						if(z<0)
							break;
						z=line.lastIndexOf(",",z);
						myMarker = line.substring(x,z+1);
					}
				}
				while(x>=0)
				{
					if((x==0)||(!Character.isLetter(line.charAt(x-1))))
					{
						int y=x+myMarker.length();
						final Stack<Character> depthStack = new Stack<Character>();
						boolean inquote = false;
						for(;y<line.length();y++)
						{
							final char c = line.charAt(y);
							if((c=='(')&&(!inquote))
								depthStack.push(Character.valueOf(')'));
							else
							if((c=='{')&&(!inquote))
								depthStack.push(Character.valueOf('}'));
							else
							if((c=='[')&&(!inquote))
								depthStack.push(Character.valueOf(']'));
							else
							if(((depthStack.size()==0&&c==')')
								||(depthStack.size()>0 && c==depthStack.peek().charValue()))
							&&(!inquote))
							{
								if(depthStack.size()==0)
									break;
								depthStack.pop();
							}
							else
							if((line.charAt(y)=='"')&&(line.charAt(y-1)!='\\'))
								inquote=!inquote;
							else
							if((line.charAt(y)==',') && (!inquote) && (depthStack.size()==0))
								break;
							if((y==line.length()-1)
							&&(!inquote)
							&&(depthStack.size()==0)
							&&(l<lst.size())
							&&((line.trim().endsWith("\"")&&(lst.get(l+1).trim().startsWith("+\"")))
								||(line.trim().endsWith("\"")&&(lst.get(l+1).trim().startsWith("+ \"")))
								||(line.trim().endsWith("\"+")&&(lst.get(l+1).trim().startsWith("\"")))
								||(line.trim().endsWith("\" +")&&(lst.get(l+1).trim().startsWith("\"")))))
							{
								int z = y;
								if(line.trim().endsWith("\"")&&(lst.get(l+1).trim().startsWith("+\"")))
									z=line.lastIndexOf("\"");
								else
								if(line.trim().endsWith("\"")&&(lst.get(l+1).trim().startsWith("+ \"")))
									z=line.lastIndexOf("\"");
								else
								if(line.trim().endsWith("\"+")&&(lst.get(l+1).trim().startsWith("\"")))
									z=line.lastIndexOf("\"+");
								else
								if(line.trim().endsWith("\" +")&&(lst.get(l+1).trim().startsWith("\"")))
									z=line.lastIndexOf("\" +");
								y=z-1;
								line = line.substring(0,z);
								l++;
								String addLine = lst.get(l).trim();
								if(addLine.startsWith("+ \""))
									addLine=addLine.substring(3);
								else
								if(addLine.startsWith("+\""))
									addLine=addLine.substring(2);
								else
									addLine=addLine.substring(1);
								line += addLine;
								inquote=true;
							}
						}
						if((y>x+myMarker.length())&&(y<line.length()))
						{
							String innards = line.substring(x+myMarker.length(),y).trim();
							boolean cleanString=isCleanString(innards);
							if(!cleanString && innards.startsWith("auto?\""))
							{
								final int i = innards.indexOf("\":\"");
								if(i>0)
								{
									final String innard1 = innards.substring(5,i+1);
									final String innard2 = innards.substring(i+2);
									if(isCleanString(innard1)&&isCleanString(innard2))
									{
										if(innard1.length()>2)
										{
											final String cs = innard1.substring(1,innard1.length()-1);
											recordFound(classes, cs);
										}
										innards = innard2;
										cleanString = true;
									}
								}
							}
							if(!cleanString  && innards.trim().startsWith("new String[]"))
							{
								final int z1 = innards.indexOf("{");
								final int z2=innards.lastIndexOf("}");
								final String inn = innards.substring(z1+1,z2).trim();
								final List<String> allFound = new ArrayList<String>();
								final List<String> allStrings = new ArrayList<String>();
								boolean inquotes = false;
								int lastQuote=-1;
								for(int i=0;i<inn.length();i++)
								{
									if((inn.charAt(i)=='"')&&((i==0)||(inn.charAt(i-1)!='\\')))
									{
										if(inquotes)
											allStrings.add(inn.substring(lastQuote,i+1));
										lastQuote=i;
										inquotes=!inquotes;
									}
								}
								for(final String s1 : allStrings)
									if(isCleanString(s1.trim()))
										allFound.add(s1.trim());
								if(allFound.size()==allStrings.size())
								{
									for(final String s1 : allFound)
									{
										if(s1.trim().length()<3)
											break;
										else
										{
											final String cs = s1.substring(1,s1.length()-1);
											recordFound(classes, cs);
										}
									}
									innards = null;
								}
							}
							if(cleanString && (innards!=null))
							{
								final String cs = innards.substring(1,innards.length()-1);
								recordFound(classes, cs);
							}
							else
							if(innards != null)
							{
								if((myMarker.length()>2)||(innards.length()<3))
									unfound.add(line.trim());
								else
									unfound.add(innards);
							}
						}
						x=line.indexOf(marker,y-1);
					}
					else
						x=line.indexOf(marker,x+myMarker.length());
				}
			}
		}
	}

	enum cmds{ ALL,BROKE,USE,DEDUP}

	public static void main(final String[] args)
	{
		try
		{
			cmds cd = cmds.ALL;
			if((args.length<2)
			||("IT".indexOf(args[0].toUpperCase().trim())<0)
			||((cd=(cmds)CMath.s_valueOf(cmds.class, args[1].toUpperCase().trim()))==null))
			{
				System.out.println("Command usage: LocalizationHelper <I or T> <command>");
				System.out.println("I for parser input, T for translator.  Commands include:");
				System.out.println("USE   : output categorized replacement lines");
				System.out.println("DEDUP : output uncategorized replacement lines, de-dupped");
				System.out.println("BROKE : output only the plain unscrappable strings");
				System.out.println("ALL   : output plain de-dupped strings and unscrappable strings");
				System.out.println("\n\r* You can put USE/DEDUP lines in [session-translation]");
				System.out.println("    in your translation_??_??.properties file.");
				System.exit(-1);
				return;
			}
			marker="L(";
			if(args[0].toUpperCase().trim().startsWith("I"))
				marker="I(";
			doDir(new File(".\\com\\planet_ink\\coffee_mud\\".replace('\\', File.separatorChar)));
			if(cd == cmds.ALL)
				System.out.println("Good       : "+foundDeDup.size()+"/"+foundAll.size());
			if((cd == cmds.ALL)||(cd == cmds.BROKE))
				System.out.println("Unscrapable: "+unfound.size());
			switch(cd)
			{
				case ALL:
				{
					System.out.println("----------------------- Good ---------------------");
					for(final String s : foundDeDup)
						System.out.println(s);
					System.out.println("------------------- Unscrapable ------------------");
					for(final String s : unfound)
						System.out.println(s);
					System.out.println("--------------------------------------------------");
					System.out.println("Good : "+foundDeDup.size()+"/"+foundAll.size());
					System.out.println("Broke: "+unfound.size());
					break;
				}
				case BROKE:
				{
					System.out.println("------------------- Unscrapable ------------------");
					for(final String s : unfound)
						System.out.println(s);
					System.out.println("--------------------------------------------------");
					System.out.println("Unscrapable: "+unfound.size());
					break;
				}
				case DEDUP:
				{
					for(final String s : foundDeDup)
						System.out.println("# replaceexact \""+s+"\" with \""+s+"\"");
					break;
				}
				case USE:
				{
					if(marker.equals("I("))
					{
						for(final String s : foundDeDup)
							System.out.println("# replaceexact \""+s+"\" with \""+s+"\"");
					}
					else
					for(final String clazz : found.keySet())
					{
						final Set<String> set = found.get(clazz);
						System.out.println();
						System.out.println("[session-translation:"+clazz+"]");
						for(final String s : set)
							System.out.println("# replaceexact \""+s+"\" with \""+s+"\"");
					}
					break;
				}
			}
			System.exit(0);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
