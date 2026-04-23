package org.example.limesurveyclient;

import java.util.List;

public class JsonRpcRequest {
    private String jsonrpc;
    private String method;
    private List<Object> params;
    private Object id;

    public JsonRpcRequest() {}

    public JsonRpcRequest(String jsonrpc, String method, List<Object> params, Object id) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = params;
        this.id = id;
    }

    public String getJsonrpc() { return jsonrpc; }
    public String getMethod() { return method; }
    public List<Object> getParams() { return params; }
    public Object getId() { return id; }

    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
    public void setMethod(String method) { this.method = method; }
    public void setParams(List<Object> params) { this.params = params; }
    public void setId(Object id) { this.id = id; }
}
