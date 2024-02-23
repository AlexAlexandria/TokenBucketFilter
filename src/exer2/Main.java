package exer2;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TokenBucketFilter filter = TokenBucketFilterFactory
                .getTokenBucketFilter(10);
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(filter::getToken);
            threadList.add(thread);
        }

        Thread.sleep(5000);

        threadList.forEach(Thread::start);

        for (Thread thread : threadList) {
            thread.join();
        }

    }
}
