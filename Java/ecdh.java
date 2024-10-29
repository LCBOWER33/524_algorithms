package com.mycompany._java_code;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author lcbba
 */
public class ecdh {
    private static final int CURVE_DEGREE = 163;
    private static final int BITVEC_MARGIN = 3;
    private static final int BITVEC_NBITS = (CURVE_DEGREE + BITVEC_MARGIN);
    private static final int BITVEC_NWORDS = 6;//(BITVEC_NBITS + 63) / 64; // Change to 64 for Long
    private static final int BITVEC_NBYTES = BITVEC_NWORDS * 8; // Change to 8 for Long

    // NIST B-163 parameters
    private static final ArrayList<Long> coeff_a = new ArrayList<>(Collections.singletonList(1L));
    private static final long cofactor = 2L;
    private static final ArrayList<Long> polynomial = new ArrayList<>(Arrays.asList(0x000000c9L, 0x00000000L, 0x00000000L, 0x00000000L, 0x00000000L, 0x00000008L));
    private static final ArrayList<Long> base_x = new ArrayList<>(Arrays.asList(0xe8343e36L, 0xd4994637L, 0xa0991168L, 0x86a2d57eL, 0xf0eba162L, 0x00000003L));
    private static final ArrayList<Long> base_y = new ArrayList<>(Arrays.asList(0x797324f1L, 0xb11c5c0cL, 0xa2cdd545L, 0x71a0094fL, 0xd51fbc6cL, 0x00000000L));
    private static final ArrayList<Long> base_order = new ArrayList<>(Arrays.asList(0xa4234c33L, 0x77e70c12L, 0x000292feL, 0x00000000L, 0x00000000L, 0x00000004L));
    private static final ArrayList<Long> coeff_b = new ArrayList<>(Arrays.asList(0x4a3205fdL, 0x512f7874L, 0x1481eb10L, 0xb8c953caL, 0x0a601907L, 0x00000002L));

    private static boolean bitvecGetBit(ArrayList<Long> x, int idx) {
        if (idx / 64 < x.size()) {
            return (x.get(idx / 64) >> (idx % 64) & 1) == 1;  // was 64
        }
        return false;
    }

    private static void bitvecClearBit(ArrayList<Long> x, int idx) {
        if (idx / 64 < x.size()) {
            x.set(idx / 64, x.get(idx / 64) & ~(1L << (idx % 63))); // Change to Long
        }
    }

    private static void bitvecCopy(ArrayList<Long> x, ArrayList<Long> y) {
        //int size = Math.min(x.size(), y.size());
        for (int i = 0; i < y.size(); i++) {
            x.set(i, y.get(i)); // Copy each element from y to x
        }
    }

    private static void bitvecSetZero(ArrayList<Long> x) {
        for (int i = 0; i < x.size(); i++) {
            x.set(i, 0L); // Set each element to zero
        }
    }
    private static boolean bitvecIsZero(ArrayList<Long> x) {
        for (long i : x) {
            if (i != 0) return false;
        }
        return true;
    }

    private static ArrayList<Long> gf2fieldAdd(ArrayList<Long> z, ArrayList<Long> x, ArrayList<Long> y) {
        /** 
         * Galois field(2^m) addition is modulo 2, 
         * so XOR is used instead - 'z := a + b' 
         */
        //System.out.println("we are in add");
        //System.out.println(x + " " + y + " " + BITVEC_NWORDS);
        for (int i = 0; i < BITVEC_NWORDS; i++) {
            z.set(i, x.get(i) ^ y.get(i));
        }
        return z;
    }

    private static void gf2fieldSetOne(ArrayList<Long> x) {
        if (!x.isEmpty()) {
            x.set(0, 1L); // Set the first element to 1
        }
        for (int i = 1; i < BITVEC_NWORDS; i++) {
            x.set(i, 0L); // Set the rest of the elements to 0
        }
    }

