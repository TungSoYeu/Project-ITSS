package com.ooas.desktop.warehouse.application;

import com.ooas.desktop.shared.exception.DatabaseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public final class WarehouseAsync {
    private final Label status;
    private final Runnable logout;

    public WarehouseAsync(Label status, Runnable logout) {
        this.status = status;
        this.logout = logout;
    }

    public <T> void call(String message, CompletableFuture<T> future, Consumer<T> success) {
        status.setText(message);
        future.whenComplete((value, error) -> Platform.runLater(() -> complete(value, error, success)));
    }

    public void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private <T> void complete(T value, Throwable error, Consumer<T> success) {
        if (error == null) {
            success.accept(value);
            status.setText("Sẵn sàng");
            return;
        }
        Throwable cause = rootCause(error);
        status.setText(cause.getMessage());
        alert(Alert.AlertType.ERROR, "Yêu cầu thất bại", cause.getMessage());
        if (cause instanceof DatabaseException exception && exception.statusCode() == 401) {
            logout.run();
        }
    }

    private Throwable rootCause(Throwable error) {
        Throwable current = error;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}


