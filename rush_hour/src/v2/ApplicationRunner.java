package v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Point {
    public int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point subtract(Point other) {
        return new Point(this.x - other.x, this.y - other.y);
    }

    public int length() {
        return (int) Math.round(Math.sqrt(this.x * this.x - this.y * this.y));
    }
}

class Car {
    public Point pos;
    public int length;
    public char direction, id;
    // public Point target;
    public boolean visited = false;

    public Car(char id, int x, int y, char direction, int length) {
        this.id = id;
        this.pos = new Point(x, y);
        this.length = length;
        this.direction = direction;
    }

    public boolean checkCollision(Point p) {
        if (this.direction == 'V') {
            return (this.pos.x == p.x) && (p.y >= this.pos.y) && (p.y <= this.pos.y + this.length);
        } else if (this.direction == 'H') {
            return (this.pos.y == p.y) && (p.x >= this.pos.x) && (p.x <= this.pos.x + this.length);
        }

        return false;
    }
}

abstract class SolutionStepAlgorithm {
    public Car car;
    public Field field;
    public Point target;

    protected Point direction;
    protected Point start;
    protected List<Car> obstacles;

    public SolutionStepAlgorithm(Car car, Field field, Point target) {
        this.car = car;
        this.field = field;
        this.target = target;
        this.start = new Point(car.pos.x, car.pos.y);
        this.direction = null;
    }

    protected abstract void findMoveDirection();

    protected abstract void findObstacles();

    public Point getMoveDirection() {
        if (this.direction == null) this.findMoveDirection();

        return this.direction;
    }

    public List<Car> getObstacles() {
        if (this.obstacles == null) this.findObstacles();

        return this.obstacles;
    }

    protected String formatMove() {
        char moveDir = '?';

        if (this.direction.x > 0) moveDir = 'R';
        else if (this.direction.x < 0) moveDir = 'L';
        else if (this.direction.y < 0) moveDir = 'D';
        else if (this.direction.y > 0) moveDir = 'U';

        int moveLen = this.target.subtract(this.start).length();

        return String.format("%c %c %d", this.car.id, moveDir, moveLen);
    }

    // TODO: IMPLEMENT!
    public Point getNewTargetFor(Car obstacle) throws Exception {
        if (obstacle.direction == this.car.direction)
            throw new Exception("Endless loop - obstacle could not be moved so that car can pass");

        if (obstacle.direction == 'V') {
            List<Point> candidates =
                    Arrays.asList(
                            new Point(obstacle.pos.x, this.car.pos.y + 1 - obstacle.pos.y + 1),
                            new Point(obstacle.pos.x, this.car.pos.y + 1 - obstacle.pos.y - obstacle.length + 1),
                            new Point(obstacle.pos.x, this.car.pos.y - 1 - obstacle.pos.y + 1),
                            new Point(obstacle.pos.x, this.car.pos.y - 1 - obstacle.pos.y - obstacle.length + 1));

            return candidates
                    .stream()
                    .sorted((c1, c2) -> c1.length() - c2.length())
                    .filter(c -> c.y > -1 && c.y < 6)
                    .findFirst()
                    .get();
        } else {
            List<Point> candidates =
                    Arrays.asList(
                            new Point(this.car.pos.x + 1 - obstacle.pos.x, obstacle.pos.y + 1),
                            new Point(this.car.pos.x + 1 - obstacle.pos.x - obstacle.length + 1, obstacle.pos.y),
                            new Point(this.car.pos.x - 1 - obstacle.pos.x, obstacle.pos.y + 1),
                            new Point(this.car.pos.x - 1 - obstacle.pos.x - obstacle.length + 1, obstacle.pos.y));

            return candidates
                    .stream()
                    .sorted((c1, c2) -> c1.length() - c2.length())
                    .filter(c -> c.x > -1 && c.x < 6)
                    .findFirst()
                    .get();
        }
    }
}

class VerticalSolutionStep extends SolutionStepAlgorithm {
    public VerticalSolutionStep(Car car, Field field, Point target) {
        super(car, field, target);
    }

    @Override
    protected void findMoveDirection() {
        this.direction = new Point(0, 0);

        int dy1 = target.y - car.pos.y;
        int dy2 = target.y - (car.pos.y + car.length - 1);
        int dy;

        // worth checking, if the new position will overlap with the given one
        if (dy1 < dy2) {
            dy = dy1;
        } else {
            dy = dy2;
            this.start.y += car.length - 1;
        }

        this.direction.y = dy / Math.abs(dy);
    }

    @Override
    protected void findObstacles() {
        this.obstacles = new ArrayList<>();

        for (int y = this.start.y; y > -1 && y < 6; y += this.getMoveDirection().y) {
            for (Car c : this.field.cars) {
                if (c == this.car) continue;

                if (c.checkCollision(new Point(this.car.pos.x, this.car.pos.y + y))) {
                    this.obstacles.add(c);
                }
            }
        }
    }
}

class HorizontalSolutionStep extends SolutionStepAlgorithm {
    public HorizontalSolutionStep(Car car, Field field, Point target) {
        super(car, field, target);
    }

    @Override
    protected void findMoveDirection() {
        this.direction = new Point(0, 0);

        int dx1 = target.x - car.pos.x;
        int dx2 = target.x - (car.pos.x + car.length - 1);
        int dx;

        if (dx1 < dx2) {
            dx = dx1;
        } else {
            dx = dx2;
            this.start.x += car.length - 1;
        }

        this.direction.x = dx / Math.abs(dx);
    }

    @Override
    protected void findObstacles() {
        this.obstacles = new ArrayList<>();

        for (int x = this.start.x; x > -1 && x < 6; x += this.getMoveDirection().x) {
            for (Car c : this.field.cars) {
                if (c == this.car) continue;

                if (c.checkCollision(new Point(this.car.pos.x + x, this.car.pos.y))) {
                    this.obstacles.add(c);
                }
            }
        }
    }
}

class Field {
    public List<Car> cars;

    public Field(List<Car> cars) {
        this.cars = cars;
    }

    protected boolean checkCollision(Point p) {
        for (Car c : this.cars) {
            if (c.checkCollision(p)) return true;
        }

        return false;
    }

    public List<String> moveCar(Car car, Point target, List<String> moves) throws Exception {
        if (car.visited) return moves;

        SolutionStepAlgorithm solver;

        if (car.direction == 'V') {
            solver = new VerticalSolutionStep(car, this, target);
        } else {
            solver = new HorizontalSolutionStep(car, this, target);
        }

        // solver.getMoveDirection();
        List<Car> obstacles = solver.getObstacles();

        moves.add(solver.formatMove());

        car.visited = true;

        for (Car c : obstacles) {
            Point newTarget = solver.getNewTargetFor(c);
            this.moveCar(c, newTarget, moves);
        }

        return moves;
    }
}

public class ApplicationRunner {
    public static void main(String[] args) throws Exception {
        List<Car> cars =
                Arrays.asList(
                        new Car('X', 0, 3, 'H', 2), new Car('A', 4, 1, 'H', 2), new Car('C', 4, 1, 'V', 3));
        Field field = new Field(cars);

        List<String> moves = field.moveCar(cars.get(0), new Point(5, 2), new ArrayList<String>());
    }
}