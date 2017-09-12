package Assembler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

public class Pass2GHI {

	Pass1GUI pass1GUI = new Pass1GUI();
	Handle handle = new Handle();
	private Hashtable<String, String> opTab;
	private Hashtable<String, String> symTab;
	private Hashtable<String, String> litTab;
	private LinkedList<String> locTab;
	private ArrayList<String> lines;
	private ArrayList<String> records;
	private ArrayList<String> sic = new ArrayList<>();
	private ArrayList<String> listFile = new ArrayList<>();
	private ArrayList<String> unHandleLines = new ArrayList<>();
	private ArrayList<String> intermediateFile = new ArrayList<>();
	private String firstLocation;
	private boolean lengthFlag = false;

	public Pass2GHI(ArrayList<String> LINES) {
		pass1GUI.Start(LINES);
		Start();
		buildFormat();
		listFile();
		// ToUpperCase();
		records = handle.convertToUpper(records);
		listFile = handle.convertToUpper(listFile);
		writeRecordFile();
		writeListingFile();
		// print();
	}

	public ArrayList<String> getRecords() {
		return records;
	}

	public ArrayList<String> getUnHandleLines() {
		return unHandleLines;
	}

	public ArrayList<String> getIntermediateFile() {
		return intermediateFile;
	}

	public ArrayList<String> getListFile() {
		return listFile;
	}

	private void currentLocationModify(String opCode, String operand, String location) {
		String objectCode = "";
		objectCode = objectCode + opTab.get(opCode);
		if (operand.equals("*")) {
			location = handle.chechLength(location, 4);
			objectCode = objectCode + location;
		}
		sic.add(objectCode);
	}

	private void modify(String opCode, String operand) {
		String objectCode = "";
		objectCode = objectCode + opTab.get(opCode);
		String test = operand.substring(operand.length() - 2, operand.length());
		String test2 = operand.substring(0, 2);
		if (test.equals(",x")) {
			String obcode = symTab.get(operand.substring(0, operand.length() - 2));
			int num1 = Integer.parseInt(obcode, 16);
			int num2 = Integer.parseInt("8000", 16);
			objectCode = objectCode + Integer.toHexString(num1 + num2);
		} else if (test2.equals("0x")) {
			objectCode = objectCode + handle.chechLength(literalHandle(operand), 4);
		} else {
			if (symTab.containsKey(operand)) {
				String operandH = symTab.get(operand);
				operandH = handle.chechLength(operandH, 4);
				objectCode = objectCode + operandH;
			} else if (litTab.containsKey(operand)) {
				objectCode = objectCode + handle.chechLength(litTab.get(operand), 4);
			}
		}
		sic.add(objectCode);
	}

	public void Start() {
		opTab = pass1GUI.getOpTab();
		symTab = pass1GUI.getSymTab();
		locTab = pass1GUI.getLocTab();
		lines = pass1GUI.getLines();
		litTab = pass1GUI.getLitTab();
		records = new ArrayList<>();
		intermediateFile = pass1GUI.getIntermediateFile();
		unHandleLines = pass1GUI.getUnHandleLines();
		sic = new ArrayList<>();
		listFile = new ArrayList<>();
		lengthFlag = false;
		firstLocation = handle.chechLength(locTab.getFirst(), 6);
	}

	private void startHandle(String label, String operand) {
		int num1 = Integer.parseInt(locTab.getFirst(), 16);
		int num2 = Integer.parseInt(locTab.getLast(), 16);

		String length = Integer.toHexString(num2 - num1);
		length = handle.chechLength(length, 6);
		operand = handle.chechLength(operand, 6);
		label = handle.rehandleLable(label);
		String headerRecode = "H" + label + "^" + operand + "^" + length;
		records.add(headerRecode);
	}

	public void buildFormat() {
		int index = 0;
		String line = lines.get(index);
		String label = pass1GUI.getLabel(line);
		String opCode = pass1GUI.getOpCode(line);
		String operand = pass1GUI.getOperand(line);
		String location = locTab.get(index);
		String lastOperand = "";
		String subOperand = "";
		boolean errorFlag = false;

		if (opCode.equals("start")) {
			startHandle(label, subOperand);
			index++;
		}
		String textRecord = "T" + firstLocation + "^";
		int sizeTextRecord = 0;
		while (/* !opCode.equals("end")|| */index < lines.size()) {
			line = lines.get(index);
			label = pass1GUI.getLabel(line);
			opCode = pass1GUI.getOpCode(line);
			operand = pass1GUI.getOperand(line);
			location = locTab.get(index);
			if (label.equals("") || label.charAt(0) != '.') {
				if (opTab.containsKey(opCode)) {
					if (operand.length() > 2) {
						lastOperand = operand.substring(operand.length() - 2, operand.length());
						subOperand = operand.substring(0, operand.length() - 2);
					}
					if ((lastOperand.equals(",x") && (symTab.containsKey(subOperand) || litTab.containsKey(subOperand)))
							|| symTab.containsKey(operand) || litTab.containsKey(operand) || operand.equals("*")) {
						if (!operand.equals("*"))
							modify(opCode, operand);
						else if (operand.equals("*"))
							currentLocationModify(opCode, operand, location);
					} else {
						// undefined symbols
						errorFlag = true;
						String objectcode = "";
						objectcode = objectcode + opTab.get(opCode) + "0000";
						sic.add(objectcode);
					}
				} else if (opCode.equals("word"))
					wordHandle(operand);
				else if (opCode.equals("byte"))
					byteHandle(operand);
				else if (opCode.equals("resw") || opCode.equals("resb") || opCode.equals("equ") || opCode.equals("end")
						|| opCode.equals("org") || opCode.equals("ltorg")) {
					index++;
					continue;
				} else if ((opCode.substring(0, 2).equals("=x") || opCode.substring(0, 2).equals("=c")
						|| opCode.substring(0, 2).equals("=w")) && label.equals("*")) {
					String objectCode = literalHandle(opCode);
					sic.add(objectCode);
				} else {
					System.out.println("Error in label in line: " + index);
					System.exit(0);
				}
				LinkedList<Object> rRecords = recordHandle(textRecord, sizeTextRecord, location);
				textRecord = new String();
				sizeTextRecord = 0;
				textRecord = (String) rRecords.get(0);
				sizeTextRecord = (int) rRecords.get(1);
			}
			if (lengthFlag)
				break;
			index++;
		}
		records.add(textRecord);
		String endRecord = "E" + firstLocation;
		records.add(endRecord);
		if (errorFlag) {
			// System.out.println("Errorrrrrrrrrrrrrrrrrrrrrrr");
		}
		if (lengthFlag) {
			System.out.println("Error in length in line " + index);
		}
	}

