package com.example.lib;

import java.io.Serializable;
import java.util.List;

public class JVMData implements Serializable {
    private String maxHeap;
    private String initHeap;
    private String CommittedHeap;
    private String UsedHeap;

    private String maxNoHeap;
    private String initNoHeap;
    private String CommittedNoHeap;
    private String UsedNoHeap;

    private String threadCount;

    private List<JVMThreadInfo> threadInfo;
    private List<JVMThreadInfo> deadLockThreadInfo;

    public List<JVMThreadInfo> getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(List<JVMThreadInfo> threadInfo) {
        this.threadInfo = threadInfo;
    }

    public List<JVMThreadInfo> getDeadLockThreadInfo() {
        return deadLockThreadInfo;
    }

    public void setDeadLockThreadInfo(List<JVMThreadInfo> deadLockThreadInfo) {
        this.deadLockThreadInfo = deadLockThreadInfo;
    }

    public String getMaxHeap() {
        return maxHeap;
    }

    public void setMaxHeap(String maxHeap) {
        this.maxHeap = maxHeap;
    }

    public String getInitHeap() {
        return initHeap;
    }

    public void setInitHeap(String initHeap) {
        this.initHeap = initHeap;
    }

    public String getCommittedHeap() {
        return CommittedHeap;
    }

    public void setCommittedHeap(String committedHeap) {
        CommittedHeap = committedHeap;
    }

    public String getUsedHeap() {
        return UsedHeap;
    }

    public void setUsedHeap(String usedHeap) {
        UsedHeap = usedHeap;
    }

    public String getMaxNoHeap() {
        return maxNoHeap;
    }

    public void setMaxNoHeap(String maxNoHeap) {
        this.maxNoHeap = maxNoHeap;
    }

    public String getInitNoHeap() {
        return initNoHeap;
    }

    public void setInitNoHeap(String initNoHeap) {
        this.initNoHeap = initNoHeap;
    }

    public String getCommittedNoHeap() {
        return CommittedNoHeap;
    }

    public void setCommittedNoHeap(String committedNoHeap) {
        CommittedNoHeap = committedNoHeap;
    }

    public String getUsedNoHeap() {
        return UsedNoHeap;
    }

    public void setUsedNoHeap(String usedNoHeap) {
        UsedNoHeap = usedNoHeap;
    }

    public String getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(String threadCount) {
        this.threadCount = threadCount;
    }


}
