import blockchain.*;
import network.messageHandling.BlockMessageParser;


public class Main{
    public static void main(String[] args) throws Exception{
        Blockchain blockchain = new Blockchain();
        System.out.println(BlockMessageParser.BlockToJSON(blockchain.getLastBlock()));
    }
}