var tabAreaHeight = 20;
var tabArea = null;
var tabTabs = [];
var tabSpacer = null;
var tabRow = null;

function configureTabs(obj)
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
	tabSpacer.innerHTML = '<FONT COLOR=LIGHTBLUE>&nbsp;&nbsp;<B>+</B></FONT>';
	tabSpacer.onclick = function() { 
		if(window.siplets.length > 0)
			AddNewSipletTab(window.siplets[window.siplets.length-1].url);
	};
	tabRow.appendChild(tabSpacer);
}

function AddNewTab()
{
	if(tabTabs.length < 10)
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
			case 9: tabSpan=0; break;
		}
		var spacerSpan=10 - ((tabTabs.length+1) * tabSpan); 
		for(var i=0;i<tabTabs.length;i++)
			tabTabs[i].colSpan=tabSpan;
		if(tabSpan == 0)
			tabSpacer.outerHTML = ''; // delete it!
		else
			tabSpacer.colSpan = spacerSpan;
		var tab = document.createElement('td');
		tab.colSpan = tabSpan;
		tab.style = "border: 1px solid white; padding: 0;background-color:yellow;foreground-color:black;";
		var currentTab = tabTabs.length;
		tab.onclick = function() {
			SetCurrentTab(currentTab)
		};
		tab.innerHTML = 'connecting...';
		if(tabSpan == 0)
			tabRow.appendChild(tab);
		else
			tabRow.insertBefore(tab,tabSpacer);
		tabTabs.push(tab);
		return tab;
	}
	return null;
}
