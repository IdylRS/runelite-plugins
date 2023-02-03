package com.generatetask;

import com.generatetask.ui.UIButton;
import com.generatetask.ui.UIGraphic;
import com.generatetask.ui.UILabel;
import com.generatetask.ui.UIPage;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

import static com.generatetask.GenerateTaskPlugin.getCenterX;
import static com.generatetask.GenerateTaskPlugin.getCenterY;

public class TaskList extends UIPage {
    private final int TASK_WIDTH = 300;
    private final int TASK_HEIGHT = 75;
    private final int TASK_ITEM_HEIGHT = 36;
    private final int TASK_ITEM_WIDTH = 42;

    private Widget window;
    private List<Task> tasks;
    private HashMap<Integer, Integer> completedTaskIDs;

    private List<UIGraphic> tasksGraphics;

    public TaskList(Widget window, List<Task> tasks, HashMap<Integer, Integer> completedTaskIDs) {
        this.window = window;
        this.tasks = tasks;
        this.completedTaskIDs = completedTaskIDs;

        final int POS_X = getCenterX(window, TASK_WIDTH);

        int i = 0;
        for(Task task : tasks) {
            final int POS_Y = 65+(i*76);

            UIGraphic taskBg = new UIGraphic(window.createChild(-1, WidgetType.GRAPHIC));
            taskBg.setSize(TASK_WIDTH, TASK_HEIGHT);
            taskBg.setPosition(POS_X, POS_Y);

            if(this.completedTaskIDs.get(task.getId()) != null) {
                taskBg.setSprite(GenerateTaskPlugin.TASK_COMPLETE_BACKGROUND_SPRITE_ID);
            }
            else {
                taskBg.setSprite(GenerateTaskPlugin.TASK_BACKGROUND_SPRITE_ID);
            }

            Widget label = window.createChild(-1, WidgetType.TEXT);
            label.setTextColor(Color.WHITE.getRGB());
            label.setTextShadowed(true);
            label.setName(task.getDescription());
            UILabel taskLabel = new UILabel(label);
            taskLabel.setFont(496);
            taskLabel.setPosition(POS_X+60, POS_Y);
            taskLabel.setSize(TASK_WIDTH-60, TASK_HEIGHT);
            taskLabel.setText(task.getDescription());

            Widget taskImageWidget = window.createChild(-1, WidgetType.GRAPHIC);
            UIGraphic taskImage = new UIGraphic(taskImageWidget);
            taskImage.setPosition(POS_X+12, POS_Y+20);
            taskImage.getWidget().setItemQuantityMode(ItemQuantityMode.NEVER);
            taskImage.setSize(42, 36);
            taskImage.setItem(task.getItemID());

            this.add(taskBg);
            this.add(taskLabel);
            this.add(taskImage);

            i++;
        }
    }

}
