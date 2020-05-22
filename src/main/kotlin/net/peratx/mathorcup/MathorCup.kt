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
import kotlin.coroutines.CoroutineContext

val s = Shelves(25, 4)

fun main() {
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
    generateCsv(false)
}

fun generateCsv(test: Boolean = false) {
    val arr = HashMap<Int, String>()
    var cnt = 0
    for (i in 1..200) {
        for (j in 1..15) {
            arr[cnt++] = "S" + i.toString().padStart(3, '0') + j.toString().padStart(2, '0')
        }
    }
    for (i in 1..13) {
        arr[cnt++] = "FH" + i.toString().padStart(2, '0')
    }
    val result = HashMap<Int, String>()
    val executor = Executor()
    val task = atomic(0)
    for (i in 1..cnt) {
        executor.launch {
            val myTask = task.getAndIncrement()
            var line = ""
            arr.values.forEach {
                line += s.calcDistance(arr[myTask]!!, it).toString() + ","
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
    if (test) {
        file.appendText("Head,")
        file.appendText(arr.values.joinToString(",") + "\n")
    }
    result.forEach { (k, v) ->
        println("Append Line $k")
        if (test) {
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
    println("$a 到 $b 距离应为 $ans 计算为 " + s.calcDistance(a, b))
}

fun getCoord(a: String) {
    println("$a 绝对坐标为 " + s.shelves[a.getShelf().getRealShelf()]!!.getCoordinate(a))
}
