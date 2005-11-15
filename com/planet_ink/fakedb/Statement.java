package com.planet_ink.fakedb;
/* 
   Copyright 2001 Thomas Neumann

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
public class Statement implements java.sql.Statement
{
   static private void log(String x) {
      System.err.println("Statement: "+x);
   }

   private Connection connection;

   Statement(Connection c) { connection=c; }

   public java.sql.Connection getConnection() { return connection; }

   private String split(String sql,String[] token)
   {
      while (true) {
         if (sql.length()==0) {
            token[0]=""; return "";
         }
         if (sql.charAt(0)==' ') {
            sql=sql.substring(1);
            continue;
         }
         int index;
         for (index=0;index<sql.length();index++) {
            char c=sql.charAt(index);
            if (c==' ') {
               break;
            } else if (c=='\'') {
               for (++index;index<sql.length();index++) {
                  c=sql.charAt(index);
                  if (c=='\\') index++; else
                  if (c=='\'') break;
               }
            }
         }
         if (index>=sql.length()) {
            token[0]=sql;
            return "";
         }
         token[0]=sql.substring(0,index);
         return sql.substring(index+1);
      }
   }
   public java.sql.ResultSet executeQuery(String sql) throws java.sql.SQLException
   {

      try {
         String[] token=new String[1];
         sql=split(sql,token);
         if (!token[0].equalsIgnoreCase("select")) throw new java.sql.SQLException("first query token not select");
         sql=split(sql,token);
         if (!token[0].equalsIgnoreCase("*")) throw new java.sql.SQLException("second token not *");
         sql=split(sql,token);
         if (!token[0].equalsIgnoreCase("from")) throw new java.sql.SQLException("third token not from");
         sql=split(sql,token);

         String relationName=token[0];
         String conditionVar=null,conditionValue=null,orderVar=null,comparitor=null;

         if (sql.length()>0) {
            sql=split(sql,token);
            if (token[0].equalsIgnoreCase("where")) {
               sql=split(sql,token);
               int e=token[0].indexOf(">=");
               if(e<0)e=token[0].indexOf("<=");
               if(e<0)e=token[0].indexOf("<>");
               if(e<0)e=token[0].indexOf("=");
               if(e<0)e=token[0].indexOf("<");
               if(e<0)e=token[0].indexOf(">");
               if (e<0) throw new java.sql.SQLException("no comparitor");
               int len=1;
               if((e<token[0].length()-1)
               &&((token[0].charAt(e+1)=='=')||(token[0].charAt(e+1)=='>')))
                   len=2;
               comparitor=token[0].substring(e,e+len);
               conditionVar=token[0].substring(0,e);
               conditionValue=token[0].substring(e+len);
               if ((conditionValue.length()>0)&&(conditionValue.charAt(0)=='\''))
                  conditionValue=conditionValue.substring(1,conditionValue.length()-1);

               if (sql.length()>0)
                  sql=split(sql,token); else
                  token[0]=null;
            }
            if ((token[0]!=null)&&(token[0].equalsIgnoreCase("order"))) {
               sql=split(sql,token);
               if (!token[0].equalsIgnoreCase("by")) throw new java.sql.SQLException("no by token");
               sql=split(sql,token);
               orderVar=token[0];
            }
            if (sql.length()>0) throw new java.sql.SQLException("extra garbage");
         }

         return connection.getBackend().constructScan(this,relationName,conditionVar,conditionValue,orderVar,comparitor);
      } catch (java.sql.SQLException e) {
         log("unsupported SQL in executeQuery: "+sql);
         throw e;
      }
   }
   private static String skipWS(String sql)
   {
      int index;
      for (index=0;index<sql.length();index++) {
         char c=sql.charAt(index);
         if ((c!=' ')&&(c!='\t')&&(c!='\r')&&(c!='\n')) break;
      }
      if (index==0) return sql; 
      return sql.substring(index);
   }
   private static String[] parseVal(String sql)
   {
      String[] result=new String[2];
      sql=skipWS(sql);
      if (sql.length()==0) {
         result[0]=result[1]="";
      } else if (sql.charAt(0)=='\'') {
         StringBuffer buffer=new StringBuffer();
         int index;
         for (index=1;index<sql.length();++index) {
            char c=sql.charAt(index);
            if (c=='\'') break;
            if (c=='\\') c=sql.charAt(++index);
            buffer.append(c);
         }
         if (index>=sql.length()) index=sql.length()-1;
         result[0]=sql.substring(index+1);
         result[1]=buffer.toString();
      } else {
         StringBuffer buffer=new StringBuffer();
         int index;
         for (index=0;index<sql.length();++index) {
            char c=sql.charAt(index);
            if ((c==' ')||(c==',')||(c==')')) break;
            buffer.append(c);
         }
         result[0]=sql.substring(index);
         result[1]=buffer.toString();
      }
      return result;
   }
   public int executeUpdate(String sql) throws java.sql.SQLException
   {
      //log("executeUpdate"+sql);

      // insert into x (a,b,c) values (a,b,c)
      // update x set a=A,b=B where x=y
      // delete from x where x=y

      String originalSql=sql;
      try {
         String[] token=new String[1];
         sql=split(sql,token);
         if (token[0].equalsIgnoreCase("insert")) {
            sql=split(sql,token);
            if (!token[0].equalsIgnoreCase("into")) throw new java.sql.SQLException("no into token");
            sql=split(sql,token);
            String relationName=token[0];
            sql=skipWS(sql);
            if ((sql.length()<0)||(sql.charAt(0)!='(')) throw new java.sql.SQLException("no open paren");
            sql=sql.substring(1);

            java.util.List attributes=new java.util.LinkedList();
            while (true) {
               sql=skipWS(sql);
               int index=sql.indexOf(',');
               int index2=sql.indexOf(')');
               if ((index<0)||(index2<index)) index=index2;
               if (index<0) throw new java.sql.SQLException("no comma");
               attributes.add(sql.substring(0,index).trim());
               char c=sql.charAt(index);
               sql=skipWS(sql.substring(index+1));
               if (c==')') break;
            }

            sql=split(sql,token);
            if (!token[0].equalsIgnoreCase("values")) throw new java.sql.SQLException("no values");
            sql=skipWS(sql);
            if ((sql.length()<0)||(sql.charAt(0)!='(')) throw new java.sql.SQLException("no value open paren");
            sql=sql.substring(1);

            java.util.List values=new java.util.LinkedList();
            while (true) {
               sql=skipWS(sql);
               String[] r=parseVal(sql);
               String val=r[1];
               sql=skipWS(r[0]);
               values.add(val);
               if (sql.length()==0) throw new java.sql.SQLException("no sql again");
               char c=sql.charAt(0);
               sql=skipWS(sql.substring(1));
               if (c==')') break;
               if (c!=',') throw new java.sql.SQLException("no comma before last paren");
            }
            if ((sql.length()>0)&&(sql.charAt(0)==';')) sql=skipWS(sql.substring(1));
            if ((sql.length()>0)||(attributes.size()!=values.size())) {
               //Syste/m.out.prin/tln(sql);
               //Syste/m.out.prin/tln(attributes.size());
               //for (java.util.Iterator iter=attributes.iterator();iter.hasNext();)
               //   Syste/m.out.prin/tln((String)iter.next());
               //Syste/m.out.prin/tln(values.size());
               //for (java.util.Iterator iter=values.iterator();iter.hasNext();)
               //   Syste/m.out.prin/tln((String)iter.next());
               throw new java.sql.SQLException("something very bad");
            }
            connection.getBackend().insertValues(relationName,(String[])attributes.toArray(new String[0]),(String[])values.toArray(new String[0]));
         } else if (token[0].equalsIgnoreCase("update")) {
            sql=split(sql,token);
            String relationName=token[0];
            sql=split(sql,token);
            if (!token[0].equalsIgnoreCase("set")) throw new java.sql.SQLException("no set");

            java.util.List attributes=new java.util.LinkedList();
            java.util.List values=new java.util.LinkedList();
            StringBuffer buffer=new StringBuffer();
            while (true) {
               sql=skipWS(sql);
               buffer.setLength(0);
               while (sql.length()>0) {
                  char c=sql.charAt(0);
                  if ((c=='=')||(c==' ')) break;
                  buffer.append(c);
                  sql=sql.substring(1);
               }
               sql=skipWS(sql);
               String attr=buffer.toString();
               if (sql.length()==0) throw new java.sql.SQLException("no more sql");
               if (sql.charAt(0)!='=') {
                  if (!attr.equalsIgnoreCase("where")) throw new java.sql.SQLException("no where");
                  break;
               }
               sql=skipWS(sql.substring(1));
               if (sql.length()==0) throw new java.sql.SQLException("no no sql no mo");
               buffer.setLength(0);
               if (sql.charAt(0)=='\'') {
                  int sub=1;
                  for (;sub<sql.length();sub++) {
                     char c=sql.charAt(sub);
                     if (c=='\'') break;
                     if (c=='\\') c=sql.charAt(++sub);
                     buffer.append(c);
                  }
                  attributes.add(attr);
                  values.add(buffer.toString());
                  sql=sql.substring(sub+1);
               } else {
                  String[] r=parseVal(sql);
                  sql=r[0];
                  attributes.add(attr);
                  values.add(r[1]);
               }
               sql=skipWS(sql);
               if ((sql.length()>0)&&(sql.charAt(0)==',')) sql=skipWS(sql.substring(1));
            }

            int split=sql.indexOf('=');
            if (split<0) throw new java.sql.SQLException("no equal sign again");
            String conditionVar=sql.substring(0,split);
            String conditionValue=sql.substring(split+1);
            if ((conditionValue.length()>0)&&(conditionValue.charAt(0)=='\''))
               conditionValue=conditionValue.substring(1,conditionValue.length()-1);
            connection.getBackend().updateRecord(relationName,conditionVar,conditionValue,(String[])attributes.toArray(new String[0]),(String[])values.toArray(new String[0]));
         } else if (token[0].equalsIgnoreCase("delete")) {
            sql=split(sql,token);
            if (!token[0].equalsIgnoreCase("from")) throw new java.sql.SQLException("no from clause");
            sql=split(sql,token);
            String relationName=token[0];
            sql=split(sql,token);
            if (!token[0].equalsIgnoreCase("where")) throw new java.sql.SQLException("no other where clause");
            sql=skipWS(sql);
            int split=sql.indexOf('=');
            if (split<0) throw new java.sql.SQLException("no another equal sign");
            String conditionVar=sql.substring(0,split);
            String conditionValue=sql.substring(split+1);
            if ((conditionValue.length()>0)&&(conditionValue.charAt(0)=='\''))
               conditionValue=conditionValue.substring(1,conditionValue.length()-1);
            connection.getBackend().deleteRecord(relationName,conditionVar,conditionValue);
         } else throw new java.sql.SQLException("no delete");
         return 1;
      } catch (java.sql.SQLException e) {
         e.printStackTrace();
         log("unsupported SQL in executeUpdate: "+originalSql);
         throw e;
      }
   }
   public int executeUpdate(String sql,int a) throws java.sql.SQLException
   { return executeUpdate(sql); }
   public int executeUpdate(String sql,int[] a) throws java.sql.SQLException
   { return executeUpdate(sql); }
   public int executeUpdate(String sql,String[] a) throws java.sql.SQLException
   { return executeUpdate(sql); }

   public void close() throws java.sql.SQLException
   {}
   public int getMaxFieldSize() throws java.sql.SQLException
   {
      return 0; 
   }
   public void setMaxFieldSize(int max) throws java.sql.SQLException
   {
   }
   public int getMaxRows() throws java.sql.SQLException
   {
      return 0;
   }
   public void setMaxRows(int max) throws java.sql.SQLException
   {
   }
   public void setEscapeProcessing(boolean enable) throws java.sql.SQLException
   {
   }
   public int getQueryTimeout() throws java.sql.SQLException
   {
      return 60;
   }
   public void setQueryTimeout(int seconds) throws java.sql.SQLException
   {
   }
   public void cancel() throws java.sql.SQLException
   {
   }
   public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
   {
      return null;
   }
   public void clearWarnings() throws java.sql.SQLException
   {
   }
   public void setCursorName(String Name) throws java.sql.SQLException
   {
   }
   public boolean execute(String sql) throws java.sql.SQLException
   {
      log("execute "+sql);
      return false;
   }
   public boolean execute(String sql,int a) throws java.sql.SQLException
   { return execute(sql); }
   public boolean execute(String sql,int[] a) throws java.sql.SQLException
   { return execute(sql); }
   public boolean execute(String sql,String[] a) throws java.sql.SQLException
   { return execute(sql); }

   public java.sql.ResultSet getResultSet() throws java.sql.SQLException
   {
      log("getResultSet");
      return null;
   }
   public int getUpdateCount() throws java.sql.SQLException
   {
      log("getUpdateCount");
      return -1;
   }
   public long getLongUpdateCount()
   {
      log("getLongUpdateCount");
      return -1;
   }
   public boolean getMoreResults() throws java.sql.SQLException
   {
      return false;
   }
   public boolean getMoreResults(int a) throws java.sql.SQLException
   { return false; }

   public int getResultSetHoldability() throws java.sql.SQLException
   { return 0; }
   public void setFetchDirection(int i) throws java.sql.SQLException {}
   public int getFetchDirection() throws java.sql.SQLException { return 0; }

   public void addBatch(String a) throws java.sql.SQLException {}
   public void clearBatch() throws java.sql.SQLException {}
   public int[] executeBatch() throws java.sql.SQLException { return null; }

   public void setFetchSize(int i) throws java.sql.SQLException {}
   public int getFetchSize() throws java.sql.SQLException { return 0; }

   public int getResultSetConcurrency() throws java.sql.SQLException { return 0; }
   public int getResultSetType() throws java.sql.SQLException { return 0; }
   public java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException { return null; }
}
