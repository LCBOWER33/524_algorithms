#include <iostream>
#include <vector>
#include <random>
#include <cstdint>
#include <cstring>

using namespace std;

const int CURVE_DEGREE = 163;
const int BITVEC_MARGIN = 3;
const int BITVEC_NBITS = (CURVE_DEGREE + BITVEC_MARGIN);
const int BITVEC_NWORDS = (BITVEC_NBITS + 31) / 32;
const int BITVEC_NBYTES = BITVEC_NWORDS * 4;

// NIST B-163 parameters
vector<uint32_t> coeff_a = { 1 };
uint32_t cofactor = 2;
vector<uint32_t> polynomial = { 0x000000c9, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000008 };
vector<uint32_t> base_x = { 0xe8343e36, 0xd4994637, 0xa0991168, 0x86a2d57e, 0xf0eba162, 0x00000003 };
vector<uint32_t> base_y = { 0x797324f1, 0xb11c5c0c, 0xa2cdd545, 0x71a0094f, 0xd51fbc6c, 0x00000000 };
vector<uint32_t> base_order = { 0xa4234c33, 0x77e70c12, 0x000292fe, 0x00000000, 0x00000000, 0x00000004 };
vector<uint32_t> coeff_b = { 0x4a3205fd, 0x512f7874, 0x1481eb10, 0xb8c953ca, 0x0a601907, 0x00000002 };

bool bitvec_get_bit(const vector<uint32_t>& x, int idx) {
    if (idx / 32 < x.size()) {  // Check range
        return (x[idx / 32] >> (idx % 31)) & 1;
    }
    return 0;
}

void bitvec_clr_bit(vector<uint32_t>& x, int idx) {
    if (idx / 32 < x.size()) {  // Check range
        x[idx / 32] &= ~(1 << (idx % 31));
    }
}

void bitvec_copy(vector<uint32_t>& x, const vector<uint32_t>& y) {
    size_t copy_size = min(x.size(), y.size());
    for (size_t i = 0; i < copy_size; i++) {
        x[i] = y[i];
    }
}

void bitvec_swap(vector<uint32_t>& x, vector<uint32_t>& y) {
    vector<uint32_t> tmp = x;
    bitvec_copy(x, y);
    bitvec_copy(y, tmp);
}

bool bitvec_equal(const vector<uint32_t>& x, const vector<uint32_t>& y) {
    if (x.size() != y.size()) return false;
    for (size_t i = 0; i < x.size(); i++) {
        if (x[i] != y[i]) return false;
    }
    return true;
}

void bitvec_set_zero(vector<uint32_t>& x) {
    fill(x.begin(), x.end(), 0);
}

bool bitvec_is_zero(const vector<uint32_t>& x) {
    for (const auto& i : x) {
        if (i != 0) return false;
    }
    return true;
}

int bitvec_degree(const vector<uint32_t>& x) {
    int i = BITVEC_NWORDS * 32;
    int u = BITVEC_NWORDS;

    while (i > 0 && u > 0 && x[u - 1] == 0) {
        i -= 32;
        u -= 1;
    }

    if (i != 0 && u > 0) {
        uint32_t u32mask = 1 << 31;
        while ((x[u - 1] & u32mask) == 0) {
            u32mask >>= 1;
            i -= 1;
        }
    }
    return i;
}

void bitvec_lshift(vector<uint32_t>& x, const vector<uint32_t>& y, int nbits) {
    int nwords = nbits / 32;
    //cout << nbits << " " << nwords << "\n";
    if (nwords < BITVEC_NWORDS) fill(x.begin(), x.begin() + nwords, 0);

    int j = 0;
    for (int i = nwords; i < BITVEC_NWORDS && j < y.size(); i++) {
        x[i] = y[j++];
    }

    nbits &= 31;
    if (nbits != 0) {
        for (int i = BITVEC_NWORDS - 1; i > 0; i--) {
            x[i] = (x[i] << nbits) | (x[i - 1] >> (32 - nbits));
        }
        x[0] <<= nbits;
    }
}

void gf2field_set_one(vector<uint32_t>& x) {
    if (!x.empty()) x[0] = 1;
    for (int i = 1; i < BITVEC_NWORDS; i++) x[i] = 0;
}

