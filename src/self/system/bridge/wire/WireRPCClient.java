package org.self.system.bridge.wire;

import java.io.IOException;
import java.util.Map;
import org.json.JSONObject;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.utils.SelfLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WireRPCClient {
    private static final String CHAIN_API = "chain";
    private static final String NET_API = "net";
    private static final String PRODUCER_API = "producer";
    private static final String DB_SIZE_API = "db_size";
    
    private final OkHttpClient client;
    private final String endpoint;
    private final String apiKey;
    
    public WireRPCClient(String endpoint, String apiKey) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }
    
    /**
     * Send a request to Wire Network's RPC API
     */
    private JSONObject sendRequest(String api, String method, Map<String, Object> params) throws IOException {
        JSONObject request = new JSONObject();
        request.put("jsonrpc", "2.0");
        request.put("id", 1);
        request.put("method", method);
        if (params != null) {
            request.put("params", new JSONObject(params));
        }
        
        Request.Builder builder = new Request.Builder()
            .url(endpoint + "/v1/" + api)
            .post(RequestBody.create(
                MediaType.parse("application/json"),
                request.toString()
            ));
            
        if (apiKey != null && !apiKey.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + apiKey);
        }
        
        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            return new JSONObject(response.body().string());
        }
    }
    
    /**
     * Get blockchain information
     */
    public JSONObject getBlockchainInfo() throws IOException {
        return sendRequest(CHAIN_API, "get_info", null);
    }
    
    /**
     * Push transaction to Wire Network
     */
    public JSONObject pushTransaction(MiniData zTxID, MiniNumber zAmount, String zDestination) throws IOException {
        Map<String, Object> params = Map.of(
            "transaction", Map.of(
                "actions", new Object[]{
                    Map.of(
                        "account", "wire.token",
                        "name", "transfer",
                        "authorization", new Object[]{
                            Map.of(
                                "actor", "self.chain",
                                "permission", "active"
                            )
                        },
                        "data", Map.of(
                            "from", "self.chain",
                            "to", zDestination,
                            "quantity", zAmount.toString(),
                            "memo", "Wire Network Transfer"
                        )
                    )
                }
            )
        );
        return sendRequest(CHAIN_API, "push_transaction", params);
    }
    
    /**
     * Get transaction status
     */
    public JSONObject getTransactionStatus(MiniData zTxID) throws IOException {
        Map<String, Object> params = Map.of(
            "id", zTxID.toString()
        );
        return sendRequest(CHAIN_API, "get_transaction", params);
    }
    
    /**
     * Validate Wire Network address
     */
    public boolean validateAddress(String zAddress) throws IOException {
        Map<String, Object> params = Map.of(
            "account_name", zAddress
        );
        try {
            sendRequest(CHAIN_API, "get_account", params);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get node status
     */
    public JSONObject getNodeStatus() throws IOException {
        return sendRequest(NET_API, "get_info", null);
    }
    
    /**
     * Get network peers
     */
    public JSONObject getPeers() throws IOException {
        return sendRequest(NET_API, "get_peers", null);
    }
    
    /**
     * Get producer status
     */
    public JSONObject getProducerStatus() throws IOException {
        return sendRequest(PRODUCER_API, "get_producer", null);
    }
    
    /**
     * Get database size
     */
    public JSONObject getDatabaseSize() throws IOException {
        return sendRequest(DB_SIZE_API, "get", null);
    }
}
