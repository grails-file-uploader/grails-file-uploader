import grails.test.*
import com.lucastex.grails.fileuploader.DownloadControllerTests

class FileUploaderTagLibTests extends TagLibUnitTestCase {
	
	def fut
	
    protected void setUp() {
        super.setUp()
		fut = new FileUploaderTagLib()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testPrettySizeBytes() {
		assertEquals fut.prettysize(size: 850).toString(), "850b"
    }

    void testPrettySize1KByte() {
		assertEquals fut.prettysize(size: 1000).toString(), "1kb"
	}
	
	void testPrettySize16KBytes() {
		assertEquals fut.prettysize(size: 16000).toString(), "16kb"
	}
			
	void testPrettySize18MBytes() {
		assertEquals fut.prettysize(size: 18432000).toString(), "18mb"
    }

    void testPrettySize2_5GBytes() {
		assertEquals fut.prettysize(size: 2621440000).toString(), "2.5gb"	
	}

}