	private void wordHandle(String operand) {
		String objectCode = "";
		if (Character.isDigit(operand.charAt(0))) {
			int num1 = Integer.parseInt(operand);
			objectCode = Integer.toHexString(num1);
			objectCode = handle.chechLength(objectCode, 6);
			sic.add(objectCode);
		} else {
			if (symTab.containsKey(operand)) {
				objectCode = symTab.get(operand);
				objectCode = handle.chechLength(objectCode, 6);
				sic.add(objectCode);
			} else {
				System.out.println("Error undefinrd Label");
				System.exit(0);
			}
		}
	}

	private LinkedList<Object> recordHandle(String textRecord, int sizeTextRecord, String location) {
		if (sizeTextRecord + sic.get(sic.size() - 1).length() <= 60) {
			sizeTextRecord += sic.get(sic.size() - 1).length();
			String lenght = Integer.toHexString(sizeTextRecord / 2);
			lenght = handle.chechLength(lenght, 2);
			if (textRecord.length() < 9)
				textRecord = textRecord + handle.chechLength(lenght, 2) + "^" + sic.get(sic.size() - 1);
			else
				textRecord = textRecord.substring(0, 8) + handle.chechLength(lenght, 2)
						+ textRecord.substring(10, textRecord.length()) + sic.get(sic.size() - 1);
		} else {
			records.add(textRecord);
			textRecord = "T" + handle.chechLength(location, 6) + "^";
			sizeTextRecord = sic.get(sic.size() - 1).length();
			String lenght = Integer.toHexString(sizeTextRecord / 2);
			textRecord = textRecord.substring(0, 8) + handle.chechLength(lenght, 2) + "^" + sic.get(sic.size() - 1);
		}
		LinkedList<Object> rRecord = new LinkedList<>();
		rRecord.add(textRecord);
		rRecord.add(sizeTextRecord);
		return rRecord;
	}

	private void byteHandle(String operand) {
		String objectCode = "";
		if (operand.charAt(0) == 'x') {
			objectCode = operand.substring(2, operand.length() - 1);
		} else if (operand.charAt(0) == 'c') {
			operand = operand.substring(2, operand.length() - 1);
			objectCode = handle.toHex(operand);
		}
		if (objectCode.length() > 6)
			lengthFlag = true;
		sic.add(objectCode);
	}

	private String literalHandle(String operand) {
		String objectCode = "";
		if (operand.substring(0, 2).equals("0x")) {
			objectCode = operand.substring(3, operand.length() - 1);
			if (objectCode.length() > 4)
				lengthFlag = true;
		}
		if (operand.substring(0, 2).equals("=x")) {
			objectCode = operand.substring(3, operand.length() - 1);
		} else if (operand.substring(0, 2).equals("=c") || operand.substring(0, 2).equals("=w")) {
			operand = operand.substring(3, operand.length() - 1);
			objectCode = handle.toHex(operand);
		}
		if (objectCode.length() > 6)
			lengthFlag = true;
		return objectCode;
	}

	private void print() {
		if (lengthFlag)
			return;
		for (int i = 0; i < records.size(); i++) {
			System.out.println(records.get(i));
		}
		System.out.println();
		for (int i = 0; i < listFile.size(); i++) {
			System.out.println(listFile.get(i));
		}

	}

	public void writeRecordFile() {
		String url = "D:\\workshop\\Records.txt";
		BufferedWriter outputWriter = null;
		try {
			outputWriter = new BufferedWriter(new FileWriter(url));
			for (int i = 0; i < records.size(); i++) {
				outputWriter.write(records.get(i));
				outputWriter.newLine();
			}
			outputWriter.flush();
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeListingFile() {
		String url = "D:\\workshop\\ListingFile.txt";
		BufferedWriter outputWriter = null;
		try {
			outputWriter = new BufferedWriter(new FileWriter(url));
			for (int i = 0; i < listFile.size(); i++) {
				outputWriter.write(listFile.get(i));
				outputWriter.newLine();
			}
			outputWriter.flush();
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listFile() {
		if (lengthFlag)
			return;
		int index = 0;
		for (int i = 0; i < lines.size(); i++) {
			String listLine = "";
			String line = lines.get(i);
			String opCode = pass1GUI.getOpCode(line);
			if (opCode.equals("start") || opCode.equals("resw") || opCode.equals("resb") || opCode.equals("end")
					|| opCode.equals("equ") || opCode.equals("org") || opCode.equals("ltorg")) {
				listLine = listLine + handle.chechLength(locTab.get(i), 6) + "            " + line;
				listFile.add(listLine);
			} else {
				listLine = listLine + handle.chechLength(locTab.get(i), 6) + "   "
						+ handle.rehandleLable(sic.get(index)) + "   " + line;
				index++;
				listFile.add(listLine);
			}
		}
	}
}
