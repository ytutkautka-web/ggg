package fun.photon.config;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public final class KeyConfigCodec implements ConfigCodec {

    private static final String SECRET = "Proton::cfg::v1::3f9a2c";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom rng = new SecureRandom();

    public KeyConfigCodec() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(SECRET.getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(Arrays.copyOf(digest, 16), "AES");
        } catch (Exception e) {
            throw new RuntimeException("KeyConfigCodec init failed", e);
        }
    }

    @Override
    public byte[] encode(byte[] jsonBytes) {
        try {
            byte[] iv = new byte[IV_LEN];
            rng.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = c.doFinal(jsonBytes);
            byte[] out = new byte[IV_LEN + ct.length];
            System.arraycopy(iv, 0, out, 0, IV_LEN);
            System.arraycopy(ct, 0, out, IV_LEN, ct.length);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("config encode failed", e);
        }
    }

    @Override
    public byte[] decode(byte[] fileBytes) {
        try {
            byte[] iv = Arrays.copyOfRange(fileBytes, 0, IV_LEN);
            byte[] ct = Arrays.copyOfRange(fileBytes, IV_LEN, fileBytes.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return c.doFinal(ct);
        } catch (Exception e) {
            throw new RuntimeException("config decode failed (bad key or tampered file)", e);
        }
    }
}
