package network.messageHandling;

import org.json.JSONObject;
import network.nodes.RemoteNode;
import java.security.PublicKey;
import util.CryptographyUtils;

public class RemoteNodeMessageParser {
    public static RemoteNode JSONObjectToNode(JSONObject data) throws Exception {
        String host = data.getString("host");
        int port = data.getInt("port");
        PublicKey publicKey = CryptographyUtils.stringToPublicKey(data.getString("publicKey"));

        return new RemoteNode(host, port, publicKey);
    }

    public static JSONObject nodeToJSONObject(RemoteNode node) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("host", node.getHost());
        jsonObject.put("port", node.getPort());
        jsonObject.put("publicKey", CryptographyUtils.publicKeyToString(node.getPublicKey()));

        return jsonObject;
    }
}