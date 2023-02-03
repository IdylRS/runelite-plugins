package com.example;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class SaveData {
    @Getter
    private HashMap<Integer, Integer> completedTasks = new HashMap<>();

    public Task currentTask;
}
