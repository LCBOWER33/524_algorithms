package com.mycompany._java_code;
import java.math.BigInteger;

/**
 *
 * @author lcbba
 */
public class diffie_hellman {
    // Power function to return value of a^b mod P
    public static BigInteger power(BigInteger a, BigInteger b, BigInteger p) {
        if (b.equals(BigInteger.ONE)) {
            return a;
        } else {
            return a.modPow(b, p);
        }
    }

    public void diffieHellman() {
        // Both persons agree upon the public keys G and P
        // A prime number P is taken
        BigInteger P = BigInteger.valueOf(23);  // primes.generateLargePrime();
        System.out.println("The value of P: " + P);

        // A primitive root for P, G is taken
        BigInteger G = BigInteger.valueOf(9);
        System.out.println("The value of G: " + G);

        // Alice chooses the private key a
        BigInteger a = BigInteger.valueOf(4);
        System.out.println("The private key a for Alice: " + a);

        // Gets the generated key
        BigInteger x = power(G, a, P);

        // Bob chooses the private key b
        BigInteger b = BigInteger.valueOf(3);
        System.out.println("The private key b for Bob: " + b);

        // Gets the generated key
        BigInteger y = power(G, b, P);

        // Generating the secret key after the exchange of keys
        BigInteger ka = power(y, a, P);  // Secret key for Alice
        BigInteger kb = power(x, b, P);  // Secret key for Bob

        System.out.println("Secret key for Alice is: " + ka);
        System.out.println("Secret key for Bob is: " + kb);
    }
}
