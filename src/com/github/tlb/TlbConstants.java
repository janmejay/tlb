package com.github.tlb;

/**
 * @understands TBL constants
 */
public interface TlbConstants {
    static final String TALK_TO_SERVICE = "TALK_TO_SERVICE";

    public interface Cruise {
        static final String CRUISE_SERVER_URL = "CRUISE_SERVER_URL";
        static final String CRUISE_PIPELINE_NAME = "CRUISE_PIPELINE_NAME";
        static final String CRUISE_STAGE_NAME = "CRUISE_STAGE_NAME";
        static final String CRUISE_JOB_NAME = "CRUISE_JOB_NAME";
        static final String CRUISE_STAGE_COUNTER = "CRUISE_STAGE_COUNTER";
        static final String CRUISE_PIPELINE_COUNTER = "CRUISE_PIPELINE_COUNTER";
        static final String CRUISE_PIPELINE_LABEL = "CRUISE_PIPELINE_LABEL";
    }

    public interface TlbServer {
        static final String JOB_NAMESPACE = "TLB_JOB_NAME";
        static final String URL = "TLB_URL";
        static final String PARTITION_NUMBER = "PARTITION_NUMBER";
        static final String TOTAL_PARTITIONS = "TOTAL_PARTITIONS";
    }

    final String PASSWORD = "TLB_PASSWORD";
    final String USERNAME = "TLB_USERNAME";
    final String TLB_CRITERIA = "TLB_CRITERIA";
    final String TEST_SUBSET_SIZE_FILE = "tlb/subset_size";
    final String CRITERIA_DEFAULTING_ORDER = "CRITERIA_DEFAULTING_ORDER";
    final String TLB_TMP_DIR = "TLB_TMP_DIR";
    final String TLB_ORDERER = "TLB_ORDERER";

    public interface Server {
        final String REPO_FACTORY = "repo_factory";
        final String REQUEST_NAMESPACE = "namespace";
        final String TLB_PORT = "TLB_PORT";
        String TLB_STORE_DIR = "tlb_store";
    }
}
