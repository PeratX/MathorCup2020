/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 *
 * 第四问：带有随机性的贪心算法
 */

package net.peratx.mathorcup

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import java.io.File

data class Worker(
    val route: ArrayList<String> = ArrayList(),
    var timeUsed: Double = 0.0,
    val tasks: ArrayList<BestResult> = ArrayList(),
    var waitTable: Double = 0.0
) {
    fun getEnd() =
        if (route.size == 0) {
            ""
        } else {
            route[route.size - 1]
        }
}

data class TableT(
    var time: Double = 0.0,
    var used: Double = 0.0
) {
    fun getWaitTime(now: Double) = //获取等待时间
        if (time > now) {
            time - now
        } else {
            0.0
        }
}

fun HashMap<Int, Worker>.getAvgTime(): Double {
    var t = 0.0
    values.forEach { t += it.timeUsed }
    return t / this.size
}

fun HashMap<Int, Worker>.getWorst(): Worker {
    var time = 0.0
    var worker: Worker? = null
    values.forEach {
        if (it.timeUsed > time) {
            time = it.timeUsed
            worker = it
        }
    }
    return worker!!
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
    val tables = hashMapOf<Int, TableT>().apply { t.forEach { this[it] = TableT() } }

    fun simulate() {
        val tasks = ArrayList(bestResults.keys)
        while (tasks.size > 0) {
            val task = tasks.random().apply { tasks.remove(this) } //随机取得一个任务
            findSuitableWorker().addTask(task)
        }
    }

    fun Worker.addTask(task: String) {
        if (getEnd() == "") { //如果是第一次走，则随机选一个
            route += ("FH" + findBestTable()).regulate()
        }
        //找时间最短的路
        val end = getEnd().getTable().toInt()
        var min = Double.MAX_VALUE
        var wait = 0.0
        var r: BestResult? = null
        bestResults[task]!!.forEach { //找耗时最短的任务
            if (it.start == end) {
                val routeTime = it.fitness.toTime()
                val waitAtTable = tables[it.end]!!.getWaitTime(timeUsed + routeTime)
                val waitTime = routeTime + waitAtTable //获取总时间
                if (waitTime < min) {
                    min = waitTime
                    r = it
                    wait = waitAtTable
                }
            }
        }
        val route = r!!
        tasks += route
        route.path.split(",").forEach { this.route += it }
        //tasks += ("FH" + route.end).regulate()
        timeUsed += min
        waitTable += wait
        val usingTime = getTaskGroup(route.task)!!.getOrders().size * 30
        tables[route.end]!!.apply {
            time = timeUsed + usingTime //复核台的占用时间
            used += usingTime //利用时间
        }
    }

    fun Int.toTime(): Double {
        return this / 1000 / 1.5
    }

    fun findBestTable(): Int {
        var min = Double.MAX_VALUE
        val table = ArrayList<Int>()
        tables.forEach {
            if (it.value.time < min) {
                min = it.value.time
                table.clear()
            }
            if (it.value.time == min) {
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

fun HashMap<Int, Worker>.toCsv(file: File) {
    file.writeText("")
    val map = genReverseMap()
    forEach { (id, worker) ->
        worker.tasks.forEach { task ->
            val group = getTaskGroup(task.task)!!
            task.path.split(",").forEach { box ->
                file.appendText(
                    "P$id,${task.task},$box,${map[box.regulate()]}," +
                            (if (box.startsWith("FH")) 0 else group.tasks.findTask(box.regulate())) + "\n"
                )
            }
        }
    }
}

fun main() {
    var simulator: Simulator? = null
    val iteration = atomic(0)

    Runtime.getRuntime().addShutdownHook(object : Thread() { //ctrl+c退出自动生成
        override fun run() {
            val sim = simulator!!
            println("迭代次数：$iteration")
            val worst = sim.workers.getWorst()
            println("最差时间：" + worst.timeUsed)
            val time = worst.timeUsed + getTaskGroup(worst.tasks[worst.tasks.size - 1].task)!!.getOrders().size * 30
            println("总耗时：$time")
            sim.tables.forEach { (idx, table) ->
                println("FH$idx 忙时间：${table.used}秒 利用率：" + ((table.used / time) * 100) + "%")
            }
            sim.workers.forEach { worker ->
                val tasks = ArrayList<String>()
                worker.value.tasks.forEach { tasks += it.task }
                println(
                    "工人：${worker.key} 等待复核台：${worker.value.waitTable}秒 耗时：${worker.value.timeUsed}秒 任务单：" +
                            tasks.joinToString(", ") + " 路径：" + worker.value.route.joinToString(", ")
                )
            }
            sim.workers.toCsv(File("worker.csv"))
        }
    })

    val executor = Executor()//协程

    val worstTime = atomic(Double.MAX_VALUE)
    while (true) {
        executor.launch {
            Simulator(9, arrayOf(1, 3, 10, 12).toIntArray(), File("routedata\\routedata.dat").readRouteData()).apply {
                simulate()
                val time = workers.getWorst().timeUsed
                if (time < worstTime.value) {
                    worstTime.getAndSet(time)
                    simulator = this
                    println("最差时间：" + worstTime.value)
                }
            }
            iteration.getAndIncrement()
        }
        Thread.sleep(1)
    }
}
