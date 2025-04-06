# Temporal Codec Application

## Overview
The Codec Application is a Spring Boot project designed to encode and decode payloads using Temporal's PayloadCodec. It also integrates with HashiCorp Vault for secure key management and caching using Caffeine.

## Features
- Encode and decode payloads via REST API.
- Secure key management with HashiCorp Vault.
- Caching with Caffeine for improved performance.
- Scheduled key rotation.

## Technologies Used
- Java
- Spring Boot
- Maven
- Temporal
- HashiCorp Vault
- Caffeine Cache

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven
- HashiCorp Vault

### Installation
1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/codec-application.git
    cd codec-application
    ```

2. Configure Vault:
    - Ensure Vault is running and accessible at `http://localhost:8200`.
    - Set the Vault token in the `application.yml` file or as an environment variable.

3. Build the project:
    ```sh
    mvn clean install
    ```

4. Run the application:
    ```sh
    mvn spring-boot:run
    ```

## Configuration
The application can be configured using the `src/main/resources/application.yml` file. Key configuration properties include:

```yaml
spring:
  application:
    name: temporal-codec
  cloud:
    vault:
      uri: http://localhost:8200
      token: ${VAULT_TOKEN}

cache:
  ttl: 60
codec:
  vault:
    config-path: config.json
    schedule-cron: 0 * * * * *
```

## API Endpoints

### Encode Payload
- **URL:** `/v1/codec/encode`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Request Body:** JSON payload to encode
- **Response:** Encoded JSON payload

Example request:
```curl
curl --location 'localhost:8080/v1/codec/encode' \
--header 'Content-Type: application/json' \
--data '{
    "payloads": [
        {
            "metadata": {
                "encoding": "anNvbi9wbGFpbg=="
            },
            "data": "eyJoaSI6ImhpIn0="
        }
    ]
}'
```

### Decode Payload
- **URL:** `/v1/codec/decode`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Request Body:** JSON payload to decode
- **Response:** Decoded JSON payload

Example request:
```curl
curl --location 'localhost:8080/v1/codec/decode' \
--header 'Content-Type: application/json' \
--data '{
    "payloads": [
        {
            "metadata": {
                "encoding": "YmluYXJ5L2VuY3J5cHRlZA==",
                "encryption-cipher": "QUVTL0dDTS9Ob1BhZGRpbmc=",
                "encryption-key-id": "a2V5LTE3NDM4OTYzNjYwODI="
            },
            "data": "nSw7gQoGtb5KFVDvZbR2Iw1qYOvt2+tti6lE77+/GMNfawcmsQiozlnEW4vjsLNIeFoi3C1aBkU3N4apqopTEdw="
        }
    ]
}'
```

## Key Management
Keys are managed using the `VaultKeyProvider` class, which interacts with HashiCorp Vault to store and retrieve keys. Keys are cached for performance and rotated based on a cron schedule.

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contributing
Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.

## Contact
For any questions or support, please contact `1.olah.istvan.75@gmail.com`.