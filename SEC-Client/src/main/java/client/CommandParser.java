package client;

import client.commands.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Pattern;

public class CommandParser {
    /**
     * Verifies if the given line contains an open command
     * @param line A line of text
     * @return an Optional that might contain or not an OpenCommand
     */
    private static Optional<Command> open(String line) {
        var regex = "open (.+)";
        var p = Pattern.compile(regex);
        var m = p.matcher(line);
        if (m.matches()) {
            return Optional.of(new OpenCommand(m.group(1)));
        } else {
            return Optional.empty();
        }
    }


    /**
     * Verifies if the given line contains an check command
     * @param line A line of text
     * @return an Optional that might contain or not an CheckCommand
     */

    private static Optional<Command> check(String line) {
        var regex = "check (.+)";
        var p = Pattern.compile(regex);
        var m = p.matcher(line);
        if (m.matches()) {
            return Optional.of(new CheckCommand(m.group(1)));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Verifies if the given line contains an send command
     * @param line A line of text
     * @return an Optional that might contain or not an SendCommand
     */

    private static Optional<Command> send(String line) {
        var regex = "send (.+) (.+) ([0-9]+)";
        var p = Pattern.compile(regex);
        var m = p.matcher(line);
        if (m.matches()) {
            var amount = Integer.parseInt(m.group(3));
            return Optional.of(new SendCommand(m.group(1), m.group(2), amount));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Verifies if the given line contains an receive command
     * @param line A line of text
     * @return an Optional that might contain or not an ReceiveCommand
     */

    private static Optional<Command> receive(String line) {
        var regex = "receive (.+) (.+)";
        var p = Pattern.compile(regex);
        var m = p.matcher(line);
        if (m.matches()) {
            return Optional.of(new ReceiveCommand(m.group(1), m.group(2)));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Verifies if the given line contains an receive command
     * @param line A line of text
     * @return an Optional that might contain or not an ReceiveCommand
     */

    private static Optional<Command> audit(String line) {
        var regex = "audit (.+)";
        var p = Pattern.compile(regex);
        var m = p.matcher(line);
        if (m.matches()) {
            return Optional.of(new AuditCommand(m.group(1)));
        } else {
            return Optional.empty();
        }
    }


    /**
     * Parse a command from a line
     * @return A command parsed from the line
     * @throws Exception If no command can be obtained.
     */

    public static Command parseCommand() throws Exception {
        var commands = "open <KEY> \ncheck <KEY> \nsend <SENDER KEY> <RECEIVER KEY> <AMOUNT> \nreceive <SENDER KEY> <RECEIVER KEY> \naudit <KEY>";
        System.out.println("Commands:\n" + commands);

        var reader = new BufferedReader(new InputStreamReader(System.in));
        var line = reader.readLine();
        var command = open(line)
                .or(() -> check(line))
                .or(() -> send(line))
                .or(() -> receive(line))
                .or(() -> audit(line))
                .orElseThrow(RuntimeException::new);
        return command;
    }
}
