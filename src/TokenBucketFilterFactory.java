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

        public TokenBucketFilterImpl(int maxSize) {
            this.maxSize = maxSize;
        }

        Thread fillerThread = new Thread(() -> {
            while(true) {
                fill();
                try {
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
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            currSize--;
            System.out.println(
                    "Obtained token by thread: , " + Thread.currentThread().getName() + ". CurrSize: " + currSize);
        }

        public synchronized void fill() {
            if (currSize < maxSize) {
                int x = ++currSize;
                System.out.println("Incremented number of tokens to: " + x);
            }
            this.notify();
        }
    }

}
