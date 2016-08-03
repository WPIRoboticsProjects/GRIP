package edu.wpi.grip.core.operations.composite

data class Contour(
        val area: Double,
        val centerX: Double,
        val centerY: Double,
        val width: Double,
        val height: Double,
        val solidity: Double) {
}
