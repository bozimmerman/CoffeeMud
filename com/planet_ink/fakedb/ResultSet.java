package com.planet_ink.fakedb;
/** Tiny (nearly) fake DB
  * (c) 2001 Thomas Neumann
  */

class ResultSet implements java.sql.ResultSet
{
   private Statement statement;
   private Backend.Relation relation;
   private java.util.Iterator iter;
   private int currentRow=0;
   private int conditionIndex;
   private String conditionValue;
   private final String[] values;
   private final boolean[] nullIndicators;
   private boolean nullFlag = false;

   ResultSet(Statement s,Backend.Relation r,int ci,String cv) {
      statement=s;
      relation=r;
      conditionIndex=ci;
      conditionValue=cv;
	  currentRow=0;
      values=new String[r.attributes.length];
      nullIndicators=new boolean[values.length];

      if ((ci<0)&&(cv!=null)) {
         iter=r.index.keySet().iterator();
      } else {
         iter=r.index.values().iterator();
      }
   }

   public java.sql.Statement getStatement() throws java.sql.SQLException { return statement; }

   public boolean next() throws java.sql.SQLException
   {
      while (true) {
         if (!iter.hasNext()) return false;
         if ((conditionIndex<0)&&(conditionValue!=null)) {
            String key=(String)iter.next();
            if(!key.startsWith(conditionValue+"\n")) continue;
			currentRow++;
	        return relation.getRecord(nullIndicators,values,(Backend.RecordInfo)relation.index.get(key));
         } else {
            if (!relation.getRecord(nullIndicators,values,(Backend.RecordInfo)iter.next())) 
				return false;
            if (conditionIndex>=0) {
               if (nullIndicators[conditionIndex]) continue;
               if (!conditionValue.equals(values[conditionIndex])) continue;
            }
			currentRow++;
            return true;
         }
      }
   }
   public void close() throws java.sql.SQLException
   {
   }
   public boolean wasNull() throws java.sql.SQLException
   {
       return nullFlag;
   }
   public String getString(int columnIndex) throws java.sql.SQLException
   {
      if ((columnIndex<0)||(columnIndex>=nullIndicators.length)||(nullIndicators[columnIndex])) {
         nullFlag=true;
         return null;
      } else {
         nullFlag=false;
         return values[columnIndex];
      }
   }
   public java.sql.Array getArray(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException(); // XXX
   }
   public java.sql.Blob getBlob(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException(); // XXX
   }
   public java.sql.Clob getClob(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException(); // XXX
   }
   public java.sql.Ref getRef(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException(); // XXX
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
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Byte.parseByte(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public short getShort(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Short.parseShort(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public int getInt(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public long getLong(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Long.parseLong(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public float getFloat(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Float.parseFloat(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public double getDouble(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Double.parseDouble(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.math.BigDecimal getBigDecimal(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return new java.math.BigDecimal(0);
      try {
         return new java.math.BigDecimal(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   /**
    * @deprecated
    */
   public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) { java.math.BigDecimal v=new java.math.BigDecimal(0); v.setScale(scale); return v; }
      try {
         java.math.BigDecimal v=new java.math.BigDecimal(s); v.setScale(scale); return v;
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public byte[] getBytes(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return s.getBytes();
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Date getDate(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return java.sql.Date.valueOf(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Time getTime(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return java.sql.Time.valueOf(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Timestamp getTimestamp(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return java.sql.Timestamp.valueOf(s);
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
      if (nullFlag) return null;
      return new java.io.ByteArrayInputStream(b);
   }
   public java.io.Reader getCharacterStream(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      return new java.io.CharArrayReader(s.toCharArray());
   }
   public Object getObject(int columnIndex) throws java.sql.SQLException
   {
      return getString(columnIndex); // XXX
   }
   public Object getObject(int columnIndex,java.util.Map m) throws java.sql.SQLException
   {
      return getString(columnIndex); // XXX
   }
   public java.net.URL getURL(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return new java.net.URL(s);
      } catch (java.net.MalformedURLException e) { throw new java.sql.SQLException(e.getMessage()); }
   }

   public int findColumn(String columnName) throws java.sql.SQLException
   {
      return relation.findAttribute(columnName);
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
   public Object getObject(String columnName,java.util.Map m) throws java.sql.SQLException
      { return getObject(findColumn(columnName),m); }

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
   public boolean last() 
   { 
	   try{
		   while(next());
	   }
	   catch(java.sql.SQLException sqle){}
	   return true;
   }
   public boolean isLast() { return false; }
   public void beforeFirst() throws java.sql.SQLException 
   { 
	   if(relation==null)
		   throw new java.sql.SQLException(); 
      if ((conditionIndex<0)&&(conditionValue!=null)) {
         iter=relation.index.keySet().iterator();
      } else {
         iter=relation.index.values().iterator();
      }
	  currentRow=0;
   }
   public boolean isBeforeFirst() { return false; }
   public void afterLast() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public boolean isAfterLast() { return false; }
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
}