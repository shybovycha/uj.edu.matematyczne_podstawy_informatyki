field = [
    {id: 'X', pos: {x: 0, y: 3}, len: 2, dir: :h},
    {id: 'A', pos: {x: 4, y: 1}, len: 2, dir: :h},
    {id: 'C', pos: {x: 4, y: 2}, len: 3, dir: :v}
]

def point_overlap?(p, car)
  (car[:dir] == :v and p[:x] == car[:pos][:x] and p[:y] >= car[:pos][:y] and p[:y] <= car[:pos][:y] + car[:len] - 1) or
      (car[:dir] == :h and p[:y] == car[:pos][:y] and p[:x] >= car[:pos][:x] and p[:x] <= car[:pos][:x] + car[:len] - 1)
end

def car_overlap?(car1, car2)
  x, y, l = car1[:pos][:x], car1[:pos][:y], car1[:len]

  if car1[:dir] == :v
    y.upto(y + l) do |py|
      return true if point_overlap?({x: x, y: py}, car2)
    end
  else
    x.upto(x + l) do |px|
      return true if point_overlap?({x: px, y: y}, car2)
    end
  end

  false
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

def visualize_field(field)
  cells = []

  6.times { cells << [' '] * 6 }

  field.each do |car|
    if car[:dir] == :h
      car[:pos][:x].upto(car[:pos][:x] + car[:len] - 1) { |x| cells[car[:pos][:y]][x] = car[:id] }
    else
      car[:pos][:y].upto(car[:pos][:y] + car[:len] - 1) { |y| cells[y][car[:pos][:x]] = car[:id] }
    end
  end

  lines = []

  lines << '+' + (['-'] * 6).join('+') + '+'

  cells.each do |row|
    lines << '|' + row.join('|') + '|'
    lines << '+' + (['-'] * 6).join('+') + '+'
  end

  lines.reverse.join("\n")
end

def visualize_moves(moves)
  move2s = {up: 'U', right: 'R', down: 'D', left: 'L'}

  moves.map do |move|
    "#{move[:car][:id]} #{move2s[move[:dir]]} #{move[:d]}"
  end.join("\n")
end

def solve(field, prev_moves = [], prev_fields = [])
  #puts "PREV_MOVES: #{prev_moves.inspect};"
  #puts "#{visualize_field field}" #; PREV_FIELDS: #{prev_fields.inspect}"

  return nil if prev_fields.include? field

  return prev_moves if solved?(field)

  moves = all_possible_moves(field)

  # new_prev_fields = prev_fields + [field]
  prev_fields << field

  moves.each do |move|
    ms = solve(apply_move(field, move), prev_moves + [move], prev_fields)

    return ms unless ms.nil?
  end

  nil
end

# C(down 1) vs A
# puts car_overlap?({pos: {x: 4, y: 1}, len: 3, dir: :v}, {pos: {x: 4, y: 1}, len: 2, dir: :h})

# A vs C(down 2)
# puts car_overlap?({pos: {x: 4, y: 0}, len: 3, dir: :v}, {pos: {x: 4, y: 1}, len: 2, dir: :h})

# possible moves for car C
# puts possible_moves(field[2], field).inspect
# puts possible_moves(field[0], field).inspect

# puts "BEFORE: #{field.inspect}"
# field2 = apply_move(field, {car: field[0], dir: :right, d: 1})
# puts "RESULT: #{field2.inspect}"
# puts "AFTER: #{field.inspect}"

# puts possible_moves(field[1], field).inspect
# puts apply_move(field, {:car=>{:id=>"A", :pos=>{:x=>4, :y=>1}, :len=>2, :dir=>:h}, :dir=>:left, :d=>2}).inspect

# puts solve([{id: 'X', pos: {x: 0, y: 3}, len: 2, dir: :h}, {id: 'A', pos: {x: 4, y: 1}, len: 2, dir: :v}]).inspect

puts visualize_moves(solve(field))
# puts visualize_field(field)