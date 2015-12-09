/**
 * 
 */
package mrtech.smarthome.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CJ
 * @date 2015年7月17日 下午4:59:19
 * @version 1.0
 */
public class NumberUtil
{
	private static String[] zArray = {"0", "00", "000", "0000", "00000", "000000", "0000000", "00000000"};
	
	/**
	 * 解码二维码内容。
	 * @param encodeString
	 * @return qeKkSXVI@umkss76gncqu 或者 空字符（二维码有问题）
	 */
	public static String decodeQRCode(String encodeString)
	{
		/**
		 * 5M1LR3-2I68TW-DX2V4M-SWJTIS-GP8RXL-8BY 这个是6个36进制的Little-Endian32位整形，
		 * 分别是 339357279, 151456820, 841621270, 1747725364, 1009858665,10798 
		 * 获取这6个整形的字节连接在一起，得到 [95, 46, 58, 20, 52, 12, 7, 9, 22, 31, 42,50, 52, 44, 44, 104, 105, 56, 49, 60, 46, 42, 0, 0]
		 * 共24个字节，取前面22个字节，得到 [95, 46, 58, 20, 52, 12, 7, 9, 22, 31, 42, 50, 52,44, 44, 104, 105, 56, 49, 60, 46, 42] 
		 * 数组中除第一个元素外的所有元素与第一个进行异或运算，得到
		 * [113, 101, 75, 107, 83, 88, 86, 73, 64, 117, 109, 107, 115, 115, 55,54, 103, 110, 99, 113, 117] 
		 * 转换为ASCII字符串，得到 qeKkSXVI@umkss76gncqu
		 */
		StringBuffer decodeBuffer = new StringBuffer();
		try
		{
			String[] values = encodeString.split("-");
			if(values.length != 6)
				return "";
			List<Integer> list = new ArrayList<Integer>();
			for(int i = 0; i < values.length; i++)
			{
				String value32 = values[i];
				String value16 = Integer.toHexString(toDecimal(value32, 36));
				if(value16.length() < 8)
				{
					value16 = zArray[8 - value16.length() - 1] + value16;
				}
				
				//Little-Endian：16进制中两个字节表示一个数据，倒过来表示就是Little-Endian
				String value160 = value16.substring(0, 2);
				String value161 = value16.substring(2, 4);
				String value162 = value16.substring(4, 6);
				String value163 = value16.substring(6, 8);
				
				list.add(Integer.valueOf(value163, 16));
				list.add(Integer.valueOf(value162, 16));
				list.add(Integer.valueOf(value161, 16));
				list.add(Integer.valueOf(value160, 16));
			}
			list.remove(list.size() - 1);
			list.remove(list.size() - 1);
			if(list.size() != 22)
				return "";
			for(int i = 1; i < list.size(); i++)
			{
				int yihuo = list.get(0) ^ list.get(i);
				decodeBuffer.append((char)yihuo);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return decodeBuffer.toString();
	}
	
	
	/**
	 * 返回10进制数
	 * @param input	需要转换的数据
	 * @param base	进制数
	 * @return
	 */
	public static int toDecimal(String input, int base)
	{
		BigInteger Bigtemp = BigInteger.ZERO, temp = BigInteger.ONE;
		int len = input.length();
		for (int i = len - 1; i >= 0; i--)
		{
			if (i != len - 1)
				temp = temp.multiply(BigInteger.valueOf(base));
			int num = changeDec(input.charAt(i));
			Bigtemp = Bigtemp.add(temp.multiply(BigInteger.valueOf(num)));
		}
		return Bigtemp.intValue();
	}
	
	private static int changeDec(char ch)
	{
		int num = 0;
		if (ch >= 'A' && ch <= 'Z')
			num = ch - 'A' + 10;
		else if (ch >= 'a' && ch <= 'z')
			num = ch - 'a' + 36;
		else
			num = ch - '0';
		return num;
	}
}
