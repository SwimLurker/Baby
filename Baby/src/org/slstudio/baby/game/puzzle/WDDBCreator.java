package org.slstudio.baby.game.puzzle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


public class WDDBCreator {
	public static final int BOARD_WIDTH = 4;
	public static final int WDTBL_SIZE =  24964;
	
	private int[][] TABLE = new int[BOARD_WIDTH][BOARD_WIDTH];
	private int WDTOP, WDEND;
	private long[] WDPTN = new long[WDTBL_SIZE];
	private byte[] WDTBL = new byte[WDTBL_SIZE];
	private short[][][] WDLINK = new short[WDTBL_SIZE][2][BOARD_WIDTH];
	
	private void simulation(){
		int space = 0;
		int piece;
		byte count;
		for(int i = 0; i<4; i++){
			for(int j=0; j<4; j++){
				TABLE[i][j] = 0;
			}
		}
		
		TABLE[0][0] = TABLE[1][1] = TABLE[2][2] = 4;
		TABLE[3][3] = 3;
		
		long table = 0;
		for(int i = 0; i<4; i++){
			for(int j=0; j<4; j++){
				table = (table<<3) | TABLE[i][j];
			}
		}
		
		WDPTN[0] = table;
		WDTBL[0] = 0;
		
		for(int i = 0; i<2; i++){
			for(int j=0; j<4; j++){
				WDLINK[0][i][j] = WDTBL_SIZE;
			}
		}
		
		WDTOP = 0;
		WDEND = 1;
		
		while(WDTOP < WDEND){
			table = WDPTN[WDTOP];
			count = WDTBL[WDTOP];
			WDTOP++;
			count++;
			
			for(int i=3; i>=0; i--){
				piece = 0;
				for(int j = 3; j>=0; j--){
					TABLE[i][j] = (int)(table & 7);
					table>>=3;
					piece += TABLE[i][j];
				}
				
				if(piece ==3) {
					space = i;
				}
			}
			
			if((piece = space + 1) < 4){
				for(int i = 0; i < 4; i++){
					if(TABLE[piece][i] != 0){
						TABLE[piece][i] --;
						TABLE[space][i] ++;
						writeTable(count, 0, i);
						TABLE[piece][i] ++;
						TABLE[space][i] --;
					}
				}
			}
			
			if((piece = space - 1) >= 0){
				for(int i = 0; i < 4; i++){
					if(TABLE[piece][i] != 0){
						TABLE[piece][i] --;
						TABLE[space][i] ++;
						writeTable(count, 1, i);
						TABLE[piece][i] ++;
						TABLE[space][i] --;
					}
				}
			}	
		}	
	}
	
	private void writeTable(byte count, int vect, int group){
		long table = 0;
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				table = (table << 3) | TABLE[i][j];
			}
		}
		int index = WDEND;
		for(int i = 0; i < WDEND; i++){
			if(WDPTN[i] == table){
				index = i;
				break;
			}
		}
		
		if(index == WDEND){
			WDPTN[WDEND] = table;
			WDTBL[WDEND] = count;
			WDEND++;
			for (int i = 0; i < 2; i++){
				for(int j = 0; j < 4; j++){
					WDLINK[index][i][j] = WDTBL_SIZE;
				}
			}
		}
		
		int index2 = WDTOP -1;
		WDLINK[index2][vect][group] = (short) index;
		WDLINK[index][vect ^ 1][group] = (short) index2;
	}
	
	private void writeToFile(String filename) throws IOException{
		File file = new File(filename);
		if(!file.exists()){
			file.createNewFile();
		}
		File file2 = new File(filename+".txt");
		if(!file2.exists()){
			file2.createNewFile();
		}
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		
		FileWriter fw = new FileWriter(file2);
		
		
		for(int i = 0; i<WDTBL_SIZE; i++){
			dos.writeLong(WDPTN[i]);
			
			//System.out.println("writing WDPTN[" + i +"]:" + toTableString(WDPTN[i]));
			fw.write("WDPTN[" + i +"]:" + toTableString(WDPTN[i]));
			fw.write("\r\n");
			
			
			//System.out.println("writing WDTBL[" + i +"]:" + Integer.toString(WDTBL[i]));
			fw.write("WDTBL[" + i +"]:" + Integer.toString(WDTBL[i]));
			fw.write("\r\n");
			dos.writeByte(WDTBL[i]);
			
			for(int j = 0; j < 2; j++){
				for (int k = 0; k < 4; k++){

					//System.out.println("writing WDLINK[" + i +"][" +j +"][" + k +"]:" + Short.toString(WDLINK[i][j][k]));
					fw.write("WDLINK[" + i +"][" +j +"][" + k +"]:" + Short.toString(WDLINK[i][j][k]));
					fw.write("\r\n");
					dos.writeShort(WDLINK[i][j][k]);
				}
			}
		}
		
		dos.flush();
		dos.close();
		fw.flush();
		fw.close();
		
	}
	
	private String toTableString(long table) {
		long tvalue = table;
		int[][] t = new int[BOARD_WIDTH][BOARD_WIDTH];
		
		for(int i=3; i>=0; i--){
			for(int j = 3; j>=0; j--){
				t[i][j] = (int)(table & 7);
				table>>=3;
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for(int i=0; i<4; i++){
			sb.append("{");
			for(int j = 0; j<4; j++){
				sb.append(t[i][j]);
				if(j != 3){
					sb.append(" ");
				}
			}
			sb.append("}");
		}
		
		sb.append("}");
		sb.append("--");
		sb.append(Long.toString(tvalue));
		return sb.toString();
		
	}
	
	private void readFromFile(String filename) throws IOException{
		File file = new File(filename);
		if(!file.exists()){
			return;
		}
		
		File file2 = new File(filename + ".r.txt");
		if(!file2.exists()){
			file2.createNewFile();
		}
		
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		
		FileWriter fw = new FileWriter(file2);
		
		for(int i = 0; i<WDTBL_SIZE; i++){
			
			WDPTN[i] = dis.readLong();
			fw.write("WDPTN[" + i +"]:" + toTableString(WDPTN[i]));
			fw.write("\r\n");
			
			WDTBL[i] = dis.readByte();
			fw.write("WDTBL[" + i +"]:" + Integer.toString(WDTBL[i]));
			fw.write("\r\n");
			
			for(int j = 0; j< 2; j++){
				for (int k= 0; k<4; k++){
					WDLINK[i][j][k] = dis.readShort();
					fw.write("WDLINK[" + i +"][" +j +"][" + k +"]:" + Short.toString(WDLINK[i][j][k]));
					fw.write("\r\n");
				}
			}
		}
		
		dis.close();
		fw.close();
		
		
	}

	public static void main(String[] args){
		System.out.println("making......");
		WDDBCreator creator = new WDDBCreator();
		creator.simulation();
		System.out.println("saving......");
		String filename = "C:\\Users\\cqli\\Documents\\WD.db";
		try {
			creator.writeToFile(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("reading......");
		
		try {
			creator.readFromFile(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
