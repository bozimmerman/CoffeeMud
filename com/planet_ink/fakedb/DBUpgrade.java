package com.planet_ink.fakedb;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class DBUpgrade
{
	static PrintStream out=System.out;
	static boolean debug=false;
	static BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
	
	private static void pl(String str)
	{
		if(out!=null) out.println(str);
	}
	private static void p(String str)
	{
		if(out!=null) out.print(str);
	}
	public static void main(String a[]) throws IOException
	{
		pl("Welcome to the CoffeeMud Database Upgrade Tool!");
		pl("(C) 2003-2004 Bo Zimmerman");
		pl("Another product of ...Planet Ink!");
		pl("");
		pl("");
		Hashtable oldTables=new Hashtable();
		while(oldTables.size()==0)
		{
			pl("Enter the path to the 'fakedb.schema' file");
			pl("for the **OLD** version of CoffeeMud.");
			pl("It doesn't matter whether you are or are not using FakeDB.");
			p(":");
			String oldfakedbfile=in.readLine().trim();
			if(oldfakedbfile.length()==0)
			{
				pl("Well, you entered nothing. I assume you want to quit and do this later. Bye!");
				return;
			}
			if(!oldfakedbfile.endsWith("fakedb.schema"))
				oldfakedbfile+=File.separatorChar+"fakedb.schema";
			File F=new File(oldfakedbfile);
			if((!F.exists())||(!F.isFile()))
			{
				pl("!Hmm.. can't find that file.  Enter it again!");
				pl("");
				continue;
			}
			try
			{
				FileReader FR=new FileReader(F);
				BufferedReader reader=new BufferedReader(FR);
				String line="";
				Vector table=null;
				while((line!=null)&&(reader.ready()))
				{
					line=reader.readLine().trim();
					if(line!=null)
					{
						if(line.length()==0)
							table=null;
						else
						{
							if(table==null)
							{
								table=new Vector();
								oldTables.put(line,table);
							}
							else
							{
								int x=line.indexOf(" ");
								if(x<0)	throw new Exception("BAH!");
								String name=line.substring(0,x).trim();
								line=line.substring(x+1).trim();
								x=line.indexOf(" ");
								if(x>0)	line=line.substring(0,x).trim();
								if(line.toUpperCase().startsWith("INT")
								||line.toUpperCase().startsWith("NUMB")
								||line.toUpperCase().startsWith("DOUB")
								||line.toUpperCase().startsWith("FLOAT"))
									table.addElement('#'+name);
								else
									table.addElement('$'+name);
							}
						}
					}
				}
				FR.close();
			}
			catch(Exception e)
			{
				pl("!!Hmm.. can't read that file.  Enter it again!!");
				pl("");
				oldTables.clear();
				continue;
			}
			if(oldTables.size()==0)
			{
				pl("!!!Hmm.. can't read that file.  Enter it again!!!");
				pl("");
				oldTables.clear();
				continue;
			}
		}
		pl("");
		pl("Cool.. I got the old schema now.");
		pl("");
		Hashtable newTables=new Hashtable();
		while(newTables.size()==0)
		{
			pl("Enter the path to the 'fakedb.schema' file");
			pl("for the **NEW** version of CoffeeMud.");
			pl("It doesn't matter whether you are or are not using FakeDB.");
			p(":");
			String newfakedbfile=in.readLine().trim();
			if(newfakedbfile.length()==0)
			{
				pl("Well, you entered nothing. I assume you want to quit and do this later. Bye!");
				return;
			}
			if(!newfakedbfile.endsWith("fakedb.schema"))
				newfakedbfile+=File.separatorChar+"fakedb.schema";
			File F=new File(newfakedbfile);
			if((!F.exists())||(!F.isFile()))
			{
				pl("!Hmm.. can't find that file.  Enter it again!");
				pl("");
				continue;
			}
			try
			{
				FileReader FR=new FileReader(F);
				BufferedReader reader=new BufferedReader(FR);
				String line="";
				Vector table=null;
				while((line!=null)&&(reader.ready()))
				{
					line=reader.readLine().trim();
					if(line!=null)
					{
						System.out.println("/"+line+"/");
						if(line.trim().length()==0)
							table=null;
						else
						{
							if(table==null)
							{
								table=new Vector();
								newTables.put(line,table);
							}
							else
							{
								int x=line.indexOf(" ");
								if(x<0)	throw new Exception("BAH!");
								String name=line.substring(0,x).trim();
								line=line.substring(x+1).trim();
								x=line.indexOf(" ");
								if(x>0)	line=line.substring(0,x).trim();
								if(line.toUpperCase().startsWith("INT")
								||line.toUpperCase().startsWith("NUMB")
								||line.toUpperCase().startsWith("DOUB")
								||line.toUpperCase().startsWith("FLOAT"))
									table.addElement('#'+name);
								else
									table.addElement('$'+name);
							}
						}
					}
				}
				FR.close();
			}
			catch(Exception e)
			{
				pl("!!Hmm.. can't read that file.  Enter it again!!");
				pl("");
				newTables.clear();
				continue;
			}
			if(newTables.size()==0)
			{
				pl("!!!Hmm.. can't read that file.  Enter it again!!!");
				pl("");
				newTables.clear();
				continue;
			}
		}
		boolean same=true;
		for(Enumeration e=oldTables.keys();e.hasMoreElements();)
		{
			String s=(String)e.nextElement();
			Vector V1=(Vector)oldTables.get(s);
			Vector V2=(Vector)newTables.get(s);
			if((V1==null)||(V2==null))
			{ same=false; break;}
			if((V1.size()!=V2.size()))
			{ same=false; break;}
			for(int v=0;v<V1.size();v++)
			{
				if(!((String)V1.elementAt(v)).equals((String)V2.elementAt(v)))
				{ same=false; break;}
			}
			if(!same) break;
		}
		for(Enumeration e=newTables.keys();e.hasMoreElements();)
		{
			String s=(String)e.nextElement();
			Vector V1=(Vector)oldTables.get(s);
			Vector V2=(Vector)newTables.get(s);
			if((V1==null)||(V2==null))
			{ same=false; break;}
			if((V1.size()!=V2.size()))
			{ same=false; break;}
			for(int v=0;v<V1.size();v++)
			{
				if(!((String)V1.elementAt(v)).equals((String)V2.elementAt(v)))
				{ same=false; break;}
			}
			if(!same) break;
		}
		pl("");
		pl("Cool.. I got the new schema now.");
		if(same)
			pl("But it doesn't look like the old schema changed at all! Oh well, on we go...");
		pl("");
		pl("");
		/////////////////////////////////////////////////////////////////////////////	
		/////// Source Database information
		/////////////////////////////////////////////////////////////////////////////
		String answer="";
		while((!answer.startsWith("Y"))&&(!answer.startsWith("N")))
		{
			pl("Is your *OLD* database stored in a FakeDB database?");
			p("If you don't know, just answer Yes.  (Yes/No)?");
			answer=in.readLine().toUpperCase().trim();
		}
		pl("");
		pl("");
		String sclass="com.planet_ink.fakedb.Driver";
		answer=answer.substring(0,1);
		boolean tested=false;
		boolean secondTime=false;
		while(!tested)
		{
			if((answer.equals("N"))||(secondTime))
			{
				pl("Enter the driver class for your *OLD* database.");
				pl("If you've forgotten what it is, you'll find it ");
				pl("as the DBCLASS entry in your old coffeemud.ini file.");
				p(":");
				sclass=in.readLine();
				if(sclass.trim().length()==0)
				{
					pl("Ooohh.. you entered NOTHING eh?  You must want to quit! Later!");
					return;
				}
			}
			try
			{
				Class.forName(sclass);
				tested=true;
			}
			catch(ClassNotFoundException ce)
			{
				pl("That class name just did not work for me.");
				pl("Are you sure it's in your CLASSPATH?");
				pl("");
			}
			secondTime=true;
		}
			
		String sservice="";
		if(answer.equals("Y"))
		{
			File F=null;
			while(F==null)
			{
				pl("Enter the directory path to the *OLD* FakeDB data directory.");
				p(":");
				String dirPath=in.readLine().trim();
				if(dirPath.trim().length()==0)
				{
					pl("Nowhere, huh?  Ok, bye then...");
					return;
				}
				F=new File(dirPath);
				if((!F.exists())||(!F.isDirectory()))
				{
					pl("Umm.. no, that's not it. Try again.");
					F=null;
				}
				else
				{
					while(dirPath.endsWith(""+File.separatorChar))
						dirPath=dirPath.substring(0,dirPath.length()-1);
					F=new File(dirPath+File.separatorChar+"fakedb.data.CMROOM");
					if((!F.exists())||(F.isDirectory()))
					{
						pl("That's not it. No fakedb.data.CMROOM file there. Try again.");
						F=null;
					}
					else
						sservice="jdbc:fakedb:"+dirPath;
				}
			}
			// where is it?
		}
		
		pl("");
		pl("");
		tested=false;
		String slogin="";
		String spassword="";
		while(!tested)
		{
			if(sservice.length()==0)
			{
				pl("Enter the database service for your *OLD* database.");
				pl("If you've forgotten what it is, you'll find it ");
				pl("as the DBSERVICE entry in your old coffeemud.ini file, ");
				pl("minus all the extraneous backslashes (\\).  For instance, if");
				pl("your INI file says the service is: jdbc\\:mysql\\://localhost\\:3306/coffeemud");
				pl("you would enter it here as: jdbc:mysql://localhost:3306/coffeemud");
				p(":");
				sservice=in.readLine();
				if(sservice.trim().length()==0)
				{
					pl("Ooohh.. you entered NOTHING eh?  You must want to quit! Later!");
					return;
				}
			}
			if(answer.equals("N"))
			{
				pl("Now enter the *OLD* database login (DBUSER from your old ini file).");
				p(":");
				slogin=in.readLine();
				pl("Now enter the *OLD* database password (DBPASS from your old ini file).");
				p(":");
				spassword=in.readLine();
			}
			try
			{
				Class.forName(sclass);
				java.sql.Connection myConnection=DriverManager.getConnection(sservice,slogin,spassword);
				java.sql.Statement myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				java.sql.ResultSet R=myStatement.executeQuery("SELECT * FROM CMROOM");
				if(R!=null){
					tested=true;
					R.close();
					myStatement.close();
					myConnection.close();
				}
			}
			catch(Exception ce)
			{
				pl("That information just did not work for me.");
				pl("");
			}
		}
		/////////////////////////////////////////////////////////////////////////////	
		/////// Destination DB info
		/////////////////////////////////////////////////////////////////////////////
		pl("");
		pl("Ok, our source database is all ready!");
		pl("");
		pl("");
		answer="";
		while((!answer.startsWith("Y"))&&(!answer.startsWith("N")))
		{
			pl("Is your *NEW* database stored in a FakeDB database?");
			p("If you don't know, just answer Yes.  (Yes/No)?");
			answer=in.readLine().toUpperCase().trim();
		}
		pl("");
		pl("");
		String dclass="com.planet_ink.fakedb.Driver";
		answer=answer.substring(0,1);
		tested=false;
		secondTime=false;
		while(!tested)
		{
			if((answer.equals("N"))||(secondTime))
			{
				pl("Enter the driver class for your *NEW* database.");
				pl("If you've forgotten what it is, you'll find it ");
				pl("as the DBCLASS entry in your new coffeemud.ini file.");
				p(":");
				dclass=in.readLine();
				if(dclass.trim().length()==0)
				{
					pl("Ooohh.. you entered NOTHING eh?  You must want to quit! Later!");
					return;
				}
			}
			try
			{
				Class.forName(dclass);
				tested=true;
			}
			catch(ClassNotFoundException ce)
			{
				pl("That class name just did not work for me.");
				pl("Are you sure it's in your CLASSPATH?");
				pl("");
			}
			secondTime=true;
		}
			
		pl("");
		pl("");
		String dservice="";
		if(answer.equals("Y"))
		{
			File F=null;
			while(F==null)
			{
				pl("Enter the directory path to the *NEW* FakeDB data directory.");
				p(":");
				String dirPath=in.readLine().trim();
				if(dirPath.trim().length()==0)
				{
					pl("Nowhere, huh?  Ok, bye then...");
					return;
				}
				F=new File(dirPath);
				if((!F.exists())||(!F.isDirectory()))
				{
					pl("Umm.. no, that's not it. Try again.");
					F=null;
				}
				else
				{
					while(dirPath.endsWith(""+File.separatorChar))
						dirPath=dirPath.substring(0,dirPath.length()-1);
					F=new File(dirPath+File.separatorChar+"fakedb.schema");
					if((!F.exists())||(F.isDirectory()))
					{
						pl("Umm.. no, that's not it. No fakedb.schema file there. Try again.");
						F=null;
					}
					else
						dservice="jdbc:fakedb:"+dirPath;
				}
			}
			// where is it?
		}
		
		tested=false;
		String dlogin="";
		String dpassword="";
		while(!tested)
		{
			if(dservice.length()==0)
			{
				pl("Enter the database service for your *NEW* database.");
				pl("If you've forgotten what it is, you'll find it ");
				pl("as the DBSERVICE entry for your new coffeemud.ini file, ");
				pl("minus all the extraneous backslashes (\\).  For instance, if");
				pl("your INI file will say the service is: jdbc\\:mysql\\://localhost\\:3306/coffeemud");
				pl("you would enter it here as: jdbc:mysql://localhost:3306/coffeemud");
				p(":");
				dservice=in.readLine().trim();
				if(dservice.trim().length()==0)
				{
					pl("Ooohh.. you entered NOTHING eh?  You must want to quit! Later!");
					return;
				}
				if(sservice.equalsIgnoreCase(dservice))
				{
					pl("That is NOT funny.  Your source and destination services MUST be different!!");
					dservice="";
					continue;
				}
			}
			if(answer.equals("N"))
			{
				pl("Now enter the *NEW* database login (DBUSER for your new ini file).");
				p(":");
				dlogin=in.readLine();
				pl("Now enter the *NEW* database password (DBPASS for your new ini file).");
				p(":");
				dpassword=in.readLine();
			}
			try
			{
				Class.forName(dclass);
				java.sql.Connection myConnection=DriverManager.getConnection(dservice,dlogin,dpassword);
				java.sql.Statement myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				java.sql.ResultSet R=myStatement.executeQuery("SELECT * FROM CMROOM");
				if(R!=null){
					tested=true;
					if(R.next())
					{
						pl("Argh! There is data in your destination database!");
						pl("Go empty all the tables and run this again!");
						return;
					}
					R.close();
					myStatement.close();
					myConnection.close();
				}
			}
			catch(Exception ce)
			{
				pl("That information just did not work for me.");
				pl("");
			}
		}
		/////////////////////////////////////////////////////////////////////////////	
		/////// Start sucking data
		/////////////////////////////////////////////////////////////////////////////
		pl("");
		pl("Ok, our destination database is all ready too!");
		pl("Time to read in your source data and get this whole thing started.");
		pl("");
		pl("");
		p("Reading source tables: ");
		Hashtable data=new Hashtable();
		try
		{
			Class.forName(sclass);
			java.sql.Connection myConnection=DriverManager.getConnection(sservice,slogin,spassword);
			for(Enumeration e=oldTables.keys();e.hasMoreElements();)
			{
				String table=(String)e.nextElement();
				Vector fields=(Vector)oldTables.get(table);
				Vector rows=new Vector();
				data.put(table,rows);
				java.sql.Statement myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				java.sql.ResultSet R=myStatement.executeQuery("SELECT * FROM "+table);
				while(R.next())
				{
					Vector row=new Vector();
					rows.addElement(row);
					for(int s=0;s<fields.size();s++)
					{
						String S=R.getString(((String)fields.elementAt(s)).substring(1));
						if(S==null) S="";
						row.addElement(S);
					}
				}
				R.close();
				myStatement.close();
				p(table+"("+rows.size()+") ");
			}
		}
		catch(Exception ce)
		{
			pl("Oops.. something bad happened!");
			pl(ce.getMessage());
			pl("Fix it and run this again!");
			return;
		}
		pl(" ");
		pl(" ");
		p("OK! Writing destination tables: ");
		
		try
		{
			Class.forName(dclass);
			java.sql.Connection myConnection=DriverManager.getConnection(dservice,dlogin,dpassword);
			for(Enumeration e=newTables.keys();e.hasMoreElements();)
			{
				String table=(String)e.nextElement();
				Vector ofields=(Vector)oldTables.get(table);
				Vector nfields=(Vector)newTables.get(table);
				Vector rows=(Vector)data.get(table);
				p(table);
				if((rows==null)||(rows.size()==0))
				{
					p(" ");
					continue;
				}
				int[] matrix=new int[nfields.size()];
				for(int i=0;i<nfields.size();i++)
				{
					String field=(String)nfields.elementAt(i);
					int oldIndex=-1;
					for(int u=0;u<ofields.size();u++)
						if(((String)ofields.elementAt(u)).substring(1).equals(field.substring(1)))
						{ oldIndex=u; break;}
					matrix[i]=oldIndex;
				}
				
				for(int r=0;r<rows.size();r++)
				{
					Vector row=(Vector)rows.elementAt(r);
					java.sql.Statement myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					StringBuffer str=new StringBuffer("INSERT INTO "+table+" (");
					for(int i=0;i<nfields.size();i++)
						str.append(((String)nfields.elementAt(i)).substring(1)+",");
					if(nfields.size()>0)
						str.setCharAt(str.length()-1,' ');
					str.append(") VALUES (");
					for(int i=0;i<nfields.size();i++)
					{
						String field=(String)nfields.elementAt(i);
						int oldIndex=matrix[i];
						String value=null;
						if(field.startsWith("#"))
						{
							if(oldIndex>=0)
								value=(String)row.elementAt(oldIndex);
							else
								value="0";
						}
						else
						{
							if(oldIndex>=0)
								value="'"+((String)row.elementAt(oldIndex))+"'";
							else
								value="''";
						}
						str.append(value+",");
					}
					if(nfields.size()>0)
						str.setCharAt(str.length()-1,')');
					else
						str.append(")");
					myStatement.executeUpdate(str.toString());
					if(debug) p(".");
					myStatement.close();
				}
				p(" ");
			}
		}
		catch(Exception ce)
		{
			pl("Oops.. something bad happened!");
			pl(ce.getMessage());
			pl("Probably out of luck -- was a good try though!");
			return;
		}
		pl(" ");
		pl(" ");
		pl("Awesome! This program is done!");
	}
}
