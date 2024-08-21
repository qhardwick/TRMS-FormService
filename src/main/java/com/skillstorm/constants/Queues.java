package com.skillstorm.constants;

public enum Queues {
    // Lookup request queues:
    USER_LOOKUP("user-lookup-queue"),
    SUPERVISOR_LOOKUP("supervisor-lookup-queue"),
    DEPARTMENT_HEAD_LOOKUP("department-head-lookup-queue"),
    BENCO_LOOKUP("benco-lookup-queue"),

    // Lookup response queues:
    USER_RESPONSE("user-response-queue"),
    SUPERVISOR_RESPONSE("supervisor-response-queue"),
    DEPARTMENT_HEAD_RESPONSE("department-head-response-queue"),
    BENCO_RESPONSE("benco-response-queue"),

    // Final reimbursement queues:
    ADJUSTMENT_REQUEST("adjustment-request-queue"),
    ADJUSTMENT_RESPONSE("adjustment-response-queue"),
    CANCEL_REQUEST("cancel-request-queue"),

    // Inbox queues:
    APPROVAL_REQUEST("approval-request-queue"),
    DELETION_REQUEST("deletion-request-queue"),
    AUTO_APPROVAL("automatic-approval-queue");

    private final String queue;

    Queues(String queue) {
        this.queue = queue;
    }

    @Override
    public String toString() {
        return queue;
    }
}
