import java. util.ArrayList;
import java.util.List;
import java.util.concurrent. CountDownLatch;

/**
 * Java 8 example that demonstrates OutOfMemoryError with traditional threads. 
 * Platform-thread stack size depends on the JVM and operating system and is often
 * around 1 MB, so creating too many threads can exhaust available memory.
 */
public class ThreadMemoryError {
    
    public static void main(String[] args) {
        System.out.println("Starting Java 8 Traditional Thread Example");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        
        List<Thread> threads = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        
        try {
            // Try to create 200,000 threads - this will likely cause OutOfMemoryError
            // Stack size is platform-dependent and is often around 1 MB
            for (int i = 0; i < 200_000; i++) {
                final int taskId = i;
                Thread thread = new Thread(() -> {
                    try {
                        // Wait for all threads to be created
                        startLatch.await();
                        
                        // Simulate some work
                        Thread.sleep(100);
                        
                        // Simulate processing
                        processTask(taskId);
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                
                threads.add(thread);
                thread.start();
                
                if (i % 1000 == 0) {
                    System.out.println("Created " + i + " threads.. .");
                }
            }
            
            System.out.println("All threads created!  Total:  " + threads.size());
            
            // Signal all threads to start work
            startLatch.countDown();
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            System.out.println("All tasks completed!");
            
        } catch (OutOfMemoryError e) {
            System.err.println("\n=== OutOfMemoryError occurred!  ===");
            System.err.println("Created " + threads.size() + " threads before running out of memory");
            System. err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted");
        }
    }
    
    private static void processTask(int taskId) {
        // Simulate some CPU work
        int result = 0;
        for (int i = 0; i < 1000; i++) {
            result += i * taskId;
        }
    }
}