bool gf2field_is_one(const vector<uint32_t>& x) {
    if (x[0] != 1) return false;
    for (int i = 1; i < BITVEC_NWORDS; i++) {
        if (x[i] != 0) return false;
    }
    return true;
}

void gf2field_add(vector<uint32_t>& z, const vector<uint32_t>& x, const vector<uint32_t>& y) {
    for (size_t i = 0; i < z.size(); i++) {
        z[i] = (i < x.size() ? x[i] : 0) ^ (i < y.size() ? y[i] : 0);
    }
}

void gf2field_mul(vector<uint32_t>& z, const vector<uint32_t>& x, const vector<uint32_t>& y) {
    vector<uint32_t> tmp(BITVEC_NWORDS, 0);
    bitvec_copy(tmp, x);
    bitvec_set_zero(z);

    for (int i = 0; i < CURVE_DEGREE; i++) {
        if (bitvec_get_bit(y, i)) gf2field_add(z, z, tmp);

        bitvec_lshift(tmp, tmp, 1);
        if (bitvec_get_bit(tmp, CURVE_DEGREE)) gf2field_add(tmp, tmp, polynomial);
    }
}

void gf2field_inv(vector<uint32_t>& z, const vector<uint32_t>& x) {
    vector<uint32_t> u(BITVEC_NWORDS, 0), v(BITVEC_NWORDS, 0), g(BITVEC_NWORDS, 0), h(BITVEC_NWORDS, 0);
    bitvec_copy(u, x);
    bitvec_copy(v, polynomial);
    bitvec_set_zero(g);
    gf2field_set_one(z);

    int count = 0;
    //cout << u.data();
    while (!gf2field_is_one(u)) {
        int i = bitvec_degree(u) - bitvec_degree(v);
        //cout << i << "\n";
        if (i < 0) {
            bitvec_swap(u, v);
            bitvec_swap(g, z);
            i = -i;
        }
        bitvec_lshift(h, v, i);
        gf2field_add(u, u, h);
        bitvec_lshift(h, g, i);
        gf2field_add(z, z, h);

        count += 2;
    }
    //cout << "count = " << count << "\n";
}

void gf2point_copy(vector<uint32_t>& x1, vector<uint32_t>& y1, const vector<uint32_t>& x2, const vector<uint32_t>& y2) {
    bitvec_copy(x1, x2);
    bitvec_copy(y1, y2);
}

void gf2point_set_zero(vector<uint32_t>& x, vector<uint32_t>& y) {
    bitvec_set_zero(x);
    bitvec_set_zero(y);
}

bool gf2point_is_zero(const vector<uint32_t>& x, const vector<uint32_t>& y) {
    return bitvec_is_zero(x) && bitvec_is_zero(y);
}

void gf2point_double(vector<uint32_t>& x, vector<uint32_t>& y) {
    vector<uint32_t> l(6, 0);
    if (bitvec_is_zero(x)) {
        bitvec_set_zero(y);
    }
    else {
        gf2field_inv(l, x);
        //cout << "we are out of inv";
        gf2field_mul(l, l, y);
        gf2field_add(l, l, x);
        gf2field_mul(y, x, x);
        gf2field_mul(x, l, l);
        if (bitvec_get_bit(x, CURVE_DEGREE)) {
            gf2field_add(x, x, polynomial);
        }
        gf2field_add(x, x, l);
        gf2field_add(x, x, coeff_b);
        gf2field_add(y, y, x);
    }
}

