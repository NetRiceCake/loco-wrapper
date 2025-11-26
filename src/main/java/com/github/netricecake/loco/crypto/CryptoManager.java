package com.github.netricecake.loco.crypto;

import com.github.netricecake.loco.util.ByteUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

public class CryptoManager {

    public final static int HANDSHAKE_BODY_SIZE = 256; // ENCRYPTED KEY

    public final static String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
    public final static int RSA_LOCO_HEADER = 16;
    public final static byte[] RSA_PUBLIC_KEY_BYTES = ByteUtil.hexStringToByteArray("30820120300D06092A864886F70D01010105000382010D00308201080282010100A3B076E8C445851F19A670C231AAC6DB42EFD09717D06048A5CC56906CD1AB27B9DF37FFD5017E7C13A1405B5D1C3E4879A6A499D3C618A72472B0B50CA5EF1EF6EEA70369D9413FE662D8E2B479A9F72142EE70CEE6C2AD12045D52B25C4A204A28968E37F0BA6A49EE3EC9F2AC7A65184160F22F62C43A4067CD8D2A6F13D9B8298AB002763D236C9D1879D7FCE5B8FA910882B21E15247E0D0A24791308E51983614402E9FA03057C57E9E178B1CC39FE67288EFC461945CBCAA11D1FCC123E750B861F0D447EBE3C115F411A42DC95DDB21DA42774A5BCB1DDF7FA5F10628010C74F36F31C40EFCFE289FD81BABA44A6556A6C301210414B6023C3F46371020103");

    public final static String AES_ALGORITHM = "AES/GCM/NoPadding";
    public final static int AES_KEY_SIZE = 128;
    public final static int AES_NONCE_SIZE = 12;
    public final static int AES_LOCO_HEADER = 3;

    private Key aesKey;
    private final SecureRandom generator = new SecureRandom();

    public CryptoManager() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            aesKey = keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CryptoManager(Key aesKey) {
        this.aesKey = aesKey;
    }

    public byte[] generateHandshakeMessage() {
        try {
            PublicKey rsaPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(RSA_PUBLIC_KEY_BYTES));
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
            byte[] length = ByteUtil.intToByteArrayLE(HANDSHAKE_BODY_SIZE);
            return ByteUtil.concatBytes(length, ByteUtil.intToByteArrayLE(RSA_LOCO_HEADER), ByteUtil.intToByteArrayLE(AES_LOCO_HEADER), encryptedKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    // 바디 사이즈가 131067가 최대인거 같은데 잘 모르겠음
    public byte[] encryptMessage(byte[] message) {
        try {
            byte[] nonce = new  byte[AES_NONCE_SIZE];
            generator.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(AES_KEY_SIZE, nonce));
            byte[] encryptedBody = cipher.doFinal(message);
            return ByteUtil.concatBytes(nonce, encryptedBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] decryptMessage(byte[] message) {
        try {
            byte[] nonce = ByteUtil.sliceBytes(message, 0, AES_NONCE_SIZE);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey,  new GCMParameterSpec(AES_KEY_SIZE, nonce));
            return cipher.doFinal(ByteUtil.sliceBytes(message, AES_NONCE_SIZE, message.length - nonce.length));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
