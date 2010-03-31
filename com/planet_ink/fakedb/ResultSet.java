package com.planet_ink.fakedb;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.fakedb.Backend.*;
/* 
   Copyright 2001 Thomas Neumann
   Copyright 2009-2010 Bo Zimmerman

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
public class ResultSet implements java.sql.ResultSet
{
   private Statement statement;
   private Backend.FakeTable fakeTable;
   private java.util.Iterator<Backend.RecordInfo> iter;
   private int currentRow=0;
   private List<FakeCondition> conditions;
   private final ComparableValue[] values;
   private final int[] showCols;
   private final int[] orderByKeyDexCols;
   private final String[] orderByConditions;
   private final Map<String,Integer> showColMap=new Hashtable<String,Integer>();
   private boolean wasNullFlag = false;
   private Object countValue = null;
   private static final List<RecordInfo> fakeList = new Vector<RecordInfo>(1);
   static
   {
	   RecordInfo info = new RecordInfo(0,0);
	   info.indexedData=new ComparableValue[0];
	   fakeList.add(info);
   }

   ResultSet(Statement stmt,
             Backend.FakeTable table,
             int[] showCols,
             List<FakeCondition> conditions,
             int[] orderByKeyDexCols,
             String[] orderByConditions) 
   {
      statement=stmt;
      fakeTable=table;
      this.conditions = conditions;
	  currentRow=0;
      this.values=new ComparableValue[table.numColumns()];
      this.showCols = showCols;
      this.orderByKeyDexCols=orderByKeyDexCols;
      this.orderByConditions=orderByConditions;
      this.iter = table.indexIterator(this.orderByKeyDexCols,this.orderByConditions);
      for(int s=0;s<showCols.length;s++)
    	  if(showCols[s]==FakeColumn.INDEX_COUNT)
    	  {
	    	  showColMap.put("COUNT", Integer.valueOf(FakeColumn.INDEX_COUNT));
	          int ct = 0;
	          while(iter.hasNext())
	          {
	        	  iter.next();
	        	  ct++;
	          }
	          countValue=Integer.valueOf(ct);
	          iter=fakeList.iterator();
    	  }
    	  else
	    	  showColMap.put(table.getColumnName(showCols[s]), Integer.valueOf(s));
   }

   public java.sql.Statement getStatement() throws java.sql.SQLException { return statement; }

   public boolean next() throws java.sql.SQLException
   {
      while (true) 
      {
         if (!iter.hasNext()) 
        	 return false;
         Backend.RecordInfo rowInfo=iter.next();
         if(countValue!=null)
         {
        	 currentRow++;
        	 return true;
         }
         if (conditions.size()>0) 
         {
             boolean[] dataLoaded = new boolean[1];
             dataLoaded[0]=false;
             if(!fakeTable.recordCompare(rowInfo,conditions,dataLoaded,values))
            	 continue;
        	 currentRow++;
        	 if(!dataLoaded[0])
        		 dataLoaded[0]=fakeTable.getRecord(values, rowInfo);
        	 if(!dataLoaded[0])
    			 return false;
        	 return true;
         }
    	 currentRow++;
         return fakeTable.getRecord(values, rowInfo);
      }
   }
   public void close() throws java.sql.SQLException
   {
   }
   public boolean wasNull() throws java.sql.SQLException
   {
       return wasNullFlag;
   }
   
   private Object getProperValue(int columnIndex)
   {
  	  wasNullFlag=false;
      if ((columnIndex<1)||(columnIndex>showCols.length))
      {
    	 wasNullFlag=true;
    	 return null;
      } 
      columnIndex=showCols[columnIndex-1];
      if(columnIndex==FakeColumn.INDEX_COUNT)
    	  return this.countValue;
      Object v=values[columnIndex].getValue(); 
      if(v == null)
      {
    	  wasNullFlag=true;
    	  return null;
      }
	  return v;
   }
   
   public String getString(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
	  return o.toString();
   }
   public java.sql.Array getArray(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
      throw new java.sql.SQLException();
   }
   public java.sql.Blob getBlob(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
      throw new java.sql.SQLException();
   }
   public java.sql.Clob getClob(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
      throw new java.sql.SQLException();
   }
   public java.sql.Ref getRef(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
      throw new java.sql.SQLException();
   }

   public boolean getBoolean(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if ((s!=null)&&(s.length()>0))
         switch (Character.toUpperCase(s.charAt(0))) {
            case 'T': case 'Y': case '1': return true;
         }
      return false;
   }
   public byte getByte(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return 0;
	  if(o instanceof Integer) return ((Integer)o).byteValue();
	  if(o instanceof Long) return ((Long)o).byteValue();
      try {
         return Byte.parseByte(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public short getShort(int columnIndex) throws java.sql.SQLException
   {
	  return (short)getLong(columnIndex);
   }
   public int getInt(int columnIndex) throws java.sql.SQLException
   {
	  return (int)getLong(columnIndex);
   }
   public long getLong(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return 0;
	  if(o instanceof Integer) return ((Integer)o).longValue();
	  if(o instanceof Long) return ((Long)o).longValue();
      try {
         return Long.parseLong(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public float getFloat(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return 0;
	  if(o instanceof Integer) return ((Integer)o).floatValue();
	  if(o instanceof Long) return ((Long)o).floatValue();
      try {
         return Float.parseFloat(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public double getDouble(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return 0;
	  if(o instanceof Integer) return ((Integer)o).doubleValue();
	  if(o instanceof Long) return ((Long)o).doubleValue();
      try {
         return Double.parseDouble(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.math.BigDecimal getBigDecimal(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return new java.math.BigDecimal(0);
      try {
         return new java.math.BigDecimal(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   /**
    * @deprecated
    */
   public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws java.sql.SQLException
   {
	  java.math.BigDecimal decimal = getBigDecimal(columnIndex);
	  decimal.setScale(scale);
	  return decimal;
   }
   public byte[] getBytes(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
      try {
         return o.toString().getBytes();
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Date getDate(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
	  if(o instanceof Integer) return new java.sql.Date(((Integer)o).longValue());
	  if(o instanceof Long) return new java.sql.Date(((Long)o).longValue());
      try {
         return java.sql.Date.valueOf(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Time getTime(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
	  if(o instanceof Integer) return new java.sql.Time(((Integer)o).longValue());
	  if(o instanceof Long) return new java.sql.Time(((Long)o).longValue());
      try {
         return java.sql.Time.valueOf(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Timestamp getTimestamp(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  if(o==null) return null;
	  if(o instanceof Integer) return new java.sql.Timestamp(((Integer)o).longValue());
	  if(o instanceof Long) return new java.sql.Timestamp(((Long)o).longValue());
      try {
         return java.sql.Timestamp.valueOf(o.toString());
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.io.InputStream getAsciiStream(int columnIndex) throws java.sql.SQLException
   {
      return getBinaryStream(columnIndex);
   }
   /**
    * @deprecated
    */
   public java.io.InputStream getUnicodeStream(int columnIndex) throws java.sql.SQLException
   {
      return getBinaryStream(columnIndex);
   }
   public java.io.InputStream getBinaryStream(int columnIndex) throws java.sql.SQLException
   {
      byte b[] = getBytes(columnIndex);
      if (b==null) return null;
      return new java.io.ByteArrayInputStream(b);
   }
   public java.io.Reader getCharacterStream(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if(s==null) return null;
      return new java.io.CharArrayReader(s.toCharArray());
   }
   public Object getObject(int columnIndex) throws java.sql.SQLException
   {
	  Object o = getProperValue(columnIndex);
	  return o;
   }
   public java.net.URL getURL(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if(s==null) return null;
      try {
         return new java.net.URL(s);
      } catch (java.net.MalformedURLException e) { throw new java.sql.SQLException(e.getMessage()); }
   }

   public int findColumn(String columnName) throws java.sql.SQLException
   {
	  if(!showColMap.containsKey(columnName))
		  return -1;
	  return showColMap.get(columnName).intValue() + 1;
   }

   public String getString(String columnName) throws java.sql.SQLException
      { return getString(findColumn(columnName)); }
   public java.sql.Array getArray(String columnName) throws java.sql.SQLException
      { return getArray(findColumn(columnName)); }
   public java.sql.Blob getBlob(String columnName) throws java.sql.SQLException
      { return getBlob(findColumn(columnName)); }
   public java.sql.Clob getClob(String columnName) throws java.sql.SQLException
      { return getClob(findColumn(columnName)); }
   public java.sql.Ref getRef(String columnName) throws java.sql.SQLException
      { return getRef(findColumn(columnName)); }
   public boolean getBoolean(String columnName) throws java.sql.SQLException
      { return getBoolean(findColumn(columnName)); }
   public byte getByte(String columnName) throws java.sql.SQLException
      { return getByte(findColumn(columnName)); }
   public short getShort(String columnName) throws java.sql.SQLException
      { return getShort(findColumn(columnName)); }
   public int getInt(String columnName) throws java.sql.SQLException
      { return getInt(findColumn(columnName)); }
   public long getLong(String columnName) throws java.sql.SQLException
      { return getLong(findColumn(columnName)); }
   public float getFloat(String columnName) throws java.sql.SQLException
      { return getFloat(findColumn(columnName)); }
   public double getDouble(String columnName) throws java.sql.SQLException
      { return getDouble(findColumn(columnName)); }
   public java.math.BigDecimal getBigDecimal(String columnName) throws java.sql.SQLException
      { return getBigDecimal(findColumn(columnName)); }
   /**
    * @deprecated
    */
   public java.math.BigDecimal getBigDecimal(String columnName, int scale) throws java.sql.SQLException
      { return getBigDecimal(findColumn(columnName), scale); }
   public byte[] getBytes(String columnName) throws java.sql.SQLException
      { return getBytes(findColumn(columnName)); }
   public java.sql.Date getDate(String columnName) throws java.sql.SQLException
      { return getDate(findColumn(columnName)); }
   public java.sql.Date getDate(int columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getDate(columnName); }
   public java.sql.Date getDate(String columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getDate(findColumn(columnName)); }
   public java.sql.Time getTime(String columnName) throws java.sql.SQLException
      { return getTime(findColumn(columnName)); }
   public java.sql.Time getTime(int columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTime(columnName); }
   public java.sql.Time getTime(String columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTime(findColumn(columnName)); }
   public java.sql.Timestamp getTimestamp(String columnName) throws java.sql.SQLException
      { return getTimestamp(findColumn(columnName)); }
   public java.sql.Timestamp getTimestamp(int columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTimestamp(columnName); }
   public java.sql.Timestamp getTimestamp(String columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTimestamp(findColumn(columnName)); }
   public java.io.Reader getCharacterStream(String columnName) throws java.sql.SQLException
      { return getCharacterStream(findColumn(columnName)); }
   public java.io.InputStream getAsciiStream(String columnName) throws java.sql.SQLException
      { return getAsciiStream(findColumn(columnName)); }
   /**
    * @deprecated
    */
   public java.io.InputStream getUnicodeStream(String columnName) throws java.sql.SQLException
      { return getUnicodeStream(findColumn(columnName)); }
   public java.io.InputStream getBinaryStream(String columnName) throws java.sql.SQLException
      { return getBinaryStream(findColumn(columnName)); }
   public java.net.URL getURL(String columnName) throws java.sql.SQLException
      { return getURL(findColumn(columnName)); }
   public Object getObject(String columnName) throws java.sql.SQLException
      { return getObject(findColumn(columnName)); }
   public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
      { return null; }
   public void clearWarnings() throws java.sql.SQLException
      { }
   public String getCursorName() throws java.sql.SQLException
      { throw new java.sql.SQLException("Positioned Update not supported.", "S1C00"); }
   public java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException
      { return null; }

   public void updateArray(int columnIndex,java.sql.Array x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateArray(String columnName,java.sql.Array x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateAsciiStream(int columnIndex,java.io.InputStream x,int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateAsciiStream(String columnName,java.io.InputStream x, int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBigDecimal(int columnIndex,java.math.BigDecimal x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBigDecimal(String columnName,java.math.BigDecimal x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBinaryStream(int columnIndex,java.io.InputStream x,int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBinaryStream(String columnName,java.io.InputStream x, int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBlob(int columnIndex,java.sql.Blob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBlob(String columnName,java.sql.Blob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBoolean(int columnIndex,boolean x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBoolean(String columnName,boolean x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateByte(int columnIndex,byte x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateByte(String columnName,byte x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBytes(int columnIndex,byte[] x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBytes(String columnName,byte[] x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateCharacterStream(int columnIndex,java.io.Reader x,int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateCharacterStream(String columnName,java.io.Reader reader, int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateClob(int columnIndex,java.sql.Clob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateClob(String columnName,java.sql.Clob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDate(int columnIndex,java.sql.Date x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDate(String columnName,java.sql.Date x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDouble(int columnIndex,double x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDouble(String columnName,double x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateFloat(int columnIndex,float x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateFloat(String columnName,float x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateInt(int columnIndex,int x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateInt(String columnName,int x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateLong(int columnIndex,long x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateLong(String columnName,long x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateNull(int columnIndex) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateNull(String columnName) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(int columnIndex,Object x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(int columnIndex,Object x,int scale) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(String columnName,Object x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(String columnName,Object x,int scale) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateRef(int columnIndex,java.sql.Ref x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateRef(String columnName,java.sql.Ref x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateShort(int columnIndex,short x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateShort(String columnName,short x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateString(int columnIndex,String x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateString(String columnName,String x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTime(int columnIndex,java.sql.Time x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTime(String columnName,java.sql.Time x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTimestamp(int columnIndex,java.sql.Timestamp x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTimestamp(String columnName,java.sql.Timestamp x) throws java.sql.SQLException { throw new java.sql.SQLException(); }

   public void deleteRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void moveToInsertRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void moveToCurrentRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void cancelRowUpdates() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void insertRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void refreshRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public int getRow()  { 	return currentRow;  }
   public boolean first() { return false; }
   public boolean previous() {  return false;  }
   public boolean isFirst() { return false; }
   private boolean afterLast=false;
   public boolean last() 
   { 
	   try{
		   while(next());
	   }
	   catch(java.sql.SQLException sqle){
		   sqle.printStackTrace();
	   }
	   afterLast=true;
	   return true;
   }
   public boolean isLast() { return false; }
   public void beforeFirst() throws java.sql.SQLException 
   { 
	   if(fakeTable==null)
		   throw new java.sql.SQLException(); 
      iter = fakeTable.indexIterator(this.orderByKeyDexCols,this.orderByConditions);
	  currentRow=0;
   }
   public boolean isBeforeFirst() { return (currentRow==0); }
   public void afterLast(){ last(); }
   public boolean isAfterLast(){return afterLast;}
   public boolean absolute(int i) { return true; }
   public boolean relative(int i) { return false; }
   public boolean rowDeleted() { return false; }
   public boolean rowInserted() { return false; }
   public boolean rowUpdated() { return false; }

   public int getConcurrency() { return 0; }
   public int getType() { return 0; }
   public void setFetchSize(int i) throws java.sql.SQLException { statement.setFetchSize(i); }
   public int getFetchSize() throws java.sql.SQLException { return statement.getFetchSize(); }
   public void setFetchDirection(int i) throws java.sql.SQLException { statement.setFetchDirection(i); }
   public int getFetchDirection() throws java.sql.SQLException { return statement.getFetchDirection(); }
   public int getResultSetConcurrency() throws java.sql.SQLException { return statement.getResultSetConcurrency(); }
   public int getResultSetType() throws java.sql.SQLException { return statement.getResultSetType(); }

    public int getHoldability() throws SQLException { return 0; }
    public Reader getNCharacterStream(int arg0) throws SQLException { return null; }
    public Reader getNCharacterStream(String arg0) throws SQLException { return null; }
    public NClob getNClob(int arg0) throws SQLException { return null; }
    public NClob getNClob(String arg0) throws SQLException { return null; }
    public String getNString(int arg0) throws SQLException { return null; }
    public String getNString(String arg0) throws SQLException { return null; }
    //public Object getObject(int arg0, Map arg1) throws SQLException { return getString(arg0); }
    public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException { return getString(arg0); }
    public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException { return getObject(findColumn(arg0),arg1); }
    //public Object getObject(String arg0, Map arg1) throws SQLException { return getObject(findColumn(arg0),arg1); }
    public RowId getRowId(int arg0) throws SQLException { return null; }
    public RowId getRowId(String arg0) throws SQLException { return null; }
    public SQLXML getSQLXML(int arg0) throws SQLException { return null; }
    public SQLXML getSQLXML(String arg0) throws SQLException { return null;}
    public boolean isClosed() throws SQLException { return false; }
    public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {}
    public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {}
    public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {}
    public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {}
    public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBlob(int arg0, InputStream arg1) throws SQLException {}
    public void updateBlob(String arg0, InputStream arg1) throws SQLException {}
    public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {}
    public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {}
    public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateClob(int arg0, Reader arg1) throws SQLException {}
    public void updateClob(String arg0, Reader arg1) throws SQLException {}
    public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {}
    public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {}
    public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNClob(int arg0, NClob arg1) throws SQLException {}
    public void updateNClob(String arg0, NClob arg1) throws SQLException {}
    public void updateNClob(int arg0, Reader arg1) throws SQLException {}
    public void updateNClob(String arg0, Reader arg1) throws SQLException {}
    public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNClob(String arg0, Reader arg1, long arg2)throws SQLException {}
    public void updateNString(int arg0, String arg1) throws SQLException {}
    public void updateNString(String arg0, String arg1) throws SQLException {}
    public void updateRowId(int arg0, RowId arg1) throws SQLException {}
    public void updateRowId(String arg0, RowId arg1) throws SQLException {}
    public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {}
    public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {}
    public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}
    public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
}
