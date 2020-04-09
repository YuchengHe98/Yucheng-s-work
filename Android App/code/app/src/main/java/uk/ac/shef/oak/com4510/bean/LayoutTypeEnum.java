package uk.ac.shef.oak.com4510.bean;

public enum LayoutTypeEnum {
    ALL_PHOTOS(0, "ALL_PHOTOS"),
    PATH_PHOTOS(1, "PATH_PHOTOS"),
    PATH_LIST(2, "PATH_LIST");

    private final int value;
    private final String desc;

    private LayoutTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
