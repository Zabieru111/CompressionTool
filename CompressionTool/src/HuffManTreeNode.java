
public class HuffManTreeNode {
    public char character;
    public int frequency;

    public HuffManTreeNode left;
    public HuffManTreeNode right;
    public  HuffManTreeNode(char c,int f,HuffManTreeNode left,HuffManTreeNode right){
        this.character = c;
        this.frequency = f;
        this.left = left;
        this.right = right;
    }
}
