package com.github.tlb.server;

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
    private static final Logger logger = Logger.getLogger(EntryRepoFactory.class.getName());

    private Map<String, EntryRepo> repos;
    private final String tlbStoreDir;

    private static interface Creator<T> {
        T create();
    }

    public EntryRepoFactory(File tlbStoreDir) {
        this.tlbStoreDir = tlbStoreDir.getAbsolutePath();
        repos = new ConcurrentHashMap<String, EntryRepo>();
    }

    public SuiteResultRepo createSuiteResultRepo(String namespace) throws ClassNotFoundException, IOException {
        return (SuiteResultRepo) findOrCreate(namespace, SUITE_RESULT, new Creator<EntryRepo>() {
            public EntryRepo create() {
                return new SuiteResultRepo();
            }
        });

    }

    public SuiteTimeRepo createSuiteTimeRepo(String namespace) throws ClassNotFoundException, IOException {
        return (SuiteTimeRepo) findOrCreate(namespace, SUITE_TIME, new Creator<EntryRepo>() {
            public EntryRepo create() {
                return new SuiteTimeRepo();
            }
        });
    }

    public SubsetSizeRepo createSubsetRepo(String namespace) throws IOException, ClassNotFoundException {
        return (SubsetSizeRepo) findOrCreate(namespace, SUBSET_SIZE, new Creator<EntryRepo>() {
            public EntryRepo create() {
                return new SubsetSizeRepo();
            }
        });
    }

    private EntryRepo findOrCreate(String namespace, String type, Creator<? extends EntryRepo> creator) throws IOException, ClassNotFoundException {
        EntryRepo repo;
        String identifier = name(namespace, type);
        synchronized (identifier.intern()) {
            repo = repos.get(identifier);
            if (repo == null) {
                repo = creator.create();
                repos.put(identifier, repo);

                File diskDump = dumpFile(identifier);
                if (diskDump.exists()) {
                    ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(diskDump));
                    repo.load(inStream);
                }
            }
        }
        return repo;
    }

    private File dumpFile(String identifier) {
        return new File(tlbStoreDir, identifier);
    }

    static String name(String namespace, String type) {
        return escape(namespace) + DELIMITER + escape(type);
    }

    private static String escape(String namespace) {
        return namespace.replace(DELIMITER, DELIMITER + DELIMITER);
    }

    @Deprecated //for tests only
    Map<String, EntryRepo> getRepos() {
        return repos;
    }

    public void run() {
        synchronized (repos) {
            for (String identifier : repos.keySet()) {
                try {
                    repos.get(identifier).diskDump(new ObjectOutputStream(new FileOutputStream(dumpFile(identifier))));
                } catch (IOException e) {
                    logger.log(Level.WARNING, String.format("disk dump of %s failed, tlb server may not be able to perform data dependent on next reboot.", identifier), e);
                }
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
