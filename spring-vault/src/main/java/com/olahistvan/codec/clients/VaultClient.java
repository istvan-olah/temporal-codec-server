package com.olahistvan.codec.clients;

import com.olahistvan.codec.dto.CodecConfigDto;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

public class VaultClient {

    private final VaultTemplate vaultTemplate;
    private final String filePath;

    public VaultClient(VaultTemplate vaultTemplate, String filePath) {
        this.vaultTemplate = vaultTemplate;
        this.filePath = filePath;
    }

    /**
     * Retrieves the codec configuration from Vault.
     *
     * @return the codec configuration
     * @throws RuntimeException if the configuration is not retrieved from Vault
     */
    public CodecConfigDto getCodecConfig() {
        VaultKeyValueOperations kv = vaultTemplate.opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
        VaultResponseSupport<CodecConfigDto> response = kv.get(filePath, CodecConfigDto.class);
        if (response == null || response.getData() == null) {
            throw new RuntimeException("Config not retrieved from Vault");
        }
        return response.getData();
    }

    /**
     * Saves the codec configuration to Vault.
     *
     * @param config the codec configuration to save
     */
    public void saveCodecConfig(CodecConfigDto config) {
        VaultKeyValueOperations kv = vaultTemplate.opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
        kv.put(filePath, config);
    }

}
