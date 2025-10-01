import java.security.*;
import javax.crypto.Cipher;
import java.util.*;

public class AnonymousVoting {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Map<String, byte[]> encryptedVotes = new HashMap<>();

    public AnonymousVoting() throws Exception {
        generateKeyPair();
    }

    private void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
    }

    public void castVote(String voterIdHash, String vote) throws Exception {
        if (encryptedVotes.containsKey(voterIdHash)) {
            System.out.println("Voter has already voted.");
            return;
        }

        byte[] encryptedVote = encryptVote(vote);
        encryptedVotes.put(voterIdHash, encryptedVote);
        System.out.println("Vote cast successfully.");
    }

    private byte[] encryptVote(String vote) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(vote.getBytes());
    }

    public void tallyVotes() throws Exception {
        Map<String, Integer> results = new HashMap<>();
        for (byte[] encryptedVote : encryptedVotes.values()) {
            String vote = decryptVote(encryptedVote);
            results.put(vote, results.getOrDefault(vote, 0) + 1);
        }

        System.out.println("Voting Results:");
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private String decryptVote(byte[] encryptedVote) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(encryptedVote));
    }

    public static String hashVoterId(String voterId) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(voterId.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public static void main(String[] args) throws Exception {
        AnonymousVoting voting = new AnonymousVoting();

        String voter1 = hashVoterId("voterA@example.com");
        String voter2 = hashVoterId("voterB@example.com");

        voting.castVote(voter1, "Option A");
        voting.castVote(voter2, "Option B");
        voting.castVote(voter1, "Option C"); // bị từ chối vì đã bỏ phiếu

        voting.tallyVotes();
    }
}

