package com.next.move.enums;
public enum SubStatus {
    UNKNOWN(-1),
    APPROVAL_PENDING(0),
    APPROVED(1),
    ACTIVE(2),
    SUSPENDED(3),
    CANCELLED(4),
    EXPIRED(5);
    private final int code;
    SubStatus(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}

/*
| Status                | Meaning                                                                                                             |
| --------------------- | ------------------------------------------------------------------------------------------------------------------- |
| **APPROVAL\_PENDING** | Buyer has been created but not yet approved by the subscriber (e.g. they closed PayPal before finishing).           |
| **APPROVED**          | Subscriber approved the subscription but it has not yet been activated (happens if you require delayed activation). |
| **ACTIVE**            | Subscription is active and billing is happening as scheduled.                                                       |
| **SUSPENDED**         | Temporarily paused — billing is stopped until you (the merchant) or the subscriber reactivates it.                  |
| **CANCELLED**         | Subscription has been canceled by you or the subscriber. It cannot be reactivated.                                  |
| **EXPIRED**           | Subscription reached the end of its billing cycles (natural end of term).                                           |
* */