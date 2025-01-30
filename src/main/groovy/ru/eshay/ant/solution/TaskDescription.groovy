package ru.eshay.ant.solution

import groovy.transform.AutoClone
import ru.eshay.ant.solution.exception.PointFormatException
import ru.eshay.ant.solution.exception.PointWeightException

@AutoClone
class TaskDescription {
    String name
    String comment
    String type
    String dimension
    String weightType
    Integer capaticity = 0
    HashMap<Integer, Point> coordinates = [:]
    Integer finalCost = 0

    TaskDescription(File inputFile) {
        Boolean isCoordinates = false
        Boolean isWeights = false
        inputFile.eachLine{
            if (it.startsWith("NAME")) {
                this.name = it.split(" : ").last()
            } else if (it.startsWith("COMMENT")) {
                this.comment = it.split(" : ").last()
            } else if (it.startsWith("TYPE")) {
                this.type = it.split(" : ").last()
            } else if (it.startsWith("DIMENSION")) {
                this.dimension = it.split(" : ").last()
            } else if (it.startsWith("EDGE_WEIGHT_TYPE")) {
                this.weightType = it.split(" : ").last()
            } else if (it.startsWith("CAPACITY")) {
                this.capaticity = it.split(" : ").last().toInteger()
            } else if (it.startsWith("NODE_COORD_SECTION")) {
                isCoordinates = true
            } else if (it.startsWith("DEMAND_SECTION")) {
                isCoordinates = false
                isWeights = true
            } else if (it.startsWith("DEPOT_SECTION")) {
                isWeights = false
            } else if (isCoordinates) {
                List<String> splitData = it.trim().split(" ")
                if (splitData.size() != 3) {
                    throw new PointFormatException(this.name)
                } else {
                    this.coordinates.put(splitData[0].toInteger(), new Point(splitData[1].toInteger(), splitData[2].toInteger()))
                }
            } else if (isWeights) {
                if (this.coordinates.keySet().size() == 0) {
                    throw new PointFormatException(this.name)
                }
                List<String> splitData = it.split(" ")
                if (splitData.size() != 2) {
                    throw new PointWeightException(this.name)
                } else {
                    try {
                        this.coordinates.get(splitData[0].toInteger()).setWeight(splitData[1].toInteger())
                    } catch (Exception exception) {
                        print(this.name)
                    }
                }
            }
        }
    }
}
