package org.knowm.xchange.idex;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.util.Arrays.asList;
import static org.web3j.crypto.Hash.sha3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

public class IdexSignature {
  static Boolean debugMe = true;

  static
  /** Generate v, r, s values from payload */
  Sign.SignatureData generateSignature(String apiSecret, List<List<String>> data) {
    byte[] rawhash = null;
    byte[] saltBytes;
    byte[] bytes = null;
    String[] last = new String[1];
    BigInteger apiSecret1;
    ECKeyPair ecKeyPair;
    try (ByteArrayOutputStream sig_arr = new ByteArrayOutputStream()) {

      if (IdexSignature.debugMe) {
        System.err.println(data);
      }
      for (List<String> d : data) {
        String data1 = d.get(1);
        /* remove 0x prefix and convert to bytes*/
        if (IdexSignature.debugMe)
          System.err.println("\n===\nsignature  for (len: " + d.get(1).length() + " ):" + d);
        byte[] segment = new byte[0];
        byte[] r = new byte[0];
        last[0] = new LinkedList<>(asList(data1.toLowerCase().split("0x"))).getLast();
        switch (d.get(2)) {
          case "address":
            {
              segment = new byte[20];
              r = new BigInteger(last[0], 16).toByteArray();

              break;
            }

          case "uint256":
            {
              segment = new byte[32];
              r = new BigInteger(last[0], 10).toByteArray();

              break;
            }
        }
        int segLen = segment.length;
        int rlen = min(max(segLen, r.length), r.length);
        int oversize = r.length - segLen;
        System.arraycopy(
            r,
            oversize > 0 ? oversize : 0,
            segment,
            oversize > 0 ? 0 : (segLen - rlen),
            min(segLen, rlen));

        sig_arr.write(segment);

        if (IdexSignature.debugMe) {
          System.err.println(
              "signature: results: (len: "
                  + rlen
                  + "->"
                  + segLen
                  + ") "
                  + Hex.toHexString(segment));
          System.err.println("signature: accumulated: " + Hex.toHexString(sig_arr.toByteArray()));
        }
      }
      rawhash = sha3(sig_arr.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }

    // salt the hashed packed string
    saltBytes = "\u0019Ethereum Signed Message:\n32".getBytes();

    assert (new String(Hex.toHexString(saltBytes)).toLowerCase()
        == "19457468657265756d205369676e6564204d6573736167653a0a3332");

    byte[] salted = null;
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      byteArrayOutputStream.write(saltBytes);
      byteArrayOutputStream.write(rawhash);
      salted = bytes = byteArrayOutputStream.toByteArray();
      //            sha3(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (IdexSignature.debugMe) {
      System.err.println("\n== unsalted:\t" + Hex.toHexString(rawhash));
      System.err.println("== salt raw :\t" + Hex.toHexString(saltBytes));
      System.err.println("== salted raw :\t" + Hex.toHexString(bytes));
      System.err.println("== salted hash:\t" + Hex.toHexString(salted));
    }
    last[0] = new LinkedList<>(asList(apiSecret.split("0x"))).getLast();
    apiSecret1 = new BigInteger(last[0], 16);
    ecKeyPair = ECKeyPair.create(apiSecret1);

    Sign.SignatureData signatureData;
    signatureData = Sign.signMessage(/* message = */ salted, /* keyPair=  */ ecKeyPair);
    return signatureData;
  }
}
