package org.example.limesurveyclient;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonRpcResponse {
    private String jsonrpc;
    private JsonNode result;
    private JsonNode error;
    private Object id;

    public JsonRpcResponse() {}

    public String getJsonrpc() { return jsonrpc; }
    public JsonNode getResult() { return result; }
    public JsonNode getError() { return error; }
    public Object getId() { return id; }

    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
    public void setResult(JsonNode result) { this.result = result; }
    public void setError(JsonNode error) { this.error = error; }
    public void setId(Object id) { this.id = id; }
}
