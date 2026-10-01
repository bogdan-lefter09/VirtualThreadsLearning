import java.time.Duration;
import java.time.Instant;
import java. util.concurrent.Executors;
import java.util.concurrent. atomic.AtomicInteger;

/**
 * Java 21 example using virtual threads. 
 * Virtual threads are lightweight and use significantly less memory than platform threads,
 * but they still consume resources and can cause OutOfMemoryError at sufficiently high counts.
 * This example runs 100 batches of one million tasks each.
 */
public class VirtualThreadEfficient {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Java 21 Virtual Thread Example");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        
        Instant start = Instant.now();
        AtomicInteger completedTasks = new AtomicInteger(0);
        int totalTasks = 0;
        for (int i = 0; i < 100; i++) {
            totalTasks += getTotalTasks(completedTasks);
        }
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        
        System.out.println("\n=== Execution Summary ===");
        System.out.println("Total tasks: " + totalTasks);
        System.out.println("Completed tasks: " + completedTasks.get());
        System.out.println("Execution time: " + duration.toSeconds() + " seconds");
        System.out.println("Tasks per second: " + (totalTasks / Math.max(1, duration.toSeconds())));
        System.out.println("Memory used: " + 
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB");
    }

    private static int getTotalTasks(AtomicInteger completedTasks) {
        int totalTasks = 1_000_000;
        
        // Create a virtual thread per task executor
        try (var executor = Executors. newVirtualThreadPerTaskExecutor()) {
            
            System.out.println("\nSubmitting " + totalTasks + " tasks to virtual threads...");
            
            for (int i = 0; i < totalTasks; i++) {
                final int taskId = i;
                
                executor.submit(() -> {
                    try {
                        // Simulate some work
                        Thread.sleep(Duration.ofMillis(100));
                        
                        // Simulate processing
                        processTask(taskId);
                        
                        int completed = completedTasks.incrementAndGet();
                        
                        if (completed % 10_000 == 0) {
                            System.out.println("Completed " + completed + " tasks...");
                        }
                        
                    } catch (InterruptedException e) {
                        Thread. currentThread().interrupt();
                    }
                });
                
                if (i % 10_000 == 0 && i > 0) {
                    System.out.println("Submitted " + i + " tasks.. .");
                }
            }
            
            System.out.println("All tasks submitted!");
            
            // Executor will wait for all tasks to complete when closed (try-with-resources)
        }
        return totalTasks;
    }
    
    private static void processTask(int taskId) {
        // Simulate some CPU work
        int result = 0;
        for (int i = 0; i < 1000; i++) {
            result += i * taskId;
        }
    }
}