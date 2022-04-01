package client;

import client.commands.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Pattern;

public class CommandParser {
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

    public static void parseCommand(ClientSide clientSide) throws Exception {
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
        command.execCommand(clientSide);
    }
}
