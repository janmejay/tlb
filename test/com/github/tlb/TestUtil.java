package com.github.tlb;

import com.github.tlb.ant.JunitFileResource;
import com.github.tlb.utils.FileUtil;
import com.github.tlb.utils.SystemEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TestUtil {
    private static final int MIN_ANONYMOUS_PORT = 1024;
    private static final int MAX_PORT_NUMBER = 65536;

    public static List<TlbFileResource> tlbFileResources(int ... numbers) {
        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        for (int number : numbers) {
            resources.add(junitFileResource("base" + number));
        }
        return resources;
    }

    public static TlbFileResource junitFileResource(String name) {
        return new JunitFileResource(new File(name));
    }

    public static TlbFileResource tlbFileResource(String dir, String name) {
        JunitFileResource fileResource = new JunitFileResource(new Project(), dir + File.separator + name + ".class");
        fileResource.setBaseDir(new File("."));
        return fileResource;
    }

    public static String fileContents(String filePath) throws IOException, URISyntaxException {
        return FileUtils.readFileToString(new File(TestUtil.class.getClassLoader().getResource(filePath).toURI()));
    }

    public static File createFileInFolder(File folder, String fileName) {
        File file = new File(folder, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        file.deleteOnExit();
        return file;
    }

    public static File mkdirInPwd(String dirName) {
        return mkdirIn(".", dirName);
    }

    public static File mkdirIn(String parent, String dirName) {
        File file = new File(parent, dirName);
        file.mkdirs();
        return file;
    }

    public static String findFreePort() {
        Random random = new Random(System.currentTimeMillis());
        for(int i = 0; i < 10; i++) {
            System.err.println("Attempting to find a free port...");
            int port = MIN_ANONYMOUS_PORT + random.nextInt(MAX_PORT_NUMBER - MIN_ANONYMOUS_PORT);
            System.err.println("Checking port number = " + port);
            try {
                new Socket("localhost", port);
                System.err.println("Busy on port number = " + port);
            } catch (IOException e) {
                System.err.println("Using port = " + port);
                return String.valueOf(port);
            }
        }
        throw new IllegalStateException("Failed to find a free port");
    }

    public static class LogFixture {
        private ArrayList<Logger> loggersRegisteredTo;
        private TestUtil.LogFixture.TestHandler handler;

        public void assertHeard(String partOfMessage) {
            assertHeard(partOfMessage, 1);
        }

        public void clearHistory() {
            handler.clearHistory();
        }

        public void assertHeardException(Exception expected) {
            boolean matched = false;
            for (Throwable actual : handler.execeptions) {
                matched = match(actual, expected);
                if (matched) break;
            }
            assertTrue(String.format("didn't find exception %s in heard throwables", expected), matched);
        }

        private boolean match(Throwable actual, Throwable expected) {
            if (actual == null) {
                return expected == null;
            }
            if (expected.getClass().equals(actual.getClass())) {
                return actual.getMessage().equals(expected.getMessage()) && match(actual.getCause(), expected.getCause());
            }
            return false;
        }


        public void assertHeard(String partOfMessage, int expectedOccurances) {
            int actualOccurances = totalOccurances(partOfMessage);
            assertThat(String.format("log message '%s' should have been heard %s times, but was actually heard %s times in %s statements %s",
                    partOfMessage, expectedOccurances, actualOccurances, handler.messages.size(), handler.messages), actualOccurances, is(expectedOccurances));
        }

        private int totalOccurances(String partOfMessage) {
            int numberOfOccurances = 0;
            for (String message : handler.messages) {
                if (message.contains(partOfMessage)) numberOfOccurances++;
            }
            return numberOfOccurances;
        }

        public void assertNotHeard(String partOfMessage) {
            int actualOccurances = totalOccurances(partOfMessage);
            assertThat(String.format("log message '%s' should NOT have been heard at all, but was actually heard %s times in %s statements %s",
                    partOfMessage, actualOccurances, handler.messages.size(), handler.messages), actualOccurances, is(0));
        }

        class TestHandler extends Handler {
            private ArrayList<String> messages;
            private ArrayList<Throwable> execeptions;

            TestHandler() {
                messages = new ArrayList<String>();
                execeptions = new ArrayList<Throwable>();
            }

            public void publish(LogRecord record) {
                messages.add(record.getMessage());
                Throwable throwable = record.getThrown();
                if (throwable != null) {
                    execeptions.add(throwable);
                }
            }

            public void flush() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void close() throws SecurityException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void clearHistory() {
                messages.clear();
            }
        }

        public LogFixture() {
            loggersRegisteredTo = new ArrayList<Logger>();
            handler = new TestHandler();
        }

        public void startListening() {
            populateLoggers();
            for (Logger logger : loggersRegisteredTo) {
                logger.addHandler(handler);
            }
        }

        private void populateLoggers() {
            Enumeration<String> activeLoggers = LogManager.getLogManager().getLoggerNames();
            while(activeLoggers.hasMoreElements()) {
                String loggerName = activeLoggers.nextElement();
                if (!loggerName.isEmpty()) {
                    loggersRegisteredTo.add(Logger.getLogger(loggerName));
                }
            }
        }

        public void stopListening() {
            for (Logger logger : loggersRegisteredTo) {
                logger.removeHandler(handler);
            }
        }
    }

    public static SystemEnvironment initEnvironment(String jobName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TlbConstants.Cruise.CRUISE_JOB_NAME, jobName);
        map.put(TlbConstants.Cruise.CRUISE_STAGE_NAME, "stage-1");
        return new SystemEnvironment(map);
    }

    public static File createTempFolder() {
        final File file = new File(System.getProperty(FileUtil.TMP_DIR), UUID.randomUUID().toString());
        file.mkdirs();
        file.deleteOnExit();
        return file;
    }
}
