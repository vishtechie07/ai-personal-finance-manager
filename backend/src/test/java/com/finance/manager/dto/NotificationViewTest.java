package com.finance.manager.dto;

import com.finance.manager.model.AppNotification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationViewTest {

    @Test
    void from_mapsBudgetWarningToBudgetsPath() {
        AppNotification n = new AppNotification();
        n.setType(AppNotification.NotificationType.BUDGET_WARNING);
        n.setTitle("Groceries");
        n.setMessage("Over budget");

        NotificationView view = NotificationView.from(n);
        assertEquals("/budgets", view.actionPath());
    }

    @Test
    void from_mapsBillDueToBillsPath() {
        AppNotification n = new AppNotification();
        n.setType(AppNotification.NotificationType.BILL_DUE);
        n.setTitle("Electric");
        n.setMessage("Due soon");

        assertEquals("/bills", NotificationView.from(n).actionPath());
    }
}
