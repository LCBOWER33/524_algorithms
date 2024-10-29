#include <iostream>
#include <vector>
#include <chrono>
#include <memory>
#include "diffie_hellman.h" 
#include "ecdh.h"          
#include "rsa.h"           

size_t memory_used = 0;

void* operator new(size_t size) {
    memory_used += size;
    return malloc(size);
}

void operator delete(void* ptr) noexcept {
    free(ptr);
}

int main() {

    int trials = 1;

    std::vector<double> diffie_hellman_execution_time;
    std::vector<double> diffie_hellman_memory_usage;

    std::vector<double> ECDH_execution_time;
    std::vector<double> ECDH_memory_usage;

    std::vector<double> RSA_execution_time;
    std::vector<double> RSA_memory_usage;

    for (int i = 0; i < trials; i++) {    
        std::cout << "Memory used by myFunction: " << memory_used << " bytes" << std::endl;
        int start_memory = memory_used;

        auto start_time = std::chrono::high_resolution_clock::now();
        diffie_hellman();
        auto end_time = std::chrono::high_resolution_clock::now();
        std::chrono::duration<double> elapsed = end_time - start_time;
        diffie_hellman_execution_time.push_back(elapsed.count());

        diffie_hellman_memory_usage.push_back(memory_used - start_memory);
        std::cout << "Memory used by myFunction: " << memory_used << " bytes" << std::endl;
        // ----------------------------------------
        start_memory = memory_used;
        start_time = std::chrono::high_resolution_clock::now();
        ECDH();
        end_time = std::chrono::high_resolution_clock::now();
        elapsed = end_time - start_time;
        ECDH_execution_time.push_back(elapsed.count());

        ECDH_memory_usage.push_back(memory_used - start_memory);
        std::cout << "Memory used by myFunction: " << memory_used << " bytes" << std::endl;
        // ----------------------------------------
        start_memory = memory_used;
        start_time = std::chrono::high_resolution_clock::now();
        RSA();
        end_time = std::chrono::high_resolution_clock::now();
        elapsed = end_time - start_time;
        RSA_execution_time.push_back(elapsed.count());

        RSA_memory_usage.push_back(memory_used - start_memory);
        std::cout << "Memory used by myFunction: " << memory_used << " bytes" << std::endl;
    }

    for (const auto& time : diffie_hellman_execution_time) {
        std::cout << time << " ";
    }
    for (const auto& time : ECDH_execution_time) {
        std::cout << time << " ";
    }
    for (const auto& time : RSA_execution_time) {
        std::cout << time << " ";
    }
    std::cout << std::endl;

    for (const auto& memory : diffie_hellman_memory_usage) {
        std::cout << memory << " ";
    }
    for (const auto& memory : ECDH_memory_usage) {
        std::cout << memory << " ";
    }
    for (const auto& memory : RSA_memory_usage) {
        std::cout << memory << " ";
    }
    std::cout << std::endl;  // MiB

    double diffie_hellman_average_execution_time = std::accumulate(diffie_hellman_execution_time.begin(), diffie_hellman_execution_time.end(), 0.0) / diffie_hellman_execution_time.size();
    double diffie_hellman_average_memory_usage = std::accumulate(diffie_hellman_memory_usage.begin(), diffie_hellman_memory_usage.end(), 0.0) / diffie_hellman_memory_usage.size();

    double ECDH_average_execution_time = std::accumulate(ECDH_execution_time.begin(), ECDH_execution_time.end(), 0.0) / ECDH_execution_time.size();
    double ECDH_average_memory_usage = std::accumulate(ECDH_memory_usage.begin(), ECDH_memory_usage.end(), 0.0) / ECDH_memory_usage.size();

    double RSA_average_execution_time = std::accumulate(RSA_execution_time.begin(), RSA_execution_time.end(), 0.0) / RSA_execution_time.size();
    double RSA_average_memory_usage = std::accumulate(RSA_memory_usage.begin(), RSA_memory_usage.end(), 0.0) / RSA_memory_usage.size();

    std::cout << "AVERAGE EXECUTION TIME: " << diffie_hellman_average_execution_time << " " << ECDH_average_execution_time << " " << RSA_average_execution_time << std::endl;
    std::cout << "AVERAGE MEMORY USAGE: " << diffie_hellman_average_memory_usage << " " << ECDH_average_memory_usage << " " << RSA_average_memory_usage << std::endl;

    return 0;
}

