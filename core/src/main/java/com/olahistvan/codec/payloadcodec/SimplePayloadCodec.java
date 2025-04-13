package com.olahistvan.codec.payloadcodec;

import com.google.protobuf.ByteString;
import com.olahistvan.codec.key.components.KeyProvider;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.converter.DataConverterException;
import io.temporal.common.converter.EncodingKeys;
import io.temporal.payload.codec.PayloadCodec;
import io.temporal.payload.codec.PayloadCodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple implementation of the PayloadCodec interface for encoding and decoding payloads using AES/GCM encryption.
 */
public class SimplePayloadCodec implements PayloadCodec {
    private static final Logger logger = LoggerFactory.getLogger(SimplePayloadCodec.class);

    static final ByteString METADATA_ENCODING =
            ByteString.copyFrom("binary/encrypted", StandardCharsets.UTF_8);

    private static final String CIPHER = "AES/GCM/NoPadding";

    static final String METADATA_ENCRYPTION_CIPHER_KEY = "encryption-cipher";
    static final ByteString METADATA_ENCRYPTION_CIPHER =
            ByteString.copyFrom(CIPHER, StandardCharsets.UTF_8);

    static final String METADATA_ENCRYPTION_TAG = "encryption-tag";
    static final String METADATA_ENCRYPTION_KEY_ID_KEY = "encryption-key-id";

    private static final int GCM_NONCE_LENGTH_BYTE = 12;
    private static final int GCM_TAG_LENGTH_BIT = 128;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final KeyProvider keyProvider;
    private final String tag;
    private final ByteString tagByteString;

    /**
     * Constructs a new CustomPayloadCodec.
     *
     * @param keyProvider the KeyProvider to use for retrieving encryption keys
     */
    public SimplePayloadCodec(KeyProvider keyProvider, String tag) {
        this.keyProvider = keyProvider;
        this.tag = tag;
        this.tagByteString = ByteString.copyFrom(tag, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a list of payloads using AES/GCM encryption.
     *
     * @param payloads the list of payloads to encode
     * @return the list of encoded payloads
     */
    @Nonnull
    @Override
    public List<Payload> encode(@Nonnull List<Payload> payloads) {
        return payloads.stream().map(this::encodePayload).collect(Collectors.toList());
    }

    /**
     * Decodes a list of payloads using AES/GCM encryption.
     *
     * @param payloads the list of payloads to decode
     * @return the list of decoded payloads
     */
    @Nonnull
    @Override
    public List<Payload> decode(@Nonnull List<Payload> payloads) {
        return payloads.stream().map(this::decodePayload).collect(Collectors.toList());
    }

    /**
     * Encodes a single payload using AES/GCM encryption.
     *
     * @param payload the payload to encode
     * @return the encoded payload
     */
    private Payload encodePayload(Payload payload) {
        String keyId = keyProvider.getCurrentKeyId();
        SecretKey key = keyProvider.getKey(keyId);

        byte[] encryptedData;
        try {
            encryptedData = encrypt(payload.toByteArray(), key);
        } catch (Throwable e) {
            logger.error("Error during payload encryption", e);
            throw new DataConverterException(e);
        }

        return Payload.newBuilder()
                .putMetadata(EncodingKeys.METADATA_ENCODING_KEY, METADATA_ENCODING)
                .putMetadata(METADATA_ENCRYPTION_CIPHER_KEY, METADATA_ENCRYPTION_CIPHER)
                .putMetadata(METADATA_ENCRYPTION_KEY_ID_KEY, ByteString.copyFromUtf8(keyId))
                .putMetadata(METADATA_ENCRYPTION_TAG, ByteString.copyFromUtf8(tag))
                .setData(ByteString.copyFrom(encryptedData))
                .build();
    }

    /**
     * Decodes a single payload using AES/GCM encryption.
     *
     * @param payload the payload to decode
     * @return the decoded payload
     */
    private Payload decodePayload(Payload payload) {
        boolean isEncoded = METADATA_ENCODING.equals(
                payload.getMetadataOrDefault(EncodingKeys.METADATA_ENCODING_KEY, null));
        boolean sameTag = tagByteString.equals(
                payload.getMetadataOrDefault(METADATA_ENCRYPTION_TAG, null));
        if (isEncoded && sameTag) {
            String keyId;
            try {
                keyId = payload.getMetadataOrThrow(METADATA_ENCRYPTION_KEY_ID_KEY).toString(UTF_8);
            } catch (Exception e) {
                throw new PayloadCodecException(e);
            }
            SecretKey key = keyProvider.getKey(keyId);

            byte[] plainData;
            Payload decryptedPayload;

            try {
                plainData = decrypt(payload.getData().toByteArray(), key);
                decryptedPayload = Payload.parseFrom(plainData);
                return decryptedPayload;
            } catch (Throwable e) {
                logger.error("Error during payload decryption", e);
                throw new PayloadCodecException(e);
            }
        } else {
            return payload;
        }
    }

    /**
     * Generates a nonce of the specified size.
     *
     * @return the generated nonce
     */
    private static byte[] getNonce() {
        byte[] nonce = new byte[SimplePayloadCodec.GCM_NONCE_LENGTH_BYTE];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /**
     * Encrypts the given plain data using the specified secret key.
     *
     * @param plainData the plain data to encrypt
     * @param key the secret key to use for encryption
     * @return the encrypted data
     * @throws Exception if an error occurs during encryption
     */
    private byte[] encrypt(byte[] plainData, SecretKey key) throws Exception {
        byte[] nonce = getNonce();

        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BIT, nonce));

        byte[] encryptedData = cipher.doFinal(plainData);
        return ByteBuffer.allocate(nonce.length + encryptedData.length)
                .put(nonce)
                .put(encryptedData)
                .array();
    }

    /**
     * Decrypts the given encrypted data using the specified secret key.
     *
     * @param encryptedDataWithNonce the encrypted data with nonce to decrypt
     * @param key the secret key to use for decryption
     * @return the decrypted data
     * @throws Exception if an error occurs during decryption
     */
    private byte[] decrypt(byte[] encryptedDataWithNonce, SecretKey key) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(encryptedDataWithNonce);

        byte[] nonce = new byte[GCM_NONCE_LENGTH_BYTE];
        buffer.get(nonce);
        byte[] encryptedData = new byte[buffer.remaining()];
        buffer.get(encryptedData);

        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BIT, nonce));

        return cipher.doFinal(encryptedData);
    }
}
