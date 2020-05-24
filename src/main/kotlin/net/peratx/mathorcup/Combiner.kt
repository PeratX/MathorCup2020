/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 *
 * 此文件用于组合结果并评估
 */

package net.peratx.mathorcup

import java.io.File
import java.io.Serializable

data class BestResult(
    val task: String,
    val start: Int,
    val end: Int,
    val cnt: Int,
    val fitness: Int,
    val path: String
) : Serializable

fun searchRoute(
    results: HashMap<String, ArrayList<BestResult>>,
    now: ArrayList<BestResult> = ArrayList(),
    routes: ArrayList<ArrayList<BestResult>> = ArrayList()
): ArrayList<ArrayList<BestResult>> {
    if (now.size > 1 && now[0].start != 3) {
        return routes
    }
    if (now.size == results.size) {
        routes.add(now.clone() as ArrayList<BestResult>)
        now.clear()
        return routes
    }
    results.forEach { (k, v) ->
        if (!now.has(k)) {
            v.forEach { r ->
                if (now.size == 0 || now[now.size - 1].end == r.start) {
                    searchRoute(results, (now + r) as ArrayList<BestResult>, routes)
                }
            }
        }
    }
    return routes
}

fun ArrayList<BestResult>.has(task: String): Boolean {
    forEach { if (it.task == task) return true }
    return false
}

fun calcTotalLength(route: ArrayList<BestResult>): Int {
    var len = 0
    route.forEach { len += it.fitness }
    return len
}

fun main() {
    var min = Double.MAX_VALUE
    val bestResult = ArrayList<ArrayList<BestResult>>()
    searchRoute(scanBestResults()).apply {
        println("共: $size")
    }.forEach { list ->
        val len = calcRouteTotalTime(list)
        if (min > len) {
            min = len
        }
        if (min == len) {
            bestResult += list
        }
    }
    println("最短耗时：$min")
    println("共 ${bestResult.size} 种走法，为")
    bestResult.forEach { list ->
        print(list[0].start.toString() + " ")
        list.forEach {
            print(it.task + " " + it.end + " ")
        }
        println()
        calcRouteTotalTime(list, true)
    }

    //输出表格
    val file = File("route3.csv").apply { writeText("") }
    val map = genReverseMap()
    val route = bestResult[bestResult.size - 1]
    val start = ("FH" + route[0].start).regulate()
    file.appendText("," + start + "," + map[start] + ",0\n")
    route.forEach {
        //val end = ("FH" + it.end).regulate()
        genCsvFromRoute(it.path, getTaskGroup(it.task)!!, file, it.task + ",")
    }
}

fun calcRouteTotalTime(route: ArrayList<BestResult>, print: Boolean = false): Double {
    val tables = HashMap<Int, Double>() //复核台占用时间
    var time = 0.0 //相对时间戳
    var group: TaskGroup? = null
    route.forEach {
        time += (it.fitness / 1000 / 1.5).apply {
            if (print) {
                print(" 路程耗时 $this")
            }
        }//路上耗时
        val taskCnt = ArrayList<String>()
        group = getTaskGroup(it.task)!!.apply {
            tasks.forEach { task ->
                time += (if (task.cnt < 3) 5 else 4) * task.cnt // 下架时间
                if (!taskCnt.contains(task.order)) {
                    taskCnt += task.order
                }
            }
        }
        if (!tables.containsKey(it.end)) {
            tables[it.end] = 0.0
        }
        if (tables[it.end]!! > time) {
            val wait = tables[it.end]!! - time
            if (print) {
                print(" ${it.task} 在 ${it.end} 处等待 $wait 秒")
            }
            time += wait //等待时间
        }
        tables[it.end] = time + taskCnt.size * 30 //复核台忙的时间
    }
    time += group!!.getOrders().size.apply {
        if (print) {
            print(" 等待 $this 个任务完成")
        }
    } * 30 //最后一个必须等
    if (print) {
        println()
    }
    return time
}
