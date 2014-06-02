PAGE.pages = function(){
    PAGES.initialize();
    return true;
}

PAGE.closeDialogArguments = function(args){
	$('#args').append(JSON.stringify(args));
}

PAGE.childUpdates = function(args){
	$('#args').append(JSON.stringify(args));
}

var PAGES = {
	
	initialize: function(){
		var me = this;

        me.addBindings();
		
		$('#inits').append('*');
		$('#args').append(JSON.stringify(PAGE.args));
	},

	addBindings: function(){
		$("#openPage, #closePage, #closeToPage, #updateParent, #updateParentPage, #refreshPage #openDialog, #openDialogWithTitle, #closeDialog, #closeDialogReset").unbind();
        $("#openPage").click(function(){
			PAGE.openPage('Page', 'system', {'test1': 'test2'});
			return false;
        });

		$("#closePage").click(function(){
			PAGE.closePage();
			return false;
		});

		$("#closeToPage").click(function(){
			PAGE.closeToPage('pages');
			return false;
		});

		
		
		$("#updateParent").click(function(){
			PAGE.updateParent({'child1': 'child2'});
			return false;
		});
		
		$("#updateParentPage").click(function(){
			PAGE.updateParentPage('pages', {'child3': 'child4'});
			return false;
		});

		$("#refreshPage").click(function(){
			PAGE.openPage('Refresh Page', 'refreshPage', {});
			return false;
		});

		
		
		$("#openDialog").click(function(){
			PAGE.openDialog('pages', {'dialogtest1': 'dialogtest2'});
			return false;
		});

		$("#openDialogWithTitle").click(function(){
			PAGE.openDialogWithTitle('Dialog Page', 'pages', {'dialogtest3': 'dialogtest4'});
			return false;
		});

		$("#closeDialog").click(function(){
			PAGE.closeDialog({'dialog': ['closed', 'arguments', 'array']});
			return false;
		});
		
		$("#closeDialogReset").click(function(){
			PAGE.closeDialog({"resetApp": true});
			return false;
		});
		
	}
}
