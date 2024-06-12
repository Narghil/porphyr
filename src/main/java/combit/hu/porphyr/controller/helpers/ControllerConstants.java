package combit.hu.porphyr.controller.helpers;

import lombok.NonNull;

public class ControllerConstants {

    // See: messages.properties
    public static final int MENU_ITEM_DEVELOPERS = 1;
    public static final int MENU_ITEM_TASKS = 2;
    public static final int MENU_ITEM_MODIFY = 3;
    public static final int MENU_ITEM_DELETE = 4;
    public static final int MENU_ITEM_POSTS = 5;
    // -- public static final int MENU_ITEM_CANCEL = 6
    public static final int MENU_ITEM_INSERT = 7;
    public static final @NonNull String ERROR_TITLE = "Hiba!";

    public static final int EDIT_LEVEL_READER = 0;
    public static final int EDIT_LEVEL_COMMENTER = 10;
    public static final int EDIT_LEVEL_EDITOR = 20;
    public static final int EDIT_LEVEL_ADMIN = 100;

    public static final String ROLE_ADMIN = "ADMIN";

    private ControllerConstants() {
    }
}


