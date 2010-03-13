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
      boolean isKey;
      int     keyNum;
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

	  public static boolean isNumber(String s)
	  {
	      try{ Double.parseDouble(s); return true;}
	      catch(Exception e){ return false;}
	  }
	  
	  public static double s_double(String DOUBLE)
	  {
	      double sdouble=0;
	      try{ sdouble=Double.parseDouble(DOUBLE); }
	      catch(Exception e){ return 0;}
	      return sdouble;
	  }
	  
	  public static long s_long(String LONG)
	  {
	      long slong=0;
	      try{ slong=Long.parseLong(LONG); }
	      catch(Exception e){ return 0;}
	      return slong;
	  }
	  
	  public static boolean isDouble(String DBL)
	  {
		  if(!isNumber(DBL))
			  return false;
		  return DBL.indexOf('.')>=0;
	  }
	  
	  public static int numCompare(String s1, String s2)
	  {
	      if((s1==null)||(s2==null)) return 0;
	      if((!isNumber(s1))||(!isNumber(s2))) return 0;
	      if(isDouble(s1)||(isDouble(s2)))
	      {
	          double d1=isDouble(s1)?s_double(s1):Long.valueOf(s_long(s1)).doubleValue();
	          double d2=isDouble(s2)?s_double(s2):Long.valueOf(s_long(s2)).doubleValue();
	          if(d1==d2) return 0;
	          if(d1>d2) return 1;
	          return -1;
	      }
	      long l1=s_long(s1);
	      long l2=s_long(s2);
	      if(l1==l2) return 0;
	      if(l1>l2) return 1;
	      return -1;
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
   
   public static enum ConnectorType { AND, OR }
   
   public class FakeCondition
   {
	   public int conditionIndex;
	   public String conditionValue;
	   public boolean eq=true;
	   public boolean lt=false;
	   public boolean gt=false;
	   public boolean not=false;
	   public ConnectorType connector = ConnectorType.AND;
	   public List<FakeCondition> contains = null;
	   public boolean compareValue(String subKey)
	   {
		   if(subKey==null) subKey="";
           int nc=(lt||gt)?numCompare(subKey,conditionValue):0;
           int sc=(lt||gt)?subKey.compareTo(conditionValue):0;
           if(!(((eq)&&(subKey.equals(conditionValue)))
           ||((lt)&&(nc<0))
           ||((gt)&&(nc>0))
           ||((lt)&&(sc<0))
           ||((gt)&&(sc>0))))
               return false;
           return true;
	   }
   }
   
   public interface FakeConditionResponder
   {
	   public void callBack(FakeKey key, boolean[] nullIndicators, String[] values, RecordInfo info) throws Exception;
   }

   
   public static class FakeKey implements Comparable
   {
	   public String[] s=null;
		public int compareTo(Object o) {
			if(o==this) return 0;
			if(hashCode()<o.hashCode()) return -1;
			return 1;
		}
	   
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

      private FakeColumn[]     columns;
      private Map<String,Integer> columnHash = new Hashtable<String,Integer>();
      private int[]            keys;
      private TreeMap<FakeKey,Backend.RecordInfo> index=new TreeMap<FakeKey,Backend.RecordInfo>();

      FakeTable(File name) 
      { 
    	  fileName=name; 
      }

      protected int numColumns(){ return columns.length;}
      protected TreeMap<FakeKey,Backend.RecordInfo> indexMap() { return index;}
      
      /**
       * 
       * @param name
       * @return
       */
      protected int findColumn(String name) 
      {
    	 if((name!=null)&&(columnHash.containsKey(name)))
    		 return columnHash.get(name).intValue();
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
	     columnHash = null;
	     keys=null;
	     index=new TreeMap<FakeKey,Backend.RecordInfo>();
	  }
	  
      /**
       * 
       * @throws IOException
       */
      protected void open() throws IOException
      {
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
               int current=-1,currentPos=ofs;
               String[] key = new String[keys.length];
               for (int index=0;index<keys.length;index++) 
               {
                  while (current<keys[index]) 
                  {
                     while (fileBuffer[currentPos]!=0x0A) 
                    	 currentPos++;
                     currentPos++; current++;
                  }
                  StringBuilder keyData = new StringBuilder(); 
                  for (int sub=currentPos;;++sub) 
                  {
                     char c=(char)(fileBuffer[sub]&0xFF);
                     if (c==0x0A) break;
                     keyData.append(c);
                  }
                  key[index]=keyData.toString();
               }
               FakeKey f = new FakeKey();
               f.s=key;
               index.put(f,new RecordInfo(fileSize,size));
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
         for (Iterator<FakeKey> iter=index.keySet().iterator();iter.hasNext();) 
         {
            FakeKey key=iter.next();
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
      protected synchronized boolean insertRecord(FakeKey key, boolean[] nullIndicators, String[] values)
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
      protected synchronized int deleteRecord(List<FakeCondition> conditions)
      {
		 int[] count={0};
         try 
         {
        	FakeConditionResponder responder = new FakeConditionResponder()
        	{
        		public int[] count;
                public FakeConditionResponder init(int[] c) { count=c; return this;}
                
				public void callBack(FakeKey key, boolean[] nullIndicators, String[] values, RecordInfo info) throws Exception 
				{
					file.seek(info.offset);
					file.write(new byte[]{(byte)'*'});
					index.remove(key);
					count[0]++;
				}
        	}.init(count);
        	recordIterator(conditions,responder);
         } 
		 catch (Exception e)
		 { 
			 e.printStackTrace(); return -1; 
		 }
         return count[0];
      }
      
      public boolean recordCompare(FakeKey rowKeyData, RecordInfo info, List<FakeCondition> conditions, boolean[] dataLoaded, boolean[] nullIndicators, String[] values)
      {
    	  boolean lastOne = true;
    	  ConnectorType connector = ConnectorType.AND;
    	  for(FakeCondition cond : conditions)
    	  {
    		  boolean thisOne = false;
    		  if(cond.contains!=null)
    			  thisOne = recordCompare(rowKeyData,info,cond.contains,dataLoaded,nullIndicators,values);
    		  else
    		  {
    			  FakeColumn column = columns[cond.conditionIndex];
    			  if(column.isKey)
    				  thisOne = cond.compareValue(rowKeyData.s[column.keyNum]);
    			  else
    			  {
	            	 if(!dataLoaded[0])
	            		 dataLoaded[0] = getRecord(nullIndicators,values,info);
	            	 if(dataLoaded[0])
	            	 {
		                 if (nullIndicators[cond.conditionIndex])
		                	  thisOne = false;
		                 else
		                 if(cond.not)
		                	  thisOne = !cond.compareValue(values[cond.conditionIndex]);
		                 else
		                	  thisOne = cond.compareValue(values[cond.conditionIndex]);
	            	 }
    			  }
    		  }
    		  if(connector == ConnectorType.OR)
    			  lastOne = lastOne || thisOne;
    		  else
    			  lastOne = lastOne && thisOne;
    		  connector = cond.connector;
    	  }
    	  return lastOne;
      }

      /**
       * 
       * @param conditions
       * @param callBack
       */
      public void recordIterator(List<FakeCondition> conditions, FakeConditionResponder callBack) throws Exception
      {
    	  TreeMap<FakeKey,Backend.RecordInfo> safeIndex = (TreeMap<FakeKey,Backend.RecordInfo>)index.clone();
    	  boolean[] dataLoaded=new boolean[1];
    	  boolean[] nullIndicators=new boolean[columns.length];
    	  String[] values=new String[columns.length];
          for (Iterator<FakeKey> iter=safeIndex.keySet().iterator();iter.hasNext();) 
          {
        	  FakeKey current=iter.next();
             RecordInfo info=(RecordInfo)safeIndex.get(current);
             dataLoaded[0]=false;
             if(recordCompare(current,info,conditions,dataLoaded,nullIndicators,values))
             {
            	 if(!dataLoaded[0])
            		 dataLoaded[0] = getRecord(nullIndicators,values,info);
            	 if(dataLoaded[0])
	            	 callBack.callBack(current, nullIndicators, values, info);
             }
          }
      }
      
      /**
       * 
       * @param key
       * @param value
       * @param columns
       * @param newValues
       * @return
       */
      protected synchronized int updateRecord(List<FakeCondition> conditions, int[] columns, String[] newValues)
      {
   		 int[] count={0};
         try 
         {
        	FakeConditionResponder responder = new FakeConditionResponder()
        	{
        		public int[] count;
                public int[] newCols;
                public String[] newValues;
                public FakeConditionResponder init(int[] c, int[] a, String[] n) { count=c; newCols=a; newValues=n; return this;}
                
				public void callBack(FakeKey key, boolean[] nullIndicators, String[] values, RecordInfo info) throws Exception 
				{
					for (int sub=0; sub<newCols.length; sub++) 
					{
					   nullIndicators[newCols[sub]]=false;
					   values[newCols[sub]]=newValues[sub];
					   for(int k=0;k<keys.length;k++)
						   if(keys[k]==newCols[sub])
							   key.s[k]=newValues[sub];
					}
					insertRecord(key, nullIndicators, values);
					file.seek(info.offset);
					file.write(new byte[]{(byte)'*'});
					count[0]++;
				}
        	}.init(count, columns, newValues);
        	recordIterator(conditions,responder);
         } 
		 catch (Exception e)
		 { 
			 e.printStackTrace(); return -1; 
		 }
         return count[0];
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
               info.isKey = true;
               info.keyNum = keys.size();
               keys.add(columnName);
            } 
            else 
            	throw new IOException("Can not read schema: attributeSpecial '"+columnModifier+"' is unknown");
            columns.add(info);
         }

         FakeTable fakeTable=new FakeTable(new File(basePath,"fakedb.data."+fakeTableName));
         fakeTable.columns=new FakeColumn[columns.size()];
         fakeTable.columnHash = new Hashtable<String,Integer>();
         int index=0;
         for (Iterator iter=columns.iterator();iter.hasNext();++index) 
         {
            FakeColumn current=(FakeColumn)iter.next();
            fakeTable.columns[index]=current;
            fakeTable.columnHash.put(current.name, Integer.valueOf(index));
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
		                                      List<Backend.FakeCondition> conditions,
		                                      String orderVar) 
   throws java.sql.SQLException
   {
      FakeTable table=(FakeTable)fakeTables.get(tableName);
      if (table==null) throw new java.sql.SQLException("unknown relation "+tableName);

      int conditionIndex=-1;
      if (orderVar!=null) 
      {
    	  //TODO: implement order by -- this is a black hold.
         int index=table.findColumn(orderVar);
         if (index<0) throw new java.sql.SQLException("unknown column "+orderVar);
         if ((table.keys.length==0)||((table.keys[0]!=index)&&((orderVar==null)||(conditionIndex>=0)||(table.keys.length<2)||(table.keys[1]!=index))))
            throw new java.sql.SQLException("order by "+orderVar+" not supported");
      }
      return new ResultSet(s,table,conditions);
   }
   
   /**
    * 
    * @param tableName
    * @param columns
    * @param dataValues
    * @throws java.sql.SQLException
    */
   protected void insertValues(String tableName, String[] columns, String[] dataValues) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(tableName);
      if (fakeTable==null) throw new java.sql.SQLException("unknown relation "+tableName);

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
      String[] keys=new String[fakeTable.keys.length];
      for (int index=0;index<fakeTable.keys.length;index++) 
      {
         int id=fakeTable.keys[index];
         if (nullIndicators[id]) 
        	 throw new java.sql.SQLException("keys may not be NULL");
         keys[index]=values[id];
      }
      FakeKey f = new FakeKey();
      f.s=keys;
      if (!fakeTable.insertRecord(f,nullIndicators,values))
         throw new java.sql.SQLException("unable to insert record");
   }
   
   /**
    * 
    * @param tableName
    * @param conditionVar
    * @param conditionValue
    * @throws java.sql.SQLException
    */
   protected void deleteRecord(String tableName, List<FakeCondition> conditions) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(tableName);
      if (fakeTable==null) 
    	  throw new java.sql.SQLException("unknown relation "+tableName);

      fakeTable.deleteRecord(conditions);
   }
   
   /**
    * 
    * @param tableName
    * @param conditionVar
    * @param conditionValue
    * @param varNames
    * @param values
    * @throws java.sql.SQLException
    */
   protected void updateRecord(String tableName, List<FakeCondition> conditions, String[] varNames, String[] values) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(tableName);
      if (fakeTable==null) 
    	  throw new java.sql.SQLException("unknown relation "+tableName);

      int[] vars=new int[varNames.length];
      for (int index=0;index<vars.length;index++)
         if ((vars[index]=fakeTable.findColumn(varNames[index]))<0)
            throw new java.sql.SQLException("unknown column "+varNames[index]);
      fakeTable.updateRecord(conditions, vars, values);
   }
   
   public FakeCondition buildFakeCondition(String tableName, String columnName, String comparitor, String value) throws java.sql.SQLException
   {
      FakeTable fakeTable=(FakeTable)fakeTables.get(tableName);
      if (fakeTable==null) throw new java.sql.SQLException("unknown table "+tableName);
      FakeCondition fake = new FakeCondition();
      if(columnName==null)
	  {
    	  fake.conditionIndex = 0;
    	  fake.conditionValue="";
    	  return fake;
	  }
      if((fake.conditionIndex = fakeTable.findColumn(columnName))<0)
	      throw new java.sql.SQLException("unknown column "+tableName+"."+columnName);
	  fake.conditionValue = value;
	  comparitor=comparitor.trim();
	  fake.not=comparitor.startsWith("!");
      fake.eq=(comparitor.indexOf("=")>=0);
      fake.lt=(comparitor.indexOf("<")>=0);
      fake.gt=(comparitor.indexOf(">")>=0);
      return fake;
   }
   
}