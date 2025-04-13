package com.olahistvan.codec.controller;

import com.google.protobuf.util.JsonFormat;
import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.payload.codec.PayloadCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

@RestController
@CrossOrigin(origins = {"http://localhost:8233"}, maxAge = 3600) // replace the origin with your temporal url
@RequestMapping("/v1/codec")
public class CodecController {

    private static final Logger logger = LoggerFactory.getLogger(CodecController.class);

    private final PayloadCodec sharedCodec;
    private final List<PayloadCodec> allPayloadCodecs;

    public CodecController(@Qualifier("sharedPayloadCodec") PayloadCodec sharedPayloadCodec,
                           List<PayloadCodec> allPayloadCodecs) {
        this.sharedCodec = sharedPayloadCodec;
        this.allPayloadCodecs = allPayloadCodecs;
    }


    @PostMapping(path = "/decode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> decode(@RequestBody String requestBody) {
        return encodeOrDecode(requestBody, this::decodeAll);
    }

    private List<Payload> decodeAll(List<Payload> payloads) {
        return allPayloadCodecs.stream()
                .reduce(payloads, (current, codec) -> codec.decode(current), (a, b) -> b);
    }

    @PostMapping(path = "/encode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> encode(@RequestBody String requestBody) {
        return encodeOrDecode(requestBody, sharedCodec::encode);
    }

    private ResponseEntity<String> encodeOrDecode(String requestBody, Function<List<Payload>, List<Payload>> encodeOrDecodeFunction) {
        try {
            Payloads.Builder incomingPayloads = Payloads.newBuilder();
            JsonFormat.parser().merge(requestBody, incomingPayloads);

            List<Payload> incomingPayloadsList = incomingPayloads.build().getPayloadsList();
            List<Payload> outgoingPayloadsList = encodeOrDecodeFunction.apply(incomingPayloadsList);

            final String responseBody = JsonFormat.printer()
                    .print(Payloads.newBuilder()
                            .addAllPayloads(outgoingPayloadsList)
                            .build());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Failed to decode payload: " + e.getMessage());
        }
    }
}