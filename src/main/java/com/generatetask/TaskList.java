package com.generatetask;

import com.generatetask.ui.UIButton;
import com.generatetask.ui.UIGraphic;
import com.generatetask.ui.UILabel;
import com.generatetask.ui.UIPage;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.generatetask.GenerateTaskPlugin.getCenterX;

@Slf4j
public class TaskList extends UIPage {
    private final int OFFSET_X = 10;
    private final int OFFSET_Y = 58;
    private final int CANVAS_WIDTH = 480;
    private final int CANVAS_HEIGHT = 252;
    private final int TASK_WIDTH = 300;
    private final int TASK_HEIGHT = 50;
    private final int TASK_ITEM_HEIGHT = 32;
    private final int TASK_ITEM_WIDTH = 36;
    private final int TASKS_PER_PAGE = 5;
    private final int UP_ARROW_SPRITE_ID = -20014;
    private final int DOWN_ARROW_SPRITE_ID = -20015;
    private final int ARROW_SPRITE_WIDTH = 39;
    private final int ARROW_SPRITE_HEIGHT = 20;
    private final int ARROW_Y_OFFSET = 40;

    private Widget window;
    private List<Task> tasks;
    private GenerateTaskPlugin plugin;
    private ClientThread clientThread;

    private Rectangle bounds = new Rectangle();

    private List<UIGraphic> taskBackgrounds = new ArrayList<>();
    private List<UILabel> taskLabels = new ArrayList<>();
    private List<UIGraphic> taskImages = new ArrayList<>();

    private int topTaskIndex = 0;

    public TaskList(Widget window, List<Task> tasks, GenerateTaskPlugin plugin, ClientThread clientThread) {
        this.window = window;
        this.tasks = tasks;
        this.plugin = plugin;
        this.clientThread = clientThread;

        refreshTasks(0);

        bounds.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        bounds.x = OFFSET_X+5;
        bounds.y = OFFSET_Y+5;

        Widget upWidget = window.createChild(-1, WidgetType.GRAPHIC);
        UIButton upArrow = new UIButton(upWidget);
        upArrow.setSprites(UP_ARROW_SPRITE_ID);
        upArrow.setSize(ARROW_SPRITE_WIDTH, ARROW_SPRITE_HEIGHT);
        upArrow.setPosition(CANVAS_WIDTH - (ARROW_SPRITE_WIDTH+5), ARROW_SPRITE_HEIGHT + ARROW_Y_OFFSET);
        upArrow.addAction("Scroll up", () -> refreshTasks(-1));

        Widget downWidget = window.createChild(-1, WidgetType.GRAPHIC);
        UIButton downArrow = new UIButton(downWidget);
        downArrow.setSprites(DOWN_ARROW_SPRITE_ID);
        downArrow.setSize(ARROW_SPRITE_WIDTH, ARROW_SPRITE_HEIGHT);
        downArrow.setPosition(CANVAS_WIDTH - (ARROW_SPRITE_WIDTH+5), ARROW_SPRITE_HEIGHT*2 + ARROW_Y_OFFSET);
        downArrow.addAction("Scroll down", () -> refreshTasks(1));

        this.add(upArrow);
        this.add(downArrow);
    }

    public void refreshTasks(int dir) {
        if(topTaskIndex+dir < 0 || topTaskIndex + dir + TASKS_PER_PAGE > tasks.size()) {
            return;
        }

        topTaskIndex += dir;

        final int POS_X = getCenterX(window, TASK_WIDTH);

        int i = 0;
        for(Task task : getTasksToShow(topTaskIndex)) {
            final int POS_Y = OFFSET_Y+(i*TASK_HEIGHT);

            UIGraphic taskBg;
            if(taskBackgrounds.size() <= i) {
                taskBg = new UIGraphic(window.createChild(-1, WidgetType.GRAPHIC));
                taskBackgrounds.add(taskBg);
                this.add(taskBg);
            }
            else {
                taskBg = taskBackgrounds.get(i);
            }

            taskBg.clearActions();
            taskBg.setSize(TASK_WIDTH, TASK_HEIGHT);
            taskBg.setPosition(POS_X, POS_Y);
            taskBg.getWidget().setPos(POS_X, POS_Y);
            taskBg.addAction("Mark", () -> plugin.completeTask(task.getId()));

            if(plugin.getSaveData().getCompletedTasks().get(task.getId()) != null) {
                taskBg.setSprite(GenerateTaskPlugin.TASK_COMPLETE_BACKGROUND_SPRITE_ID);
            }
            else if(plugin.getSaveData().currentTask != null && plugin.getSaveData().currentTask.getId() == task.getId()) {
                taskBg.setSprite(GenerateTaskPlugin.TASK_CURRENT_BACKGROUND_SPRITE_ID);
            }
            else {
                taskBg.setSprite(GenerateTaskPlugin.TASK_LIST_BACKGROUND_SPRITE_ID);
            }

            UILabel taskLabel;
            if(taskLabels.size() <= i) {
                taskLabel = new UILabel(window.createChild(-1, WidgetType.TEXT));
                this.add(taskLabel);
                taskLabels.add(taskLabel);
            }
            else {
                taskLabel = taskLabels.get(i);
            }

            taskLabel.getWidget().setTextColor(Color.WHITE.getRGB());
            taskLabel.getWidget().setTextShadowed(true);
            taskLabel.getWidget().setName(task.getDescription());
            taskLabel.setFont(496);
            taskLabel.setPosition(POS_X+60, POS_Y);
            taskLabel.setSize(TASK_WIDTH-60, TASK_HEIGHT);
            taskLabel.setText(task.getDescription());

            UIGraphic taskImage;
            if(taskImages.size() <= i) {
                taskImage = new UIGraphic(window.createChild(-1, WidgetType.GRAPHIC));
                this.add(taskImage);
                taskImages.add(taskImage);
            }
            else {
                taskImage = taskImages.get(i);
            }

            taskImage.setPosition(POS_X+12, POS_Y+6);
            taskImage.getWidget().setBorderType(1);
            taskImage.getWidget().setItemQuantityMode(ItemQuantityMode.NEVER);
            taskImage.setSize(TASK_ITEM_WIDTH, TASK_ITEM_HEIGHT);
            taskImage.setItem(task.getItemID());

            i++;
        }
    }

    private List<Task> getTasksToShow(int topTaskIndex) {
        List<Task> tasksToShow = new ArrayList<>();
        for(int i=0;i<TASKS_PER_PAGE;i++) {
            if(topTaskIndex + i > tasks.size()) break;
            tasksToShow.add(tasks.get(topTaskIndex+i));
        }

        return tasksToShow;
    }

    public void handleWheel(final MouseWheelEvent event)
    {
        log.info(event.getWheelRotation()+" "+this.isVisible()+bounds.contains(event.getPoint()));

        if (!this.isVisible() || !bounds.contains(event.getPoint()))
        {
            return;
        }

        event.consume();

        clientThread.invoke(() -> refreshTasks(event.getWheelRotation()));
    }
}
