package com.github.tlb.domain;

import java.io.Serializable;

/**
 * @understands line that represents a record 
 */
public interface Entry extends Serializable {
    String dump();
}
