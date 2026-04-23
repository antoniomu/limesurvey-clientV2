package org.example.limesurveyclient;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JsonRpcRequestTest {
    @Test
    public void serialization() throws Exception {
        JsonRpcRequest r = new JsonRpcRequest("2.0", "test", List.of("a", 1), 1);
        assertEquals("2.0", r.getJsonrpc());
        assertEquals("test", r.getMethod());
        assertEquals(1, r.getId());
    }
}
