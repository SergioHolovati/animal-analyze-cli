package com.cli;

import com.cli.command.AnalyzeCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CliApplication {

	public static void main(String[] args) {
		SpringApplication.run(CliApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(AnalyzeCommand analyzeCommand) {
		return args -> analyzeCommand.analyze(args);
	}
}
