package com.android.liveshooter.vo;

public class NALU_t {

	public int startcodeprefix_len; // ! 4 for parameter sets and first slice in
								// picture, 3 for everything else (suggested)
	public int len; // ! Length of the NAL unit (Excluding the start code, which does
				// not belong to the NALU)
	public int max_size; // ! Nal Unit Buffer size
	public int forbidden_bit; // ! should be always FALSE
	public int nal_reference_idc; // ! NALU_PRIORITY_xxxx
	public int nal_unit_type; // ! NALU_TYPE_xxxx
	public byte[] buf = new byte[8000000]; // ! contains the first byte followed by the
									// EBSP
	public int lost_packets; // ! true, if packet loss is detected
}
