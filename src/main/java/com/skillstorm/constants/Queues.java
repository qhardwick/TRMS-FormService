package com.skillstorm.constants;

import lombok.Getter;

@Getter
public enum Queues {
    // Lookup request queues:
    SUPERVISOR_LOOKUP("supervisor-lookup-queue"),
    DEPARTMENT_HEAD_LOOKUP("department-head-lookup-queue"),
    BENCO_LOOKUP("benco-lookup-queue"),

    // Lookup response queues:
    SUPERVISOR_RESPONSE("supervisor-response-queue"),
    DEPARTMENT_HEAD_RESPONSE("department-head-response-queue"),
    BENCO_RESPONSE("benco-response-queue"),

    // Final reimbursement queues:
    ADJUSTMENT_REQUEST("adjustment-request-queue"),
    ADJUSTMENT_RESPONSE("adjustment-response-queue"),
    CANCEL_REQUEST("cancel-request-queue"),

    // Inbox queues:
    INBOX("inbox-queue");

    private final String queue;

    Queues(String queue) {
        this.queue = queue;
    }

    @Override
    public String toString() {
        return queue;
    }
}
