package blockchain;

import java.util.ArrayList;
import java.util.List


public class Chain{
    private List<Block> Blockchain;
    private int difficulty;

    public Chain(){
        this.Blockchain=new ArrayList<Block>();
        this.difficulty=4;
    }

    public Chain(List<Block> Blockchain, int difficulty){
        this.Blockchain=Blockchain;
        this.difficulty=difficulty;
    }



}