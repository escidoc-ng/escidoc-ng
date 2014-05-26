package net.objecthunter.larch.bench;/*
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.XORShiftRNG;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BenchTool {

    private static final Logger log = LoggerFactory.getLogger(BenchTool.class);

    public static final XORShiftRNG RNG = new XORShiftRNG();

    public static final int[] SLICE = new int[1024];

    static {
        for (int i = 0; i< SLICE.length;i++) {
            SLICE[i] = RNG.nextInt();
        }
    }


    public static void main(String[] args) {
        final Options ops = createOptions();
        final CommandLineParser cliParser = new BasicParser();

        /* default values */
        String larchUri = "http://localhost:8080";
        Action action = Action.INGEST;
        int numActions = 1;
        int numThreads = 1;
        long size = 1024;
        String user = "admin";
        String password = "admin";

        /* parse the user supplied args */
        try {
            final CommandLine cli = cliParser.parse(ops, args);
            if (cli.hasOption('h')) {
                printUsage(ops);
                return;
            }
            if (cli.hasOption('l')) {
                larchUri = cli.getOptionValue('l');
            }
            if (cli.hasOption('a')) {
                action = Action.valueOf(cli.getOptionValue('a').toUpperCase());
            }
            if (cli.hasOption('n')) {
                numActions = Integer.parseInt(cli.getOptionValue('n'));
            }
            if (cli.hasOption('s')) {
                size = getSizeFromArgument(cli.getOptionValue('s'));
            }
            if (cli.hasOption('t')) {
                numThreads = Integer.parseInt(cli.getOptionValue('t'));
            }
            if (cli.hasOption('u')) {
                user = cli.getOptionValue('u');
            }
            if (cli.hasOption('p')) {
                password = cli.getOptionValue('p');
            }
        } catch (ParseException e) {
            log.error("Unable to parse commandline.\n", e);
        }

        log.info("Running {} {} actions with size {} against {} using {} threads", numActions, action,
                size, larchUri, numThreads);
        final BenchToolRunner runner = new BenchToolRunner(action, URI.create(larchUri), user, password, numActions, numThreads, size);
        try {
            long time = System.currentTimeMillis();
            final List<BenchToolResult> results = runner.run();
            ResultFormatter.printResults(results, numActions, size, System.currentTimeMillis() - time, System.out);
        } catch (IOException e) {
            log.error("Error while running bench\n", e);
        }
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        final Options ops = new Options();
        ops.addOption(OptionBuilder.withArgName("larch-url")
                .withDescription("The URL of the Larch instance")
                .withLongOpt("larch-url")
                .hasArg()
                .create('l'));
        ops.addOption(OptionBuilder.withArgName("action")
                .withDescription("The action to perform")
                .withLongOpt("action")
                .hasArg()
                .create('a'));
        ops.addOption(OptionBuilder.withArgName("num-actions")
                .withDescription("The number of Actions to perform")
                .withLongOpt("num-actions")
                .hasArg()
                .create('n'));
        ops.addOption(OptionBuilder.withArgName("size")
                .withDescription("The size of the individual binaries created")
                .withLongOpt("size")
                .hasArg()
                .create('s'));
        ops.addOption(OptionBuilder.withArgName("num-threads")
                .withDescription("The number of threads used")
                .withLongOpt("num-threads")
                .hasArg()
                .create('t'));
        ops.addOption(OptionBuilder.withArgName("help")
                .withDescription("Print usage information")
                .withLongOpt("help")
                .create('h'));
        ops.addOption(OptionBuilder.withArgName("user")
                .withDescription("The larch user")
                .withLongOpt("user")
                .hasArg()
                .create('u'));
        ops.addOption(OptionBuilder.withArgName("password")
                .withDescription("The larch user's password")
                .withLongOpt("password")
                .hasArg()
                .create('p'));
        return ops;
    }

    private static long getSizeFromArgument(final String optionValue) {
        final Matcher m = Pattern.compile("^(\\d*)([kKmMgGtT]{0,1})$").matcher(optionValue);
        if (!m.find()) {
            throw new IllegalArgumentException("Size " + optionValue + " could not be parsed");
        }
        final long size = Long.parseLong(m.group(1));
        if (m.groupCount() == 1) {
            return size;
        }
        final char postfix = m.group(2).charAt(0);
        switch (postfix) {
            case 'k':
            case 'K':
                return size * 1024l;
            case 'm':
            case 'M':
                return size * 1024l * 1024l;
            case 'g':
            case 'G':
                return size * 1024l * 1024l * 1024l;
            case 't':
            case 'T':
                return size * 1024l * 1024l * 1024l * 1024l;
            default:
                return size;
        }
    }

    public static void printUsage(final Options ops) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BenchTool", ops);
        System.out.println("\n\nExamples:\n");
        System.out.println(" * Ingest a single 100mb file:\n   ---------------------------");
        System.out.println("   java -jar larch-benchtool.jar -l http://localhost:8080 -n 1 -a ingest -s 100m\n");
        System.out.println(" * Ingest 20 files of 1gb using 5 threads\n   --------------------------------------");
        System.out.println("   java -jar larch-benchtool.jar -l http://localhost:8080 -n 20 -a ingest -s 1g -t 5\n");
    }

    public static enum Action {
        INGEST
    }
}