package com.jimmy.friday.framework.utils;

public interface TransactionConstants {

    /**
     * 所有流程完成后创建，代表该事务已经完成生命周期
     */
    String FILE_SUFFIX_DONE = ".done";

    /**
     * 事务提交的时候创建，记录事务的最终状态
     */
    String FILE_SUFFIX_STATE = ".state";

    /**
     * 新建事务时创建，内容为空
     */
    String FILE_SUFFIX_WAIT = ".wait";

    /**
     * 回调时由{@link TransactionConstants#FILE_SUFFIX_WAIT}改名，记录当前事务回调处理的seq
     */
    String FILE_SUFFIX_LOCK = ".lock";

    String DEFAULT_FILE_PATH = "/data/transaction/";
}
