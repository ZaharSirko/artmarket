package com.artmarket.user_service.DTO;


public enum UserType {
    ARTIST,
    COLLECTOR,
    GALLERY_OWNER,
    ADMIN;

    public static boolean contains(String role) {
        try {
            UserType.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