    private static void gf2fieldMul(ArrayList<Long> z, ArrayList<Long> x, ArrayList<Long> y) {
        ArrayList<Long> tmp = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        bitvecCopy(tmp, x);
        bitvecSetZero(z);

        for (int i = 0; i < CURVE_DEGREE; i++) {
            if (bitvecGetBit(y, i)) gf2fieldAdd(z, z, tmp);

            bitvecLShift(tmp, tmp, 1);
            if (bitvecGetBit(tmp, CURVE_DEGREE)) gf2fieldAdd(tmp, tmp, polynomial);
        }
    }

    private static ArrayList<Long> bitvecLShift(ArrayList<Long> x, ArrayList<Long> y, int nbits) {
        /** Left shift by n bits **/

        int j = 0;

        // Shift whole words first if nwords > 0
        int nwords = nbits / 32;
        //System.out.println("nwords= " + nwords);
        for (int i = 0; i < nwords; i++) {
            // Zero-initialize from least-significant word until offset reached
            x.set(i, 0L);
        }

        // Copy to x output
        for (int i = nwords; i < BITVEC_NWORDS; i++) {
            x.set(i, y.get(j));
            j++;
            //System.out.println("we do enter this loop");
        }
        //System.out.println("this is x " + x);

        // Shift the rest if count was not multiple of bitsize of DTYPE
        nbits &= 31;
        int test = BITVEC_NWORDS - 1;
        //System.out.println("nbits = " + nbits + " " + test);
        if (nbits != 0) {
            for (int i = BITVEC_NWORDS - 1; i > 0; i--) {
                //System.out.println(x.get(i) + " " + nbits);
                
                long myLong = 0x00000000FFFFFFFFL; // Example long value
                int shiftAmount = 2; // Example shift amount

                // Cast to int, perform the shift, then cast back to long
                long result = (long) ((int) myLong << shiftAmount); 
                long truth = myLong << shiftAmount;

//                System.out.println("Original long: " + Long.toHexString(myLong)); 
//                System.out.println("Shifted long: " + Long.toHexString(result));
//                System.out.println("Shifted truth: " + Long.toHexString(truth));
//                
                var shifted = (long) (x.get(i).intValue() << nbits) & 0xFFFFFFFFL; 
                //System.out.println(shifted);
                

                //System.out.println(x.get(i) << nbits);  // this is the part that is wrong because it has 64 bits to shift it is multiplying by 4 instead of shifting in the 32 bit space
                //System.out.println(x.get(i - 1) >>> (32 - nbits));
                x.set(i, shifted | (x.get(i - 1) >>> (32 - nbits)));  // this is our error line
                //System.out.println("this is x " + i + " " + x);
            }
            x.set(0, (long) (x.get(0).intValue() << nbits) & 0xFFFFFFFFL);
        } //System.out.println("this is x " + x);

        return x;
    }

    private static int gf2fieldDegree(ArrayList<Long> x) {
        for (int i = BITVEC_NWORDS - 1; i >= 0; i--) {
            if (x.get(i) != 0) return 64 * i + Long.numberOfTrailingZeros(x.get(i)) - 1; // Change to Long
        }
        return -1;
    }

    private static boolean gf2fieldIsOne(ArrayList<Long> x) {
        if (x.get(0) != 1) return false;
        for (int i = 1; i < BITVEC_NWORDS; i++) {
            if (x.get(i) != 0) return false;
        }
        return true;
    }
    
    private static int bitvecDegree(ArrayList<Long> x) {
        int i = BITVEC_NWORDS * 32;
        int u = BITVEC_NWORDS;

        while (i > 0 && x.get(u - 1) == 0) {
            i -= 32;
            u -= 1;
        }

        if (i != 0) {
            long u32mask = 1L << 31;
            while ((x.get(u - 1) & u32mask) == 0) {
                u32mask >>= 1;
                i -= 1;
            }
        }
        return i;
    }
    
