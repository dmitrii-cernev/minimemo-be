package md.cernev.minimemo.entity;

public enum Subscriptions {
    FREE("free"),
    LITE("lite"),
    PRO("pro");

    private final String subscription;

    Subscriptions(String subscription) {
        this.subscription = subscription;
    }

    public String getSubscription() {
        return subscription;
    }

}
