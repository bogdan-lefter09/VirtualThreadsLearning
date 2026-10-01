import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadOptimized {

    public static void main(String[] args) throws InterruptedException {
        Instant start = Instant.now();
        AtomicInteger completedTasks = new AtomicInteger(0);
        int totalTasks = 10_000_000;

        // Limit concurrent tasks to avoid memory buildup
        Semaphore concurrencyLimit = new Semaphore(500_000);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < totalTasks; i++) {
                final int taskId = i;

                // Block if we've hit the concurrency limit
                concurrencyLimit.acquire();

                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100));
                        processTask(taskId);

                        int completed = completedTasks.incrementAndGet();
                        if (completed % 100_000 == 0) {
                            System.out.println("Completed " + completed + " tasks...");
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        concurrencyLimit.release(); // Free up slot
                    }
                });
            }
        }

        Instant end = Instant.now();
        System.out.println("Execution time: " + Duration.between(start, end).toSeconds() + "s");
        System.out.println("Memory used: " +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB");
    }

    private static void processTask(int taskId) {
        // Simulate some CPU work
        int result = 0;
        for (int i = 0; i < 1000; i++) {
            result += i * taskId;
        }
    }
}