    private static void bitvecSwap(ArrayList<Long> x, ArrayList<Long> y) {
        ArrayList<Long> tmp = new ArrayList<>(x); // Create a copy of x
        bitvecCopy(x, y); // Copy y into x
        bitvecCopy(y, tmp); // Copy tmp (original x) into y
    }
    private static void gf2fieldInv(ArrayList<Long> z, ArrayList<Long> x) {
        ArrayList<Long> u = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        ArrayList<Long> v = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        ArrayList<Long> g = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        ArrayList<Long> h = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        
        //System.out.println(x.size() + " " + z.size());
        
        bitvecCopy(u, x);
        bitvecCopy(v, polynomial);
        bitvecSetZero(g);
        gf2fieldSetOne(z);
        //System.out.println(x.size() + " " + z.size());
        //System.out.println(u.size() + " " + v.size());

        int count = 0;

        while (!gf2fieldIsOne(u)) {  // this is not terminating
            //System.out.println(u.size());  // u and v need to be size 6 not 3
            //System.out.println(v.size());
            int i = bitvecDegree(u) - bitvecDegree(v);  // this line is the problem
            //System.out.println(i);
            
            //System.out.println(u + " " + v);

            if (i < 0) {
                bitvecSwap(u, v);
                bitvecSwap(g, z);  // this is fine
                i = -i;
            }
            //System.out.println(u + " " + v);

            h = bitvecLShift(h, v, i);        // h = v << i
            u = gf2fieldAdd(u, u, h);         // u = u + h            // this is the one thats wrong, h is going in wrong
            h = bitvecLShift(h, g, i);        // h = g << i
            z = gf2fieldAdd(z, z, h);         // z = z + h
            
            count += 2;
        }
        //System.out.println("count = " + count);
    }

    private static boolean gf2pointIsZero(ArrayList<Long> x, ArrayList<Long> y) {
    return bitvecIsZero(x) && bitvecIsZero(y);
}
    
    private static void gf2pointCopy(ArrayList<Long> x1, ArrayList<Long> y1, ArrayList<Long> x2, ArrayList<Long> y2) {
    bitvecCopy(x1, x2);
    bitvecCopy(y1, y2);
}
    
    public static void gf2pointAdd(ArrayList<Long> x1, ArrayList<Long> y1, ArrayList<Long> x2, ArrayList<Long> y2, ArrayList<Long> coeffA, ArrayList<Long> coeffB) {
        ArrayList<Long> l = new ArrayList<>(Collections.nCopies(6, 0L));

        if (gf2pointIsZero(x2, y2)) {
            //System.out.print("we are in if");
            return; // No-op if the second point is the identity point
        } else if (gf2pointIsZero(x1, y1)) {
            //System.out.print("we are in else if");
            gf2pointCopy(x1, y1, x2, y2); // If the first point is zero, copy the second point
        } else {
            //System.out.print("we are in else");
            //System.out.println(l);
            gf2fieldAdd(l, x1, x2); // Calculate l = (x1 + x2)
            l.set(0, 3895737910L);
            //System.out.println(l);
            //System.out.print("we are in else");
            gf2fieldInv(l, l); // l = 1 / l (invert l in the field)
            //System.out.print("we are in else");
            gf2fieldAdd(l, y1, y2); // l = y1 + y2
            gf2fieldMul(l, l, l); // l = l * l
            //System.out.print("we are in else");

            gf2fieldAdd(l, l, l); // l = 2 * l, double for affine point addition
            gf2fieldAdd(l, l, coeffA); // Add the curve coefficient

            // Intermediate variables for storing new x3, y3
            ArrayList<Long> x3 = new ArrayList<>(Collections.nCopies(6, 0L));
            ArrayList<Long> y3 = new ArrayList<>(Collections.nCopies(6, 0L));

            gf2fieldAdd(x3, x1, x2); // x3 = x1 + x2
            gf2fieldMul(x3, x3, x3); // x3 = (x1 + x2)^2
            gf2fieldAdd(x3, x3, l); // x3 = x3 + l
            gf2fieldAdd(x3, x3, coeffB); // x3 = x3 + b

            gf2fieldAdd(y3, x1, x3); // y3 = x1 + x3
            gf2fieldMul(y3, y3, l); // y3 = y3 * l
            gf2fieldAdd(y3, y3, y1); // y3 = y3 + y1

            gf2pointCopy(x1, y1, x3, y3); // Set x1, y1 to the new values of x3, y3
        }
    }


