package com.example;

import com.example.ui.UIButton;
import com.example.ui.UIGraphic;
import com.example.ui.UILabel;
import com.example.ui.UIPage;
import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

import java.awt.*;

import static com.example.ExamplePlugin.*;

public class TaskDashboard extends UIPage {
    private final int DEFAULT_BUTTON_WIDTH = 140;
    private final int DEFAULT_BUTTON_HEIGHT = 30;

    @Getter
    private Widget window;

    private UILabel confirmAfford;
    private UILabel taskLabel;

    private UIButton completeTaskBtn;
    private UIButton generateTaskBtn;

    public TaskDashboard(Widget window) {
        this.window = window;

        int labelHeight = 100;
        int titleHeight = 50;
        final int POS_X = getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH);
        final int POS_Y = getCenterY(window, labelHeight);

        Widget label = window.createChild(-1, WidgetType.TEXT);
        label.setTextColor(Color.WHITE.getRGB());
        label.setTextShadowed(true);
        label.setName("Task Label");

        Widget afford = window.createChild(-1, WidgetType.TEXT);
        this.confirmAfford = new UILabel(afford);
        this.confirmAfford.setFont(495);
        this.confirmAfford.setSize(COLLECTION_LOG_WINDOW_WIDTH, titleHeight);
        this.confirmAfford.setPosition(getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH), getCenterY(window, titleHeight)+40);

        Widget completeTaskWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.completeTaskBtn = new UIButton(completeTaskWidget);
        this.completeTaskBtn.setSize(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        this.completeTaskBtn.setPosition(getCenterX(window, 140) - (DEFAULT_BUTTON_WIDTH / 2 + 15), getCenterY(window, 30) + 75);

        Widget generateTaskWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.generateTaskBtn = new UIButton(generateTaskWidget);
        this.generateTaskBtn.setSize(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        this.generateTaskBtn.setPosition(getCenterX(window, 140) + (DEFAULT_BUTTON_WIDTH / 2 + 15), getCenterY(window, 30) + 75);
        this.generateTaskBtn.setSprites();

        this.taskLabel = new UILabel(label);
        this.taskLabel.setFont(495);
        this.taskLabel.setPosition(POS_X, POS_Y);
        this.taskLabel.setSize(Math.min(COLLECTION_LOG_WINDOW_WIDTH, window.getWidth()), labelHeight);

        this.add(this.taskLabel);
        this.add(this.completeTaskBtn);
        this.add(this.generateTaskBtn);
        this.add(this.confirmAfford);
    }

    public void setTaskText(String desc) {
        this.taskLabel.setText(desc);
    }
}
