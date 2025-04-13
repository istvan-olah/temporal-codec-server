package com.olahistvan.codec.key.components;

public interface KeyGenerator {

    CodecKey generateNewKey();


    class CodecKey {
        private final String id;
        private final String value;

        public CodecKey(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

    }
}
