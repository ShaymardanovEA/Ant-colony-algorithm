package ru.eshay.ant.solution

class SolutionDescription {
    Map<Integer, List<Integer>> routes = [:]
    Integer cost = 0

    SolutionDescription(File inputFile) {
        inputFile.eachLine{
            if (it.startsWith("Route #")) {
                List<String> splitData = it.split(": ")
                this.routes.put(splitData.first().split("#").last().toInteger(),
                        [splitData.last().split(" ").collect{ strin -> strin.toInteger()}] as List<Integer>)
            } else if (it.startsWith("Cost")) {
                this.cost = it.split(" ").last().toInteger()
            }
        }
    }
}
