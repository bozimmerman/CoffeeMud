package com.planet_ink.fakedb;

/* 
   Copyright 2001 Thomas Neumann
   Copyright 2009-20010 Bo Zimmerman
   
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

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Backend
{
   File basePath;
   private Map<String,FakeTable> fakeTables=new HashMap<String,FakeTable>();

   /**
    * 
    */
   protected static class FakeColumn 
   {
      String  name;
      int     type;
      boolean canNull;
   }
   
   /**
    * 
    */
   protected static class RecordInfo 
   {
      int offset,size;
      RecordInfo(int o,int s) 
      { 
    	  offset=o; 
    	  size=s; 
      }
   }

   /**
    * 
    */
   public void clearFakeTables()
   {
	   basePath=null;
	   if(fakeTables!=null)
		   for(FakeTable R : fakeTables.values())
			   R.close();
	   fakeTables=new HashMap<String,FakeTable>();
   }
   
   /**
    * 
    */
   protected static class FakeTable 
   {
      File             fileName;
      RandomAccessFile file;
      int              fileSize;
      byte[]           fileBuffer;

      FakeColumn[]     columns;
      int[]            keys;
      Map              index=new TreeMap();

      FakeTable(File name) { fileName=name; }

      /**
       * 
       * @param name
       * @return
       */
      protected int findColumn(String name) 
      {
         // might even be faster than using a hashtable (overhead for Integer etc.) when only a
         // few attributes are present...
         for (int index=0;index<columns.length;++index)
            if (columns[index].name.equals(name))
               return index;
         return -1;
      }
	  
      /**
       * 
       */
      protected void close()
	  {
    	 fileName=null;
	     if(file!=null)
	     {
		     try
		     {
			     file.close();
		     } 
		     catch(Exception e)
		     {
		     }
		     file=null;
	     }
	     columns=null;
	     keys=null;
	     index=new TreeMap();
	  }
	  
      /**
       * 
       * @throws IOException
       */
      protected void open() throws IOException
      {
         StringBuffer key=new StringBuffer();
         file=new RandomAccessFile(fileName,"rw");
         fileSize=0;
         fileBuffer=new byte[4096];

         int remaining=0,ofs=0;
         int found=0,skipped=0;
         while (true) 
         {
            if (remaining==0) 
            {
               ofs=0;
               remaining=file.read(fileBuffer);
               if (remaining<0) break;
            }
            boolean skip;
            if (fileBuffer[ofs]=='-') // deleted
            {
               skip=false;
            } 
            else if (fileBuffer[ofs]=='*')  // active
            {
               skip=true;
            } 
            else 
            	break;
            // check if valid...
            boolean valid=true;
            int     size=0;
            while (true) 
            {
               int toCheck=columns.length+1;
               for (int index=ofs,left=remaining;left>0;left--,index++)
                  if (fileBuffer[index]==0x0A)
                     if (--toCheck==0) 
                     { 
                    	 size=index-ofs+1; 
                    	 break; 
                     }
               if (toCheck==0) 
            	   break;
               if (ofs>0) 
               {
                  System.arraycopy(fileBuffer,ofs,fileBuffer,0,remaining);
                  ofs=0;
               }
               if (ofs+remaining==fileBuffer.length) 
               {
                  byte[] newFileBuffer=new byte[fileBuffer.length*2];
                  System.arraycopy(fileBuffer,0,newFileBuffer,0,remaining);
                  fileBuffer=newFileBuffer;
               }
               int additional=file.read(fileBuffer,remaining,fileBuffer.length-remaining);
               if (additional<0) 
               { 
            	   valid=false; 
            	   break; 
               }
               remaining+=additional;
            }
            if (!valid) break;
            // Build index string
            if (!skip) 
            {
               key.setLength(0);
               int current=-1,currentPos=ofs;
               for (int index=0;index<keys.length;index++) 
               {
                  while (current<keys[index]) 
                  {
                     while (fileBuffer[currentPos]!=0x0A) 
                    	 currentPos++;
                     currentPos++; current++;
                  }
                  for (int sub=currentPos;;++sub) 
                  {
                     char c=(char)(fileBuffer[sub]&0xFF);
                     key.append(c);
                     if (c==0x0A) break;
                  }
               }
               this.index.put(key.toString(),new RecordInfo(fileSize,size));
            } 
            else 
            	skipped+=size;
            found+=size;
            // Fix pointers
            ofs+=size; remaining-=size;
            fileSize+=size;
         }
         // Too much space wasted?
         if (skipped>(found/10))
            vacuum();
      }
      
      /**
       * 
       * @throws IOException
       */
      private void vacuum() throws IOException
      {
         File tempFileName=new File(fileName.getName()+".tmp");
         File tempFileName2=new File(fileName.getName()+".cpy");
         RandomAccessFile tempOut=new RandomAccessFile(tempFileName,"rw");
         int newFileSize=0;
         for (Iterator iter=index.keySet().iterator();iter.hasNext();) 
         {
            String key=(String)iter.next();
            RecordInfo info=(RecordInfo)index.get(key);
            file.seek(info.offset);
            file.readFully(fileBuffer,0,info.size);
            tempOut.write(fileBuffer,0,info.size);
            info.offset=newFileSize; newFileSize+=info.size;
         }
         tempOut.getFD().sync();
         tempOut.close();
         file.close();
         tempFileName2.delete();
         fileName.renameTo(tempFileName2);
         tempFileName.renameTo(fileName);
         tempFileName2.delete();
         file=new RandomAccessFile(fileName,"rw");
		 fileSize=newFileSize;
      }
      
      /**
       * 
       * @param nullIndicators
       * @param values
       * @param info
       * @return
       */
      protected synchronized boolean getRecord(boolean[] nullIndicators, String[] values, RecordInfo info)
      {
         try 
         {
            file.seek(info.offset);
            file.readFully(fileBuffer,0,info.size);
            int ofs=0;
            StringBuffer buffer=new StringBuffer();
            for (int index=0;index<columns.length;index++) 
            {
               while (fileBuffer[ofs]!=0x0A) 
            	   ofs++;
               ofs++;
               if ((fileBuffer[ofs]=='\\')&&(fileBuffer[ofs+1]=='?')) 
               {
                  nullIndicators[index]=true;
                  values[index]=null;
               } 
               else 
               {
                  nullIndicators[index]=false;
                  buffer.setLength(0);
                  for (int sub=ofs;;sub++) 
                  {
                     char c=(char)(fileBuffer[sub]&0xFF);
                     if (c==0x0A) break;
                     if (c=='\\') 
                     {
                        if (fileBuffer[sub+1]=='\\') 
                        {
                           buffer.append('\\');
                           sub++;
                        } 
                        else if (fileBuffer[sub+1]=='n') 
                        {
                           buffer.append((char)0x0A);
                           sub++;
                        } 
                        else 
                        {
                           int val=0;
                           for (int i=0;i<4;i++) 
                           {
                              c=(char)(fileBuffer[++sub]&0xFF);
                              if (c>='A')
                                 val=(16*val)+(c-'A'); 
                              else
                                 val=(16*val)+(c-'0');
                           }
                        }
                     } 
                     else 
                    	 buffer.append(c);
                  }
                  values[index]=buffer.toString();
               }
            }
            return true;
         } 
         catch (IOException e) 
         { 
        	 return false; 
         }
      }
      
      /**
       * 
       * @param required
       */
      private void increaseBuffer(int required) 
      {
         int newSize=((required+4095)>>>12)<<12;
         byte[] newBuffer=new byte[newSize];
         System.arraycopy(fileBuffer,0,newBuffer,0,fileBuffer.length);
         fileBuffer=newBuffer;
      }
      
      /**
       * 
       * @param key
       * @param nullIndicators
       * @param values
       * @return
       */
      protected synchronized boolean insertRecord(String key, boolean[] nullIndicators, String[] values)
      {
         try 
         {
            int ofs=2;
            fileBuffer[0]=(byte)'-'; fileBuffer[1]=(byte)0x0A;
            for (int index=0;index<nullIndicators.length;index++)
               if (nullIndicators[index]) 
               {
                  if (ofs+3>fileBuffer.length) increaseBuffer(ofs+3);
                  fileBuffer[ofs+0]=(byte)'\\'; fileBuffer[ofs+1]=(byte)'?'; fileBuffer[ofs+2]=(byte)0x0A;
                  ofs+=3;
               } 
               else 
               {
                  int size=0;
                  for (int sub=0;sub<values[index].length();sub++) 
                  {
                     char c=values[index].charAt(sub);
                     if (c=='\\') 
                    	 size+=2; 
                     else
                     if (c=='\n') 
                    	 size+=2; 
                     else
                     if (c>255) 
                    	 size+=5; 
                     else 
                    	 size++;
                  }
                  if (ofs+size+1>fileBuffer.length) 
                	  increaseBuffer(ofs+size+1);
                  for (int sub=0;sub<values[index].length();sub++) 
                  {
                     char c=values[index].charAt(sub);
                     if (c=='\\') 
                     {
                        fileBuffer[ofs]=(byte)'\\'; 
                        fileBuffer[ofs+1]=(byte)'\\'; 
                        ofs+=2;
                     } 
                     else 
                     if (c=='\n') 
                     {
                        fileBuffer[ofs]=(byte)'\\'; 
                        fileBuffer[ofs+1]=(byte)'n'; 
                        ofs+=2;
                     } 
                     else 
                     if (c>255) 
                     {
                        fileBuffer[ofs++]=(byte)'\\';
                        for (int i=0;i<4;i++) 
                        {
                           fileBuffer[ofs++]=(byte)("0123456789ABCDEF".charAt(c>>>12));
                           c<<=4;
                        }
                     } 
                     else 
                    	 fileBuffer[ofs++]=(byte)c;
                  }
                  fileBuffer[ofs++]=(byte)0x0A;
               }
            file.seek(fileSize);
            file.write(fileBuffer,0,ofs);
            file.getFD().sync();

            index.put(key,new RecordInfo(fileSize,ofs));
            fileSize+=ofs;

            return true;
         } 
         catch (IOException e) 
         { 
        	 return false; 
         }
      }
      
      /**
       * 
       * @param key
       * @param value
       * @return
       */
      protected synchronized int deleteRecord(int key, String value)
      {
         int count=0;
         try 
         {
            byte[] mark=new byte[1]; mark[0]=(byte)'*';
            if (key<0) 
            {
               for (Iterator iter=index.keySet().iterator();iter.hasNext();) 
               {
	                String current=(String)iter.next();
					if(!current.startsWith(value)) 
						continue;
					RecordInfo info=(RecordInfo)index.get(current);
					file.seek(info.offset);
					file.write(mark);
					count++;
					iter.remove();
               }
            } 
            else 
            {
               boolean[] nullIndicators=new boolean[columns.length];
               String[] values=new String[columns.length];
               for (Iterator iter=index.values().iterator();iter.hasNext();) 
               {
                  RecordInfo info=(RecordInfo)iter.next();
                  getRecord(nullIndicators,values,info);
                  if (nullIndicators[key]) 
                	  continue;
                  if (!values[key].equals(value)) 
                	  continue;
                  file.seek(info.offset);
                  file.write(mark);
                  count++;
                  iter.remove();
               }
            }
         } 
         catch (IOException e)
         { 
        	 return -1; 
         }
         return count;
      }
      
      /**
       * 
       * @param key
       * @param value
       * @param attributes
       * @param newValues
       * @return
       */
      protected synchronized int updateRecord(int key, String value, int[] attributes, String[] newValues)
      {
         int count=0;
         try 
         {
            boolean[] nullIndicators=new boolean[this.columns.length];
            String[] values=new String[this.columns.length];
            byte[] mark=new byte[1]; 
            mark[0]=(byte)'*';
            if (key<0) 
            {
               for (Iterator iter=index.keySet().iterator();iter.hasNext();) 
               {
                  String current=(String)iter.next();
				  if(!current.startsWith(value)) 
					  continue;
				  RecordInfo info=(RecordInfo)index.get(current);
				  getRecord(nullIndicators,values,info);
				  for (int sub=0; sub<attributes.length; sub++) 
				  {
				     nullIndicators[attributes[sub]]=false;
				     values[attributes[sub]]=newValues[sub];
				  }
				  insertRecord(current, nullIndicators, values);
				  file.seek(info.offset);
				  file.write(mark);
				  count++;
               }
            } 
            else 
            {
               for (Iterator iter=index.keySet().iterator();iter.hasNext();) 
               {
                  String current=(String)iter.next();
                  RecordInfo info=(RecordInfo)index.get(current);
                  getRecord(nullIndicators, values, info);
                  if (nullIndicators[key]) 
                	  continue;
                  if (!values[key].equals(value)) 
                	  continue;
                  for (int sub=0; sub<attributes.length; sub++) 
                  {
                     nullIndicators[attributes[sub]]=false;
                     values[attributes[sub]]=newValues[sub];
                  }
                  insertRecord(current, nullIndicators, values);
                  file.seek(info.offset);
                  file.write(mark);
                  count++;
               }
            }
         } 
		 catch (IOException e) 
		 { 
			 e.printStackTrace(); return -1; 
		 }
         return count;
      }
   }

   /**
    * 
    * @param basePath
    * @param schema
    * @throws IOException
    */
   private void readSchema(File basePath,File schema) throws IOException
   {
      BufferedReader in=new BufferedReader(new FileReader(schema));

      while (true) 
      {
         String fakeTableName=in.readLine();
         if (fakeTableName==null) 
        	 break;
         if (fakeTableName.length()==0) 
        	 throw new IOException("Can not read schema: relationName is null");
         if (fakeTables.get(fakeTableName)!=null) 
        	 throw new IOException("Can not read schema: relationName is missing: "+fakeTableName);

         List columns=new LinkedList();
         List keys=new LinkedList();
         while (true) 
         {
            String line=in.readLine();
            if (line==null) 
            	break;
            if (line.length()==0) 
            	break;
            int split=line.indexOf(' ');
            if (split<0) 
            	throw new IOException("Can not read schema: expected space in line '"+line+"'");
            String columnName=line.substring(0,split); 
            line=line.substring(split+1);
            split=line.indexOf(' ');
            String columnType,columnModifier;
            if (split<0) 
            {
               columnType=line;
               columnModifier="";
            } 
            else 
            {
               columnType=line.substring(0,split);
               columnModifier=line.substring(split+1);
            }

            FakeColumn info=new FakeColumn();
            info.name=columnName;
            if (columnType.equals("string")) 
            {
               info.type=0;
            } 
            else 
            if (columnType.equals("integer")) 
            {
               info.type=1;
            } 
            else 
            if (columnType.equals("datetime")) 
            {
               info.type=2;
            } 
            else 
            	throw new IOException("Can not read schema: attributeType '"+columnModifier+"' is unknown");
            if (columnModifier.equals("")) 
            {
            } 
            else 
            if (columnModifier.equals("NULL")) 
            {
               info.canNull=true;
            } 
            else 
            if (columnModifier.equals("KEY")) 
            {
               keys.add(columnName);
            } 
            else 
            	throw new IOException("Can not read schema: attributeSpecial '"+columnModifier+"' is unknown");
            columns.add(info);
         }

         FakeTable fakeTable=new FakeTable(new File(basePath,"fakedb.data."+fakeTableName));
         fakeTable.columns=new FakeColumn[columns.size()];
         int index=0;
         for (Iterator iter=columns.iterator();iter.hasNext();++index) 
         {
            FakeColumn current=(FakeColumn)iter.next();
            fakeTable.columns[index]=current;
         }
         index=0;
         fakeTable.keys=new int[keys.size()];
         for (Iterator iter=keys.iterator();iter.hasNext();++index)
            fakeTable.keys[index]=fakeTable.findColumn((String)iter.next());

         fakeTable.open();
         fakeTables.put(fakeTableName,fakeTable);
      }
   }
   
   /**
    * 
    * @param basePath
    * @return
    */
   protected boolean open(File basePath)
   {
      try 
      {
         readSchema(basePath,new File(basePath,"fakedb.schema"));
         return true;
      } 
      catch (IOException e) 
	  { 
		  e.printStackTrace(); 
		  return false; 
	  }
   }
   
   /**
    * 
    * @param s
    * @param tableName
    * @param conditionVar
    * @param conditionValue
    * @param orderVar
    * @param comparitor
    * @return
    * @throws java.sql.SQLException
    */
   protected java.sql.ResultSet constructScan(Statement s,
		                                      String tableName,
		                                      String conditionVar,
		                                      String conditionValue,
		                                      String orderVar,
		                                      String comparitor) 
   throws java.sql.SQLException
   {
      FakeTable relation=(FakeTable)fakeTables.get(tableName);
      if (relation==null) throw new java.sql.SQLException("unknown relation "+tableName);

      int    conditionIndex=-1;
      if (conditionVar!=null) 
      {
         int index=relation.findColumn(conditionVar);
         if (index<0) throw new java.sql.SQLException("unknown column "+conditionVar);
         if (relation.keys.length>0)
            if (index==relation.keys[0])
               { conditionVar+="\n"; index=-1; }
         if (index>=0) conditionIndex=index;
      }
      if (orderVar!=null) 
      {
         int index=relation.findColumn(orderVar);
         if (index<0) throw new java.sql.SQLException("unknown column "+conditionVar);
         if ((relation.keys.length==0)||((relation.keys[0]!=index)&&((conditionVar==null)||(conditionIndex>=0)||(relation.keys.length<2)||(relation.keys[1]!=index))))
            throw new java.sql.SQLException("order by "+orderVar+" not supported");
      }
      return new ResultSet(s,relation,conditionIndex,conditionValue,comparitor);
   }
   
   /**
    * 
    * @param relationName
    * @param columns
    * @param dataValues
    * @throws java.sql.SQLException
    */
   protected void insertValues(String relationName, String[] columns, String[] dataValues) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(relationName);
      if (fakeTable==null) throw new java.sql.SQLException("unknown relation "+relationName);

      boolean[] nullIndicators=new boolean[fakeTable.columns.length];
      String[] values=new String[fakeTable.columns.length];
      for (int index=0;index<nullIndicators.length;index++)
         nullIndicators[index]=true;

      for (int index=0;index<columns.length;index++) 
      {
         int id=fakeTable.findColumn(columns[index]);
         if (id<0) 
        	 throw new java.sql.SQLException("unknown column "+columns[index]);
         nullIndicators[id]=false;
         values[id]=dataValues[index];
      }
      StringBuffer key=new StringBuffer();
      for (int index=0;index<fakeTable.keys.length;index++) 
      {
         int id=fakeTable.keys[index];
         if (nullIndicators[id]) throw new java.sql.SQLException("keys may not be NULL");
         key.append(values[id]); key.append((char)0x0A);
      }
      if (!fakeTable.insertRecord(key.toString(),nullIndicators,values))
         throw new java.sql.SQLException("unable to insert record");
   }
   
   /**
    * 
    * @param relationName
    * @param conditionVar
    * @param conditionValue
    * @throws java.sql.SQLException
    */
   protected void deleteRecord(String relationName, String conditionVar, String conditionValue) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(relationName);
      if (fakeTable==null) 
    	  throw new java.sql.SQLException("unknown relation "+relationName);

      int conditionIndex=fakeTable.findColumn(conditionVar);
      if (conditionIndex<0) 
    	  throw new java.sql.SQLException("unknown column "+conditionVar);
      if ((fakeTable.keys.length>0)&&(fakeTable.keys[0]==conditionIndex))
      { 
    	  conditionIndex=-1; 
    	  conditionValue+="\n"; 
      }
      fakeTable.deleteRecord(conditionIndex,conditionValue);
   }
   
   /**
    * 
    * @param relationName
    * @param conditionVar
    * @param conditionValue
    * @param varNames
    * @param values
    * @throws java.sql.SQLException
    */
   protected void updateRecord(String relationName, String conditionVar, String conditionValue, String[] varNames, String[] values) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(relationName);
      if (fakeTable==null) throw new java.sql.SQLException("unknown relation "+relationName);

      int conditionIndex=fakeTable.findColumn(conditionVar);
      if (conditionIndex<0) 
    	  throw new java.sql.SQLException("unknown column "+conditionVar);
      if ((fakeTable.keys.length>0)&&(fakeTable.keys[0]==conditionIndex))
      { 
    	  conditionIndex=-1; 
    	  conditionValue+="\n"; 
      }
      int[] vars=new int[varNames.length];
      for (int index=0;index<vars.length;index++)
         if ((vars[index]=fakeTable.findColumn(varNames[index]))<0)
            throw new java.sql.SQLException("unknown column "+varNames[index]);
      fakeTable.updateRecord(conditionIndex, conditionValue, vars, values);
   }
}