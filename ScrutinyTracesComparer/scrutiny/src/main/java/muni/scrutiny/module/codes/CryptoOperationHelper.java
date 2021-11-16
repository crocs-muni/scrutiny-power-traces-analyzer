package muni.scrutiny.module.codes;

import java.util.ArrayList;
import java.util.List;

public class CryptoOperationHelper {
    private List<String> primaryNames = new ArrayList<String>()
    {{
        add("ALG_SECURE_RANDOM_128B");
        add("LENGTH_AES_256");
        add("LENGTH_DES3_3KEY");
        add("ALG_AES_BLOCK_128_CBC_NOPAD");
        add("ALG_DES_CBC_NOPAD");
        add("ALG_SHA");
        add("ALG_SHA_256");
        add("ALG_RSA_CRT_LENGTH_RSA_512");
        add("ALG_RSA_SHA_PKCS1_LENGTH_RSA_512");
        add("ALG_EC_FP_192");
        add("ALG_ECDSA_SHA_EC_FP_192");
        add("ALG_EC_FP_256");
        add("ALG_ECDSA_SHA_EC_FP_256");
    }};
}
