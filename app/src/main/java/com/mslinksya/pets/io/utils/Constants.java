package com.mslinksya.pets.io.utils;

public class Constants {

    public static final int SERVER_RESPONSE_OK = 0;
    public static final String SERVER_LOGIN = "auth";
    public static final String SERVER_SCHEME_HTTPS = "http";
    public static final String SERVER_HOST_CLOUD = "pets-io.herokuapp.com";
    public static final String SERVER_HOST_LOCAL = "192.168.15.27";
    public static final int SERVER_PORT_CLOUD = 80;
    public static final int SERVER_PORT_LOCAL = 5000;

    public static final String SERVER_CREATE_ACCOUNT = "createAccount";
    public static final String SERVER_VERIFY_ACCOUNT = "verifyAccount";
    public static final String SERVER_LOG_OUT = "deauth";
    public static final String SERVER_UPDATE_PET_PICS = "updatePetPictures";
    public static final String SERVER_REGISTER_DEVICE = "registerDevice";
    public static final String SERVER_REQUEST_DEVICES = "devices";
    public static final String SERVER_REQUEST_EVENTS = "events";
    public static final String SERVER_EVENT_PIC = "eventPicture";
    public static final String SERVER_REQUEST_DELETE_EVENT = "deleteEvent";
    public static final String SERVER_REQUEST_EDIT_EVENT = "editEvent";
    public static final String SERVER_REQUEST_INIT_PENDING_PETS = "initPets";
    public static final String SERVER_REQUEST_GET_PETS = "pets";
    public static final String SERVER_REQUEST_GET_PET_PICTURE = "petPicture";
    public static final String SERVER_REQUEST_EDIT_PETS = "editPets";

    public static final int MIN_NAME_LENGTH = 4;
    public static final int MIN_PASSWORD_LENGTH = 4;

    public static final int FCM_GET_TOKEN_TIMEOUT_MS = 10000;

    //CreateAccountActivity
    public static final String TOAST_NETWORK_NOT_DETECTED = "Network not detected\nMake sure you are connected to the internet";
    public static final String TOAST_UNABLE_OPEN_IMAGE = "Unable to open image";
    public static final String MSG_INVALID_EMAIL = "Invalid email!\n";
    public static final String MSG_INVALID_NAME = "Invalid name, your name must have at least " + Constants.MIN_NAME_LENGTH + " characters!\n";
    public static final String MSG_INVALID_PASSWORD = "Invalid password, your password must have at least " + Constants.MIN_PASSWORD_LENGTH + " characters!\n";
    public static final String MSG_PASSWORD_MISMATCH =  "Passwords don't match!";
    public static final String ALERT_INVALID_INPUT = "Input not valid!";

    public static final String USER_NAME_KEY = "UserName";
    public static final String USER_TOKEN_KEY = "UserToken";
    public static final String USER_N_PETS_KEY = "UserNPets";
    public static final String USER_PENDING_PETS_KEY = "UserPendingPets";
    public static final String USER_N_DEVICES_KEY = "UserNDevices";
    public static final String USER_EMAIL_KEY = "UserEmail";
    public static final String USER_PASS_KEY = "UserPass";
    public static final String FCM_TOKEN_KEY = "FCMToken";
    public static final String FCM_PREVIOUS_TOKEN_KEY = "FCMPreviousToken";
    public static final String DEVICE_ID_KEY = "DeviceID";
    public static final String EVENT_ID_KEY = "EventID";
    public static final String EVENT_PICTURE_KEY = "EventPicture";
    public static final String EVENT_PET_KEY = "EventPet";
    public static final String EVENT_EXTRA_KEY = "EventExtra";
    public static final String EVENT_TIMESTAMP_KEY = "Timestamp";
    public static final String PET_PICTURE_KEY = "PetPicture";
    public static final String PET_NAME_KEY = "PetName";
    public static final String PET_ID_KEY = "PetID";
    public static final String PET_OWNER_KEY = "PetOwner";

    public static final String USER_DATA = "UserData";
    public static final String FCM_DATA = "FCMData";

    public static final String KEY_AUTH_TOKEN = "Bearer ";

    public static final String EMAIL_REGEX_STRING = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public static final String BLE_UUID_SERVICE = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    public static final String BLE_UUID_CHAR_SSID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    public static final String BLE_UUID_CHAR_PASS = "995a473b-498e-43d0-9be3-7f3c37ba005f";
    public static final String BLE_UUID_CHAR_TOKN = "70461f4d-4142-4135-9582-8d9a9fc50ea4";
    public static final String BLE_UUID_CHAR_CAM = "dd1e4f59-d6c5-4d65-a2c5-ed2b6d2570dd";
    public static final String BLE_UUID_CHAR_CON = "55246fe7-1105-4395-a472-f26201b3d75c";
    public static final String BLE_UUID_CHAR_REG = "2f6255b5-c4fa-403c-9b18-11f40ffee21b";

    public static final String SHARED_PREFERENCES_BOOLEAN_SETTINGS = "SHARED_PREFERENCES_BOOLEAN_SETTINGS";
    public static final String SETTING_ENV = "SETTING_ENV";
    public static final String SETTING_REGISTER = "SETTING_REGISTER";

    public static final String STATUS_BLE_NOT_INITIALIZED = "not initialized";
    public static final String STATUS_BLE_SUCCESS = "success";
    public static final String STATUS_BLE_ERROR = "error";
    public static final String STATUS_BLE_CONNECTING = "connecting";
    public static final String STATUS_BLE_WAITING = "waiting";
    public static final String STATUS_BLE_SKIPPED = "skipped";
    public static final String STATUS_BLE_REGISTERING = "registering";

    public static final String PETS_IO_BLE_TAG = "Pets.io_PIO";

    public static final int BLE_CONNECT_TIMEOUT_MS = 10000;

    public static final int BLE_SCAN_TIMEOUT_MS = 2000;
}
