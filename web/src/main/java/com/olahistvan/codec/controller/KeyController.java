package com.olahistvan.codec.controller;

import com.olahistvan.codec.key.components.KeyRotator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/keys")
public class KeyController {

    private final List<KeyRotator> keyRotators;

    public KeyController(List<KeyRotator> keyRotators) {
        this.keyRotators = keyRotators;
    }

    @PostMapping(path = "/rotate", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rotate() {
        keyRotators.forEach(KeyRotator::rotateCurrentKey);
    }

}
