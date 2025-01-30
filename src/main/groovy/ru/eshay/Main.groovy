package ru.eshay

import ru.eshay.ant.solution.CVRP_ACO
import ru.eshay.ant.solution.FileReader
import ru.eshay.ant.solution.OverviewTask
import ru.eshay.ant.solution.exception.PointFormatException

import java.util.concurrent.CopyOnWriteArrayList

static void main(String[] args) {
//    File task = new File("C:/Users/eshay/Downloads/Telegram Desktop/data/data/P/P-n16-k8.vrp")
//    File solution = new File("C:/Users/eshay/Downloads/Telegram Desktop/data/data/P/P-n16-k8.sol")
//    OverviewTask example = new OverviewTask(task, solution)
//    example.evaluate()
////    println("CUrrent len: ${example.}")
//    println("Accurancy: ${example.accuracy}")
//    println("Total time: ${example.totalTime}")
//    println("Best Route Length: ${example.solutionDescription.cost}")
//    println("Route: ${example.bestRoute}")

    Map<String, List<Long>> totalTimeResult = [:]
    Map<String, List<Double>> totalAccuracyResult = [:]
    FileReader fileReader = new FileReader("C:/Users/eshay/Downloads/Telegram Desktop/data/data/")
    fileReader.allTasks.entrySet().parallelStream().forEach { typeEntry ->
        typeEntry.value.entrySet().parallelStream().forEach { taskEntry ->
            if (typeEntry.key != "B") return
            try {
                OverviewTask overviewTask = new OverviewTask(taskEntry.value)
                overviewTask.processTask()
                totalTimeResult.compute(typeEntry.key, { k, v ->
                    if (v == null) {
                        new CopyOnWriteArrayList<Long>([overviewTask.totalTime])
                    } else {
                        def newList = new CopyOnWriteArrayList<Long>(v)
                        newList.add(overviewTask.totalTime)
                        return newList
                    }
                })
                totalAccuracyResult.compute(typeEntry.key, { k, v ->
                    if (v == null) {
                        new CopyOnWriteArrayList<Double>([overviewTask.accuracy])
                    } else {
                        def newList = new CopyOnWriteArrayList<Double>(v)
                        newList.add(overviewTask.accuracy)
                        return newList
                    }
                })
            } catch (PointFormatException e) {
                println("Skip ${taskEntry.key}")
            }
        }
    }

    totalTimeResult.each {
        println("Общее время выполнения категории $it.key составило ${it.value.sum() / it.value.size()} с размером ${it.value.size()}")
    }

    totalAccuracyResult.each {
        println("Средняя точность категории $it.key составила ${(it.value.sum() / it.value.size()) * 100} с размером ${it.value.size()}")
    }
}