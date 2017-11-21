package PacketLib;

import java.util.Arrays;

public class binaryTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int AckFlag = 1;
        int SynFlag = 0;
        int FinFlag = 1;
        int dataFlag = 1;
		int type = AckFlag | (SynFlag << 1) | (FinFlag << 4) | (dataFlag << 6);
		System.out.println("Before :");
		System.out.println("type: "+ type);
		System.out.println("AckFlag : " + AckFlag +"\nSynFlag :"+ SynFlag +"\nFinFlag: "+ FinFlag +"\ndataFlag: "+ dataFlag);
		System.out.println(Integer.toString(type, 2));
		String temp = "0" +Integer.toString(type, 2);
		System.out.println("temp: "+ temp);
		char[] chars = temp.toCharArray();
		System.out.println("chars : "+chars);
        String[] strs= new String[(chars.length+1)/2];
        System.out.println("strs : "+strs);
        for(int i=0,j=0;i<chars.length;i+=2,j++)
        {
           strs[j]=new String(Arrays.copyOfRange(chars,i,i+2));
        }
//        byte [] bData = new byte [2];
//        bData[0] = (byte) type;
//        bData[1] = (byte) (type >>> 8);
//        bData[2] = (byte) (type >>> 16);
//        bData[3] = (byte) (type >>> 24);
        AckFlag = Integer.parseInt(strs[0],2);
        SynFlag = Integer.parseInt(strs[1],2);
        FinFlag = Integer.parseInt(strs[2],2);
        dataFlag = Integer.parseInt(strs[0],2);
        System.out.println("After :");
		System.out.println("AckFlag : " + AckFlag +"\nSynFlag :"+ SynFlag +"\nFinFlag: "+ FinFlag +"\ndataFlag: "+ dataFlag);
		
	}

}
