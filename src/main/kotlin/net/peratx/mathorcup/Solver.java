package net.peratx.mathorcup;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import io.jenetics.util.Streams;
import kotlin.io.FilesKt;
import kotlin.text.Charsets;

import java.io.File;
import java.time.Duration;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 */
class Solver {
    public static final int[] DEFAULT_END = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

    public final TaskGroup group;
    public final int start;
    public final int[] end;
    public final File file;

    /**
     * @param group 任务组
     * @param start 起点复核台
     * @param end   终点复核台数组
     */
    Solver(TaskGroup group, int start, int[] end) {
        this.group = group;
        this.start = start;
        this.end = end;
        this.file = new File(group.getGroup() + "_" + start + "_" + end[0]);
        FilesKt.writeText(file, "", Charsets.UTF_8);
    }

    void solve() {
        //构造问题实例
        var solver = new ProblemSolver(this);
        //构造遗传引擎
        var engine = Engine.builder(solver)
                //设定适应度是越大还是越小好
                .optimize(Optimize.MINIMUM) //适应度越小越好
                .alterers( //变化器
                        new SwapMutator<>(0.4), //突变
                        new PartiallyMatchedCrossover<>(0.96) //交叉
                )
                .build();

        FilesKt.appendText(file, "代数\t个数\t平均适应度\t最佳适应度\t路径\n", Charsets.UTF_8);
        engine.stream().limit(1000_0000)
                .flatMap(Streams.toIntervalMax(Duration.ofMillis(1000))) //100毫秒输出一次结果
                //.map(program -> program.bestPhenotype().genotype())
                .forEach(best -> print(solver, best)); //输出函数
        /*
        var stat = EvolutionStatistics.ofNumber();
        var best = b.collect(toBestPhenotype());
                //.peek(stat);
        engine.stream();
         */
    }

    void print(ProblemSolver solver, EvolutionResult<EnumGene<Task>, Integer> b) {
        var best = b.bestPhenotype().genotype();
        var path = best.chromosome().stream().map(Gene::allele).collect(ISeq.toISeq()).asList();

        var genotypes = b.genotypes();
        var total = 0;
        var cnt = 0;
        for (var t : genotypes) {
            total += solver.fitness(t);
            cnt++;
        }

        var str = new StringBuilder(b.generation() + "\t" + cnt + "\t" + Math.round(total / cnt) + "\t" + solver.fitness(best) + "\t");
        for (var task : path) {
            str.append(task.getBox()).append(",");
        }
        str.append(findNearestTable(path.get(path.size() - 1))).append("\n");
        FilesKt.appendText(file, str.toString(), Charsets.UTF_8);
    }

    String findNearestTable(Task task) {
        int min = Integer.MAX_VALUE;
        String table = "";
        for (var i : end) {
            var value = ShelfContainer.INSTANCE.getS().calcDistance(task.getBox(), "FH" + i);
            if (value < min) {
                min = value;
                table = "FH" + i;
            }
        }
        return table;
    }

    static class ProblemSolver implements Problem<ISeq<Task>, EnumGene<Task>, Integer> {
        private final ISeq<Task> tasks;
        private final Solver solver;

        ProblemSolver(Solver solver) {
            tasks = ISeq.of(solver.group.getTasks());
            this.solver = solver;
        }

        @Override
        public Function<ISeq<Task>, Integer> fitness() {
            //初始点
            return route -> route.stream().collect(getCollector(
                    new Task("0", "FH" + solver.start, 0),
                    solver.end
            ));
        }

        @Override
        public Codec<ISeq<Task>, EnumGene<Task>> codec() {
            return Codecs.ofPermutation(tasks);
        }

        private Collector<Task, ?, Integer> getCollector(Task task, int[] end) {
            return Collector.of(
                    () -> new RouteCollector(task, end),
                    RouteCollector::add,
                    RouteCollector::combine,
                    RouteCollector::length
            );
        }
    }

    static class RouteCollector {
        private int length = 0;
        private Task lastTask;
        private int[] end;

        RouteCollector(Task initialTask, int[] end) {
            lastTask = initialTask;
            this.end = end;
        }

        void add(Task task) {
            length += ShelfContainer.INSTANCE.getS().calcDistance(lastTask.getBox(), task.getBox());
            lastTask = task;
        }

        RouteCollector combine(RouteCollector collector) {
            throw new UnsupportedOperationException();
        }

        int length() {
            return length + findShortestReturnLength();
        }

        int findShortestReturnLength() {
            int min = Integer.MAX_VALUE;
            for (var i : end) {
                min = Math.min(min, ShelfContainer.INSTANCE.getS().calcDistance(lastTask.getBox(), "FH" + i));
            }
            return min;
        }
    }
}
