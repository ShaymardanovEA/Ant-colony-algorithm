package ru.eshay.ant.solution.exception

class PointWeightException extends Exception {
    PointWeightException(String taskName) {
        super("Task $taskName has invalid weight date")
    }
}