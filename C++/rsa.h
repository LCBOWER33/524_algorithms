#pragma once
#include <iostream>
#include <vector>
#include <set>
#include <random>
#include <numeric>

std::set<int> prime;
int public_key = 0;
int private_key = 0;
int n = 0;

int gcd(int a, int b) {
    // Find Minimum of a and b
    int res = std::min(a, b);

    // Testing divisiblity with all numbers starting from
  // min(a, b) to 1

    while (res > 1) {

        // If any number divide both a and b, so we
        // got the answer
        if (a % res == 0 && b % res == 0)
            break;
        res--;
    }
    return res;
}

// Function to fill the primes set using Sieve of Eratosthenes
void primefiller() {
    std::vector<bool> seive(250, true);
    seive[0] = false;
    seive[1] = false;
    for (int i = 2; i < 250; ++i) {
        for (int j = i * 2; j < 250; j += i) {
            seive[j] = false;
        }
    }

    // Filling the prime numbers
    for (int i = 0; i < seive.size(); ++i) {
        if (seive[i]) {
            prime.insert(i);
        }
    }
}

// Picking a random prime number and erasing that prime number from the set
int pickrandomprime() {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, prime.size() - 1);
    int k = dis(gen);

    auto it = prime.begin();
    std::advance(it, k);
    int ret = *it;
    prime.erase(it);
    return ret;
}

void setkeys() {
    int prime1 = pickrandomprime();  // First prime number
    int prime2 = pickrandomprime();  // Second prime number

    n = prime1 * prime2;
    int fi = (prime1 - 1) * (prime2 - 1);

    int e = 2;
    while (gcd(e, fi) != 1) {
        e++;
    }

    public_key = e;

    int d = 2;
    while ((d * e) % fi != 1) {
        d++;
    }

    private_key = d;
}

// To encrypt the given number
int encrypt(int message) {
    int e = public_key;
    int encrypted_text = 1;
    while (e > 0) {
        encrypted_text *= message;
        encrypted_text %= n;
        e--;
    }
    return encrypted_text;
}

// To decrypt the given number
int decrypt(int encrypted_text) {
    int d = private_key;
    int decrypted = 1;
    while (d > 0) {
        decrypted *= encrypted_text;
        decrypted %= n;
        d--;
    }
    return decrypted;
}

// To encode the message
std::vector<int> encoder(const std::string& message) {
    std::vector<int> encoded;
    for (char letter : message) {
        encoded.push_back(encrypt(static_cast<int>(letter)));
    }
    return encoded;
}

// To decode the message
std::string decoder(const std::vector<int>& encoded) {
    std::string s;
    for (int num : encoded) {
        s += static_cast<char>(decrypt(num));
    }
    return s;
}

void RSA() {
    primefiller();
    setkeys();
    std::string message = "Test Message";
    // Uncomment below for manual input
    // std::cout << "Enter the message\n";
    // std::getline(std::cin, message);

    std::vector<int> coded = encoder(message);

    std::cout << "Initial message:\n" << message << "\n\n";
    std::cout << "The encoded message(encrypted by public key)\n";
    for (int p : coded) {
        std::cout << p;
    }
    std::cout << "\n\nThe decoded message(decrypted by public key)\n";
    std::cout << decoder(coded) << std::endl;
}
