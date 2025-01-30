package ru.eshay.ant.solution

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TypeChecked

class CVRP_ACO {
    TaskDescription task
    int numAnts
    double alpha
    double beta
    double evaporationRate
    HashMap<String, Double> pheromones

    CVRP_ACO(TaskDescription task, double alpha = 0.05, double beta = 2.0, double evaporationRate = 0.45) {
        this.task = task.clone() as TaskDescription
        this.numAnts = this.task.coordinates.keySet().size()
        this.alpha = alpha
        this.beta = beta
        this.evaporationRate = evaporationRate
        this.pheromones = new HashMap<>()
        initPheromones()
    }

    void initPheromones() {
        for (int i = 1; i <= task.coordinates.size(); i++) {
            for (int j = 1; j <= task.coordinates.size(); j++) {
                if (i != j) {
                    pheromones.put("$i-$j", 0.1)
                }
            }
        }
    }
    @CompileStatic
    static double calculateDistance(Point p1, Point p2) {
        return getLength(p1.x, p2.x, p1.y, p2.y)
    }

    @CompileStatic
    @Memoized
    static double getLength(Integer x1, Integer x2, Integer y1, Integer y2) {
        Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2))
    }

//    Integer selectNextNode(Integer currentNode, List<Integer> unvisitedNodes, Integer currentCapacity) {
//        Map<Integer, Double> probabilities = [:]
//        double totalProbability = 0.0
//
//        for (Integer nextNode in unvisitedNodes) {
//            double pheromone = pheromones.get("${currentNode}-${nextNode}")
//            double distance = calculateDistance(task.coordinates.get(currentNode), task.coordinates.get(nextNode))
//            double desirability = Math.pow(pheromone, alpha) * Math.pow(1.0 / distance as double, beta)
//
//            if (task.coordinates.get(nextNode).weight + currentCapacity <= task.capaticity) {
//                probabilities.put(nextNode, desirability)
//                totalProbability += desirability
//            }
//        }
//        if (probabilities.isEmpty() || totalProbability <= 0) {
//            return null
//        }
//        double random = Math.random() * totalProbability * 1.001
//        double cumulativeProbability = 0.0
//
//        List<Integer> nodes = new ArrayList<>(probabilities.keySet())
//        for (Integer nextNode in nodes) {
//            cumulativeProbability += probabilities.get(nextNode)
//            if (cumulativeProbability >= random) {
//                return nextNode
//            }
//        }
//        return null
//    }

    Integer selectNextNode(Integer currentNode, List<Integer> unvisitedNodes, Integer currentCapacity, double q0) {
        TreeMap<Double, Integer> cumulativeProbabilities = new TreeMap<>();
        double totalProbability = 0.0
        Integer bestNode = null
        double bestDesirability = -1

        for (Integer nextNode : unvisitedNodes) {
            double pheromone = pheromones.get("${currentNode}-${nextNode}")
            double distance = calculateDistance(task.coordinates.get(currentNode), task.coordinates.get(nextNode))
            double desirability = Math.pow(pheromone, alpha) * Math.pow(1.0 / distance as double, beta)
            if (task.coordinates.get(nextNode).weight + currentCapacity <= task.capaticity) {
                if(desirability > bestDesirability){
                    bestDesirability = desirability
                    bestNode = nextNode
                }
                totalProbability += desirability
                cumulativeProbabilities.put(totalProbability, nextNode)
            }
        }


        if (cumulativeProbabilities.isEmpty() || totalProbability <= 0) {
            return null
        }

        double random = Math.random() * totalProbability
        if (random < q0) {
            return bestNode
        }

        return cumulativeProbabilities.higherEntry(random)?.getValue()
    }

    @TypeChecked
    List<List<Integer>> constructRoute() {
        Integer startNode = 1
        List<List<Integer>> routes = []
        List<Integer> unvisitedNodes = (2..task.coordinates.size()).toList()

        while (unvisitedNodes.size() > 0) {
            List<Integer> route = [startNode]
            Integer currentCapacity = 0
            Integer currentNode = startNode
            double q0 = 0.8
            double q0Decay = 0.5
            while(true){
                Integer nextNode = selectNextNode(currentNode, unvisitedNodes, currentCapacity, q0)
                q0 = Math.max(0, q0 - q0Decay)
                if (nextNode == null) {
                    break
                }
                currentCapacity += task.coordinates.get(nextNode).weight
                route.add(nextNode)
                unvisitedNodes.remove(unvisitedNodes.indexOf(nextNode))
                currentNode = nextNode
            }
            route.add(startNode)
            routes.add(route)
        }

        return routes
    }


    @CompileStatic
    double calculateRouteLength(List<List<Integer>> routes) {
        double totalDistance = 0.0
        for (List<Integer> route : routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                totalDistance += calculateDistance(task.coordinates.get(route[i]), task.coordinates.get(route[i + 1]))
            }
        }
        return totalDistance
    }

    @CompileStatic
    boolean isRouteValid(List<List<Integer>> routes) {
        for (List<Integer> route : routes) {
            Integer currentCapacity = 0
            for (int i = 1; i < route.size() - 1; i++) {
                currentCapacity += task.coordinates.get(route[i]).weight
                if (currentCapacity > task.capaticity) {
                    return false
                }
            }
        }
        return true
    }


    void updatePheromones(List<List<List<Integer>>> antRoutes, List<Double> routeLengths, double bestRouteLength) {
        pheromones.replaceAll { k, v -> v * (1 - evaporationRate) }
        for (int i = 0; i < antRoutes.size(); i++) {
            List<List<Integer>> route = antRoutes[i]
            double routeLength = routeLengths[i]
            if (routeLength < bestRouteLength) {
                for (List<Integer> subRoute : route) {
                    for (int j = 0; j < subRoute.size() - 1; j++) {
                        int from = subRoute.get(j)
                        int to = subRoute.get(j + 1)
                        double deltaPheromone = 1.0 / routeLength
                        pheromones.computeIfPresent("${from}-${to}") { k, v -> v + deltaPheromone }
                    }
                }
            }
        }
    }

    List<List<Integer>> solve(int iterations) {
        List<List<Integer>> bestRoute = null
        double bestRouteLength = Double.POSITIVE_INFINITY

        for (int i = 0; i < iterations; i++) {
            List<List<List<Integer>>> antRoutes = []
            List<Double> routeLengths = []

            for (int j = 0; j < numAnts; j++) {
                List<List<Integer>> route = constructRoute()
                antRoutes.add(route)
                double routeLength = calculateRouteLength(route)
                routeLengths.add(routeLength)
            }
            updatePheromones(antRoutes, routeLengths, bestRouteLength)
            for(int k = 0; k < routeLengths.size(); k++){
                if(routeLengths[k] < bestRouteLength){
                    bestRouteLength = routeLengths[k]
                    bestRoute = antRoutes[k]
                }
            }
        }
        print(bestRouteLength)
        task.finalCost = (Integer) bestRouteLength.round()
        return bestRoute
    }
}
