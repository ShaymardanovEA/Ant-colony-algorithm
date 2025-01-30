package ru.eshay.ant.solution

class Point {
    Integer x
    Integer y
    Integer weight = 0

    Point(Integer x, Integer y) {
        this.x = x
        this.y = y
    }

    void setWeight(Integer weight) {
        this.weight = weight
    }
}