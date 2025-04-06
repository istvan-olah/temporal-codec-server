package com.olahistvan.codec.controller;

import com.olahistvan.codec.keyprovider.KeyProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/keys")
public class KeyController {

    private final KeyProvider keyProvider;

    public KeyController(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @PostMapping(path = "/rotate", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rotate() {
        keyProvider.rotateCurrentKey();
    }

    @PostMapping(path = "/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public void reset() {
        keyProvider.reset();
    }


}