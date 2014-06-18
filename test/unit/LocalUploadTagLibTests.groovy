
import grails.test.*
import grails.test.mixin.TestFor
@TestFor(LocalUploadTagLib)
class LocalUploadTagLibTests {

	private void setBasics(controllerName, actionName, params){
		LocalUploadTagLib.metaClass.controllerName = controllerName
		LocalUploadTagLib.metaClass.actionName= actionName
		LocalUploadTagLib.metaClass.params = params
	}
	
	void testMinimalParamsForDownload(){
		setBasics('example', 'show', [id:'1'])
		assert applyTemplate('<localUpload:download fileId="30">my link</localUpload:download>') == 
				'<a href="/localUpload/download/30?errorAction=show&amp;errorController=example&amp;saveAssocId=1&amp;contentDisposition=inline">my link</a>'
	}
	
	void testMaxParamsForDownload(){
		setBasics('example', 'show', [id:'1'])
		assert applyTemplate('<localUpload:download class="btn btn-default" fileId="30" errorController="widgets" errorAction="show" saveAssocId="2"><i>myfile</i></localUpload:download>') == 
				'<a href="/localUpload/download/30?errorAction=show&amp;errorController=widgets&amp;saveAssocId=2&amp;contentDisposition=inline" class="btn btn-default"><i>myfile</i></a>'
	}
	
	void testPrettySizeBytes(){
		assert applyTemplate('<localUpload:prettysize size="850" />') == '850 B'
	}
	
	void testMinimalParamsForMinUpload(){
		assert applyTemplate('<localUpload:minupload bucket="docs"/>') ==
			'''<input type="hidden" name="bucket" value="docs" /><input type="hidden" name="fileParam" value="files"/><input type="file" name="files" />'''
	}
	
	void testMaxParamsForMinUpload(){
		assert applyTemplate('<localUpload:minupload class="inSession" bucket="docs" name="bobs" multiple="true"/>') ==
			'''<input type="hidden" name="bucket" value="docs" /><input type="hidden" name="fileParam" value="bobs"/><input type="file" name="bobs" multiple="multiple" class="inSession" />'''
	}
	
	void testForm(){
		setBasics('example', 'show', [id:'1'])
		
		String tag = '''<form action="/localUpload/upload/1" method="post" enctype="multipart/form-data" >'''
		tag += '''<input type="hidden" name="bucket" value="docs" />'''
		tag += '''<input type="hidden" name="saveAssoc" value="example" />'''
		tag += '''<input type="hidden" name="errorAction" value="show" />'''
		tag += '''<input type="hidden" name="errorController" value="example" />'''
		tag += '''<input type="hidden" name="successAction" value="show" />'''
		tag += '''<input type="hidden" name="successController" value="example" />'''
		tag += '''<input type="file" name="files" /><input type="submit" name="submit" value="Submit" /></form>'''
		
		assert applyTemplate('<localUpload:form bucket="docs" saveAssoc="example" id="1" />') == tag
	}
	
	void testMultipleTrueForm(){
		setBasics('example', 'show', [id:'1'])
		
		String tag = '''<form action="/localUpload/upload/1" method="post" enctype="multipart/form-data" >'''
		tag += '''<input type="hidden" name="bucket" value="docs" />'''
		tag += '''<input type="hidden" name="saveAssoc" value="example" />'''
		tag += '''<input type="hidden" name="errorAction" value="show" />'''
		tag += '''<input type="hidden" name="errorController" value="example" />'''
		tag += '''<input type="hidden" name="successAction" value="show" />'''
		tag += '''<input type="hidden" name="successController" value="example" />'''
		tag += '''<input type="file" name="files" multiple="multiple"/><input type="submit" name="submit" value="Submit" /></form>'''
		
		assert applyTemplate('<localUpload:form bucket="docs" multiple="true" saveAssoc="example" id="1" />') == tag
	}
}