    private static void gf2pointMul(ArrayList<Long> x, ArrayList<Long> y, ArrayList<Long> k) {
        ArrayList<Long> z = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        ArrayList<Long> x1 = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        ArrayList<Long> y1 = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long
        ArrayList<Long> z1 = new ArrayList<>(Collections.nCopies(BITVEC_NWORDS, 0L)); // Change to Long

        gf2fieldSetOne(z); // z = 1
        bitvecCopy(x1, x); // x1 = x
        bitvecCopy(y1, y); // y1 = y
        //System.out.println(z.size() + " " + x.size());
        for (int i = 0; i < CURVE_DEGREE; i++) {
            if (bitvecGetBit(k, i)) {
                gf2pointAdd(z, z1, x1, y1, z1, z1); // z1 = z1 + (x1, y1)
            }
            //System.out.println("are we here");
            gf2pointAdd(x1, y1, x1, y1, x1, y1); // Double (x1, y1)
            //System.out.println("are we here");
        }

        bitvecCopy(x, z1); // z1 = z
        bitvecCopy(y, z1); // z1 = y
    }
    
    private static void ecdhGenerateKeys(ArrayList<Long> privateKey, ArrayList<Long> publicKey) {
        Random rand = new Random();

        // Generate random private key
        for (int i = 0; i < 6; i++) {
            privateKey.set(i, rand.nextLong() & 0xFFFFFFFFL); // Mask to 32 bits
        }

        // Initialize public key coordinates from base_x and base_y
        ArrayList<Long> pubX = new ArrayList<>(base_x);
        ArrayList<Long> pubY = new ArrayList<>(base_y);

        // Multiply the base point by the private key to get the public key
        gf2pointMul(pubX, pubY, privateKey);

        // Combine public key coordinates into a single list
        publicKey.clear(); // Clear existing public key
        publicKey.addAll(pubX);
        publicKey.addAll(pubY);
    }
    
    public static ArrayList<Long> ecdhSharedSecret(ArrayList<Long> privateKey, ArrayList<Long> publicKey) {
        // Split the public key into x and y components
        ArrayList<Long> pubX = new ArrayList<>(publicKey.subList(0, 6));
        ArrayList<Long> pubY = new ArrayList<>(publicKey.subList(6, publicKey.size()));

        // Perform the point multiplication
        gf2pointMul(pubX, pubY, privateKey);

        // Combine the results
        pubX.addAll(pubY);
        return pubX;
    }


    public void ecdhProcess() {
        // Create private and public key storage
        ArrayList<Long> privateKey = new ArrayList<>(Collections.nCopies(6, 0L));
        ArrayList<Long> publicKey = new ArrayList<>(Collections.nCopies(12, 0L));
        //System.out.println(BITVEC_NWORDS);
        //System.out.println(privateKey.size());
        // Generate keys for the current instance
        ecdhGenerateKeys(privateKey, publicKey);

        // Print the private key
        System.out.print("Private Key: ");
        for (Long v : privateKey) {
            System.out.printf("%x ", v);
        }
        System.out.println();

        // Print the public key
        System.out.print("Public Key: ");
        for (Long v : publicKey) {
            System.out.printf("%x ", v);
        }
        System.out.println();

        // Generate keys for the peer
        ArrayList<Long> peerPrivateKey = new ArrayList<>(Collections.nCopies(6, 0L));
        ArrayList<Long> peerPublicKey = new ArrayList<>(Collections.nCopies(12, 0L));
        ecdhGenerateKeys(peerPrivateKey, peerPublicKey);

        // Compute shared secrets
        ArrayList<Long> sharedSecret = ecdhSharedSecret(privateKey, peerPublicKey);
        ArrayList<Long> sharedSecret2 = ecdhSharedSecret(peerPrivateKey, publicKey);

        // Print the first shared secret
        System.out.print("Shared Secret: ");
        for (Long v : sharedSecret) {
            System.out.printf("%x ", v);
        }
        System.out.println();

        // Print the second shared secret
        System.out.print("Shared Secret 2: ");
        for (Long v : sharedSecret2) {
            System.out.printf("%x ", v);
        }
        System.out.println();

        // Check if shared secrets match
        System.out.println("Keys match: " + sharedSecret.equals(sharedSecret2));
    }

}
