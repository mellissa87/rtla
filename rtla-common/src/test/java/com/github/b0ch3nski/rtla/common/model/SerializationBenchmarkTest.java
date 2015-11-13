package com.github.b0ch3nski.rtla.common.model;

import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author bochen
 */
@SuppressWarnings("all")
public class SerializationBenchmarkTest {

//    @Test
    public void launchBenchmark() throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(getClass().getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(5))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(5))
                .measurementIterations(10)
                .shouldFailOnError(false)
                .forks(1)
                .threads(1)
                .build();
        new Runner(opts).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        public SimplifiedLogSerializer serializer;
        public SimplifiedLog toSerialize;
        public List<SimplifiedLog> listToSerialize;

        @Setup(Level.Trial)
        public void initialize() {
            serializer = new SimplifiedLogSerializer();
            toSerialize = RandomLogFactory.create();
            listToSerialize = RandomLogFactory.create(1000);
        }
    }

    @Benchmark
    public byte[] serializeSingleObjectWithCalculatedBufferSize(BenchmarkState state) {
        return state.serializer.toBytesWithCalculatedBufferSize(state.toSerialize);
    }

    @Benchmark
    public byte[] serializeListWithCalculatedBufferSize(BenchmarkState state) {
        return state.serializer.listToBytesWithCalculatedBufferSize(state.listToSerialize);
    }

    @Benchmark
    public byte[] serializeSingleObjectWithOutputStream(BenchmarkState state) {
        return state.serializer.toBytesWithOutputStream(state.toSerialize);
    }

    @Benchmark
    public byte[] serializeListWithOutputStream(BenchmarkState state) {
        return state.serializer.listToBytesWithOutputStream(state.listToSerialize);
    }

    @State(Scope.Benchmark)
    public static class ParametrizedBenchmarks {

        @Param({"128", "256", "512", "768", "1024"/*, "1536", "2048", "4096" */})
        public int kryoBufferSize;

        @Param({"-1", "128", "256", "512", "768", "1024"/*, "1536", "2048", "4096"*/})
        public int maxKryoBufferSize;

        @Benchmark
        public byte[] serializeSingleObjectWithDefinedBufferSize(BenchmarkState state) {
            return state.serializer.toBytesWithDefinedBufferSize(state.toSerialize, kryoBufferSize, maxKryoBufferSize);
        }
    }
}
