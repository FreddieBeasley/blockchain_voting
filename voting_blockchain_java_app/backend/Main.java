import blockchain.*;
import util.ParserUtils;


public class Main{
    public static void main(String[] args) throws Exception{
        Blockchain blockchain = new Blockchain(); // will attempt to load all for
        System.out.println(ParserUtils.BlockToJSON(blockchain.getLastBlock()));
    }
}