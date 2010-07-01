package com.github.tlb;

/**
 * @understands TBL constants
 */
public interface TlbConstants {
    
    final String CRUISE_SERVER_URL = "CRUISE_SERVER_URL";
    final String CRUISE_PIPELINE_NAME = "CRUISE_PIPELINE_NAME";
    final String CRUISE_STAGE_NAME = "CRUISE_STAGE_NAME";
    final String CRUISE_JOB_NAME = "CRUISE_JOB_NAME";
    final String CRUISE_STAGE_COUNTER = "CRUISE_STAGE_COUNTER";
    final String CRUISE_PIPELINE_COUNTER = "CRUISE_PIPELINE_COUNTER";
    final String CRUISE_PIPELINE_LABEL = "CRUISE_PIPELINE_LABEL";
    final String PASSWORD = "TLB_PASSWORD";
    final String USERNAME = "TLB_USERNAME";
    final String TLB_CRITERIA = "TLB_CRITERIA";
    final String TEST_SUBSET_SIZE_FILE = "tlb/subset_size";
    final String CRITERIA_DEFAULTING_ORDER = "CRITERIA_DEFAULTING_ORDER";
    final String TLB_TMP_DIR = "TLB_TMP_DIR";
    final String TLB_ORDERER = "TLB_ORDERER";

    public interface Server {
        final String REPO_FACTORY = "repo_factory";
        final String FAMILY_NAME = "family_name";
        final String SUBSET_SIZE = "subset_size";
    }
}
