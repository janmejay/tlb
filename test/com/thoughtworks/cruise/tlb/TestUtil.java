package com.thoughtworks.cruise.tlb;

import com.thoughtworks.cruise.tlb.ant.JunitFileResource;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TestUtil {
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
        return FileUtils.readFileToString(new File(com.thoughtworks.cruise.tlb.TestUtil.class.getClassLoader().getResource(filePath).toURI()));
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

    public static class LogFixture {
        private ArrayList<Logger> loggersRegisteredTo;
        private TestUtil.LogFixture.TestHandler handler;

        public void assertHeard(String partOfMessage) {
            assertHeard(partOfMessage, 1);
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

            TestHandler() {
                messages = new ArrayList<String>();
            }

            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            public void flush() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void close() throws SecurityException {
                //To change body of implemented methods use File | Settings | File Templates.
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
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_JOB_NAME, jobName);
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_STAGE_NAME, "stage-1");
        return new SystemEnvironment(map);
    }

    public static File createTempFolder() {
        final File file = new File(System.getProperty(FileUtil.TMP_DIR), UUID.randomUUID().toString());
        file.mkdirs();
        file.deleteOnExit();
        return file;
    }
}
