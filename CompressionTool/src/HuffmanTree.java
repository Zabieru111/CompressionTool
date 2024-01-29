
public class HuffmanTree {
    public HuffmanTree(){
    }
    public HuffManTreeNode build(HuffManTreeNode n1,HuffManTreeNode n2){
        return new HuffManTreeNode('\0', n1.frequency+ n2.frequency,n1,n2);
    }
}
