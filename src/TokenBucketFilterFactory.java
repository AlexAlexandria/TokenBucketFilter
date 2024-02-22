import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketFilterFactory {
    private TokenBucketFilterFactory() {}
    static TokenBucketFilterImpl filter;

    public static TokenBucketFilter getTokenBucketFilter(int maxSize) {
        filter = new TokenBucketFilterImpl(maxSize);
        filter.init();
        return filter;
    }

    public static class TokenBucketFilterImpl implements TokenBucketFilter {
        private int maxSize;
        private int currSize;
        Lock lock = new ReentrantLock();
        Condition consumer = lock.newCondition();
        Condition producer = lock.newCondition();

        public TokenBucketFilterImpl(int maxSize) {
            this.maxSize = maxSize;
        }

        Thread fillerThread = new Thread(() -> {
            while(true) {
                try {
                    fill();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        private void init() {
            fillerThread.start();
        }

        public synchronized void getToken()  {
            while (currSize == 0) {
                try {
                    consumer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            currSize--;
            System.out.println(
                    "Obtained token by thread: , " + Thread.currentThread().getName() + ". CurrSize: " + currSize);
            producer.signal();
        }

        public synchronized void fill() throws InterruptedException {
            if (currSize < maxSize) {
                producer.wait();
                int x = ++currSize;
                System.out.println("Incremented number of tokens to: " + x);
            }
            consumer.notify();
        }
    }

}
