package org.grails.plugins.localupload

import grails.test.mixin.*

import org.junit.Test

class FileSizeUtilsTest {

	@Test
	void prettySizeNegativeTest(){
		assert "0" == FileSizeUtils.prettySizeFromBytes(-1)
		assert "0" == FileSizeUtils.prettySizeFromBytes("-1")
	}

	@Test
	void prettySizeZeroTest(){
		assert "0" == FileSizeUtils.prettySizeFromBytes(0)
		assert "0" == FileSizeUtils.prettySizeFromBytes("0")
	}

	@Test
	void prettySizeOneTest(){
		assert "1 B" == FileSizeUtils.prettySizeFromBytes(1)
		assert "1 B" == FileSizeUtils.prettySizeFromBytes("1")
	}

	@Test
	void prettySize50byteTest(){
		assert "50 B" == FileSizeUtils.prettySizeFromBytes(50)
		assert "50 B" == FileSizeUtils.prettySizeFromBytes("50")
	}

	@Test
	void prettySize1000byteTest(){
		assert "1.0 KiB" == FileSizeUtils.prettySizeFromBytes(1024)
		assert "1.0 KiB" == FileSizeUtils.prettySizeFromBytes("1024")
	}

	@Test
	void prettySizeRoundingTest(){
		assert "1.5 KiB" == FileSizeUtils.prettySizeFromBytes(1536)
		assert "1.6 KiB" == FileSizeUtils.prettySizeFromBytes(1640)
		assert "1.5 KiB" == FileSizeUtils.prettySizeFromBytes(1500)
	}
	
	@Test
	void prettySize500000byteTest(){
		assert "500.0 KiB" == FileSizeUtils.prettySizeFromBytes(512000)
		assert "500.0 KiB" == FileSizeUtils.prettySizeFromBytes("512000")
	}
	
	@Test
	void prettySize500000000byteTest(){
		assert "500.0 MiB" == FileSizeUtils.prettySizeFromBytes(524288000)
		assert "500.0 MiB" == FileSizeUtils.prettySizeFromBytes("524288000")
	}
}
