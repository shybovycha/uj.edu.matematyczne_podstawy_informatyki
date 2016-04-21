package v1;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by shybovycha on 19/04/16.
 */
class Point {
    public int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

enum Position {
    VERTICAL,
    HORIZONTAL
}

enum Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT
}

class Car {
    public char id;
    public Point startsAt;
    public Position position;
    public int length;

    public Car(char id, int x, int y, char direction, int length) {
        this.id = id;
        this.startsAt = new Point(x, y);

        if (direction == 'H')
            this.position = Position.HORIZONTAL;
        else if (direction == 'V')
            this.position = Position.VERTICAL;

        this.length = length;
    }

    public boolean isOverlap(int x, int y) {
        if (this.position == Position.HORIZONTAL) {
            return (y == this.startsAt.y) && (x >= this.startsAt.x) && (x <= this.startsAt.x + this.length);
        } else {
            return (x == this.startsAt.x) && (y >= this.startsAt.y) && (y <= this.startsAt.y + this.length);
        }
    }
}

class ProblemSolver {
    private List<Car> cars;
    private List<String> solution;

    public ProblemSolver(List<Car> cars) {
        this.cars = new ArrayList<>();
        this.cars.addAll(cars);

        this.solution = new ArrayList<>();
    }

    public List<String> solve() {
        return this.solution;
    }

    // TODO: REWORK
    private Direction findBestDirection(Car source) {
        return Direction.UP;
    }

    private Set<Car> findObstacles(Car source, Direction direction) {
        Set<Car> obstacles = new HashSet<>();

        if (source.position == Position.HORIZONTAL) {
            if (direction == Direction.LEFT) {
                for (int x = source.startsAt.x; x > -1; --x) {
                    obstacles.addAll(this.checkObstaclesAt(x, source.startsAt.y));
                }
            }

            if (direction == Direction.RIGHT) {
                for (int x = source.startsAt.x + source.length; x < 6; ++x) {
                    obstacles.addAll(this.checkObstaclesAt(x, source.startsAt.y));
                }
            }
        } else {
            if (direction == Direction.UP) {
                for (int y = source.startsAt.y; y > -1; --y) {
                    obstacles.addAll(this.checkObstaclesAt(source.startsAt.x, y));
                }
            }

            if (direction == Direction.DOWN) {
                for (int y = source.startsAt.y + source.length; y < 6; ++y) {
                    obstacles.addAll(this.checkObstaclesAt(source.startsAt.x, y));
                }
            }
        }

        return obstacles;
    }

    private Set<Car> checkObstaclesAt(int x, int y) {
        return cars.stream().filter(c -> c.isOverlap(x, y)).collect(Collectors.toSet());
    }
}

class ProblemInputReader {
    private Scanner scanner;
    private Integer testCases;
    private Integer currentTestCase;
    private Pattern pattern;

    public ProblemInputReader(InputStream inputStream) {
        this.scanner = new Scanner(inputStream);

        // [id] - identyfikatorem samochodu jako duża litera
        // [start point] - punkt znajdujący się najblizej w pionie i poziomie do punktu (0,0)
        // [direction] - polozenie samochodu wartosci V albo H
        // [length] - dlugość samochodu
        this.pattern = Pattern.compile("^([A-Z]) ([0-5]) ([0-5]) ([VH]) ([1-6])$");
    }

    public int testCases() {
        if (this.testCases == null) {
            this.testCases = scanner.nextInt();
            this.currentTestCase = 0;
        }

        return this.testCases;
    }

    public boolean hasTestCases() {
        return this.currentTestCase < this.testCases;
    }

    public List<Car> nextTestCase() {
        List<Car> cars = new ArrayList<>();

        int n = scanner.nextInt();

        for (int i = 0; i < n; ++i) {
            String s = scanner.nextLine();
            Matcher matcher = this.pattern.matcher(s);

            if (!matcher.matches())
                throw new RuntimeException("Was not able understand input line format");

            char id = matcher.group(1).charAt(0); // id
            int x = Integer.parseInt(matcher.group(2)); // x
            int y = Integer.parseInt(matcher.group(3)); // y
            char direction = matcher.group(4).charAt(0); // direction
            int length = Integer.parseInt(matcher.group(5)); // length

            cars.add(new Car(id, x, y, direction, length));
        }

        this.currentTestCase++;

        return cars;
    }
}

class SolverRunner {
    private ProblemInputReader inputReader;

    public SolverRunner(InputStream inputStream) {
        this.inputReader = new ProblemInputReader(inputStream);
    }

    public void run() {
        while (this.inputReader.hasTestCases()) {
            List<Car> cars = inputReader.nextTestCase();

            ProblemSolver solver = new ProblemSolver(cars);

            solver.solve();
        }
    }
}

public class ApplicationRunner {
    public static void main(String[] args) {
        SolverRunner runner = new SolverRunner(System.in);

        runner.run();
    }
}
