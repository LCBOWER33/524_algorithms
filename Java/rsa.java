package com.mycompany._java_code;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author lcbba
 */
public class rsa {
    private static HashSet<Integer> prime = new HashSet<>();
    private static Integer publicKey = null;
    private static Integer privateKey = null;
    private static Integer n = null;
    private static Random random = new Random();

    // We will run the function only once to fill the set of prime numbers
    private static void primefiller() {
        // Method used to fill the primes set is Sieve of Eratosthenes
        boolean[] sieve = new boolean[250];
        for (int i = 2; i < sieve.length; i++) {
            sieve[i] = true;
        }
        sieve[0] = false;
        sieve[1] = false;

        for (int i = 2; i < 250; i++) {
            if (sieve[i]) {
                for (int j = i * 2; j < 250; j += i) {
                    sieve[j] = false;
                }
            }
        }

        // Filling the prime numbers
        for (int i = 0; i < sieve.length; i++) {
            if (sieve[i]) {
                prime.add(i);
            }
        }
    }

    // Picking a random prime number and erasing that prime number from list
    private static int pickrandomprime() {
        int k = random.nextInt(prime.size());
        Iterator<Integer> it = prime.iterator();
        for (int i = 0; i < k; i++) {
            it.next();
        }

        int ret = it.next();
        prime.remove(ret);
        return ret;
    }

    private static void setkeys() {
        int prime1 = pickrandomprime(); // First prime number
        int prime2 = pickrandomprime(); // Second prime number

        n = prime1 * prime2;
        int fi = (prime1 - 1) * (prime2 - 1);

        int e = 2;
        while (true) {
            if (gcd(e, fi) == 1) {
                break;
            }
            e++;
        }

        // d = (k*Φ(n) + 1) / e for some integer k
        publicKey = e;

        int d = 2;
        while (true) {
            if ((d * e) % fi == 1) {
                break;
            }
            d++;
        }

        privateKey = d;
    }

    // To encrypt the given number
    private static int encrypt(int message) {
        int e = publicKey;
        int encryptedText = 1;
        while (e > 0) {
            encryptedText *= message;
            encryptedText %= n;
            e--;
        }
        return encryptedText;
    }

    // To decrypt the given number
    private static int decrypt(int encryptedText) {
        int d = privateKey;
        int decrypted = 1;
        while (d > 0) {
            decrypted *= encryptedText;
            decrypted %= n;
            d--;
        }
        return decrypted;
    }

    // First converting each character to its ASCII value and then encoding it
    private static int[] encoder(String message) {
        int[] encoded = new int[message.length()];
        // Calling the encrypting function in encoding function
        for (int i = 0; i < message.length(); i++) {
            encoded[i] = encrypt((int) message.charAt(i));
        }
        return encoded;
    }

    private static String decoder(int[] encoded) {
        StringBuilder s = new StringBuilder();
        // Calling the decrypting function decoding function
        for (int num : encoded) {
            s.append((char) decrypt(num));
        }
        return s.toString();
    }

    public void RSAProcess() {
        primefiller();
        setkeys();
        String message = "Test Message";
        // Uncomment below for manual input
        // Scanner scanner = new Scanner(System.in);
        // System.out.println("Enter the message");
        // String message = scanner.nextLine();
        // Calling the encoding function
        int[] coded = encoder(message);

        System.out.println("Initial message:");
        System.out.println(message);
        System.out.println("\n\nThe encoded message(encrypted by public key)\n");
        for (int p : coded) {
            System.out.print(p);
        }
        System.out.println("\n\nThe decoded message(decrypted by public key)\n");
        System.out.println(decoder(coded));
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
