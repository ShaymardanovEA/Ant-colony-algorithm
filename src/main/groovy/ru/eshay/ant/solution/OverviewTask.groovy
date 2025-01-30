package ru.eshay.ant.solution


import ru.eshay.ant.solution.exception.FileCombinationException

class OverviewTask {
    TaskDescription taskDescription
    SolutionDescription solutionDescription
    Double accuracy = 0.0
    long totalTime = 0
    List<List<Integer>> bestRoute = []

    OverviewTask(List<File> paths) {
        if (paths == null || paths.size() != 2) {
            throw new FileCombinationException(paths.first().name)
        }
        def path1 = paths.first()
        def path2 = paths.last()
        if (path1.name.split("\\.").last() == "sol") {
            taskDescription = new TaskDescription(path2)
            solutionDescription = new SolutionDescription(path1)
        } else {
            solutionDescription = new SolutionDescription(path1)
            taskDescription = new TaskDescription(path2)
        }
    }

    OverviewTask(File path1, File path2) {
        if (path1.name.split("\\.").last() == "sol") {
            solutionDescription = new SolutionDescription(path1)
            taskDescription = new TaskDescription(path2)
        } else {
            solutionDescription = new SolutionDescription(path2)
            taskDescription = new TaskDescription(path1)
        }
    }

    double getCurrency() {
        CVRP_ACO aco = new CVRP_ACO(this.taskDescription)
        aco.solve(500)
        return (aco.task.finalCost - solutionDescription.cost) / solutionDescription.cost
    }

    void processTask() {
        totalTime = measureTime { evaluate() }
    }

    void evaluate() {
        CVRP_ACO aco = new CVRP_ACO(this.taskDescription)
        bestRoute = aco.solve(500)
        accuracy = (aco.task.finalCost - solutionDescription.cost) / solutionDescription.cost
    }

    static long measureTime(Closure closure) {
        def startTime = System.currentTimeMillis()
        closure()
        def endTime = System.currentTimeMillis()
        return endTime - startTime
    }
}
