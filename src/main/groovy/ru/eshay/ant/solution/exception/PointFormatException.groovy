package ru.eshay.ant.solution.exception

class PointFormatException extends Exception {
    PointFormatException(String taskName) {
        super("Task $taskName has invalid point date")
    }
}
