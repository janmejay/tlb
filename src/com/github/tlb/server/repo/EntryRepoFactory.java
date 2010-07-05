package com.github.tlb.server.repo;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands creation of EntryRepo
 */
public class EntryRepoFactory implements Runnable {
    public static final String DELIMITER = "|";
    public static final String SUBSET_SIZE = "subset_size";
    public static final String SUITE_TIME = "suite_time";
    public static final String SUITE_RESULT = "suite_result";
    public static final String LATEST_VERSION = "LATEST";
    private static final Logger logger = Logger.getLogger(EntryRepoFactory.class.getName());

    private Map<String, EntryRepo> repos;
    private final String tlbStoreDir;

    static interface Creator<T> {
        T create();
    }

    public EntryRepoFactory(File tlbStoreDir) {
        this.tlbStoreDir = tlbStoreDir.getAbsolutePath();
        repos = new ConcurrentHashMap<String, EntryRepo>();
    }

    public SuiteResultRepo createSuiteResultRepo(final String namespace, final String version) throws ClassNotFoundException, IOException {
        return (SuiteResultRepo) findOrCreate(namespace, version, SUITE_RESULT, new Creator<SuiteResultRepo>() {
            public SuiteResultRepo create() {
                return new SuiteResultRepo();
            }
        });

    }

    public SuiteTimeRepo createSuiteTimeRepo(final String namespace, final String version) throws ClassNotFoundException, IOException {
        return (SuiteTimeRepo) findOrCreate(namespace, version, SUITE_TIME, new Creator<SuiteTimeRepo>() {
            public SuiteTimeRepo create() {
                return new SuiteTimeRepo();
            }
        });
    }

    public SubsetSizeRepo createSubsetRepo(final String namespace, final String version) throws IOException, ClassNotFoundException {
        return (SubsetSizeRepo) findOrCreate(namespace, version, SUBSET_SIZE, new Creator<SubsetSizeRepo>() {
            public SubsetSizeRepo create() {
                return new SubsetSizeRepo();
            }
        });
    }

    EntryRepo findOrCreate(String namespace, String version, String type, Creator<? extends EntryRepo> creator) throws IOException, ClassNotFoundException {
        String identifier = name(namespace, version, type);
        synchronized (identifier.intern()) {
            EntryRepo repo = repos.get(identifier);
            if (repo == null) {
                repo = creator.create();
                repo.setFactory(this);
                repo.setNamespace(namespace);
                repos.put(identifier, repo);

                File diskDump = dumpFile(identifier);
                if (diskDump.exists()) {
                    ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(diskDump));
                    repo.load(inStream);
                }
            }
            return repo;
        }
    }

    private File dumpFile(String identifier) {
        return new File(tlbStoreDir, identifier);
    }

    public static String name(String namespace, String version, String type) {
        return escape(namespace) + DELIMITER + escape(version) + DELIMITER + escape(type);
    }

    private static String escape(String str) {
        return str.replace(DELIMITER, DELIMITER + DELIMITER);
    }

    @Deprecated //for tests only
    Map<String, EntryRepo> getRepos() {
        return repos;
    }

    public void run() {
        for (String identifier : repos.keySet()) {
            try {
                //don't care about a couple entries not being persisted(at teardown), as client is capable of balancing on averages(treat like new suites)
                synchronized (identifier.intern()) {
                    repos.get(identifier).diskDump(new ObjectOutputStream(new FileOutputStream(dumpFile(identifier))));
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, String.format("disk dump of %s failed, tlb server may not be able to perform data dependent on next reboot.", identifier), e);
            }
        }
    }

    public void registerExitHook() {
        Runtime.getRuntime().addShutdownHook(exitHook());
    }

    public Thread exitHook() {
        return new Thread(this);
    }
}
