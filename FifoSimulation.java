import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * FIFO simulator with two classic OS simulations:
 *   1. FCFS CPU Scheduling (First-In-First-Out process scheduling)
 *   2. FIFO Page Replacement
 *
 * Run interactively:    java FifoSimulation
 * Run the built-in demo: java FifoSimulation --demo
 */
public class FifoSimulation {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            runDemo();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== FIFO Simulator ===");
            System.out.println("1. FCFS CPU Scheduling (FIFO)");
            System.out.println("2. FIFO Page Replacement");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            if ("1".equals(choice)) {
                fcfsInteractive(scanner);
            } else if ("2".equals(choice)) {
                pageReplacementInteractive(scanner);
            } else if ("0".equals(choice)) {
                running = false;
            } else {
                System.out.println("Invalid choice. Enter 0, 1 or 2.");
            }
        }

        scanner.close();
        System.out.println("Goodbye.");
    }

    // ---------------------------------------------------------------------
    // Demo data
    // ---------------------------------------------------------------------
    private static void runDemo() {
        System.out.println("=== DEMO 1: FCFS CPU Scheduling (FIFO) ===");
        System.out.println("Processes: P1(0,5) P2(1,3) P3(2,8) P4(3,6)");
        System.out.println("Format: (arrival time, burst time)");
        System.out.println();
        fcfs(new int[][] {
                { 1, 0, 5 },
                { 2, 1, 3 },
                { 3, 2, 8 },
                { 4, 3, 6 }
        });

        System.out.println();
        System.out.println("=== DEMO 2: FIFO Page Replacement (3 frames) ===");
        System.out.println("Reference string: 7 0 1 2 0 3 0 4 2 3 0 3 2");
        System.out.println();
        pageReplacement(new int[] { 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2 }, 3);
    }

    // ---------------------------------------------------------------------
    // 1) FCFS CPU Scheduling (non-preemptive FIFO)
    // ---------------------------------------------------------------------
    private static class Process {
        int id;
        int arrival;
        int burst;
        int start;
        int completion;
        int waiting;
        int turnaround;
    }

    private static void fcfsInteractive(Scanner scanner) {
        int count = readInt(scanner, "Number of processes (1-100): ", 1, 100);
        int[][] data = new int[count][3];

        for (int i = 0; i < count; i++) {
            int id = i + 1;
            int arrival = readInt(scanner, "P" + id + " arrival time: ", 0, 100000);
            int burst = readInt(scanner, "P" + id + " burst time: ", 1, 100000);
            data[i] = new int[] { id, arrival, burst };
        }

        fcfs(data);
    }

    private static void fcfs(int[][] data) {
        if (data.length == 0) {
            System.out.println("No processes to simulate.");
            return;
        }

        Process[] processes = new Process[data.length];
        for (int i = 0; i < data.length; i++) {
            Process p = new Process();
            p.id = data[i][0];
            p.arrival = data[i][1];
            p.burst = data[i][2];
            processes[i] = p;
        }

        // FIFO: the process that arrives first gets the CPU first.
        Arrays.sort(processes, new Comparator<Process>() {
            @Override
            public int compare(Process a, Process b) {
                if (a.arrival != b.arrival) {
                    return Integer.compare(a.arrival, b.arrival);
                }
                return Integer.compare(a.id, b.id);
            }
        });

        int clock = 0;
        double totalWaiting = 0;
        double totalTurnaround = 0;

        System.out.printf("%-5s %-9s %-8s %-8s %-9s %-11s %-8s%n",
                "PID", "Arrival", "Burst", "Start", "Waiting", "Turnaround", "Finish");
        System.out.println("--------------------------------------------------------------");

        for (Process p : processes) {
            p.start = Math.max(clock, p.arrival);
            p.completion = p.start + p.burst;
            p.waiting = p.start - p.arrival;
            p.turnaround = p.completion - p.arrival;
            clock = p.completion;

            totalWaiting += p.waiting;
            totalTurnaround += p.turnaround;

            System.out.printf("%-5s %-9d %-8d %-8d %-9d %-11d %-8d%n",
                    "P" + p.id, p.arrival, p.burst, p.start, p.waiting,
                    p.turnaround, p.completion);
        }

        StringBuilder gantt = new StringBuilder();
        for (int i = 0; i < processes.length; i++) {
            Process p = processes[i];
            if (i > 0) {
                gantt.append(" -> ");
            }
            gantt.append("P").append(p.id).append(" [")
                    .append(p.start).append("-").append(p.completion).append("]");
        }

        System.out.println();
        System.out.println("Gantt Chart: " + gantt);
        System.out.printf("Average Waiting Time    : %.2f%n", totalWaiting / processes.length);
        System.out.printf("Average Turnaround Time : %.2f%n", totalTurnaround / processes.length);
    }

    // ---------------------------------------------------------------------
    // 2) FIFO Page Replacement
    // ---------------------------------------------------------------------
    private static void pageReplacementInteractive(Scanner scanner) {
        int frameCount = readInt(scanner, "Number of frames (1-50): ", 1, 50);
        System.out.print("Reference string (example 7,0,1,2,0,3): ");
        int[] pages = parsePages(scanner.nextLine());

        if (pages.length == 0) {
            System.out.println("No valid pages in the reference string.");
            return;
        }

        pageReplacement(pages, frameCount);
    }

    private static void pageReplacement(int[] pages, int frameCount) {
        if (frameCount <= 0 || pages.length == 0) {
            System.out.println("Invalid simulation input.");
            return;
        }

        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);
        LinkedList<Integer> queue = new LinkedList<Integer>();

        int faults = 0;
        int hits = 0;

        System.out.printf("%-5s %-22s %-8s%n", "Step", "Frames", "Result");
        System.out.println("---------------------------------------------");

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean hit = false;

            for (int frame : frames) {
                if (frame == page) {
                    hit = true;
                    break;
                }
            }

            if (hit) {
                hits++;
            } else {
                faults++;

                int emptyIndex = -1;
                for (int f = 0; f < frames.length; f++) {
                    if (frames[f] == -1) {
                        emptyIndex = f;
                        break;
                    }
                }

                if (emptyIndex != -1) {
                    frames[emptyIndex] = page;
                } else {
                    int victim = queue.removeFirst();
                    for (int f = 0; f < frames.length; f++) {
                        if (frames[f] == victim) {
                            frames[f] = page;
                            break;
                        }
                    }
                }

                queue.addLast(page);
            }

            System.out.printf("%-5d %-22s %-8s%n",
                    i + 1, Arrays.toString(frames), hit ? "HIT" : "FAULT");
        }

        int length = pages.length;
        double faultRate = (faults * 100.0) / length;

        System.out.println();
        System.out.println("Total references : " + length);
        System.out.println("Page hits        : " + hits);
        System.out.println("Page faults      : " + faults);
        System.out.printf("Fault rate       : %.2f%%%n", faultRate);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static int[] parsePages(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new int[0];
        }

        String[] parts = line.trim().split("[,\\s]+");
        List<Integer> values = new ArrayList<Integer>();

        for (String part : parts) {
            try {
                values.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
                // Skip tokens that are not whole numbers.
            }
        }

        int[] pages = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            pages[i] = values.get(i);
        }
        return pages;
    }
}
