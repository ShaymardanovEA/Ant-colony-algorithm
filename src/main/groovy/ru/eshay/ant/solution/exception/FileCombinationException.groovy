package ru.eshay.ant.solution.exception

class FileCombinationException extends Exception {
    FileCombinationException(String taskName) {
        super("Task $taskName has invalid files count")
    }
}
