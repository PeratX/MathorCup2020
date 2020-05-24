/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 */

package net.peratx.mathorcup

import java.io.File

data class Worker(
    val route: ArrayList<String> = ArrayList(),
    var timeUsed: Double = 0.0,
    val tasks: ArrayList<String> = ArrayList()
) {
    fun getEnd() =
        if (route.size == 0) {
            ""
        } else {
            route[route.size - 1]
        }
}

class Simulator(
    workerCnt: Int, t: IntArray,
    val bestResults: HashMap<String, ArrayList<BestResult>>
) { //基于简单的贪心算法，只考虑眼前最优，初始随机，且时间相同情况下随机复核台
    val workers = hashMapOf<Int, Worker>().apply {
        for (i in 0 until workerCnt) {
            this[i] = Worker()
        }
    }
    val tables = hashMapOf<Int, Double>().apply { t.forEach { this[it] = 0.0 } }

    fun simulate() {
        val tasks = ArrayList(bestResults.keys)
        while (tasks.size > 0) {
            val task = tasks.random().apply { tasks.remove(this) } //随机取得一个任务
            findSuitableWorker().addTask(task)
        }
    }

    fun Worker.addTask(task: String) {
        tasks += task
        if (getEnd() == "") { //如果是第一次走，则随机选一个
            route += ("FH" + findBestTable()).regulate()
        }
        //找时间最短的路
        val end = getEnd().getTable().toInt()
        var min = Int.MAX_VALUE
        var r: BestResult? = null
        bestResults[task]!!.forEach { //找耗时最短的任务
            if (it.start == end) {
                val waitTime = it.fitness.toTime() + tables.getWaitTime(it.end, timeUsed) //获取总时间
                if (waitTime < min) {
                    min = it.fitness
                    r = it
                }
            }
        }
        val route = r!!
        route.path.split(",").forEach { this.route += it }
        //tasks += ("FH" + route.end).regulate()
        timeUsed += min
        tables[route.end] = timeUsed + getTaskGroup(route.task)!!.getOrders().size * 30 //复核台的占用时间
    }

    fun HashMap<Int, Double>.getWaitTime(k: Int, now: Double) = //获取等待时间
        if (this[k]!! > now) {
            this[k]!! - now
        } else {
            0.0
        }

    fun Int.toTime(): Double {
        return this / 1000 / 1.5
    }

    fun findBestTable(): Int {
        var min = Double.MAX_VALUE
        val table = ArrayList<Int>()
        tables.forEach {
            if (it.value < min) {
                min = it.value
                table.clear()
            }
            if (it.value == min) {
                table += it.key
            }
        }
        return table.random() //从等待时间相同的复核台中随机找一个
    }

    fun findSuitableWorker(): Worker { //找比较闲的工人
        var time = Double.MAX_VALUE
        var worker: Worker? = null
        workers.values.forEach {
            if (it.timeUsed < time) { //已用时间最少
                time = it.timeUsed
                worker = it
            }
        }
        return worker!!
    }
}

fun main() {
    val simulator = Simulator(9, arrayOf(1, 3, 10, 12).toIntArray(), File("routedata\\routedata.dat").readRouteData())
    simulator.simulate()
    simulator.workers.forEach {
        println(
            "工人：${it.key} 耗时：${it.value.timeUsed} 任务单：" +
                    it.value.tasks.joinToString(", ") + " 路径：" + it.value.route.joinToString(", ")
        )
    }
}
