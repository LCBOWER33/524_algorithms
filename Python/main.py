import time
import diffie_hellman as dh
import ecdh
import rsa
import tracemalloc

if __name__ == '__main__':
    trials = 1

    diffie_hellman_execution_time = []
    diffie_hellman_memory_usage = []

    ECDH_execution_time = []
    ECDH_memory_usage = []

    RSA_execution_time = []
    RSA_memory_usage = []

    for i in range(trials):
        tracemalloc.start()
        start_time = time.time()
        dh.diffie_hellman()
        end_time = time.time()
        diffie_hellman_execution_time.append(end_time - start_time)

        diffie_hellman_memory_usage.append(tracemalloc.get_traced_memory()[1])
        print("Memory used by function: " + str(tracemalloc.get_traced_memory()) + " bytes")
        tracemalloc.stop()
        # ----------------------------------------
        tracemalloc.start()
        start_time = time.time()
        ecdh.ECDH()
        end_time = time.time()
        ECDH_execution_time.append(end_time - start_time)

        ECDH_memory_usage.append(tracemalloc.get_traced_memory()[1])
        print("Memory used by function: " + str(tracemalloc.get_traced_memory()) + " bytes")
        tracemalloc.stop()
        # ----------------------------------------
        tracemalloc.start()
        start_time = time.time()
        rsa.RSA()
        end_time = time.time()
        RSA_execution_time.append(end_time - start_time)

        RSA_memory_usage.append(tracemalloc.get_traced_memory()[1])
        print("Memory used by function: " + str(tracemalloc.get_traced_memory()) + " bytes")
        tracemalloc.stop()

    print(diffie_hellman_execution_time, ECDH_execution_time, RSA_execution_time)
    print(diffie_hellman_memory_usage, ECDH_memory_usage, RSA_memory_usage)  # MiB

    diffie_hellman_average_execution_time = sum(diffie_hellman_execution_time) / len(diffie_hellman_execution_time)
    diffie_hellman_average_memory_usage = sum(diffie_hellman_memory_usage) / len(diffie_hellman_memory_usage)

    ECDH_average_execution_time = sum(ECDH_execution_time) / len(ECDH_execution_time)
    ECDH_average_memory_usage = sum(ECDH_memory_usage) / len(ECDH_memory_usage)

    RSA_average_execution_time = sum(RSA_execution_time) / len(RSA_execution_time)
    RSA_average_memory_usage = sum(RSA_memory_usage) / len(RSA_memory_usage)

    print("AVERAGE EXECUTION TIME: ", diffie_hellman_average_execution_time, ECDH_average_execution_time, RSA_average_execution_time)
    print("AVERAGE MEMORY USAGE: ", diffie_hellman_average_memory_usage, ECDH_average_memory_usage, RSA_average_memory_usage)
