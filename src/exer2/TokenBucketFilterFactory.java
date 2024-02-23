package exer2;

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

        public void getToken()  {
            lock.lock();
            try {
                while (currSize == 0) {
                    consumer.await();
                }

                currSize--;
                System.out.println(
                        "Consumer token by thread: , " + Thread.currentThread().getName()
                        + ". CurrSize: " + currSize
                        + ". Time of consumption: " + System.currentTimeMillis());
                producer.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void fill() throws InterruptedException {
            lock.lock();
            try {
                if (currSize > 0) {
                    producer.await();
                }

                int x = ++currSize;
                System.out.println("Incremented number of tokens to: " + x + ". " +
                        "Time of production: " + System.currentTimeMillis());
                consumer.signal();
            } finally {
                lock.unlock();
            }
        }
    }

}
