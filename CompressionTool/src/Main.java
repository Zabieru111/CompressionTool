import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String command = args[0];
        String filePath = args[1];
        File file = new File(filePath);
        if(command.equals("-e")){
            encode(file);
        }
        else if(command.equals("-d")){
            decode(file);
        }
        else{
            System.out.println("Commande invalide");
        }
    }
    private static void encode(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            HashMap<Character, Integer> map = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                for (int i = 0; i < line.length(); ++i) {
                    char c = line.charAt(i);
                    int value = map.getOrDefault(c, 0) + 1;
                    map.put(c, value);
                }
                map.put('\n', map.getOrDefault('\n', 0) + 1);
            }
            map.put('\1', 1);
            HashMap<Character, String> encoding = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            HuffManTreeNode node = createTree(map);
            traverse(node, encoding, sb);
            FileOutputStream pw = new FileOutputStream("Output/encoded");
            StringBuilder bitString = new StringBuilder();
            br = new BufferedReader(new FileReader(file));
            String header = createHeader(map);
            pw.write(header.getBytes());
            while ((line = br.readLine()) != null) {
                for (int i = 0; i < line.length(); ++i) {
                    char c = line.charAt(i);
                    bitString.append(encoding.get(c));
                }
                bitString.append(encoding.get(('\n')));
            }
            bitString.append(encoding.get('\1'));

            int bitStringLength = bitString.length();
            int byteLength = (bitStringLength + 7) / 8;  // Calculate the number of bytes needed

            byte[] bytes = new byte[byteLength];
            int byteIndex = 0;
            int bitIndex = 0;
            for (int i = 0; i < bitStringLength; i++) {
                if (bitString.charAt(i) == '1') {
                    bytes[byteIndex] |= (byte) (1 << (7 - bitIndex));
                }

                bitIndex++;

                if (bitIndex == 8) {
                    bitIndex = 0;
                    byteIndex++;
                }
            }
            for (byte aByte : bytes) {
                pw.write(aByte);
            }
            pw.close();
        }catch (Exception e){
            System.out.println("File not found");
        }
    }
    private static HuffManTreeNode createTree(HashMap<Character, Integer> map){
        HuffmanTree builder = new HuffmanTree();
        ArrayList<HuffManTreeNode>temp = new ArrayList<>();
        for(char c : map.keySet()){
            int frequency = map.get(c);
            int index= binaryAdd(map.get(c),temp);
            temp.add(index,new HuffManTreeNode(c,frequency,null,null));
        }
        while(temp.size()>1){
            HuffManTreeNode n1 = temp.remove(0);
            HuffManTreeNode n2 = temp.remove(0);
            HuffManTreeNode tmpNode = builder.build(n1,n2);
            temp.add(binaryAdd(tmpNode.frequency, temp),tmpNode);
        }
        return temp.get(0);
    }
    private static int binaryAdd(Integer i,ArrayList<HuffManTreeNode>a) {
        int left = 0;
        int right = a.size()-1;
        while(left<=right){
            int mid = (right+left)/2;
            int value = a.get(mid).frequency;
            if(i<value){
                right = mid-1;
            }
            else if(i>value){
                left = mid+1;
            }
            else{
                return mid;
            }
        }
        return left;
    }
    private static void traverse(HuffManTreeNode cur, HashMap<Character, String> map, StringBuilder sb){
        if(cur.character!='\0'){
            map.put(cur.character,sb.toString());
            return;
        }
        sb.append(0);
        traverse(cur.left,map,sb);
        sb.deleteCharAt(sb.length()-1).append(1);
        traverse(cur.right,map,sb);
        sb.deleteCharAt(sb.length()-1);
    }
    private static String createHeader(HashMap<Character,Integer>map){
        StringBuilder temp = new StringBuilder();
        temp.append(map.size()).append(' ');
        for(char c : map.keySet()){
            temp.append(c).append(map.get(c)).append(' ');
        }
        return temp.toString();
    }
    private static void decode(File path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            byte[] bytes = fis.readAllBytes();
            HashMap<Character, Integer> map = new HashMap<>();
            int i = 0;
            StringBuilder temp = new StringBuilder();
            while ((char) bytes[i] != ' ') {
                temp.append((char) bytes[i++]);
            }
            i++;
            int size = Integer.parseInt(temp.toString());
            int spaces = 0;
            while (spaces < size) {
                ArrayList<Byte> c = new ArrayList<>();
                c.add(bytes[i++]);
                char j;
                temp = new StringBuilder();
                while ((j = (char) bytes[i]) != ' ') {
                    if (j - '0' < 0 || j - '9' > 0) {
                        c.add(bytes[i]);
                    } else {
                        temp.append(j);
                    }
                    i++;
                }
                i++;
                byte[] tempByte = new byte[c.size()];
                for (int k = 0; k < c.size(); ++k) {
                    tempByte[k] = c.get(k);
                }
                char letter = new String(tempByte, StandardCharsets.UTF_8).charAt(0);
                int number = Integer.parseInt(temp.toString());
                map.put(letter, number);
                spaces++;
            }
            HuffManTreeNode node = createTree(map);
            HuffManTreeNode tempNode = node;
            StringBuilder text = new StringBuilder();
            int bitIndex = 7;
            while (tempNode.character != '\1') {
                if (tempNode.character != '\0') {
                    text.append(tempNode.character);
                    tempNode = node;
                }
                boolean isBitSet = (bytes[i] & (1 << bitIndex--)) != 0;
                if (isBitSet) {
                    tempNode = tempNode.right;
                } else {
                    tempNode = tempNode.left;
                }
                if (bitIndex < 0) {
                    bitIndex = 7;
                    i++;
                }
            }
            PrintWriter pw = new PrintWriter("Output/decoded.txt");
            pw.write(text.toString());
            pw.close();
        }catch (Exception e){
            System.out.println("File is not encoded in a valid way or is not found");
        }
    }
}
