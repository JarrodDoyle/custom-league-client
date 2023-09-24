package com.hawolt.client.cache;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created: 12/09/2023 08:30
 * Author: Twitter @hawolt
 **/

public class JWT {
    private final byte[] headerBytes, payloadBytes, signatureBytes;
    private final JSONObject header, payload;

    public JWT(String plain) {
        String[] compartments = plain.split("\\.");
        this.headerBytes = Base64.getUrlDecoder().decode(compartments[0]);
        this.payloadBytes = Base64.getUrlDecoder().decode(compartments[1]);
        this.signatureBytes = Base64.getUrlDecoder().decode(compartments[2]);
        this.header = new JSONObject(new String(headerBytes, StandardCharsets.UTF_8));
        this.payload = new JSONObject(new String(payloadBytes, StandardCharsets.UTF_8));
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= getExpirationTime();
    }

    public String getAlgorithm() {
        return header.getString("alg");
    }

    public long getIssuedAt() {
        if (!payload.has("iat")) return System.currentTimeMillis();
        return payload.getLong("iat") * 1000;
    }

    public long getExpirationTime() {
        if (!payload.has("exp")) return Long.MAX_VALUE;
        return payload.getLong("exp") * 1000;
    }

    public String getIssuer() {
        return payload.getString("iss");
    }

    public byte[] getHeaderBytes() {
        return headerBytes;
    }

    public byte[] getPayloadBytes() {
        return payloadBytes;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    public JSONObject getHeader() {
        return header;
    }

    public JSONObject getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return String.join(
                ".",
                new String(Base64.getUrlEncoder().encode(headerBytes)).split("=")[0],
                new String(Base64.getUrlEncoder().encode(payloadBytes)).split("=")[0],
                new String(Base64.getUrlEncoder().encode(signatureBytes)).split("=")[0]
        );
    }
}
