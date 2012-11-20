
import grails.test.*
import grails.test.mixin.TestFor
@TestFor(FileUploaderTagLib)
class FileUploaderTagLibTests {


    void testPrettySizeBytes() {
		assert applyTemplate('<fileuploader:prettysize size="850" />') == '850b'
    }

    void testPrettySize1KByte() {
		assert applyTemplate('<fileuploader:prettysize size="1000" />') == '1kb'
	}
	
	void testPrettySize16KBytes() {
		assert applyTemplate('<fileuploader:prettysize size="16000" />') == '16kb'
	}
			
	void testPrettySize18MBytes() {
		assert applyTemplate('<fileuploader:prettysize size="18432000" />') == '18mb'
    }

    void testPrettySize2_5GBytes() {
		assert applyTemplate('<fileuploader:prettysize size="2621440000" />') == '2.5gb'
	}

}
