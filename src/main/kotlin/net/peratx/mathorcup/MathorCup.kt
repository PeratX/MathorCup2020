/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 */

package net.peratx.mathorcup

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.File
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

object ShelfContainer {
    val s = Shelves(25, 4)
}

fun main() {
    //测试距离
    /*
    testCalc("S00913", "S00909", 4700)
    testCalc("S00913", "S01710", 12500)
    testCalc("S00102", "S01009", 15700)
    testCalc("S12315", "S06311", 51100)
    testCalc("S02510", "FH03", 12300)
    testCalc("S02614", "FH04", 16900)
    //testCalc("S03414", "FH04", 15800)
    testCalc("S00204", "FH09", 10400)
    testCalc("S00106", "FH11", 7600)
    testCalc("S02103", "FH13", 8200 + 11000)
     */
    //测试生成矩阵
    //generateCsv(false)
    //测试寻找最短路径
    //Solver(getTaskGroup("T0001"), 10, Solver.DEFAULT_END).solve()
    //RouteSolver().solve(testTaskGroup())
    //测试时间
    //println("花费：" + calcTime(getTaskGroup("T0001")!!, 381000) + "秒")

    //求最优解
    gen15()
}

fun gen15() {
    thread {
        Solver(getTaskGroup("T0002"), 3, IntArray(1).apply { set(0, 3) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0002"), 3, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0002"), 11, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0003"), 3, IntArray(1).apply { set(0, 3) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0003"), 3, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0003"), 11, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0004"), 3, IntArray(1).apply { set(0, 3) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0004"), 3, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0004"), 11, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0005"), 3, IntArray(1).apply { set(0, 3) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0005"), 3, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0005"), 11, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0006"), 3, IntArray(1).apply { set(0, 3) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0006"), 3, IntArray(1).apply { set(0, 11) }).solve()
    }
    thread {
        Solver(getTaskGroup("T0006"), 11, IntArray(1).apply { set(0, 11) }).solve()
    }
}

fun generateCsv(withHeader: Boolean = false) {
    val arr = generateAbstractMap()
    val result = HashMap<Int, String>()
    val executor = Executor()
    val task = atomic(0)
    for (i in 1..arr.size) {
        executor.launch {
            val myTask = task.getAndIncrement()
            var line = ""
            arr.values.forEach {
                line += ShelfContainer.s.calcDistance(arr[myTask]!!, it).toString() + ","
            }
            line = line.substring(0, line.length - 1)
            result[myTask] = line
            println("Line $myTask done!")
        }
    }
    while (!executor.job.children.none()) {
        Thread.sleep(100)
    }
    val file = File("result.csv")
    file.writeText("")
    if (withHeader) {
        file.appendText("Header,")
        file.appendText(arr.values.joinToString(",") + "\n")
    }
    result.forEach { (k, v) ->
        println("Append Line $k")
        if (withHeader) {
            file.appendText(arr[k] + ",")
        }
        file.appendText(v + "\n")
    }
}

class Executor : CoroutineScope {
    val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = newFixedThreadPoolContext(8, "MathorCup") + job
}

fun testCalc(a: String, b: String, ans: Int) {
    println("$a 到 $b 距离应为 $ans 计算为 " + ShelfContainer.s.calcDistance(a, b))
}

fun getCoord(a: String) {
    println("$a 绝对坐标为 " + ShelfContainer.s.shelves[a.getShelf().getRealShelf()]!!.getCoordinate(a))
}

fun calcTime(group: TaskGroup, routeLen: Int): Double {
    var time = routeLen / 1000 / 1.5 //路程/1.5m/s

    group.tasks.forEach {
        time += (if (it.cnt < 3) 5 else 4) * it.cnt //下架时间
    }

    return time + 30 //复核时间
}
