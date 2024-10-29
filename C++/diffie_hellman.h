#pragma once
#include <iostream>
using namespace std;

// Power function to return value of a^b mod P
int power(int a, int b, int p) {
    if (b == 1) {
        return a;
    }
    else {
        return (static_cast<long long>(pow(a, b)) % p);
    }
}

void diffie_hellman() {
    // Both persons agree upon the public keys G and P
    // A prime number P is taken
    int P = 23;  // primes.generateLargePrime();
    cout << "The value of P: " << P << endl;

    // A primitive root for P, G is taken
    int G = 9;
    cout << "The value of G: " << G << endl;

    // Alice chooses the private key a
    int a = 4;
    cout << "The private key a for Alice: " << a << endl;

    // Gets the generated key
    int x = power(G, a, P);

    // Bob chooses the private key b
    int b = 3;
    cout << "The private key b for Bob: " << b << endl;

    // Gets the generated key
    int y = power(G, b, P);

    // Generating the secret key after the exchange of keys
    int ka = power(y, a, P);  // Secret key for Alice
    int kb = power(x, b, P);  // Secret key for Bob

    cout << "Secret key for Alice is: " << ka << endl;
    cout << "Secret key for Bob is: " << kb << endl;
}
