/* 
 * Copyright 2016 João Bosco Monteiro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.mobclip;

import br.com.mobclip.kafka.ExtractionStarter;
import br.com.mobclip.kafka.PageDetailExtractor;
import br.com.mobclip.kafka.PageExtractor;
import br.com.mobclip.kafka.PropertyPersister;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class Application implements CommandLineRunner {

    @Autowired
    private ExtractionStarter starter;

    @Autowired
    private PropertyPersister persister;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Options options = createOptions();

        String mode = null;
        int threads = 1;

        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("mobclip", options);
                System.exit(0);
            }

            mode = line.getOptionValue("mode", "starter");
            threads = Integer.valueOf(line.getOptionValue("threads", "1"));

        } catch (Exception exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            System.exit(0);
        }

        switch (mode) {

            case "starter":
                starter.configure(2, 2);
                submitTasks(1, starter);
                break;
            case "persister":
                submitTasks(threads, persister);
                break;
            case "page-extractor":
                submitTasks(threads, new PageExtractor());
                break;
            case "page-detail-extractor":
                submitTasks(threads, new PageDetailExtractor());
                break;

            case "standalone":
                submitTasks(threads, persister);
                submitTasks(threads, new PageExtractor());
                submitTasks(threads, new PageDetailExtractor());
                starter.configure(2, 2);
                submitTasks(1, starter);
                break;

            default:
                System.out.println("Invalid application mode: " + mode);
        }
    }

    public void submitTasks(int threads, Runnable runnable) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.submit(runnable);
        }
    }

    public Options createOptions() {
        Options options = new Options();
        options.addOption("mode", true, "define which module should run");
        options.addOption("threads", true, "Number of threads for the pool");
        options.addOption("help", "help");
        return options;
    }
}
