package com.ooas.desktop.warehouse.ui.component;

import javafx.scene.Node;
import javafx.scene.control.MenuButton;

public final class WarehouseEvents {
    private WarehouseEvents() {
    }

    public static boolean isMenuTarget(Object target) {
        Node node = target instanceof Node value ? value : null;
        while (node != null) {
            if (node instanceof MenuButton) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }
}