void gf2point_add(vector<uint32_t>& x1, vector<uint32_t>& y1, const vector<uint32_t>& x2, const vector<uint32_t>& y2) {
    vector<uint32_t> l(6, 0);

    if (gf2point_is_zero(x2, y2)) {
        return; // No-op if the second point is the identity point
    }
    else if (gf2point_is_zero(x1, y1)) {
        gf2point_copy(x1, y1, x2, y2); // If the first point is zero, copy the second point
    }
    else {
        gf2field_add(l, x1, x2); // Calculate l = (x1 + x2)
        gf2field_inv(l, l); // l = 1 / l (invert l in the field)
        gf2field_add(l, y1, y2); // l = y1 + y2
        gf2field_mul(l, l, l); // l = l * l

        gf2field_add(l, l, l); // l = 2 * l, double for affine point addition
        gf2field_add(l, l, coeff_a); // Add the curve coefficient

        // Intermediate variables for storing new x3, y3
        vector<uint32_t> x3(6, 0);
        vector<uint32_t> y3(6, 0);

        gf2field_add(x3, x1, x2); // x3 = x1 + x2
        gf2field_mul(x3, x3, x3); // x3 = (x1 + x2)^2
        gf2field_add(x3, x3, l); // x3 = x3 + l
        gf2field_add(x3, x3, coeff_b); // x3 = x3 + b

        gf2field_add(y3, x1, x3); // y3 = x1 + x3
        gf2field_mul(y3, y3, l); // y3 = y3 * l
        gf2field_add(y3, y3, y1); // y3 = y3 + y1

        gf2point_copy(x1, y1, x3, y3); // Set x1, y1 to the new values of x3, y3
    }
}

void gf2point_mul(vector<uint32_t>& x, vector<uint32_t>& y, const vector<uint32_t>& k) {
    vector<uint32_t> x1(6, 0);
    vector<uint32_t> y1(6, 0);
    gf2point_set_zero(x1, y1);

    vector<uint32_t> x2(6, 0);
    vector<uint32_t> y2(6, 0);
    bitvec_copy(x2, x);
    bitvec_copy(y2, y);

    for (int i = 0; i < CURVE_DEGREE; i++) {
        if (bitvec_get_bit(k, i)) {
            gf2point_add(x1, y1, x2, y2);
        }
        gf2point_double(x2, y2);
    }

    gf2point_copy(x, y, x1, y1);
}

void ecdh_generate_keys(vector<uint32_t>& private_key, vector<uint32_t>& public_key) {
    random_device rd;
    mt19937 gen(rd());
    uniform_int_distribution<uint32_t> dis(0, 0xFFFFFFFF);

    for (int i = 0; i < 6; i++) {
        private_key[i] = dis(gen);
    }

    vector<uint32_t> pub_x(base_x); // Wrap base_x in a vector
    vector<uint32_t> pub_y(base_y); // Wrap base_y in a vector
    gf2point_mul(pub_x, pub_y, private_key);

    public_key = pub_x;
    public_key.insert(public_key.end(), pub_y.begin(), pub_y.end());
}


vector<uint32_t> ecdh_shared_secret(const vector<uint32_t>& private_key, const vector<uint32_t>& public_key) {
    vector<uint32_t> pub_x(public_key.begin(), public_key.begin() + 6);
    vector<uint32_t> pub_y(public_key.begin() + 6, public_key.end());
    gf2point_mul(pub_x, pub_y, private_key);

    pub_x.insert(pub_x.end(), pub_y.begin(), pub_y.end());
    return pub_x;
}

void ECDH() {
    vector<uint32_t> private_key(6, 0);
    vector<uint32_t> public_key;

    ecdh_generate_keys(private_key, public_key);

    cout << "Private Key: ";
    for (const auto& v : private_key) {
        cout << hex << v << " ";
    }
    cout << endl;

    cout << "Public Key: ";
    for (const auto& v : public_key) {
        cout << hex << v << " ";
    }
    cout << endl;

    vector<uint32_t> peer_private_key(6, 0);
    vector<uint32_t> peer_public_key;

    ecdh_generate_keys(peer_private_key, peer_public_key);

    vector<uint32_t> shared_secret = ecdh_shared_secret(private_key, peer_public_key);
    vector<uint32_t> shared_secret2 = ecdh_shared_secret(peer_private_key, public_key);

    cout << "Shared Secret: ";
    for (const auto& v : shared_secret) {
        cout << hex << v << " ";
    }
    cout << endl;

    cout << "Shared Secret 2: ";
    for (const auto& v : shared_secret2) {
        cout << hex << v << " ";
    }
    cout << endl;

    cout << "Keys match: " << (shared_secret == shared_secret2) << endl;
}
