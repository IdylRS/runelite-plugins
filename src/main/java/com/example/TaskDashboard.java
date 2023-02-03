package com.example;

import com.example.ui.UIButton;
import com.example.ui.UIGraphic;
import com.example.ui.UILabel;
import com.example.ui.UIPage;
import lombok.Getter;
import net.runelite.api.FontID;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

import java.awt.*;

import static com.example.ExamplePlugin.*;

public class TaskDashboard extends UIPage {
    private final int DEFAULT_BUTTON_WIDTH = 140;
    private final int DEFAULT_BUTTON_HEIGHT = 30;
    private final int DEFAULT_TASK_DETAILS_WIDTH = 300;
    private final int DEFAULT_TASK_DETAILS_HEIGHT = 75;
    private final int GENERATE_TASK_SPRITE_ID = -20001;
    private final int COMPLETE_TASK_SPRITE_ID = -20000;
    private final int GENERATE_TASK_HOVER_SPRITE_ID = -20003;
    private final int COMPLETE_TASK_HOVER_SPRITE_ID = -20002;
    private final int GENERATE_TASK_DISABLED_SPRITE_ID = -20005;
    private final int COMPLETE_TASK_DISABLED_SPRITE_ID = -20004;
    private final int TASK_BACKGROUND_SPRITE_ID = -20006;

    @Getter
    private Widget window;
    private ExamplePlugin plugin;

    private UIPage taskDetails;

    private UILabel title;
    private UILabel taskLabel;

    private UIGraphic taskImage;
    private UIGraphic taskBg;

    private UIButton completeTaskBtn;
    private UIButton generateTaskBtn;

    public TaskDashboard(ExamplePlugin plugin, Widget window) {
        this.window = window;
        this.plugin = plugin;

        createTaskDetails();

        Widget titleWidget = window.createChild(-1, WidgetType.TEXT);
        this.title = new UILabel(titleWidget);
        this.title.setFont(FontID.QUILL_CAPS_LARGE);
        this.title.setSize(COLLECTION_LOG_WINDOW_WIDTH, DEFAULT_TASK_DETAILS_HEIGHT);
        this.title.setPosition(getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH), 80);
        this.title.setText("Current Task");

        Widget completeTaskWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.completeTaskBtn = new UIButton(completeTaskWidget);
        this.completeTaskBtn.setSize(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        this.completeTaskBtn.setPosition(getCenterX(window, DEFAULT_BUTTON_WIDTH) + (DEFAULT_BUTTON_WIDTH / 2 + 15), getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 100);
        this.completeTaskBtn.setSprites(COMPLETE_TASK_SPRITE_ID, COMPLETE_TASK_HOVER_SPRITE_ID);

        Widget generateTaskWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.generateTaskBtn = new UIButton(generateTaskWidget);
        this.generateTaskBtn.setSize(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        this.generateTaskBtn.setPosition(getCenterX(window, DEFAULT_BUTTON_WIDTH) - (DEFAULT_BUTTON_WIDTH / 2 + 15), getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 100);
        this.generateTaskBtn.setSprites(GENERATE_TASK_SPRITE_ID, GENERATE_TASK_HOVER_SPRITE_ID);

        this.add(this.title);
        this.add(this.taskBg);
        this.add(this.taskLabel);
        this.add(this.taskImage);
        this.add(this.completeTaskBtn);
        this.add(this.generateTaskBtn);
    }

    private void createTaskDetails() {
        final int POS_X = getCenterX(window, DEFAULT_TASK_DETAILS_WIDTH);
        final int POS_Y = getCenterY(window, DEFAULT_TASK_DETAILS_HEIGHT)+35;

        Widget taskBgWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.taskBg = new UIGraphic(taskBgWidget);
        this.taskBg.setSize(DEFAULT_TASK_DETAILS_WIDTH, DEFAULT_TASK_DETAILS_HEIGHT);
        this.taskBg.setPosition(POS_X, POS_Y);
        this.taskBg.setSprite(TASK_BACKGROUND_SPRITE_ID);

        Widget label = window.createChild(-1, WidgetType.TEXT);
        label.setTextColor(Color.WHITE.getRGB());
        label.setTextShadowed(true);
        label.setName("Task Label");
        this.taskLabel = new UILabel(label);
        this.taskLabel.setFont(496);
        this.taskLabel.setPosition(POS_X+60, POS_Y);
        this.taskLabel.setSize(DEFAULT_TASK_DETAILS_WIDTH-60, DEFAULT_TASK_DETAILS_HEIGHT);

        Widget taskImageWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.taskImage = new UIGraphic(taskImageWidget);
        this.taskImage.setPosition(POS_X+12, POS_Y+20);
        this.taskImage.getWidget().setItemQuantityMode(ItemQuantityMode.NEVER);
        this.taskImage.setSize(42, 36);

    }

    public void setTask(String desc, int taskItemID) {
        this.taskLabel.setText(desc);
        this.taskImage.setItem(taskItemID);
    }

    public void disableGenerateTask() {
        this.generateTaskBtn.setSprites(GENERATE_TASK_DISABLED_SPRITE_ID);
        this.generateTaskBtn.clearActions();

        this.generateTaskBtn.addAction("Disabled", plugin::playFailSound);

        this.enableCompleteTask();
    }

    public void enableGenerateTask() {
        this.generateTaskBtn.clearActions();
        this.generateTaskBtn.setSprites(GENERATE_TASK_SPRITE_ID, GENERATE_TASK_HOVER_SPRITE_ID);
        this.generateTaskBtn.addAction("Generate task", plugin::generateTask);

        this.disableCompleteTask();
    }

    public void disableCompleteTask() {
        this.completeTaskBtn.setSprites(COMPLETE_TASK_DISABLED_SPRITE_ID);
        this.completeTaskBtn.clearActions();

        this.completeTaskBtn.addAction("Disabled", plugin::playFailSound);
    }

    public void enableCompleteTask() {
        this.completeTaskBtn.clearActions();
        this.completeTaskBtn.setSprites(COMPLETE_TASK_SPRITE_ID, COMPLETE_TASK_HOVER_SPRITE_ID);
        this.completeTaskBtn.addAction("Complete", plugin::completeTask);
    }
}
