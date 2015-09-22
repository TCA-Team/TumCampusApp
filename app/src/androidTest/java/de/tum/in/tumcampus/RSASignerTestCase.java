package de.tum.in.tumcampus;

import android.util.Base64;

import junit.framework.TestCase;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatMessage;

@SuppressWarnings("All")
public class RSASignerTestCase extends TestCase {

    private RSASigner signer;
    private PrivateKey privateKeyFixture;
    private ArrayList<ChatMessage> messageFixtures;


    private static PrivateKey buildPrivateKey(String privateKeyString) {
        byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            KeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }


    @Override
    protected void setUp() {
        privateKeyFixture = buildPrivateKey(
                "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM46wq9uOF7y1dmNO3nY8D1P6bCe" +
                        "t3izsm2GKQtvWmV78WbBxk1rZI2GNExvZ3aVg4mb6jOToGzm+jdNiWR07kBFSlrgNC5zq7Jmm0gz" +
                        "yTgrou6eV5NoQsPO2fI3tworvxgGa886Hu0U8p3gFk+BJnffndSq8DdBKcIjGDunTv4dAgMBAAEC" +
                        "gYB3QywLX+ZhonVhVneqw3ZLPseaSG858lGhXRCneEICpma4Uh9n7k88OPxNp69huJ1VG0GZiiog" +
                        "UIMrMD/gRG7y2NJxKkDbR2aVB+YR6aGAWYfwZVDM+Swxe3wUbDfRsAuXGapKCXnjHOKGbUCi/gx9" +
                        "AvFH4YJ9IImFGD+T7jSogQJBAP/oBCSXhDCHQXoN3vqdncmNNKl1J9QvzPY2ESMzfbdJ4QESb4YZ" +
                        "f88ixXUZpb3JVamW4WI6QHEHGMnGZwJyL6UCQQDOThaq2Mw/8QyPiKup7B+QT7/Bz7wpQx7IHw6a" +
                        "r5pX5AYAO7WmihxOgDMX6VfhCJlksxfLTVSuiJdXuJSm90sZAkBdXuJkF4R70F3rkrQQ7QFtUMAu" +
                        "NDjcCrTWANQv69Gq1qHqKjfWzeb8RMuW9kyq+pLu1cZWeLqaguRgequLEO6hAkBsq1NjUOldsQI1" +
                        "xP7vdbI2mNtgIqVxcqqPLVTLBD6flzvV+Z24iL1aWEsRiFdC8P2jvnaFH0nA2bAmg9LBlDdZAkEA" +
                        "9tg4rD0aCHQZ6kEquwN6emc9QM0X6DR0dx6Bqq8CGDkVdk0hXHBR9VUBGE4YSsxpn+LnyWSWyJum" +
                        "dWuepeUKig==");

        messageFixtures = new ArrayList<>();
        messageFixtures.add(buildChatMessage(
                "This is a message!",
                "Tw7Geajto7C/orsLT4TfNCUa1gnu6pGumfp+Nck7/QoOmDxilgQCpuzlpxa5Y7xuQ2rQB4XhFSm4\n" +
                        "3gOHijTwF91SQx2sdIWClofzr/H0JABpQRkkMbsVQikwOnQYp+d9c1eylNPeendoYW4NAEBKpNyw\n" +
                        "ShtHN6jcC2Usw1lAfxE=\n"));
        messageFixtures.add(buildChatMessage(
                "A message with German characters: öäüßÖÄÜ!",
                "tSHKrusEPatW7CUJGbPjLfpPkoO/hQnJPMCQDztVjQJNqpEk+Jbm+FTwakOQ49OaMtmZTfnKUoJQ\n" +
                        "MBwgp/I6zL7Xlafxiw+jA72ah/kvixm46VlpGFF2sEYfC0Ts3Agyq1T7XXYgkrGKjC3vs6sGNFGv\n" +
                        "IefIoEAOGaWIfZnnbuM=\n"));
        messageFixtures.add(buildChatMessage(
                "This is a Korean message: \uC88B\uC740 \uAC8C\uC784",
                "Td+E1WOg5FweCrBKzsjjVbbf3EeiNLu/PWID1Tg41ak5NFllqsFUcPEzPP0bZ+Dpv0sU7deQ9BaQ\n" +
                        "lNVaNClQsI7Y5jTmoqS5elRdrig+eq9Qzl7bvEr0EI5CUvwLZJU4LCpLYUJEGD++IOzE0xZxB6/j\n" +
                        "MES0525W5YVR0knzoKw=\n"));
    }

    protected static ChatMessage buildChatMessage(String text, String signature) {
        ChatMessage message = new ChatMessage(text);
        message.setSignature(signature);

        return message;
    }

    /**
     * Tests that a valid ASCII-based message is correctly signed.
     */
    public void testAsciiMessageSigning() {
        signer = new RSASigner(privateKeyFixture);
        ChatMessage message = messageFixtures.get(0);

        assertEquals(message.getSignature(), signer.sign(message.getText()));
    }

    /**
     * Tests that a unicode (european) message is correctly signed.
     */
    public void testUnicodeMessageSigning() {
        signer = new RSASigner(privateKeyFixture);
        ChatMessage message = messageFixtures.get(1);

        assertEquals(message.getSignature(), signer.sign(message.getText()));
    }

    /**
     * Tests that a unicode (korean) message is correctly signed.
     */
    public void testUnicodeKoreanMessageSigning() {
        signer = new RSASigner(privateKeyFixture);
        ChatMessage message = messageFixtures.get(2);

        assertEquals(message.getSignature(), signer.sign(message.getText()));
    }

    /**
     * Tests that when the private key associated with
     * the signer is null, the signer returns null.
     */
    public void testPrivateKeyNull() {
        signer = new RSASigner(null);
        ChatMessage message = messageFixtures.get(0);

        assertNull(signer.sign(message.getText()));
    }
}
