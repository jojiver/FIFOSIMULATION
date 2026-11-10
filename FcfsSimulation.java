public class FcfsSimulation {
    public static void main(String[] args) {
        // {arrival time, burst time} — already in FCFS arrival order
        int[][] p = { { 0, 5 }, { 1, 3 }, { 2, 8 }, { 3, 6 } };
        int time = 0;
        int totalWait = 0;
        int totalTurn = 0;

        System.out.println("PID Arrival Burst Start Wait Turn Finish");

        for (int i = 0; i < p.length; i++) {
            int arrival = p[i][0];
            int burst = p[i][1];

            int start = Math.max(time, arrival); // CPU idle? start at arrival
            int finish = start + burst;
            int wait = start - arrival;
            int turn = finish - arrival;

            time = finish;
            totalWait += wait;
            totalTurn += turn;

            System.out.println("P" + (i + 1) + "   " + arrival + "       "
                    + burst + "     " + start + "     " + wait + "    "
                    + turn + "     " + finish);
        }

        System.out.printf("Average waiting time: %.2f%n",
                (double) totalWait / p.length);
        System.out.printf("Average turnaround time: %.2f%n",
                (double) totalTurn / p.length);
    }
}
