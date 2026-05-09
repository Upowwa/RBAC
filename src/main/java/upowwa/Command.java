package upowwa;

import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(String arguments, Scanner scanner, RBACSystem system);
}