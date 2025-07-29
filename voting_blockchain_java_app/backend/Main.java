import blockchain.*;
import util.ParserUtils;

public class Main{
    public static void main(String[] args) throws Exception{
        Blockchain blockchain = new Blockchain();
        System.out.println(ParserUtils.BlockToJSON(blockchain.getLastBlock()));
    }
}