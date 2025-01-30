package ru.eshay.ant.solution

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TypeChecked

class CVRP_ACO_old {
    TaskDescription task
    int numAnts
    double alpha
    double beta
    double evaporationRate
    double pheromoneDeposit
    HashMap<String, Double> pheromones

    CVRP_ACO_old(TaskDescription task, double alpha = 1.0, double beta = 2.0, double evaporationRate = 0.5, double pheromoneDeposit = 100.0) {
        this.task = task.clone() as TaskDescription
        this.numAnts = this.task.coordinates.keySet().size()
        this.alpha = alpha
        this.beta = beta
        this.evaporationRate = evaporationRate
        this.pheromoneDeposit = pheromoneDeposit
        this.pheromones = new HashMap<>()
        initPheromones()
    }

    // Инициализация феромонов
    void initPheromones() {
        for (int i = 1; i <= task.coordinates.size(); i++) {
            for (int j = 1; j <= task.coordinates.size(); j++) {
                if (i != j) {
                    pheromones.put("$i-$j", 1.0)  // Начальное значение феромона
                }
            }
        }
    }
    // Расчет расстояния между двумя точками
    @CompileStatic
    static double calculateDistance(Point p1, Point p2) {
        return getLength(p1.x, p2.x, p1.y, p2.y)
    }

    @CompileStatic
    @Memoized
    static double getLength(Integer x1, Integer x2, Integer y1, Integer y2) {
        Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2))
    }

    // Выбор следующего узла для муравья
//    @CompileStatic
    Integer selectNextNode(Integer currentNode, List<Integer> unvisitedNodes, Integer currentCapacity) {
        def probabilities = [:]
        double totalProbability = 0.0

        for (Integer nextNode in unvisitedNodes) {
            double pheromone = pheromones.get("${currentNode}-${nextNode}")
            double distance = calculateDistance(task.coordinates.get(currentNode), task.coordinates.get(nextNode))
            double desirability = Math.pow(pheromone, alpha) * Math.pow(1.0 / distance as double, beta)

            if (task.coordinates.get(nextNode).weight + currentCapacity <= task.capaticity) {
                probabilities[nextNode] = desirability
                totalProbability += desirability
            }
        }

        if(probabilities.size() == 0) return null

        double random = Math.random() * totalProbability
        double cumulativeProbability = 0.0

        for (Integer nextNode in (probabilities.keySet() as List<Integer>)) {
            cumulativeProbability += probabilities.get(nextNode)
            if (cumulativeProbability >= random) {
                return nextNode
            }
        }

        return null
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

            while(true){
                Integer nextNode = selectNextNode(currentNode, unvisitedNodes, currentCapacity)
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


    // Обновление феромонов
    void updatePheromones(List<List<List<Integer>>> antRoutes, List<Double> routeLengths, double bestRouteLength) {
        pheromones.replaceAll { key, value -> value * (1 - evaporationRate) }

        // Обновление феромонов для лучших маршрутов
        for (int i = 0; i < antRoutes.size(); i++) {
            if (routeLengths[i] != Double.POSITIVE_INFINITY) {
                List<List<Integer>> routes = antRoutes[i]
                double reward = pheromoneDeposit / routeLengths[i]

                for (List<Integer> route : routes) {
                    for (int j = 0; j < route.size() - 1; j++) {
                        String edge = "${route[j]}-${route[j + 1]}"
                        pheromones.computeIfPresent(edge) { k, v -> v + reward }
                    }
                }
            }
        }

//        if (routeLengths.indexOf(bestRouteLength) != -1) {
//            List<List<Integer>> routes = antRoutes[routeLengths.indexOf(bestRouteLength)]
//            double reward = pheromoneDeposit / routeLengths[routeLengths.indexOf(bestRouteLength)]
//            for (List<Integer> route : routes) {
//                for (int j = 0; j < route.size() - 1; j++) {
//                    String edge = "${route[j]}-${route[j + 1]}"
//                    pheromones.computeIfPresent(edge) { k, v -> v + reward }
//                }
//            }
//        }
    }

    // Запуск алгоритма
    List<List<Integer>> solve(int iterations) {
        List<List<Integer>> bestRoute = null
        double bestRouteLength = Double.POSITIVE_INFINITY

        for (int i = 0; i < iterations; i++) {
            List<List<List<Integer>>> antRoutes = []
            List<Double> routeLengths = []

            // Построение маршрутов муравьями
            for (int j = 0; j < numAnts; j++) {
                List<List<Integer>> route = constructRoute()
                antRoutes.add(route)
                double routeLength = Double.POSITIVE_INFINITY
                if(isRouteValid(route)){
                    routeLength = calculateRouteLength(route)
                }
                routeLengths.add(routeLength)
            }

            // Обновление феромонов

            updatePheromones(antRoutes, routeLengths, bestRouteLength)
            for(int k = 0; k < routeLengths.size(); k++){
                if(routeLengths[k] < bestRouteLength){
                    bestRouteLength = routeLengths[k]
                    bestRoute = antRoutes[k]
                }
            }
//            println(pheromones.values().unique())
            // Поиск лучшего решения в текущей итерации
//            if(i % 10 == 0){
//                println("Iteration: ${i}, Best Route Length: ${bestRouteLength}")
//            }
        }

        println("${task.name} Best Route Length: ${bestRouteLength}")
        task.finalCost = (Integer) bestRouteLength.round()
        return bestRoute

    }
}
