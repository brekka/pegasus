$(function () {
	
	if (window.File && window.FileReader && window.FileList && window.Blob) {
		// Must have the api
		$('#normal_filefield').remove();
		
		var multiple = (navigator.userAgent.indexOf("Firefox") == -1);
		var cnt = 0;
		$('#enhanced').html('<input id="fileupload" type="file" name="files[]" multiple="' + multiple + '" />');
		$('#send_button').attr("disabled", "disabled");
	    $('#fileupload').fileupload({
	    	maxChunkSize: 1000000,
	        dataType: 'json',
	        url: 'upload',
	        done: function (e, data) {
	        	$.each(data.files, function (index, file) {
	        		$(file.progress).text("100%");
	        		cnt --;
	            });
	        	if (cnt == 0) {
	        		$('#send_button').removeAttr("disabled");
	        	}
	        },
	        add: function (e, data) {
	        	$('#files').show();
	        	//$('#files tbody').empty();
	        	$.each(data.files, function (index, file) {
	        		$('#files tbody').append('<tr><td>' + file.name + '</td><td>' + file.size + '</td><td class="progress">0%</td></tr>');
	        		file.progress = $('#files tr:last .progress');
	        		cnt ++;
	            });
	        	data.submit();
	        },
	        progress : function (e, data) {
	        	var p = parseInt(data.loaded / data.total * 100, 10);
	        	$.each(data.files, function (index, file) {
	        		$(file.progress).text(p + "%");
	            });
	        },
	        start: function (e) {
	        	$('#send_button').attr("disabled", "disabled");
	        },
	    });
	}
});
