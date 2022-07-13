package correcter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    enum Operation {
        encode,
        send,
        decode
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        StringBuilder line;
        System.out.print("Write a mode: ");
        Operation op = Operation.valueOf(scanner.next());

        switch (op) {
            case encode:
                line = new StringBuilder(readStringFromFile("send.txt"));
                System.out.println("text view: " + line);
                line = convertStringToBinary(line);
                System.out.println("bin view: " + line);
                line = binaryEncoderHamming(line);
                System.out.println("hamming bin: " + line);
                writeBynaryInFile("encoded.txt", line.toString());
                break;
            case send:
                line = readBytesFromFile("encoded.txt");
                System.out.println("hamming bin: " + line);
                line = makeErrorInBytes(line);
                System.out.println("error bin: " + line);
                writeBynaryInFile("received.txt", line.toString());
                break;
            case decode:
                line = readBytesFromFile("received.txt");
                System.out.println("error bin: " + line);
                line = binaryDecoderHamming(line);
                System.out.println("decoded bin: " + line);
                line = convertBinaryToString(line);
                System.out.println("text view: " + line);
                writeStringInFile("decoded.txt", line.toString());
                break;
            default:
                System.out.println("Error");
        }
    }

    public static StringBuilder readBytesFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        byte[] array = inputStream.readAllBytes();
        StringBuilder result = new StringBuilder();
        for (byte b : array) {
            result.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return result;
    }

    public static StringBuilder readStringFromFile(String filePath) throws IOException {
        return new StringBuilder(new String(Files.readAllBytes(Paths.get(filePath))));
    }

    public static void writeBynaryInFile(String filePath, String s) throws IOException {
        File file = new File(filePath);
        OutputStream outputStream = new FileOutputStream(file, false);

        for (int i = 0; i < s.length(); i += 8) {
            outputStream.write((byte)Integer.parseInt(s.substring(i, i + 8), 2));
        }


        outputStream.close();
    }

    public static void writeStringInFile(String filePath, String s) throws IOException {
        File file = new File(filePath);
        FileWriter fileWriter = new FileWriter(file);

        fileWriter.write(s);
        fileWriter.close();
    }

    public static StringBuilder binaryEncoderHamming(StringBuilder s){
        StringBuilder result = new StringBuilder();

        int counter = 0;
        int iterator = 0;

        while (counter < s.length()) {
            if (iterator % 8 == 0
                || iterator % 8 == 1
                || iterator % 8 == 3
                || iterator % 8 == 7) {
                result.append(".");
            } else {
                result.append(s.charAt(counter));
                counter++;
            }
            iterator++;
        }
        result.append('.');


        int onesCounter = 0;

        for (int i = 0; i < result.length(); i += 8) {
            for (int j = i; j < 8 + i; j += 2) {
                if (result.charAt(j) == '1') {
                    onesCounter++;
                }
            }
            if (onesCounter % 2 == 0) {
                result.setCharAt(i, '0');
            } else {
                result.setCharAt(i, '1');
            }
            onesCounter = 0;

            for (int j = i + 1; j < 8 + i; j += 4) {
                if (result.charAt(j) == '1') {
                    onesCounter++;
                }
                if (result.charAt(j + 1) == '1') {
                    onesCounter++;
                }
            }
            if (onesCounter % 2 == 0) {
                result.setCharAt(i + 1, '0');
            } else {
                result.setCharAt(i + 1, '1');
            }
            onesCounter = 0;

            for (int j = i + 3; j < 7 + i; j++) {
                if (result.charAt(j) == '1') {
                    onesCounter++;
                }
            }
            if (onesCounter % 2 == 0) {
                result.setCharAt(i + 3, '0');
            } else {
                result.setCharAt(i + 3, '1');
            }
            onesCounter = 0;

            result.setCharAt(i + 7, '0');
        }
        return result;
    }

    public static StringBuilder binaryDecoderHamming(StringBuilder s) {
        StringBuilder result = new StringBuilder();
        StringBuilder buf = new StringBuilder(s);
        int offset = 1;
        int onesCounter = 0;
        int[] errorBits = new int[4];
        int sumBit = 0;

        for (int i = 0; i < s.length(); i += 8) {
            for (int iter = 0; iter < 3; iter++) {
                for (int j = i + offset - 1; j < i + 8; j += offset * 2) {
                    for (int k = j; k <= j + offset - 1; k++) {
                        if (k != i + offset - 1 && s.charAt(k) == '1') {
                            onesCounter++;
                        }
                    }
                }
                if (onesCounter % 2 != Character.getNumericValue(s.charAt(i + offset - 1))) {
                    errorBits[offset - 1] = offset;
                }
                offset *= 2;
                onesCounter = 0;
            }

            for (int aInt : errorBits) {
                sumBit += aInt;
            }
            errorBits = new int[4];
            if (sumBit > 0) {
                if (buf.charAt(i + sumBit - 1) == '1') {
                    buf.setCharAt(i + sumBit - 1, '0');
                } else {
                    buf.setCharAt(i + sumBit - 1, '1');
                }
            }
            sumBit = 0;
            offset = 1;
        }

        for (int i = 0; i < buf.length(); i++) {
            if (i % 8 == 2
                || i % 8 == 4
                || i % 8 == 5
                || i % 8 == 6) {
                result.append(buf.charAt(i));
            }
        }

        return result;
    }

    public static StringBuilder convertStringToBinary(StringBuilder s) {

        StringBuilder result = new StringBuilder();
        char[] chars = s.toString().toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))
                            .replaceAll(" ", "0")
            );
        }
        return new StringBuilder(result.toString());

    }

    public static StringBuilder convertBinaryToString(StringBuilder s) {
        StringBuilder result = new StringBuilder();
        StringBuilder buf = new StringBuilder();
        char[] array = s.toString().toCharArray();

        for (int i = 0; i < array.length; i += 8) {
            for (int j = 0; j < 8; j++) {
                buf.append(array[j + i]);
            }
            result.append((char)Integer.parseInt(buf.toString(), 2));
            buf = new StringBuilder();
        }
        return result;
    }


    public static StringBuilder makeErrorInBytes(StringBuilder s) {
        StringBuilder result = new StringBuilder(s);
        Random random = new Random();
        for (int i = 0; i < s.length(); i += 8) {
            int randomSymbol = random.nextInt(8);
            if (result.charAt(i + randomSymbol) == '0') {
                result.setCharAt(i + randomSymbol, '1');
            } else {
                result.setCharAt(i + randomSymbol, '0');
            }
        }
        return result;
    }
}
