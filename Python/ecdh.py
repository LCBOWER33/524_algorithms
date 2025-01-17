import numpy as np

def ECDH():
    # https://github.com/danielsousaoliveira/tiny-ECDH-python/tree/main

    # Bit vectors size definition
    # Margin for overhead needed in intermediate calculations
    CURVE_DEGREE = 163
    BITVEC_MARGIN = 3
    BITVEC_NBITS = (CURVE_DEGREE + BITVEC_MARGIN)
    BITVEC_NWORDS = (int)((BITVEC_NBITS + 31) / 32)
    BITVEC_NBYTES = BITVEC_NWORDS * 4

    # NIST B-163 parameters
    coeff_a = 1
    cofactor = 2
    polynomial = [0x000000c9, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000008]
    base_x = [0xe8343e36, 0xd4994637, 0xa0991168, 0x86a2d57e, 0xf0eba162, 0x00000003]
    base_y = [0x797324f1, 0xb11c5c0c, 0xa2cdd545, 0x71a0094f, 0xd51fbc6c, 0x00000000]
    base_order = [0xa4234c33, 0x77e70c12, 0x000292fe, 0x00000000, 0x00000000, 0x00000004]
    coeff_b = [0x4a3205fd, 0x512f7874, 0x1481eb10, 0xb8c953ca, 0x0a601907, 0x00000002]

    # -------------------------------------------------------------------
    # Some basic bit-manipulation routines that act on bit-vectors follow
    # Most of them aren't needed in Python
    # -------------------------------------------------------------------

    def bitvec_get_bit(x, idx):

        """ Get bit of index idx"""

        return ((x[idx // 32] >> (idx & 31)) & 1)

    def bitvec_clr_bit(x, idx):

        """ Clear bit of index idx"""

        x[idx // 32] &= ~(1 << (idx & 31))

        return x

    def bitvec_copy(x, y):

        """ Return a copy of the bit vector"""

        for i in range(len(y)):
            x[i] = y[i]

        return x

    def bitvec_swap(x, y):

        """ Swap bit vectors"""

        tmp = x.copy()
        x = bitvec_copy(x, y)
        y = bitvec_copy(y, tmp)

        return x, y

    def bitvec_equal(x, y):

        """ Check if bit vectors are equal """

        for i in range(len(x)):
            if x[i] != y[i]:
                return False

        return True

    def bitvec_set_zero(x):

        """ Set bit vector to 0 """

        for i in range(len(x)):
            x[i] = 0

        return x

    def bitvec_is_zero(x):

        """ Check if bit vector is 0 """

        for i in x:
            if i != 0:
                return False

        return True

    def bitvec_degree(x):

        """ Returns the number of the highest one-bit + 1 """

        i = BITVEC_NWORDS * 32
        u = BITVEC_NWORDS

        # Start at the back of the vector and skip empty/zero words
        while (i > 0) and (x[u - 1] == 0):
            i -= 32
            u -= 1

        # Run through rest if count is not multiple of bitsize of DTYPE
        if i != 0:
            u32mask = np.uint32(1 << 31)
            while (x[u - 1] & u32mask) == 0:
                u32mask >>= 1
                i -= 1

        return i

    def bitvec_lshift(x, y, nbits):

        """ Left shift by n bits """

        j = 0

        # Shift whole words first if nwords > 0
        nwords = nbits // 32
        #print("nwords= ", nwords)
        for i in range(nwords):
            # Zero-initialize from least-significant word until offset reached
            x[i] = 0

        # Copy to x output
        for i in range(nwords, BITVEC_NWORDS):
            x[i] = y[j]
            j += 1
            #print("we do enter this loop")
        #print("this is x ", x)
        # Shift the rest if count was not multiple of bitsize of DTYPE
        nbits &= 31
        #print("nbits = ", nbits, BITVEC_NWORDS - 1)
        if nbits != 0:
            for i in range(BITVEC_NWORDS - 1, 0, -1):
                # print(type(x[i]), type(np.int64(x[i])), type(np.int64(nbits)))  # need to convert the nbits to uint32
                #print(np.uint32(x[i]), np.uint32(nbits))
                #print(np.uint32(x[i]) << np.uint32(nbits))
                #print(np.uint32(x[i - 1]) >> (32 - np.uint32(nbits)))
                x[i] = (np.uint32(x[i]) << np.uint32(nbits)) | (np.uint32(x[i - 1]) >> (32 - np.uint32(nbits)))
                #print("this is x ", i, x)
            x[0] <<= np.uint32(nbits)
        #print("this is x ",  x)
        return x

    # -------------------------------------------------------------------------------
    # Code that does arithmetic on bit-vectors in the Galois Field GF(2^CURVE_DEGREE)
    # -------------------------------------------------------------------------------

    def gf2field_set_one(x):

        """ Set first word to one and the rest to zero """

        x[0] = 1
        for i in range(1, BITVEC_NWORDS):
            x[i] = 0

        return x

    def gf2field_is_one(x):

        """ Check if the bit vector is == 1 """

        if x[0] != 1:
            return False
        else:
            for i in range(1, BITVEC_NWORDS):
                if x[i] != 0:
                    return False
            return True

    def gf2field_add(z, x, y):

        """ Galois field(2^m) addition is modulo 2, so XOR is used instead - 'z := a + b' """
        #print("we are in add ", x, y, BITVEC_NWORDS)
        for i in range(BITVEC_NWORDS):
            z[i] = x[i] ^ y[i]

        return z

    def gf2field_inc(x):

        """ Increment element """

        x[0] ^= 1

        return x

    def gf2field_mul(z, x, y):

        """ Field multiplication 'z := (x * y)' """

        tmp = np.zeros(6, dtype='u4')
        tmp = bitvec_copy(tmp, x)

        # If LSB is set, start with x
        if bitvec_get_bit(y, 0) != 0:
            z = bitvec_copy(z, x)
        else:
            # Else start with zero
            z = bitvec_set_zero(z)

        # Then add 2^i * x for the rest
        for i in range(1, CURVE_DEGREE):

            # Lshift 1 - doubling the value of tmp
            tmp = bitvec_lshift(tmp, tmp, 1)

            # Module reduction polynomial if degree(tmp) > CURVE_DEGREE
            if bitvec_get_bit(tmp, CURVE_DEGREE):
                tmp = gf2field_add(tmp, tmp, polynomial)

            # Add 2^i * tmp if this factor in y is non-zero
            if bitvec_get_bit(y, i):
                z = gf2field_add(z, z, tmp)

        return z

    def gf2field_inv(z, x):

        """ Field inversion 'z := 1/x'"""

        u = np.zeros(6, dtype='u4')
        v = u.copy()
        g = u.copy()
        h = u.copy()

        u = bitvec_copy(u, x)
        v = bitvec_copy(v, polynomial)
        g = bitvec_set_zero(g)
        z = gf2field_set_one(z)

        while not gf2field_is_one(u):
            # print(u.shape, v)
            i = (bitvec_degree(u) - bitvec_degree(v))
            #print(i)
            #print(u, v)

            if i < 0:
                u, v = bitvec_swap(u, v)
                g, z = bitvec_swap(g, z)
                i = -i
            #print(u, v)
            h = bitvec_lshift(h, v, i)
            u = gf2field_add(u, u, h)
            h = bitvec_lshift(h, g, i)
            z = gf2field_add(z, z, h)

        return z

    # -----------------------------------------------------------------------
    # The following code takes care of Galois-Field arithmetic.
    # Elliptic curve points are represented  by pairs (x,y) of bitvec_t.
    # It is assumed that curve coefficient 'a' is {0,1}
    # This is the case for all NIST binary curves.
    # Coefficient 'b' is given in 'coeff_b'.
    # '(base_x, base_y)' is a point that generates a large prime order group.
    # -----------------------------------------------------------------------

    def gf2point_copy(x1, y1, x2, y2):

        """ Copy point (x,y) """

        x1 = bitvec_copy(x1, x2)
        y1 = bitvec_copy(y1, y2)

        return x1, y1

    def gf2point_set_zero(x, y):

        """ Set point (x,y) to zero """

        x = bitvec_set_zero(x)
        y = bitvec_set_zero(y)

        return x, y

    def gf2point_is_zero(x, y):

        """ Check if the point (x,y) is zero"""

        return bitvec_is_zero(x) and bitvec_is_zero(y)

    def gf2point_double(x, y):

        """ Double the point (x,y) """

        l = np.zeros(6, dtype='u4')
        if bitvec_is_zero(x):
            y = bitvec_set_zero(y)
        else:
            l = gf2field_inv(l, x)
            l = gf2field_mul(l, l, y)
            l = gf2field_add(l, l, x)
            y = gf2field_mul(y, x, x)
            x = gf2field_mul(x, l, l)
            if coeff_a == 1:
                l = gf2field_inc(l)
            x = gf2field_add(x, x, l)
            l = gf2field_mul(l, l, x)
            y = gf2field_add(y, y, l)

        return x, y
# ----------------------------------------------------------- here down needs to be converted to C++
    def gf2point_add(x1, y1, x2, y2):

        """ Add two points together (x1, y1) := (x1, y1) + (x2, y2) """

        a = np.zeros(6, dtype='u4')
        b = a.copy()
        c = a.copy()
        d = a.copy()

        if not gf2point_is_zero(x2, y2):
            if gf2point_is_zero(x1, y1):
                x1, y1 = gf2point_copy(x1, y1, x2, y2)
            else:
                if bitvec_equal(x1, x2):
                    if bitvec_equal(y1, y2):
                        x1, y1 = gf2point_double(x1, y1)
                    else:
                        x1, y1 = gf2point_set_zero(x1, y1)
                else:
                    #print("a is " + a)
                    a = gf2field_add(a, y1, y2)
                    #print(a)
                    b = gf2field_add(b, x1, x2)
                    c = gf2field_inv(c, b)
                    c = gf2field_mul(c, c, a)
                    d = gf2field_mul(d, c, c)
                    d = gf2field_add(d, d, c)
                    d = gf2field_add(d, d, b)
                    if coeff_a == 1:
                        d = gf2field_inc(d)
                    x1 = gf2field_add(x1, x1, d)
                    a = gf2field_mul(a, x1, c)
                    a = gf2field_add(a, a, d)
                    y1 = gf2field_add(y1, y1, a)
                    x1 = bitvec_copy(x1, d)

        return x1, y1

    def gf2point_mul(x, y, exp):

        """ Point multiplication via double-and-add algorithm """

        tmpx = np.zeros(6, dtype='u4')
        tmpy = tmpx.copy()

        nbits = bitvec_degree(exp)
        tmpx, tmpy = gf2point_set_zero(tmpx, tmpy)

        for i in range(nbits - 1, -1, -1):
            tmpx, tmpy = gf2point_double(tmpx, tmpy)

            if bitvec_get_bit(exp, i):
                tmpx, tmpy = gf2point_add(tmpx, tmpy, x, y)

        x, y = gf2point_copy(x, y, tmpx, tmpy)

        return x, y

    def gf2point_on_curve(x, y):

        """ Check if y^2 + x*y = x^3 + a*x^2 + coeff_b holds """

        a = np.zeros(6, dtype='u4')
        b = a.copy()

        if gf2point_is_zero(x, y):
            return True
        else:
            a = gf2field_mul(a, x, x)

            if (coeff_a == 0):
                a = gf2field_mul(a, a, x)
            else:
                b = gf2field_mul(b, a, x)
                a = gf2field_add(a, a, b)

            a = gf2field_add(a, a, np.array(coeff_b, dtype=np.uint32))
            b = gf2field_mul(b, y, y)
            a = gf2field_add(a, a, b)
            b = gf2field_mul(b, x, y)

            return bitvec_equal(a, b)

    # -------------------------------------------------------------------
    # Elliptic Curve Diffie-Hellman key exchange protocol.
    # -------------------------------------------------------------------

    def ecdh_generate_keys(private_key, public_key):

        """ Generate public key based on a random private key defined earlier

        Returns:
            np.uint32[]: Clear private key
            np.uint32[]: Associated public key
        """

        # Get copy of "base" point 'G'
        pub1, pub2 = gf2point_copy(public_key[:6], public_key[6:], base_x, base_y)

        # Abort key generation if random number is too small
        if bitvec_degree(private_key) < (CURVE_DEGREE // 2):
            return None, None
        else:
            nbits = bitvec_degree(base_order)
            for i in range(nbits - 1, BITVEC_NWORDS * 32):
                # Clear bits > CURVE_DEGREE in highest word to satisfy constraint 1 <= exp < n
                private_key = bitvec_clr_bit(private_key, i)

            # Multiply base-point with scalar (private-key)
            pub1, pub2 = gf2point_mul(pub1, pub2, private_key)
            public_key = np.append(pub1, pub2)

            return private_key, public_key

    def ecdh_shared_secret(private_key, others_pub):

        """ Calculate shared key between two parties

        Returns:
            np.uint32[]: Shared key
        """

        output = np.zeros(12, dtype='u4')
        others1 = others_pub[:6]
        others2 = others_pub[6:]

        # Do some basic validation of other party's public key
        if not gf2point_is_zero(others1, others2) and gf2point_on_curve(others1, others2):
            for i in range(12):
                # Copy other side's public key to output
                output[i] = others_pub[i]

            # Multiply other side's public key with own private key
            others1, others2 = gf2point_mul(others1, others2, private_key)
            output = np.append(others1, others2)

            return output
        else:
            return None

    # 1. Alice picks a (secret) random natural number 'a', calculates P = a * g and sends P to Bob.
    private = np.zeros(6, dtype='u4')
    public = np.zeros(12, dtype='u4')
    sec = np.zeros(12, dtype='u4')

    for i in range(6):
        private[i] = np.random.randint(4294967296, dtype=np.uint32)

    private, public = ecdh_generate_keys(private, public)

    # 2. Bob picks a (secret) random natural number 'b', calculates Q = b * g and sends Q to Alice.
    private2 = np.zeros(6, dtype='u4')
    public2 = np.zeros(12, dtype='u4')
    sec2 = np.zeros(12, dtype='u4')

    for i in range(6):
        private2[i] = np.random.randint(4294967296, dtype=np.uint32)

    private2, public2 = ecdh_generate_keys(private2, public2)

    # 3. Alice calculates S = a * Q = a * (b * g).
    sec = ecdh_shared_secret(private, public2)

    # 4. Bob calculates T = b * P = b * (a * g).
    sec2 = ecdh_shared_secret(private2, public)

    # 5. Assert equality, i.e. check that both parties calculated the same value.
    try:
        if np.any(sec) and np.any(sec2):
            print(sec, sec2)
            np.array_equal(sec, sec2)
            print("Equal Key")
        else:
            print("Error None Key")
    except AssertionError:
        print("Error Key Not Equal")
