/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 *
 * 本文件无法被编译，泛型错误，请勿使用
 */

package net.peratx.mathorcup

import io.jenetics.EnumGene
import io.jenetics.Optimize
import io.jenetics.PartiallyMatchedCrossover
import io.jenetics.SwapMutator
import io.jenetics.engine.*
import io.jenetics.util.ISeq
import net.peratx.mathorcup.ShelfContainer.s
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import kotlin.math.roundToInt

class RouteSolver {
    fun solve(group: TaskGroup) {
        val solver = ProblemSolver(group)
        val engine = Engine.builder(solver)
            .optimize(Optimize.MINIMUM) //适应度越小越好
            .alterers(
                SwapMutator(0.4),
                PartiallyMatchedCrossover(0.96)
            )
            .build()
        println("代数\t个数\t平均适应度\t最佳适应度\t路径")
        /*
        engine.stream()
            .flatMap(Streams.toIntervalMax(Duration.ofMillis(100)))
            .forEach { best -> print(solver, best) }

         */
    }

    fun print(solver: ProblemSolver, b: EvolutionResult<EnumGene<Task>, Int>) {
        val best = b.bestPhenotype().genotype()
        val path = best.chromosome().stream()
            .map { it.allele() }
            .collect(ISeq.toISeq()).asList()
        val genotypes = b.genotypes()
        var total = 0
        var cnt = 0
        for (t in genotypes) {
            total += solver.fitness(t)
            cnt++
        }
        print(
            b.generation()
                .toString() + "\t" + cnt + "\t" + (total / cnt.toFloat()).roundToInt() + "\t" + solver.fitness(best) + "\t"
        )
        for ((_, box) in path) {
            print("$box,")
        }
        println(findNearestTable(path[path.size - 1]))
    }

    fun findNearestTable(task: Task): String {
        var min = Int.MAX_VALUE
        var table = ""
        for (i in 1..13) {
            val value = s.calcDistance(task.box, "FH$i")
            if (value < min) {
                min = value
                table = "FH$i"
            }
        }
        return table
    }
}

class ProblemSolver(group: TaskGroup) : Problem<ISeq<Task>, EnumGene<Task>, Int> {
    private val tasks = ISeq.of(group.tasks)

    override fun fitness(): Function<ISeq<Task>, Int> {
        return Function { route: ISeq<Task> ->
            route.stream().collect(getCollector(Task("0", "FH10", 0)))
        }
    }

    override fun codec(): Codec<ISeq<Task>, EnumGene<Task>> {
        return Codecs.ofPermutation(tasks)
    }

    private fun getCollector(task: Task): Collector<Task, *, Int> {
        return Collector.of(
            Supplier { RouteCollector(task) },
            BiConsumer { obj, t -> obj.add(t!!) },
            BinaryOperator { obj, collector -> obj.combine(collector) },
            Function { obj: RouteCollector -> obj.length() }
        )
    }

}

class RouteCollector(private var lastTask: Task) {
    private var length = 0

    fun add(task: Task) {
        length += s.calcDistance(lastTask.box, task.box)
        lastTask = task
    }

    fun combine(collector: RouteCollector): RouteCollector {
        throw UnsupportedOperationException()
    }

    fun length(): Int {
        return length + findShortestReturnLength()
    }

    fun findShortestReturnLength(): Int {
        var minValue = Int.MAX_VALUE
        for (i in 1..13) {
            minValue = minValue.coerceAtMost(s.calcDistance(lastTask.box, "FH$i"))
        }
        return minValue
    }
}
