window.whichPicked = -1;
window.buttPage = 0;
window.iconsPerPage = 50;
var buttUp = false;
var style = 0; // 0=bottom, 1=top, 2=right
var saveStyle = 0;
var butts = [];

function MakeButtonFrame()
{
	if(style == 1)
		win.displayText('<FRAME PLUGBUTTS ACTION=OPEN INTERNAL ALIGN=TOP HEIGHT=40px SCROLLING=NO>');
	else
	if(style == 2)
		win.displayText('<FRAME PLUGBUTTS ACTION=OPEN INTERNAL ALIGN=RIGHT WIDTH=40px SCROLLING=NO>');
	else
		win.displayText('<FRAME PLUGBUTTS ACTION=OPEN INTERNAL ALIGN=BOTTOM HEIGHT=40px SCROLLING=NO>');
}

function RebuildBar()
{
	var html = '<DEST PLUGBUTTS EOF>';
	var i;
	for(i=0;i<butts.length;i++)
	{
		var fn = butts[i].iconPath;
		var ac = butts[i].command;
		if(fn && ac)
		{
			html += '<a HREF="javascript:addToPrompt(\''+ac+'\',true);">';
			html += '<IMG SRC="media://'+fn+'" WIDTH=32 HEIGHT=32>';
			html += '</a>';
		}
	}
	html += '<SEND COMMAND HREF="win.sendPlugin(\'ButtonBar\',\'ButtonEditor\')">';
	if(style == 2)
		html += '<IMG ALIGN=LEFT STYLE="position:absolute;bottom:0;left:0;" SRC="media:///icons/fc2.png" WIDTH=32 HEIGHT=32>';
	else
		html += '<IMG ALIGN=RIGHT SRC="media:///icons/fc2.png" WIDTH=32 HEIGHT=32>';
	html += '</SEND></DEST>';
	win.displayAt(html, 'PLUGBUTTS');
	buttUp = true;
}

LoadButtons();
MakeButtonFrame();
setTimeout(RebuildBar(),100);

function ShowButtonEditor()
{
	win.displayText('<FRAME BUTTONEDITOR ACTION=OPEN TITLE="Button Editor" FLOATING=close SCROLLING=y TOP=10% LEFT=25% WIDTH=50% HEIGHT=50%>');
	var html = '<DEST NAME=BUTTONEDITOR EOF><FONT COLOR=WHITE>';
	saveStyle = style;
	html += 'Style:';
	html += '&nbsp;<INPUT TYPE=RADIO '+((saveStyle == 0)?'CHECKED=CHECKED':'')+' NAME=ButtStyle VALUE=0>Bottom';
	html += '&nbsp;<INPUT TYPE=RADIO '+((saveStyle == 1)?'CHECKED=CHECKED':'')+' NAME=ButtStyle VALUE=1>Top';
	html += '&nbsp;<INPUT TYPE=RADIO '+((saveStyle == 2)?'CHECKED=CHECKED':'')+' NAME=ButtStyle VALUE=2>Right';
	html += '<BR>';
	html += '<i>Click on an icon to change it, then type in the command to run when clicked.	';
	html += 'Leave blank to remove a button.</i><BR><BR>';
	var i;
	var last = -1;
	for(i=0;i<50;i++)
	{
		var fn = win.getEntity('ButtonIcon'+i);
		var ac = win.getEntity('ButtonSend'+i);
		if(fn && ac)
		{
			html+='Icon #'+(i+1)+'<BR>';
			html+='<INPUT TYPE=HIDDEN NAME="ButtonIcon'+i+'" VALUE="'+fn+'">';
			html+='<SEND HREF="win.sendPlugin(\'ButtonBar\',\'ButtonPick'+i+'\')">';
			html+='<IMG SRC="media://'+fn+'" WIDTH=32 HEIGHT=32>';
			html+='</SEND>';
			html+='&nbsp;:&nbsp;<INPUT TYPE=TEXT NAME="ButtonSend'+i+'" VALUE="'+ac+'">';
			html+='<BR>';
		}
		else
		if(last<0)
			last=i;
	}
	if(last<0)
		last=0;
	html+='Icon #'+(last+1)+'<BR>';
	html+='<INPUT TYPE=HIDDEN NAME="ButtonIcon'+last+'" VALUE="'+fn+'">';
	html+='<SEND HREF="win.sendPlugin(\'ButtonBar\',\'ButtonPick'+last+'\')">';
	html+='<IMG SRC="media:///icons/fc13.png" WIDTH=32 HEIGHT=32>';
	html+='</SEND>';
	html+='&nbsp;:&nbsp;<INPUT TYPE=TEXT NAME="ButtonSend'+last+'" VALUE="'+fn+'">';
	html += '<BR><BR>';
	html += '<INPUT TYPE=BUTTON NAME="ButtonClose" VALUE="Save and Close">';
	html += '</FONT></DEST>';
	win.displayAt(html,'BUTTONEDITOR');
}

