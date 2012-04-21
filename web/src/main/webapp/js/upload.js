$(function () {
	if (window.File && window.FileReader && window.FileList && window.Blob) {
		// Must have the api
		$('#normal_filefield').hide();
		var multiple = (navigator.userAgent.indexOf("Firefox") == -1);
		$('#enhanced').html('<input id="fileupload" type="file" name="files[]" multiple="' + multiple + '" />');
	    $('#fileupload').fileupload({
	    	maxChunkSize: 1000000,
	        dataType: 'json',
	        url: 'upload',
	        done: function (e, data) {
	           alert(data);
	        }
	    });
	}
});
