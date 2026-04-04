package com.thinkschool.coach.utility;

public enum ContentType {
	AUDIO("AUDIO"),
	TEXT("TEXT");
	
	private final String value;

    ContentType(String value) {
        this.value = value;
    }
    
    public static ContentType fromValue(String value) {
        for (ContentType type : ContentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid content type: " + value);
    }
    
    public String getValue() {
        return value;
    }
}
