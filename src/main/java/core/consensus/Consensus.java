
package core.consensus;

import chainUtil.ChainUtil;
import chainUtil.KeyGenerator;
import core.blockchain.Block;
import core.blockchain.Blockchain;
import core.blockchain.Transaction;
import core.blockchain.Validation;
import core.communicationHandler.MessageSender;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Consensus {

    private static Consensus consensus;
    ArrayList<AgreementCollector> agreementCollectors;
    ArrayList<Transaction> agreedTransactiions;

    private Consensus() {
        agreedTransactiions = new ArrayList<>();
        agreementCollectors = new ArrayList<>();
    }

    public static Consensus getInstance() {
        if(consensus == null) {
            consensus = new Consensus();
        }
        return consensus;
    }


    public boolean requestAgreementForBlock(Block block) {
        ArrayList<String> validators = new ArrayList<>();
       // Validation[] validations = block.getTransaction().getValidations(); //changed
        ArrayList<Validation> validations = block.getTransaction().getValidations();
        for(Validation validation: validations) {
            validators.add(validation.getValidator().getValidator());
        }

        //send the block to validators in the validators array for agreements
        return true;
    }

    public boolean responseForBlockAgreement(Block block, String agreed, int neighbourIndex) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException, SignatureException, InvalidKeyException {
        if(agreedTransaction(block.getTransaction())) {
            sendAgreementForBlock(block,agreed,neighbourIndex);
            return true;
        }
        return false;
    }

    public boolean sendAgreementForBlock(Block block, String agreed, int neighbourIndex) throws
            InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException, SignatureException, InvalidKeyException {
        MessageSender.getInstance().sendAgreement(block,1,agreed,
                ChainUtil.sign(KeyGenerator.getInstance().getPrivateKey(),agreed));
        return true;
    }

    public boolean checkAgreementForBlock (Block block) {

        if(agreedTransactiions.contains(block.getTransaction())){ //changed
            return true;
        }
        return false;
    }

    public boolean agreedTransaction(Transaction transaction) {
        if (!agreedTransactiions.contains(transaction)) {
            agreedTransactiions.add(transaction);
            return true;
        } else {
            return false;
        }
    }

    public boolean addAgreedNodeForBlock(Block block, PublicKey agreedNode) throws NoSuchAlgorithmException {
        boolean status = getAgreementCollectorByBlock(block).addAgreedNode(agreedNode);
        if(status) {
            checkForEligibilty(block);
            return true;
        }
        return false;
    }

    public AgreementCollector getAgreementCollectorByBlock(Block block) throws NoSuchAlgorithmException {
        String id = AgreementCollector.generateAgreementCollectorId(block);
        for(int i = 0; i< agreementCollectors.size(); i++) {
            if(agreementCollectors.get(i).getId() == id) {
                return agreementCollectors.get(i);
            }
        }
        return null;
    }

    public boolean insertBlock(Block block) {
        long receivedBlockNumber = block.getHeader().getBlockNumber();
        Timestamp receivedBlockTimestamp = block.getHeader().getTimestamp();

        if (!blockExistence(block)) {
            Blockchain.getBlockchain().addBlock(block);
            return true;
        } else {
            Block existBlock = Blockchain.getBlockchain().getBlockByNumber(receivedBlockNumber);

            if (existBlock.getHeader().getTimestamp().after(receivedBlockTimestamp)) {
                Blockchain.getBlockchain().rollBack(receivedBlockNumber);
                Blockchain.getBlockchain().addBlock(block);
                return true;
            } else if (existBlock.getHeader().getTimestamp() == receivedBlockTimestamp) {
                Blockchain.getBlockchain().rollBack(receivedBlockNumber);
                return false;
            }
            return false;
        }
    }

    public boolean blockExistence(Block block) {
        if(Blockchain.getBlockchain().getBlockchainArray().size() > block.getHeader().getBlockNumber()) {
            return true;
        }
        return false;
    }

    public void BlockHandler(Block block) {
        if(agreedTransaction(block.getTransaction())) {
            System.out.println("Agreed block");
            insertBlock(block);
        }else {
            System.out.println("Not agreed block. requesting agreements");
            requestAgreementForBlock(block);
        }
    }

    public void checkForEligibilty(Block block) throws NoSuchAlgorithmException {
        int threshold = 1; //get from the predefined rules

        if(getAgreementCollectorByBlock(block).getAgreedNodesCount() == threshold) {
            System.out.println("Agreements received upto threshold level");
            insertBlock(block);
        }
    }

}
