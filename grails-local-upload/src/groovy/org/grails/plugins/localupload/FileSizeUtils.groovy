package org.grails.plugins.localupload

import java.math.RoundingMode

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class FileSizeUtils {

	static final Log log = LogFactory.getLog(FileSizeUtils)
		
	static Long _byte  = 1
	static Long _kbyte = 1	* 1024
	static Long _mbyte = 1 	* 1024	* 1024
	static Long _gbyte = 1	* 1024	* 1024	* 1024
	static Long _tbyte = 1	* 1024	* 1024	* 1024	* 1024
	static Long _pbyte = 1	* 1024	* 1024	* 1024	* 1024	* 1024
	
	static String prettySizeFromBytes(byteSize){
		BigDecimal valSize
		
		try{
			valSize = new BigDecimal(byteSize)
		}catch(Exception e){
			log.info "Invalid byteSize ( $byteSize ) submitted to FileSizeUtils"
			return "0"
		}
		
		if(valSize < 0){
			log.info "Invalid byteSize( $byteSize ) submitted to FileSizeUtils - negative numbers not handled"
			return "0"
		}
		
		if(valSize == 0){
			return "0"
		}
		

		Long selectedUnit = 1
		String selectedUnitName
		if (valSize >= _byte && valSize < _kbyte) {
			return "$valSize B"
		} else if (valSize >= _kbyte && valSize < _mbyte) {
			selectedUnit = _kbyte
			selectedUnitName = "KiB"
		} else if (valSize >= _mbyte && valSize < _gbyte) {
			selectedUnit = _mbyte
			selectedUnitName = "MiB"
		} else if (valSize >= _gbyte) {
			selectedUnit = _gbyte
			selectedUnitName = "GiB"
		}else if (valSize >= _tbyte) {
			selectedUnit = _tbyte
			selectedUnitName = "TiB"
		}else if (valSize >= _pbyte) {
			selectedUnit = _pbyte
			selectedUnitName = "PiB"
		}
		
		return (valSize / selectedUnit).setScale(1, RoundingMode.HALF_UP) + ' ' + selectedUnitName
	}
	
	/*
	(0 - 1000) size = bytes
	(1000 - 1000*1024) size / 1000 = kbytes
	(1000*1024 - 1000*1024*1024) size / (1000 * 1024) = mbytes
	(else) size / (1000 * 1024 * 1024) = gbytes
	*/
}
