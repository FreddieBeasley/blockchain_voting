/*
Function : stores a Queue containing votes that have been submitted but not added to a block yet

Functionality :
    - Does not need to be encrypted - votes cannot be altered PrivateKey
    - Needs to be persistent - votes should not be lost due to a system failure
 */
package blockchain;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import java.util.LinkedList;
import java.util.Queue;

public class PendingVotes {
    // Fields
    private final Queue<Vote> pendingVotes;

    // Local Variables
    File pendingVotesFile;

    // Initialisation
    public PendingVotes() {
        this.pendingVotesFile = new File("pendingVotes.txt");
        this.pendingVotes = new LinkedList<>();
    }

    public void addVote(Vote vote) {
        pendingVotes.add(vote);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("pendingVotes.txt", true)); // Appending
            bufferedWriter.write(vote.serialise());
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println("Error making pending votes persistent: " + e.getMessage());
        }
    }

    public boolean isEmpty() {
        return pendingVotes.isEmpty();
    }

    // poll in Java == pop in Python
    public Vote pollVote() {
        return pendingVotes.poll();
    }

    public Queue<Vote> getPendingVotes() {
        return pendingVotes;
    }

    public void clearPendingVotes() {
        pendingVotes.clear();
    }
}