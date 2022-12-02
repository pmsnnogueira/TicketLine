package pt.isec.pd.ticketline.src.model.server;

public enum MULTICAST {
    IP("239.39.39.39"),
    PORT("4004");

    private final String value;

    MULTICAST(String value){
        this.value = value;
    }

    public static String getValue(int field){
        MULTICAST[] constants = MULTICAST.values();
        return constants[field].value;
    }
}
