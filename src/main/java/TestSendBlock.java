import Exceptions.FileUtilityException;
import config.CommonConfigHolder;
import constants.Constants;
import network.Client.RequestMessage;
import network.Node;
import network.Protocol.BlockMessageCreator;
import org.json.JSONObject;
import org.slf4j.impl.SimpleLogger;

public class TestSendBlock {
    public static void main(String[] args) throws FileUtilityException {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

        /*
        * Set the main directory as home
        * */
        System.setProperty(Constants.CARBC_HOME, System.getProperty("user.dir"));

        /*
        * At the very beginning
        * A Config common to all: network, blockchain, etc.
        * */
        CommonConfigHolder commonConfigHolder = CommonConfigHolder.getInstance();
        commonConfigHolder.setConfigUsingResource("peer1");

        /*
        * when initializing the network
        * */
        Node node = Node.getInstance();
        node.init();

        /*
        * when we want our node to start listening
        * */
        node.startListening();

        /*
        * when we want to send a block
        * */
//        JSONObject ourBlock = new JSONObject();
//        ourBlock.put("someField", "someValue");
//        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(ourBlock);
//        blockMessage.addHeader("keepActive", "false");
//        node.sendMessageToNeighbour(0, blockMessage);
    }
}
