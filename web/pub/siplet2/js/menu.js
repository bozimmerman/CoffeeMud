var menuAreaHeight = 20;
var menuArea = null;
function configureMenu(obj)
{
	menuArea=obj;
	menuArea.style.position='fixed';
	menuArea.style.top=0;
	menuArea.style.width='100%';
	menuArea.style.height=menuAreaHeight+'px';
	menuArea.style.background='#404040';
	
	var menuData = [
		{"Muds": [
			{"n":"Open"},
			{"n":"Disconnect"},
			{"n":"Reconnect"}]},
		{"Options": [
			{"n":"blah"},
			{"n":"blah"},
			{"n":"blah"}]},
		{"Help": [
			{"n":"blah"},
			{"n":"blah"},
			{"n":"blah"}]}
	];
	var html = '';
	html += '<TABLE style="border: 1px solid white; border-collapse: collapse; height: 20px; table-layout: fixed; width: 100%;">';
	html +='<TR style="height: 20px;">';
	for(var to=0;to<menuData.length;to++)
	{
		var topO = menuData[to];
		var topN = Object.keys(topO)[0];
		var subList = topO[topN];
		html += '<TD style="border: 1px solid white; padding: 0;">';
		html += '<FONT COLOR=YELLOW><B>&nbsp;&nbsp;';
		html += topN + '</B></FONT></TD>';
	}
	html += '</TR></TABLE>';
	menuArea.innerHTML = html;
}