function ShowIconPicker()
{
	win.displayText('<FRAME ICONPICKER SCROLLING=y TITLE="Select Icon" ACTION=OPEN FLOATING=close SCROLLING=y TOP=10% LEFT=25% WIDTH=25% HEIGHT=75%>');
	var html = '<DEST NAME=ICONPICKER EOF><FONT COLOR=WHITE>';
	for(i=0;i<window.icons.length;i++)
	{
		var fn = window.icons[i].split('/').pop();
		var x = fn.match(/\d+/)[0];
		html+='<SEND HREF="win.sendPlugin(\'ButtonBar\',\'ButtonPicked'+x+'\')">';
		html+='<IMG SRC="media:///icons/fc'+x+'.png" WIDTH=32 HEIGHT=32>';
		html+='</SEND>';
	}
	win.displayAt(html,'ICONPICKER');
}

function LoadButtons()
{
	var bbstr = localStorage.getItem('BarButtons');
	if (bbstr)
		butts = JSON.parse(bbstr);
	if(!Array.isArray(butts))
	{
		style = Number(butts.style);
		butts = butts.buttons;
	}
}

function LoadButtonEditor()
{
	var bbstr = localStorage.getItem('BarButtons');
	var butts = [];
	if (bbstr)
		butts = JSON.parse(bbstr);
	style = 0;
	if(!Array.isArray(butts))
	{
		style = Number(butts.style);
		butts = butts.buttons;
	}
	for(i=0;i<butts.length;i++)
	{
		win.setEntity('ButtonIcon'+i, butts[i].iconPath);
		win.setEntity('ButtonSend'+i, butts[i].command);
	}
	for(i=butts.length;i<50;i++)
	{
		win.setEntity('ButtonIcon'+i, null);
		win.setEntity('ButtonSend'+i, null);
	}
}

function SaveButtonEditor()
{
	var i;
	var newbutts = [];
	for(i=0;i<50;i++)
	{
		var fn = win.getEntity('ButtonIcon'+i);
		var ac = win.getEntity('ButtonSend'+i);
		if(fn && ac)
		{
			var obj = 
			{
				"iconPath": fn,
				"command": ac
			};
			newbutts.push(obj);
		}
		win.setEntity('ButtonIcon'+i, null);
		win.setEntity('ButtonSend'+i, null);
	}
	var obj = 
	{
		"style": style,
		"buttons": newbutts
	}
	localStorage.setItem('BarButtons', JSON.stringify(obj));
	butts = newbutts;
}

function ShowHelp()
{
	win.displayText('<FRAME BUTTONHELP SCROLLING=y TITLE="Button Bar Help" ACTION=OPEN FLOATING=close SCROLLING=y TOP=10% LEFT=25% WIDTH=250 HEIGHT=150>');
	var html = '<DEST NAME=BUTTONHELP EOF><FONT COLOR=WHITE>';
	html += 'Click the Widget icon or enter /buttonedit to modify your button bar.';
	html += '<BR><BR>Enter /buttonbar to toggle the bar on/off.';
	win.displayAt(html,'BUTTONHELP');
}

window.onevent = function(event)
{
	if(event)
	{
		if(event.type && event.type === 'ButtonUpdate')
			ShowButtonEditor();
		if(event.type && event.type === 'ButtonClose')
		{
			if(style != saveStyle)
			{
				win.displayText('<FRAME PLUGBUTTS ACTION=CLOSE INTERNAL>');
				style = saveStyle;
				MakeButtonFrame();
			}
			SaveButtonEditor();
			RebuildBar();
			win.displayText('<FRAME BUTTONEDITOR ACTION=CLOSE>');
		}
		if(event.type && event.type === 'ButtonEdit')
		{
			LoadButtonEditor();
			ShowButtonEditor();
		}
		if(event.type && event.type === 'ButtStyle')
		{
			saveStyle = win.getEntity('ButtStyle');
		}
		if(event.type && event.type === 'ButtonToggle')
		{
			if(buttUp)
			{
				win.displayText('<FRAME PLUGBUTTS ACTION=CLOSE INTERNAL>');
				buttUp=false;
			}
			else
			{
				MakeButtonFrame();
				setTimeout(RebuildBar,250);
			}
		}
		if(event.data && (typeof event.data === 'string'))
		{
			if(event.data === 'ButtonEditor')
			{
				LoadButtonEditor();
				ShowButtonEditor();
			}
			if(event.data === 'Help')
			{
				ShowHelp();
			}
			if(event.data.startsWith('ButtonPicked'))
			{
				if (window.whichPicked>=0)
				{
					var which = Number(event.data.substr(12).trim());
					win.setEntity('ButtonIcon'+window.whichPicked, 'icons/fc'+which+'.png');
					win.displayText('<FRAME ICONPICKER ACTION=CLOSE>');
					if(!win.getEntity('ButtonSend'+window.whichPicked))
							win.setEntity('ButtonSend'+window.whichPicked, '(change me)');
					window.whichPicked = -1;
					ShowButtonEditor();
				}
			}
			else
			if(event.data.startsWith('ButtonPick'))
			{
				var which = Number(event.data.substr(10).trim());
				window.whichPicked = which;
				ShowIconPicker();
			}
		}
	}
}