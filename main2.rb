field = [
    {id: 'X', pos: {x: 0, y: 3}, len: 2, dir: :h},
    {id: 'A', pos: {x: 4, y: 1}, len: 2, dir: :h},
    {id: 'C', pos: {x: 4, y: 2}, len: 3, dir: :v}
]

def point_overlap?(p, car)
  (car[:dir] == :v and p[:x] == car[:pos][:x] and p[:y] >= car[:pos][:y] and p[:y] <= car[:pos][:y] + car[:len] - 1) or
      (car[:dir] == :h and p[:y] == car[:pos][:y] and p[:x] >= car[:pos][:x] and p[:x] <= car[:pos][:x] + car[:len] - 1)
end

def possible_moves(car, field)
  other_cars = field.reject { |c| c == car }
  moves = []

  if car[:dir] == :v
    (car[:pos][:y] - 1).downto(0) do |py|
      p = {x: car[:pos][:x], y: py}
      obstacles = other_cars.select { |c| point_overlap?(p, c) }

      break unless obstacles.empty?

      moves << {car: car, dir: :down, d: car[:pos][:y] - py} if obstacles.empty?
    end

    (car[:pos][:y] + car[:len]).upto(5) do |py|
      p = {x: car[:pos][:x], y: py}
      obstacles = other_cars.select { |c| point_overlap?(p, c) }

      break unless obstacles.empty?

      moves << {car: car, dir: :up, d: py - car[:pos][:y] - car[:len] + 1} if obstacles.empty?
    end
  else
    (car[:pos][:x] - 1).downto(0) do |px|
      p = {x: px, y: car[:pos][:y]}
      obstacles = other_cars.select { |c| point_overlap?(p, c) }

      break unless obstacles.empty?

      moves << {car: car, dir: :left, d: car[:pos][:x] - px} if obstacles.empty?
    end

    (car[:pos][:x] + car[:len]).upto(5) do |px|
      p = {x: px, y: car[:pos][:y]}
      obstacles = other_cars.select { |c| point_overlap?(p, c) }

      break unless obstacles.empty?

      moves << {car: car, dir: :right, d: px - car[:pos][:x] - car[:len] + 1} if obstacles.empty?
    end
  end

  moves
end

def apply_move(field, move)
  field.map do |car|
    c = {id: car[:id], pos: {x: car[:pos][:x], y: car[:pos][:y]}, dir: car[:dir], len: car[:len]}

    next c if car[:id] != move[:car][:id]

    c[:pos][:y] -= move[:d] if move[:dir] == :down
    c[:pos][:y] += move[:d] if move[:dir] == :up
    c[:pos][:x] -= move[:d] if move[:dir] == :left
    c[:pos][:x] += move[:d] if move[:dir] == :right

    c
  end
end

def solved?(field)
  x_car = field.select { |c| c[:id] == 'X' }.first
  point_overlap?({x: 5, y: 3}, x_car)
end

def any_overlap?(p, field)
  field.any? { |c| point_overlap?(p, c) }
end

def all_possible_moves(field)
  x_car = field.select { |c| c[:id] == 'X' }.first

  if x_car[:dir] == :h
    if ((x_car[:pos][:x] + x_car[:len])..5).to_a.none? { |x| any_overlap?({x: x, y: 3}, field) }
      return [{car: x_car, dir: :right, d: 5 - x_car[:pos][:x] - x_car[:len] + 1}]
    end
  else
    if x_car[:pos][:y] > 3 and x_car[:len] == 1 and not any_overlap?({x: 5, y: 4}, field)
      return [{car: x_car, dir: :down, d: 2}]
    elsif x_car[:pos][:y] < 3 and ((x_car[:pos][:y] + x_car[:len])..3).to_a.none? { |y| any_overlap?({x: 5, y: y}, field) }
      return [{car: x_car, dir: :up, d: 3 - x_car[:pos][:y] - x_car[:len] + 1}]
    end
  end

  field.inject([]) { |acc, car| acc += possible_moves(car, field) }
end

def visualize_moves(moves)
  move2s = {up: 'U', right: 'R', down: 'D', left: 'L'}

  moves.map do |move|
    "#{move[:car][:id]} #{move2s[move[:dir]]} #{move[:d]}"
  end.join("\n")
end

def solve(field, prev_moves = [], prev_fields = [])
  return nil if prev_fields.include? field
  return prev_moves if solved?(field)

  moves = all_possible_moves(field)

  prev_fields << field

  moves.each do |move|
    ms = solve(apply_move(field, move), prev_moves + [move], prev_fields)

    return ms unless ms.nil?
  end

  nil
end

puts visualize_moves(solve(field))
