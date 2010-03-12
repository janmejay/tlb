package com.thoughtworks.cruise.tlb;

import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TestUtil {

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
    public static List<FileResource> files(int ... numbers) {
        ArrayList<FileResource> resources = new ArrayList<FileResource>();
        for (int number : numbers) {
            resources.add(file("base" + number));
        }
        return resources;
    }

    public static FileResource file(String name) {
        return new FileResource(new File(name));
    }

    public static FileResource file(String dir, String name) {
        FileResource fileResource = new FileResource(new Project(), dir + File.separator + name + ".class");
        fileResource.setBaseDir(new File("."));
        return fileResource;
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
