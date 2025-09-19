# Openssl

## How to generate key

### Check If Openssl on Machine

```shell
openssl version
```

### Generate Key with Openssl

- `rand` -> random
- `-base64` -> algorithm to encoding the key
- `64` -> length in minimum 32 bytes, longer key for better security

```shell
openssl rand -base64 64
```
