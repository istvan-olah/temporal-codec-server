package com.olahistvan.codec.key.components.impl;

import com.olahistvan.codec.key.components.KeyRotator;

public class DefaultKeyRotator implements KeyRotator {

    @Override
    public void rotateCurrentKey() {
        throw new UnsupportedOperationException("Key rotation is not supported in this implementation.");
    }
}
