var tabAreaHeight = 20;
var tabArea = null;
var tabTabs = [];
var tabSpacer = null;
var tabRow = null;

function ConfigureMainTabs(obj)
{
	tabArea=obj;
	tabArea.style.position='fixed';
	tabArea.style.top='20px';
	tabArea.style.width='100%';
	tabArea.style.height=tabAreaHeight+'px';
	tabArea.style.background='#000000';
	tabArea.style.background='black';
	var html = '';
	html += '<TABLE style="border: 1px solid white; border-collapse: collapse; height: 20px; table-layout: fixed; width: 100%;">';
	html +='<TR style="height: 19px;">';
	html += '</TR></TABLE>';
	tabArea.innerHTML = html;
	tabRow = Array.from(tabArea.getElementsByTagName('tr'))[0];
	tabTabs = Array.from(tabRow.getElementsByTagName('td'));
	tabTabs.splice(tabTabs.length-1);
	tabSpacer = document.createElement('td');
	tabSpacer.colSpan = 10;
	tabSpacer.style = "border: 1px solid white; padding: 0;";
	tabSpacer.innerHTML='';
	tabSpacer.onclick = function() { 
		if(window.siplets.length > 0)
		{
			var copySiplet = window.siplets[window.siplets.length-1];
			var win = AddNewSipletTab(copySiplet.url);
			if(win) {
				win.pb = copySiplet.pb;
				win.pbwhich = copySiplet.pbwhich;
				win.tabTitle = copySiplet.tabTitle;
			}
		}
	};
	tabRow.appendChild(tabSpacer);
}

function FindTabSpan()
{
	var tabSpan=1;
	switch(tabTabs.length)
	{
		case 0: tabSpan=9; break;
		case 1: tabSpan=4; break;
		case 2: tabSpan=3; break;
		case 3: tabSpan=2; break;
		case 4: tabSpan=2; break;
		case 5: tabSpan=1; break;
		case 6: tabSpan=1; break;
		case 7: tabSpan=1; break;
		case 8: tabSpan=1; break;
		case 9: tabSpan=1; break;
	}
	return tabSpan;
}

function CloseTab(img)
{
	var tab = img.parentNode;
	var siplet = tab.siplet;
	siplet.close();
	try {
		siplet.topWindow.outerHTML = '';
	} catch(e) { }
	var isCurrent =(window.currWin == siplet);
	var i = window.siplets.indexOf(siplet);
	window.siplets.splice(i,1);
	if(isCurrent)
	{
		if(window.siplets.length == 0)
			window.currWin = null;
		else
			SetCurrentTab(0);
	}
	var tabRow = tab.parentNode;
	tabRow.removeChild(tab);
	var x = tabTabs.indexOf(tab);
	tabTabs.splice(x,1);
	var tabSpan = FindTabSpan();
	var spacerSpan=10 - ((tabTabs.length) * tabSpan); 
	for(var i=0;i<tabTabs.length;i++)
		tabTabs[i].colSpan=tabSpan;
	if(tabTabs.length == 9)
		tabRow.appendChild(tabSpacer); // re-add it
	tabSpacer.colSpan = spacerSpan;
	if(window.siplets.length == 0)
		tabSpacer.innerHTML = '';
	else
		tabSpacer.innerHTML = '<FONT COLOR=LIGHTBLUE>&nbsp;&nbsp;<B>+</B></FONT>';
}

function ReconnectTab(img)
{
	var tab = img.parentNode;
	var siplet = tab.siplet;
	siplet.closeSocket();
	siplet.reset();
	siplet.connect(siplet.url);
}

function AddNewTab()
{
	if(tabTabs.length < 10)
	{
		var tabSpan = FindTabSpan();
		var spacerSpan=10 - ((tabTabs.length+1) * tabSpan); 
		for(var i=0;i<tabTabs.length;i++)
			tabTabs[i].colSpan=tabSpan;
		if(tabTabs.length == 9)
			tabSpacer.parentNode.removeChild(tabSpacer); // delete it, but also keep it
		else
			tabSpacer.colSpan = spacerSpan;
		var tab = document.createElement('td');
		tab.onmouseover=function() {
			var old = tab.getElementsByTagName('img');
			if((old==null)||(old.length ==0))
			{
				if(tab.siplet && !tab.siplet.isConnected())
				{
					var reconn = '<IMG style="position:absolute;top:0;right:18px;width:16px; height:16px;" '
					+'ONCLICK="ReconnectTab(this);" TITLE="Re-Connect"'
					+'SRC="images/recon.gif">';
					tab.innerHTML += reconn;
				}
				var close = '<IMG style="position:absolute;top:0;right:0;width:16px; height:16px;" '
				+'ONCLICK="CloseTab(this);"  TITLE="Close Tab"'
				+'SRC="images/close.gif">';
				tab.innerHTML += close;
			}
		};
		tab.onmouseleave=function() {
			var old = tab.getElementsByTagName('img');
			if(old)
			{
				old =  Array.from(old);
				for(var i=0;i<old.length;i++)
					tab.removeChild(old[i]);
			}
		}
		tab.colSpan = tabSpan;
		tab.style = "border: 1px solid white; padding: 0;background-color:yellow;color:black;";
		tab.style.fontSize = 10;
		tab.style.position = 'relative';
		tab.onclick = function() {
			SetCurrentTabByTab(this);
		};
		if(tabTabs.length == 9)
			tabRow.appendChild(tab);
		else
			tabRow.insertBefore(tab,tabSpacer);
		tabTabs.push(tab);
		tabSpacer.innerHTML = '<FONT COLOR=LIGHTBLUE>&nbsp;&nbsp;<B>+</B></FONT>';
		return tab;
	}
	return null;
}
