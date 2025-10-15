viewon = false;

function showHelp()
{
	win.displayText('<FRAME GMCPHELP SCROLLING=y TITLE="GMCP Help" ACTION=OPEN FLOATING=close SCROLLING=y TOP=10% LEFT=15% WIDTH=450 HEIGHT=250>');
	var html = '<DEST NAME=GMCPHELP EOF><FONT COLOR=WHITE>';
	html += 'This plugin requires an active connection to a MUD with GMCP enabled.<BR><BR>';
	html += 'Enter <B>/gmcp [command] [json]</B> to send a GMCP command.<BR>';
	html += 'For example: /gmcp core.supports.add [&quot;room.info 1&quot;]<BR><BR>';
	html += 'Enter <B>/gmcp on</B> to display GMCP data as it is received.<BR><BR>';
	html += 'Enter <B>/gmcp off</B> to disable GMCP display.<BR><BR>';
	win.displayAt(html,'GMCPHELP');
}

window.onevent=function(event)
{
	if((event.type==='gmcp')&&(viewon))
	{
		if(!viewon)
			return;
		win.displayText('<BR><FONT COLOR=WHITE><B>'+event.command+'</B></FONT><BR>'+JSON.stringify(event.data,null,2))
		return;
	}
	if(event.data && event.data.toLowerCase)
	{
		if(event.data === 'Help')
		{
			showHelp();
			return;
		}
		else
		if(event.data.toLowerCase() == 'on')
		{
			if(viewon)
				win.displayText('<BR><FONT COLOR=WHITE>GMCP View is already on.</FONT><BR>');
			else
				win.displayText('<BR><FONT COLOR=WHITE>GMCP View is now ON.</FONT><BR>');
			viewon = true;
			return;
		}
		if(event.data.toLowerCase() == 'off')
		{
			if(!viewon)
				win.displayText('<BR><FONT COLOR=WHITE>GMCP View is already off.</FONT><BR>');
			else
				win.displayText('<BR><FONT COLOR=WHITE>GMCP View is now OFF.</FONT><BR>');
			viewon = false;
			return;
		}
		var x = event.data.indexOf(' ');
		if(x<0)
		{
			event.data += ' {}'
			x = event.data.indexOf(' ');
		}
		var cmd = event.data.substr(0,x);
		var json = event.data.substr(x+1)
		win.displayText('<BR><FONT COLOR=WHITE>Sent GMCP Command: ' + event.data+'</FONT><BR>');
		win.sendGMCP(cmd,json);
	}
};
