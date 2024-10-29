package com.mycompany._java_code;
import java.util.ArrayList;
import java.util.List;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
/**
 *
 * @author lcbba
 */
public class App {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        diffie_hellman dh = new diffie_hellman();
        ecdh ECDH = new ecdh();
        rsa RSA = new rsa();

        int trials = 1;

        List<Double> diffieHellmanExecutionTime = new ArrayList<>();
        List<Long> diffieHellmanMemoryUsage = new ArrayList<>();

        List<Double> ecdhExecutionTime = new ArrayList<>();
        List<Long> ecdhMemoryUsage = new ArrayList<>();

        List<Double> rsaExecutionTime = new ArrayList<>();
        List<Long> rsaMemoryUsage = new ArrayList<>();
        
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        for (int i = 0; i < trials; i++) {
            long start_memory = memoryMXBean.getHeapMemoryUsage().getUsed();
            long startTime = System.nanoTime();
            dh.diffieHellman();
            long endTime = System.nanoTime();
            diffieHellmanExecutionTime.add((endTime - startTime) / 1e9);

            long end_memory = memoryMXBean.getHeapMemoryUsage().getUsed();
            long used_memory = end_memory - start_memory;
            System.out.println("Memory used by function: " + used_memory + " bytes");
            diffieHellmanMemoryUsage.add(used_memory);
            // ----------------------------------------
            start_memory = memoryMXBean.getHeapMemoryUsage().getUsed();
            startTime = System.nanoTime();
            ECDH.ecdhProcess();
            endTime = System.nanoTime();
            ecdhExecutionTime.add((endTime - startTime) / 1e9);

            end_memory = memoryMXBean.getHeapMemoryUsage().getUsed();
            used_memory = end_memory - start_memory;
            System.out.println("Memory used by function: " + used_memory + " bytes");
            ecdhMemoryUsage.add(used_memory);
            // ----------------------------------------
            start_memory = memoryMXBean.getHeapMemoryUsage().getUsed();
            startTime = System.nanoTime();
            RSA.RSAProcess();
            endTime = System.nanoTime();
            rsaExecutionTime.add((endTime - startTime) / 1e9);

            end_memory = memoryMXBean.getHeapMemoryUsage().getUsed();
            used_memory = end_memory - start_memory;
            System.out.println("Memory used by function: " + used_memory + " bytes");
            rsaMemoryUsage.add(used_memory);
        }

        System.out.println(diffieHellmanExecutionTime + " " + ecdhExecutionTime + " " + rsaExecutionTime);
        System.out.println(diffieHellmanMemoryUsage + " " + ecdhMemoryUsage + " " + rsaMemoryUsage);  // MiB

        double diffieHellmanAverageExecutionTime = calculateAverageD(diffieHellmanExecutionTime);
        double diffieHellmanAverageMemoryUsage = calculateAverageL(diffieHellmanMemoryUsage);

        double ecdhAverageExecutionTime = calculateAverageD(ecdhExecutionTime);
        double ecdhAverageMemoryUsage = calculateAverageL(ecdhMemoryUsage);

        double rsaAverageExecutionTime = calculateAverageD(rsaExecutionTime);
        double rsaAverageMemoryUsage = calculateAverageL(rsaMemoryUsage);

        System.out.println("AVERAGE EXECUTION TIME: " + diffieHellmanAverageExecutionTime + " " + ecdhAverageExecutionTime + " " + rsaAverageExecutionTime);
        System.out.println("AVERAGE MEMORY USAGE: " + diffieHellmanAverageMemoryUsage + " " + ecdhAverageMemoryUsage + " " + rsaAverageMemoryUsage);
    }

    private static double getMaxMemoryUsage(Runnable runnable) {
        // Implement memory usage tracking logic here
        return 0.0; // Placeholder
    }

    private static double calculateAverageD(List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    private static double calculateAverageL(List<Long> list) {
        return list.stream().mapToDouble(Long::longValue).average().orElse(0.0);
    }
}
