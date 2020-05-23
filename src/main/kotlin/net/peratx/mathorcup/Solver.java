package net.peratx.mathorcup;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import io.jenetics.util.Streams;

import java.time.Duration;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 */
class Solver {
    void solve(TaskGroup group, String start) {
        //构造问题实例
        var solver = new ProblemSolver(group, start);
        //构造遗传引擎
        var engine = Engine.builder(solver)
                //设定适应度是越大还是越小好
                .optimize(Optimize.MINIMUM) //适应度越小越好
                .alterers( //变化器
                        new SwapMutator<>(0.4), //突变
                        new PartiallyMatchedCrossover<>(0.96) //交叉
                )
                .build();

        System.out.println("代数\t个数\t平均适应度\t最佳适应度\t路径");
        engine.stream()
                .flatMap(Streams.toIntervalMax(Duration.ofMillis(100))) //100毫秒输出一次结果
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

        System.out.print(b.generation() + "\t" + cnt + "\t" + Math.round(total / cnt) + "\t" + solver.fitness(best) + "\t");
        for (var task : path) {
            System.out.print(task.getBox() + ",");
        }
        System.out.println(findNearestTable(path.get(path.size() - 1)));
    }

    String findNearestTable(Task task) {
        int min = Integer.MAX_VALUE;
        String table = "";
        for (int i = 1; i <= 13; i++) {
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
        private final String start;

        ProblemSolver(TaskGroup group, String start) {
            tasks = ISeq.of(group.getTasks());
            this.start = start;
        }

        @Override
        public Function<ISeq<Task>, Integer> fitness() {
            //初始点
            return route -> route.stream().collect(getCollector(new Task("0", start, 0)));
        }

        @Override
        public Codec<ISeq<Task>, EnumGene<Task>> codec() {
            return Codecs.ofPermutation(tasks);
        }

        private Collector<Task, ?, Integer> getCollector(Task task) {
            return Collector.of(
                    () -> new RouteCollector(task),
                    RouteCollector::add,
                    RouteCollector::combine,
                    RouteCollector::length
            );
        }
    }

    static class RouteCollector {
        private int length = 0;
        private Task lastTask;

        RouteCollector(Task initialTask) {
            lastTask = initialTask;
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
            for (int i = 1; i <= 13; i++) {
                min = Math.min(min, ShelfContainer.INSTANCE.getS().calcDistance(lastTask.getBox(), "FH" + i));
            }
            return min;
        }
    }
}
