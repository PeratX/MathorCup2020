package net.peratx.mathorcup

import java.io.File

data class BestResult(
    val task: String,
    val start: Int,
    val end: Int,
    val cnt: Int,
    val fitness: Int,
    val path: String
)

fun searchRoute(
    results: HashMap<String, ArrayList<BestResult>>,
    now: ArrayList<BestResult> = ArrayList(),
    routes: ArrayList<ArrayList<BestResult>> = ArrayList()
): ArrayList<ArrayList<BestResult>> {
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
    forEach {
        if (it.task == task) {
            return true
        }
    }
    return false
}

fun calcTotalLength(route: ArrayList<BestResult>): Int {
    var len = 0
    route.forEach {
        len += it.fitness
    }
    return len
}

fun main() {
    var min = Int.MAX_VALUE
    val bestResult = ArrayList<ArrayList<BestResult>>()
    searchRoute(scanBestResults()).apply {
        println("共: $size")
    }.forEach { list ->
        val len = calcTotalLength(list)
        if (min > len) {
            min = len
        }
        if (min == len) {
            bestResult += list
        }
    }
    println("最短路程：$min")
    println("共 ${bestResult.size} 种走法，为")
    bestResult.forEach { list ->
        print(list[0].start.toString() + " ")
        list.forEach {
            print(it.task + " " + it.end + " ")
        }
        println()
    }

    //输出表格
    val file = File("route2.csv").apply { writeText("") }
    val map = genReverseMap()
    val start = ("FH" + bestResult[bestResult.size - 1][0].start).regulate()
    file.appendText("," + start + "," + map[start] + ",0\n")
    bestResult[bestResult.size - 1].forEach {
        //val end = ("FH" + it.end).regulate()
        genCsvFromRoute(it.path, getTaskGroup(it.task)!!, file, it.task + ",")
    }

}
