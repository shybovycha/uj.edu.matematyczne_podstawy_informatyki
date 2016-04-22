package v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
  
  @Override
  public String toString() {
    return String.format("(%d, %d)", this.x, this.y);
  }
}

class Car {
  public Point pos;
  public int len;
  public char dir, id;

  public Car(char id, int x, int y, char direction, int length) {
    this.id = id;
    this.pos = new Point(x, y);
    this.len = length;
    this.dir = direction;
  }

  public boolean checkCollision(Point p) {
    return (dir == 'V' && p.x == pos.x && p.y >= pos.y && p.y <= pos.y + len - 1) ||
        (dir == 'H' && p.y == pos.y && p.x >= pos.x && p.x <= pos.x + len - 1);
  }
  
  public Car dup() {
    return new Car(this.id, this.pos.x, this.pos.y, this.dir, this.len);
  }
  
  public Car move(Move move) {
    Car newCar = this.dup();
    
    if (move.dir == 'D')
      newCar.pos.y -= move.d;
    
    if (move.dir == 'U')
      newCar.pos.y += move.d;
    
    if (move.dir == 'L')
      newCar.pos.x -= move.d;
    
    if (move.dir == 'R')
      newCar.pos.x += move.d;
    
    return newCar;
  }
  
  @Override
  public String toString() {
    return String.format("%c [%c] %s", this.id, this.dir, this.pos.toString());
  }
}

class Move {
  public Car car;
  public char dir;
  public int d;
  
  public Move(Car car, char direction, int distance) {
    this.car = car;
    this.dir = direction;
    this.d = distance;
  }
  
  @Override
  public String toString() {
    return String.format("%c %c %d", this.car.id, this.dir, this.d);
  }
}

class Field {
  public List<Car> cars;
  
  public Field(List<Car> cars) {
    this.cars = new ArrayList<>();
    this.cars.addAll(cars);
  }
  
  public List<Move> possibleMoves(Car car) {
    List<Car> otherCars = this.cars.stream().filter(c -> c.id != car.id).collect(Collectors.toList());
    List<Move> moves = new ArrayList<>();
    
    if (car.dir == 'V') {
      for (int py = car.pos.y; py > -1; --py) {
        Point p = new Point(car.pos.x, py);
        
        if (otherCars.stream().anyMatch(c -> c.checkCollision(p)))
          break;
        
        moves.add(new Move(car, 'D', car.pos.y - py));
      }
      
      for (int py = car.pos.y + car.len; py < 6; ++py) {
        Point p = new Point(car.pos.x, py);

        if (otherCars.stream().anyMatch(c -> c.checkCollision(p)))
          break;
        
        moves.add(new Move(car, 'U', py - car.pos.y - car.len + 1));
      }
    } else {
      for (int px = car.pos.x - 1; px > -1; --px) {
        Point p = new Point(px, car.pos.y);
        
        if (otherCars.stream().anyMatch(c -> c.checkCollision(p)))
          break;
        
        moves.add(new Move(car, 'L', car.pos.x - px));
      }
      
      for (int px = car.pos.x + car.len; px < 6; ++px) {
        Point p = new Point(px, car.pos.y);
        
        if (otherCars.stream().anyMatch(c -> c.checkCollision(p)))
          break;
        
        moves.add(new Move(car, 'R', px - car.pos.x - car.len + 1));
      }
    }
    
    return moves;
  }
  
  public Field applyMove(Move move) {
    return new Field(this.cars.stream().map(c -> (c.id == move.car.id) ? c.move(move) : c.dup()).collect(Collectors.toList()));
    
    /*
    List<Car> newCars = new ArrayList<>();
    
    for (Car c : this.cars) {
      if (c.id != move.car.id)
        newCars.add(c.dup()); else
          newCars.add(c.move(move));
    }
    
    return new Field(newCars);*/
  }
  
  protected Car getXCar() {
    return this.cars.stream().filter(c -> c.id == 'X').findFirst().get();
  }
  
  public boolean isSolved() {
    return getXCar().checkCollision(new Point(5, 3));
  }
  
  public boolean isAnyOverlap(Point p) {
    return this.cars.stream().anyMatch(c -> c.checkCollision(p));
  }
  
  public List<Move> allPossibleMoves() {
      Car xCar = this.getXCar();
      
      if (xCar.dir == 'H') {
        if (IntStream.range(xCar.pos.x + xCar.len, 6).noneMatch(x -> this.isAnyOverlap(new Point(x, 3)))) {
          return Arrays.asList(new Move(xCar, 'R', 5 - xCar.pos.x - xCar.len + 1));
        }
      } else {
        if (xCar.pos.y > 3 && xCar.len == 1 && !this.isAnyOverlap(new Point(5, 4))) 
          return Arrays.asList(new Move(xCar, 'U', 2));
        else if (xCar.pos.y < 3 && IntStream.range(xCar.pos.y + xCar.len, 4).noneMatch(y -> this.isAnyOverlap(new Point(5, y))))
          return Arrays.asList(new Move(xCar, 'D', 3 - xCar.pos.y - xCar.len + 1));
      }
      
      return this.cars.stream().map(car -> this.possibleMoves(car)).flatMap(l -> l.stream()).collect(Collectors.toList());
  }
  
  @Override
  public String toString() {
    return this.cars.stream().map(Car::toString).collect(Collectors.joining(";"));
  }
  
  @Override
  public boolean equals(Object other) {
    return (other != null) && (other instanceof Field) && (other.toString().equals(this.toString()));
  }
}

class Solver {
  public static List<Move> solve(Field field, List<Move> prevMoves, List<Field> prevFields) {
    List<Move> result = new ArrayList<>();
    
    if (prevFields.contains(field))
      return result;
    
    if (field.isSolved())
      return prevMoves;
    
    List<Move> moves = field.allPossibleMoves();
    prevFields.add(field);
    
    for (Move move : moves) {
      List<Move> newMoves = new ArrayList<Move>();
      newMoves.addAll(prevMoves);
      newMoves.add(move);
      
      List<Move> ms = solve(field.applyMove(move), newMoves, prevFields);
      
      if (!ms.isEmpty())
        return ms;
    }
    
    return result;
  }
  
  public static List<Move> solve(Field field) {
    return solve(field, new ArrayList<Move>(), new ArrayList<Field>());
  }
}

public class Application {
  public static void main(String[] args) throws Exception {
    List<Car> cars =
        Arrays.asList(
            new Car('X', 0, 3, 'H', 2), new Car('A', 4, 1, 'H', 2), new Car('C', 4, 2, 'V', 3));
    
    Field field = new Field(cars);
    
    List<Move> moves = Solver.solve(field);
    
    System.out.printf(moves.stream().map(Move::toString).collect(Collectors.joining("\n")));
  }
}
