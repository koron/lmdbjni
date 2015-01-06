package org.fusesource.lmdbjni;

import org.junit.Test;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RocksDBPerfTest extends Setup {
    static RocksDB rocksDb;
    static {
        Maven.recreateDir(new File("/tmp/rocks"));
        org.rocksdb.Options options = new org.rocksdb.Options();
        options.setCreateIfMissing(true);
        try {
            rocksDb = RocksDB.open(options, "/tmp/rocks");
            for (int i = 0; i < 100000; i++) {
                rocksDb.put(Bytes.fromLong(i), Bytes.fromLong(i));
            }

        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + RocksDBPerfTest.class.getSimpleName() + ".*")
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .jvmArgs("-server")
                .jvmClasspath(Maven.classPath)
                .outputFormat(OutputFormatType.TextReport)
                .build();
        new Runner(options).run();
    }

    static RocksIterator it;

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void rocksdb_iterate() throws IOException {
        if (it == null) {
            it = rocksDb.newIterator();
        }
        it.next();

        if (!it.isValid()) {
            it.seekToFirst();
        }
        it.key();
        it.value();
    }
}