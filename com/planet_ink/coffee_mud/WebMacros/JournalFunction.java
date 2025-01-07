package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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
public class JournalFunction extends StdWebMacro
{
	@Override
	public String name()
	{
		return "JournalFunction";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final java.util.Map<String,String> parms=parseParms(parm);
		final String journalName=httpReq.getUrlParameter("JOURNAL");
		if(journalName==null)
			return "Function not performed -- no Journal specified.";

		final Clan setClan=CMLib.clans().getClan(httpReq.getUrlParameter("CLAN"));
		final JournalsLibrary.ForumJournal forum=CMLib.journals().getForumJournal(journalName,setClan);
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(CMLib.journals().isArchonJournalName(journalName))
		{
			if((M==null)||(!CMSecurity.isASysOp(M)))
				return " @break@";
		}
		if(parms.containsKey("DESTROYFOREVER"))
		{
			if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
				return "Destruction cancelled -- You are not authorized to delete this forum.";
			if(!CMSecurity.isAllowedEverywhere(M,CMSecurity.SecFlag.JOURNALS))
				return "Destruction cancelled -- You are not authorized.";
			CMLib.database().DBDeleteJournal(journalName, null);
			return "Journal "+journalName+" deleted.";
		}
		if(parms.containsKey("SUBSCRIBE"))
		{
			if(forum==null)
				return "Subscription cancelled -- no forum.";
			if(CMLib.journals().subscribeToJournal(journalName, M.Name(), true))
			{
				return "Now subscribed to "+journalName+".";
			}
			return "New subscribtion to "+journalName+" failed.";
		}
		if(parms.containsKey("UNSUBSCRIBE"))
		{
			if(forum==null)
				return "UnSubscription cancelled -- no forum.";
			if(CMLib.journals().unsubscribeFromJournal(journalName, M.Name(), true))
			{
				return "Now unsubscribed from "+journalName+".";
			}
			return "Unsubscription from "+journalName+" failed -- were you ever subscribed?";
		}
		String from="Anonymous";
		if(M!=null)
			from=M.Name();
		if(parms.containsKey("NEWPOST"))
		{
			if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.POST)))
				return "Post not submitted -- Unauthorized.";
			String to=httpReq.getUrlParameter("TO");
			if((to==null)||(M==null)||(to.equalsIgnoreCase("all")))
				to="ALL";
			if((!to.equals("ALL"))&&(!to.toUpperCase().trim().startsWith("MASK=")))
			{
				if(!CMLib.players().playerExists(to) && (!CMLib.players().accountExists(to)))
					return "Post not submitted -- TO user does not exist.  Try 'All'.";
				to=CMStrings.capitalizeAndLower(to);
			}
			else
			if(journalName.equalsIgnoreCase(CMProps.getVar(CMProps.Str.MAILBOX))
			&&(!CMSecurity.isAllowedEverywhere(M,CMSecurity.SecFlag.JOURNALS)))
				return "Post not submitted -- You are not authorized to send email to ALL.";
			String subject=httpReq.getUrlParameter("SUBJECT");
			if(subject==null)
				subject="";
			final String parent=httpReq.getUrlParameter("PARENT");
			if((subject.length()==0)&&(parent==null))
				return "Post not submitted -- No subject!";
			if((parent!=null)&&(parent.length()>0)&&(subject.length()==0))
			{
				JournalEntry parentEntry = null;
				parentEntry=CMLib.database().DBReadJournalEntry(journalName, parent);
				if(parentEntry!=null)
					subject="RE: "+parentEntry.subj();
			}
			final String date=httpReq.getUrlParameter("DATE");
			final String icon=httpReq.getUrlParameter("MSGICON");
			final List<String> flags=CMParms.parseCommas(httpReq.getUrlParameter("FLAGS"), true);
			if((flags.size()>0)&&(forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
				return "Post not submitted -- Unauthorized flags.";
			final String text=httpReq.getUrlParameter("NEWTEXT");
			if((text==null)||(text.length()==0))
				return "Post not submitted -- No text!";
			if(journalName.equalsIgnoreCase(CMProps.getVar(CMProps.Str.MAILBOX))
			&&(CMProps.getIntVar(CMProps.Int.MAXMAILBOX)>0)
			&&(!to.equalsIgnoreCase("ALL")))
			{
				final int count=CMLib.database().DBCountJournal(journalName,null,to);
				if(count>=CMProps.getIntVar(CMProps.Int.MAXMAILBOX))
					return "Post not submitted -- Mailbox is full!";
			}
			final JournalEntry msg = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
			msg.from(from);
			msg.subj(clearWebMacros(subject));
			msg.msg(clearWebMacros(CMStrings.fixMudCRLF(text)));
			if((date!=null) && (CMath.isLong(date)))
				msg.dateStr(""+CMath.s_long(date));
			else
				msg.dateStr(""+System.currentTimeMillis());
			msg.update(System.currentTimeMillis());
			msg.parent((parent==null)?"":parent);
			msg.msgIcon((icon==null)?"":icon);
			if(flags.contains("STUCKY"))
				msg.attributes(msg.attributes()|JournalEntry.JournalAttrib.STUCKY.bit);
			if(flags.contains("PROTECTED"))
				msg.attributes(msg.attributes()|JournalEntry.JournalAttrib.PROTECTED.bit);
			final List<Pair<String,byte[]>> attachmentsV = new ArrayList<Pair<String,byte[]>>();
			if((httpReq.getMultiParts().size()>0)
			&&((forum==null)||(forum.authorizationCheck(M, ForumJournalFlags.ATTACH))))
			{
				String file="";
				byte[] buf=null;
				int maxFiles = 0;
				for(final MultiPartData data : httpReq.getMultiParts())
				{
					if(data.getVariables().containsKey("filename"))
					{
						file=data.getVariables().get("filename");
						if(file==null)
							file="";
						buf=data.getData();
					}
					if(file.length()==0)
						continue;
					if(buf == null)
						return "File not uploaded -- no buffer!";
					if((forum!=null)&&(forum.maxAttach()>0)&&(++maxFiles > forum.maxAttach()))
						return "File not uploaded -- maximum "+forum.maxAttach()+" attachments!";
					attachmentsV.add(new Pair<String,byte[]>(file,buf));
				}
				if(attachmentsV.size()>0)
					msg.attributes(msg.attributes()|JournalEntry.JournalAttrib.ATTACHMENT.bit);
			}
			msg.data("");
			msg.to(to);
			// check for dups
			final List<JournalEntry> chckEntries = CMLib.database().DBReadJournalMsgsNewerThan(journalName, to, msg.date()-1);
			for(final JournalEntry entry : chckEntries)
			{
				if((entry.date() == msg.date())
				&&(entry.from().equals(msg.from()))
				&&(entry.subj().equals(msg.subj()))
				&&(entry.parent().equals(msg.parent())))
					return "";
			}
			CMLib.journals().notifyPosting(journalName, msg.from(), msg.to(), msg.subj());
			final String newKey = CMLib.database().DBWriteJournal(journalName,msg);
			if(newKey == null)
				return "Post failed";
			JournalInfo.clearJournalCache(httpReq, journalName);
			if(parent!=null)
				CMLib.database().DBTouchJournalMessage(parent);
			CMLib.journals().clearJournalSummaryStats(forum);
			if(attachmentsV.size()>0)
			{
				for(final Pair<String,byte[]> p : attachmentsV)
				{
					final String fileName = newKey+"/"+parent+"/"+p.first;
					if(fileName.length()>252)
						return "Post submitted.  Some attachments failed.";
					CMLib.database().DBCreateVFSFile(fileName, CMFile.VFS_MASK_ATTACHMENT, from, System.currentTimeMillis(), p.second);
				}
			}
			return "Post submitted.";
		}
		else
		if(parms.containsKey("ADMINSUBMIT"))
		{
			if(forum==null)
				return "Changes not submitted -- Unsupported.";
			else
			if(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN))
				return "Changes not submitted -- Unauthorized.";
			final String longDesc=fixForumString(httpReq.getUrlParameter("LONGDESC"));
			final String shortDesc=fixForumString(httpReq.getUrlParameter("SHORTDESC"));
			final String imgPath=httpReq.getUrlParameter("IMGPATH");
			final JournalsLibrary.JournalMetaData metaData = CMLib.journals().getJournalStats(forum);
			if(metaData == null)
				return "Changes not submitted -- No Stats!";
			if(longDesc!=null)
				metaData.longIntro(clearWebMacros(longDesc));
			if(shortDesc!=null)
				metaData.shortIntro(clearWebMacros(shortDesc));
			if(imgPath!=null)
				metaData.imagePath(clearWebMacros(imgPath));
			CMLib.database().DBUpdateJournalMetaData(journalName, metaData);
			CMLib.journals().clearJournalSummaryStats(forum);
			return "Changed applied.";
		}
		String parent=httpReq.getUrlParameter("JOURNALPARENT");
		if(parent==null)
			parent="";
		String dbsearch=httpReq.getUrlParameter("DBSEARCH");
		if(dbsearch==null)
			dbsearch="";
		String page=httpReq.getUrlParameter("JOURNALPAGE");
		if(page==null)
			page=parms.get("JOURNALPAGE");
		String mpage=httpReq.getUrlParameter("MESSAGEPAGE");
		if(mpage==null)
			mpage=parms.get("MESSAGEPAGE");
		int pageLimit;
		if(httpReq.isUrlParameter("JOURNALPAGELIMIT"))
			pageLimit = CMath.s_int(httpReq.getUrlParameter("JOURNALPAGELIMIT"));
		else
			pageLimit = CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
		if(pageLimit <= 0)
			pageLimit=Integer.MAX_VALUE;
		final List<JournalEntry> msgs=JournalInfo.getMessages(journalName,forum,page,mpage,parent,dbsearch,pageLimit, httpReq.getRequestObjects());
		String msgKey=httpReq.getUrlParameter("JOURNALMESSAGE");
		int cardinalNumber = CMath.s_int(httpReq.getUrlParameter("JOURNALCARDINAL"));
		String srch=httpReq.getUrlParameter("JOURNALMESSAGESEARCH");
		if(srch!=null)
			srch=srch.toLowerCase();
		final boolean doThemAll=parms.containsKey("EVERYTHING");
		if(doThemAll)
		{
			final JournalEntry entry = JournalInfo.getNextEntry(msgs, null);
			if(entry==null)
				msgKey="";
			else
				msgKey=entry.key();
			cardinalNumber=1;
		}
		final StringBuffer messages=new StringBuffer("");
		boolean keepProcessing=((msgKey!=null)&&(msgKey.length()>0));
		String fieldSuffix="";
		while(keepProcessing)
		{
			if(doThemAll)
			{
				parms.clear();
				parms.put("EVERYTHING","EVERYTHING");
				final String fate=httpReq.getUrlParameter("FATE"+msgKey);
				final String replyemail=httpReq.getUrlParameter("REPLYEMAIL"+msgKey);
				cardinalNumber = CMath.s_int(httpReq.getUrlParameter("CARDINAL"+msgKey));
				if((fate!=null)&&(fate.length()>0)&&(CMStrings.isUpperCase(fate)))
					parms.put(fate,fate);
				if((replyemail!=null)&&(replyemail.length()>0)&&(CMStrings.isUpperCase(replyemail)))
					parms.put(replyemail,replyemail);
				if(parms.size()==1)
				{
					// means there was no action found
					JournalEntry entry = JournalInfo.getNextEntry(msgs, msgKey);
					while((entry!=null) && (!CMLib.journals().canReadMessage(entry,srch,M,parms.containsKey("NOPRIV"))))
						entry = JournalInfo.getNextEntry(msgs, entry.key());

					if(entry==null)
						keepProcessing=false;
					else
						msgKey=entry.key();
					continue;
				}
				fieldSuffix=msgKey;
			}
			else
				keepProcessing=false;
			JournalEntry entry = JournalInfo.getEntry(msgs, msgKey);
			if(entry==null)
				entry=CMLib.database().DBReadJournalEntry(journalName, msgKey);
			if(entry == null)
				return "Function not performed -- illegal journal message specified.<BR>";
			if(!doThemAll)
				entry.cardinal(cardinalNumber);
			final String to=entry.to();
			final String author=entry.from();
			if((M!=null)
			&&(CMSecurity.isAllowedAnywhere(M,CMSecurity.SecFlag.JOURNALS)
				||(to.equalsIgnoreCase(M.Name()))
				||(author.equalsIgnoreCase(M.Name())))
			&&((forum==null)||(forum.authorizationCheck(M, ForumJournalFlags.READ))))
			{
				if(parms.containsKey("REPLY"))
				{
					if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.REPLY)))
						return "Reply not submitted -- Unauthorized.";
					final String text=httpReq.getUrlParameter("NEWTEXT"+fieldSuffix);
					if((text==null)||(text.length()==0))
						messages.append("Reply to #"+cardinalNumber+" not submitted -- No text!<BR>");
					else
					{
						CMLib.journals().notifyReplying(journalName, entry.from(), from, entry.subj());
						CMLib.database().DBWriteJournalReply(journalName,entry.key(),from,"","",clearWebMacros(text));
						CMLib.journals().clearJournalSummaryStats(forum);
						JournalInfo.clearJournalCache(httpReq, journalName);
						messages.append("Reply to #"+cardinalNumber+" submitted<BR>");
					}
				}
				else
				if(parms.containsKey("EMAIL"))
				{
					if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.REPLY)))
						return "Email not submitted -- Unauthorized.";
					final String replyMsg=httpReq.getUrlParameter("NEWTEXT"+fieldSuffix);
					if(replyMsg.length()==0)
						messages.append("Email to #"+cardinalNumber+" not submitted -- No text!<BR>");
					else
					{
						final String toName=entry.from();
						final PlayerLibrary.ThinPlayer toTP = CMLib.players().getThinPlayer(toName);
						if(toTP==null)
							messages.append("Player '"+toName+"' does not exist.<BR>");
						else
						{
							if(toTP.email().indexOf('@')<0)
								messages.append("Warning: Player '"+toName+"' has no email address..<BR>");
							CMLib.smtp().emailOrJournal(M.Name(),
														M.Name(),
														toTP.name(),
														"RE: "+entry.subj(),
														clearWebMacros(replyMsg));
							JournalInfo.clearJournalCache(httpReq, journalName);
							messages.append("Email to #"+cardinalNumber+" queued<BR>");
						}
					}
				}
				if(parms.containsKey("DELETE")||parms.containsKey("DELETEREPLY"))
				{
					if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
						return "Delete not authorized.";
					if(parms.containsKey("DELETEREPLY")
					&&(entry.parent()!=null)
					&&(entry.parent().length()>0))
					{
						CMLib.database().DBDeleteJournal(journalName,entry.key());
						// this constitutes a threaded reply -- update the counter
						final JournalEntry parentEntry=CMLib.database().DBReadJournalEntry(journalName, entry.parent());
						if(parentEntry!=null)
							CMLib.database().DBUpdateMessageReplies(parentEntry.key(),parentEntry.replies()-1);
						JournalInfo.clearJournalCache(httpReq, journalName);
						httpReq.addFakeUrlParameter("JOURNALMESSAGE",entry.parent());
						httpReq.addFakeUrlParameter("JOURNALPARENT","");
						if(cardinalNumber==0)
							cardinalNumber=entry.cardinal();
						if(cardinalNumber==0)
							messages.append("Reply deleted.<BR>");
						else
							messages.append("Reply #"+cardinalNumber+" deleted.<BR>");
					}
					else
					{
						JournalEntry nextEntry = null;
						if(doThemAll && keepProcessing)
						{
							cardinalNumber++;
							nextEntry = JournalInfo.getNextEntry(msgs, msgKey);
							while((nextEntry!=null) && (!CMLib.journals().canReadMessage(entry,srch,M,parms.containsKey("NOPRIV"))))
								nextEntry = JournalInfo.getNextEntry(msgs, nextEntry.key());
							cardinalNumber--;
						}
						CMLib.database().DBDeleteJournal(journalName,entry.key());
						if(cardinalNumber==0)
							cardinalNumber=entry.cardinal();
						if(cardinalNumber==0)
							messages.append("Message deleted.<BR>");
						else
							messages.append("Message #"+cardinalNumber+" deleted.<BR>");
						JournalInfo.clearJournalCache(httpReq, journalName);
						httpReq.addFakeUrlParameter("JOURNALMESSAGE","");
						if(nextEntry != null)
						{
							cardinalNumber++;
							msgKey=nextEntry.key();
							CMLib.journals().clearJournalSummaryStats(forum);
							continue;
						}
					}
					CMLib.journals().clearJournalSummaryStats(forum);
				}
				else
				if(parms.containsKey("EDIT"))
				{
					if((entry.to().equals(M.Name()))
					||((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
					||CMSecurity.isAllowedAnywhere(M,CMSecurity.SecFlag.JOURNALS))
					{
						String subj=entry.subj();
						if(httpReq.isUrlParameter("NEWSUBJ"+fieldSuffix))
						{
							final String s=httpReq.getUrlParameter("NEWSUBJ"+fieldSuffix);
							if((s!=null)&&(s.length()>0))
								subj=clearWebMacros(s);
						}
						final String text=httpReq.getUrlParameter("NEWTEXT"+fieldSuffix);
						if((text==null)||(text.length()==0))
							messages.append("Edit to #"+cardinalNumber+" not submitted -- No text!<BR>");
						else
						{
							long attributes=0;
							if((forum!=null)&&(forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
							{
								attributes = entry.attributes() | JournalEntry.JournalAttrib.ATTACHMENT.bit;
								String ISSTUCKY=httpReq.getUrlParameter("ISSTICKY"+fieldSuffix);
								if(ISSTUCKY==null)
									ISSTUCKY=httpReq.getUrlParameter("ISSTUCKY"+fieldSuffix);
								if((ISSTUCKY!=null)&&(ISSTUCKY.equalsIgnoreCase("on")))
									attributes|=JournalEntry.JournalAttrib.STUCKY.bit;
								final String ISPROTECTED=httpReq.getUrlParameter("ISPROTECTED"+fieldSuffix);
								if((ISPROTECTED!=null)&&(ISPROTECTED.equalsIgnoreCase("on")))
									attributes|=JournalEntry.JournalAttrib.PROTECTED.bit;
							}
							if((httpReq.getMultiParts().size()>0)
							&&((forum==null)||(forum.authorizationCheck(M, ForumJournalFlags.ATTACH))))
							{
								String file="";
								byte[] buf=null;
								int maxFiles = entry.attachmentKeys().size();
								for(final MultiPartData data : httpReq.getMultiParts())
								{
									if(data.getVariables().containsKey("filename"))
									{
										file=data.getVariables().get("filename");
										if(file==null)
											file="";
										buf=data.getData();
									}
									if(file.length()>0)
									{
										if(buf == null)
											return "File not uploaded -- no buffer!";
										if((forum!=null)&&(forum.maxAttach()>0)&&(++maxFiles > forum.maxAttach()))
											return "File not uploaded -- maximum "+forum.maxAttach()+" attachments!";
										final String fileName = entry.key()+"/"+entry.parent()+"/"+file;
										if(fileName.length()>252)
											return "Reply not submitted.  Some attachments failed.";
										CMLib.database().DBCreateVFSFile(fileName, CMFile.VFS_MASK_ATTACHMENT, from, System.currentTimeMillis(), buf);
										attributes=attributes|JournalEntry.JournalAttrib.ATTACHMENT.bit;
									}
								}
							}
							CMLib.database().DBUpdateJournal(entry.key(), subj, clearWebMacros(text), attributes);
							if(cardinalNumber==0)
								cardinalNumber=entry.cardinal();
							if(cardinalNumber==0)
								messages.append("Message modified.<BR>");
							else
								messages.append("Message #"+cardinalNumber+" modified.<BR>");
							JournalInfo.clearJournalCache(httpReq, journalName);
							if((entry.parent()!=null)&&(entry.parent().length()>0))
							{
								httpReq.addFakeUrlParameter("JOURNALMESSAGE",entry.parent());
								httpReq.addFakeUrlParameter("JOURNALPARENT","");
							}
							CMLib.journals().clearJournalSummaryStats(forum);
						}
					}
					else
						return "Edit not authorized.";
				}
				else
				if(CMSecurity.isAllowedAnywhere(M,CMSecurity.SecFlag.JOURNALS))
				{
					if(parms.containsKey("TRANSFER")||parms.containsKey("COPY"))
					{
						if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
							return "Email not submitted -- Unauthorized.";
						final boolean transfer=parms.containsKey("TRANSFER");
						String journal=httpReq.getUrlParameter("NEWJOURNAL"+fieldSuffix);
						if((journal==null) || (journal.length()==0))
							messages.append("Transfer #"+cardinalNumber+" not completed -- No journal!<BR>");

						String realName=null;
						if(journal!=null)
						{
							final List<String> users=new ArrayList<String>();
							//if(forum != null)
							//	users.addAll(CMParms.parseAny(forum.getFlag(ForumJournalFlags.ASSIGN),':',true));
							if(!users.contains("ALL"))
								users.add("ALL");
							final boolean isPlayer=CMLib.players().playerExists(CMStrings.capitalizeAndLower(journal));
							if(journal.equals("FROM")||users.contains(journal)||isPlayer)
							{
								final String toName;
								MOB toM=null;
								if(journal.equals("FROM"))
								{
									toName=entry.from();
									toM=CMLib.players().getPlayerAllHosts(journal);
								}
								else
								if(isPlayer)
								{
									toName=CMStrings.capitalizeAndLower(journal);
									toM=CMLib.players().getPlayerAllHosts(journal);
								}
								else
									toName=journal;
								if(toM != null)
								{
									toM.tell(L("A message in @x1 was "
											+ ((transfer)?"transferred ":"copied ")
											+ "to you.",journalName));
								}
								CMLib.journals().clearJournalSummaryStats(forum);
								JournalInfo.clearJournalCache(httpReq, journalName);
								if(transfer)
								{
									entry.to(toName);
									CMLib.database().DBUpdateJournal(journalName, entry);
									messages.append("Message #"+cardinalNumber+" transferred<BR>");
								}
								else
								{
									CMLib.database().DBWriteJournal(journalName,
																	entry.from(),
																	toName,
																	entry.subj(),
																	entry.msg());
									messages.append("Message #"+cardinalNumber+" copied<BR>");
								}
								journal=null;
							}
							else
							if(journal.equals("MAILBOX"))
							{
								entry.to(entry.from());
								realName=CMProps.getVar(CMProps.Str.MAILBOX);
							}
							else
							{
								for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
								{
									final JournalsLibrary.CommandJournal CMJ=e.nextElement();
									if(journal.equalsIgnoreCase(CMJ.NAME())
									||journal.equalsIgnoreCase(CMJ.NAME()+"s")
									||journal.equalsIgnoreCase(CMJ.JOURNAL_NAME()))
									{
										realName=CMJ.JOURNAL_NAME();
										break;
									}
								}
							}
						}
						if(journal != null)
						{
							if(realName==null)
								realName=CMLib.database().DBGetRealJournalName(journal);
							if(realName==null)
								realName=CMLib.database().DBGetRealJournalName(journal.toUpperCase());
							if(realName==null)
								messages.append("The journal '"+journal+"' does not presently exist.  Aborted.<BR>");
							else
							{
								CMLib.journals().clearJournalSummaryStats(forum);
								if(transfer)
									CMLib.database().DBDeleteJournal(journalName,entry.key());
								if(journalName.toUpperCase().startsWith("SYSTEM_"))
									entry.update(System.currentTimeMillis());
								if(transfer)
									CMLib.database().DBWriteJournal(realName,entry);
								else
									CMLib.database().DBWriteJournal(realName, entry.from(), entry.to(), entry.subj(), entry.msg());
								CMLib.journals().clearJournalSummaryStats(forum);
								JournalInfo.clearJournalCache(httpReq, journalName);
								httpReq.addFakeUrlParameter("JOURNALMESSAGE","");
								if(transfer)
									messages.append("Message #"+cardinalNumber+" transferred<BR>");
								else
									messages.append("Message #"+cardinalNumber+" copied<BR>");
							}
						}
					}
				}
				else
					messages.append("You are not allowed to perform this function on message #"+cardinalNumber+".<BR>");
			}
			if(keepProcessing)
			{
				cardinalNumber++;
				entry = JournalInfo.getNextEntry(msgs, msgKey);
				while((entry!=null) && (!CMLib.journals().canReadMessage(entry,srch,M,parms.containsKey("NOPRIV"))))
					entry = JournalInfo.getNextEntry(msgs, entry.key());
				if(entry==null)
					keepProcessing=false;
				else
					msgKey=entry.key();
			}
		}
		return messages.toString();
	}

	public String fixForumString(final String s)
	{
		if(s==null)
			return "";
		final int x=s.toUpperCase().indexOf("<P>");
		final int y=s.toUpperCase().lastIndexOf("</P>");
		if((x>=0)&&(y>x))
		{
			return s.substring(0,x)+s.substring(x+3,y)+s.substring(y+4);
		}
		return s;
	}

}
